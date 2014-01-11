package javaoo.eclipse;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

@SuppressWarnings("restriction")
public aspect ExpressionAspect {
	private MessageSend Expression.translate;

	public static MessageSend getTranslate(Expression e) {
		return e.translate;
	}
	public static void setTranslate(Expression e, MessageSend t) {
		e.translate = t;
	}
	public static Expression removeAndGetTranslate(Expression x) {
		MessageSend e = getTranslate(x);
		setTranslate(x, null); // to prevent loop
		if (!Arrays.equals("valueOf".toCharArray(), e.selector)) {
			x.implicitConversion = e.implicitConversion = x.implicitConversion | e.implicitConversion;
			x.bits = e.bits = x.bits | e.bits;
		}
		return e;
	}

	pointcut generateCode(Expression that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
		execution(* org.eclipse.jdt.internal.compiler.ast.Expression.generateCode(BlockScope, CodeStream, boolean)) &&
		this(that) && args(currentScope, codeStream, valueRequired);

	void around(Expression that, BlockScope currentScope, CodeStream codeStream, boolean valueRequired):
			generateCode(that, currentScope, codeStream, valueRequired) {
		if (getTranslate(that) == null) {
			proceed(that, currentScope, codeStream, valueRequired);
		} else {
			ExpressionAspect.removeAndGetTranslate(that).generateCode(currentScope, codeStream, valueRequired);
		}
	}

	void around(Expression that, Scope scope, TypeBinding runtimeType, TypeBinding compileTimeType):
		execution(* org.eclipse.jdt.internal.compiler.ast.Expression.computeConversion(..)) &&
		this(that) && args(scope, runtimeType, compileTimeType)
	{
		MessageSend translate = getTranslate(that);
		if (translate != null) {
			if (!Arrays.equals("valueOf".toCharArray(), translate.selector))
				translate.computeConversion(scope, runtimeType, compileTimeType);
		} else
			proceed(that, scope, runtimeType, compileTimeType);
	}
}
