/* Copyright 2016 Artem Melentyev <amelentev@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javaoo.idea;

import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl;
import com.intellij.psi.JavaElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiResolveHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.org.objectweb.asm.*;
import org.jetbrains.org.objectweb.asm.commons.Remapper;
import org.jetbrains.org.objectweb.asm.commons.RemappingClassAdapter;

import java.util.function.Function;

/** Signature of {@link com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl} but with public constructor */
public class FakeHighlightVisitorImpl extends JavaElementVisitor implements HighlightVisitor {
    public final @NotNull PsiResolveHelper myResolveHelper;

    public FakeHighlightVisitorImpl(@NotNull PsiResolveHelper resolveHelper) {
        myResolveHelper = resolveHelper;
    }

    final static public boolean isFake = true;

    public static final Function<ClassVisitor, ClassVisitor> transformer = cw -> {
        // rename HighlightVisitorImpl to HighlightVisitorImpl_public including inner classes
        Remapper renamer = new Remapper() {
            @Override
            public String map(String typeName) {
                String internalName = Type.getInternalName(HighlightVisitorImpl.class);
                if (typeName.equals(internalName)) {
                    return typeName+"_public";
                } else if (typeName.startsWith(internalName)) {
                    int i = typeName.indexOf('$');
                    return typeName.substring(0, i) + "_public" + typeName.substring(i);
                }
                return super.map(typeName);
            }
        };
        RemappingClassAdapter renameVisitor = new RemappingClassAdapter(cw, renamer);
        // make public constructor
        return new ClassVisitor(Opcodes.ASM5, renameVisitor) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if ("<init>".equals(name))
                    access = Opcodes.ACC_PUBLIC; // public constructor
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
    };

    //<editor-fold desc="generated code below">
    @Override
    public boolean suitableForFile(@NotNull PsiFile file) {
        return false;
    }

    @Override
    public void visit(@NotNull PsiElement element) {

    }

    @Override
    public boolean analyze(@NotNull PsiFile file, boolean updateWholeFile, @NotNull HighlightInfoHolder holder, @NotNull Runnable action) {
        return false;
    }

    @NotNull
    @Override
    public HighlightVisitor clone() {
        return null;
    }

    @Override
    public int order() {
        return 0;
    }
    //</editor-fold>
}
