/* Copyright 2013 Artem Melentyev <amelentev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javaoo.idea;

import com.intellij.psi.*;
import com.intellij.psi.impl.source.tree.java.PsiMethodCallExpressionImpl;
import com.intellij.psi.util.TypeConversionUtil;
import javaoo.OOMethods;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OOResolver {

    public static @NotNull PsiType getOOType(@NotNull PsiBinaryExpression e) {
        return getOOType(e.getLOperand().getType(), e.getROperand().getType(), e.getOperationSign());
    }
    public static @NotNull PsiType getOOType(PsiType ltype, PsiType rtype, @NotNull PsiJavaToken op) {
        String methodname =  OOMethods.binary.get(op.getText());
        if (methodname!=null && rtype!=null) {
            PsiType res = resolveMethod(ltype, methodname, rtype);
            if (res!=null)
                return res;
        }
        return TypeConversionUtil.NULL_TYPE;
    }

    public static @NotNull PsiType getOOType(@NotNull PsiPrefixExpression e) {
        PsiType optype = e.getOperand().getType();
        String methodname = OOMethods.unary.get(e.getOperationSign().getText());
        if (methodname!=null) {
            PsiType res = resolveMethod(optype, methodname);
            if (res!=null)
                return res;
        }
        return TypeConversionUtil.NULL_TYPE;
    }

    public static @NotNull PsiType indexGet(@NotNull PsiArrayAccessExpression e) {
        PsiType res = OOResolver.resolveMethod(e.getArrayExpression().getType(), OOMethods.indexGet, e.getIndexExpression().getType());
        return res!=null ? res : TypeConversionUtil.NULL_TYPE;
    }

    public static PsiType indexSet(PsiArrayAccessExpression paa, PsiExpression value) {
        for (String method : OOMethods.indexSet) {
            PsiType res = OOResolver.resolveMethod(paa.getArrayExpression(), method, paa.getIndexExpression(), value);
            if (res!=null) return res;
        }
        return TypeConversionUtil.NULL_TYPE;
    }

    public static @Nullable PsiType resolveMethod(@NotNull PsiExpression clas, @NotNull String methodName, @NotNull PsiExpression... args) {
        PsiType[] argTypes = new PsiType[args.length];
        for (int i = 0; i < args.length; i++)
            argTypes[i] = args[i].getType();
        return resolveMethod(clas.getType(), methodName, argTypes);
    }
    // TODO: find a better way to do it
    public static @Nullable PsiType resolveMethod(PsiType type, @NotNull String methodName, @NotNull PsiType... argTypes) {
        if (!(type instanceof PsiClassType)) return null;
        PsiClassType clas = ((PsiClassType) type);
        PsiSubstitutor subst = clas.resolveGenerics().getSubstitutor();
        PsiMethod methods[] = clas.resolve().findMethodsByName(methodName, true);
        for (PsiMethod method : methods) {
            PsiParameter[] pars = method.getParameterList().getParameters();
            if (pars.length == argTypes.length) {
                boolean ok = true;
                for (int i = 0; i < pars.length; i++)
                    ok &= subst.substitute(pars[i].getType()).isAssignableFrom(argTypes[i]);
                if (ok) return subst.substitute(method.getReturnType());
            }
        }
        return null;
    }
}
