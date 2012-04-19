import java.util.HashMap;
import java.util.Map;
public class MapIndexTest {
    public static boolean test() {
        Map<String, String> map = new HashMap<String, String>();
        map["qwe"] = "asd";
        return map["qwe"].equals("asd");
    }
}
