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

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import javaoo.OOMethods;

import java.util.HashSet;
import java.util.Set;

import static com.sun.tools.javac.code.Flags.*;
import static com.sun.tools.javac.code.Flags.ABSTRACT;
import static com.sun.tools.javac.code.Kinds.ERR;
import static com.sun.tools.javac.code.Kinds.MTH;
import static com.sun.tools.javac.code.TypeTags.CLASS;
import static com.sun.tools.javac.code.TypeTags.TYPEVAR;

public class OOResolve extends Resolve {
    protected OOResolve(Context context) {
        super(context);
    }
    public static OOResolve instance(Context context) {
        Resolve res = context.get(resolveKey);
        if (res instanceof OOResolve) return (OOResolve)res;
        context.put(resolveKey, (Resolve)null);
        return new OOResolve(context);
    }

    @Override
    Symbol findMethod(Env<AttrContext> env,
                      Type site,
                      Name name,
                      List<Type> argtypes,
                      List<Type> typeargtypes,
                      boolean allowBoxing,
                      boolean useVarargs,
                      boolean operator) {
        Symbol bestSoFar = methodNotFound;
        return findMethod(env,
                site,
                name,
                argtypes,
                typeargtypes,
                site.tsym.type,
                true,
                bestSoFar,
                allowBoxing,
                useVarargs,
                operator,
                new HashSet<Symbol.TypeSymbol>());
    }

    private Symbol findMethod(Env<AttrContext> env,
                              Type site,
                              Name name,
                              List<Type> argtypes,
                              List<Type> typeargtypes,
                              Type intype,
                              boolean abstractok,
                              Symbol bestSoFar,
                              boolean allowBoxing,
                              boolean useVarargs,
                              boolean operator,
                              Set<Symbol.TypeSymbol> seen) {
        for (Type ct = intype; ct.tag == CLASS || ct.tag == TYPEVAR; ct = types.supertype(ct)) {
            while (ct.tag == TYPEVAR)
                ct = ct.getUpperBound();
            Symbol.ClassSymbol c = (Symbol.ClassSymbol)ct.tsym;
            if (!seen.add(c)) return bestSoFar;
            if ((c.flags() & (ABSTRACT | INTERFACE | ENUM)) == 0)
                abstractok = false;
            for (Scope.Entry e = c.members().lookup(name);
                 e.scope != null;
                 e = e.next()) {
                //- System.out.println(" e " + e.sym);
                if (e.sym.kind == MTH &&
                        (e.sym.flags_field & SYNTHETIC) == 0) {
                    bestSoFar = selectBest(env, site, argtypes, typeargtypes,
                            e.sym, bestSoFar,
                            allowBoxing,
                            useVarargs,
                            operator);
                }
            }
            if (name == names.init)
                break;
            //- System.out.println(" - " + bestSoFar);
            if (abstractok) {
                Symbol concrete = methodNotFound;
                if ((bestSoFar.flags() & ABSTRACT) == 0)
                    concrete = bestSoFar;
                for (List<Type> l = types.interfaces(c.type);
                     l.nonEmpty();
                     l = l.tail) {
                    bestSoFar = findMethod(env, site, name, argtypes,
                            typeargtypes,
                            l.head, abstractok, bestSoFar,
                            allowBoxing, useVarargs, operator, seen);
                }
                if (concrete != bestSoFar &&
                        concrete.kind < ERR  && bestSoFar.kind < ERR &&
                        types.isSubSignature(concrete.type, bestSoFar.type))
                    bestSoFar = concrete;
            }
        }
        if (bestSoFar.kind >= ERR && operator) { // try operator overloading
            String opname = null;
            List<Type> args = List.nil();
            if (argtypes.size() == 2) {
                opname = OOMethods.binary.get(name.toString());
                args = List.of(argtypes.get(1));
            } else if (argtypes.size() == 1)
                opname = OOMethods.unary.get(name.toString());
            if (opname != null) {
                Symbol method = findMethod(env, argtypes.get(0), names.fromString(opname),
                        args, null, false, false, false);
                if (method.kind == Kinds.MTH) {
                    bestSoFar = new Symbol.OperatorSymbol(method.name, method.type, ByteCodes.error+1, method);
                    if (OOMethods.compareTo.equals(opname)) { // change result type to boolean if </>
                        Type.MethodType oldmt = (Type.MethodType) method.type;
                        bestSoFar.type = new Type.MethodType(oldmt.argtypes, syms.booleanType, oldmt.thrown, oldmt.tsym);
                    }
                }
            }
        }
        return bestSoFar;
    }
}
