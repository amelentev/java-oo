public class Vec {
    private double    x;
    private double    y;

    public static boolean test() {
        Vec[] test = new Vec[] { new Vec(), new Vec() };
	test[1][0] = new Double(1.0);
	test[1][1] = 2.f;
        (test[0][0] = test[1][0])[1] = 3.;
	return test[0][0] == 1.0 && 2.f == test[1][1] && test[0][1] == 3.;
    }

    public double get(int i) {
        return (i == 0 ? x : y);
    }

    public Vec set(int i, double v) {
	switch (i) {
            case 0:
                x = v;
                break;
            case 1:
                y = v;
                break;
        }
        return this;
    }
}
