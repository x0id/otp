/*
 * %CopyrightBegin%
 *
 * Copyright Ericsson AB 2000-2013. All Rights Reserved.
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
package com.ericsson.otp.erlang;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Implements binder object to keep values of variables in pattern
 * bound to particular Erlang term object.
 */
public class OtpErlangBind {
    private HashMap<String, OtpErlangObject> map;

    /**
     * default constructor.
     */
    public OtpErlangBind() {
        map = new HashMap<String, OtpErlangObject>();
    }

    /**
     * bound new value to given variable or check if this already bound to the
     * same value. No overrides allowed.
     * 
     * @param var
     *            variable name
     * @param val
     *            variable value
     * @return true if new variable is placed, or existing variable value equal
     *         to given value, false otherwise, what means value was not
     *         re-bound to the name.
     */
    public boolean put(String var, OtpErlangObject val) {
        OtpErlangObject old = map.get(var);
        if (old != null)
            return old.equals(val);
        map.put(var, val);
        return true;
    }

    /**
     * retrieve value of variable with given name.
     * 
     * @param var
     *            variable name
     * @return variable value or null if not bound yet
     */
    public OtpErlangObject value(String var) {
        return map.get(var);
    }

    /**
     * retrieve variable value as integer
     * 
     * @param var
     *            variable value
     * @return integer value
     * @throws OtpErlangException
     *             if variable not bound to integer ({@link OtpErlangLong})
     *             type.
     */
    public int intValue(String var) throws OtpErlangException {
        OtpErlangObject o = value(var);
        if (o instanceof OtpErlangLong)
            return ((OtpErlangLong) o).intValue();
        throw new OtpErlangException("not an integer");
    }

    /**
     * retrieve variable value as long
     * 
     * @param var
     *            variable value
     * @return long value
     * @throws OtpErlangException
     *             if variable not bound to long ({@link OtpErlangLong}) type.
     */
    public long longValue(String var) throws OtpErlangException {
        OtpErlangObject o = value(var);
        if (o instanceof OtpErlangLong)
            return ((OtpErlangLong) o).longValue();
        throw new OtpErlangException("not an integer");
    }

    /**
     * retrieve variable value as list
     * 
     * @param var
     *            variable value
     * @return list value ({@link OtpErlangList})
     * @throws OtpErlangException
     *             if variable not bound to list ({@link OtpErlangList}) type.
     */
    public OtpErlangList listValue(String var) throws OtpErlangException {
        OtpErlangObject o = value(var);
        if (o instanceof OtpErlangList)
            return (OtpErlangList) o;
        throw new OtpErlangException("not a list");
    }

    /**
     * reset bindings, clear all variables.
     */
    public void clear() {
        map.clear();
    }

    /**
     * pretty prints bindings collection
     * 
     * @param out
     *            output print stream
     */
    public void print(PrintStream out) {
        for (Map.Entry<String, OtpErlangObject> e : map.entrySet()) {
            out.println(e.getKey() + " -> " + e.getValue());
        }
    }
}
