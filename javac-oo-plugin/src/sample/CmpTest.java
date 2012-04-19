import java.math.BigDecimal;

public class CmpTest {
    public static boolean test() {
        BigDecimal a = BigDecimal.ZERO,
                b = BigDecimal.ONE,
                c = BigDecimal.valueOf(1);
        return (a < b && c > a && b >= c && c >= b);
    }
}