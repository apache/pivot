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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.nio.charset.Charset;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.EchoReader;
import org.apache.pivot.io.EchoWriter;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a JavaScript Object Notation (JSON) file.
 */
public class JSONSerializer implements Serializer<Object> {
    private static class JSONSerializerListenerList extends ListenerList<JSONSerializerListener>
        implements JSONSerializerListener {
        @Override
        public void beginDictionary(JSONSerializer jsonSerializer, Dictionary<String, ?> value) {
            for (JSONSerializerListener listener : this) {
                listener.beginDictionary(jsonSerializer, value);
            }
        }

        @Override
        public void endDictionary(JSONSerializer jsonSerializer) {
            for (JSONSerializerListener listener : this) {
                listener.endDictionary(jsonSerializer);
            }
        }

        @Override
        public void readKey(JSONSerializer jsonSerializer, String key) {
            for (JSONSerializerListener listener : this) {
                listener.readKey(jsonSerializer, key);
            }
        }

        @Override
        public void beginSequence(JSONSerializer jsonSerializer, Sequence<?> value) {
            for (JSONSerializerListener listener : this) {
                listener.beginSequence(jsonSerializer, value);
            }
        }

        @Override
        public void endSequence(JSONSerializer jsonSerializer) {
            for (JSONSerializerListener listener : this) {
                listener.endSequence(jsonSerializer);
            }
        }

        @Override
        public void readString(JSONSerializer jsonSerializer, String value) {
            for (JSONSerializerListener listener : this) {
                listener.readString(jsonSerializer, value);
            }
        }

        @Override
        public void readNumber(JSONSerializer jsonSerializer, Number value) {
            for (JSONSerializerListener listener : this) {
                listener.readNumber(jsonSerializer, value);
            }
        }

        @Override
        public void readBoolean(JSONSerializer jsonSerializer, Boolean value) {
            for (JSONSerializerListener listener : this) {
                listener.readBoolean(jsonSerializer, value);
            }
        }

        @Override
        public void readNull(JSONSerializer jsonSerializer) {
            for (JSONSerializerListener listener : this) {
                listener.readNull(jsonSerializer);
            }
        }
    }

    private Charset charset;
    private Type type;

    private boolean alwaysDelimitMapKeys = false;
    private boolean verbose = false;

    private int c = -1;

    private JSONSerializerListenerList jsonSerializerListeners = null;

    public static final String DEFAULT_CHARSET_NAME = "UTF-8";
    public static final Type DEFAULT_TYPE = Object.class;

    public static final String JSON_EXTENSION = "json";
    public static final String MIME_TYPE = "application/json";
    public static final int BUFFER_SIZE = 2048;

    public JSONSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME), DEFAULT_TYPE);
    }

    public JSONSerializer(Charset charset) {
        this(charset, DEFAULT_TYPE);
    }

    public JSONSerializer(Type type) {
        this(Charset.forName(DEFAULT_CHARSET_NAME), type);
    }

    public JSONSerializer(Charset charset, Type type) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        if (type == null) {
            throw new IllegalArgumentException("type is null.");
        }

        this.charset = charset;
        this.type = type;
    }

    /**
     * Returns the character set used to encode/decode the JSON data.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns the type of the object that will be returned by {@link #readObject(Reader)}.
     */
    public Type getType() {
        return type;
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
     * Returns the serializer's verbosity flag.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the serializer's verbosity flag. When verbosity is enabled, all data read or
     * written will be echoed to the console.
     *
     * @param verbose
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Reads data from a JSON stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @see #readObject(Reader)
     */
    @SuppressWarnings("resource")
    @Override
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null.");
        }

        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), BUFFER_SIZE);
        if (verbose) {
            reader = new EchoReader(reader);
        }

        return readObject(reader);
    }

    /**
     * Reads data from a JSON stream.
     *
     * @param reader
     * The reader from which data will be read.
     *
     * @return
     * One of the following types, depending on the content of the stream
     * and the value of {@link #getType()}:
     *
     * <ul>
     * <li>pivot.collections.Dictionary</li>
     * <li>pivot.collections.Sequence</li>
     * <li>java.lang.String</li>
     * <li>java.lang.Number</li>
     * <li>java.lang.Boolean</li>
     * <li><tt>null</tt></li>
     * <li>A JavaBean object</li>
     * </ul>
     */
    public Object readObject(Reader reader)
        throws IOException, SerializationException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null.");
        }

        // Move to the first character
        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        c = lineNumberReader.read();

        // Ignore BOM (if present)
        if (c == 0xFEFF) {
            c = lineNumberReader.read();
        }

        // Read the root value
        Object object;
        try {
            object = readValue(lineNumberReader, type);
        } catch (SerializationException exception) {
            System.err.println("An error occurred while processing input at line number "
                + (lineNumberReader.getLineNumber() + 1));

            throw exception;
        }

        return object;
    }

    private Object readValue(Reader reader, Type typeArgument)
        throws IOException, SerializationException {
        Object object = null;

        skipWhitespaceAndComments(reader);

        if (c == -1) {
            throw new SerializationException("Unexpected end of input stream.");
        }

        if (c == 'n') {
            object = readNullValue(reader);
        } else if (c == '"' || c == '\'') {
            object = readStringValue(reader, typeArgument);
        } else if (c == '+' || c == '-' || Character.isDigit(c)) {
            object = readNumberValue(reader, typeArgument);
        } else if (c == 't' || c == 'f') {
            object = readBooleanValue(reader, typeArgument);
        } else if (c == '[') {
            object = readListValue(reader, typeArgument);
        } else if (c == '{') {
            object = readMapValue(reader, typeArgument);
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

    private Object readNullValue(Reader reader)
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

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readNull(this);
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

    private Object readStringValue(Reader reader, Type typeArgument)
        throws IOException, SerializationException {
        if (!(typeArgument instanceof Class<?>)) {
            throw new SerializationException("Cannot convert string to " + typeArgument + ".");
        }

        String string = readString(reader);

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readString(this, string);
        }

        return BeanAdapter.coerce(string, (Class<?>)typeArgument);
    }

    private Object readNumberValue(Reader reader, Type typeArgument)
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

        while (c != -1 && (Character.isDigit(c) || c == '.'
            || c == 'e' || c == 'E' || c == '-')) {
            stringBuilder.append((char)c);
            integer &= !(c == '.');
            c = reader.read();
        }

        if (integer) {
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

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.readNumber(this, number);
        }

        return BeanAdapter.coerce(number, (Class<?>)typeArgument);
    }

    private Object readBooleanValue(Reader reader, Type typeArgument)
        throws IOException, SerializationException {
        if (!(typeArgument instanceof Class<?>)) {
            throw new SerializationException("Cannot convert boolean to " + typeArgument + ".");
        }

        String text = (c == 't') ? "true" : "false";
        int n = text.length();
        int i = 0;

        while (c != -1 && i < n) {
            if (text.charAt(i) != c) {
                throw new SerializationException("Unexpected character in input stream.");
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

        return BeanAdapter.coerce(value, (Class<?>)typeArgument);
    }

    @SuppressWarnings("unchecked")
    private Object readListValue(Reader reader, Type typeArgument)
        throws IOException, SerializationException {
        Sequence<Object> sequence = null;
        Type itemType = null;

        if (typeArgument == Object.class) {
            // Return the default sequence and item types
            sequence = new ArrayList<Object>();
            itemType = Object.class;
        } else {
            // Determine the item type from generic parameters
            Type parentType = typeArgument;
            while (parentType != null) {
                if (parentType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType)parentType;
                    Class<?> rawType = (Class<?>)parameterizedType.getRawType();

                    if (Sequence.class.isAssignableFrom(rawType)) {
                        itemType = parameterizedType.getActualTypeArguments()[0];
                    }

                    break;
                }

                Class<?> classType = (Class<?>)parentType;
                Type[] genericInterfaces = classType.getGenericInterfaces();

                for (int i = 0; i < genericInterfaces.length; i++) {
                    Type genericInterface = genericInterfaces[i];

                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType)genericInterface;
                        Class<?> interfaceType = (Class<?>)parameterizedType.getRawType();

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
                ParameterizedType parameterizedType = (ParameterizedType)typeArgument;
                sequenceType = (Class<?>)parameterizedType.getRawType();
            } else {
                sequenceType = (Class<?>)typeArgument;
            }

            try {
                sequence = (Sequence<Object>)sequenceType.newInstance();
            } catch (InstantiationException exception) {
                throw new RuntimeException(exception);
            } catch (IllegalAccessException exception) {
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
            sequence.add(readValue(reader, itemType));
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

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.endSequence(this);
        }

        return sequence;
    }

    @SuppressWarnings("unchecked")
    private Object readMapValue(Reader reader, Type typeArgument)
        throws IOException, SerializationException {
        Dictionary<String, Object> dictionary = null;
        Type valueType = null;

        if (typeArgument == Object.class) {
            // Return the default dictionary and value types
            dictionary = new HashMap<String, Object>();
            valueType = Object.class;
        } else {
            // Determine the value type from generic parameters
            Type parentType = typeArgument;
            while (parentType != null) {
                if (parentType instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType)parentType;
                    Class<?> rawType = (Class<?>)parameterizedType.getRawType();

                    if (Dictionary.class.isAssignableFrom(rawType)) {
                        valueType = parameterizedType.getActualTypeArguments()[1];
                    }

                    break;
                }

                Class<?> classType = (Class<?>)parentType;
                Type[] genericInterfaces = classType.getGenericInterfaces();

                for (int i = 0; i < genericInterfaces.length; i++) {
                    Type genericInterface = genericInterfaces[i];

                    if (genericInterface instanceof ParameterizedType) {
                        ParameterizedType parameterizedType = (ParameterizedType)genericInterface;
                        Class<?> interfaceType = (Class<?>)parameterizedType.getRawType();

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
                Class<?> beanType = (Class<?>)typeArgument;

                try {
                    dictionary = new BeanAdapter(beanType.newInstance());
                } catch (InstantiationException exception) {
                    throw new RuntimeException(exception);
                } catch (IllegalAccessException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                Class<?> dictionaryType;
                if (typeArgument instanceof ParameterizedType) {
                    ParameterizedType parameterizedType = (ParameterizedType)typeArgument;
                    dictionaryType = (Class<?>)parameterizedType.getRawType();
                } else {
                    dictionaryType = (Class<?>)typeArgument;
                }

                try {
                    dictionary = (Dictionary<String, Object>)dictionaryType.newInstance();
                } catch (InstantiationException exception) {
                    throw new RuntimeException(exception);
                } catch (IllegalAccessException exception) {
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

                while (c != -1
                    && c != ':' && !Character.isWhitespace(c)) {
                    if (!Character.isJavaIdentifierPart(c)) {
                        throw new SerializationException("Illegal identifier character.");
                    }

                    keyBuilder.append((char)c);
                    c = reader.read();
                }

                if (c == -1) {
                    throw new SerializationException("Unexpected end of input stream.");
                }

                key = keyBuilder.toString();
            }

            if (key == null
                || key.length() == 0) {
                throw new SerializationException("\"" + key + "\" is not a valid key.");
            }

            // Notify listeners
            if (jsonSerializerListeners != null) {
                jsonSerializerListeners.readKey(this, key);
            }

            skipWhitespaceAndComments(reader);

            if (c != ':') {
                throw new SerializationException("Unexpected character in input stream.");
            }

            // Move to the first character after ':'
            c = reader.read();

            if (valueType == null) {
                // The map is a bean instance; get the generic type of the property
                Type genericValueType = ((BeanAdapter)dictionary).getGenericType(key);

                if (genericValueType != null) {
                    // Set the value in the bean
                    dictionary.put(key, readValue(reader, genericValueType));
                } else {
                    // The property does not exist; ignore this value
                    readValue(reader, Object.class);
                }
            } else {
                dictionary.put(key, readValue(reader, valueType));
            }

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

        // Notify the listeners
        if (jsonSerializerListeners != null) {
            jsonSerializerListeners.endDictionary(this);
        }

        return (dictionary instanceof BeanAdapter) ? ((BeanAdapter)dictionary).getBean() : dictionary;
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
    @SuppressWarnings("resource")
    @Override
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset), BUFFER_SIZE);
        if (verbose) {
            writer = new EchoWriter(writer);
        }

        writeObject(object, writer);
    }

    /**
     * Writes data to a JSON stream.
     *
     * @param object
     * The object to serialize. Must be one of the following types:
     *
     * <ul>
     * <li>pivot.collections.Map</li>
     * <li>pivot.collections.List</li>
     * <li>java.lang.String</li>
     * <li>java.lang.Number</li>
     * <li>java.lang.Boolean</li>
     * <li><tt>null</tt></li>
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
                char ci = string.charAt(i);

                switch(ci) {
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
                        stringBuilder.append("\\" + ci);
                        break;
                    }

                    default: {
                        if (charset.name().startsWith("UTF")
                            || ci <= 0xFF) {
                            stringBuilder.append(ci);
                        } else {
                            stringBuilder.append("\\u");
                            stringBuilder.append(String.format("%04x", (short)ci));
                        }
                    }
                }

            }

            writer.append("\"" + stringBuilder.toString() + "\"");
        } else if (object instanceof Number) {
            Number number = (Number)object;

            if (number instanceof Float) {
                Float f = (Float)number;
                if (f.isNaN()
                    || f.isInfinite()) {
                    throw new SerializationException(number + " is not a valid value.");
                }
            } else if (number instanceof Double) {
                Double d = (Double)number;
                if (d.isNaN()
                    || d.isInfinite()) {
                    throw new SerializationException(number + " is not a valid value.");
                }
            }

            writer.append(number.toString());
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
        } else {
            Map<String, Object> map;
            if (object instanceof Map<?, ?>) {
                map = (Map<String, Object>)object;
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
    public String getMIMEType(Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
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
     * Converts a object to a JSON string representation. The map keys will always
     * be quote-delimited.
     *
     * @param value
     * The object to convert.
     *
     * @return
     * The resulting JSON string.
     *
     * @see #toString(Object, boolean)
     */
    public static String toString(Object value) throws SerializationException {
        return toString(value, false);
    }

    /**
     * Converts a object to a JSON string representation.
     *
     * @param value
     * The object to convert.
     *
     * @param alwaysDelimitMapKeys
     * A flag indicating whether or not map keys will always be quote-delimited.
     *
     * @return
     * The resulting JSON string.
     */
    public static String toString(Object value, boolean alwaysDelimitMapKeys) throws SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();
        jsonSerializer.setAlwaysDelimitMapKeys(alwaysDelimitMapKeys);

        StringWriter writer = new StringWriter();

        try {
            jsonSerializer.writeObject(value, writer);
        } catch(IOException exception) {
            throw new RuntimeException(exception);
        }

        return writer.toString();
    }

    public ListenerList<JSONSerializerListener> getJSONSerializerListeners() {
        if (jsonSerializerListeners == null) {
            jsonSerializerListeners = new JSONSerializerListenerList();
        }

        return jsonSerializerListeners;
    }
}
