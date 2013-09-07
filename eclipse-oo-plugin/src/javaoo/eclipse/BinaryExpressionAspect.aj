/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 *     some code from org.eclipse.jdt.code
 ******************************************************************************/
package javaoo.eclipse;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings("restriction")
public aspect BinaryExpressionAspect {
	@SuppressWarnings("serial")
	public final static java.util.Map<String, String> binaryOperators = new java.util.HashMap<String, String>() {{
		put("+", "add");
		put("-", "subtract");
		put("*", "multiply");
		put("/", "divide");
		put("%", "remainder");
		put("&", "and");
		put("|", "or");
		put("^", "xor");
		put("<<", "shiftLeft");
		put(">>", "shiftRight");
		put("<", "compareTo");
		put(">", "compareTo");
		put("<=", "compareTo");
		put(">=", "compareTo");
	}};

	public static TypeBinding overloadBinaryOperator(BinaryExpression that, BlockScope scope) {
		// try operator overloading
		String method = (String) binaryOperators.get(that.operatorToString());
		if (method != null) {
			// find method
			MessageSend ms = Utils.findMethod(scope, that.left, method, new Expression[]{that.right});
			if (ms != null) { // found
				if ("compareTo".equals(method)) { //$NON-NLS-1$
					// rewrite to `left.compareTo(right) </> 0`
					that.left = ms;
					that.right = IntLiteral.buildIntLiteral("0".toCharArray(), that.sourceStart, that.sourceEnd); //$NON-NLS-1$
					that.right.resolve(scope);
					int leftTypeID = that.left.resolvedType.id;
					int rightTypeID = that.right.resolvedType.id;
					if (leftTypeID == rightTypeID) { // if compareTo really returns int
						// resolve rest info about `left </> 0`
						int operator = (that.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
						int operatorSignature = OperatorExpression.OperatorSignatures[operator][(leftTypeID << 4) + rightTypeID];
						that.left.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), that.left.resolvedType);
						that.right.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 8) & 0x0000F), that.right.resolvedType);
						that.bits |= operatorSignature & 0xF;
						that.computeConstant(scope, leftTypeID, rightTypeID);
						return that.resolvedType = TypeBinding.BOOLEAN;
					}
				} else {
					ExpressionAspect.setTranslate(that, ms);
					that.constant = Constant.NotAConstant;
					return that.resolvedType = ms.resolvedType;
				}
			}
		}
		return null;
	}

	pointcut isCompactableOperation(BinaryExpression that):
		execution(* org.eclipse.jdt.internal.compiler.ast.BinaryExpression.isCompactableOperation()) &&
		within(org.eclipse.jdt.internal.compiler.ast.BinaryExpression) &&
		this(that);

	boolean around(BinaryExpression that): isCompactableOperation(that) {
		return ExpressionAspect.getTranslate(that) == null;
	}

	pointcut resolveType(BlockScope scope):
		execution(* BinaryExpression.resolveType(BlockScope)) && args(scope);

	ThreadLocal<BlockScope> _scope = new ThreadLocal<BlockScope>();
	TypeBinding around(BlockScope scope): resolveType(scope) {
		try {
			_scope.set(scope);
			return proceed(scope);
		} catch (ReturnException e) {
			return (TypeBinding) e.getReturn();
		}
	}

	void around(BinaryExpression that):
		withincode(* BinaryExpression.resolveType(BlockScope))
		&& call(* ProblemReporter.invalidOperator(BinaryExpression, TypeBinding, TypeBinding)) && args(that, ..)
	{
		BlockScope scope = _scope.get();
		TypeBinding res = overloadBinaryOperator(that, scope);
		if (res != null)
			throw new ReturnException(res);
		proceed(that);
	}
}
