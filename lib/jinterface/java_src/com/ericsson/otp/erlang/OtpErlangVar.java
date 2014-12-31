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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Variable placeholder object used when ErlangOtpParser creates object from
 * specification containing variables (names starting with capital letter or _).
 */
public class OtpErlangVar extends OtpErlangObject implements OtpErlangMatcher {

    private static final long serialVersionUID = 7681811977231464284L;

    private final String name;
    private final boolean tail;
    private final boolean skip;

    private Method getter = null;
    private Method setter = null;

    public OtpErlangVar(String name, boolean tail, Class<?> mapper)
            throws OtpErlangException {
        this.name = name;
        this.tail = tail;
        this.skip = name.equals("_");
        if (mapper != null && !skip) {
            String getterName = "get" + name;
            String setterName = "set" + name;
            Class<?> varType = OtpErlangObject.class;
            try {
                getter = mapper.getMethod(getterName);
            } catch (NoSuchMethodException ignore) {
            }
            try {
                setter = mapper.getMethod(setterName, varType);
            } catch (NoSuchMethodException ignore) {
            }
        }
    }

    /**
     * retrieve variable name.
     * 
     * @return variable name as a string.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the printable representation of the object. This is usually
     * similar to the representation used by Erlang for the same type of
     * object.
     */
    @Override
    public String toString() {
        return '<' + (tail ? "Tail" : "Var") + ':' + name + '>';
    }

    /**
     * Determine if two Erlang objects are equal. In general, Erlang objects are
     * equal if the components they consist of are equal.
     *
     * @param o the object to compare to.
     * @return true if the objects are identical.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof OtpErlangVar) {
            System.out.println("comp var " + name + " to var " + ((OtpErlangVar) o).name);
            return 0 == name.compareTo(((OtpErlangVar) o).name)
                    && tail == ((OtpErlangVar) o).tail;
        }
        return false;
    }

    /**
     * Matches given object against this object optionally containing variable
     * placeholders, filling out bindings, if not null.
     * 
     * @param o
     *            the object to match
     * @param bindings
     *            object to store values of matched variables
     * @throws OtpErlangException
     */
    public void match(OtpErlangObject o, Object bindings)
            throws OtpErlangException {
        if (skip || bindings == null)
            return;
        if (bindings instanceof OtpErlangBinding) {
            if (((OtpErlangBinding) bindings).put(name, o))
                return;
            throw new OtpErlangException(name + "-variable didn't match " +
                    "previously bound value");
        } else {
            if (setter == null)
                throw new RuntimeException(name + "-setter not set");
            try {
                setter.invoke(bindings, o);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause instanceof OtpErlangException)
                    throw (OtpErlangException) cause;
                else
                    throw new RuntimeException(name +
                            "-setter thrown unexpected exception", cause);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        name + "-setter access violation", e);
            }
        }
    }

    /**
     * Makes new Erlang term replacing Var placeholder(s) with real value(s)
     * from bindings
     *
     * @param bindings variable bindings
     * @return new eterm object
     */
    public OtpErlangObject bind(Object bindings) throws OtpErlangException {
        if (bindings == null)
            throw new RuntimeException(name + "-variable: null bindings");
        if (bindings instanceof OtpErlangBinding) {
            OtpErlangObject o = ((OtpErlangBinding) bindings).value(name);
            if (o == null)
                throw new OtpErlangException(name + "-variable not set");
            return o;
        } else {
            if (getter == null)
                throw new RuntimeException(name + "-getter not set");
            try {
                OtpErlangObject o = (OtpErlangObject) getter.invoke(bindings);
                if (o == null)
                    throw new RuntimeException(name + "-getter returned null");
                return o;
            } catch (InvocationTargetException e) {
                Throwable cause = e.getTargetException();
                if (cause instanceof OtpErlangException)
                    throw (OtpErlangException) cause;
                else
                    throw new RuntimeException(name +
                            "-getter thrown unexpected exception", cause);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(
                        name + "-getter access violation", e);
            }
        }
    }

    /**
     * Convert the object according to the rules of the Erlang external format.
     * This is mainly used for sending Erlang terms in messages, however it can
     * also be used for storing terms to disk.
     *
     * @param buf an output stream to which the encoded term should be
     *            written.
     */
    @Override
    public void encode(OtpOutputStream buf) {
        // ignore
    }

    /**
     * Answer the question if this is a tail variable. Tail variable is a
     * placeholder for list tail, for example in pattern object
     * {@code {A, 123, [A,B|T], lalala}} "T" is a tail variable.
     * 
     * @return true if this is a tail variable.
     */
    public boolean isTailVariable() {
        return tail;
    }
}
