import java.math.BigInteger;

public class Math {
    public static boolean test() {
        BigInteger a = BigInteger.ONE,
                b = BigInteger.TEN;
        // without OO:
        BigInteger r1 = a.negate().add(b.multiply(b)).subtract(b.divide(a));
        // with OO:
        BigInteger r2 = -a + b * b - b/a;
        return r1.equals(r2);
    }
    public static void main(String[] args) {
		System.out.println(Math.test());
	}
}
