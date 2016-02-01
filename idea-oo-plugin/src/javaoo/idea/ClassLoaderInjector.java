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

import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl;
import javaoo.OOMethods;
import org.jetbrains.org.objectweb.asm.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

public class ClassLoaderInjector {
    /** for GC not to dispose of injected classes */
    public Collection<Class> injectedClasses = new ArrayList<>();

    public Class injectOOHighlightVisitorImplClass(ClassLoader classLoader) throws IOException {
        injectedClasses.clear();
        injectedClasses.add( transformClass(classLoader, HighlightVisitorImpl.class.getName()+"_public",
                HighlightVisitorImpl.class.getName(), FakeHighlightVisitorImpl.transformer) );
        for (String ic : getInnerClasses(HighlightVisitorImpl.class)) {
            ic = ic.replace('/', '.');
            int i = ic.indexOf('$');
            String newName = ic.substring(0, i) + "_public" + ic.substring(i);
            injectedClasses.add( transformClass(classLoader, newName, ic, FakeHighlightVisitorImpl.transformer) );
        }

        List<String> referencedClasses = Arrays.asList(
                OOResolver.class.getName(), Util.class.getName(), OOMethods.class.getName(), Util.class.getName(),
                OOMethods.class.getName() + "$1", OOMethods.class.getName() + "$2");
        for (String className : referencedClasses)
            injectedClasses.add(transformClass(classLoader, className, className, (cv -> cv)));
        Class res = transformClass(classLoader, OOHighlightVisitorImpl.class.getName(),
                OOHighlightVisitorImpl.class.getName(), OOHighlightVisitorImpl.transformer);
        injectedClasses.add(res);
        return res;
    }

    static Class transformClass(ClassLoader classLoader, String className, String origClassName,
                           Function<ClassVisitor, ClassVisitor> transformer) throws IOException {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException ignored) {}
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = transformer.apply(cw);
        try (InputStream is = ClassLoaderInjector.class.getClassLoader().getResourceAsStream(origClassName.replace('.', '/') + ".class")) {
            ClassReader cr = new ClassReader(is);
            cr.accept(cv, ClassReader.EXPAND_FRAMES);
        }
        byte[] bytes = cw.toByteArray();
        return (Class) Util.invoke(ClassLoader.class, classLoader, "defineClass",
                new Class[]{String.class, byte[].class, int.class, int.class},
                className, bytes, 0, bytes.length);
    }

    static Set<String> getInnerClasses(Class<?> type) throws IOException {
        ClassLoader classLoader = type.getClassLoader();
        final Set<String> innerClasses = new HashSet<>();
        try (InputStream is = classLoader.getResourceAsStream(Type.getInternalName(type) + ".class")) {
            ClassReader cr = new ClassReader(is);
            cr.accept(new ClassVisitor(Opcodes.ASM5) {
                @Override
                public void visitInnerClass(String name, String outerName, String innerName, int access) {
                    if (name.startsWith(Type.getInternalName(type)+"$"))
                        innerClasses.add(name);
                    super.visitInnerClass(name, outerName, innerName, access);
                }
            }, 0);
        }
        return innerClasses;
    }
}
