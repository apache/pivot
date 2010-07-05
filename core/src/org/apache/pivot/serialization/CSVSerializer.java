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
 */
public class CSVSerializer implements Serializer<List<?>> {
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
    private Class<?> itemClass = HashMap.class;
    private boolean writeKeys = false;
    private boolean verbose = false;

    public static final String DEFAULT_CHARSET_NAME = "ISO-8859-1";
    public static final String CSV_EXTENSION = "csv";
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
    public Sequence<String> getKeys() {
        return keys;
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

            String[] keys = line.split(",");
            this.keys = new ArrayList<String>(keys.length);

            for (int i = 0; i < keys.length; i++) {
                String key = keys[i];
                this.keys.add(key.trim());
            }
        }

        ArrayList<Object> items = new ArrayList<Object>();

        // Move to the first character
        c = lineNumberReader.read();

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
}
