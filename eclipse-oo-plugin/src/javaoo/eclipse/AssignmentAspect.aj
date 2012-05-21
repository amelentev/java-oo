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
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.Reference;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.TagBits;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

// TODO: remove privileged
@SuppressWarnings("restriction")
public privileged aspect AssignmentAspect {
	pointcut resolveType(Assignment that, BlockScope scope):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.Assignment) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.Assignment.resolveType(BlockScope)) &&
		args(scope);
	
	// TODO: cut before scope.problemReporter().typeMismatchError
	TypeBinding around(Assignment that, BlockScope scope):
			resolveType(that, scope) {
		// due to syntax lhs may be only a NameReference, a FieldReference or an ArrayReference
		that.constant = Constant.NotAConstant;
		if (!(that.lhs instanceof Reference) || that.lhs.isThis()) {
			scope.problemReporter().expressionShouldBeAVariable(that.lhs);
			return null;
		}
		TypeBinding lhsType = that.lhs.resolveType(scope);
		that.expression.setExpectedType(lhsType); // needed in case of generic method invocation
		if (lhsType != null) {
			that.resolvedType = lhsType.capture(scope, that.sourceEnd);
		}
		LocalVariableBinding localVariableBinding = that.lhs.localVariableBinding();
		if (localVariableBinding != null) {
			localVariableBinding.tagBits &= ~TagBits.IsEffectivelyFinal;
		}
		TypeBinding rhsType = that.expression.resolveType(scope);
		if (lhsType == null || rhsType == null) {
			return null;
		}
		// check for assignment with no effect
		Binding left = that.getDirectBinding(that.lhs);
		if (left != null && !left.isVolatile() && left == that.getDirectBinding(that.expression)) {
			scope.problemReporter().assignmentHasNoEffect(that, left.shortReadableName());
		}
		
		if (that.lhs instanceof ArrayReference) {
			ArrayReference alhs = (ArrayReference) that.lhs;
			if (!alhs.receiver.resolvedType.isArrayType()) {
				Expression[] args = new Expression[]{alhs.position, that.expression}; 
				MessageSend ms = Utils.findMethod(scope, alhs.receiver, "set", args); //$NON-NLS-1$
				if (ms==null)
					ms = Utils.findMethod(scope, alhs.receiver, "put", args); //$NON-NLS-1$
				if (ms==null)
					scope.problemReporter().referenceMustBeArrayTypeAt(alhs.receiver.resolvedType, alhs);
				else {
					Utils.set(alhs, "overloadMethod", ms);
					that.resolvedType = ms.resolvedType;
				}
			}
		}
		
		// Compile-time conversion of base-types : implicit narrowing integer into byte/short/character
		// may require to widen the rhs expression at runtime
		if (lhsType != rhsType) { // must call before computeConversion() and typeMismatchError()
			scope.compilationUnitScope().recordTypeConversion(lhsType, rhsType);
		}
		if (that.expression.isConstantValueOfTypeAssignableToType(rhsType, lhsType)
				|| rhsType.isCompatibleWith(lhsType)) {
			that.expression.computeConversion(scope, lhsType, rhsType);
			that.checkAssignment(scope, lhsType, rhsType);
			if (that.expression instanceof CastExpression
					&& (that.expression.bits & ASTNode.UnnecessaryCast) == 0) {
				CastExpression.checkNeedForAssignedCast(scope, lhsType, (CastExpression) that.expression);
			}
			return that.resolvedType;
		} else if (that.isBoxingCompatible(rhsType, lhsType, that.expression, scope)) {
			that.expression.computeConversion(scope, lhsType, rhsType);
			if (that.expression instanceof CastExpression
					&& (that.expression.bits & ASTNode.UnnecessaryCast) == 0) {
				CastExpression.checkNeedForAssignedCast(scope, lhsType, (CastExpression) that.expression);
			}
			return that.resolvedType;
		}
		scope.problemReporter().typeMismatchError(rhsType, lhsType, that.expression, that.lhs);
		return lhsType;
	}
}
