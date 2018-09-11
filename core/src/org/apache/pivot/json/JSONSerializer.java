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
package org.apache.pivot.json;

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.adapter.MapAdapter;
import org.apache.pivot.io.EchoReader;
import org.apache.pivot.io.EchoWriter;
import org.apache.pivot.serialization.MacroReader;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.Constants;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Serializer} interface that reads data from and
 * writes data to a JavaScript Object Notation (JSON) file.
 */
public class JSONSerializer implements Serializer<Object> {
    private Charset charset;
    private Type type;

    private boolean alwaysDelimitMapKeys = false;
    private boolean verbose = false;
    private boolean macros = false;

    private int c = -1;

    private JSONSerializerListener.Listeners jsonSerializerListeners = null;

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    public static final Type DEFAULT_TYPE = Object.class;

    public static final String JSON_EXTENSION = "json";
    public static final String MIME_TYPE = "application/json";

    public JSONSerializer() {
        this(DEFAULT_CHARSET, DEFAULT_TYPE);
    }

    public JSONSerializer(final Charset charset) {
        this(charset, DEFAULT_TYPE);
    }

    public JSONSerializer(final Type type) {
        this(DEFAULT_CHARSET, type);
    }

    public JSONSerializer(final Charset charset, final Type type) {
        Utils.checkNull(charset, "charset");
        Utils.checkNull(type, "type");

        this.charset = charset;
        this.type = type;
    }

    /**
     * Returns the character set used to encode/decode the JSON data.
     * @return The current character set.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns the type of the object that will be returned by
     * {@link #readObject(Reader)}.
     * @return The current object type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns a flag indicating whether or not map keys will always be
     * quote-delimited.
     * <p> Note: the JSON "standard" requires keys to be delimited.
     * @return <tt>true</tt> if map keys must always be delimited (that is,
     * enclosed in double quotes), <tt>false</tt> for the default behavior
     * that does not require double quotes.
     */
    public boolean getAlwaysDelimitMapKeys() {
        return alwaysDelimitMapKeys;
    }

    /**
     * Sets a flag indicating that map keys should always be quote-delimited.
     *
     * @param alwaysDelimitMapKeys <tt>true</tt> to bound map keys in double
     * quotes; <tt>false</tt> to only quote-delimit keys as necessary.
     */
    public void setAlwaysDelimitMapKeys(final boolean alwaysDelimitMapKeys) {
        this.alwaysDelimitMapKeys = alwaysDelimitMapKeys;
    }

    /**
     * Returns the serializer's verbosity flag.
     * @return The verbosity flag for this serializer.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the serializer's verbosity flag. When verbosity is enabled, all data
     * read or written will be echoed to the console.
     *
     * @param verbose <tt>true</tt> to set verbose mode, <tt>false</tt> to disable.
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Returns the flag indicating whether this serializer will allow macros in the text.
     * @return The "macros allowed" flag.
     */
    public boolean getAllowMacros() {
        return macros;
    }

    /**
     * Sets the flag indicating whether macros are allowed in the text.  This is definitely
     * a non-standard feature.  See the documentation in {@link MacroReader} for more details
     * on the specification of macros.
     * <p> Note: must be called before {@link #readObject} is called.
     * @param macros Flag indicating whether macros are allowed (default is {@code false}).
     * The flag must be set to true in order to activate this feature, because there is a
     * definitely measured 25x slowdown when using it, even if no macros are defined.
     */
    public void setAllowMacros(final boolean macros) {
        this.macros = macros;
    }

    /**
     * Reads data from a JSON stream.
     *
     * @param inputStream The input stream from which data will be read.
     * @see #readObject(Reader)
     */
    @SuppressWarnings("resource")
    @Override
    public Object readObject(final InputStream inputStream) throws IOException, SerializationException {
        Utils.checkNull(inputStream, "inputStream");

        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), Constants.BUFFER_SIZE);
        if (verbose) {
            reader = new EchoReader(reader);
        }

        return readObject(reader);
    }

    /**
     * Reads data from a JSON stream.
     * <p> Processes macros at this level using {@link MacroReader}.
     *
     * @param reader The reader from which data will be read.
     * @return One of the following types, depending on the content of the stream
     * and the value of {@link #getType()}:
     * <ul>
     * <li>pivot.collections.Dictionary</li>
     * <li>pivot.collections.Sequence</li>
     * <li>java.lang.String</li>
     * <li>java.lang.Number</li>
     * <li>java.lang.Boolean</li>
     * <li><tt>null</tt></li>
     * <li>A JavaBean object</li>
     * </ul>
     * @throws IOException for any I/O related errors while reading.
     * @throws SerializationException for any formatting errors in the data.
     */
    public Object readObject(final Reader reader) throws IOException, SerializationException {
        Utils.checkNull(reader, "reader");

        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        Reader realReader = lineNumberReader;
        if (macros) {
            realReader = new MacroReader(realReader);
        }
        // Move to the first character
        c = realReader.read();

        // Ignore BOM (if present)
        if (c == Constants.BYTE_ORDER_MARK) {
            c = realReader.read();
        }

        // Read the root value
        Object object;
        try {
            object = readValue(realReader, type, type.getTypeName());
        } catch (SerializationException exception) {
            System.err.println("An error occurred while processing input at line number "
                + (lineNumberReader.getLineNumber() + 1));

            throw exception;
        }

        return object;
    }

    private Object readValue(final Reader reader, final Type typeArgument, final String key)
        throws IOException, SerializationException {
        Object object = null;

        skipWhitespaceAndComments(reader);

        if (c == -1) {
            throw new SerializationException("Unexpected end of input stream.");
        }

        if (c == 'n') {
            object = readNullValue(reader);
        } else if (c == '"' || c == '\'') {
            object = readStringValue(reader, typeArgument, key);
        } else if (c == '+' || c == '-' || Character.isDigit(c)) {
            object = readNumberValue(reader, typeArgument, key);
        } else if (c == 't' || c == 'f') {
            object = readBooleanValue(reader, typeArgument, key);
        } else if (c == '[') {
            object = readListValue(reader, typeArgument, key);
        } else if (c == '{') {
            object = readMapValue(reader, typeArgument);
        } else {
            throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
        }

        return object;
    }

    private void skipWhitespaceAndComments(final Reader reader) throws IOException, SerializationException {
        while (c != -1 && (Character.isWhitespace(c) || c == '/')) {
            boolean comment = (c == '/');

            // Read the next character
            c = reader.read();

            if (comment) {
                if (c == '/') {
                    // Single-line comment
                    while (c != -1 && c != '\n' && c != '\r') {
                        c = reader.read();
                    }
                } else if (c == '*') {
                    // Multi-line comment
                    boolean closed = false;

                    while (c != -1 && !closed) {
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
                    throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
                }
            }
        }
    }

    private Object readNullValue(final Reader reader) throws IOException, SerializationException {
        String nullString = "null";

        int n = nullString.length();
        int i = 0;

        while (c != -1 && i < n) {
            if (nullString.charAt(i) != c) {
                throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
            }

            c = reader.read();
            i++;
        }

        if (i < n) {
            throw new SerializationException("Incomplete null value in input stream.");
        }

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readNull(this);
        }

        return null;
    }

    private String readString(final Reader reader) throws IOException, SerializationException {
        StringBuilder stringBuilder = new StringBuilder();

        // Use the same delimiter to close the string
        int t = c;

        // Move to the next character after the delimiter
        c = reader.read();

        while (c != -1 && c != t) {
            // The JSON spec says that control characters are not supported,
            // so silently ignore them
            if (!Character.isISOControl(c)) {
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
                            unicodeBuilder.append((char) c);
                        }

                        String unicode = unicodeBuilder.toString();
                        c = (char) Integer.parseInt(unicode, 16);
                    } else {
                        if (!(c == '\\' || c == '/' || c == '\"' || c == '\'' || c == t)) {
                            throw new SerializationException(
                                "Unsupported escape sequence in input stream.");
                        }
                    }
                }

                stringBuilder.append((char) c);
            }

            c = reader.read();
        }

        if (c != t) {
            throw new SerializationException("Unterminated string in input stream.");
        }

        // Move to the next character after the delimiter
        c = reader.read();

        return stringBuilder.toString();
    }

    private Object readStringValue(final Reader reader, final Type typeArgument, final String key)
        throws IOException, SerializationException {
        if (!(typeArgument instanceof Class<?>)) {
            throw new SerializationException("Cannot convert string to " + typeArgument + ".");
        }

        String string = readString(reader);

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readString(this, string);
        }

        return BeanAdapter.coerce(string, (Class<?>) typeArgument, key);
    }

    private Object readNumberValue(final Reader reader, final Type typeArgument, final String key)
        throws IOException, SerializationException {
        if (!(typeArgument instanceof Class<?>)) {
            throw new SerializationException("Cannot convert number to " + typeArgument + ".");
        }

        Number number = null;

        StringBuilder stringBuilder = new StringBuilder();
        boolean negative = false;
        boolean integer = true;

        if (c == '+' || c == '-') {
            negative = (c == '-');
            c = reader.read();
        }

        while (c != -1 && (Character.isDigit(c) || c == '.' || c == 'e' || c == 'E' || c == '-')) {
            stringBuilder.append((char) c);
            integer &= !(c == '.');
            c = reader.read();
        }

        if (integer) {
            long value = Long.parseLong(stringBuilder.toString()) * (negative ? -1 : 1);

            if (value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
                number = value;
            } else {
                number = (int) value;
            }
        } else {
            number = Double.parseDouble(stringBuilder.toString()) * (negative ? -1.0d : 1.0d);
        }

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readNumber(this, number);
        }

        return BeanAdapter.coerce(number, (Class<?>) typeArgument, key);
    }

    private Object readBooleanValue(final Reader reader, final Type typeArgument, final String key)
        throws IOException, SerializationException {
        if (!(typeArgument instanceof Class<?>)) {
            throw new SerializationException("Cannot convert boolean to " + typeArgument + ".");
        }

        String text = (c == 't') ? "true" : "false";
        int n = text.length();
        int i = 0;

        while (c != -1 && i < n) {
            if (text.charAt(i) != c) {
                throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
            }

            c = reader.read();
            i++;
        }

        if (i < n) {
            throw new SerializationException("Incomplete boolean value in input stream.");
        }

        // Get the boolean value
        Boolean value = Boolean.parseBoolean(text);

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readBoolean(this, value);
        }

        return BeanAdapter.coerce(value, (Class<?>) typeArgument, key);
    }

    @SuppressWarnings("unchecked")
    private Object readListValue(final Reader reader, final Type typeArgument, final String key)
        throws IOException, SerializationException {
        Sequence<Object> sequence = null;
        Type itemType = null;

        if (typeArgument == Object.class) {
            // Return the default sequence and item types
            sequence = new ArrayList<>();
            itemType = Object.class;
        } else {
            // Determine the item type from generic parameters
            Type parentType = typeArgument;
            while (parentType != null) {
                if (parentType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) parentType;
                    Class<?> rawType = (Class<?>) parameterizedType.getRawType();

                    if (Sequence.class.isAssignableFrom(rawType)) {
                        itemType = parameterizedType.getActualTypeArguments()[0];
                    }

                    break;
                }

                Class<?> classType = (Class<?>) parentType;
                Type[] genericInterfaces = classType.getGenericInterfaces();

                for (int i = 0; i < genericInterfaces.length; i++) {
                    Type genericInterface = genericInterfaces[i];

                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                        Class<?> interfaceType = (Class<?>) parameterizedType.getRawType();

                        if (Sequence.class.isAssignableFrom(interfaceType)) {
                            itemType = parameterizedType.getActualTypeArguments()[0];

                            if (itemType instanceof TypeVariable<?>) {
                                itemType = Object.class;
                            }

                            break;
                        }
                    }
                }

                if (itemType != null) {
                    break;
                }

                parentType = classType.getGenericSuperclass();
            }

            if (itemType == null) {
                throw new SerializationException("Could not determine sequence item type.");
            }

            // Instantiate the sequence type
            Class<?> sequenceType;
            if (typeArgument instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) typeArgument;
                sequenceType = (Class<?>) parameterizedType.getRawType();
            } else {
                sequenceType = (Class<?>) typeArgument;
            }

            try {
                sequence = (Sequence<Object>) sequenceType.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                    | InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.beginSequence(this, sequence);
        }

        // Move to the next character after '['
        c = reader.read();
        skipWhitespaceAndComments(reader);

        while (c != -1 && c != ']') {
            sequence.add(readValue(reader, itemType, key));
            skipWhitespaceAndComments(reader);

            if (c == ',') {
                c = reader.read();
                skipWhitespaceAndComments(reader);
            } else if (c == -1) {
                throw new SerializationException("Unexpected end of input stream.");
            } else {
                if (c != ']') {
                    throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
                }
            }
        }

        // Move to the next character after ']'
        c = reader.read();

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.endSequence(this);
        }

        return sequence;
    }

    @SuppressWarnings("unchecked")
    private Object readMapValue(final Reader reader, final Type typeArgument)
        throws IOException, SerializationException {
        Dictionary<String, Object> dictionary = null;
        Type valueType = null;

        if (typeArgument == Object.class) {
            // Return the default dictionary and value types
            dictionary = new HashMap<>();
            valueType = Object.class;
        } else {
            // Determine the value type from generic parameters
            Type parentType = typeArgument;
            while (parentType != null) {
                if (parentType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) parentType;
                    Class<?> rawType = (Class<?>) parameterizedType.getRawType();

                    if (Dictionary.class.isAssignableFrom(rawType)) {
                        valueType = parameterizedType.getActualTypeArguments()[1];
                    }

                    break;
                }

                Class<?> classType = (Class<?>) parentType;
                Type[] genericInterfaces = classType.getGenericInterfaces();

                for (int i = 0; i < genericInterfaces.length; i++) {
                    Type genericInterface = genericInterfaces[i];

                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType) genericInterface;
                        Class<?> interfaceType = (Class<?>) parameterizedType.getRawType();

                        if (Dictionary.class.isAssignableFrom(interfaceType)) {
                            valueType = parameterizedType.getActualTypeArguments()[1];

                            if (valueType instanceof TypeVariable<?>) {
                                valueType = Object.class;
                            }

                            break;
                        }
                    }
                }

                if (valueType != null) {
                    break;
                }

                parentType = classType.getGenericSuperclass();
            }

            // Instantiate the dictionary or bean type
            if (valueType == null) {
                Class<?> beanType = (Class<?>) typeArgument;

                try {
                    dictionary = new BeanAdapter(beanType.getDeclaredConstructor().newInstance());
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                Class<?> dictionaryType;
                if (typeArgument instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType) typeArgument;
                    dictionaryType = (Class<?>) parameterizedType.getRawType();
                } else {
                    dictionaryType = (Class<?>) typeArgument;
                }

                try {
                    dictionary = (Dictionary<String, Object>) dictionaryType.getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                        | InvocationTargetException exception) {
                    throw new RuntimeException(exception);
                }
            }
        }

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.beginDictionary(this, dictionary);
        }

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
                StringBuilder keyBuilder = new StringBuilder();

                if (!Character.isJavaIdentifierStart(c)) {
                    throw new SerializationException("Illegal identifier start character.");
                }

                while (c != -1 && c != ':' && !Character.isWhitespace(c)) {
                    if (!Character.isJavaIdentifierPart(c)) {
                        throw new SerializationException("Illegal identifier character.");
                    }

                    keyBuilder.append((char) c);
                    c = reader.read();
                }

                if (c == -1) {
                    throw new SerializationException("Unexpected end of input stream.");
                }

                key = keyBuilder.toString();
            }

            if (key == null || key.length() == 0) {
                throw new SerializationException("\"" + key + "\" is not a valid key.");
            }

            // Notify listeners
            if (jsonSerializerListeners != null) {
                jsonSerializerListeners.readKey(this, key);
            }

            skipWhitespaceAndComments(reader);

            if (c != ':') {
                throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
            }

            // Move to the first character after ':'
            c = reader.read();

            if (valueType == null) {
                // The map is a bean instance; get the generic type of the property
                Type genericValueType = ((BeanAdapter) dictionary).getGenericType(key);

                if (genericValueType != null) {
                    // Set the value in the bean
                    dictionary.put(key, readValue(reader, genericValueType, key));
                } else {
                    // The property does not exist; ignore this value
                    readValue(reader, Object.class, key);
                }
            } else {
                dictionary.put(key, readValue(reader, valueType, key));
            }

            skipWhitespaceAndComments(reader);

            if (c == ',') {
                c = reader.read();
                skipWhitespaceAndComments(reader);
            } else if (c == -1) {
                throw new SerializationException("Unexpected end of input stream.");
            } else {
                if (c != '}') {
                    throw new SerializationException("Unexpected character in input stream: '" + (char) c + "'");
                }
            }
        }

        // Move to the first character after '}'
        c = reader.read();

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.endDictionary(this);
        }

        return (dictionary instanceof BeanAdapter) ? ((BeanAdapter) dictionary).getBean()
            : dictionary;
    }

    /**
     * Writes data to a JSON stream.
     *
     * @param object The root object to be written.
     * @param outputStream The output stream to which data will be written.
     * @throws IOException for any errors during the writing process.
     * @throws SerializationException for any formatting errors in the data.
     * @see #writeObject(Object, Writer)
     */
    @SuppressWarnings("resource")
    @Override
    public void writeObject(final Object object, final OutputStream outputStream)
        throws IOException, SerializationException {
        Utils.checkNull(outputStream, "outputStream");

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset),
            Constants.BUFFER_SIZE);
        if (verbose) {
            writer = new EchoWriter(writer);
        }

        writeObject(object, writer);
    }

    /**
     * Writes data to a JSON stream.
     *
     * @param object The object to serialize. Must be one of the following
     * types:
     * <ul>
     * <li>pivot.collections.Map</li>
     * <li>pivot.collections.List</li>
     * <li>java.lang.String</li>
     * <li>java.lang.Number</li>
     * <li>java.lang.Boolean</li>
     * <li><tt>null</tt></li>
     * </ul>
     * @param writer The writer to which data will be written.
     * @throws IOException for any errors during the writing process.
     * @throws SerializationException for any formatting errors in the data.
     */
    @SuppressWarnings("unchecked")
    public void writeObject(final Object object, final Writer writer)
        throws IOException, SerializationException {
        Utils.checkNull(writer, "writer");

        if (object == null) {
            writer.append("null");
        } else if (object instanceof String) {
            String string = (String) object;
            StringBuilder stringBuilder = new StringBuilder();

            for (int i = 0, n = string.length(); i < n; i++) {
                char ci = string.charAt(i);

                switch (ci) {
                    case '\t':
                        stringBuilder.append("\\t");
                        break;
                    case '\n':
                        stringBuilder.append("\\n");
                        break;
                    case '\r':
                        stringBuilder.append("\\r");
                        break;
                    case '\f':
                        stringBuilder.append("\\f");
                        break;
                    case '\b':
                        stringBuilder.append("\\b");
                        break;
                    case '\\':
                    case '\"':
                    case '\'':
                        stringBuilder.append("\\" + ci);
                        break;
                    default:
                        // For Unicode character sets if it is a control character, then use \\uXXXX notation
                        // and for other character sets if the value is an ASCII control character.
                        if ((charset.name().startsWith("UTF") && !Character.isISOControl(ci))
                         || (ci > 0x1F && ci != 0x7F && ci <= 0xFF)) {
                            stringBuilder.append(ci);
                        } else {
                            stringBuilder.append("\\u");
                            stringBuilder.append(String.format("%04x", (short) ci));
                        }
                        break;
                }

            }

            writer.append("\"" + stringBuilder.toString() + "\"");
        } else if (object instanceof Number) {
            Number number = (Number) object;

            if (number instanceof Float) {
                Float f = (Float) number;
                if (f.isNaN() || f.isInfinite()) {
                    throw new SerializationException(number + " is not a valid value.");
                }
            } else if (number instanceof Double) {
                Double d = (Double) number;
                if (d.isNaN() || d.isInfinite()) {
                    throw new SerializationException(number + " is not a valid value.");
                }
            }

            writer.append(number.toString());
        } else if (object instanceof Boolean) {
            writer.append(object.toString());
        } else if (object instanceof List<?>) {
            List<Object> list = (List<Object>) object;
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
        } else {
            Map<String, Object> map;
            if (object instanceof Map<?, ?>) {
                map = (Map<String, Object>) object;
            } else if (object instanceof java.util.Map<?, ?>) {
                map = new MapAdapter<>((java.util.Map<String, Object>) object);
            } else {
                map = new BeanAdapter(object, true);
            }

            writer.append("{");

            int i = 0;
            for (String key : map) {
                Object value = map.get(key);

                boolean identifier = true;
                StringBuilder keyStringBuilder = new StringBuilder();

                for (int j = 0, n = key.length(); j < n; j++) {
                    char cj = key.charAt(j);
                    identifier &= Character.isJavaIdentifierPart(cj);

                    if (cj == '"') {
                        keyStringBuilder.append('\\');
                    }

                    keyStringBuilder.append(cj);
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
        }

        writer.flush();
    }

    @Override
    public String getMIMEType(final Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
    }

    /**
     * Converts a JSON value to a Java object.
     *
     * @param json The JSON value.
     * @return The parsed object.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Object parse(final String json) throws SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();

        Object object;
        try {
            object = jsonSerializer.readObject(new StringReader(json));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return object;
    }

    /**
     * Converts a JSON value to a string.
     *
     * @param json The JSON value.
     * @return The parsed string.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static String parseString(final String json) throws SerializationException {
        return (String) parse(json);
    }

    /**
     * Converts a JSON value to a number.
     *
     * @param json The JSON value.
     * @return The parsed number.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Number parseNumber(final String json) throws SerializationException {
        return (Number) parse(json);
    }

    /**
     * Converts a JSON value to a short.
     *
     * @param json The JSON value.
     * @return The parsed short.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Short parseShort(final String json) throws SerializationException {
        return (Short) parse(json);
    }

    /**
     * Converts a JSON value to a integer.
     *
     * @param json The JSON value.
     * @return The parsed integer.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Integer parseInteger(final String json) throws SerializationException {
        return (Integer) parse(json);
    }

    /**
     * Converts a JSON value to a long.
     *
     * @param json The JSON value.
     * @return The parsed number.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Long parseLong(final String json) throws SerializationException {
        return (Long) parse(json);
    }

    /**
     * Converts a JSON value to a float.
     *
     * @param json The JSON value.
     * @return The parsed float.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Float parseFloat(final String json) throws SerializationException {
        return (Float) parse(json);
    }

    /**
     * Converts a JSON value to a double.
     *
     * @param json The JSON value.
     * @return The parsed double.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Double parseDouble(final String json) throws SerializationException {
        return (Double) parse(json);
    }

    /**
     * Converts a JSON value to a boolean.
     *
     * @param json The JSON value.
     * @return The parsed boolean.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static Boolean parseBoolean(final String json) throws SerializationException {
        return (Boolean) parse(json);
    }

    /**
     * Converts a JSON value to a list.
     *
     * @param json The JSON value.
     * @return The parsed list.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static List<?> parseList(final String json) throws SerializationException {
        return (List<?>) parse(json);
    }

    /**
     * Converts a JSON value to a map.
     *
     * @param json The JSON value.
     * @return The parsed map.
     * @throws SerializationException for any formatting errors in the data.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, ?> parseMap(final String json) throws SerializationException {
        return (Map<String, ?>) parse(json);
    }

    /**
     * Converts a object to a JSON string representation. The map keys will
     * always be quote-delimited.
     *
     * @param value The object to convert.
     * @return The resulting JSON string.
     * @throws SerializationException for any formatting errors in the data.
     * @see #toString(Object, boolean)
     */
    public static String toString(final Object value) throws SerializationException {
        return toString(value, false);
    }

    /**
     * Converts a object to a JSON string representation.
     *
     * @param value The object to convert.
     * @param alwaysDelimitMapKeys A flag indicating whether or not map keys will
     * always be quote-delimited.
     * @return The resulting JSON string.
     * @throws SerializationException for any formatting errors in the data.
     */
    public static String toString(final Object value, final boolean alwaysDelimitMapKeys)
        throws SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();
        jsonSerializer.setAlwaysDelimitMapKeys(alwaysDelimitMapKeys);

        StringWriter writer = new StringWriter();

        try {
            jsonSerializer.writeObject(value, writer);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }

        return writer.toString();
    }

    public ListenerList<JSONSerializerListener> getJSONSerializerListeners() {
        if (jsonSerializerListeners == null) {
            jsonSerializerListeners = new JSONSerializerListener.Listeners();
        }

        return jsonSerializerListeners;
    }
}
