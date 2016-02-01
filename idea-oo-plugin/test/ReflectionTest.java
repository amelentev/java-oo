import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.extensions.impl.ExtensionComponentAdapter;
import com.intellij.openapi.extensions.impl.ExtensionPointImpl;
import com.intellij.psi.impl.source.tree.JavaElementType;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import javaoo.idea.Util;
import org.junit.Test;

import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

// To check on Community AND Ultimate editions
public class ReflectionTest {
    @Test public void findField() {
        assertTrue(asList("myErrorCount", "f").contains(Util.findField(HighlightInfoHolder.class, int.class).getName()));
        assertTrue(asList("myInfos", "c").contains(Util.findField(HighlightInfoHolder.class, List.class).getName()));
        assertEquals("myExtensionAdapters", Util.findField(ExtensionPointImpl.class, Set.class).getName());
        assertEquals("myImplementationClass", Util.findField(ExtensionComponentAdapter.class, Class.class).getName());
    }

    @Test public void setJavaElementConstructor() {
        Util.setJavaElementConstructor(JavaElementType.BINARY_EXPRESSION, PsiBinaryExpressionImpl.class);
    }
}
