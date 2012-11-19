package javaoo.eclipse;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public aspect ExpressionAspect {
	public Expression Expression.translate;

	pointcut generateCode(Expression that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
		execution(* org.eclipse.jdt.internal.compiler.ast.Expression.generateCode(BlockScope, CodeStream, boolean)) &&
		this(that) && args(currentScope, codeStream, valueRequired);

	void around(Expression that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
			generateCode(that, currentScope, codeStream, valueRequired) {
		if (that.translate == null) {
			proceed(that, currentScope, codeStream, valueRequired);
		} else {
			Utils.removeAndGetTranslate(that).generateCode(currentScope, codeStream, valueRequired);
		}
	}
}
