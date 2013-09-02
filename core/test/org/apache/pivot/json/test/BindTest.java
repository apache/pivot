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

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.StringReader;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.TypeLiteral;
import org.junit.Test;

public class BindTest {
    /**
     * Tests returning an untyped list.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testUntypedList() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer(ArrayList.class);
        List<?> list = (List<?>)listSerializer.readObject(new StringReader("[1, 2, 3, 4, 5]"));
        assertEquals(list.get(0), 1);
    }

    /**
     * Tests returning a typed list using {@code org.apache.pivot.util.TypeLiteral}.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testTypedList() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        List<Object> list =
            (List<Object>)listSerializer.readObject(getClass().getResourceAsStream("list.json"));

        JSONSerializer typedListSerializer =
            new JSONSerializer((new TypeLiteral<ArrayList<SampleBean2>>() {
                // empty block
            }).getType());
        ArrayList<SampleBean2> typedList =
            (ArrayList<SampleBean2>)typedListSerializer.readObject(getClass().getResourceAsStream("list.json"));

        Object item0 = typedList.get(0);
        assertTrue(item0 instanceof SampleBean2);
        assertEquals(typedList.get(0).getA(), JSON.get(list, "[0].a"));
    }

    /**
     * Tests returning a subclass of a generic {@code org.apache.pivot.collections.List}.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testListSubclass() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        List<Object> list =
            (List<Object>)listSerializer.readObject(getClass().getResourceAsStream("list.json"));

        JSONSerializer typedListSerializer = new JSONSerializer(SampleBean2ListSubclass.class);
        SampleBean2List typedList =
            (SampleBean2List)typedListSerializer.readObject(getClass().getResourceAsStream("list.json"));

        Object item0 = typedList.get(0);
        assertTrue(item0 instanceof SampleBean2);
        assertEquals(typedList.get(0).getA(), JSON.get(list, "[0].a"));
    }

    /**
     * Tests returning a class that implements {@code org.apache.pivot.collections.Sequence}.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testSequence() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        List<Object> list =
            (List<Object>)listSerializer.readObject(getClass().getResourceAsStream("list.json"));

        JSONSerializer sequenceSerializer = new JSONSerializer(SampleBean2SequenceSubclass.class);
        SampleBean2Sequence sequence =
            (SampleBean2Sequence)sequenceSerializer.readObject(getClass().getResourceAsStream("list.json"));

        Object item0 = sequence.get(0);
        assertNotNull(item0);
        // assertTrue(item0 instanceof SampleBean2);  // true but superfluous
        assertEquals(sequence.get(0).getA(), JSON.get(list, "[0].a"));
    }

    /**
     * Tests returning an untyped map.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testUntypedMap() throws IOException, SerializationException {
        JSONSerializer mapSerializer = new JSONSerializer(HashMap.class);
        HashMap<String, ?> map = (HashMap<String, ?>)mapSerializer.readObject(new StringReader("{a:1, b:2, c:'3'}"));
        assertEquals(map.get("a"), 1);
    }

    /**
     * Tests returning a typed map using {@code org.apache.pivot.util.TypeLiteral}.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testTypedMap() throws IOException, SerializationException {
        JSONSerializer typedMapSerializer =
            new JSONSerializer((new TypeLiteral<HashMap<String, SampleBean2>>() {
                // empty block
            }).getType());

        HashMap<String, SampleBean2> map =
            (HashMap<String, SampleBean2>)typedMapSerializer.readObject(new StringReader("{foo: {a:1, b:2, c:'3'}}"));

        assertTrue(JSON.get(map, "foo") instanceof SampleBean2);
        assertEquals(JSON.get(map, "foo.c"), "3");
    }

    /**
     * Tests returning a subclass of a generic {@code org.apache.pivot.collections.Map}.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testMapSubclass() throws IOException, SerializationException {
        JSONSerializer typedMapSerializer = new JSONSerializer(SampleBean2MapSubclass.class);

        SampleBean2Map map =
            (SampleBean2Map)typedMapSerializer.readObject(new StringReader("{foo: {a:1, b:2, c:'3'}}"));

        assertTrue(JSON.get(map, "foo") instanceof SampleBean2);
        assertEquals(JSON.get(map, "foo.c"), "3");
    }

    /**
     * Tests returning a class that implements {@code org.apache.pivot.collections.Dictionary}.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testDictionary() throws IOException, SerializationException {
        JSONSerializer dictionarySerializer = new JSONSerializer(SampleBean2DictionarySubclass.class);

        SampleBean2Dictionary dictionary =
            (SampleBean2Dictionary)dictionarySerializer.readObject(new StringReader("{foo: {a:1, b:2, c:'3'}}"));

        assertTrue(JSON.get(dictionary, "foo") instanceof SampleBean2);
        assertEquals(JSON.get(dictionary, "foo.c"), "3");
    }

    /**
     * Tests returning a Java bean value.
     *
     * @throws IOException
     * @throws SerializationException
     */
    @Test
    public void testBean() throws IOException, SerializationException {
        JSONSerializer mapSerializer = new JSONSerializer();
        Map<String, Object> map =
            (Map<String, Object>)mapSerializer.readObject(getClass().getResourceAsStream("map.json"));

        JSONSerializer beanSerializer = new JSONSerializer(SampleBean1.class);
        SampleBean1 typedMap =
            (SampleBean1)beanSerializer.readObject(getClass().getResourceAsStream("map.json"));

        assertEquals(typedMap.getA(), JSON.get(map, "a"));
        assertEquals(typedMap.getB(), JSON.get(map, "b"));
        assertEquals(typedMap.getC(), JSON.get(map, "c"));
        assertEquals(typedMap.getD(), JSON.get(map, "d"));
        assertEquals(typedMap.getE(), JSON.get(map, "e"));
        assertEquals(typedMap.getI().getA(), JSON.get(map, "i.a"));

        Object k0 = typedMap.getK().get(0);
        assertTrue(k0 instanceof SampleBean2);
        assertEquals(typedMap.getK().get(0).getA(), JSON.get(map, "k[0].a"));
    }
}
