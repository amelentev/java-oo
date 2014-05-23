import com.intellij.codeInsight.daemon.impl.analysis.HighlightInfoHolder;
import com.intellij.openapi.extensions.impl.ExtensionPointImpl;
import com.intellij.psi.impl.source.tree.JavaElementType;
import javaoo.idea.Util;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertNotNull;

public class ReflectionTest {
    @Test
    public void testFindField() {
        assertNotNull(Util.findField(HighlightInfoHolder.class, int.class)); // "myErrorCount"
        assertNotNull(Util.findField(ExtensionPointImpl.class, Set.class)); //"myExtensionAdapters"
        assertNotNull(Util.findField(HighlightInfoHolder.class, List.class)); //"myInfos"
        assertNotNull(Util.findField(JavaElementType.JavaCompositeElementType.class, Constructor.class));  //"myConstructor"
    }
}
