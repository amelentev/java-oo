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

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

public class OOTransTypes extends TransTypes {
    public static OOTransTypes instance(Context context) {
        TransTypes res = context.get(transTypesKey);
        if (res instanceof OOTransTypes) return (OOTransTypes) res;
        context.put(transTypesKey, (TransTypes)null);
        return new OOTransTypes(context);
    }
    protected OOTransTypes(Context context) {
        super(context);
        attr = OOAttr.instance(context);
    }
    private OOAttr attr;

    @Override
    public <T extends JCTree> T translate(T tree) {
        JCTree.JCExpression t = attr.translateMap.remove(tree);
        if (t!=null)
            return (T) translate(t);
        return super.translate(tree);
    }
}
