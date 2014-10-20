import java.util.HashMap;

public class MapIndex {
    public static boolean test() {
        HashMap<String, String> map = new HashMap<>();
        // without OO:
        map.put("qwe", "qwe");
        String r1 = map.get("qwe");
        // with OO:
        map["qwe"] = "asd";
        String r2 = map["qwe"];

        return "asd".equals(r2) && "qwe".equals(r1);
    }
    public static void main(String[] args) {
		System.out.println(MapIndex.test());
	}
}
