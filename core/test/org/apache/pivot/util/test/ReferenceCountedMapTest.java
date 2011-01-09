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

import static junit.framework.Assert.*;

import java.util.Iterator;
import java.util.Map;

import org.apache.pivot.util.ReferenceCountedMap;
import org.junit.Test;

public class ReferenceCountedMapTest {
    public static final String A_KEY = "A";
    public static final Integer A_VALUE = 100;

    public static final String B_KEY = "B";
    public static final Integer B_VALUE = 200;

    public static final Integer C_VALUE = 300;

    @Test
    public void getRemoveTest() {
        ReferenceCountedMap<String, Integer> map = new ReferenceCountedMap<String, Integer>();

        map.put(A_KEY, A_VALUE);

        assertEquals(map.countOf(A_KEY), 0);

        Integer a1 = map.get(A_KEY);
        assertEquals(a1, A_VALUE);
        assertEquals(map.countOf(A_KEY), 1);

        Integer a2 = map.get(A_KEY);
        assertEquals(a1, a2);
        assertEquals(map.countOf(A_KEY), 2);

        Integer removed1 = map.remove(A_KEY);
        assertEquals(removed1, null);
        assertEquals(map.countOf(A_KEY), 1);
        assertTrue(map.containsKey(A_KEY));

        Integer removed2 = map.remove(A_KEY);
        assertEquals(removed2, null);
        assertEquals(map.countOf(A_KEY), 0);
        assertTrue(map.containsKey(A_KEY));

        Integer removed3 = map.remove(A_KEY);
        assertEquals(removed3, A_VALUE);
        assertEquals(map.countOf(A_KEY), 0);
        assertFalse(map.containsKey(A_KEY));
    }

    @Test
    public void entrySetTest() {
        ReferenceCountedMap<String, Integer> map = new ReferenceCountedMap<String, Integer>();

        map.put(A_KEY, A_VALUE);
        map.put(B_KEY, B_VALUE);

        Iterator<Map.Entry<String, Integer>> iterator;

        // Iterate over entries but don't get values
        iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
        }

        for (String key : map.keySet()) {
            assertEquals(map.countOf(key), 0);
        }

        // Iterate over entries and get values
        iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            entry.getValue();
        }

        for (String key : map.keySet()) {
            assertEquals(map.countOf(key), 1);
        }

        // Iterate over entries and update values
        iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Integer> entry = iterator.next();
            entry.setValue(C_VALUE);
        }

        for (String key : map.keySet()) {
            // Verify that the value has been updated
            assertEquals(map.get(key), C_VALUE);

            // The count should be 1, since the set should have cleared it
            // and the get should have incremented it
            assertEquals(map.countOf(key), 1);
        }

        // Iterate over entries and remove values
        iterator = map.entrySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        for (String key : map.keySet()) {
            assertFalse(map.containsKey(key));
        }
    }
}
