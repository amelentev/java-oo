/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 
 * GNU Public License v2.0 + OpenJDK assembly exception.
 * 
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 ******************************************************************************/
package com.sun.tools.javac.comp;

import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

public class OOTransTypes extends TransTypes {
    public static OOTransTypes hook(Context context) {
        context.put(transTypesKey, (TransTypes)null);
        return new OOTransTypes(context);
    }
    protected OOTransTypes(Context context) {
        super(context);
        syms = Symtab.instance(context);
        types = Types.instance(context);
    }
    protected Types types;
    protected Symtab syms;

    @Override
    public void visitIndexed(JCTree.JCArrayAccess tree) {
        if (types.isArray(tree.indexed.type))
            super.visitIndexed(tree);
        // index overload
        tree.indexed = translate(tree.indexed, types.erasure(tree.indexed.type));
        tree.index = translate(tree.index, syms.intType);
        result = tree;
    }
}
