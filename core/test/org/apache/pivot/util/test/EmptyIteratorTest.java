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

import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

import org.apache.pivot.util.EmptyIterator;


public class EmptyIteratorTest {
    @Test
    public void basicTest() {
        Iterator<String> iter = new EmptyIterator<String>();
        assertTrue(!iter.hasNext());
        try {
            iter.next();
            assertTrue(false);
        } catch (NoSuchElementException nsee) {
            assertTrue(true);
        }
    }

    @Test
    public void removeTest() {
        Iterator<String> iter = new EmptyIterator<String>();
        try {
            iter.remove();
            assertTrue(false);
        } catch (UnsupportedOperationException uoe) {
            assertTrue(true);
        }
    }
}
