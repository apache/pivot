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
package org.apache.pivot.json.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.json.JSONSerializerListener;
import org.apache.pivot.serialization.SerializationException;
import org.junit.Test;

public class JSONSerializerTest {
    @Test
    public void testCarriageReturns() {
        List<?> emptyList;
        try {
            emptyList = JSONSerializer.parseList("[\n]");
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        assertEquals(0, emptyList.getLength());
    }

    @Test
    public void testE() throws SerializationException {
        assertEquals(5000000, JSONSerializer.parseDouble("5.0E6"), 0);
        assertEquals(0.000005, JSONSerializer.parseDouble("5.0E-6"), 0);
    }

    @Test(expected = SerializationException.class)
    public void testFloatNaN() throws SerializationException {
        JSONSerializer.toString(Float.NaN);
    }

    @Test(expected = SerializationException.class)
    public void testFloatNegativeInfinity() throws SerializationException {
        JSONSerializer.toString(Float.NEGATIVE_INFINITY);
    }

    @Test(expected = SerializationException.class)
    public void testFloatPositiveInfinity() throws SerializationException {
        JSONSerializer.toString(Float.POSITIVE_INFINITY);
    }

    @Test(expected = SerializationException.class)
    public void testDoubleNaN() throws SerializationException {
        JSONSerializer.toString(Double.NaN);
    }

    @Test(expected = SerializationException.class)
    public void testDoubleNegativeInfinity() throws SerializationException {
        JSONSerializer.toString(Double.NEGATIVE_INFINITY);
    }

    @Test(expected = SerializationException.class)
    public void testDoublePositiveInfinityN() throws SerializationException {
        JSONSerializer.toString(Double.POSITIVE_INFINITY);
    }

    @Test
    public void testEquals() throws IOException, SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();
        jsonSerializer.setAllowMacros(true);
        JSONSerializerListener jsonSerializerListener = new JSONSerializerListener() {
            @Override
            public void beginDictionary(final JSONSerializer jsonSerializerArgument,
                final Dictionary<String, ?> value) {
                System.out.println("Begin dictionary: " + value);
            }

            @Override
            public void endDictionary(final JSONSerializer jsonSerializerArgument) {
                System.out.println("End dictionary");
            }

            @Override
            public void readKey(final JSONSerializer jsonSerializerArgument, final String key) {
                System.out.println("Read key: " + key);
            }

            @Override
            public void beginSequence(final JSONSerializer jsonSerializerArgument, final Sequence<?> value) {
                System.out.println("Begin sequence: " + value);
            }

            @Override
            public void endSequence(final JSONSerializer jsonSerializerArgument) {
                System.out.println("End sequence");
            }

            @Override
            public void readString(final JSONSerializer jsonSerializerArgument, final String value) {
                System.out.println("Read string: " + value);
            }

            @Override
            public void readNumber(final JSONSerializer jsonSerializerArgument, final Number value) {
                System.out.println("Read number: " + value);
            }

            @Override
            public void readBoolean(final JSONSerializer jsonSerializerArgument, final Boolean value) {
                System.out.println("Read boolean: " + value);
            }

            @Override
            public void readNull(final JSONSerializer jsonSerializerArgument) {
                System.out.println("Read null");
            }
        };

        jsonSerializer.getJSONSerializerListeners().add(jsonSerializerListener);
        Object o1 = jsonSerializer.readObject(getClass().getResourceAsStream("map.json"));

        assertEquals((Integer) JSON.get(o1, "a"), (Integer) 100);
        assertEquals(JSON.get(o1, "b"), "Hello");
        assertEquals(JSON.get(o1, "c"), false);
        assertEquals((Integer) JSON.get(o1, "e.g"), (Integer) 5);
        assertEquals((Integer) JSON.get(o1, "i.a"), (Integer) 200);
        assertEquals(JSON.get(o1, "i.c"), true);
        assertEquals(JSON.get(o1, "m"), "Hello\r\n\tWorld!");

        jsonSerializer.getJSONSerializerListeners().remove(jsonSerializerListener);
        Object o2 = jsonSerializer.readObject(getClass().getResourceAsStream("map.json"));
        assertEquals((Integer) JSON.get(o2, "k[1].a"), (Integer) 10);
        assertEquals((Integer) JSON.get(o2, "k[2].a"), (Integer) 100);
        assertEquals((Integer) JSON.get(o2, "k[2].b"), (Integer) 200);
        assertEquals(JSON.get(o2, "k[2].c"), "300");
        assertEquals((Integer) JSON.get(o2, "j"), (Integer) 200);
        assertEquals(JSON.get(o2, "n"), "This is a \"test\" of the 'quoting' in \\JSON\\");

        assertTrue(o1.equals(o2));

        List<?> d = JSON.get(o1, "d");
        d.remove(0, 1);

        assertFalse(o1.equals(o2));
    }

    @Test
    public void testJavaMap() throws SerializationException {
        System.out.println("Test interaction with Standard java.util.Map");

        java.util.HashMap<String, java.util.Map<String, String>> root = new java.util.HashMap<>();
        java.util.HashMap<String, String> child = new java.util.HashMap<>();

        child.put("name", "John Doe");
        child.put("address", "123 Main St.\r\nAnytown USA");
        root.put("child", child);

        String childName = JSON.get(root, "child.name");
        String childAddr = JSON.get(root, "child.address");
        System.out.println("JSON child.name = \"" + childName + "\", child.address = \"" + childAddr + "\"");
        assertEquals(childName, "John Doe");
        assertEquals(childAddr, "123 Main St.\r\nAnytown USA");

        String serializedForm = JSONSerializer.toString(root);
        System.out.println("Serialized form: \"" + serializedForm + "\"");
        assertEquals(serializedForm, "{child: {address: \"123 Main St.\\r\\nAnytown USA\", name: \"John Doe\"}}");
    }

}
