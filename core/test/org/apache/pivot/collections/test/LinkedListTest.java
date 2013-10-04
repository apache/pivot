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
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.ConcurrentModificationException;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.LinkedList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.junit.Test;

public class LinkedListTest {
    @Test
    public void basicTest() {
        LinkedList<String> list = new LinkedList<>();
        list.insert("B", 0);
        list.insert("C", 1);
        list.insert("D", 2);
        list.insert("A", 0);

        assertEquals(list.indexOf("A"), 0);

        list.remove(0, 1);
        assertEquals(list.get(0), "B");
        assertEquals(list.getLength(), 3);
        assertEquals(list.indexOf("C"), 1);

        Sequence<String> removed = list.remove(1, 1);
        assertNotNull(removed.get(0));
        assertTrue(removed.get(0).equals("C"));

        list.insert("E", 1);
        assertEquals(list.getLength(), 3);
        assertNotNull(list.get(1));
        assertTrue(list.get(1).equals("E"));

        list.update(1, "F");
        assertNotNull(list.get(1));
        assertTrue(list.get(1).equals("F"));

        list.insert("G", 0);
        assertTrue(list.equals(new LinkedList<>("G", "B", "F", "D")));
        assertFalse(list.equals(new LinkedList<>("G", "B", "F", "D", "E")));
        assertFalse(list.equals(new LinkedList<>("B", "F", "E")));

        assertEquals(4, list.getLength());

        ArrayList<String> copy = new ArrayList<>("G", "B", "F", "D");
        int i = 0;
        for (String item : list) {
            assertEquals(item, copy.get(i++));
        }

        int j = 0;
        List.ItemIterator<String> iterator = list.iterator();
        while (j < list.getLength()) {
            iterator.next();
            j++;
        }

        while (iterator.hasPrevious()) {
            String s = iterator.previous();
            assertEquals(s, copy.get(--j));
        }

        iterator = list.iterator();
        assertEquals(iterator.next(), "G");
        assertEquals(iterator.next(), "B");
        assertEquals(iterator.previous(), "B");
        assertEquals(iterator.previous(), "G");
        assertEquals(iterator.next(), "G");

        iterator = list.iterator();
        iterator.insert("M");
        assertEquals(list, new LinkedList<>("M", "G", "B", "F", "D"));

        assertEquals(iterator.next(), "M");
        iterator.insert("N");
        assertEquals(list, new LinkedList<>("M", "N", "G", "B", "F", "D"));

        iterator = list.iterator();
        iterator.toEnd();
        assertEquals(iterator.previous(), "D");

        iterator.toStart();
        assertEquals(iterator.next(), "M");
    }

    @Test
    public void sortTest1() {
        LinkedList<String> linkedList = new LinkedList<>();
        linkedList.setComparator(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
        });

        linkedList.add("N");
        linkedList.add("P");
        linkedList.add("d");
        linkedList.add("A");
        linkedList.add("z");

        assertEquals(linkedList, new LinkedList<>("A", "d", "N", "P", "z"));
    }

    @Test
    public void sortTest2() {
        LinkedList<String> linkedList = new LinkedList<>();

        linkedList.add("N");
        linkedList.add("P");
        linkedList.add("d");
        linkedList.add("A");
        linkedList.add("z");

        linkedList.setComparator(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.toLowerCase().compareTo(s2.toLowerCase());
            }
        });

        assertEquals(linkedList, new LinkedList<>("A", "d", "N", "P", "z"));
    }

    @Test(expected = ConcurrentModificationException.class)
    public void simpleConcurrentModificationTest() {
        LinkedList<String> linkedList = new LinkedList<>("a", "b", "c", "d", "e");
        List.ItemIterator<String> iterator = linkedList.iterator();
        linkedList.remove(0, 1);
        iterator.next();
    }

    @Test(expected = ConcurrentModificationException.class)
    public void advancedConcurrentModificationTest() {
        LinkedList<String> linkedList = new LinkedList<>("a", "b", "c", "d", "e");

        List.ItemIterator<String> iterator1 = linkedList.iterator();
        List.ItemIterator<String> iterator2 = linkedList.iterator();

        iterator1.next();
        iterator2.next();

        iterator1.insert("a1");
        iterator2.next();
    }
}
