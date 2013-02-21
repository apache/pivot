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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.EchoReader;
import org.apache.pivot.io.EchoWriter;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a comma-separated value (CSV) file.
 */
public class CSVSerializer implements Serializer<List<?>> {
    private static class CSVSerializerListenerList extends ListenerList<CSVSerializerListener>
        implements CSVSerializerListener {
        @Override
        public void beginList(CSVSerializer csvSerializer, List<?> list) {
            for (CSVSerializerListener listener : this) {
                listener.beginList(csvSerializer, list);
            }
        }

        @Override
        public void endList(CSVSerializer csvSerializer) {
            for (CSVSerializerListener listener : this) {
                listener.endList(csvSerializer);
            }
        }

        @Override
        public void readItem(CSVSerializer csvSerializer, Object item) {
            for (CSVSerializerListener listener : this) {
                listener.readItem(csvSerializer, item);
            }
        }
    }

    private Charset charset;
    private Type itemType;

    private ArrayList<String> keys = new ArrayList<String>();

    private boolean writeKeys = false;
    private boolean verbose = false;

    private int c = -1;

    private CSVSerializerListenerList csvSerializerListeners = null;

    public static final String DEFAULT_CHARSET_NAME = "ISO-8859-1";
    public static final Type DEFAULT_ITEM_TYPE = HashMap.class;

    public static final String CSV_EXTENSION = "csv";
    public static final String MIME_TYPE = "text/csv";
    public static final int BUFFER_SIZE = 2048;

    public CSVSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME), DEFAULT_ITEM_TYPE);
    }

    public CSVSerializer(Charset charset) {
        this(charset, DEFAULT_ITEM_TYPE);
    }

    public CSVSerializer(Type itemType) {
        this(Charset.forName(DEFAULT_CHARSET_NAME), itemType);
    }

    public CSVSerializer(Charset charset, Type itemType) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        if (itemType == null) {
            throw new IllegalArgumentException("itemType is null.");
        }

        this.charset = charset;
        this.itemType = itemType;
    }

    /**
     * Returns the character set used to encode/decode the CSV data.
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns the type of the item that will be instantiated by the serializer
     * during a read operation.
     */
    public Type getItemType() {
        return itemType;
    }

    /**
     * Returns the keys that will be read or written by this serializer.
     */
    public Sequence<String> getKeys() {
        return keys;
    }

    /**
     * Sets the keys that will be read or written by this serializer.
     *
     * @param keys
     */
    public void setKeys(Sequence<String> keys) {
        if (keys == null) {
            throw new IllegalArgumentException();
        }

        this.keys = new ArrayList<String>(keys);
    }

    /**
     * Sets the keys that will be read or written by this serializer.
     *
     * @param keys
     */
    public void setKeys(String... keys) {
        if (keys == null) {
            throw new IllegalArgumentException();
        }

        setKeys(new ArrayAdapter<String>(keys));
    }

    /**
     * Returns the serializer's write keys flag.
     */
    public boolean getWriteKeys() {
        return writeKeys;
    }

    /**
     * Sets the serializer's write keys flag.
     *
     * @param writeKeys
     * If <tt>true</tt>, the first line of the output will contain the keys.
     * Otherwise, the first line will contain the first line of data.
     */
    public void setWriteKeys(boolean writeKeys) {
        this.writeKeys = writeKeys;
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
     * Reads values from a comma-separated value stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @see #readObject(Reader)
     */
    @SuppressWarnings("resource")
    @Override
    public List<?> readObject(InputStream inputStream)
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
     * Reads values from a comma-separated value stream.
     *
     * @param reader
     * The reader from which data will be read.
     *
     * @return
     * A list containing the data read from the CSV file. The list items are
     * instances of Dictionary<String, Object> populated by mapping columns in
     * the CSV file to keys in the key sequence.
     * <p>
     * If no keys have been specified when this method is called, they are assumed
     * to be defined in the first line of the file.
     */
    public List<?> readObject(Reader reader)
        throws IOException, SerializationException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null.");
        }

        LineNumberReader lineNumberReader = new LineNumberReader(reader);

        if (keys.getLength() == 0) {
            // Read keys from first line
            String line = lineNumberReader.readLine();
            if (line == null) {
                throw new SerializationException("Could not read keys from input.");
            }

            String[] keysLocal = line.split(",");
            this.keys = new ArrayList<String>(keysLocal.length);

            for (int i = 0; i < keysLocal.length; i++) {
                String key = keysLocal[i];
                this.keys.add(key.trim());
            }
        }

        // Create the list and notify the listeners
        ArrayList<Object> items = new ArrayList<Object>();

        if (csvSerializerListeners != null) {
            csvSerializerListeners.beginList(this, items);
        }

        // Move to the first character
        c = lineNumberReader.read();

        // Ignore BOM (if present)
        if (c == 0xFEFF) {
            c = lineNumberReader.read();
        }

        try {
            while (c != -1) {
                Object item = readItem(lineNumberReader);
                while (item != null) {
                    items.add(item);

                    // Move to next line
                    while (c != -1
                        && (c == '\r' || c == '\n')) {
                        c = lineNumberReader.read();
                    }

                    // Read the next item
                    item = readItem(lineNumberReader);
                }
            }
        } catch (SerializationException exception) {
            System.err.println("An error occurred while processing input at line number "
                + (lineNumberReader.getLineNumber() + 1));

            throw exception;
        }

        // Notify the listeners
        if (csvSerializerListeners != null) {
            csvSerializerListeners.endList(this);
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    private Object readItem(Reader reader)
        throws IOException, SerializationException {
        Object item = null;

        if (c != -1) {
            // Instantiate the item
            Dictionary<String, Object> itemDictionary;

            try {
                if (itemType instanceof ParameterizedType) {
                    ParameterizedType parameterizedItemType = (ParameterizedType)itemType;
                    Class<?> rawItemType = (Class<?>)parameterizedItemType.getRawType();
                    item = rawItemType.newInstance();
                } else {
                    Class<?> classItemType = (Class<?>)itemType;
                    item = classItemType.newInstance();
                }

                if (item instanceof Dictionary<?, ?>) {
                    itemDictionary = (Dictionary<String, Object>)item;
                } else {
                    itemDictionary = new BeanAdapter(item);
                }
            } catch(IllegalAccessException exception) {
                throw new SerializationException(exception);
            } catch(InstantiationException exception) {
                throw new SerializationException(exception);
            }

            // Add values to the item
            for (int i = 0, n = keys.getLength(); i < n; i++) {
                String key = keys.get(i);
                String value = readValue(reader);
                if (value == null) {
                    throw new SerializationException("Error reading value for "
                        + key + " from input stream.");
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

    private String readValue(Reader reader)
        throws IOException, SerializationException {
        String value = null;

        // Read the next value from this line, returning null if there are
        // no more values on the line
        if (c != -1
            && (c != '\r' && c != '\n')) {
            // Read the value
            StringBuilder valueBuilder = new StringBuilder();

            // Values may be bounded in quotes; the double-quote character is
            // escaped by two successive occurrences
            boolean quoted = (c == '"');
            if (quoted) {
                c = reader.read();
            }

            while (c != -1
                && (quoted || (c != ',' && c != '\r' && c != '\n'))) {
                if (c == '"') {
                    if (!quoted) {
                        throw new SerializationException("Dangling quote.");
                    }

                    c = reader.read();

                    if (c != '"'
                        && (c != ',' && c != '\r' && c != '\n' && c != -1)) {
                        throw new SerializationException("Prematurely terminated quote.");
                    }

                    quoted &= (c == '"');
                }

                if (c != -1
                    && (quoted || (c != ',' && c != '\r' && c != '\n'))) {
                    valueBuilder.append((char)c);
                    c = reader.read();
                }
            }

            if (quoted) {
                throw new SerializationException("Unterminated string.");
            }

            value = valueBuilder.toString();

            // Move to the next character after ',' (don't automatically advance to
            // the next line)
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
     * @param items
     *
     * @param outputStream
     * The output stream to which data will be written.
     *
     * @see #writeObject(List, Writer)
     */
    @SuppressWarnings("resource")
    @Override
    public void writeObject(List<?> items, OutputStream outputStream)
        throws IOException, SerializationException {
        if (items == null) {
            throw new IllegalArgumentException("items is null.");
        }

        if (outputStream == null) {
            throw new IllegalArgumentException("outputStream is null.");
        }

        Writer writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset), BUFFER_SIZE);
        if (verbose) {
            writer = new EchoWriter(writer);
        }

        writeObject(items, writer);
    }

    /**
     * Writes values to a comma-separated value stream.
     *
     * @param items
     * A list containing the data to write to the CSV
     * file. List items must be instances of Dictionary<String, Object>. The
     * dictionary values will be written out in the order specified by the
     * key sequence.
     *
     * @param writer
     * The writer to which data will be written.
     */
    @SuppressWarnings("unchecked")
    public void writeObject(List<?> items, Writer writer) throws IOException {
        if (items == null) {
            throw new IllegalArgumentException("items is null.");
        }

        if (writer == null) {
            throw new IllegalArgumentException("writer is null.");
        }

        if (writeKeys) {
            // Write keys as first line
            for (int i = 0, n = keys.getLength(); i < n; i++) {
                String key = keys.get(i);

                if (i > 0) {
                    writer.append(",");
                }

                writer.append(key);
            }
        }

        for (Object item : items) {
            Dictionary<String, Object> itemDictionary;
            if (item instanceof Dictionary<?, ?>) {
                itemDictionary = (Dictionary<String, Object>)item;
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

                    if (string.indexOf(',') >= 0
                        || string.indexOf('"') >= 0
                        || string.indexOf('\r') >= 0
                        || string.indexOf('\n') >= 0) {
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
    public String getMIMEType(List<?> objects) {
        return MIME_TYPE + "; charset=" + charset.name();
    }

    public ListenerList<CSVSerializerListener> getCSVSerializerListeners() {
        if (csvSerializerListeners == null) {
            csvSerializerListeners = new CSVSerializerListenerList();
        }

        return csvSerializerListeners;
    }
}
