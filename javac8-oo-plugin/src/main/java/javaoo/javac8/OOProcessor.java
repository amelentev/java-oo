/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 
 * GNU Public License v2.0 + OpenJDK assembly exception.
 * 
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 ******************************************************************************/
package javaoo.javac8;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.comp.*;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class OOProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JavacProcessingEnvironment pe = (JavacProcessingEnvironment) processingEnv;
        JavaCompiler compiler = JavaCompiler.instance(pe.getContext());
        try {
            ClassLoader pclassloader = (ClassLoader) get(pe, "processorClassLoader");
            if (pclassloader != null && (!pclassloader.getClass().equals(ClassLoader.class)))
                // do not let compiler to close our classloader. we need it later.
                set(pe, JavacProcessingEnvironment.class, "processorClassLoader", new ClassLoader(pclassloader) {});
            if (pclassloader == null) { // netbeans
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Injecting OO to netbeans");
                patch(compiler, OOProcessor.class.getClassLoader());
                return;
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Injecting OO to javac8");
            final MultiTaskListener taskListener = (MultiTaskListener) get(compiler, "taskListener");
            taskListener.add(new WaitAnalyzeTaskListener(compiler, pclassloader));
        } catch (Exception e) {
            sneakyThrow(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    static class WaitAnalyzeTaskListener implements TaskListener {
        JavaCompiler compiler;
        ClassLoader pclassloader;
        boolean done = false;
        public WaitAnalyzeTaskListener(JavaCompiler compiler, ClassLoader pclassloader) {
            this.compiler = compiler;
            this.pclassloader = pclassloader;
        }
        @Override
        public void started(TaskEvent e) {
            if (!done && e.getKind() == TaskEvent.Kind.ANALYZE) {
                patch(compiler, pclassloader);
                done = true;
            }
        }
        @Override
        public void finished(TaskEvent e) {}
    }

    static void patch(JavaCompiler compiler, ClassLoader pcl) {
        try {
            JavaCompiler delCompiler = (JavaCompiler) get(compiler, "delegateCompiler");
            if (delCompiler != null)
                compiler = delCompiler; // javac has delegateCompiler. netbeans hasn't
            Context context = (Context) get(compiler, "context");
            Attr attr = Attr.instance(context);
            if (attr instanceof OOAttr)
                return;
            ClassLoader destcl = attr.getClass().getClassLoader();

            // hack: load classes to the same classloader so they will be able to use and override default accessor members
            Class<?> attrClass = reloadClass("com.sun.tools.javac.comp.OOAttr", pcl, destcl);
            Class<?> resolveClass = reloadClass("com.sun.tools.javac.comp.OOResolve", pcl, destcl);
            Class<?> transTypesClass = reloadClass("com.sun.tools.javac.comp.OOTransTypes", pcl, destcl);
            reloadClass("javaoo.OOMethods", pcl, destcl);
            reloadClass("javaoo.OOMethods$1", pcl, destcl);
            reloadClass("javaoo.OOMethods$2", pcl, destcl);

            getInstance(resolveClass, context);
            attr = (Attr) getInstance(attrClass, context);
            Object transTypes = getInstance(transTypesClass, context);

            set(compiler, JavaCompiler.class, "attr", attr);
            set(compiler, JavaCompiler.class, "transTypes", transTypes);
            set(MemberEnter.instance(context), MemberEnter.class, "attr", attr);
        } catch (Exception e) {
            sneakyThrow(e);
        }
    }
    @SuppressWarnings("unchecked")
    /** add class claz to outClassLoader */
    static <T> Class<T> reloadClass(final String claz, ClassLoader incl, ClassLoader outcl) throws Exception {
        try { // already loaded?
            return (Class<T>) outcl.loadClass(claz);
        } catch (ClassNotFoundException e) {}
        String path = claz.replace('.', '/') + ".class";
        InputStream is = incl.getResourceAsStream(path);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        Method m = ClassLoader.class.getDeclaredMethod("defineClass", new Class[] {
                String.class, byte[].class, int.class, int.class });
        m.setAccessible(true);
        return (Class<T>) m.invoke(outcl, claz, bytes, 0, bytes.length);
    }

    // reflection stuff
    static Object getInstance(Class<?> clas, Context context) throws ReflectiveOperationException {
        return clas.getDeclaredMethod("instance", Context.class).invoke(null, context);
    }
    static Object get(Object obj, String field) throws ReflectiveOperationException {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(obj);
    }
    static void set(Object obj, Class clas, String field, Object val) throws ReflectiveOperationException {
        Field f = clas.getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, val);
    }
    public static void sneakyThrow(Throwable ex) {
        OOProcessor.<RuntimeException>sneakyThrowInner(ex);
    }
    private static <T extends Throwable> T sneakyThrowInner(Throwable ex) throws T {
        throw (T) ex;
    }
}
