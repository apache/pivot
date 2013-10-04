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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.util.Locale;

import org.apache.pivot.collections.List;
import org.apache.pivot.util.Resources;
import org.junit.Before;
import org.junit.Test;

public class ParentResourcesTest {

    private Resources parent;
    private Resources main;

    @Before
    public void before() throws Exception {
        parent = new Resources(getClass().getName() + "Parent");
        assertNotNull("From parent", parent.get("parentKeyString"));
        main = new Resources(parent, getClass().getName(), Locale.ENGLISH);
    }

    @Test
    public void testString() {
        assertNotNull("In main only", main.get("mainKeyString"));
        assertEquals("In parent", "From Parent", main.getParent().get("someMagicString"));
        assertEquals("In both", "From Main", main.get("someMagicString"));
    }

    @Test
    public void testNumber() {
        assertNotNull("In main only", main.get("mainKeyNumber"));
        assertEquals("In parent", Integer.valueOf(100), main.getParent().get("someMagicNumber"));
        assertEquals("In both", Integer.valueOf(200), main.get("someMagicNumber"));
    }

    @Test
    public void testBoolean() {
        assertNotNull("In main only", main.get("mainKeyBoolean"));
        assertEquals("In parent", false, main.getParent().get("someMagicBoolean"));
        assertEquals("In both", true, main.get("someMagicBoolean"));
    }

    @Test
    public void testMyMap() {
        assertFalse("not contains noMap", main.containsKey("noMap"));
        assertTrue("contains myMap", main.containsKey("myMap"));
        assertNotNull("myMap as object not null", main.get("myMap"));
        assertNull("noMap as map is null", main.get("noMap"));
        assertNotNull("myMap as map not null", main.get("myMap"));
    }

    @Test
    public void testList() {

        List<?> list = (List<?>) main.get("aList");
        assertNotNull("aList not null", list);
        assertEquals("6 items", 6, list.getLength());

    }

    @Test
    public void testLanguage() {

        assertEquals("Language", "This is specifically English", main.get("languageKey"));

        assertEquals("Language in parent", "This is not a specific language and is in the parent.",
            parent.get("languageKey"));

    }

    @Test
    public void testConstructors() throws Exception {
        main = new Resources(parent, getClass().getName(), Charset.defaultCharset());
        testString();

        main = new Resources(parent, getClass().getName(), Charset.defaultCharset());
        testString();

        main = new Resources(parent, getClass().getName(), Locale.ENGLISH);
        testString();

        main = new Resources(parent, getClass().getName(), Locale.ENGLISH, Charset.defaultCharset());
        testString();

        main = new Resources(parent, getClass().getName(), Locale.ENGLISH, Charset.defaultCharset());
        testString();

    }

}
