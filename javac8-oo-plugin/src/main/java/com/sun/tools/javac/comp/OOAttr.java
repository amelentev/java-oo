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
import static com.sun.tools.javac.code.TypeTag.*;


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
        Resolve.MethodResolutionContext newrc = null;
        try {
            if (rs.currentResolutionContext == null) { // rc.findMethod requires resolution context
                newrc = rs.new MethodResolutionContext();
                newrc.step = Resolve.MethodResolutionPhase.BOX; // allow autoboxing, no varargs
                newrc.methodCheck = rs.resolveMethodCheck;
                rs.currentResolutionContext = newrc;
            }
            for (String methodName: methodNames) {
                Symbol m = rs.findMethod(env, site, names.fromString(methodName), argts, null, true, false, false);
                if (m.kind == Kinds.MTH) return m;
                m = rs.findMethod(env, site, names.fromString(methodName), argts, null, true, false, false); // with boxing
                if (m.kind == Kinds.MTH) return m;
            }
            return null;
        } finally {
            if (rs.currentResolutionContext == newrc)
                rs.currentResolutionContext = null;
        }
    }

    @Override
    Type check(final JCTree tree, final Type found, final int ownkind, final ResultInfo resultInfo) {
        // mimic super.check
        Infer.InferenceContext inferenceContext = resultInfo.checkContext.inferenceContext();
        Type owntype = found;
        if (!owntype.hasTag(ERROR) && !resultInfo.pt.hasTag(METHOD) && !resultInfo.pt.hasTag(FORALL)) {
            if (allowPoly && inferenceContext.free(found)) {}
            else if ((ownkind & ~resultInfo.pkind) == 0) {
                JCTree.JCExpression t = tryImplicitConversion(tree, owntype, resultInfo);
                if (t != null) {
                    translateMap.put(tree, t);
                    return tree.type = owntype;
                }
            }
        }
        return super.check(tree, owntype, ownkind, resultInfo);
    }

    /** try implicit conversion tree to pt type via #valueOf
     * @return static valueOf method call iff successful. null otherwise */
    JCTree.JCMethodInvocation tryImplicitConversion(JCTree tree, Type owntype, ResultInfo resultInfo) {
        if (!isImplicitConversionAllowed(owntype, resultInfo.pt))
            return null;
        JCTree.JCExpression param = translateMap.get(tree);
        // construct "<req>.valueOf(tree)" static method call
        tree.type = owntype;
        make.pos = tree.pos;
        for (String methodName : OOMethods.valueOf) {
            JCTree.JCMethodInvocation method = make.Apply(null,
                    make.Select(make.Ident(resultInfo.pt.tsym), names.fromString(methodName)),
                    List.of(param == null ? (JCTree.JCExpression) tree : param));
            method.type = attribTree(method, env, resultInfo);
            if (types.isAssignable(method.type, resultInfo.pt))
                return method;
        }
        return null;
    }
    boolean isImplicitConversionAllowed(Type found, Type req) {
        // similar to Check#checkType
        if (req.hasTag(ERROR) || req.hasTag(NONE) || types.isAssignable(found, req) || found.isNumeric() && req.isNumeric())
            return false;
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
                    check(aa, aa.type, VAR, resultInfo);
                    result = check(tree, owntype, VAL, resultInfo);
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
        } else if (!atype.isErroneous()) {
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
        if ((pkind() & VAR) == 0) owntype = types.capture(owntype);
        result = check(tree, owntype, VAR, resultInfo);
    }
}
