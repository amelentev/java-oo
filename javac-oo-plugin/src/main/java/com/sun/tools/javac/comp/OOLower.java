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

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import javaoo.OOMethods;

import java.lang.reflect.Field;

public class OOLower extends Lower {
    public static OOLower instance(Context context) {
        Lower res = context.get(lowerKey);
        if (res instanceof OOLower) return (OOLower) res;
        context.put(lowerKey, (Lower)null);
        return new OOLower(context);
    }
    protected OOLower(Context context) {
        super(context);
        syms = Symtab.instance(context);
        make = TreeMaker.instance(context);
        rs = OOResolve.instance(context);
    }
    private Symtab syms;
    private TreeMaker make;
    private OOResolve rs;
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
        super.visitUnary(tree);
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
                if (ms.name.contentEquals(OOMethods.compareTo)) {
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
        super.visitBinary(tree);
    }
}
