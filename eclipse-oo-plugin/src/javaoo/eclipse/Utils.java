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

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.MessageSend;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class Utils {
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
