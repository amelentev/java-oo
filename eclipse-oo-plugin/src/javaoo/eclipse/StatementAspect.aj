/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 ******************************************************************************/
package javaoo.eclipse;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.SingleNameReference;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

@SuppressWarnings("restriction")
public aspect StatementAspect {
	pointcut isBoxingCompatible(Statement that, TypeBinding expressionType, TypeBinding targetType, Expression expression, Scope scope):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.Statement) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.Statement.isBoxingCompatible(TypeBinding, TypeBinding, Expression, Scope)) &&
		args(expressionType, targetType, expression, scope);

	boolean around(Statement that, TypeBinding expressionType, TypeBinding targetType, Expression expression, Scope scope):
			isBoxingCompatible(that, expressionType, targetType, expression, scope) {
		return proceed(that, expressionType, targetType, expression, scope)
				|| tryBoxingOverload(expressionType, targetType, expression, scope);
	}
	private static boolean tryBoxingOverload(TypeBinding expressionType, TypeBinding targetType, Expression expression, Scope scope) {
		if (!(scope instanceof BlockScope)) return false;
		BlockScope bscope = (BlockScope) scope;
		Expression receiver = new SingleNameReference(targetType.shortReadableName(), expression.sourceStart);
		receiver.resolvedType = targetType;
		MessageSend ms = Utils.findMethod(bscope, receiver, "valueOf", new Expression[]{expression}); //$NON-NLS-1$
		if (ms != null && ms.resolvedType == targetType) {
			ExpressionAspect.setTranslate(expression, ms);
			expression.resolvedType = ms.resolvedType;
			return true;
		}
		return false;
	}
}
