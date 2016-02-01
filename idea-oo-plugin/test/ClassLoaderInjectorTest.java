import com.intellij.codeInsight.daemon.impl.HighlightVisitor;
import com.intellij.codeInsight.daemon.impl.analysis.HighlightVisitorImpl;
import com.intellij.psi.PsiResolveHelper;
import com.intellij.psi.impl.source.resolve.PsiResolveHelperImpl;
import javaoo.idea.ClassLoaderInjector;
import javaoo.idea.OOHighlightVisitorImpl;
import org.junit.Test;

import static org.junit.Assert.*;

public class ClassLoaderInjectorTest {
    @Test
    public void test() throws Exception {
        ClassLoader mycl = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(OOHighlightVisitorImpl.class.getName()))
                    return findClass(name);
                return super.loadClass(name);
            }
        };
        ClassLoaderInjector injector = new ClassLoaderInjector();
        Class clas = injector.injectOOHighlightVisitorImplClass(mycl);
        assertEquals(HighlightVisitorImpl.class.getName()+"_public", clas.getSuperclass().getName());
        PsiResolveHelperImpl prh = new PsiResolveHelperImpl(null);
        HighlightVisitor res = (HighlightVisitor) clas.getConstructor(PsiResolveHelper.class).newInstance(prh);
        assertNotNull(res);
        try {
            res.getClass().getSuperclass().getDeclaredField("isFake");
            assertTrue("found fake superclass", false);
        } catch (NoSuchFieldException e) {} // expected
    }
}
