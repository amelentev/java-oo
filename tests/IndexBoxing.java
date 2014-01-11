public class IndexBoxing {
	int get(int a) { return a; }
	public static boolean test() {
		IndexBoxing a = new IndexBoxing();
		Integer i = new Integer(1);
		a.get(i);
		return a[i] == 1;
	}
}
