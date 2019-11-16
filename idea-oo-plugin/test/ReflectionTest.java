import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.extensions.impl.ExtensionComponentAdapter;
import com.intellij.openapi.extensions.impl.ExtensionPointImpl;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import javaoo.idea.Util;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// To check on Community AND Ultimate editions
public class ReflectionTest {
    @Test public void findField() {
        assertTrue(asList("myErrorCount", "b").contains(Util.findField(HighlightInfoHolder.class, int.class).getName()));
        assertTrue(asList("myInfos", "f").contains(Util.findField(HighlightInfoHolder.class, List.class).getName()));
        assertTrue(asList("myConstructor", "a").contains(Util.findField(JavaElementType.JavaCompositeElementType.class, Supplier.class).getName()));
        assertEquals("myAdapters", Util.findField(ExtensionPointImpl.class, List.class, "myAdapters").getName());
        assertEquals("myImplementationClassOrName", Util.findField(ExtensionComponentAdapter.class, Object.class, "myImplementationClassOrName").getName());
    }

    @Test public void setJavaElementConstructor() {
        Util.setJavaElementConstructor(JavaElementType.BINARY_EXPRESSION, PsiBinaryExpressionImpl::new);
    }
}
