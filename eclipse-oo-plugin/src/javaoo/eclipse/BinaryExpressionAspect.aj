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

import static org.eclipse.jdt.internal.compiler.lookup.TypeIds.*;

import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.BinaryExpression;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.IntLiteral;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.OperatorExpression;
import org.eclipse.jdt.internal.compiler.ast.OperatorIds;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

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

	pointcut resolveType(BinaryExpression be, BlockScope scope):
		this(be) && within(org.eclipse.jdt.internal.compiler.ast.BinaryExpression) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.BinaryExpression.resolveType(BlockScope)) &&
		args(scope);
	
	// nothing interesting below
	/* TODO: maybe cflow && !cflowbelow
	 pointcut resolveInvalidOp():
		call(* org.eclipse.jdt.internal.compiler.problem.ProblemReporter.invalidOperator(..))
		&& withincode(* org.eclipse.jdt.internal.compiler.ast.BinaryExpression.resolveType(BlockScope));  
	 */
	TypeBinding around(BinaryExpression that, BlockScope scope): resolveType(that, scope) {
		// keep implementation in sync with CombinedBinaryExpression#resolveType
		// and nonRecursiveResolveTypeUpwards
		boolean leftIsCast, rightIsCast;
		if ((leftIsCast = that.left instanceof CastExpression) == true) that.left.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		TypeBinding leftType = that.left.resolveType(scope);

		if ((rightIsCast = that.right instanceof CastExpression) == true) that.right.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		TypeBinding rightType = that.right.resolveType(scope);

		// use the id of the type to navigate into the table
		if (leftType == null || rightType == null) {
			that.constant = Constant.NotAConstant;
			return null;
		}

		int leftTypeID = leftType.id;
		int rightTypeID = rightType.id;

		// autoboxing support
		boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		if (use15specifics) {
			if (!leftType.isBaseType() && rightTypeID != TypeIds.T_JavaLangString && rightTypeID != TypeIds.T_null) {
				leftTypeID = scope.environment().computeBoxingType(leftType).id;
			}
			if (!rightType.isBaseType() && leftTypeID != TypeIds.T_JavaLangString && leftTypeID != TypeIds.T_null) {
				rightTypeID = scope.environment().computeBoxingType(rightType).id;
			}
		}
		if (leftTypeID > 15
			|| rightTypeID > 15) { // must convert String + Object || Object + String
			if (leftTypeID == TypeIds.T_JavaLangString) {
				rightTypeID = TypeIds.T_JavaLangObject;
			} else if (rightTypeID == TypeIds.T_JavaLangString) {
				leftTypeID = TypeIds.T_JavaLangObject;
			} else {
				TypeBinding res = overloadBinaryOperator(that, scope);
				if (res != null)
					return res;
				that.constant = Constant.NotAConstant;
				scope.problemReporter().invalidOperator(that, leftType, rightType);
				return null;
			}
		}
		if (((that.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT) == OperatorIds.PLUS) {
			if (leftTypeID == TypeIds.T_JavaLangString) {
				that.left.computeConversion(scope, leftType, leftType);
				if (rightType.isArrayType() && ((ArrayBinding) rightType).elementsType() == TypeBinding.CHAR) {
					scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(that.right);
				}
			}
			if (rightTypeID == TypeIds.T_JavaLangString) {
				that.right.computeConversion(scope, rightType, rightType);
				if (leftType.isArrayType() && ((ArrayBinding) leftType).elementsType() == TypeBinding.CHAR) {
					scope.problemReporter().signalNoImplicitStringConversionForCharArrayExpression(that.left);
				}
			}
		}

		// the code is an int
		// (cast)  left   Op (cast)  right --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8    <<4       <<0

		// Don't test for result = 0. If it is zero, some more work is done.
		// On the one hand when it is not zero (correct code) we avoid doing the test
		int operator = (that.bits & ASTNode.OperatorMASK) >> ASTNode.OperatorSHIFT;
		int operatorSignature = OperatorExpression.OperatorSignatures[operator][(leftTypeID << 4) + rightTypeID];

		that.left.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), leftType);
		that.right.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 8) & 0x0000F), rightType);
		that.bits |= operatorSignature & 0xF;
		switch (operatorSignature & 0xF) { // record the current ReturnTypeID
			// only switch on possible result type.....
			case T_boolean :
				that.resolvedType = TypeBinding.BOOLEAN;
				break;
			case T_byte :
				that.resolvedType = TypeBinding.BYTE;
				break;
			case T_char :
				that.resolvedType = TypeBinding.CHAR;
				break;
			case T_double :
				that.resolvedType = TypeBinding.DOUBLE;
				break;
			case T_float :
				that.resolvedType = TypeBinding.FLOAT;
				break;
			case T_int :
				that.resolvedType = TypeBinding.INT;
				break;
			case T_long :
				that.resolvedType = TypeBinding.LONG;
				break;
			case T_JavaLangString :
				that.resolvedType = scope.getJavaLangString();
				break;
			default : //error........
				that.constant = Constant.NotAConstant;
				scope.problemReporter().invalidOperator(that, leftType, rightType);
				return null;
		}

		// check need for operand cast
		if (leftIsCast || rightIsCast) {
			CastExpression.checkNeedForArgumentCasts(scope, operator, operatorSignature, that.left, leftTypeID, leftIsCast, that.right, rightTypeID, rightIsCast);
		}
		// compute the constant when valid
		that.computeConstant(scope, leftTypeID, rightTypeID);
		return that.resolvedType;
	}
}
