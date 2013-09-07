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

import org.eclipse.jdt.internal.compiler.ast.ArrayReference;
import org.eclipse.jdt.internal.compiler.ast.Assignment;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

// TODO: remove privileged
@SuppressWarnings("restriction")
public privileged aspect AssignmentAspect {
	static ThreadLocal<BlockScope> _scope = new ThreadLocal<BlockScope>();
	// catch scope param
	before(BlockScope scope):
		execution(* org.eclipse.jdt.internal.compiler.ast.Assignment.resolveType(BlockScope))
		&& args(scope)
	{
		_scope.set(scope);
	}

	before(Assignment that, Expression e):
		withincode(* org.eclipse.jdt.internal.compiler.ast.Assignment.resolveType(..))
		&& call(* org.eclipse.jdt.internal.compiler.ast.Expression.getDirectBinding(..))
		&& this(that) && args(e)
	{
		if (e!=that.lhs) return; // ignore second call to getDirectBinding
		BlockScope scope = _scope.get();
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
					ExpressionAspect.setTranslate(alhs, ms);
					that.resolvedType = ms.resolvedType;
				}
			}
		}
	}
}
