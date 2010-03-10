/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.serialization;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.collections.immutable.ImmutableMap;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a JavaScript Object Notation (JSON) file.
 */
public class JSONSerializer extends JSON implements Serializer<Object> {
    // TODO Don't extends JSON when this class is moved to org.apache.pivot.json
    // TODO Remove deprecated methods
    private Charset charset;
    private boolean immutable;

    private int c = -1;
    private boolean alwaysDelimitMapKeys = false;
    private LineNumberReader lineNumberReader = null;

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final String MIME_TYPE = "application/json";
    public static final int BUFFER_SIZE = 2048;

    public JSONSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME), false);
    }

    public JSONSerializer(Charset charset) {
        this(charset, false);
    }

    public JSONSerializer(boolean immutable) {
        this(Charset.forName(DEFAULT_CHARSET_NAME), immutable);
    }

    public JSONSerializer(Charset charset, boolean immutable) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
        this.immutable = immutable;
    }

    /**
     * Returns the character set used to encode/decode the JSON data.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns the serializer's immutable flag.
     *
     * @return
     * If <tt>true</tt>, all list and map values will be wrapped in an immutable equivalent.
     */
    public boolean isImmutable() {
        return immutable;
    }

    /**
     * Reads data from a JSON stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @see #readObject(Reader)
     */
    @Override
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
        Object object = readObject(reader);

        return object;
    }

    /**
     * Reads data from a JSON stream.
     *
     * @param reader
     * The reader from which data will be read.
     *
     * @return
     * One of the following types, depending on the content of the stream:
     *
     * <ul>
     * <li>java.lang.String</li>
     * <li>java.lang.Number</li>
     * <li>java.lang.Boolean</li>
     * <li>pivot.collections.List</li>
     * <li>pivot.collections.Map</li>
     * </ul>
     */
    public Object readObject(Reader reader)
        throws IOException, SerializationException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null.");
        }

        // Move to the first character
        c = reader.read();

        // Read the root value
        lineNumberReader = new LineNumberReader(reader);
        Object object;

        try {
            object = readValue(lineNumberReader);
        } catch (IOException exception) {
            logException(exception);
            throw exception;
        } catch (SerializationException exception) {
            logException(exception);
            throw exception;
        } catch (RuntimeException exception) {
            logException(exception);
            throw exception;
        }

        lineNumberReader = null;

        return object;
    }

    private void logException(Exception exception) {
        System.err.println("An error occurred while processing input at line number "
            + (lineNumberReader.getLineNumber() + 1));
    }

    private Object readValue(Reader reader)
        throws IOException, SerializationException {
        Object object = null;

        skipWhitespaceAndComments(reader);

        if (c == -1) {
            throw new SerializationException("Unexpected end of input stream.");
        }

        if (c == 'n') {
            object = readNull(reader);
        } else if (c == '"' || c == '\'') {
            object = readString(reader);
        } else if (c == '+' || c == '-' || Character.isDigit(c)) {
            object = readNumber(reader);
        } else if (c == 't' || c == 'f') {
            object = readBoolean(reader);
        } else if (c == '[') {
            object = readList(reader);
        } else if (c == '{') {
            object = readMap(reader);
        } else {
            throw new SerializationException("Unexpected character in input stream.");
        }

        return object;
    }

    private void skipWhitespaceAndComments(Reader reader)
        throws IOException, SerializationException {
        while (c != -1
            && (Character.isWhitespace(c)
                || c == '/')) {
            boolean comment = (c == '/');

            // Read the next character
            c = reader.read();

            if (comment) {
                if (c == '/') {
                    // Single-line comment
                    while (c != -1
                        && c != '\n'
                        && c != '\r') {
                        c = reader.read();
                    }
                } else if (c == '*') {
                    // Multi-line comment
                    boolean closed = false;

                    while (c != -1
                        && !closed) {
                        c = reader.read();

                        if (c == '*') {
                            c = reader.read();
                            closed = (c == '/');
                        }
                    }

                    if (!closed) {
                        throw new SerializationException("Unexpected end of input stream.");
                    }

                    if (c != -1) {
                        c = reader.read();
                    }
                } else {
                    throw new SerializationException("Unexpected character in input stream.");
                }
            }
        }
    }

    private Object readNull(Reader reader)
        throws IOException, SerializationException {
        String nullString = "null";

        int n = nullString.length();
        int i = 0;

        while (c != -1 && i < n) {
            if (nullString.charAt(i) != c) {
                throw new SerializationException("Unexpected character in input stream.");
            }

            c = reader.read();
            i++;
        }

        if (i < n) {
            throw new SerializationException("Incomplete null value in input stream.");
        }

        return null;
    }

    private String readString(Reader reader)
        throws IOException, SerializationException {
        StringBuilder stringBuilder = new StringBuilder();

        // Use the same delimiter to close the string
        int t = c;

        // Move to the next character after the delimiter
        c = reader.read();

        while (c != -1 && c != t) {
            if (c == '\\') {
                c = reader.read();

                if (c == 'b') {
                    c = '\b';
                } else if (c == 'f') {
                    c = '\f';
                } else if (c == 'n') {
                    c = '\n';
                } else if (c == 'r') {
                    c = '\r';
                } else if (c == 't') {
                    c = '\t';
                } else if (c == 'u') {
                    StringBuilder unicodeBuilder = new StringBuilder();
                    while (unicodeBuilder.length() < 4) {
                        c = reader.read();
                        unicodeBuilder.append((char)c);
                    }

                    String unicode = unicodeBuilder.toString();
                    c = (char)Integer.parseInt(unicode, 16);
                } else {
                    if (!(c == '\\'
                        || c == '/'
                        || c == '\"'
                        || c == '\''
                        || c == t)) {
                        throw new SerializationException("Unsupported escape sequence in input stream.");
                    }
                }
            }

            stringBuilder.append((char)c);
            c = reader.read();
        }

        if (c != t) {
            throw new SerializationException("Unterminated string in input stream.");
        }

        // Move to the next character after the delimiter
        c = reader.read();

        return stringBuilder.toString();
    }

    private Number readNumber(Reader reader) throws IOException {
        Number number = null;

        StringBuilder stringBuilder = new StringBuilder();
        boolean negative = false;
        boolean integer = true;

        if (c == '+' || c == '-') {
            negative = (c == '-');
            c = reader.read();
        }

        while (c != -1 && (Character.isDigit(c) || c == '.'
            || c == 'e' || c == 'E')) {
            stringBuilder.append((char)c);
            integer &= !(c == '.');
            c = reader.read();
        }

        if (integer) {
            // TODO 5/28/2008 Remove 32-bit optimization when 64-bit processors
            // are more prevalent
            long value = Long.parseLong(stringBuilder.toString()) * (negative ? -1 : 1);

            if (value > Integer.MAX_VALUE
                || value < Integer.MIN_VALUE) {
                number = value;
            } else {
                number = (int)value;
            }
        } else {
            number = Double.parseDouble(stringBuilder.toString()) * (negative ? -1.0d : 1.0d);
        }

        return number;
    }

    private Boolean readBoolean(Reader reader)
        throws IOException, SerializationException {
        String booleanString = (c == 't') ? "true" : "false";
        int n = booleanString.length();
        int i = 0;

        while (c != -1 && i < n) {
            if (booleanString.charAt(i) != c) {
                throw new SerializationException("Unexpected character in input stream.");
            }

            c = reader.read();
            i++;
        }

        if (i < n) {
            throw new SerializationException("Incomplete boolean value in input stream.");
        }

        return Boolean.parseBoolean(booleanString);
    }

    private List<Object> readList(Reader reader)
        throws IOException, SerializationException {
        List<Object> list = new ArrayList<Object>();

        // Move to the next character after '['
        c = reader.read();
        skipWhitespaceAndComments(reader);

        while (c != -1 && c != ']') {
            list.add(readValue(reader));
            skipWhitespaceAndComments(reader);

            if (c == ',') {
                c = reader.read();
                skipWhitespaceAndComments(reader);
            } else if (c == -1) {
                throw new SerializationException("Unexpected end of input stream.");
            } else {
                if (c != ']') {
                    throw new SerializationException("Unexpected character in input stream.");
                }
            }
        }

        // Move to the next character after ']'
        c = reader.read();

        if (immutable) {
            list = new ImmutableList<Object>(list);
        }

        return list;
    }

    private Map<String, Object> readMap(Reader reader)
        throws IOException, SerializationException {
        Map<String, Object> map = new HashMap<String, Object>();

        // Move to the next character after '{'
        c = reader.read();
        skipWhitespaceAndComments(reader);

        while (c != -1 && c != '}') {
            String key = null;

            if (c == '"' || c == '\'') {
                // The key is a delimited string
                key = readString(reader);
            } else {
                // The key is an undelimited string; it must adhere to Java
                // identifier syntax
                StringBuilder keyStringBuilder = new StringBuilder();

                if (!Character.isJavaIdentifierStart(c)) {
                    throw new SerializationException("Illegal identifier start character.");
                }

                while (c != -1
                    && c != ':' && !Character.isWhitespace(c)) {
                    if (!Character.isJavaIdentifierPart(c)) {
                        throw new SerializationException("Illegal identifier character.");
                    }

                    keyStringBuilder.append((char)c);
                    c = reader.read();
                }

                if (c == -1) {
                    throw new SerializationException("Unexpected end of input stream.");
                }

                key = keyStringBuilder.toString();
            }

            if (key == null
                || key.length() == 0) {
                throw new SerializationException("\"" + key + "\" is not a valid key.");
            }

            skipWhitespaceAndComments(reader);

            if (c != ':') {
                throw new SerializationException("Unexpected character in input stream.");
            }

            // Move to the first character after ':'
            c = reader.read();

            map.put(key, readValue(reader));
            skipWhitespaceAndComments(reader);

            if (c == ',') {
                c = reader.read();
                skipWhitespaceAndComments(reader);
            } else if (c == -1) {
                throw new SerializationException("Unexpected end of input stream.");
            } else {
                if (c != '}') {
                    throw new SerializationException("Unexpected character in input stream.");
                }
            }
        }

        // Move to the first character after '}'
        c = reader.read();

        if (immutable) {
            map = new ImmutableMap<String, Object>(map);
        }

        return map;
    }

    /**
     * Writes data to a JSON stream.
     *
     * @param object
     *
     * @param outputStream
     * The output stream to which data will be written.
     *
     * @see #writeObject(Object, Writer)
     */
    @Override
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset),
            BUFFER_SIZE);
        writeObject(object, writer);
    }

    /**
     * Writes data to a JSON stream.
     *
     * @param object
     * The object to serialize. Must be one of the following types:
     *
     * <ul>
     * <li>java.lang.String</li>
     * <li>java.lang.Number</li>
     * <li>java.lang.Boolean</li>
     * <li>pivot.collections.List</li>
     * <li>pivot.collections.Map</li>
     * </ul>
     *
     * @param writer
     * The writer to which data will be written.
     */
    @SuppressWarnings("unchecked")
    public void writeObject(Object object, Writer writer)
        throws IOException, SerializationException {
        if (writer == null) {
            throw new IllegalArgumentException("writer is null.");
        }

        if (object == null) {
            writer.append("null");
        } else if (object instanceof String) {
            String string = (String)object;
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0, n = string.length(); i < n; i++) {
                char c = string.charAt(i);

                switch(c) {
                    case '\t': {
                        stringBuilder.append("\\t");
                        break;
                    }

                    case '\n': {
                        stringBuilder.append("\\n");
                        break;
                    }

                    case '\\':
                    case '\"':
                    case '\'': {
                        stringBuilder.append("\\" + c);
                        break;
                    }

                    default: {
                        if (charset.name().startsWith("UTF")
                            || c <= 0xFF) {
                            stringBuilder.append(c);
                        } else {
                            stringBuilder.append("\\u");
                            stringBuilder.append(String.format("%04x", (short)c));
                        }
                    }
                }

            }

            writer.append("\"" + stringBuilder.toString() + "\"");
        } else if (object instanceof Number) {
            writer.append(object.toString());
        } else if (object instanceof Boolean) {
            writer.append(object.toString());
        } else if (object instanceof List<?>) {
            List<Object> list = (List<Object>)object;
            writer.append("[");

            int i = 0;
            for (Object item : list) {
                if (i > 0) {
                    writer.append(", ");
                }

                writeObject(item, writer);
                i++;
            }

            writer.append("]");
        } else if (object instanceof Map<?, ?>) {
            Map<String, Object> map = (Map<String, Object>)object;
            writer.append("{");

            int i = 0;
            for (String key : map) {
                Object value = map.get(key);

                boolean identifier = true;
                StringBuilder keyStringBuilder = new StringBuilder();

                for (int j = 0, n = key.length(); j < n; j++) {
                    char c = key.charAt(j);
                    identifier &= Character.isJavaIdentifierPart(c);

                    if (c == '"') {
                        keyStringBuilder.append('\\');
                    }

                    keyStringBuilder.append(c);
                }

                key = keyStringBuilder.toString();

                if (i > 0) {
                    writer.append(", ");
                }

                // Write the key
                if (!identifier || alwaysDelimitMapKeys) {
                    writer.append('"');
                }

                writer.append(key);

                if (!identifier || alwaysDelimitMapKeys) {
                    writer.append('"');
                }

                writer.append(": ");

                // Write the value
                writeObject(value, writer);

                i++;
            }

            writer.append("}");
        } else {
            throw new IllegalArgumentException(object.getClass()
                + " is not a supported type.");
        }

        writer.flush();
    }

    @Override
    public String getMIMEType(Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
    }

    /**
     * Returns a flag indicating whether or not map keys will always be
     * quote-delimited.
     */
    public boolean getAlwaysDelimitMapKeys() {
        return alwaysDelimitMapKeys;
    }

    /**
     * Sets a flag indicating that map keys should always be quote-delimited.
     *
     * @param alwaysDelimitMapKeys
     * <tt>true</tt> to bound map keys in double quotes; <tt>false</tt> to
     * only quote-delimit keys as necessary.
     */
    public void setAlwaysDelimitMapKeys(boolean alwaysDelimitMapKeys) {
        this.alwaysDelimitMapKeys = alwaysDelimitMapKeys;
    }

    /**
     * @deprecated
     * @see JSON#get(Object, String)
     */
    public static Object get(Object root, String path) {
        return JSON.get(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getString(Object, String)
     */
    public static String getString(Object root, String path) {
        return JSON.getString(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getNumber(Object, String)
     */
    public static Number getNumber(Object root, String path) {
        return JSON.getNumber(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getShort(Object, String)
     */
    public static Short getShort(Object root, String path) {
        return JSON.getShort(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getInteger(Object, String)
     */
    public static Integer getInteger(Object root, String path) {
        return JSON.getInteger(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getLong(Object, String)
     */
    public static Long getLong(Object root, String path) {
        return JSON.getLong(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getFloat(Object, String)
     */
    public static Float getFloat(Object root, String path) {
        return JSON.getFloat(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getDouble(Object, String)
     */
    public static Double getDouble(Object root, String path) {
        return JSON.getDouble(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getBoolean(Object, String)
     */
    public static Boolean getBoolean(Object root, String path) {
        return JSON.getBoolean(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getList(Object, String)
     */
    public static List<?> getList(Object root, String path) {
        return JSON.getList(root, path);
    }

    /**
     * @deprecated
     * @see JSON#getMap(Object, String)
     */
    public static Map<String, ?> getMap(Object root, String path) {
        return JSON.getMap(root, path);
    }

    /**
     * @deprecated
     * @see JSON#put(Object, String, Object)
     */
    public static Object put(Object root, String path, Object value) {
        return JSON.put(root, path, value);
    }

    /**
     * @deprecated
     * @see JSON#remove(Object, String)
     */
    public static Object remove(Object root, String path) {
        return JSON.remove(root, path);
    }

    /**
     * @deprecated
     * @see JSON#containsKey(Object, String)
     */
    public static boolean containsKey(Object root, String path) {
        return JSON.containsKey(root, path);
    }

    /**
     * Converts a JSON value to a Java object.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed object.
     */
    public static Object parse(String json) throws SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();

        Object object;
        try {
            object = jsonSerializer.readObject(new StringReader(json));
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }

        return object;
    }

    /**
     * Converts a JSON value to a string.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed string.
     */
    public static String parseString(String json) throws SerializationException {
        return (String)parse(json);
    }

    /**
     * Converts a JSON value to a number.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed number.
     */
    public static Number parseNumber(String json) throws SerializationException {
        return (Number)parse(json);
    }

    /**
     * Converts a JSON value to a short.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed short.
     */
    public static Short parseShort(String json) throws SerializationException {
        return (Short)parse(json);
    }

    /**
     * Converts a JSON value to a integer.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed integer.
     */
    public static Integer parseInteger(String json) throws SerializationException {
        return (Integer)parse(json);
    }

    /**
     * Converts a JSON value to a long.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed number.
     */
    public static Long parseLong(String json) throws SerializationException {
        return (Long)parse(json);
    }

    /**
     * Converts a JSON value to a float.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed float.
     */
    public static Float parseFloat(String json) throws SerializationException {
        return (Float)parse(json);
    }

    /**
     * Converts a JSON value to a double.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed double.
     */
    public static Double parseDouble(String json) throws SerializationException {
        return (Double)parse(json);
    }

    /**
     * Converts a JSON value to a boolean.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed boolean.
     */
    public static Boolean parseBoolean(String json) throws SerializationException {
        return (Boolean)parse(json);
    }

    /**
     * Converts a JSON value to a list.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed list.
     */
    public static List<?> parseList(String json) throws SerializationException {
        return (List<?>)parse(json);
    }

    /**
     * Converts a JSON value to a map.
     *
     * @param json
     * The JSON value.
     *
     * @return
     * The parsed map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> parseMap(String json) throws SerializationException {
        return (Map<String, ?>)parse(json);
    }

    /**
     * Converts a object to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(Object value) throws SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();
        StringWriter writer = new StringWriter();

        try {
            jsonSerializer.writeObject(value, writer);
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }

        return writer.toString();
    }

    /**
     * Converts a string to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(String value) {
        try {
            return toString((Object)value);
        } catch(SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Converts a number to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(Number value) {
        try {
            return toString((Object)value);
        } catch(SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Converts a boolean to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(Boolean value) {
        try {
            return toString((Object)value);
        } catch(SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Converts a list to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(List<?> value) {
        try {
            return toString((Object)value);
        } catch(SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Converts a map to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(Map<String, ?> value) {
        try {
            return toString((Object)value);
        } catch(SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }
}
