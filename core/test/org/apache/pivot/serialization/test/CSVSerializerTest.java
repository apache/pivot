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
package org.apache.pivot.serialization.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.serialization.CSVSerializerListener;
import org.apache.pivot.serialization.SerializationException;
import org.junit.Test;

public class CSVSerializerTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testBasicReadObject() throws IOException, SerializationException {
        // Test multiple line break formats
        StringBuilder buf = new StringBuilder();
        buf.append("a1,b1,c1\r\n");
        buf.append("a2,b2,c2\n");
        buf.append("a3,b3,c3\r");
        buf.append("a4,b4,c4");

        StringReader reader = new StringReader(buf.toString());

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");
        serializer.getCSVSerializerListeners().add(new CSVSerializerListener() {
            @Override
            public void beginList(CSVSerializer csvSerializer, List<?> list) {
                System.out.println("Begin list: " + list);
            }

            @Override
            public void endList(CSVSerializer csvSerializer) {
                System.out.println("End list");
            }

            @Override
            public void readItem(CSVSerializer csvSerializer, Object item) {
                System.out.println("Read item: " + item);
            }
        });

        List<?> result = serializer.readObject(reader);

        Dictionary<String, Object> row;

        // Test the first row
        row = (Dictionary<String, Object>) result.get(0);
        assertEquals(row.get("A"), "a1");
        assertEquals(row.get("B"), "b1");
        assertEquals(row.get("C"), "c1");

        // Test the second row
        row = (Dictionary<String, Object>) result.get(1);
        assertEquals(row.get("A"), "a2");
        assertEquals(row.get("B"), "b2");
        assertEquals(row.get("C"), "c2");

        // Test the third row
        row = (Dictionary<String, Object>) result.get(2);
        assertEquals(row.get("A"), "a3");
        assertEquals(row.get("B"), "b3");
        assertEquals(row.get("C"), "c3");

        // Test the fourth row
        row = (Dictionary<String, Object>) result.get(3);
        assertEquals(row.get("A"), "a4");
        assertEquals(row.get("B"), "b4");
        assertEquals(row.get("C"), "c4");
    }

    @Test
    public void testQuotedCommaReadObject() throws IOException, SerializationException {
        StringBuilder buf = new StringBuilder();
        buf.append("a,\",b,\",c\r\n");

        StringReader reader = new StringReader(buf.toString());

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        List<?> result = serializer.readObject(reader);

        @SuppressWarnings("unchecked")
        Dictionary<String, Object> row = (Dictionary<String, Object>) result.get(0);
        assertEquals("a", row.get("A"));
        assertEquals(",b,", row.get("B"));
        assertEquals("c", row.get("C"));
    }

    @Test
    public void testQuotedQuoteReadObject() throws IOException, SerializationException {
        StringBuilder buf = new StringBuilder();
        buf.append("a,\"\"\"b\"\"\",c\r\n");

        StringReader reader = new StringReader(buf.toString());

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        List<?> result = serializer.readObject(reader);

        @SuppressWarnings("unchecked")
        Dictionary<String, Object> row = (Dictionary<String, Object>) result.get(0);
        assertEquals("a", row.get("A"));
        assertEquals("\"b\"", row.get("B"));
        assertEquals("c", row.get("C"));
    }

    @Test
    public void testQuotedNewlineReadObject() throws IOException, SerializationException {
        StringBuilder buf = new StringBuilder();
        buf.append("a,\"b\nb  \",c\r\n");

        StringReader reader = new StringReader(buf.toString());

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        List<?> result = serializer.readObject(reader);

        @SuppressWarnings("unchecked")
        Dictionary<String, Object> row = (Dictionary<String, Object>) result.get(0);
        assertEquals("a", row.get("A"));
        assertEquals("b\nb", row.get("B"));
        assertEquals("c", row.get("C"));
    }

    @SuppressWarnings("unchecked")
    // or it will generate a warning during build with Java 7
    @Test
    public void testBasicWriteObject() throws IOException {
        List<Object> items = new ArrayList<>();
        items.add(new HashMap<>(new Dictionary.Pair<String, Object>("A", "a1"),
            new Dictionary.Pair<String, Object>("B", "b1"), new Dictionary.Pair<String, Object>(
                "C", "c1")));
        items.add(new HashMap<>(new Dictionary.Pair<String, Object>("A", "a2"),
            new Dictionary.Pair<String, Object>("B", "b2"), new Dictionary.Pair<String, Object>(
                "C", "c2")));

        StringWriter writer = new StringWriter();

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        serializer.writeObject(items, writer);

        assertEquals("a1,b1,c1\r\na2,b2,c2\r\n", writer.toString());
    }

    @SuppressWarnings("unchecked")
    // or it will generate a warning during build with Java 7
    @Test
    public void testQuotedCommaWriteObject() throws IOException {
        List<Object> items = new ArrayList<>();
        items.add(new HashMap<>(new Dictionary.Pair<String, Object>("A", "a"),
            new Dictionary.Pair<String, Object>("B", ",b,"), new Dictionary.Pair<String, Object>(
                "C", "c")));

        StringWriter writer = new StringWriter();

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        serializer.writeObject(items, writer);

        assertEquals("a,\",b,\",c\r\n", writer.toString());
    }

    @SuppressWarnings("unchecked")
    // or it will generate a warning during build with Java 7
    @Test
    public void testQuotedQuoteWriteObject() throws IOException {
        List<Object> items = new ArrayList<>();
        items.add(new HashMap<>(new Dictionary.Pair<String, Object>("A", "a"),
            new Dictionary.Pair<String, Object>("B", "\"b\""), new Dictionary.Pair<String, Object>(
                "C", "c")));

        StringWriter writer = new StringWriter();

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        serializer.writeObject(items, writer);

        assertEquals("a,\"\"\"b\"\"\",c\r\n", writer.toString());
    }

    @SuppressWarnings("unchecked")
    // or it will generate a warning during build with Java 7
    @Test
    public void testQuotedNewlineWriteObject() throws IOException {
        List<Object> items = new ArrayList<>();
        items.add(new HashMap<>(new Dictionary.Pair<String, Object>("A", "a"),
            new Dictionary.Pair<String, Object>("B", "\nb\n"), new Dictionary.Pair<String, Object>(
                "C", "c")));

        StringWriter writer = new StringWriter();

        CSVSerializer serializer = new CSVSerializer();
        serializer.setKeys("A", "B", "C");

        serializer.writeObject(items, writer);

        assertEquals("a,\"\nb\n\",c\r\n", writer.toString());
    }

    @Test
    public void testStreamReader() {
        // TODO
    }

    @Test
    public void testInlineKeys() throws IOException, SerializationException {
        StringBuilder buf = new StringBuilder();
        buf.append("A \t, B ,C \n");
        buf.append("a1,b1,c1\n");

        StringReader reader = new StringReader(buf.toString());

        CSVSerializer serializer = new CSVSerializer();
        List<?> result = serializer.readObject(reader);
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> row = (Dictionary<String, Object>) result.get(0);
        assertEquals(row.get("A"), "a1");
        assertEquals(row.get("B"), "b1");
        assertEquals(row.get("C"), "c1");
    }
}
