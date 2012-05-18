package javaoo.eclipse;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class Utils {
	public static Object get(Object o, String field) {
		try {
			return o.getClass().getField(field).get(o);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static void set(Object o, String field, Object val) {
		try {
			o.getClass().getField(field).set(o, val);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	public static MessageSend findMethod(Scope scope, Expression receiver, String selector, Expression[] args) {
		char[] s = selector.toCharArray();
		MessageSend ms = new MessageSend();
		ms.receiver = receiver;
		ms.selector = s;
		ms.arguments = args;
		ms.actualReceiverType = receiver.resolvedType;
		TypeBinding[] targs = new TypeBinding[args.length];
		for (int i = 0; i < args.length; i++)
			targs[i] = args[i].resolvedType;
		ms.binding = scope.getMethod(ms.actualReceiverType, s, targs, ms);
		if (ms.binding != null && ms.binding.isValidBinding()) {
			ms.resolvedType = ms.binding.returnType;
			ms.constant = Constant.NotAConstant;
			ms.sourceStart = receiver.sourceStart;
			ms.sourceEnd = receiver.sourceEnd;
			return ms;
		}
		return null;
	}
}
