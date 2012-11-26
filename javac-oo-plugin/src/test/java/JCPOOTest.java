import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sun.tools.javac.Main;
import sun.misc.Unsafe;

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
    @Test public void testCompAss() throws Exception {
        compile("CompAss", "../tests");
    }
    void compile(String clas) throws Exception {
        compile(clas, "../examples/");
    }
    void compile(String clas, String path) throws Exception {
        String file = path+"/"+clas+".java";
        String opts = file + " -processor javaoo.javac.OOProcessor -d target/test-classes";
        assertEquals("compilation failed", 0, Main.compile(opts.split(" ")));
        assertEquals(true, Class.forName(clas).getDeclaredMethod("test").invoke(null));
    }
}
