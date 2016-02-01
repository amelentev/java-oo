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
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.resolve.JavaResolveCache;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.Function;

public class PsiOOBinaryExpressionImpl extends PsiBinaryExpressionImpl {
    @Override
    public PsiType getType() {
        return JavaResolveCache.getInstance(getProject()).getType(this, new Function<PsiBinaryExpression, PsiType>() {
            @Override
            public PsiType fun(PsiBinaryExpression e) {
                if (TypeConversionUtil.isBinaryOperatorApplicable(e.getOperationTokenType(), e.getLOperand(), e.getROperand(), false))
                    return PsiOOBinaryExpressionImpl.super.getType();
                return OOResolver.getOOType(PsiOOBinaryExpressionImpl.this);
            }
        });
    }
}
