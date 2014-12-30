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

import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.ArrayList;

/**
 * Provides function for creating Erlang terms from format specification and
 * arbitrary arguments. This functionality is similar to implemented in C Erlang
 * Interface function: ETERM *erl_format(FormatStr, ... ).
 */
public class OtpErlangParser {
    private String fmtStr;
    private Object argArr[];
    private int fmtPos;
    private int argPos;
    private Class<?> mapper;

    /**
     * Creates Erlang term from format specification and arguments.
     * <p>
     * Some examples:
     * 
     * <pre>
     * OtpErlangParser.format(&quot;&tilde;c&quot;, 'A');
     * OtpErlangParser.format(&quot;&tilde;i&quot;, 123);
     * OtpErlangParser.format(&quot;&tilde;i&quot;, Long.MAX_VALUE);
     * OtpErlangParser.format(&quot;&tilde;f&quot;, 0.1);
     * OtpErlangParser.format(&quot;&tilde;f&quot;, Double.NaN);
     * OtpErlangParser.format(&quot;&tilde;f&quot;, Double.MIN_VALUE);
     * OtpErlangParser.format(&quot;{\&quot;string\&quot;, 'atom', also@atom, 123.0}&quot;);
     * OtpErlangParser.format(&quot;[]&quot;);
     * OtpErlangParser.format(&quot;[a, [b], {1.2, []}]&quot;);
     * OtpErlangParser.format(&quot;[a|[]]&quot;);
     * OtpErlangParser.format(&quot;#{b =&gt; &tilde;a, a =&gt; &tilde;d, c =&gt; &tilde;b}&quot;, &quot;atom&quot;, 123, true);
     * OtpErlangParser.format(&quot;{_, A, 123, #{x := ping, y := [_,A|_], z := B}, C}&quot;);
     * OtpErlangParser.format(&quot;{_, A, &tilde;i, #{x := &tilde;a, y := [_,A|_], z := B}, C}&quot;, 123, ping);
     * // using custom variable binder (mapper)
     * OtpErlangObject p = OtpErlangParser.format(&quot;{a, A, B, C}&quot;, RecA.class);
     * // using custom variable binder (mapper) and value arguments
     * OtpErlangObject p = OtpErlangParser.format(&quot;{&tilde;a, A, B, C, &tilde;d}&quot;, RecA.class, a, 123);
     * </pre>
     * 
     * @param fmt
     *            format specification. It resembles Erlang term syntax with
     *            optional format specifiers to be replaced with corresponding
     *            argument values. Similar to printf().
     *            <p>
     *            Set of valid format specifiers is as follows:
     *            <ul>
     *            <li>~c - character
     *            <li>~i - integer
     *            <li>~d - integer
     *            <li>~f - floating point
     *            <li>~a - atom
     *            <li>~s - string
     *            <li>~w - arbitrary term
     *            <li>~b - boolean
     *            </ul>
     * @param args
     *            arguments to be used in format specifiers substitution. One
     *            important exclusion is the first argument when it is of type
     *            Class&lt;?&gt;. In such case first argument specifies binder
     *            class which must define setter methods for each variable in
     *            the format specification named after the variable name. I.e.
     *            for "{A, B}" format specifier binder class should define the
     *            following methods: <pre>
     *            void setA(OtpErlangObject o) throws OtpErlangException; 
     *            void setB(OtpErlangObject o) throws OtpErlangException;</pre>
     * @return new Erlang term object. It may include {@link OtpErlangVar}
     * objects as elements of internal structure (build of tuples, lists, maps)
     * when format specifier contains variables (literals starting with capital
     * letter or underscore character '_'). Anonymous variables (starting with
     * '_') will not be bound to value when matching object to particular Erlang
     * term, corresponding element will be skipped.  
     * @throws OtpErlangException
     */
    public static OtpErlangObject format(String fmt, Object... args)
            throws OtpErlangException {
        return new OtpErlangParser(fmt, args).make();
    }

    private OtpErlangParser(String format, Object... args) {
        fmtStr = format;
        fmtPos = 0;
        argArr = args;
        argPos = 0;
        mapper = null;
    }

    private OtpErlangObject make() throws OtpErlangException {
        if (argArr.length > 0 && argArr[0] instanceof Class<?>) {
            mapper = (Class<?>) argArr[0];
            argPos++;
        }
        OtpErlangObject ret = parse();
        if (argPos != argArr.length)
            throw new OtpErlangException((argArr.length - argPos) +
                    " extra argument(s)");
        int end = skipWS();
        if (end != -1)
            throw new OtpErlangException("trailing garbage in format string");
        return ret;
    }

    private OtpErlangObject parse() throws OtpErlangException {
        int c = skipWS();
        switch (c) {
            case '{':
                return parseTuple();
            case '[':
                return parseList();
            case '#':
                return parseMap();
            case '~':
                return makeElement();
            case '"':
                return parseString();
            case '\'':
                return parseQuotedAtom();
            case '$':
                return parseChar();
            default:
                if (Character.isLowerCase(c))
                    return parseAtom();
                if (Character.isUpperCase(c) || c == '_')
                    return parseVariable(false);
                return parseNumber();
        }
    }

    private OtpErlangObject parseChar() {
        char c = (char) nextFmt();
        return new OtpErlangChar(c);
    }

    private OtpErlangObject parseVariable(boolean tail) throws
            OtpErlangException {
        int start = fmtPos - 1;
        int c;
        do {
            c = nextFmt();
        } while (Character.isLetterOrDigit(c) || c == '_');
        if (c != -1)
            stepBack();
        return new OtpErlangVar(fmtStr.substring(start, fmtPos), tail, mapper);
    }

    private OtpErlangObject parseAtom() {
        int start = fmtPos - 1;
        int c;
        do {
            c = nextFmt();
        } while (Character.isLetterOrDigit(c) || c == '_' || c == '@');
        if (c != -1)
            stepBack();
        return new OtpErlangAtom(fmtStr.substring(start, fmtPos));
    }

    private OtpErlangObject parseQuotedAtom() {
        int start = fmtPos;
        int c, last = -1;
        do while ((c = nextFmt()) != '\'') {
            if (c < 0) return null;
            last = c;
        } while (last == '\\');
        // TODO: transform escaped sequences?
        // TODO: use iterator range
        return new OtpErlangAtom(fmtStr.substring(start, fmtPos - 1));
    }

    private OtpErlangObject parseString() {
        int start = fmtPos;
        int c, last = -1;
        do while ((c = nextFmt()) != '"') {
            if (c < 0) return null;
            last = c;
        } while (last == '\\');
        // TODO: transform escaped sequences?
        // TODO: use iterator range
        return new OtpErlangString(fmtStr.substring(start, fmtPos - 1));
    }

    private OtpErlangObject parseNumber() throws OtpErlangException {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false); // avoid parsing a list as a single number
        ParsePosition pos = new ParsePosition(fmtPos - 1);
        Number number = nf.parse(fmtStr, pos);
        if (pos.getIndex() < fmtPos || number == null)
            throw new OtpErlangException("not a number");
        fmtPos = pos.getIndex();
        return erlangNumber(number);
    }

    private OtpErlangObject erlangNumber(Number number) throws
            OtpErlangException {
        if (number instanceof Byte)
            return new OtpErlangByte(number.byteValue());
        if (number instanceof Short)
            return new OtpErlangShort(number.shortValue());
        if (number instanceof Integer)
            return new OtpErlangInt(number.intValue());
        if (number instanceof Long)
            return new OtpErlangLong(number.longValue());
        if (number instanceof Float)
            return new OtpErlangFloat(number.floatValue());
        if (number instanceof Double)
            return new OtpErlangDouble(number.doubleValue());
        throw new OtpErlangException("unsupported numeric class " +
                number.getClass().toString());
    }

    private OtpErlangObject parseTuple() throws OtpErlangException {
        ArrayList<OtpErlangObject> items = new ArrayList<OtpErlangObject>();
        OtpErlangObject item;
        while ((item = parse()) != null) {
            items.add(item);
            int c = skipWS();
            switch (c) {
                case ',':
                    continue;
                case '}':
                    return new OtpErlangTuple(items.toArray(new
                            OtpErlangObject[items.size()]));
                default:
                    throw new OtpErlangException("unknown format: " + c);
            }
        }
        throw new OtpErlangException("incomplete tuple");
    }

    private OtpErlangObject parseMap() throws OtpErlangException {
        ArrayList<OtpErlangObject> keys = new ArrayList<OtpErlangObject>();
        ArrayList<OtpErlangObject> vals = new ArrayList<OtpErlangObject>();
        OtpErlangObject key, val;
        int c = nextFmt();
        if (c != '{')
            throw new OtpErlangException("incomplete map");
        while ((key = parse()) != null) {
            keys.add(key);
            switch (c = skipWS()) {
            case '=':
                if ((c = nextFmt()) != '>')
                    throw new OtpErlangException("incomplete map");
                if ((val = parse()) == null)
                    throw new OtpErlangException("no value in a map");
                vals.add(val);
                switch (c = skipWS()) {
                case '}':
                    return new OtpErlangMap(
                            keys.toArray(new OtpErlangObject[keys.size()]),
                            vals.toArray(new OtpErlangObject[vals.size()]));
                case ',':
                    continue;
                }
                throw new OtpErlangException("incomplete map");
            case ':':
                if ((c = nextFmt()) != '=')
                    throw new OtpErlangException("incomplete map");
                if ((val = parse()) == null)
                    throw new OtpErlangException("no value in a map");
                vals.add(val);
                switch (c = skipWS()) {
                case '}':
                    return new OtpErlangMap(
                            keys.toArray(new OtpErlangObject[keys.size()]),
                            vals.toArray(new OtpErlangObject[vals.size()]));
                case ',':
                    continue;
                }
                throw new OtpErlangException("incomplete map");
            default:
                throw new OtpErlangException("incomplete map: " + (char) c);
            }
        }
        throw new OtpErlangException("incomplete map");
    }

    private OtpErlangObject parseList() throws OtpErlangException {
        if ((skipWS()) == ']') return new OtpErlangList();
        stepBack();
        return parseList1();
    }

    private OtpErlangObject parseList1() throws OtpErlangException {
        ArrayList<OtpErlangObject> items = new ArrayList<OtpErlangObject>();
        OtpErlangObject item;
        while ((item = parse()) != null) {
            items.add(item);
            int c = skipWS();
            switch (c) {
                case ',':
                    continue;
                case '|':
                    OtpErlangObject tail;
                    if ((c = skipWS()) == '[')
                        tail = parseList();
                    else if (Character.isUpperCase(c) || c == '_')
                        tail = parseVariable(true);
                    else
                        throw new OtpErlangException("invalid tail: " + c);
                    if ((c = skipWS()) != ']')
                        throw new OtpErlangException("uncompleted list: " + c);
                    OtpErlangList list = new OtpErlangList(items.toArray(
                            new OtpErlangObject[items.size()]), tail);
                    return list;
                case ']':
                    return new OtpErlangList(items.toArray(new
                            OtpErlangObject[items.size()]));
                default:
                    throw new OtpErlangException("unknown format: " + c);
            }
        }
        throw new OtpErlangException("incomplete list");
    }

    private OtpErlangObject makeElement() throws OtpErlangException {
        int chr = nextFmt();
        Object obj;
        switch (chr) {
            case 'i':
            case 'd':
                obj = nextArg();
                if (obj instanceof Integer)
                    return new OtpErlangInt((Integer) obj);
                if (obj instanceof Long)
                    return new OtpErlangLong((Long) obj);
                throw new OtpErlangException("incompatible arg: " + obj);
            case 'f':
                obj = nextArg();
                if (obj instanceof Integer)
                    return new OtpErlangFloat(((Integer) obj).floatValue());
                if (obj instanceof Long)
                    return new OtpErlangFloat(((Long) obj).floatValue());
                if (obj instanceof Float)
                    return new OtpErlangFloat((Float) obj);
                if (obj instanceof Double)
                    return new OtpErlangDouble((Double) obj);
                throw new OtpErlangException("incompatible arg: " + obj);
            case 's':
                obj = nextArg();
                if (obj instanceof String)
                    return new OtpErlangString((String) obj);
                throw new OtpErlangException("incompatible arg: " + obj);
            case 'a':
                obj = nextArg();
                if (obj instanceof String)
                    return new OtpErlangAtom((String) obj);
                throw new OtpErlangException("incompatible arg: " + obj);
            case 'b':
                obj = nextArg();
                if (obj instanceof Boolean)
                    return new OtpErlangBoolean((Boolean) obj);
                throw new OtpErlangException("incompatible arg: " + obj);
            case 'c':
                obj = nextArg();
                if (obj instanceof Character)
                    return new OtpErlangChar((Character) obj);
                throw new OtpErlangException("incompatible arg: " + obj);
            case 'w':
                obj = nextArg();
                if (obj instanceof Number)
                    return erlangNumber((Number) obj);
                if (obj instanceof Boolean)
                    return new OtpErlangBoolean((Boolean) obj);
                if (obj instanceof Character)
                    return new OtpErlangChar((Character) obj);
                if (obj instanceof String)
                    return new OtpErlangString((String) obj);
                if (obj instanceof OtpErlangObject)
                    return (OtpErlangObject) ((OtpErlangObject) obj).clone();
                throw new OtpErlangException("incompatible arg: " + obj);
            default:
                throw new OtpErlangException("unknown format: ~" + chr);
        }
    }

    private int nextFmt() {
        return fmtPos < fmtStr.length() ? fmtStr.charAt(fmtPos++) : -1;
    }

    private int skipWS() {
        while (true) {
            int c = nextFmt();
            if (c == ' ' || c == '\t' || c == '\n')
                continue;
            return c;
        }
    }

    private void stepBack() {
        --fmtPos;
    }

    private Object nextArg() throws OtpErlangException {
        if (argPos < argArr.length)
            return argArr[argPos++];
        throw new OtpErlangException("missing argument(s)");
    }
}
