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

import com.intellij.codeInsight.daemon.impl.HighlightInfo;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.TypeConversionUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OOHighlightVisitorImpl extends HighlightVisitorImpl {
    public OOHighlightVisitorImpl(@NotNull PsiResolveHelper resolveHelper) {
        super(resolveHelper);
    }

    @Override
    public void visitPolyadicExpression(PsiPolyadicExpression expression) {
        super.visitPolyadicExpression(expression);
        if (isHighlighted(expression)) {
            PsiExpression[] operands = expression.getOperands();
            PsiType lType = operands[0].getType();
            for (int i = 1; i < operands.length; i++) {
                PsiExpression operand = operands[i];
                PsiType rType = operand.getType();
                // TODO: case A + A + int ? A.add(A) : int
                lType = OOResolver.getOOType(lType, rType, expression.getTokenBeforeOperand(operand));
            }
            if (lType != TypeConversionUtil.NULL_TYPE)
                removeLastHighlight();
        }
    }

    @Override
    public void visitPrefixExpression(PsiPrefixExpression expression) {
        super.visitPrefixExpression(expression);
        if (isHighlighted(expression)
                && OOResolver.getOOType(expression) != TypeConversionUtil.NULL_TYPE) {
            removeLastHighlight();
        }
    }

    @Override
    public void visitExpression(PsiExpression expression) {
        super.visitExpression(expression);
        if (expression instanceof PsiArrayAccessExpression) {
            PsiArrayAccessExpression paa = (PsiArrayAccessExpression) expression;
            if (isHighlighted(paa.getArrayExpression()) && OOResolver.indexGet((PsiArrayAccessExpression) expression)!=TypeConversionUtil.NULL_TYPE)
                removeLastHighlight();
        }
    }

    private HighlightInfoHolder getMyHolder() {
        return (HighlightInfoHolder) Util.get(HighlightVisitorImpl.class, this, "myHolder");
    }

    private boolean isHighlighted(PsiElement expression) {
        HighlightInfoHolder myHolder = getMyHolder();
        if (myHolder.hasErrorResults()) {
            HighlightInfo hi = myHolder.get(myHolder.size()-1);
            TextRange tr = expression.getTextRange();
            return hi.startOffset==tr.getStartOffset() && hi.endOffset==tr.getEndOffset();
        }
        return false;
    }

    private void removeLastHighlight() {
        List myInfos = (List) Util.get(HighlightInfoHolder.class, getMyHolder(), "myInfos");
        myInfos.remove(getMyHolder().size()-1);
    }
}
