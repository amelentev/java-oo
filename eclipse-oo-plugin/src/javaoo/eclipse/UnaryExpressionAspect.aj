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
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings("restriction")
public aspect UnaryExpressionAspect {
	@SuppressWarnings("serial")
	public final static java.util.Map<String, String> unaryOperators = new java.util.HashMap<String, String>() {{
		put("-", "negate");
		put("~", "not");
	}};

	public static TypeBinding overloadUnaryOperator(UnaryExpression that, BlockScope scope) {
		// similar to BinaryExpression#overloadBinaryOperator
		String method = (String) unaryOperators.get(that.operatorToString());
		if (method != null) {
			// find method
			MessageSend ms = Utils.findMethod(scope, that.expression, method, new Expression[0]);
			if (ms != null) {
				ExpressionAspect.setTranslate(that, ms);
				return that.resolvedType = ms.resolvedType;
			}
		}
		return null;
	}

	ThreadLocal<BlockScope> _scope = new ThreadLocal<BlockScope>();

	TypeBinding around(BlockScope scope):
		execution(* UnaryExpression.resolveType(BlockScope)) && args(scope)
	{
		try {
			_scope.set(scope);
			return proceed(scope);
		} catch (ReturnException e) {
			return (TypeBinding) e.getReturn();
		}
	}

	void around(UnaryExpression that):
		withincode(* UnaryExpression.resolveType(BlockScope))
		&& call(* ProblemReporter.invalidOperator(UnaryExpression, TypeBinding)) && args(that, ..)
	{
		TypeBinding res = overloadUnaryOperator(that, _scope.get());
		if (res != null)
			throw new ReturnException(res);
		proceed(that);
	}
}
