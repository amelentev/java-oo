public class VecMat {
    static class Vec {
        Vec multiply(Mat A) { return this; }
    }
    static class Mat {}
    public static boolean test() {
        Vec a = new Vec();
        Mat A = new Mat();
	return a*A == a;
    }
}
