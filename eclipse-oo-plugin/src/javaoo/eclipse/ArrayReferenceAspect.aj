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

import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

@SuppressWarnings("restriction")
public aspect ArrayReferenceAspect implements TypeIds {
	declare precedence: ExpressionAspect;

	ThreadLocal<BlockScope> _scope = new ThreadLocal<BlockScope>();
	TypeBinding around(BlockScope scope):
		execution(* ArrayReference.resolveType(BlockScope)) && args(scope)
	{
		try {
			_scope.set(scope);
			return proceed(scope);
		} catch (ReturnException e) {
			return (TypeBinding) e.getReturn();
		}
	}
	
	void around(ArrayReference that):
		withincode(* ArrayReference.resolveType(BlockScope))
		&& call(* ProblemReporter.referenceMustBeArrayTypeAt(TypeBinding, ArrayReference)) && args(*, that)
	{
		BlockScope scope = _scope.get();
		that.position.resolveType(scope);
		MessageSend ms = Utils.findMethod(scope, that.receiver, "get", new Expression[]{that.position}); //$NON-NLS-1$
		if (ms == null)
			proceed(that);
		else {
			ExpressionAspect.setTranslate(that, ms);
			that.resolvedType = ms.resolvedType;
			throw new ReturnException(that.resolvedType);
		}
	}

	pointcut generateAssignment(ArrayReference that, BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.ArrayReference) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.ArrayReference.generateAssignment(BlockScope, CodeStream, Assignment, boolean)) &&
		args(currentScope, codeStream, assignment, valueRequired);
	
	void around(ArrayReference that, BlockScope currentScope, CodeStream codeStream, Assignment assignment, boolean valueRequired):
			generateAssignment(that, currentScope, codeStream, assignment, valueRequired) {
		if (ExpressionAspect.getTranslate(that)==null) {
			proceed(that, currentScope, codeStream, assignment, valueRequired);
		} else {
			ExpressionAspect.removeAndGetTranslate(that).generateCode(currentScope, codeStream, valueRequired);
		}
	}
	
	pointcut generateCode(ArrayReference that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
		this(that) && within(org.eclipse.jdt.internal.compiler.ast.ArrayReference) &&
		execution(* org.eclipse.jdt.internal.compiler.ast.ArrayReference.generateCode(BlockScope, CodeStream, boolean)) &&
		args(currentScope, codeStream, valueRequired);
	
	void around(ArrayReference that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
			generateCode(that, currentScope, codeStream, valueRequired) {
		if (ExpressionAspect.getTranslate(that) == null) {
			proceed(that, currentScope, codeStream, valueRequired);
		} else {
			ExpressionAspect.removeAndGetTranslate(that).generateCode(currentScope, codeStream, valueRequired);
		}
	}
}
