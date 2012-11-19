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
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.CastExpression;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.NullLiteral;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

@SuppressWarnings("restriction")
public aspect ArrayReferenceAspect implements TypeIds {
	pointcut resolveType(ArrayReference that, BlockScope scope):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.ArrayReference) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.ArrayReference.resolveType(BlockScope)) &&
		args(scope);
	
	// TODO: after receiver.resolveType
	TypeBinding around(ArrayReference that, BlockScope scope):
			resolveType(that, scope) {
		that.constant = Constant.NotAConstant;
		if (that.receiver instanceof CastExpression	// no cast check for ((type[])null)[0]
				&& ((CastExpression)that.receiver).innermostCastedExpression() instanceof NullLiteral) {
			that.receiver.bits |= ASTNode.DisableUnnecessaryCastCheck; // will check later on
		}
		TypeBinding arrayType = that.receiver.resolveType(scope);
		if (arrayType != null) {
			that.receiver.computeConversion(scope, arrayType, arrayType);
			if (arrayType.isArrayType()) {
				TypeBinding elementType = ((ArrayBinding) arrayType).elementsType();
				that.resolvedType = ((that.bits & ASTNode.IsStrictlyAssigned) == 0) ? elementType.capture(scope, that.sourceEnd) : elementType;
			} else {
				that.position.resolveType(scope);
				MessageSend ms = Utils.findMethod(scope, that.receiver, "get", new Expression[]{that.position}); //$NON-NLS-1$
				if (ms == null)
					scope.problemReporter().referenceMustBeArrayTypeAt(arrayType, that);
				else {
					that.translate = ms;
					return that.resolvedType = ms.resolvedType;
				}
			}
		}
		TypeBinding positionType = that.position.resolveTypeExpecting(scope, TypeBinding.INT);
		if (positionType != null) {
			that.position.computeConversion(scope, TypeBinding.INT, positionType);
		}
		return that.resolvedType;
	}
	
	pointcut generateAssignment(ArrayReference that, BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.ArrayReference) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.ArrayReference.generateAssignment(BlockScope, CodeStream, Assignment, boolean)) &&
		args(currentScope, codeStream, assignment, valueRequired);
	
	void around(ArrayReference that, BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired):
			generateAssignment(that, currentScope, codeStream, assignment, valueRequired) {
		if (that.translate==null) {
			proceed(that, currentScope, codeStream, assignment, valueRequired);
		} else {
			Utils.removeAndGetTranslate(that).generateCode(currentScope, codeStream, valueRequired);
		}
	}
	
	pointcut generateCode(ArrayReference that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.ArrayReference) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.ArrayReference.generateCode(BlockScope, CodeStream, boolean)) &&
		args(currentScope, codeStream, valueRequired);
	
	void around(ArrayReference that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
			generateCode(that, currentScope, codeStream, valueRequired) {
		if (that.translate == null) {
			proceed(that, currentScope, codeStream, valueRequired);
		} else {
			Expression ms = Utils.removeAndGetTranslate(that);
			ms.generateCode(currentScope, codeStream, valueRequired);
			codeStream.checkcast(ms.resolvedType);
			// remaining code from #generateCode
			// Generating code for the potential runtime type checking
			if (valueRequired) {
				codeStream.generateImplicitConversion(that.implicitConversion);
			} else {
				boolean isUnboxing = (that.implicitConversion & TypeIds.UNBOXING) != 0;
				// conversion only generated if unboxing
				if (isUnboxing) codeStream.generateImplicitConversion(that.implicitConversion);
				switch (isUnboxing ? that.postConversionType(currentScope).id : that.resolvedType.id) {
					case T_long :
					case T_double :
						codeStream.pop2();
						break;
					default :
						codeStream.pop();
				}
			}
			codeStream.recordPositionsFrom(codeStream.position, that.sourceStart);
		}
	}
}
