import java.util.Arrays;

public class Vector {
	int[] d;
	public Vector(int... d) {
		this.d = d;
	}
	public static Vector valueOf(int[] d) { // for implicit type conversion
		return new Vector(d);
	}
	public Vector add(Vector a) {
		assert d.length == a.d.length;
		int[] n = d.clone();
		for (int i = 0; i < a.d.length; i++)
			n[i] += a.d[i];
		return new Vector(n);
	}
	public Vector multiply(int a) {
		int[] n = d.clone();
		for (int i = 0; i < d.length; i++)
			n[i] *= a;
		return new Vector(n);
	}
	public Vector multiplyRev(int a) {
		return multiply(a);
	}
	public int get(int i) {
		return d[i];
	}
	public String toString() {
		return Arrays.toString(d);
	}
	public boolean equals(Object obj) {
		if (obj==null || obj.getClass()!=getClass()) return false;
		return Arrays.equals(d, ((Vector)obj).d);
	}

	public Vector multiply(Matrix A) {
		assert d.length == A.n();
		int res[] = new int[A.m()];
		for (int j = 0; j < A.m(); j++)
			for (int i = 0; i < A.n(); i++)
				res[j] += d[i] * A[i][j];
		return new Vector(res);
	}

	public static class Matrix {
		int d[][];
		public Matrix(int d[][]) {
			for (int[] a : d)
				assert a.length == d[0].length;
			this.d = d;
		}
		public int n() { return d.length; }
		public int m() { return d[0].length; }
		public Vector get(int i) { return new Vector(d[i]); }
		public String toString() { return Arrays.deepToString(d); }
	}

	public static boolean test() {
		Vector a = new Vector(1,2),
				b = new int[]{2,3},
				c = a + 2*b;
		if (!"[5, 8]".equals(c.toString()))
			return false;
		Matrix A = new Matrix(new int[][] {{1,2,3}, {4,5,6}});
		return (a*A).equals(new Vector(9,12,15));
	}
	public static void main(String[] args) {
		System.out.println(test());
	}
}
