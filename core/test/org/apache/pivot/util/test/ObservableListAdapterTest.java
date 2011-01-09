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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;

import org.apache.pivot.util.ObservableListAdapter;
import org.junit.Test;

public class ObservableListAdapterTest {
    @Test
    public void basicTest() {
        List<String> list = new ObservableListAdapter<String>(new ArrayList<String>());
        list.add(0, "B");
        list.add(1, "C");
        list.add(2, "D");
        list.add(0, "A");

        Object[] array = list.toArray();
        assertEquals(array[0], "A");

        list.remove(0);
        assertEquals(list.get(0), "B");
        assertEquals(list.size(), 3);
        assertEquals(list.indexOf("C"), 1);

        String removed = list.remove(1);
        assertNotNull(removed);
        assertTrue(removed.equals("C"));

        list.add(1, "E");
        assertEquals(list.size(), 3);
        assertNotNull(list.get(1));
        assertTrue(list.get(1).equals("E"));

        assertTrue(list.equals(Arrays.asList("B", "E", "D")));
        assertFalse(list.equals(Arrays.asList("B", "E", "D", "C")));
        assertFalse(list.equals(Arrays.asList("E", "C", "D")));

        List<String> copy = Arrays.asList("B", "E", "D");
        int i = 0;
        for (String item : list) {
            assertEquals(item, copy.get(i++));
        }

        int j = 0;
        ListIterator<String> iterator = list.listIterator();
        while (j < list.size()) {
            iterator.next();
            j++;
        }

        while (iterator.hasPrevious()) {
            String s = iterator.previous();
            assertEquals(s, copy.get(--j));
        }

        iterator = list.listIterator();
        assertEquals(iterator.next(), "B");
        assertEquals(iterator.next(), "E");
        assertEquals(iterator.previous(), "E");
        assertEquals(iterator.previous(), "B");
        assertEquals(iterator.next(), "B");

        iterator = list.listIterator();
        iterator.add("M");

        assertEquals(list, Arrays.asList("M", "B", "E", "D"));

        assertEquals(iterator.next(), "B");
        iterator.add("N");
        assertEquals(list, Arrays.asList("M", "B", "N", "E", "D"));
    }
}
