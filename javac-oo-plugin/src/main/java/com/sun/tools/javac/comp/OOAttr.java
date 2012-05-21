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
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import static com.sun.tools.javac.code.Kinds.VAR;
import static com.sun.tools.javac.code.TypeTags.ERROR;

// XXX: NBAttr?
public class OOAttr extends Attr {
    protected OOAttr(Context context) {
        super(context);
    }
    public static OOAttr hook(Context context) {
        context.put(attrKey, (Attr)null);
        return new OOAttr(context);
    }

    @Override
    public void visitIndexed(JCTree.JCArrayAccess tree) {
        Type owntype = types.createErrorType(tree.type);
        Type atype = attribExpr(tree.indexed, env);
        if (types.isArray(atype)) {
            attribExpr(tree.index, env, syms.intType);
            owntype = types.elemtype(atype);
        } else if (atype.tag != ERROR) {
            attribExpr(tree.index, env);
            boolean ok = false;
            if (env.tree.getKind() == Tree.Kind.ASSIGNMENT) {
                JCTree.JCAssign ass = (JCTree.JCAssign) env.tree;
                if (ass.lhs == tree) {
                    Type rhstype = attribExpr(ass.rhs, env);
                    List<Type> argtypes = List.of(tree.index.type, rhstype);
                    Symbol m = rs.findMethod(env, atype, names.fromString("set"), argtypes, null, true, false, false);
                    if (m.kind != Kinds.MTH)
                        m = rs.findMethod(env, atype, names.fromString("put"), argtypes, null, true, false, false); // Map#put
                    if (m.kind == Kinds.MTH) {
                        JCTree.JCMethodInvocation mi = make.Apply(null, make.Select(tree.indexed, m), List.of(tree.index, ass.rhs));
                        mi.type = attribExpr(mi, env);
                        tree.indexed = mi;
                        owntype = rhstype;
                        ok = true;
                    }
                }
            }
            if (!ok) {
                List<Type> argtypes = List.of(tree.index.type);
                Symbol m = rs.findMethod(env, atype, names.fromString("get"), argtypes, null, true, false, false);
                if (m.kind == Kinds.MTH) {
                    //owntype = rs.instantiate(env, atype, m, argtypes, null, true, false, noteWarner).getReturnType();
                    JCTree.JCMethodInvocation mi = make.Apply(null, make.Select(tree.indexed, m), List.of(tree.index));
                    attribExpr(mi, env);
                    tree.indexed = mi;
                    owntype = mi.type;
                    ok = true;
                }
            }
            if (!ok)
                log.error(tree.pos(), "array.req.but.found", atype);
        }
        if ((pkind & VAR) == 0) owntype = types.capture(owntype);
        result = check(tree, owntype, VAR, pkind, pt);
    }
}
