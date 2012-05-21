import java.util.Arrays;

public class Vector {
	int[] d;
	public Vector(int... d) {
		this.d = d;
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
	public String toString() {
		return Arrays.toString(d);
	}
	
	public static boolean test() {
		Vector a = new Vector(1,2),
				b = new Vector(2,3),
				c = a + b*2;
		return "[5, 8]".equals(c.toString());
	}
	public static void main(String[] args) {
		System.out.println(test());
	}
}
