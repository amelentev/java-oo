import java.math.BigInteger;

public class CompAssBug {
	public static boolean test1() {
		boolean res = true;
		res |= false;
		return res;
	}
	public static boolean test2() {
		BigInteger x = BigInteger.ONE;
		x = x + BigInteger.TEN;
		return x.equals(BigInteger.valueOf(11));
	}
	public static boolean test() { return test1() && test2(); }
	public static void main(String[] args) throws Exception {
		System.out.println(test());
	}
}