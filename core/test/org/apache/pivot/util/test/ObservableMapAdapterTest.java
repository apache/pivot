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
package org.apache.pivot.util.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.pivot.util.ObservableMapAdapter;
import org.junit.Test;

public class ObservableMapAdapterTest {
    @Test
    public void basicTest() {
        ObservableMapAdapter<String, Integer> map =
            new ObservableMapAdapter<String, Integer>(new HashMap<String, Integer>());

        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
        assertFalse(map.containsKey("a"));
        assertNotNull(map.getObservableMapListeners());
        assertNotNull(map.toString());

        assertNull(map.put("a", Integer.valueOf(1)));

        assertEquals(1, (int) map.put("a", 2));

        assertEquals(2, (int) map.get("a"));

        assertEquals(1, map.size());

        assertEquals(2, (int) map.remove("a"));

        assertEquals(0, map.size());

        map.put("a", 1);
        assertEquals(1, map.size());
        map.put("b", 2);
        assertEquals(2, map.size());
        map.put("c", 3);
        assertEquals(3, map.size());

        assertEquals(1, (int) map.get("a"));
        assertEquals(2, (int) map.get("b"));
        assertEquals(3, (int) map.get("c"));

        Iterator<String> iter = map.keySet().iterator();
        int count = 0;
        while (iter.hasNext()) {
            String s = iter.next();
            if (!map.containsKey(s)) {
                fail("Unknown element in map " + s);
            }

            count++;
        }
        assertEquals(3, count);

        iter = map.keySet().iterator();
        while (iter.hasNext()) {
            iter.next();
            iter.remove();
        }
        assertEquals(0, map.size());

        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.clear();

        assertEquals(0, map.size());

        assertEquals(null, map.get("a"));
        assertEquals(null, map.get("b"));
        assertEquals(null, map.get("c"));
    }

    @Test
    public void iteratorConcurrentModificationTest() {
        ObservableMapAdapter<Integer, Integer> map =
            new ObservableMapAdapter<Integer, Integer>(new HashMap<Integer, Integer>());

        map.put(1, 1);
        map.put(2, 2);
        Iterator<Integer> iter = map.keySet().iterator();
        iter.next();
        map.put(3, 3);
        try {
            iter.next();
            fail("Expecting " + ConcurrentModificationException.class);
        } catch (ConcurrentModificationException ex) {
            // expecting this
        }
    }

    @Test
    public void iteratorTest() {
        ObservableMapAdapter<String, Object> map =
            new ObservableMapAdapter<String, Object>(new HashMap<String, Object>());
        map.put("font", "Verdana 11");
        map.put("colors", "#ff0000");

        Iterator<String> iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            System.out.println(key);
        }
    }

    @Test
    public void equalsTest() {
        ObservableMapAdapter<String, String> map1 =
            new ObservableMapAdapter<String, String>(new HashMap<String, String>());
        map1.put("a", "one");
        map1.put("b", "two");
        map1.put("c", "three");

        ObservableMapAdapter<String, String> map2 =
            new ObservableMapAdapter<String, String>(new HashMap<String, String>());
        map2.put("a", "one");
        map2.put("b", "two");
        map2.put("c", "three");

        // Same
        assertTrue(map1.equals(map2));

        // Different values
        map2.put("c", "four");
        assertFalse(map1.equals(map2));

        map1.put("c", null);
        assertFalse(map1.equals(map2));

        // Null comparison
        map2.put("c", null);
        assertTrue(map1.equals(map2));

        // Different lengths
        map2.put("d", "four");
        assertFalse(map1.equals(map2));
    }
}
