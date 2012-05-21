public class CompAssBug {
	public static boolean test() {
		boolean res = true;
		res |= false;
		return res;
	}
    public static void main(String[] args) throws Exception {
		System.out.println(test());
    }
}