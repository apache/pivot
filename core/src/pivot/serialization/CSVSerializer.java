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
import java.io.Writer;
import java.nio.charset.Charset;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Sequence;

/**
 * Implementation of the {@link Serializer} interface that reads data from
 * and writes data to a comma-separated value (CSV) file.
 * <p>
 * TODO Allow caller to specify a class that does not implement Dictionary.
 * We can use BeanDictionary to allow the caller to instantiate and populate
 * arbitrary types.
 * <p>
 * TODO Add "firstLineContainsKeys" flag.
 * <p>
 * TODO Add support for variable delimiters.
 *
 * @author gbrown
 */
public class CSVSerializer implements Serializer {
    /**
     * Class representing the serializers key sequence.
     */
    public class KeySequence implements Sequence<String> {
        public int add(String item) {
            return keys.add(item);
        }

        public void insert(String item, int index) {
            keys.insert(item, index);
        }

        public String update(int index, String item) {
            return keys.update(index, item);
        }

        public int remove(String item) {
            return keys.remove(item);
        }

        public Sequence<String> remove(int index, int count) {
            return keys.remove(index, count);
        }

        public String get(int index) {
            return keys.get(index);
        }

        public int indexOf(String item) {
            return keys.indexOf(item);
        }

        public int getLength() {
            return keys.getLength();
        }
    }

    private Charset charset;

    private ArrayList<String> keys = new ArrayList<String>();
    private KeySequence keySequence = new KeySequence();

    public static final String MIME_TYPE = "text/csv";
    public static final int BUFFER_SIZE = 2048;

    int c = -1;
    private Class<?> itemClass = HashMap.class;

    public CSVSerializer() {
        this(Charset.defaultCharset());
    }

    public CSVSerializer(String charsetName) {
        this(charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName));
    }

    public CSVSerializer(Charset charset) {
        if (charset == null) {
            throw new IllegalArgumentException("charset is null.");
        }

        this.charset = charset;
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
     * Reads values from a comma-separated value stream.
     *
     * @param inputStream
     * The input stream from which data will be read.
     *
     * @see #readObject(Reader)
     */
    @SuppressWarnings("unchecked")
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        Reader reader = new BufferedReader(new InputStreamReader(inputStream, charset),
            BUFFER_SIZE);
        Object object = readObject(reader);
        reader.close();

        return object;
    }

    /**
     * Reads values from a comma-separated value stream.
     *
     * @param reader
     * The reader from which data will be read.
     *
     * @return
     * An instance of List<Object> containing the data read from the CSV file.
     * The list items are instances of Dictionary<String, Object> populated by
     * mapping columns in the CSV file to keys in the key sequence.
     */
    public Object readObject(Reader reader)
        throws IOException, SerializationException {
        ArrayList<Dictionary<String, Object>> items = new ArrayList<Dictionary<String, Object>>();

        // Move to the first character
        c = reader.read();

        while (c != -1) {
            Dictionary<String, Object> item = readItem(reader);
            while (item != null) {
                items.add(item);

                // Move to next line
                while (c != -1
                    && (c == '\r' || c == '\n')) {
                    c = reader.read();
                }

                // Read the next item
                item = readItem(reader);
            }
        }

        return items;
    }

    @SuppressWarnings("unchecked")
    private Dictionary<String, Object> readItem(Reader reader)
        throws IOException, SerializationException {
        Dictionary<String, Object> item = null;

        if (c != -1) {
            // Instantiate the item
            try {
                item = (Dictionary<String, Object>)itemClass.newInstance();
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

                item.put(key, value);
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
                && (quoted || c != ',')
                && (c != '\r' && c != '\n')) {
                if (c == '"') {
                    c = reader.read();
                    quoted &= (c == '"');
                }

                if (quoted || c != ',') {
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
     * @param object
     *
     * @param outputStream
     * The output stream to which data will be written.
     *
     * @see #writeObject(Object, Writer)
     */
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(outputStream, charset),
                BUFFER_SIZE);
            writeObject(object, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Writes values to a comma-separated value stream.
     *
     * @param object
     * An instance of List<Object> containing the data to write to the CSV
     * file. List items must be instances of Dictionary<String, Object>. The
     * dictionary values will be written out in the order specified by the
     * key sequence.
     *
     * @param writer
     * The writer to which data will be written.
     */
    @SuppressWarnings("unchecked")
    public void writeObject(Object object, Writer writer)
        throws IOException, SerializationException {
        List<Dictionary<String, Object>> items = (List<Dictionary<String, Object>>)object;

        try {
            for (Dictionary<String, Object> item : items) {
                for (int i = 0, n = keys.getLength(); i < n; i++) {
                    String key = keys.get(i);

                    if (i > 0) {
                        writer.append(",");
                    }

                    Object value = item.get(key);
                    writer.append(value.toString());
                }

                writer.append("\r\n");
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public String getMIMEType(Object object) {
        return MIME_TYPE + "; charset=" + charset.name();
    }
}
