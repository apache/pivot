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

package org.apache.pivot.collections.test;

 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.junit.Test;

public class HashMapTest {

    @Test
    public void basicTest() {

        HashMap<String, Integer> map = new HashMap<String, Integer>();

        assertTrue(map.isEmpty());
        assertEquals(0, map.getCount());
        assertNull(map.getComparator());
        assertFalse(map.containsKey("a"));
        assertNotNull(map.getMapListeners());
        assertNotNull(map.toString());

        assertNull(map.put("a", Integer.valueOf(1)));

        assertEquals(1, (int) map.put("a", 2));

        assertEquals(2, (int) map.get("a"));

        assertEquals(1, map.getCount());

        assertEquals(2, (int) map.remove("a"));

        assertEquals(0, map.getCount());

        map.put("a", 1);
        assertEquals(1, map.getCount());
        map.put("b", 2);
        assertEquals(2, map.getCount());
        map.put("c", 3);
        assertEquals(3, map.getCount());

        assertEquals(1, (int) map.get("a"));
        assertEquals(2, (int) map.get("b"));
        assertEquals(3, (int) map.get("c"));

        Iterator<String> iter = map.iterator();
        int count = 0;
        while (iter.hasNext()) {
            String s = iter.next();
            if (!map.containsKey(s)) {
                fail("Unknown element in map " + s);
            }

            count++;
        }
        assertEquals(3, count);

        try {
            iter.remove();
            fail("Expecting " + UnsupportedOperationException.class);
        } catch (UnsupportedOperationException ex) {
            // ignore, we're expecting this as part of the test
        }

        map.clear();

        assertEquals(0, map.getCount());

        assertEquals(null, map.get("a"));
        assertEquals(null, map.get("b"));
        assertEquals(null, map.get("c"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void constructorTests() throws Exception {
        // TODO Verify that this does not generate a warning under JDK 7
        HashMap<String, Integer> map = new HashMap<String, Integer>(new Map.Pair<String, Integer>(
            "a", 1), new Map.Pair<String, Integer>("b", 2));
        assertEquals(2, map.getCount());

        map = new HashMap<String, Integer>(map);
        assertEquals(2, map.getCount());

        map = new HashMap<String, Integer>(map);
        assertEquals(2, map.getCount());

    }

    @Test
    public void comparatorTest() {
        Comparator<Character> comparator = new Comparator<Character>() {
            @Override
            public int compare(Character c1, Character c2) {
                return c1.compareTo(c2);
            }
        };

        HashMap<Character, Integer> map = new HashMap<Character, Integer>(comparator);
        ArrayList<Character> keys = new ArrayList<Character>('c', 'a', 'x', 'r', 'd', 'n', 'f');

        int n = keys.getLength();

        int i = 0;
        while (i < n) {
            map.put(keys.get(i), i++);
        }

        keys.setComparator(comparator);

        int j = 0;
        for (Character c : keys) {
            assertEquals(keys.get(j++), c);
        }
    }

    @Test
    public void iteratorConcurrentModificationTest() {
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();

        map.put(1, 1);
        map.put(2, 2);
        Iterator<Integer> iter = map.iterator();
        iter.next();
        map.put(3, 3);
        try {
            iter.next();
            fail("Expecting " + ConcurrentModificationException.class);
        } catch (ConcurrentModificationException ex) {
            // expecting this
        }
    }

    private static int LOAD_COUNT = 50000;

    @Test
    public void pivotHashMapSpeedTest() {
        long t0 = System.currentTimeMillis();
        HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (int i = 0; i < LOAD_COUNT; i++) {
            map.put(Integer.valueOf(i), Integer.valueOf(i));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("org.apache.pivot.HashMap " + (t1 - t0) + "ms");
    }

    @Test
    public void javaHashMapSpeedTest() {
        long t0 = System.currentTimeMillis();
        java.util.HashMap<Integer, Integer> map = new java.util.HashMap<Integer, Integer>();
        for (int i = 0; i < LOAD_COUNT; i++) {
            map.put(Integer.valueOf(i), Integer.valueOf(i));
        }
        long t1 = System.currentTimeMillis();
        System.out.println("java.util.HashMap " + (t1 - t0) + "ms");
    }

    @Test
    public void iteratorTest() {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("font", "Verdana 11");
        map.put("colors", "#ff0000");

        Iterator<String> iterator = map.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            System.out.println(key);
        }
    }
}
