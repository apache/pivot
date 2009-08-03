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
import static org.junit.Assert.assertTrue;

import java.util.Comparator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.FilteredList;
import org.apache.pivot.util.Filter;
import org.junit.Test;

public class FilteredListTest {
    @Test
    public void basicTest() {
        // Create a list
        ArrayList<String> sourceList = new ArrayList<String>("Apple", "Banana", "Cherry", "Donut");
        FilteredList<String> filteredList = new FilteredList<String>(sourceList);

        assertEquals(filteredList.getLength(), 4);

        for (int i = 0, n = filteredList.getLength(); i < n; i++) {
            assertEquals(filteredList.get(i), sourceList.get(i));
        }

        // Filter the list
        filteredList.setFilter(new Filter<String>() {
            public boolean include(String string) {
                return !string.startsWith("D");
            }
        });

        assertEquals(filteredList.getLength(), 3);

        for (int i = 0, n = filteredList.getLength(); i < n; i++) {
            assertEquals(filteredList.get(i), sourceList.get(i));
        }

        // Add items to the source list
        sourceList.add("Eggplant");
        sourceList.add("Dr. Pepper");
        assertEquals(filteredList.getLength(), 4);

        // Add items to the filtered list
        filteredList.add("Fig");
        filteredList.add("Diet Coke");
        assertEquals(filteredList.getLength(), 5);
        assertEquals(sourceList.getLength(), 8);

        // Remove items from the source list
        sourceList.remove("Banana");
        assertEquals(filteredList.getLength(), 4);
        assertEquals(sourceList.getLength(), 7);

        sourceList.remove("Diet Coke");
        assertEquals(filteredList.getLength(), 4);
        assertEquals(sourceList.getLength(), 6);

        // Remove items from the filtered list
        filteredList.remove("Cherry");
        assertEquals(filteredList.getLength(), 3);
        assertEquals(sourceList.getLength(), 5);

        // Update items in the source list
        sourceList.update(0, "Doorknob");
        assertEquals(filteredList.getLength(), 2);

        sourceList.update(0, "Window");
        assertEquals(filteredList.getLength(), 3);

        // Update items in the filtered list
        filteredList.update(0, "Pickle");
        assertEquals(filteredList.get(0), "Pickle");

        try {
            filteredList.update(0, "Delivery");
        } catch(IllegalArgumentException exception) {
            assertTrue(true);
        }

        // Apply different comparators
        sourceList.setComparator(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return -s1.compareTo(s2);
            }
        });

        filteredList.setComparator(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        assertEquals(sourceList.get(0), "Window");
        assertEquals(filteredList.get(2), "Window");

        // Clear the filtered list
        filteredList.clear();
        assertEquals(sourceList.getLength(), 2);

        for (String string : sourceList) {
            assertEquals(Character.toLowerCase(string.charAt(0)), 'd');
        }
    }
}
