/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import pivot.collections.ArrayList;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;

/**
 * Serializes data to and from JavaScript Object Notation (JSON).
 *
 * TODO Wrap reader in a CountingReader that tracks line/character index.
 *
 * @author gbrown
 */
public class JSONSerializer implements Serializer {
    private int c = -1;
    private boolean alwaysDelimitMapKeys = false;

    public static final String MIME_TYPE = "application/json";

    /**
     * Deserializes data from a JSON stream. See {@link #readObject(Reader)}.
     */
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        Reader reader = new InputStreamReader(inputStream);
        Object object = readObject(reader);

        return object;
    }

    /**
     * Deserializes data from a JSON stream.
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
        // Move to the first character
        c = reader.read();

        // Read the root value
        Object object = readValue(reader);

        return object;
    }

    private Object readValue(Reader reader)
        throws IOException, SerializationException {
        Object object = null;

        skipWhitespace(reader);

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

    private void skipWhitespace(Reader reader)
        throws IOException {
        while (c != -1 && Character.isWhitespace(c)) {
            c = reader.read();
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
                if (!(c == '\'' || c == t)) {
                    throw new SerializationException("Unsupported escape sequence in input stream.");
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

    private Number readNumber(Reader reader)
        throws IOException, SerializationException {
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

        while (c != -1 && c != ']') {
            list.add(readValue(reader));
            skipWhitespace(reader);

            if (c == ',') {
                c = reader.read();
                skipWhitespace(reader);
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

        return list;
    }

    private Map<String, Object> readMap(Reader reader)
        throws IOException, SerializationException {
        Map<String, Object> map = new HashMap<String, Object>();

        // Move to the next character after '{'
        c = reader.read();
        skipWhitespace(reader);

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

            skipWhitespace(reader);

            if (c != ':') {
                throw new SerializationException("Unexpected character in input stream.");
            }

            // Move to the first character after ':'
            c = reader.read();

            map.put(key, readValue(reader));
            skipWhitespace(reader);

            if (c == ',') {
                c = reader.read();
                skipWhitespace(reader);
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

        return map;
    }

    /**
     * Serializes data to a JSON stream. See {@link #writeObject(Object, OutputStream)
     */
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        Writer writer = null;

        try {
            writer = new OutputStreamWriter(outputStream);
            writeObject(object, writer);
        } finally {
            if (writer != null) {
                writer.flush();
            }
        }
    }

    /**
     * Serializes data to a JSON stream.
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
     */
    @SuppressWarnings("unchecked")
    public void writeObject(Object object, Writer writer)
        throws IOException, SerializationException {
        if (object == null) {
            writer.append("null");
        } else if (object instanceof String) {
            writer.append("\"" + ((String)object).replace("\"", "\\\"") + "\"");
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
                writeObject(map.get(key), writer);

                i++;
            }

            writer.append("}");
        } else {
            throw new IllegalArgumentException(object.getClass()
                + " is not a supported type.");
        }
    }

    public String getMIMEType() {
        return MIME_TYPE;
    }

    /**
     * Returns a flag indicating whether or not map keys will always be
     * quote-delimited.
     *
     * @return
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
     * Returns the value at the given path.
     *
     * @param root
     * The root object; must be an instance of {@link pivot.collections.Map}
     * or {@link pivot.collections.List}.
     *
     * @param path
     * The path to the value, in JavaScript path notation.
     *
     * @return
     * The value at the given path.
     */
    @SuppressWarnings("unchecked")
    public static Object getValue(Object root, String path) {
        Object value = root;

        int i = 0;
        int n = path.length();

        while (i < n) {
            char c = path.charAt(i++);

            boolean keyed = true;
            StringBuilder identifierBuilder = new StringBuilder();

            boolean bracketed = (c == '[');
            if (bracketed
                && i < n) {
                c = path.charAt(i++);

                char quote = Character.UNASSIGNED;

                boolean quoted = (c == '"'
                    || c == '\'');
                if (quoted
                    && i < n) {
                    quote = c;
                    c = path.charAt(i++);
                }

                keyed = quoted;

                while (i <= n
                    && bracketed) {
                    bracketed = quoted || (c != ']');

                    if (bracketed) {
                        if (c == quote) {
                            if (i < n) {
                                c = path.charAt(i++);
                                quoted = (c == quote);
                            }
                        }

                        if (quoted || c != ']') {
                            if (Character.isISOControl(c)) {
                                throw new IllegalArgumentException("Illegal identifier character.");
                            }

                            identifierBuilder.append(c);

                            if (i < n) {
                                c = path.charAt(i++);
                            }
                        }
                    }
                }

                if (quoted) {
                    throw new IllegalArgumentException("Unterminated quoted identifier.");
                }

                if (bracketed) {
                    throw new IllegalArgumentException("Unterminated bracketed identifier.");
                }

                if (i < n) {
                    c = path.charAt(i);

                    if (c == '.') {
                        i++;
                    }
                }
            } else {
                keyed = true;

                while(i <= n
                    && c != '.'
                    && c != '[') {
                    if (!Character.isJavaIdentifierPart(c)) {
                        throw new IllegalArgumentException("Illegal identifier character.");
                    }

                    identifierBuilder.append(c);

                    if (i < n) {
                        c = path.charAt(i);
                    }

                    i++;
                }

                if (c == '[') {
                    i--;
                }
            }

            if (c == '.'
                && i == n) {
                throw new IllegalArgumentException("Path cannot end with a '.' character.");
            }

            if (identifierBuilder.length() == 0) {
                throw new IllegalArgumentException("Missing identifier.");
            }

            String identifier = identifierBuilder.toString();

            if (keyed) {
                if (!(value instanceof Map<?, ?>)){
                    throw new IllegalArgumentException("Invalid path.");
                }

                String key = identifier;
                Map<String, Object> map = (Map<String, Object>)value;
                value = map.get(key);
            } else {
                if (!(value instanceof List<?>)){
                    throw new IllegalArgumentException("Invalid path.");
                }

                int index = Integer.parseInt(identifier);
                List<Object> list = (List<Object>)value;
                value = list.get(index);
            }
        }

        return value;
    }

    public static String getString(Object root, String path) {
        return (String)getValue(root, path);
    }

    public static Number getNumber(Object root, String path) {
        return (Number)getValue(root, path);
    }

    public static Short getShort(Object root, String path) {
        return (Short)getValue(root, path);
    }

    public static Integer getInteger(Object root, String path) {
        return (Integer)getValue(root, path);
    }

    public static Long getLong(Object root, String path) {
        return (Long)getValue(root, path);
    }

    public static Float getFloat(Object root, String path) {
        return (Float)getValue(root, path);
    }

    public static Double getDouble(Object root, String path) {
        return (Double)getValue(root, path);
    }

    public static Boolean getBoolean(Object root, String path) {
        return (Boolean)getValue(root, path);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> getList(Object root, String path) {
        return (List<Object>)getValue(root, path);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Object root, String path) {
        return (Map<String, Object>)getValue(root, path);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> parseList(String string) {
        List<Object> list = null;
        JSONSerializer jsonSerializer = new JSONSerializer();

        Object object = null;
        try {
            object = jsonSerializer.readObject(new StringReader(string));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        list = (List<Object>)object;

        return list;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> parseMap(String string) {
        Map<String, Object> map = null;
        JSONSerializer jsonSerializer = new JSONSerializer();

        Object object = null;
        try {
            object = jsonSerializer.readObject(new StringReader(string));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        map = (Map<String, Object>)object;

        return map;
    }
}
