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
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.List;
import pivot.collections.Sequence;

/**
 * Reads data from and writes data to a comma-separated value (CSV) file.
 *
 * TODO Add "firstLineContainsKeys" flag.
 *
 * TODO Add support for variable delimiters.
 */
public class CSVSerializer implements Serializer {
    public static final String MIME_TYPE = "text/csv";

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

    private ArrayList<String> keys = new ArrayList<String>();
    private KeySequence keySequence = new KeySequence();

    int c = -1;
    private Class<?> itemClass = HashMap.class;

    public CSVSerializer() {
    }

    public KeySequence getKeys() {
        return keySequence;
    }

    public Class<?> getItemClass() {
        return itemClass;
    }

    public void setItemClass(Class<?> itemClass) {
        if (itemClass == null) {
            throw new IllegalArgumentException("itemClass is null.");
        }

        this.itemClass = itemClass;
    }

    /**
     * Reads values from a comma-separated value file.
     *
     * @param inputStream
     * The input stream from which to read data.
     *
     * @return
     * An instance of List<Object> containing the data read from the CSV file.
     * The list items are instances of Dictionary<String, Object> populated by
     * mapping columns in the CSV file to keys in the key sequence.
     */
    @SuppressWarnings("unchecked")
    public Object readObject(InputStream inputStream)
        throws IOException, SerializationException {
        Object object = null;
        Reader reader = null;

        try {
            reader = new InputStreamReader(inputStream);
            object = readObject(reader);
        }
        finally {
            if (reader != null) {
                reader.close();
            }
        }

        return object;
    }

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
                    quoted = (c == '"');
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
     * Writes values to a comma-separated file.
     *
     * @param object
     * An instance of List<Object> containing the data to write to the CSV
     * file. List items must be instances of Dictionary<String, Object>. The
     * dictionary values will be written out in the order specified by the
     * key sequence.
     */
    public void writeObject(Object object, OutputStream outputStream)
        throws IOException, SerializationException {
        Writer writer = null;

        try {
            writer = new OutputStreamWriter(outputStream);
            writeObject(object, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void writeObject(Object object, Writer writer)
        throws IOException, SerializationException {
        List<Dictionary<String, Object>> items = (List<Dictionary<String, Object>>)object;

        PrintWriter printWriter = null;

        try {
            printWriter = new PrintWriter(writer);

            for (Dictionary<String, Object> item : items) {
                for (int i = 0, n = keys.getLength(); i < n; i++) {
                    String key = keys.get(i);

                    if (i > 0) {
                        printWriter.print(",");
                    }

                    Object value = item.get(key);
                    printWriter.print(value.toString());
                }

                printWriter.println();
            }
        } finally {
            if (printWriter != null) {
                printWriter.close();
            }
        }
    }

    public String getMIMEType() {
        return MIME_TYPE;
    }
}
