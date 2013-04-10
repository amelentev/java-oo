/*******************************************************************************
 * Copyright (c) 2012 Artem Melentyev <amelentev@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the 
 * GNU Public License v2.0 + OpenJDK assembly exception.
 * 
 * Contributors:
 *     Artem Melentyev <amelentev@gmail.com> - initial API and implementation
 ******************************************************************************/
package javaoo.javac;

import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Lower;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.comp.TransTypes;
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
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class OOProcessor extends AbstractProcessor {
    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        JavacProcessingEnvironment pe = (JavacProcessingEnvironment) processingEnv;
        JavaCompiler compiler = JavaCompiler.instance(pe.getContext());
        try {
            ClassLoader pclassloader = (ClassLoader) get(pe, "processorClassLoader");
            if (pclassloader != null && (!pclassloader.getClass().equals(ClassLoader.class)))
                set(pe, "processorClassLoader", new ClassLoader(pclassloader) {});
            if (pclassloader == null) { // netbeans
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Injecting OO to netbeans");
                patch(compiler, OOProcessor.class.getClassLoader());
                return;
            }
            processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Injecting OO to javac");
            final TaskListener oldTaskListener = (TaskListener) get(compiler, "taskListener");
            if (oldTaskListener instanceof WaitAnalyzeTaskListener)
                return;
            TaskListener newTaskListener = new WaitAnalyzeTaskListener(oldTaskListener, compiler, pclassloader);
            set(compiler, "taskListener", newTaskListener);
            pe.getContext().put(TaskListener.class, (TaskListener)null);
            pe.getContext().put(TaskListener.class, newTaskListener);
        } catch (Exception e) {
            if (e instanceof RuntimeException)
                throw (RuntimeException)e;
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        return false;
    }

    static class WaitAnalyzeTaskListener implements TaskListener {
        TaskListener oldTaskListener;
        JavaCompiler compiler;
        ClassLoader pclassloader;
        boolean done = false;
        public WaitAnalyzeTaskListener(TaskListener old, JavaCompiler compiler, ClassLoader pclassloader) {
            this.oldTaskListener = old;
            this.compiler = compiler;
            this.pclassloader = pclassloader;
        }
        @Override
        public void started(TaskEvent e) {
            if (oldTaskListener != null)
                oldTaskListener.started(e);
            if (e.getKind() == TaskEvent.Kind.ANALYZE && !done) {
                patch(compiler, pclassloader);
                done = true;
            }
        }
        @Override
        public void finished(TaskEvent e) {
            if (oldTaskListener != null)
                oldTaskListener.finished(e);
        }
    }

    static Object get(Object obj, String field) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        return f.get(obj);
    }
    static void set(Object obj, String field, Object val) throws NoSuchFieldException, IllegalAccessException {
        Field f = obj.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(obj, val);
    }

    static void patch(JavaCompiler compiler, ClassLoader pcl) {
        try {
            JavaCompiler delCompiler = (JavaCompiler) get(compiler, "delegateCompiler");
            if (delCompiler != null)
                compiler = delCompiler; // javac has delegateCompiler. netbeans hasn't
            Context context = (Context) get(compiler, "context");

            // hack: load classes to the same classloader as Resolve&Lower so they will be able to use and override default accessor members
            Class<?> attrClass = reloadClass("com.sun.tools.javac.comp.OOAttr", pcl, Attr.class.getClassLoader());
            Class<?> resolveClass = reloadClass("com.sun.tools.javac.comp.OOResolve", pcl, Resolve.class.getClassLoader());
            Class<?> lowerClass = reloadClass("com.sun.tools.javac.comp.OOLower", pcl, Lower.class.getClassLoader());
            Class<?> transTypesClass = reloadClass("com.sun.tools.javac.comp.OOTransTypes", pcl, TransTypes.class.getClassLoader());
            reloadClass("javaoo.OOMethods", pcl, TransTypes.class.getClassLoader());
            reloadClass("javaoo.OOMethods$1", pcl, TransTypes.class.getClassLoader());
            reloadClass("javaoo.OOMethods$2", pcl, TransTypes.class.getClassLoader());

            resolveClass.getDeclaredMethod("instance", Context.class).invoke(null, context);
            Object attr = attrClass.getDeclaredMethod("instance", Context.class).invoke(null, context);
            Object lower = lowerClass.getDeclaredMethod("instance", Context.class).invoke(null, context);
            Object transTypes = transTypesClass.getDeclaredMethod("instance", Context.class).invoke(null, context);

            set(compiler, "attr", attr);
            set(MemberEnter.instance(context), "attr", attr);
            set(compiler, "lower", lower);
            set(compiler, "transTypes", transTypes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    /** add class claz to outClassLoader */
    static <T> Class<T> reloadClass(String claz, ClassLoader incl, ClassLoader outcl) throws Exception {
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
}
