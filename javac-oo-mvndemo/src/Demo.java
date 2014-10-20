import java.math.*;
import java.util.*;
public class Demo {
	public static void main(String[] args) {
		BigInteger  a = BigInteger.valueOf(1),	// without OO
				b = 2,	// with OO

				c1 = a.negate().add(b.multiply(b)).add(b.divide(a)), // without OO
				c2 = -a + b*b + b/a; // with OO

		if (c1.compareTo(c2)<0 || c1.compareTo(c2)>0) System.out.println("impossible"); // without OO
		if (c1<c2 || c1>c2) System.out.println("impossible"); // with OO

		HashMap<String, String> map = new HashMap<>();
		if (!map.containsKey("qwe")) map.put("qwe", "asd"); // without OO
		if (map["qwe"]==null) map["qwe"] = "asd"; // with OO
	}
	public static boolean test() { Demo.main(null); return true; }
}
