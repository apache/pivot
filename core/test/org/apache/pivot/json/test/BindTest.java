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
    @Test
    public void testUntypedList() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer(ArrayList.class);
        List<?> list = (List<?>)listSerializer.readObject(new StringReader("[1, 2, 3, 4, 5]"));
        assertEquals(list.get(0), 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTypedList() throws IOException, SerializationException {
        JSONSerializer listSerializer = new JSONSerializer();
        List<Object> list =
            (List<Object>)listSerializer.readObject(getClass().getResourceAsStream("list.json"));

        JSONSerializer typedListSerializer =
            new JSONSerializer((new TypeLiteral<ArrayList<TestBean2>>() {}).getType());
        ArrayList<TestBean2> typedList =
            (ArrayList<TestBean2>)typedListSerializer.readObject(getClass().getResourceAsStream("list.json"));

        Object item0 = typedList.get(0);
        assertTrue(item0 instanceof TestBean2);
        assertEquals(typedList.get(0).getA(), JSON.get(list, "[0].a"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testUntypedMap() throws IOException, SerializationException {
        JSONSerializer mapSerializer = new JSONSerializer(HashMap.class);
        HashMap<String, ?> map = (HashMap<String, ?>)mapSerializer.readObject(new StringReader("{a:1, b:2, c:3}"));
        assertEquals(map.get("a"), 1);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTypedMap() throws IOException, SerializationException {
        JSONSerializer mapSerializer = new JSONSerializer();
        Map<String, Object> map =
            (Map<String, Object>)mapSerializer.readObject(getClass().getResourceAsStream("map.json"));

        JSONSerializer typedMapSerializer = new JSONSerializer(TestBean1.class);
        TestBean1 typedMap =
            (TestBean1)typedMapSerializer.readObject(getClass().getResourceAsStream("map.json"));

        assertEquals(typedMap.getA(), JSON.get(map, "a"));
        assertEquals(typedMap.getB(), JSON.get(map, "b"));
        assertEquals(typedMap.getC(), JSON.get(map, "c"));
        assertEquals(typedMap.getD(), JSON.get(map, "d"));
        assertEquals(typedMap.getE(), JSON.get(map, "e"));
        assertEquals(typedMap.getI().getA(), JSON.get(map, "i.a"));

        Object k0 = typedMap.getK().get(0);
        assertTrue(k0 instanceof TestBean2);
        assertEquals(typedMap.getK().get(0).getA(), JSON.get(map, "k[0].a"));
    }
}
