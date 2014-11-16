import java.math.BigInteger;

public class BinaryRev {
	static class MyInt {
		public int v;
		public MyInt(int v) { this.v = v; }
		public MyInt multiply(int a) { return new MyInt(v*a); }
		public MyInt multiplyRev(int a) { return multiply(a); }
		public boolean equals(Object o) {
			return (o instanceof MyInt) && ((MyInt)o).v == v;
		}
	}

	public static boolean test() {
		MyInt a = new MyInt(1);
		return (a*2).equals(2*a);
	}
}
