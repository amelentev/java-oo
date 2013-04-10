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

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;
import com.intellij.psi.impl.source.resolve.JavaResolveCache;
import com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.Function;
import com.intellij.util.NullableFunction;

public class PsiOOPolyadicExpressionImpl extends PsiPolyadicExpressionImpl {
    @Override
    public PsiType getType() {
        return JavaResolveCache.getInstance(getProject()).getType(this, OO_TYPE_EVALUATOR);
    }
    private static final Function<PsiPolyadicExpressionImpl,PsiType> OO_TYPE_EVALUATOR = new NullableFunction<PsiPolyadicExpressionImpl, PsiType>() {
        @Override
        public PsiType fun(PsiPolyadicExpressionImpl param) {
            // copied from com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl.doGetType
            PsiExpression[] operands = param.getOperands();
            PsiType lType = null;

            IElementType sign = param.getOperationTokenType();
            for (int i=1; i<operands.length;i++) {
                PsiType rType = operands[i].getType();
                // optimization: if we can calculate type based on right type only
                PsiType type = TypeConversionUtil.calcTypeForBinaryExpression(null, rType, sign, false);
                if (type != TypeConversionUtil.NULL_TYPE) return type;
                if (lType == null) lType = operands[0].getType();
                PsiType oldlType = lType;
                lType = TypeConversionUtil.calcTypeForBinaryExpression(lType, rType, sign, true);
                // try OO if something wrong
                if (!(lType != PsiType.INT || oldlType instanceof PsiPrimitiveType || PsiPrimitiveType.getUnboxedType(oldlType)!=null))
                    lType = OOResolver.getOOType(oldlType, rType, param.getTokenBeforeOperand(operands[i]));
            }
            return lType;
        }
    };
}
