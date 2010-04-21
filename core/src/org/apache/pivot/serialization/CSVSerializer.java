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
import java.nio.charset.Charset;
import java.util.NoSuchElementException;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.io.EchoReader;
import org.apache.pivot.io.EchoWriter;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a comma-separated value (CSV) file.
 * <p>
 * TODO Add "firstLineContainsKeys" flag.
 */
public class CSVSerializer implements Serializer<List<?>> {
    /**
     * Class representing the serializers key sequence.
     */
    public class KeySequence implements Sequence<String> {
        @Override
        public int add(String item) {
            return keys.add(item);
        }

        @Override
        public void insert(String item, int index) {
            keys.insert(item, index);
        }

        @Override
        public String update(int index, String item) {
            return keys.update(index, item);
        }

        @Override
        public int remove(String item) {
            return keys.remove(item);
        }

        @Override
        public Sequence<String> remove(int index, int count) {
            return keys.remove(index, count);
        }

        @Override
        public String get(int index) {
            return keys.get(index);
        }

        @Override
        public int indexOf(String item) {
            return keys.indexOf(item);
        }

        @Override
        public int getLength() {
            return keys.getLength();
        }
    }

    /**
     * Allows a caller to retrieve the contents of a CSV stream iteratively.
     */
    public class StreamIterator {
        private Reader reader;

        private StreamIterator(Reader reader) throws IOException {
            this.reader = reader;

            // Move to the first character
            c = reader.read();
        }

        public boolean hasNext() {
            return (c != -1);
        }

        public Object next() throws IOException, SerializationException {
            if (c == -1) {
                throw new NoSuchElementException();
            }

            Object item = readItem(reader);
            if (item != null) {
                // Move to next line
                while (c != -1
                    && (c == '\r' || c == '\n')) {
                    c = reader.read();
                }
            }

            return item;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private Charset charset;

    int c = -1;
    private ArrayList<String> keys = new ArrayList<String>();
    private KeySequence keySequence = new KeySequence();
    private Class<?> itemClass = HashMap.class;
    private boolean verbose = false;

    private LineNumberReader lineNumberReader = null;

    public static final String DEFAULT_CHARSET_NAME = "ISO-8859-1";
    public static final String MIME_TYPE = "text/csv";
    public static final int BUFFER_SIZE = 2048;

    public CSVSerializer() {
        this(Charset.forName(DEFAULT_CHARSET_NAME));
    }

    public CSVSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
    }

    public Charset getCharset() {
        return charset;
    }

    /**
     * Returns a sequence representing the fields that will be read or written
     * by this serializer.
     */
    public KeySequence getKeys() {
        return keySequence;
    }

    /**
     * Returns the item class that will be instantiated by the serializer during
     * a read operation.
     */
    public Class<?> getItemClass() {
        return itemClass;
    }

    /**
     * Sets the item class that will be instantiated by the serializer during
     * a read operation. The class must implement the {@link Dictionary}
     * interface.
     */
    public void setItemClass(Class<?> itemClass) {
        if (itemClass == null) {
            throw new IllegalArgumentException("itemClass is null.");
        }

        this.itemClass = itemClass;
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
     */
    public List<?> readObject(Reader reader)
        throws IOException, SerializationException {
        if (reader == null) {
            throw new IllegalArgumentException("reader is null.");
        }

        ArrayList<Object> items = new ArrayList<Object>();

        // Move to the first character
        c = reader.read();

        lineNumberReader = new LineNumberReader(reader);

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

        lineNumberReader = null;

        return items;
    }

    /**
     * Reads values from a comma-separated value stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @see #getStreamIterator(Reader)
     */
    public StreamIterator getStreamIterator(InputStream inputStream) throws IOException {
        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset),
            BUFFER_SIZE);
        return getStreamIterator(reader);
    }

    /**
     * Reads values from a comma-separated value stream.
     *
     * @param reader
     * The reader from which data will be read.
     *
     * @return
     * A stream iterator on the data read from the CSV file. The list items are
     * instances of Dictionary<String, Object> populated by mapping columns in
     * the CSV file to keys in the key sequence.
     */
    public StreamIterator getStreamIterator(Reader reader) throws IOException {
        return new StreamIterator(reader);
    }

    @SuppressWarnings("unchecked")
    private Object readItem(Reader reader)
        throws IOException, SerializationException {
        Object item = null;

        if (c != -1) {
            // Instantiate the item
            Dictionary<String, Object> itemDictionary;

            try {
                item = itemClass.newInstance();

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

                itemDictionary.put(key, value);
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

            // Move to the next character after ','
            c = reader.read();
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
}
