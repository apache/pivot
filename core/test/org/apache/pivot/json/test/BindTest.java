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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
     * Read the given resource using the given serializer.
     *
     * @param serializer The JSON serializer to use.
     * @param file The name of the file resource to read.
     * @return The object read from the given resource.
     * @throws IOException possible from reading the resource.
     * @throws SerializationException if there was a problem with the syntax.
     */
    private Object readListResource(final JSONSerializer serializer, final String file)
        throws IOException, SerializationException {
        return serializer.readObject(getClass().getResourceAsStream(file));
    }

    /**
     * Read the "list.json" resource using the given serializer.
     * @param serializer The JSON serializer to use.
     * @return The object read from the "list.json" resource.
     * @throws IOException possible from reading the resource.
     * @throws SerializationException if there was a problem with the syntax.
     */
    private Object readListResource(final JSONSerializer serializer) throws IOException,
        SerializationException {
        return readListResource(serializer, "list.json");
    }

    private static final String A_B_C = "{a:1, b:2, c:'3'}";
    private static final String FOO_A_B_C = "{foo: {a:1, b:2, c:'3'}}";

    private static final double NANOS_PER_SEC = 1_000_000_000.0d;

    /**
     * Tests returning an untyped list.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testUntypedList() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer(ArrayList.class);
        List<?> list = (List<?>) listSerializer.readObject(new StringReader("[1, 2, 3, 4, 5]"));
        assertEquals(list.get(0), 1);
    }

    /**
     * Tests returning a typed list using
     * {@code org.apache.pivot.util.TypeLiteral}.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testTypedList() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) readListResource(listSerializer);

        JSONSerializer typedListSerializer = new JSONSerializer(
            (new TypeLiteral<ArrayList<SampleBean2>>() {
                // empty block
            }).getType());
        @SuppressWarnings("unchecked")
        ArrayList<SampleBean2> typedList = (ArrayList<SampleBean2>) readListResource(typedListSerializer);

        Object item0 = typedList.get(0);
        assertTrue(item0 instanceof SampleBean2);
        assertEquals((Integer) typedList.get(0).getA(), (Integer) JSON.get(list, "[0].a"));
    }

    /**
     * Tests returning a subclass of a generic
     * {@code org.apache.pivot.collections.List}.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testListSubclass() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) readListResource(listSerializer);

        JSONSerializer typedListSerializer = new JSONSerializer(SampleBean2ListSubclass.class);
        SampleBean2List typedList = (SampleBean2List) readListResource(typedListSerializer);

        Object item0 = typedList.get(0);
        assertTrue(item0 instanceof SampleBean2);
        assertEquals((Integer) typedList.get(0).getA(), (Integer) JSON.get(list, "[0].a"));
    }

    /**
     * Tests returning a class that implements
     * {@code org.apache.pivot.collections.Sequence}.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testSequence() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) readListResource(listSerializer);

        JSONSerializer sequenceSerializer = new JSONSerializer(SampleBean2SequenceSubclass.class);
        SampleBean2Sequence sequence = (SampleBean2Sequence) readListResource(sequenceSerializer);

        Object item0 = sequence.get(0);
        assertNotNull(item0);
        // assertTrue(item0 instanceof SampleBean2); // true but superfluous
        assertEquals((Integer) sequence.get(0).getA(), (Integer) JSON.get(list, "[0].a"));
    }

    /**
     * Tests returning an untyped map.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testUntypedMap() throws IOException, SerializationException {
        JSONSerializer mapSerializer = new JSONSerializer(HashMap.class);
        @SuppressWarnings("unchecked")
        HashMap<String, ?> map = (HashMap<String, ?>) mapSerializer.readObject(new StringReader(A_B_C));
        assertEquals(map.get("a"), 1);
    }

    /**
     * Tests returning a typed map using
     * {@code org.apache.pivot.util.TypeLiteral}.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testTypedMap() throws IOException, SerializationException {
        JSONSerializer typedMapSerializer = new JSONSerializer(
            (new TypeLiteral<HashMap<String, SampleBean2>>() {
                // empty block
            }).getType());

        @SuppressWarnings("unchecked")
        HashMap<String, SampleBean2> map =
                (HashMap<String, SampleBean2>) typedMapSerializer.readObject(new StringReader(FOO_A_B_C));

        assertTrue(JSON.get(map, "foo") instanceof SampleBean2);
        assertEquals(JSON.get(map, "foo.c"), "3");
    }

    /**
     * Tests returning a subclass of a generic
     * {@code org.apache.pivot.collections.Map}.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testMapSubclass() throws IOException, SerializationException {
        JSONSerializer typedMapSerializer = new JSONSerializer(SampleBean2MapSubclass.class);

        SampleBean2Map map = (SampleBean2Map) typedMapSerializer.readObject(new StringReader(FOO_A_B_C));

        assertTrue(JSON.get(map, "foo") instanceof SampleBean2);
        assertEquals(JSON.get(map, "foo.c"), "3");
    }

    /**
     * Tests returning a class that implements
     * {@code org.apache.pivot.collections.Dictionary}.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testDictionary() throws IOException, SerializationException {
        JSONSerializer dictionarySerializer = new JSONSerializer(
            SampleBean2DictionarySubclass.class);

        SampleBean2Dictionary dictionary =
                (SampleBean2Dictionary) dictionarySerializer.readObject(new StringReader(FOO_A_B_C));

        assertTrue(JSON.get(dictionary, "foo") instanceof SampleBean2);
        assertEquals(JSON.get(dictionary, "foo.c"), "3");
    }

    /**
     * Tests returning a Java bean value.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testBean() throws IOException, SerializationException {
        JSONSerializer mapSerializer = new JSONSerializer();
        mapSerializer.setAllowMacros(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) readListResource(mapSerializer, "map.json");

        JSONSerializer beanSerializer = new JSONSerializer(SampleBean1.class);
        beanSerializer.setAllowMacros(true);
        SampleBean1 typedMap = (SampleBean1) readListResource(beanSerializer, "map.json");

        assertEquals((Integer) typedMap.getA(), (Integer) JSON.get(map, "a"));
        assertEquals(typedMap.getB(), JSON.get(map, "b"));
        assertEquals(typedMap.getC(), JSON.get(map, "c"));
        assertEquals(typedMap.getD(), JSON.get(map, "d"));
        assertEquals(typedMap.getE(), JSON.get(map, "e"));
        assertEquals((Integer) typedMap.getI().getA(), (Integer) JSON.get(map, "i.a"));

        Object k0 = typedMap.getK().get(0);
        assertTrue(k0 instanceof SampleBean2);
        assertEquals((Integer) typedMap.getK().get(0).getA(), (Integer) JSON.get(map, "k[0].a"));
    }

    /**
     * Tests the speed reading a large file, with and without the macro reader involved.
     *
     * @throws IOException from reading the resource.
     * @throws SerializationException if there was a syntax error.
     */
    @Test
    public void testSpeed() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        listSerializer.setAllowMacros(true);
        long start1 = System.nanoTime();
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) readListResource(listSerializer);
        long end1 = System.nanoTime();

        // Read the same resource again (which should be cached), but this time
        // without the overhead of checking for macros
        JSONSerializer listSerializer2 = new JSONSerializer();
        listSerializer2.setAllowMacros(false);
        long start2 = System.nanoTime();
        @SuppressWarnings("unchecked")
        List<Object> list2 = (List<Object>) readListResource(listSerializer2);
        long end2 = System.nanoTime();

        long withMacrosTime = (end1 - start1);
        long withoutMacrosTime = (end2 - start2);
        double withMacrosSecs = (double) withMacrosTime / NANOS_PER_SEC;
        double withoutMacrosSecs = (double) withoutMacrosTime / NANOS_PER_SEC;
        // Just report the times, there is no right answer here
        System.out.format("Time with macro overhead = %1$.6f secs; time without macros = %2$.6f secs%n",
            withMacrosSecs, withoutMacrosSecs);
    }
}
