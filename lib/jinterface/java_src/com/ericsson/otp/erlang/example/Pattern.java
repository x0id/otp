package com.ericsson.otp.erlang.example;

import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpPattern;

public class Pattern {
    OtpPattern<Bindings, Counter> pattern, bound;

    public Pattern(final OtpErlangObject o) {
        // immutable pattern
        pattern = new OtpPattern<Bindings, Counter>(o);
        // pattern accumulating bound variables when calling bindPartial
        bound = pattern;
    }

    public boolean match(final OtpErlangObject term, final Bindings bindings) {
        return pattern.match(term, bindings);
    }

    public OtpErlangObject bind(final Bindings bindings)
            throws OtpErlangException {
        return pattern.bind(bindings);
    }

    public OtpErlangObject bindPartial(final Bindings bindings)
            throws OtpErlangException {
        final Counter counter = new Counter();
        final OtpErlangObject ret = bound.bindPartial(bindings, counter);
        if (counter.get() > 0) {
            bound = new OtpPattern<Bindings, Counter>(ret);
            return null;
        } else {
            // reset bound pattern to initial value
            bound = pattern;
            // return resulting object
            return ret;
        }
    }
}
