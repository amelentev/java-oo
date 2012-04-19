import java.math.BigInteger;

public class MathTest {
    public static boolean test() {
        BigInteger a = BigInteger.ONE,
                b = BigInteger.TEN,
                c = a + b * b + b/b;
        return c.equals(BigInteger.valueOf(102));
    }
}
