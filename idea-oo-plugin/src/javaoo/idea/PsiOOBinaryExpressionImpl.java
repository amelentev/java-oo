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

import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.resolve.JavaResolveCache;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.util.Function;

public class PsiOOBinaryExpressionImpl extends PsiBinaryExpressionImpl {
    private static final Function<PsiBinaryExpressionImpl,PsiType> OO_TYPE_EVALUATOR = new Function<PsiBinaryExpressionImpl, PsiType>() {
        @Override
        public PsiType fun(PsiBinaryExpressionImpl expression) {
            PsiType type = (PsiType) Util.invoke(PsiBinaryExpressionImpl.class, null, "doGetType",
                    new Class[]{PsiBinaryExpressionImpl.class},
                    new Object[]{expression});
            PsiType lType = expression.getLOperand().getType();
            if (type != null && type != OOResolver.NoType
                    && (type != PsiType.INT || lType instanceof PsiPrimitiveType || PsiPrimitiveType.getUnboxedType(lType)!=null))
                return type;
            return OOResolver.getOOType(expression);
        }
    };

    @Override
    public PsiType getType() {
        return JavaResolveCache.getInstance(getProject()).getType(this, OO_TYPE_EVALUATOR);
    }
}
