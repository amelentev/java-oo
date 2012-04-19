import com.sun.tools.javac.Main;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.File;

public class OOTest {
    @Test public void test() throws Exception {
        compile("MathTest");
        compile("CmpTest");
        compile("ListIndexGetTest");
        compile("ListIndexSetTest");
        compile("MapIndexTest");
    }
    void compile(String clas) throws Exception {
        String file = "src/sample/"+clas+".java";
        String opts = file + " -processor javaoo.javac.OOProcessor -d target/test-classes";
        Main.compile(opts.split(" "));
        assertEquals(true, Class.forName(clas).getDeclaredMethod("test").invoke(null));
    }
}
