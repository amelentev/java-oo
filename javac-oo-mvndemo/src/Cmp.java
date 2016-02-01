import java.math.BigDecimal;

public class Cmp {
    public static boolean test() {
        // without OO:
        BigDecimal a = BigDecimal.valueOf(0),
        // with OO:
                b = 1,
                c = 2;
        // without OO:
        boolean r1 = a.compareTo(b)<0 && c.compareTo(a)>0 && (b.add(b)).compareTo(c)>=0 && c.compareTo(b.add(b))>=0;
        // with OO:
        boolean r2 = a < b && c > a && b+b >= c && c >= b+b;
        System.out.printf("(a < b && c > a && b >= c && c >= b) = %b\n", r2);
        System.out.printf("Here a,b,c's class is %s, and a=%s, b=%s, c=%s\n", a.getClass().getSimpleName(), a, b, c);
        return r1 && r2;
    }
    public static void main(String[] args) {
        System.out.println(Cmp.test());
    }
}
