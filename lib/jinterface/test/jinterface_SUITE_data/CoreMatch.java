import com.ericsson.otp.erlang.OtpBindings;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpOutputStream;
import com.ericsson.otp.erlang.OtpPattern;

public class CoreMatch {

	/*
	 * example of variable
	 */
	public static class Var extends OtpErlangObject {

		private static final long serialVersionUID = -2471384139735685505L;
		private String name;

		public Var(String name) {
			super();
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		@Override
		public void encode(OtpOutputStream buf) {
		}

		@Override
		public boolean equals(Object o) {
			return false;
		}

		/*
		 * example of match
		 */
		@Override
		protected boolean match(OtpErlangObject term, Object... bindings) {
			if (bindings.length > 0) {
				Object o = bindings[0];
				if (o instanceof OtpBindings) {
					return ((OtpBindings) o).put(name, term);
				}
			}
			return true;
		}

		/*
		 * example of bind
		 */
		@Override
		protected OtpErlangObject bind(Object... bindings)
				throws OtpErlangException {
			if (bindings.length > 0) {
				Object o = bindings[0];
				if (o instanceof OtpBindings) {
					return ((OtpBindings) o).get(name);
				}
			}
			return this;
		}

	}

	// create tuple helper
	static OtpErlangTuple makeTuple(OtpErlangObject... erlangObjects) {
		return new OtpErlangTuple(erlangObjects);
	}

	static void test1() throws OtpErlangException {
		OtpPattern pattern = new OtpPattern(makeTuple(new OtpErlangAtom("a"),
				new Var("A")));

		OtpErlangTuple term = makeTuple(new OtpErlangAtom("a"),
				new OtpErlangInt(1000));

		OtpBindings bindings = new OtpBindings();
		System.out.println("A = " + bindings.get("A"));

		// call instance method
		boolean r1 = pattern.match(term, bindings);
		System.out.println("ret = " + r1 + ", A = " + bindings.get("A"));

		// call class method - "pattern match term"
		boolean r2 = OtpPattern.matchTerm(pattern, term, bindings);
		System.out.println("ret = " + r2 + ", A = " + bindings.get("A"));

		// call class method - "term match pattern"
		boolean r3 = OtpPattern.matchPattern(term, pattern, bindings);
		System.out.println("ret = " + r3 + ", A = " + bindings.get("A"));

		// call instance method - bind
		OtpErlangObject t1 = pattern.bind(bindings);
		System.out.println("bind result: " + t1);

		// call class method - bind
		OtpErlangObject t2 = OtpPattern.bindPattern(pattern, bindings);
		System.out.println("bind result: " + t2);
	}

	public static void main(String[] args) {
		try {
			test1();
		} catch (OtpErlangException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("ok");
	}

}
