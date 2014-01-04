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
import javaoo.OOMethods;

import java.util.Map;
import java.util.WeakHashMap;

import static com.sun.tools.javac.code.Kinds.VAR;
import static com.sun.tools.javac.code.TypeTags.*;

// XXX: NBAttr?
public class OOAttr extends Attr {
    protected OOAttr(Context context) {
        super(context);
    }
    public static OOAttr instance(Context context) {
        Attr attr = context.get(attrKey);
        if (attr instanceof OOAttr) return (OOAttr) attr;
        context.put(attrKey, (Attr)null);
        return new OOAttr(context);
    }

    /** WeakHashMap to allow GC collect entries. Because we don't need them then they are gone */
    public Map<JCTree, JCTree.JCExpression> translateMap = new WeakHashMap<>();

    @Override
    Type check(JCTree tree, Type owntype, int ownkind, int pkind, Type pt) {
        if (owntype.tag != ERROR && pt.tag == CLASS && (ownkind & ~pkind) == 0) {
            JCTree.JCExpression t = tryImplicitConversion(tree, owntype, pt);
            if (t != null) {
                translateMap.put(tree, t);
                return tree.type = owntype;
            }
        }
        return super.check(tree, owntype, ownkind, pkind, pt);
    }

    /** try implicit conversion tree to pt type via #valueOf
     * @return static valueOf method call iff successful */
    JCTree.JCMethodInvocation tryImplicitConversion(JCTree tree, Type owntype, Type req) {
        if (!isBoxingAllowed(owntype, req))
            return null;
        JCTree.JCExpression param = translateMap.get(tree);
        // construct "<req>.valueOf(tree)" static method call
        tree.type = owntype;
        JCTree.JCMethodInvocation valueOf = make.Apply(null,
                make.Select(make.Ident(pt.tsym), names.fromString(OOMethods.valueOf)),
                List.of(param == null ? (JCTree.JCExpression)tree : param));
        valueOf.type = attribTree(valueOf, env, pkind, pt);
        return types.isAssignable(valueOf.type, req) ? valueOf : null;
    }
    boolean isBoxingAllowed(Type found, Type req) {
        // similar to Check#checkType
        if (req.tag == ERROR)
            return false; // req
        if (found.tag == FORALL)
            return false; // chk.instantiatePoly(pos, (ForAll)found, req, convertWarner(pos, found, req));
        if (req.tag == NONE)
            return false; //found;
        if (types.isAssignable(found, req)) //convertWarner(pos, found, req)))
            return false; // found;
        if (found.tag <= DOUBLE && req.tag <= DOUBLE)
            return false; // typeError(pos, diags.fragment("possible.loss.of.precision"), found, req);
        if (found.isSuperBound()) {
            //log.error(pos, "assignment.from.super-bound", found);
            return false; //types.createErrorType(found);
        }
        if (req.isExtendsBound()) {
            //log.error(pos, "assignment.to.extends-bound", req);
            return false; //types.createErrorType(found);
        }
        return true;
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
            if (env.tree.getKind() == Tree.Kind.ASSIGNMENT && ((JCTree.JCAssign)env.tree).lhs == tree) {
                // index set
                JCTree.JCAssign ass = (JCTree.JCAssign) env.tree;
                Type rhstype = attribExpr(ass.rhs, env);
                List<Type> argtypes = List.of(tree.index.type, rhstype);
                Symbol m = null;
                for (String indexSet : OOMethods.indexSet) {
                    m = rs.findMethod(env, atype, names.fromString(indexSet), argtypes, null, true, false, false);
                    if (m.kind == Kinds.MTH)
                        break;
                }
                if (m.kind == Kinds.MTH) {
                    JCTree.JCMethodInvocation mi = make.Apply(null, make.Select(tree.indexed, m), List.of(tree.index, ass.rhs));
                    owntype = mi.type = attribExpr(mi, env);
                    translateMap.put(ass, mi);
                    ok = true;
                }
            } else {
                // index get
                List<Type> argtypes = List.of(tree.index.type);
                Symbol m = rs.findMethod(env, atype, names.fromString(OOMethods.indexGet), argtypes, null, true, false, false);
                if (m.kind == Kinds.MTH) {
                    //owntype = rs.instantiate(env, atype, m, argtypes, null, true, false, noteWarner).getReturnType();
                    JCTree.JCMethodInvocation mi = make.Apply(null, make.Select(tree.indexed, m), List.of(tree.index));
                    attribExpr(mi, env);
                    translateMap.put(tree, mi);
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
