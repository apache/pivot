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
package pivot.web.test;

import java.util.HashSet;
import java.util.Set;

import junit.framework.TestCase;
import pivot.web.Query;
import pivot.web.QueryDictionary;

public class QueryDictionaryTest extends TestCase {

    public void testQueryDictionary() {
        QueryDictionary dict = new QueryDictionary();

        assertNull(dict.get("key"));

        dict.put("key", "value");
        assertNotNull(dict.get("key"));

        assertEquals("value", dict.get("key", 0));

        assertEquals(1, dict.getLength("key"));

        try {
            dict.get("key", 1);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
        }

        assertEquals("value", dict.put("key", "value2"));
        assertEquals("value2", dict.get("key"));

        dict.add("key", "another value");

        assertEquals("another value", dict.get("key", 1));

        assertEquals(0, dict.getLength("nokey"));

        assertEquals(0, dict.add("key2", "new value"));

        dict.insert("key", "yet another value", 0);

        assertEquals(3, dict.getLength("key"));

        try {
            dict.insert("key", "bad value", 10);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {

        }

        assertEquals("yet another value", dict.remove("key"));
        assertNull(dict.remove("key"));

        dict.add("key2", "2nd value");
        assertEquals("new value", dict.remove("key2", 0));

        try {
            dict.remove("key2", 10);
            fail("Expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {

        }

        dict.add("key3", "something");

        Set<String> validKeys = new HashSet<String>();
        validKeys.add("key2");
        validKeys.add("key3");

        for (String s : (Iterable<String>) dict) {
            assertTrue(s, validKeys.remove(s));
        }

        assertEquals(0, validKeys.size());

        assertTrue(dict.containsKey("key2"));
        assertFalse(dict.isEmpty());

        dict.clear();
        assertTrue(dict.isEmpty());

    }
}
