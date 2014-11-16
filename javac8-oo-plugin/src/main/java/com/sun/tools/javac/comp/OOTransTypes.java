/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 
 * GNU Public License v2.0 + OpenJDK assembly exception.
 * 
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 ******************************************************************************/
package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import javaoo.OOMethods;

import java.lang.reflect.Field;

public class OOTransTypes extends TransTypes {
    private final Symtab syms;
    private final TreeMaker make;
    private final OOResolve rs;
    private final OOAttr attr;

    public static OOTransTypes instance(Context context) {
        TransTypes res = context.get(transTypesKey);
        if (res instanceof OOTransTypes) return (OOTransTypes) res;
        context.put(transTypesKey, (TransTypes)null);
        return new OOTransTypes(context);
    }
    protected OOTransTypes(Context context) {
        super(context);
        syms = Symtab.instance(context);
        make = TreeMaker.instance(context);
        attr = OOAttr.instance(context);
        rs = OOResolve.instance(context);
    }

    private Env<AttrContext> getEnv() {
        try {
            Field f = TransTypes.class.getDeclaredField("env");
            f.setAccessible(true);
            return (Env<AttrContext>) f.get(this);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends JCTree> T translate(T tree) {
        JCTree.JCExpression t = attr.translateMap.remove(tree);
        if (t!=null)
            return (T) translate(t);
        return super.translate(tree);
    }

    @Override
    public void visitUnary(JCTree.JCUnary tree) {
        if (tree.operator instanceof Symbol.OperatorSymbol) {
            // similar to #visitBinary
            Symbol.OperatorSymbol os = (Symbol.OperatorSymbol) tree.operator;
            if (os.opcode == ByteCodes.error+1) {
                Symbol.MethodSymbol ms = (Symbol.MethodSymbol) os.owner;
                JCTree.JCFieldAccess meth = make.Select(tree.arg, ms.name);
                meth.type = ms.type;
                meth.sym = ms;
                result = make.Apply(null, meth, List.<JCTree.JCExpression>nil())
                        .setType(tree.type);
                result = translate(result);
                return;
            }
        }
        super.visitUnary(tree);
    }

    @Override
    public void visitBinary(JCTree.JCBinary tree) {
        if (tree.operator instanceof Symbol.OperatorSymbol) {
            Symbol.OperatorSymbol os = (Symbol.OperatorSymbol) tree.operator;
            if (os.opcode == ByteCodes.error+1) { // if operator overloading?
                Symbol.MethodSymbol ms = (Symbol.MethodSymbol) os.owner;
                boolean isRev = ms.name.toString().endsWith(OOMethods.revSuffix); // reverse hs if methodRev
                JCTree.JCExpression lhs = isRev ? tree.rhs : tree.lhs;
                JCTree.JCExpression rhs = isRev ? tree.lhs : tree.rhs;
                // construct method invocation ast
                JCTree.JCFieldAccess meth = make.Select(lhs, ms.name);
                meth.type = ms.type;
                meth.sym = ms;
                result = make.Apply(null, meth, List.of(rhs))
                        .setType( ((Type.MethodType)ms.type).restype ); // tree.type may be != ms.type.restype. see below
                if (ms.name.contentEquals(OOMethods.compareTo)) {
                    // rewrite to `left.compareTo(right) </> 0`
                    JCTree.JCLiteral zero = make.Literal(0);
                    JCTree.JCBinary r = make.Binary(tree.getTag(), (JCTree.JCExpression) result, zero);
                    r.type = syms.booleanType;
                    r.operator = rs.resolveBinaryOperator(tree, tree.getTag(), getEnv(), result.type, zero.type);
                    result = r;
                }
                result = translate(result);
                return;
            }
        }
        super.visitBinary(tree);
    }
}
