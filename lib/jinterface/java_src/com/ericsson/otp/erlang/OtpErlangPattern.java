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
 * Wraps special kind of OtpErlangObject, which may contain unbound variables.
 * It is used for matching other Erlang terms to the pattern, or for generating
 * Erlang term via bounding pattern variables to values.
 * <p>
 * Note, that {@link OtpErlangPattern#match(OtpErlangObject, Object)} and
 * {@link OtpErlangPattern#decode(OtpErlangObject, Object)} perform same
 * operation but have slightly different interface. Function decode is intended
 * for using in nested decoder components when throwing exception is more
 * appropriate.
 * <p>
 * Examples: <blockquote>
 * 
 * <pre>
 * // custom data binder class
 * class Data {
 *     int a;
 *     String b;
 * 
 *     // this method is called to fill pattern variable A
 *     public void setA(OtpErlangObject o) throws OtpErlangException {
 *         if (o instanceof OtpErlangLong)
 *             a = ((OtpErlangLong) o).intValue();
 *         else
 *             throw new OtpErlangException(&quot;not an integer: &quot; + o);
 *     }
 * 
 *     // this method is called to fill pattern variable B
 *     public void setB(OtpErlangObject o) throws OtpErlangException {
 *         if (o instanceof OtpErlangAtom)
 *             b = ((OtpErlangAtom) o).atomValue();
 *         else
 *             throw new OtpErlangException(&quot;not an atom: &quot; + o);
 *     }
 * }
 * 
 * // using custom data binder - fast data access, additional type checking can
 * // be done when filling pattern variable, custom binder class is used.
 * OtpErlangObject testOne(OtpErlangObject eterm) throws OtpErlangException {
 *     OtpErlangPattern p = new OtpErlangPattern(&quot;{data, A, B}&quot;, Data.class);
 *     Data data = new Data();
 *     if (p.match(eterm, data))
 *         return OtpErlangParser.format(&quot;[&tilde;s, &tilde;d]&quot;, data.b, data.a);
 *     else
 *         return null;
 * }
 * 
 * // using standard variable binder - slower variable value access, no variable
 * // data type checking performed during matching, no custom class needed.
 * OtpErlangObject testTwo(OtpErlangObject eterm) throws OtpErlangException {
 *     OtpErlangPattern p = new OtpErlangPattern(&quot;{boy, Age, Name}&quot;);
 *     OtpErlangBind vars = new OtpErlangBind();
 *     if (p.match(eterm, vars))
 *         return OtpErlangParser.format(&quot;#{name =&gt; &tilde;s, age =&gt; &tilde;i}&quot;,
 *                 vars.value(&quot;Name&quot;), vars.value(&quot;Age&quot;));
 *     else
 *         return null;
 * }
 * </pre>
 * 
 * </blockquote>
 * 
 */
public class OtpErlangPattern {
    private OtpErlangObject term;

    /**
     * Creates new Erlang term pattern. The pattern may be used later to match
     * another Erlang term or to generate new Erlang term binding pattern
     * variables (if any) to values. Constructor passes given parameters to
     * {@link OtpErlangParser#format(String, Object...)}, consult it for
     * detailed description of parameters.
     * 
     * @param fmt
     *            format specification
     * @param args
     *            arguments to be used in format specifiers substitution. First
     *            argument may be of type Class&lt;?&gt;, if custom binder is
     *            used.
     * @throws OtpErlangException
     */
    public OtpErlangPattern(String fmt, Object... args) throws
            OtpErlangException {
        term = OtpErlangParser.format(fmt, args);
    }

    /**
     * match eterm to pattern, populate bindings, if not null.
     * 
     * @param eterm
     *            arbitrary Erlang term
     * @param bindings
     *            custom data binder object or {@link OtpErlangBind} or null. If
     *            bindings set to null, perform legacy Erlang term comparison.
     * @return result of matching.
     */
    public boolean match(OtpErlangObject eterm, Object bindings) {
        if (term instanceof OtpErlangVarrier) try {
            ((OtpErlangVarrier) term).match(eterm, bindings);
            return true;
        } catch (OtpErlangException e) {
            return false;
        }
        return term.equals(eterm);
    }

    /**
     * decode Erlang term fitting it to pattern and filling out bindings object,
     * if not null. Throw exception if input does not match pattern.
     * 
     * @param eterm
     *            arbitrary Erlang term
     * @param bindings
     *            custom data binder object or {@link OtpErlangBind} or null. If
     *            bindings set to null, perform legacy Erlang term comparison.
     * @throws OtpErlangException
     *             if eterm not matched pattern.
     */
    public void decode(OtpErlangObject eterm, Object bindings)
            throws OtpErlangException {
        if (term instanceof OtpErlangVarrier)
            ((OtpErlangVarrier) term).match(eterm, bindings);
        else if (!term.equals(eterm))
            throw new OtpErlangException("couldn't match const pattern");
    }

    /**
     * build Erlang term object from given pattern and bindings, throws an
     * exception in case of error.
     * 
     * @param bindings
     *            custom data binder object or {@link OtpErlangBind} used as
     *            values container for variables in pattern.
     * @return new Erlang term
     * @throws OtpErlangException
     */
    public OtpErlangObject bind(Object bindings) throws OtpErlangException {
        if (term instanceof OtpErlangVarrier && bindings == null)
            throw new RuntimeException("null bindings");
        return (term instanceof OtpErlangVarrier)
                ? ((OtpErlangVarrier) term).bind(bindings)
                : (OtpErlangObject) term.clone();
    }

    @Override
    public String toString() {
        return term.toString();
    }
}
