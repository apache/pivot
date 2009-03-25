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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.nio.charset.Charset;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.collections.Sequence;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a JavaScript Object Notation (JSON) file.
 * <p>
 * TODO Wrap reader in a CountingReader that tracks line/character index.
 *
 * @author gbrown
 */
public class JSONSerializer implements Serializer<Object> {
    private Charset charset = null;

    private int c = -1;
    private boolean alwaysDelimitMapKeys = false;

    public static final String MIME_TYPE = "application/json";
    public static final int BUFFER_SIZE = 2048;

    public JSONSerializer() {
        this(Charset.defaultCharset());
    }

    public JSONSerializer(String charsetName) {
        this(charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName));
    }

    public JSONSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
    }

    /**
     * Reads data from a JSON stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @see #readObject(Reader)
     */
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
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

                if (c == 't') {
                	c = '\t';
                } else if (c == 'n') {
                	c = '\n';
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

            if (key == null
                || key.length() == 0) {
                throw new SerializationException("\"" + key + "\" is not a valid key.");
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
     * Writes data to a JSON stream.
     *
     * @param object
     *
     * @param outputStream
     * The output stream to which data will be written.
     *
     * @see #writeObject(Object, Writer)
     */
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
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
                if (!(value instanceof Dictionary<?, ?>)){
                    throw new IllegalArgumentException("Invalid path.");
                }

                String key = identifier;
                Dictionary<String, Object> dictionary = (Dictionary<String, Object>)value;
                value = dictionary.get(key);
            } else {
                if (!(value instanceof Sequence<?>)){
                    throw new IllegalArgumentException("Invalid path.");
                }

                int index = Integer.parseInt(identifier);
                Sequence<Object> sequence = (Sequence<Object>)value;
                value = sequence.get(index);
            }
        }

        return value;
    }

    /**
     * Returns the value at the given path as a string.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static String getString(Object root, String path) {
        return (String)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a number.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Number getNumber(Object root, String path) {
        return (Number)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a short.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Short getShort(Object root, String path) {
        return (Short)getValue(root, path);
    }

    /**
     * Returns the value at the given path as an integer.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Integer getInteger(Object root, String path) {
        return (Integer)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a long.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Long getLong(Object root, String path) {
        return (Long)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a float.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Float getFloat(Object root, String path) {
        return (Float)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a double.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Double getDouble(Object root, String path) {
        return (Double)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a boolean.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    public static Boolean getBoolean(Object root, String path) {
        return (Boolean)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a list.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    @SuppressWarnings("unchecked")
    public static List<?> getList(Object root, String path) {
        return (List<?>)getValue(root, path);
    }

    /**
     * Returns the value at the given path as a map.
     *
     * @param root
     * @param path
     *
     * @see #getValue(Object, String)
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> getMap(Object root, String path) {
        return (Map<String, ?>)getValue(root, path);
    }

    /**
     * Parses a JSON-formatted array value into a list.
     *
     * @param string
     * A string containing a JSON array (e.g. "[1, 2, 3]").
     *
     * @return
     * A {@link List} instance containing the parsed JSON data.
     */
    @SuppressWarnings("unchecked")
    public static List<?> parseList(String string) {
        List<?> list = null;
        JSONSerializer jsonSerializer = new JSONSerializer();

        Object object = null;
        try {
            object = jsonSerializer.readObject(new StringReader(string));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        list = (List<?>)object;

        return list;
    }

    /**
     * Parses a JSON-formatted object value into a map.
     *
     * @param string
     * A string containing a JSON object (e.g. "{a:1, b:2, c:3}").
     *
     * @return
     * A {@link Map} instance containing the parsed JSON data.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> parseMap(String string) {
        Map<String, ?> map = null;
        JSONSerializer jsonSerializer = new JSONSerializer();

        Object object = null;
        try {
            object = jsonSerializer.readObject(new StringReader(string));
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        map = (Map<String, ?>)object;

        return map;
    }
}
