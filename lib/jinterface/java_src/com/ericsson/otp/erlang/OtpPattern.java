package com.ericsson.otp.erlang;

public class OtpPattern extends OtpErlangObject {

    private static final long serialVersionUID = -5217528359942859115L;

    public OtpErlangObject body;

    public OtpPattern(final OtpErlangObject term) {
        body = term;
    }

    @Override
    public String toString() {
        return "<<<" + body + ">>>";
    }

    @Override
    public void encode(final OtpOutputStream buf) {
        body.encode(buf);
    }

    @Override
    public boolean equals(final Object o) {
        return body.equals(o);
    }

    public boolean match(final OtpErlangObject term, final OtpBindings bindings) {
        return body.match(term, bindings);
    }

    public static boolean matchTerm(final OtpPattern pattern,
            final OtpErlangObject term, final OtpBindings bindings) {
        return pattern.match(term, bindings);
    }

    public static boolean matchPattern(final OtpErlangObject term,
            final OtpPattern pattern, final OtpBindings bindings) {
        return pattern.match(term, bindings);
    }

    public OtpErlangObject bind(final OtpBindings bindings)
            throws OtpErlangException {
        return body.bind(bindings);
    }

    public static OtpErlangObject bindPattern(final OtpPattern pattern,
            final OtpBindings bindings) throws OtpErlangException {
        return pattern.bind(bindings);
    }
}
