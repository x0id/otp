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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Provides a Java representation of Erlang maps. Maps may be empty. 
 *
 * <p>
 * The arity of the map is the number of elements it contains. The keys and
 * values can be retrieved as arrays and the value for a key can be queried.
 *
 */
public class OtpErlangMap extends OtpErlangObject implements OtpErlangVarrier {
    // don't change this!
    private static final long serialVersionUID = -6410770117696198497L;

    private static final OtpErlangObject[] NO_ELEMENTS = new OtpErlangObject[0];

    private HashMap<OtpErlangObject, OtpErlangObject> map;

    /**
     * Create an empty map.
     */
    public OtpErlangMap() {
        map = new HashMap<OtpErlangObject, OtpErlangObject>();
    }

    /**
     * Create a map from an array of keys and an array of values.
     *
     * @param keys
     *            the array of terms to create the map keys from.
     * @param values
     *            the array of terms to create the map values from.
     *
     * @exception java.lang.IllegalArgumentException
     *                if any array is empty (null) or contains null elements.
     */
    public OtpErlangMap(final OtpErlangObject[] keys,
            final OtpErlangObject[] values) {
        this(keys, 0, keys.length, values, 0, values.length);
    }

    /**
     * Create a map from an array of terms.
     *
     * @param keys
     *            the array of terms to create the map from.
     * @param kstart
     *            the offset of the first key to insert.
     * @param kcount
     *            the number of keys to insert.
     * @param values
     *            the array of values to create the map from.
     * @param vstart
     *            the offset of the first value to insert.
     * @param vcount
     *            the number of values to insert.
     *
     * @exception java.lang.IllegalArgumentException
     *                if any array is empty (null) or contains null elements.
     * @exception java.lang.IllegalArgumentException
     *                if kcount and vcount differ.
     */
    public OtpErlangMap(final OtpErlangObject[] keys, final int kstart,
            final int kcount, final OtpErlangObject[] values, final int vstart,
            final int vcount) {
        if (keys == null || values == null)
            throw new java.lang.IllegalArgumentException(
                    "Map content can't be null");
        if (kcount != vcount)
            throw new java.lang.IllegalArgumentException(
                    "Map keys and values must have same arity");
        map = new HashMap<OtpErlangObject, OtpErlangObject>(vcount);
        OtpErlangObject key, val;
        for (int i = 0; i < vcount; i++) {
            if ((key = keys[kstart + i]) == null)
                throw new java.lang.IllegalArgumentException(
                        "Map key cannot be null (element" + (kstart + i)
                                + ")");
            if ((val = values[vstart + i]) == null)
                throw new java.lang.IllegalArgumentException(
                        "Map value cannot be null (element" + (vstart + i)
                                + ")");
            put(key, val);
        }
    }

    /**
     * Create a map from a stream containing a map encoded in Erlang external
     * format.
     *
     * @param buf
     *            the stream containing the encoded map.
     *
     * @exception OtpErlangDecodeException
     *                if the buffer does not contain a valid external
     *                representation of an Erlang map.
     */
    public OtpErlangMap(final OtpInputStream buf)
            throws OtpErlangDecodeException {
        final int arity = buf.read_map_head();

        if (arity > 0) {
            map = new HashMap<OtpErlangObject, OtpErlangObject>(arity);
            for (int i = 0; i < arity; i++) {
                OtpErlangObject key, val;
                key = buf.read_any();
                val = buf.read_any();
                put(key, val);
            }
        } else
            map = new HashMap<OtpErlangObject, OtpErlangObject>();
    }

    /**
     * Get the arity of the map.
     *
     * @return the number of elements contained in the map.
     */
    public int arity() {
        return map.size();
    }

    /**
     * Put value corresponding to key into the map. For detailed behavior
     * description see {@link Map#put(Object, Object)}.
     * 
     * @param key
     *            key to associate value with
     * @param value
     *            value to associate with key
     * @return previous value associated with key or null
     */
    public OtpErlangObject put(final OtpErlangObject key,
            final OtpErlangObject value) {
        return map.put(key, value);
    }

    /**
     * removes mapping for the key if present.
     * 
     * @param key
     *            key for which mapping is to be remove
     * @return value associated with key or null
     */
    public OtpErlangObject remove(final OtpErlangObject key) {
        return map.remove(key);
    }

    /**
     * Get the specified value from the map.
     *
     * @param key
     *            the key of the requested value.
     *
     * @return the requested value, of null if key is not a valid key.
     */
    public OtpErlangObject get(final OtpErlangObject key) {
        return map.get(key);
    }

    /**
     * Get all the keys from the map as an array.
     *
     * @return an array containing all of the map's keys.
     */
    public OtpErlangObject[] keys() {
        return map.keySet().toArray(new OtpErlangObject[arity()]);
    }

    /**
     * Get all the values from the map as an array.
     *
     * @return an array containing all of the map's values.
     */
    public OtpErlangObject[] values() {
        return map.values().toArray(new OtpErlangObject[arity()]);
    }

    /**
     * make Set view of the map key-value pairs
     * 
     * @return a set containing key-value pairs
     */
    public Set<Entry<OtpErlangObject, OtpErlangObject>> entrySet() {
        return map.entrySet();
    }

    /**
     * Get the string representation of the map.
     *
     * @return the string representation of the map.
     */
    @Override
    public String toString() {
        final StringBuffer s = new StringBuffer();

        s.append("#{");

        boolean first = true;
        for (Map.Entry<OtpErlangObject, OtpErlangObject> pair : entrySet()) {
            if (first)
                first = false;
            else
                s.append(",");
            s.append(pair.getKey().toString());
            s.append(" => ");
            s.append(pair.getValue().toString());
        }

        s.append("}");

        return s.toString();
    }

    /**
     * Convert this map to the equivalent Erlang external representation.
     *
     * @param buf
     *            an output stream to which the encoded map should be written.
     */
    @Override
    public void encode(final OtpOutputStream buf) {
        final int arity = arity();

        buf.write_map_head(arity);

        for (Map.Entry<OtpErlangObject, OtpErlangObject> pair : entrySet()) {
            buf.write_any(pair.getKey());
            buf.write_any(pair.getValue());
        }
    }

    /**
     * Determine if two maps are equal. Maps are equal if they have the same
     * arity and all of the elements are equal.
     *
     * @param o
     *            the map to compare to.
     *
     * @return true if the maps have the same arity and all the elements are
     *         equal.
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof OtpErlangObject)
            try {
                match((OtpErlangObject) o, null);
                return true;
            } catch (OtpErlangException e) {
                return false;
            }
        else
            return false;
    }

    /**
     * Matches given object against this object optionally containing
     * variable placeholders, filling out bindings, if not null.
     *
     * @param o        the object to match
     * @param bindings variable bindings or null
     * @throws com.ericsson.otp.erlang.OtpErlangException if not matched
     */
    public void match(OtpErlangObject o, Object bindings)
            throws OtpErlangException {
        if (!(o instanceof OtpErlangMap))
            throw new OtpErlangException("not a map");

        final OtpErlangMap m = (OtpErlangMap) o;
        final int a = arity();

        if (a != m.arity() && bindings == null)
            throw new OtpErlangException("map arity mismatch");

        if (a > 0) {
            OtpErlangObject key, val;
            for (Map.Entry<OtpErlangObject, OtpErlangObject> e : entrySet()) {
                key = e.getKey();
                val = e.getValue();
                if (val instanceof OtpErlangVarrier) {
                    if (m.get(key) == null)
                        throw new OtpErlangException("map value mismatch");
                    ((OtpErlangVarrier) val).match(m.get(key), bindings);
                } else {
                    OtpErlangObject v = m.get(key);
                    if (v == null || !v.equals(val))
                        throw new OtpErlangException("map value mismatch");
                }
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
            throw new OtpErlangException("null bindings");
        OtpErlangMap ret = (OtpErlangMap) this.clone();
        for (Map.Entry<OtpErlangObject, OtpErlangObject> e : ret.entrySet()) {
            OtpErlangObject val = e.getValue();
            if (val instanceof OtpErlangVarrier)
                ret.put(e.getKey(), ((OtpErlangVarrier) val).bind(bindings));
        }
        return ret;
    }

    @Override
    protected int doHashCode() {
        final OtpErlangObject.Hash hash = new OtpErlangObject.Hash(9);
        hash.combine(map.hashCode());
        return hash.valueOf();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object clone() {
        final OtpErlangMap newMap = (OtpErlangMap) super.clone();
        newMap.map = (HashMap<OtpErlangObject, OtpErlangObject>) map.clone();
        return newMap;
    }
}
