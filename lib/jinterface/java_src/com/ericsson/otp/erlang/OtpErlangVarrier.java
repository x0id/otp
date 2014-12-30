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

/**
 * Interface describing objects capable to perform match and bind operations.
 */

public interface OtpErlangVarrier {
    /**
     * Matches given object against this object optionally containing variable
     * placeholders, filling out bindings, if not null.
     * 
     * @param o
     *            the object to match
     * @param bindings
     *            variable bindings or null. Object of type
     *            {@link OtpErlangBind} may be provided and used later to get
     *            bound values by variable names. Also, if instance of the
     *            object was created with
     *            {@link OtpErlangParser#format(String, Object...)} with class
     *            implementing variable setters passed as first argument
     *            following format specification, this parameter may receive
     *            object of such class. Each setter will be called once next
     *            variable is being matched. Setter receives OtpErlangObject as
     *            the only argument, returns nothing, throwing
     *            {@link OtpErlangException} in case of mismatch. For example,
     *            setter for variable "Age" would have signature:
     *            <pre>public void setAge(OtpErlangObject o) throws OtpErlangException</pre>
     * @throws com.ericsson.otp.erlang.OtpErlangException
     *             if not matched
     */
    public void match(final OtpErlangObject o, Object bindings)
            throws OtpErlangException;

    /**
     * Makes new Erlang term replacing Var placeholder(s) with real value(s)
     * from bindings
     *
     * @param bindings variable bindings
     * @return new eterm object
     */
    public OtpErlangObject bind(Object bindings) throws OtpErlangException;
}
