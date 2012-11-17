import java.math.*;

public class ValueOf {
	public static boolean test() {
		BigInteger a = BigInteger.valueOf(123);
		BigInteger b = 123;
		return a.equals(b);
	}
	public static void main(String []args) {
		System.out.println(test());		
	}
}
