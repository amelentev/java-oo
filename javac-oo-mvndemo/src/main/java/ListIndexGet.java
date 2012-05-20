import java.util.Arrays;
import java.util.List;

public class ListIndexGet {
    public static boolean test() {
        List<Integer> lst = Arrays.asList(1);
        // without OO:
        int resl1 = lst.get(0);
        // with OO:
        int resl2 = lst[0];
        return resl2 == resl1;
    }
    public static void main(String[] args) {
		System.out.println(ListIndexGet.test());
	}
}