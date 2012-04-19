import java.util.Arrays;
import java.util.List;

public class ListIndexGetTest {
    public static boolean test() {
        Integer[] arr = new Integer[] {1};
        List<Integer> lst = Arrays.asList(1);
        int resa = arr[0];
        int resl1 = lst.get(0);
        int resl2 = lst[0];
        return resl2 == resa;
    }
}