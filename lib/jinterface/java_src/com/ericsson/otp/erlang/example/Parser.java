package com.ericsson.otp.erlang.example;

import com.ericsson.otp.erlang.OtpErlangAtom;
import com.ericsson.otp.erlang.OtpErlangInt;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpErlangTuple;

public class Parser {

    // create tuple helper
    static OtpErlangTuple makeTuple(final OtpErlangObject... erlangObjects) {
        return new OtpErlangTuple(erlangObjects);
    }

    public static Pattern makePattern(final String string) {
        // TODO
        if (string.equals("{a, A}")) {
            return new Pattern(makeTuple(new OtpErlangAtom("a"), new Variable(
                    "A")));
        }
        return null;
    }

    public static OtpErlangTuple makeTerm(final String string) {
        // TODO
        if (string.equals("{a, 1000}")) {
            return makeTuple(new OtpErlangAtom("a"), new OtpErlangInt(1000));
        }
        return null;
    }
}
