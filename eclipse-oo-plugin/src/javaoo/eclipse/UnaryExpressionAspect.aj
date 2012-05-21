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

import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.UnaryExpression;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.BooleanConstant;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import static org.eclipse.jdt.internal.compiler.ast.ASTNode.*;
import static org.eclipse.jdt.internal.compiler.ast.OperatorIds.*;
import static org.eclipse.jdt.internal.compiler.ast.OperatorExpression.*;

@SuppressWarnings("restriction")
public aspect UnaryExpressionAspect {
	@SuppressWarnings("serial")
	public final static java.util.Map<String, String> unaryOperators = new java.util.HashMap<String, String>() {{
		put("-", "negate");
		put("~", "not");
	}};
	
	public MessageSend UnaryExpression.overloadMethod;
	
	public static TypeBinding overloadUnaryOperator(UnaryExpression that, BlockScope scope) {
		// similar to BinaryExpression#overloadBinaryOperator
		String method = (String) unaryOperators.get(that.operatorToString());
		if (method != null) {
			// find method
			MessageSend ms = Utils.findMethod(scope, that.expression, method, new Expression[0]);
			if (ms != null) {
				Utils.set(that, "overloadMethod", ms);
				that.constant = Constant.NotAConstant;
				return that.resolvedType = ms.resolvedType;
			}
		}
		return null;
	}
	
	pointcut generateCode(UnaryExpression that, BlockScope scope, CodeStream stream, boolean valueRequired):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.UnaryExpression) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.UnaryExpression.generateCode(BlockScope, CodeStream, boolean)) &&
		args(scope, stream, valueRequired);

	// same as in BinaryExpressionAspect
	void around(UnaryExpression that, BlockScope scope, CodeStream codeStream, boolean valueRequired): 
			generateCode(that, scope, codeStream, valueRequired) {
		MessageSend ms = (MessageSend) Utils.get(that, "overloadMethod"); // TODO: aspectj doesn't see overloadMethod
		if (ms==null) {
			proceed(that, scope, codeStream, valueRequired);
			return;
		}
		ms.generateCode(scope, codeStream, valueRequired);
		if (valueRequired)
			codeStream.generateImplicitConversion(that.implicitConversion);
		codeStream.recordPositionsFrom(codeStream.position, that.sourceStart);
		return;
	}
	
	pointcut resolveType(UnaryExpression that, BlockScope scope):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.UnaryExpression) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.UnaryExpression.resolveType(BlockScope)) &&
		args(scope);

	// nothing interesting here
	// TODO:
	TypeBinding around(UnaryExpression that, BlockScope scope): resolveType(that, scope) {
		boolean expressionIsCast;
		if ((expressionIsCast = that.expression instanceof CastExpression) == true) that.expression.bits |= DisableUnnecessaryCastCheck; // will check later on
		TypeBinding expressionType = that.expression.resolveType(scope);
		if (expressionType == null) {
			that.constant = Constant.NotAConstant;
			return null;
		}
		int expressionTypeID = expressionType.id;
		// autoboxing support
		boolean use15specifics = scope.compilerOptions().sourceLevel >= ClassFileConstants.JDK1_5;
		if (use15specifics) {
			if (!expressionType.isBaseType()) {
				expressionTypeID = scope.environment().computeBoxingType(expressionType).id;
			}
		}
		if (expressionTypeID > 15) {
			TypeBinding res = overloadUnaryOperator(that, scope);
			if (res != null)
				return res;
			that.constant = Constant.NotAConstant;
			scope.problemReporter().invalidOperator(that, expressionType);
			return null;
		}

		int tableId;
		switch ((that.bits & OperatorMASK) >> OperatorSHIFT) {
			case NOT :
				tableId = AND_AND;
				break;
			case TWIDDLE :
				tableId = LEFT_SHIFT;
				break;
			default :
				tableId = MINUS;
		} //+ and - cases

		// the code is an int
		// (cast)  left   Op (cast)  rigth --> result
		//  0000   0000       0000   0000      0000
		//  <<16   <<12       <<8    <<4       <<0
		int operatorSignature = OperatorSignatures[tableId][(expressionTypeID << 4) + expressionTypeID];
		that.expression.computeConversion(scope, TypeBinding.wellKnownType(scope, (operatorSignature >>> 16) & 0x0000F), expressionType);
		that.bits |= operatorSignature & 0xF;
		switch (operatorSignature & 0xF) { // only switch on possible result type.....
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
			default : //error........
				that.constant = Constant.NotAConstant;
				if (expressionTypeID != T_undefined)
					scope.problemReporter().invalidOperator(that, expressionType);
				return null;
		}
		// compute the constant when valid
		if (that.expression.constant != Constant.NotAConstant) {
			that.constant =
				Constant.computeConstantOperation(
					that.expression.constant,
					expressionTypeID,
					(that.bits & OperatorMASK) >> OperatorSHIFT);
		} else {
			that.constant = Constant.NotAConstant;
			if (((that.bits & OperatorMASK) >> OperatorSHIFT) == NOT) {
				Constant cst = that.expression.optimizedBooleanConstant();
				if (cst != Constant.NotAConstant)
					that.optimizedBooleanConstant = BooleanConstant.fromValue(!cst.booleanValue());
			}
		}
		if (expressionIsCast) {
		// check need for operand cast
			CastExpression.checkNeedForArgumentCast(scope, tableId, operatorSignature, that.expression, expressionTypeID);
		}
		return that.resolvedType;
	}
}
