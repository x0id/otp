/*
 * %CopyrightBegin%
 *
 * Copyright Ericsson AB 2004-2010. All Rights Reserved.
 *
 * The contents of this file are subject to the Erlang Public License,
 * Version 1.1, (the "License"); you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * Erlang Public License along with this software. If not, it can be
 * retrieved online at http://www.erlang.org/.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See
 * the License for the specific language governing rights and limitations
 * under the License.
 *
 * %CopyrightEnd%
 */

import com.ericsson.otp.erlang.*;

/**
 * Implements test case jinterface_SUITE:format_match_bind/1
 */
public class TestParser {

    public static OtpErlangObject eterm(String fmt, Object... args) throws
            OtpErlangException {
        return OtpErlangParser.format(fmt, args);
    }

    public static OtpErlangPattern pattern(String fmt, Object... args) throws
            OtpErlangException {
         return new OtpErlangPattern(fmt, args);
    }

    public static void test(String fmt, Object... args) throws
            OtpErlangException {
        OtpErlangObject erlangObject = eterm(fmt, args);
        System.out.print("(\"" + fmt + '"');
        for (Object arg : args) System.out.print(", " + arg);
        System.out.println(") -> " + erlangObject.getClass() + ", " +
                "value: " + erlangObject);
    }

    private static void tm(OtpErlangPattern p, OtpErlangObject in, Object out,
            boolean match) throws OtpErlangException {
        final boolean ret = p.match(in, out);
        final String msg = out == null ? (ret ? "matched pattern " + p
                : "didn't match pattern " + p) : (ret ? "matched pattern " + p
                + ": " + out : in + " didn't match pattern " + p + " for "
                + out.getClass());
        if (match == ret)
            System.out.println(msg);
        else
            fail(msg);
    }

    public static void match(OtpErlangPattern pattern, OtpErlangObject term,
            boolean match) throws OtpErlangException {
        OtpErlangBind bindings = new OtpErlangBind();
        tm(pattern, term, bindings, match);
        System.out.println("bindings:");
        bindings.print(System.out);
    }

    public static void testMatch() throws OtpErlangException {
        match(pattern("123"), eterm("123"), true);
        match(pattern("A"), eterm("123"), true);
        match(pattern("[H|T]"), eterm("[1, 2, 3]"), true);
        match(pattern("[H|T]"), eterm("[1,2,3]"), true);
        match(pattern("[H|T]"), eterm("[1|[2|[3]]]"), true);
        match(pattern("[A,B|T]"), eterm("[1|[2|[3]]]"), true);
        match(pattern("[A,B,C|T]"), eterm("[1|[2|[3]]]"), true);
        match(pattern("[H|T]"), eterm("[1]"), true);
        match(pattern("[A,A|T]"), eterm("[1,1]"), true);
        match(pattern("[A,A|T]"), eterm("[1,2]"), false);
        // unusual cases
        match(pattern("A"), eterm("B"), true);
        match(pattern("[A,A|T]"), eterm("[B,B]"), true);
        match(pattern("[A,A|T]"), eterm("[B,C]"), false);
    }

    public static void main(String[] args) {
        try {
            testInt();
            testFloat();
            testString();
            testBoolean();
            testCharacter();
            testTuple();
            testMap();
            testMisc();
            testMatch();
            testMapper();
        } catch (OtpErlangException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void testInt() throws OtpErlangException {
        test("~i", 0);
        test("~i", 0L);
        test("~i", 1);
        test(" ~i", 1);
        test("~i ", 1);
        test(" ~i ", 1);
        test("~i", -1);
        test("~i", Integer.MIN_VALUE);
        test("~i", Integer.MAX_VALUE);
        test("~i", Long.MIN_VALUE);
        test("~i", Long.MAX_VALUE);
    }

    private static void testFloat() throws OtpErlangException {
        test("~f", 0);
        test("~f", Float.NaN);
        test("~f", Float.MAX_VALUE);
        test("~f", Float.MAX_VALUE);
        test("~f", Double.NaN);
        test("~f", Double.MIN_VALUE);
        test("~f", Double.MAX_VALUE);
    }

    private static void testString() throws OtpErlangException {
        test("~s", "");
        test("~s", "abc");
    }

    private static void testBoolean() throws OtpErlangException {
        test("~b", true);
        test("~b", false);
    }

    private static void testCharacter() throws OtpErlangException {
        test("~c", 'c');
        test("~c", 'A');
    }

    private static void testTuple() throws OtpErlangException {
        test("{~i, ~f}", 1, 2);
        test("{~i, ~f, -.123}", 1, 2);
        test("{~i, ~f, -.123E-5}", 1, 2);
        test("{~i, ~f, -.123E-5, 22}", 1, 2);
    }

    private static void testMap() throws OtpErlangException {
        test("#{~i=>~f}", 1, 2);
        test("#{~i:=~f}", 1, 2);
        test("#{~i => ~f}", 1, 2);
        test("#{~i := ~f}", 1, 2);
        test("#{~i => ~f, {a, b} := 3}", 1, 2);
        test("#{#{a:=b}=>lalala,~i => ~f, {a, b} := 3}", 1, 2);
    }

    private static void testMisc() throws OtpErlangException {
        test("\"abc\"");
        test("\"ab\\c\"");
        test("\"ab\\\\c\"");
        test("\"ab\\\"c\"");
        test("'ab\\\'c'");
        test("{\"string\", 'atom', also@atom, 123.0}");
        test("[]");
        test("[a]");
        test("[a, b]");
        test("[a, [b], {1.2, []}]");
        test("[a|[]]");

        test("a");
        test("A");
        test("Abc");
        test("_");
        test("_lala");
        test("a@b");
        test("1");
        test("1.0");

        test("[A|_]");
        test("[A|[B|C]]");
        test("[A,B|C]");
        test("[a, [b], {1.2, V}]");
    }

    /*
     * used in testMapper()
     */
    public static class RecA {
        int a;
        String b;
        public void setA(OtpErlangObject o) throws OtpErlangException {
            System.out.println("set A to " + o);
            if (o instanceof OtpErlangLong) {
                a = ((OtpErlangLong) o).intValue();
            } else {
                System.out.println("bad A type: " + o.getClass());
                throw new OtpErlangException("not long/int");
            }
        }
        public void setB(OtpErlangObject o) throws OtpErlangException {
            if (o instanceof OtpErlangAtom)
            	b = ((OtpErlangAtom)o).atomValue();
            else
            	throw new OtpErlangException("not an atom: " + o);
        }
        public void setC(OtpErlangObject o) {
            System.out.println("set C to " + o);
        }
        public final String toString() {
        	return "a = " + a + ", b = " + b;
        }
    }

    /*
     * used in testMapper()
     */
    public static class RecB {
        public OtpErlangObject getA() {
            return new OtpErlangAtom("a");
        }
        public OtpErlangObject getB() {
            return new OtpErlangAtom("b");
        }
        public OtpErlangObject getC() {
            return new OtpErlangAtom("c");
        }
    }

    private static void testMapper() throws OtpErlangException {

		// working without OtpErlangPattern class - just for example
		OtpErlangObject p = OtpErlangParser.format("{a, A, B, C}", RecA.class);
		OtpErlangObject o = OtpErlangParser.format("{a, 1, lalala, 3}");
		RecA a = new RecA();
		((OtpErlangVarrier) p).match(o, a);
		System.out.println("a: " + a);

    	OtpErlangPattern p1 = pattern("{recA, A, B, C}", RecA.class);

        tm(p1, eterm("{recA, 100, 'some atom', 3}"), new RecA(), true);

        OtpErlangPattern p2 = pattern("#{a := A, b := B, c := C}", RecA.class);

        tm(p2, eterm("#{b => '100', a => 2, c => 3}"), new RecA(), true);
        tm(p2, eterm("#{b => 100, a => 2, c => 3}"), new RecA(), false);

        // #{a := A, b := B, c := C} = #{a => 2, b => lalala, c => 3}.
        tm(p2, eterm("#{a => 2, b => lalala, c => 3}"), new RecA(), true);

        // #{a := A, b := B, c := C} = #{a => 2, b => lalala, c => 3, d => []}.
        tm(p2, eterm("#{a => 2, b => lalala, c => 3, d => []}"), new RecA(), true);

        // #{a := A, b := B, c := C} = #{a => 2, b => lalala}.
        tm(p2, eterm("#{a => 2, b => lalala}"), new RecA(), false);

        OtpErlangPattern p3 = pattern("#{a := A, b := B, c := B}", RecA.class);

        // #{a := A, b := B, c := B} = #{a => 2, b => lalala, c => 3}.
        tm(p3, eterm("#{a => 2, b => lalala, c => 3}"), new RecA(), false);

        // #{a := A, b := B, c := B} = #{a => 2, b => lalala, c => lalala}.
        tm(p3, eterm("#{a => 2, b => lalala, c => lalala}"), new RecA(), true);

        // complex nested match tests
        OtpErlangPattern p4 = pattern("{_, A, 123, #{x := ping, y := [_,A|_], z := B}, C}", RecA.class);
        tm(p4, eterm("{lalala, 0, 123, #{z => a, x := ping, y => [{1,2,3}, 0]}, []}"), new RecA(), true);
        tm(p4, eterm("{lalala, 0, 123, #{z => a, x := ping, y => [{1,2,3}, 0, []]}, []}"), new RecA(), true);
        tm(p4, eterm("{lalala, 0, 125, #{z => a, x := ping, y => [{1,2,3}, 0]}, []}"), new RecA(), false);
        tm(p4, eterm("{lalala, 0, 123, #{z => 3, x := ping, y => [{1,2,3}, 0]}, []}"), new RecA(), false);

        OtpErlangPattern p5 = pattern("{recB, A, B, C}", RecB.class);

        // generate term from pattern and binding object
        OtpErlangObject out = p5.bind(new RecB());
        System.out.println(out);
    }

	private static void fail(String why) throws OtpErlangException {
    	throw new OtpErlangException(why);
        // System.err.println(why);
        // System.exit(1);
    }
}
