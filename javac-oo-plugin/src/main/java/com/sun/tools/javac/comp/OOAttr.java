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

import static com.sun.tools.javac.code.Kinds.VAL;
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
    Map<JCTree, JCTree.JCExpression> translateMap = new WeakHashMap<>();

    private Symbol findMethods(Type site, List<Type> argts, String... methodNames) {
        for (String methodName : methodNames) {
            Symbol m = rs.findMethod(env, site, names.fromString(methodName), argts, null, false, false, false); // without boxing
            if (m.kind == Kinds.MTH) return m;
            m = rs.findMethod(env, site, names.fromString(methodName), argts, null, true, false, false); // with boxing
            if (m.kind == Kinds.MTH) return m;
        }
        return null;
    }

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
        make.pos = tree.pos;
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
        return findMethods(req, List.of(found), OOMethods.valueOf) != null;
    }

    @Override
    public void visitAssign(JCTree.JCAssign tree) {
        if (tree.lhs.getKind() == Tree.Kind.ARRAY_ACCESS) { // index-set OO: "a[i] = v"
            JCTree.JCArrayAccess aa = (JCTree.JCArrayAccess) tree.lhs;
            Type atype = attribExpr(aa.indexed, env);
            if (!atype.isErroneous() && !types.isArray(atype)) {
                Type itype = attribExpr(aa.index, env);
                Type rhstype = attribExpr(tree.rhs, env);
                Symbol m = findMethods(atype, List.of(itype, rhstype), OOMethods.indexSet);
                if (m != null) {
                    JCTree.JCMethodInvocation mi = make.Apply(null, make.Select(aa.indexed, m), List.of(aa.index, tree.rhs));
                    Type owntype = attribExpr(mi, env);
                    translateMap.put(tree, mi);
                    aa.type = rhstype;
                    check(aa, aa.type, VAR, VAR, Type.noType);
                    result = check(tree, owntype, VAL, pkind, pt);
                    return;
                }
            }
        }
        super.visitAssign(tree);
    }

    @Override
    public void visitIndexed(JCTree.JCArrayAccess tree) {
        Type owntype = types.createErrorType(tree.type);
        Type atype = attribExpr(tree.indexed, env);
        if (types.isArray(atype)) {
            attribExpr(tree.index, env, syms.intType);
            owntype = types.elemtype(atype);
        } else if (!atype.isErroneous()) { // index get
            attribExpr(tree.index, env);
            Symbol m = findMethods(atype, List.of(tree.index.type), OOMethods.indexGet);
            if (m != null) {
                JCTree.JCMethodInvocation mi = make.Apply(null, make.Select(tree.indexed, m), List.of(tree.index));
                attribExpr(mi, env);
                translateMap.put(tree, mi);
                owntype = mi.type;
            } else
                log.error(tree.pos(), "array.req.but.found", atype);
        }
        if ((pkind & VAR) == 0) owntype = types.capture(owntype);
        result = check(tree, owntype, VAR, pkind, pt);
    }
}
