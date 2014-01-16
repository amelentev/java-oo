public abstract class Abstract {
	public abstract Interface add(Interface a);

	interface Interface {
		Interface add(Interface a);
	}

	static class Concrete extends Abstract implements Interface {
		public Interface add(Interface a) {
			return this;
		}
	}

	public static boolean test() {
		Abstract a1 = new Concrete();
		Interface a2 = new Concrete();
		return a1 == (a1 + a2);
	}
}
