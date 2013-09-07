package javaoo.eclipse;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

@SuppressWarnings("restriction")
public aspect ExpressionAspect {
	private Expression Expression.translate;

	public static Expression getTranslate(Expression e) {
		return e.translate;
	}
	public static void setTranslate(Expression e, Expression t) {
		e.translate = t;
	}
	public static Expression removeAndGetTranslate(Expression x) {
		Expression e = getTranslate(x);
		setTranslate(x, null); // to prevent loop
		return e;
	}

	pointcut generateCode(Expression that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
		execution(* org.eclipse.jdt.internal.compiler.ast.Expression.generateCode(BlockScope, CodeStream, boolean)) &&
		this(that) && args(currentScope, codeStream, valueRequired);

	void around(Expression that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
			generateCode(that, currentScope, codeStream, valueRequired) {
		if (that.translate == null) {
			proceed(that, currentScope, codeStream, valueRequired);
		} else {
			ExpressionAspect.removeAndGetTranslate(that).generateCode(currentScope, codeStream, valueRequired);
		}
	}
}
