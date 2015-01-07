import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;
import com.ericsson.otp.erlang.example.Bindings;
import com.ericsson.otp.erlang.example.Parser;
import com.ericsson.otp.erlang.example.Pattern;

public class CoreMatch {

	static void test() throws OtpErlangException {

		Pattern pattern = Parser.makePattern("{a, A}");
		OtpErlangTuple term = Parser.makeTerm("{a, 1000}");

		Bindings bindings = new Bindings();
		System.out.println("A = " + bindings.get("A"));

		// call match
		boolean r1 = pattern.match(term, bindings);
		System.out.println("ret = " + r1 + ", A = " + bindings.get("A"));

		// call partial bind
		OtpErlangObject t1 = pattern.bindPartial(new Bindings());
		System.out.println("bind result: " + t1);

		// call bind
		OtpErlangObject t2 = pattern.bind(bindings);
		System.out.println("bind result: " + t2);
	}

	public static void main(String[] args) {
		try {
			test();
		} catch (OtpErlangException e) {
			e.printStackTrace();
			System.exit(1);
		}
		System.out.println("ok");
	}
}
