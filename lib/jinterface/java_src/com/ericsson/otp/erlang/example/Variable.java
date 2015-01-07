package com.ericsson.otp.erlang.example;

import com.ericsson.otp.erlang.OtpErlangException;
import com.ericsson.otp.erlang.OtpErlangObject;
import com.ericsson.otp.erlang.OtpOutputStream;

public class Variable extends OtpErlangObject {

    private static final long serialVersionUID = 3207192237384962886L;
    private final String name;

    public Variable(final String name) {
        super();
        this.name = name;
    }

    @Override
    public String toString() {
        return "<" + name + ">";
    }

    @Override
    public void encode(final OtpOutputStream buf) {
    }

    @Override
    public boolean equals(final Object o) {
        return false;
    }

    @Override
    protected boolean match(final OtpErlangObject term,
            final Object... bindings) {
        if (bindings.length > 0) {
            final Object o = bindings[0];
            if (o instanceof Bindings) {
                return ((Bindings) o).put(name, term);
            }
        }
        return true;
    }

    @Override
    protected OtpErlangObject bind(final Object... bindings)
            throws OtpErlangException {
        if (bindings.length > 0) {
            final Object o = bindings[0];
            if (o instanceof Bindings) {
                return ((Bindings) o).get(name);
            }
        }
        return this;
    }
}
