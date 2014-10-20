import java.lang.Integer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListIndexSet {
    public static boolean test() {
        List<Integer> lst = new ArrayList<>(Arrays.asList(1));
        // without OO:
        lst.set(0, 1);
        // with OO:
        lst[0] = 2;
        return lst.get(0)==2;
    }
    public static void main(String[] args) {
		System.out.println(ListIndexSet.test());
	}
}
