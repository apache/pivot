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

import static org.junit.Assert.*;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.junit.Test;

public class ArrayListTest {
    @Test
    public void basicTest() {
        ArrayList<String> list = new ArrayList<String>();
        list.insert("B", 0);
        list.insert("C", 1);
        list.insert("D", 2);
        list.insert("A", 0);

        assertEquals(ArrayList.binarySearch(list, "A"), 0);

        list.remove(0, 1);
        assertEquals(list.get(0), "B");
        assertEquals(list.getLength(), 3);
        assertEquals(list.indexOf("C"), 1);

        Sequence<String> removed = list.remove(1, 1);
        assertNotNull(removed.get(0));
        assertTrue(removed.get(0).equals("C"));

        list.trimToSize();
        list.ensureCapacity(10);
        assertEquals(list.getCapacity(), 10);

        list.insert("E", 1);
        assertEquals(list.getLength(), 3);
        assertNotNull(list.get(1));
        assertTrue(list.get(1).equals("E"));

        assertTrue(list.equals(new ArrayList<String>("B", "E", "D")));
        assertFalse(list.equals(new ArrayList<String>("B", "E", "D", "C")));
        assertFalse(list.equals(new ArrayList<String>("E", "C", "D")));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void copyConstructorTest() {
        ArrayList<Object> list = new ArrayList<Object>("a", "b", "c");

        Sequence<?> sequence = list;
        list = new ArrayList<Object>((Sequence<Object>)sequence);

        assertEquals(list.getLength(), 3);
        assertEquals(list.get(0), "a");
        assertEquals(list.get(1), "b");
        assertEquals(list.get(2), "c");
    }
}
