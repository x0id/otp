package com.ericsson.otp.erlang;

import java.util.HashMap;

public class OtpBindings {
    HashMap<String, OtpErlangObject> map = new HashMap<String, OtpErlangObject>();

    public boolean put(final String name, final OtpErlangObject term) {
        final OtpErlangObject t = map.get(name);
        if (t != null) {
            return t.equals(term);
        }
        map.put(name, term);
        return true;
    }

    public OtpErlangObject get(final String var) {
        return map.get(var);
    }
}
