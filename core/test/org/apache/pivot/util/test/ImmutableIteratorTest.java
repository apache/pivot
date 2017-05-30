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

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.util.ImmutableIterator;


public class ImmutableIteratorTest {
    @Test
    public void test() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("Tom");
        strings.add("Dick");
        strings.add("Harry");
        strings.forEach(value -> System.out.println(value));

        Iterator<String> iter = strings.iterator();
        assertEquals(iter.hasNext(), true);
        assertEquals(iter.next(), "Tom");
        assertEquals(iter.hasNext(), true);
        assertEquals(iter.next(), "Dick");
        assertEquals(iter.hasNext(), true);
        assertEquals(iter.next(), "Harry");
        assertEquals(iter.hasNext(), false);

        iter = strings.iterator();
        ImmutableIterator<String> iiter = new ImmutableIterator<>(iter);

        try {
            iiter.remove();
            assertTrue(false);
        } catch (UnsupportedOperationException uoe) {
            assertTrue(true);
        }

        assertEquals(iiter.hasNext(), true);
        assertEquals(iiter.next(), "Tom");
        assertEquals(iiter.hasNext(), true);
        assertEquals(iiter.next(), "Dick");
        assertEquals(iiter.hasNext(), true);
        assertEquals(iiter.next(), "Harry");
        assertEquals(iiter.hasNext(), false);
    }
}

