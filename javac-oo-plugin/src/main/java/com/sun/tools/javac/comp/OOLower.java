/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 
 * GNU Public License v2.0 + OpenJDK assembly exception.
 * 
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 *     some code from from OpenJDK langtools (GPL2 + assembly exception)
 ******************************************************************************/
package com.sun.tools.javac.comp;

import com.sun.source.tree.Tree;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;

import java.lang.reflect.Field;

import static com.sun.tools.javac.jvm.ByteCodes.bool_not;

public class OOLower extends Lower {
    public static OOLower hook(Context context) {
        context.put(lowerKey, (Lower)null);
        return new OOLower(context);
    }
    protected OOLower(Context context) {
        super(context);
        syms = Symtab.instance(context);
        types = Types.instance(context);
        make = TreeMaker.instance(context);
        rs = Resolve.instance(context);
        cfolder = ConstFold.instance(context);
    }
    protected Types types;
    protected Symtab syms;
    protected TreeMaker make;
    protected Resolve rs;
    protected ConstFold cfolder;
    protected JCDiagnostic.DiagnosticPosition getMake_pos() {
        try {
            Field f = Lower.class.getDeclaredField("make_pos");
            f.setAccessible(true);
            return (JCDiagnostic.DiagnosticPosition) f.get(this);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitIndexed(JCArrayAccess tree) {
        if (types.isArray(tree.indexed.type)) {
            tree.indexed = translate(tree.indexed);
            tree.index = translate(tree.index, syms.intType);
            result = tree;
        } else {
            result = translate(tree.indexed);
        }
    }

    @Override
    public void visitAssign(JCAssign tree) {
        JCTree waslhs = tree.lhs;
        tree.lhs = translate(tree.lhs, tree);
        tree.rhs = translate(tree.rhs, tree.lhs.type);

        // If translated left hand side is an Apply, we are
        // seeing an access method invocation. In this case, append
        // right hand side as last argument of the access method.
        if (tree.lhs.getTag() == JCTree.APPLY) {
            JCMethodInvocation app = (JCMethodInvocation)tree.lhs;
            app.args = List.of(tree.rhs).prependList(app.args);
            result = app;
        } else if (waslhs.getKind() == Tree.Kind.ARRAY_ACCESS && !types.isArray(((JCArrayAccess)waslhs).indexed.type)) {
            result = tree.lhs;
        } else {
            result = tree;
        }
    }

    @Override
    public void visitBinary(JCBinary tree) {
        if (tree.operator instanceof Symbol.OperatorSymbol) {
            Symbol.OperatorSymbol os = (Symbol.OperatorSymbol) tree.operator;
            if (os.opcode == ByteCodes.error+1) { // if operator overloading?
                Symbol.MethodSymbol ms = (Symbol.MethodSymbol) os.owner;
                // construct method invocation ast
                JCFieldAccess meth = make.Select(tree.lhs, ms.name);
                meth.type = ms.type;
                meth.sym = ms;
                result = make.Apply(null, meth, List.of(tree.rhs))
                        .setType( ((Type.MethodType)ms.type).restype ); // tree.type may be != ms.type.restype. see below
                if (ms.name.contentEquals("compareTo")) {
                    // rewrite to `left.compareTo(right) </> 0`
                    JCLiteral zero = make.Literal(0);
                    JCBinary r = make.Binary(tree.getTag(), (JCExpression) result, zero);
                    r.type = syms.booleanType;
                    r.operator = rs.resolveBinaryOperator(getMake_pos(), tree.getTag(), attrEnv, result.type, zero.type);
                    result = r;
                }
                result = translate(result);
                return;
            }
        }
        List<Type> formals = tree.operator.type.getParameterTypes();
        JCTree lhs = tree.lhs = translate(tree.lhs, formals.head);
        switch (tree.getTag()) {
            case JCTree.OR:
                if (lhs.type.isTrue()) {
                    result = lhs;
                    return;
                }
                if (lhs.type.isFalse()) {
                    result = translate(tree.rhs, formals.tail.head);
                    return;
                }
                break;
            case JCTree.AND:
                if (lhs.type.isFalse()) {
                    result = lhs;
                    return;
                }
                if (lhs.type.isTrue()) {
                    result = translate(tree.rhs, formals.tail.head);
                    return;
                }
                break;
        }
        tree.rhs = translate(tree.rhs, formals.tail.head);
        result = tree;
    }

    @Override
    public void visitUnary(JCUnary tree) {
        if (tree.operator instanceof Symbol.OperatorSymbol) {
            // similar to #visitBinary
            Symbol.OperatorSymbol os = (Symbol.OperatorSymbol) tree.operator;
            if (os.opcode == ByteCodes.error+1) {
                Symbol.MethodSymbol ms = (Symbol.MethodSymbol) os.owner;
                JCFieldAccess meth = make.Select(tree.arg, ms.name);
                meth.type = ms.type;
                meth.sym = ms;
                result = make.Apply(null, meth, List.<JCExpression>nil())
                        .setType(tree.type);
                result = translate(result);
                return;
            }
        }
        boolean isUpdateOperator =
                JCTree.PREINC <= tree.getTag() && tree.getTag() <= JCTree.POSTDEC;
        if (isUpdateOperator && !tree.arg.type.isPrimitive()) {
            switch(tree.getTag()) {
                case JCTree.PREINC:            // ++ e
                    // translate to e += 1
                case JCTree.PREDEC:            // -- e
                    // translate to e -= 1
                {
                    int opcode = (tree.getTag() == JCTree.PREINC)
                            ? JCTree.PLUS_ASG : JCTree.MINUS_ASG;
                    JCAssignOp newTree = makeAssignop(opcode,
                            tree.arg,
                            make.Literal(1));
                    result = translate(newTree, tree.type);
                    return;
                }
                case JCTree.POSTINC:           // e ++
                case JCTree.POSTDEC:           // e --
                {
                    result = translate(lowerBoxedPostop(tree), tree.type);
                    return;
                }
            }
            throw new AssertionError(tree);
        }

        tree.arg = boxIfNeeded(translate(tree.arg, tree), tree.type);

        if (tree.getTag() == JCTree.NOT && tree.arg.type.constValue() != null) {
            tree.type = cfolder.fold1(bool_not, tree.arg.type);
        }

        // If translated left hand side is an Apply, we are
        // seeing an access method invocation. In this case, return
        // that access method invocation as result.
        if (isUpdateOperator && tree.arg.getTag() == JCTree.APPLY) {
            result = tree.arg;
        } else {
            result = tree;
        }
    }
}
