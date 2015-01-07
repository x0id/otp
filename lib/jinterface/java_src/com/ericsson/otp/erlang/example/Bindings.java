package com.ericsson.otp.erlang.example;

import java.util.HashMap;

import com.ericsson.otp.erlang.OtpErlangObject;

public class Bindings {
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
