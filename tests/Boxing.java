public class Boxing {
	static class Vec {
		double d[];
		Vec(double... d) { this.d = d; }
		double get(int i) { return d[i]; }
		double set(int i, double a) { return d[i] = a; }
		double add(double a) { return a; }
	}
	static class WVec {
		Double d[];
		WVec(Double... a) { d = a; }
		Double get(Integer i) { return d[i]; }
		Double set(Integer i, Double a) { return d[i] = a; }
		Double add(Double a) { return a; }
	}
	public static boolean test() {
		Vec v = new Vec(1.,2.,3.);
		Double d = (v[new Integer(2)] = new Double(4.));
		return d==4. && new Double(4.).equals(v[2]) 
			&& (new Double(1.).equals(v + new Double(1.)))
			&& wtest();
	}
	public static boolean wtest() {
		WVec v = new WVec(1.,2.,3.);
		double d = (v[2] = 4.0);
		return d == 4. && 4 == v[2] && (v+1.)==1.;
	}
}
