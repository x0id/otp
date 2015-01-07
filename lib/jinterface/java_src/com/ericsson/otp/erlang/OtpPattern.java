package com.ericsson.otp.erlang;

public class OtpPattern<Bindings> extends OtpErlangObject {

    private static final long serialVersionUID = -5217528359942859115L;

    public OtpErlangObject patternObject;

    public OtpPattern(final OtpErlangObject term) {
        patternObject = term;
    }

    @Override
    public String toString() {
        return "<<<" + patternObject + ">>>";
    }

    @Override
    public void encode(final OtpOutputStream buf) {
        patternObject.encode(buf);
    }

    @Override
    public boolean equals(final Object o) {
        return patternObject.equals(o);
    }

    public boolean match(final OtpErlangObject term, final Bindings bindings) {
        return patternObject.match(term, bindings);
    }

    public OtpErlangObject bind(final Bindings bindings)
            throws OtpErlangException {
        return patternObject.bind(bindings);
    }
}
