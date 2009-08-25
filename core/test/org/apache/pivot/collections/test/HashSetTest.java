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

import org.apache.pivot.collections.HashSet;
import org.junit.Test;

public class HashSetTest {
    @Test
    public void basicTest() {
        HashSet<String> hashSet = new HashSet<String>();

        hashSet.add("A");
        assertTrue(hashSet.contains("A"));

        hashSet.add("B");
        assertTrue(hashSet.contains("A"));
        assertTrue(hashSet.contains("B"));

        assertEquals(hashSet.getCount(), 2);

        int i = 0;
        for (String element : hashSet) {
            assertTrue(element.equals("A")
                || element.equals("B"));
            i++;
        }

        assertEquals(i, 2);

        hashSet.remove("B");
        assertFalse(hashSet.contains("B"));

        hashSet.remove("A");
        assertFalse(hashSet.contains("A"));

        assertTrue(hashSet.isEmpty());
    }
}
