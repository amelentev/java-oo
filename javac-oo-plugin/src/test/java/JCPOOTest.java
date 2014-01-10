import com.sun.tools.javac.Main;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class JCPOOTest {
    @Test public void testMath() throws Exception {
        compile("Math");
    }
    @Test public void testCmp() throws Exception {
        compile("Cmp");
    }
    @Test public void testList() throws Exception {
        compile("ListIndexGet");
        compile("ListIndexSet");
    }
    @Test public void testMap() throws Exception {
        compile("MapIndex");
    }
    @Test public  void testValueOf() throws Exception {
        compile("ValueOf");
    }
    @Test public void testDemo() throws Exception {
        compile("Demo");
    }
    @Test public void testVector() throws Exception {
        compile("Vector");
    }
    @Test public void testCompAss() throws Exception {
        compile("CompAss", "../tests");
    }
    @Test public void testVecMat() throws Exception {
        compile("VecMat", "../tests");
    }
    @Test public void testVec() throws Exception {
        compile("Vec", "../tests");
    }
    @Test public void testBoxing() throws Exception {
        compile("Boxing", "../tests");
    }
    void compile(String clas) throws Exception {
        compile(clas, "../examples/");
    }
    void compile(String clas, String path) throws Exception {
        String outputDir = "target/test-classes";
        if (!new File(outputDir).isDirectory()) { // workaround for IDEA current dir.
            path = path.substring(3); // remove "../"
            outputDir = "javac-oo-plugin/" + outputDir;
        }
        String file = path+"/"+clas+".java";
        String opts = file + " -processor javaoo.javac.OOProcessor -d "+outputDir;
        assertEquals("compilation failed", 0, Main.compile(opts.split(" ")));
        assertEquals(true, Class.forName(clas).getDeclaredMethod("test").invoke(null));
    }
}
