package com.ericsson.otp.erlang.example;

import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpPattern;

public class Pattern {
    OtpPattern<Bindings> pattern;

    public Pattern(final OtpErlangObject o) {
        pattern = new OtpPattern<Bindings>(o);
    }

    public boolean match(final OtpErlangObject term, final Bindings bindings) {
        return pattern.match(term, bindings);
    }

    public OtpErlangObject bind(final Bindings bindings)
            throws OtpErlangException {
        return pattern.bind(bindings);
    }
}
