import java.util.HashMap;
import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.OtpOutputStream;

public class CoreMatch {

	/*
	 * example of bindings
	 */
	public static class Vars {

		HashMap<String, OtpErlangObject> map = new HashMap<>();

		public void put(String name, OtpErlangObject term)
				throws OtpErlangException {
			if (map.containsKey(name))
				throw new OtpErlangException("variable override " + name);
			map.put(name, term);
		}

		public OtpErlangObject get(String var) {
			return map.get(var);
		}

	}

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

		// FIXME: change encode signature to throw exception?
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
		public void match(OtpErlangObject term, Object... bindings)
				throws OtpErlangException {
			if (bindings.length > 0) {
				Object o = bindings[0];
				if (o instanceof Vars) {
					((Vars) o).put(name, term);
				}
			}
		}

		/*
		 * example of bind
		 */
		@Override
		public OtpErlangObject bind(Object... bindings)
				throws OtpErlangException {
			if (bindings.length > 0) {
				Object o = bindings[0];
				if (o instanceof Vars) {
					return ((Vars) o).get(name);
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
		OtpErlangTuple p1 = makeTuple(new OtpErlangAtom("a"), new Var("A"));

		OtpErlangTuple t1 = makeTuple(new OtpErlangAtom("a"), new OtpErlangInt(
				1000));

		// call match with no bindings
		p1.match(t1);
		Vars vars = new Vars();
		System.out.println("A = " + vars.get("A"));

		// call match with bindings
		p1.match(t1, vars);
		System.out.println("A = " + vars.get("A"));

		// call bind with no bindings
		OtpErlangObject t2 = p1.bind();
		System.out.println("fake bind result: " + t2);

		// call bind with bindings
		OtpErlangObject t3 = p1.bind(vars);
		System.out.println("real bind result: " + t3);
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
