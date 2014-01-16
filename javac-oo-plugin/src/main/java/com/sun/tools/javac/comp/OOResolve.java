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

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import javaoo.OOMethods;

import static com.sun.tools.javac.code.Kinds.ERR;

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
        Symbol bestSoFar = super.findMethod(env, site, name, argtypes, typeargtypes, allowBoxing, useVarargs, operator);
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
                        args, null, true, false, false);
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
