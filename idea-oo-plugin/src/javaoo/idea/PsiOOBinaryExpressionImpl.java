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

import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.resolve.JavaResolveCache;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.util.Function;

public class PsiOOBinaryExpressionImpl extends PsiBinaryExpressionImpl {
    private final PsiBinaryExpressionImpl cache = new PsiBinaryExpressionImpl();
    @Override
    public PsiType getType() {
        return JavaResolveCache.getInstance(getProject()).getType(cache, new Function<PsiBinaryExpression, PsiType>() {
            @Override
            public PsiType fun(PsiBinaryExpression psiOOBinaryExpression) {
                PsiType type = PsiOOBinaryExpressionImpl.super.getType();
                PsiType lType = getLOperand().getType();
                if (type != null && type != OOResolver.NoType
                        && (type != PsiType.INT || lType instanceof PsiPrimitiveType || PsiPrimitiveType.getUnboxedType(lType) != null))
                    return type;
                return OOResolver.getOOType(PsiOOBinaryExpressionImpl.this);
            }
        });
    }
}
