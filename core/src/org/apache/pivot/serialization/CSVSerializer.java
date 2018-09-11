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
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.EchoReader;
import org.apache.pivot.io.EchoWriter;
import org.apache.pivot.util.Constants;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Serializer} interface that reads data from and
 * writes data to a comma-separated value (CSV) file.
 */
public class CSVSerializer implements Serializer<List<?>> {
    private Charset charset;
    private Type itemType;

    private List<String> keys = new ArrayList<>();

    private boolean writeKeys = false;
    private boolean verbose = false;

    private int c = -1;

    private CSVSerializerListener.Listeners csvSerializerListeners = null;

    public static final Charset DEFAULT_CHARSET = StandardCharsets.ISO_8859_1;
    public static final Type DEFAULT_ITEM_TYPE = HashMap.class;

    public static final String CSV_EXTENSION = "csv";
    public static final String MIME_TYPE = "text/csv";

    public CSVSerializer() {
        this(DEFAULT_CHARSET, DEFAULT_ITEM_TYPE);
    }

    public CSVSerializer(final Charset charset) {
        this(charset, DEFAULT_ITEM_TYPE);
    }

    public CSVSerializer(final Type itemType) {
        this(DEFAULT_CHARSET, itemType);
    }

    public CSVSerializer(final Charset charset, final Type itemType) {
        Utils.checkNull(charset, "charset");
        Utils.checkNull(itemType, "itemType");

        this.charset = charset;
        this.itemType = itemType;
    }

    /**
     * Returns the character set used to encode/decode the CSV data.
     * @return The current character set.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns the type of the item that will be instantiated by the serializer
     * during a read operation.
     * @return The type of the item to be returned by the serializer.
     */
    public Type getItemType() {
        return itemType;
    }

    /**
     * Returns the keys that will be read or written by this serializer.
     * @return The sequence of read/write keys.
     */
    public Sequence<String> getKeys() {
        return keys;
    }

    /**
     * Sets the keys that will be read or written by this serializer.
     *
     * @param keys The keys to be read/written.
     * @throws IllegalArgumentException for {@code null} input.
     */
    public void setKeys(final Sequence<String> keys) {
        Utils.checkNull(keys, "keys");

        this.keys = new ArrayList<>(keys);
    }

    /**
     * Sets the keys that will be read or written by this serializer.
     *
     * @param keys The list of keys to be read/written.
     * @throws IllegalArgumentException for {@code null} input.
     */
    public void setKeys(final String... keys) {
        Utils.checkNull(keys, "keys");

        setKeys(new ArrayAdapter<>(keys));
    }

    /**
     * Returns the serializer's write keys flag.
     * @return <tt>true</tt> if keys will be written, <tt>false</tt> otherwise.
     * @see #setWriteKeys(boolean)
     */
    public boolean getWriteKeys() {
        return writeKeys;
    }

    /**
     * Sets the serializer's write keys flag.
     *
     * @param writeKeys If <tt>true</tt>, the first line of the output will
     * contain the keys. Otherwise, the first line will contain the first line
     * of data.
     */
    public void setWriteKeys(final boolean writeKeys) {
        this.writeKeys = writeKeys;
    }

    /**
     * Returns the serializer's verbosity flag.
     * @return <tt>true</tt> if the serializer is echoing input, <tt>false</tt>
     * if not.
     */
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets the serializer's verbosity flag. When verbosity is enabled, all data
     * read or written will be echoed to the console.
     *
     * @param verbose Whether or not to echo the input.
     */
    public void setVerbose(final boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Reads values from a comma-separated value stream.
     *
     * @param inputStream The input stream from which data will be read.
     * @return The list of values read from the stream.
     * @throws IOException for any errors during reading.
     * @throws SerializationException for any formatting errors with the data.
     * @throws IllegalArgumentException for {@code null} input stream.
     * @see #readObject(Reader)
     */
    @SuppressWarnings("resource")
    @Override
    public List<?> readObject(final InputStream inputStream) throws IOException, SerializationException {
        Utils.checkNull(inputStream, "inputStream");

        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset), Constants.BUFFER_SIZE);
        if (verbose) {
            reader = new EchoReader(reader);
        }

        return readObject(reader);
    }

    /**
     * Reads values from a comma-separated value stream.
     *
     * @param reader The reader from which data will be read.
     * @return A list containing the data read from the CSV file. The list items
     * are instances of <tt>Dictionary&lt;String, Object&gt;</tt> populated by mapping columns
     * in the CSV file to keys in the key sequence. <p> If no keys have been
     * specified when this method is called, they are assumed to be defined in
     * the first line of the file.
     * @throws IOException for any errors during reading.
     * @throws SerializationException for any formatting errors with the data.
     * @throws IllegalArgumentException for {@code null} input reader.
     */
    public List<?> readObject(final Reader reader) throws IOException, SerializationException {
        Utils.checkNull(reader, "reader");

        LineNumberReader lineNumberReader = new LineNumberReader(reader);
        lineNumberReader.setLineNumber(1);

        if (keys.getLength() == 0) {
            // Read keys from first line
            String line = lineNumberReader.readLine();
            if (line == null) {
                throw new SerializationException("Could not read keys from input.");
            }

            String[] keysOnLine = line.split(",");
            this.keys = new ArrayList<>(keysOnLine.length);

            for (String key : keysOnLine) {
                this.keys.add(key.trim());
            }
        }

        // Create the list and notify the listeners
        List<Object> items = new ArrayList<>();

        if (csvSerializerListeners != null) {
            csvSerializerListeners.beginList(this, items);
        }

        // Move to the first character
        c = lineNumberReader.read();

        // Ignore Byte Order Mark (if present)
        if (c == Constants.BYTE_ORDER_MARK) {
            c = lineNumberReader.read();
        }

        try {
            while (c != -1) {
                Object item = readItem(lineNumberReader);
                while (item != null) {
                    items.add(item);

                    // Move to next line
                    while (c != -1 && (c == '\r' || c == '\n')) {
                        c = lineNumberReader.read();
                    }

                    // Read the next item
                    item = readItem(lineNumberReader);
                }
            }
        } catch (SerializationException exception) {
            System.err.println("An error occurred while processing input at line number "
                + lineNumberReader.getLineNumber());

            throw exception;
        }

        // Notify the listeners
        if (csvSerializerListeners != null) {
            csvSerializerListeners.endList(this);
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    private Object readItem(final Reader reader) throws IOException, SerializationException {
        Object item = null;

        if (c != -1) {
            // Instantiate the item
            Dictionary<String, Object> itemDictionary;

            try {
                if (itemType instanceof ParameterizedType) {
                    ParameterizedType parameterizedItemType = (ParameterizedType) itemType;
                    Class<?> rawItemType = (Class<?>) parameterizedItemType.getRawType();
                    item = rawItemType.getDeclaredConstructor().newInstance();
                } else {
                    Class<?> classItemType = (Class<?>) itemType;
                    item = classItemType.getDeclaredConstructor().newInstance();
                }

                if (item instanceof Dictionary<?, ?>) {
                    itemDictionary = (Dictionary<String, Object>) item;
                } else {
                    itemDictionary = new BeanAdapter(item);
                }
            } catch (IllegalAccessException | InstantiationException | NoSuchMethodException
                   | InvocationTargetException exception) {
                throw new SerializationException(exception);
            }

            // Add values to the item
            for (int i = 0, n = keys.getLength(); i < n; i++) {
                String key = keys.get(i);
                String value = readValue(reader);
                if (value == null) {
                    throw new SerializationException("Error reading value for " + key
                        + " from input stream.");
                }

                if (c == '\r' || c == '\n') {
                    if (i < n - 1) {
                        throw new SerializationException("Line data is incomplete.");
                    }

                    // Move to next char; if LF, move again
                    c = reader.read();

                    if (c == '\n') {
                        c = reader.read();
                    }
                }

                itemDictionary.put(key, value);
            }

            // Notify the listeners
            if (csvSerializerListeners != null) {
                csvSerializerListeners.readItem(this, item);
            }
        }

        return item;
    }

    private String readValue(final Reader reader) throws IOException, SerializationException {
        String value = null;

        // Read the next value from this line, returning null if there are
        // no more values on the line
        if (c != -1 && (c != '\r' && c != '\n')) {
            // Read the value
            StringBuilder valueBuilder = new StringBuilder();

            // Values may be bounded in quotes; the double-quote character is
            // escaped by two successive occurrences
            boolean quoted = (c == '"');
            if (quoted) {
                c = reader.read();
            }

            while (c != -1 && (quoted || (c != ',' && c != '\r' && c != '\n'))) {
                if (c == '"') {
                    if (!quoted) {
                        throw new SerializationException("Dangling quote.");
                    }

                    c = reader.read();

                    if (c != '"' && (c != ',' && c != '\r' && c != '\n' && c != -1)) {
                        throw new SerializationException("Prematurely terminated quote.");
                    }

                    quoted &= (c == '"');
                }

                if (c != -1 && (quoted || (c != ',' && c != '\r' && c != '\n'))) {
                    valueBuilder.append((char) c);
                    c = reader.read();
                }
            }

            if (quoted) {
                throw new SerializationException("Unterminated string.");
            }

            value = valueBuilder.toString();

            // Move to the next character after ',' (don't automatically advance
            // to the next line)
            if (c == ',') {
                c = reader.read();
            }
        }

        // Trim the value
        if (value != null) {
            value = value.trim();
        }

        return value;
    }

    /**
     * Writes values to a comma-separated value stream.
     *
     * @param items The list of items to write.
     * @param outputStream The output stream to which data will be written.
     * @throws IOException for any errors during writing.
     * @throws SerializationException for any formatting errors with the data.
     * @throws IllegalArgumentException for {@code null} input arguments.
     * @see #writeObject(List, Writer)
     */
    @SuppressWarnings("resource")
    @Override
    public void writeObject(final List<?> items, final OutputStream outputStream) throws IOException,
        SerializationException {
        Utils.checkNull(items, "items");
        Utils.checkNull(outputStream, "outputStream");

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset),
            Constants.BUFFER_SIZE);
        if (verbose) {
            writer = new EchoWriter(writer);
        }

        writeObject(items, writer);
    }

    /**
     * Writes values to a comma-separated value stream.
     *
     * @param items A list containing the data to write to the CSV file. List
     * items must be instances of <tt>Dictionary&lt;String, Objecti&gt;</tt>. The dictionary
     * values will be written out in the order specified by the key sequence.
     * @param writer The writer to which data will be written.
     * @throws IOException for any errors during writing.
     * @throws IllegalArgumentException for {@code null} input arguments.
     */
    @SuppressWarnings("unchecked")
    public void writeObject(final List<?> items, final Writer writer) throws IOException {
        Utils.checkNull(items, "items");
        Utils.checkNull(writer, "writer");

        if (writeKeys) {
            // Write keys as first line
            for (int i = 0, n = keys.getLength(); i < n; i++) {
                String key = keys.get(i);

                if (i > 0) {
                    writer.append(",");
                }

                writer.append(key);
            }

            writer.append("\r\n");
        }

        for (Object item : items) {
            Dictionary<String, Object> itemDictionary;
            if (item instanceof Dictionary<?, ?>) {
                itemDictionary = (Dictionary<String, Object>) item;
            } else {
                itemDictionary = new BeanAdapter(item);
            }

            for (int i = 0, n = keys.getLength(); i < n; i++) {
                String key = keys.get(i);

                if (i > 0) {
                    writer.append(",");
                }

                Object value = itemDictionary.get(key);

                if (value != null) {
                    String string = value.toString();

                    if (string.indexOf(',') >= 0 || string.indexOf('"') >= 0
                        || string.indexOf('\r') >= 0 || string.indexOf('\n') >= 0) {
                        writer.append('"');

                        if (string.indexOf('"') == -1) {
                            writer.append(string);
                        } else {
                            writer.append(string.replace("\"", "\"\""));
                        }

                        writer.append('"');
                    } else {
                        writer.append(string);
                    }
                }
            }

            writer.append("\r\n");
        }

        writer.flush();
    }

    @Override
    public String getMIMEType(final List<?> objects) {
        return MIME_TYPE + "; charset=" + charset.name();
    }

    public ListenerList<CSVSerializerListener> getCSVSerializerListeners() {
        if (csvSerializerListeners == null) {
            csvSerializerListeners = new CSVSerializerListener.Listeners();
        }

        return csvSerializerListeners;
    }
}
