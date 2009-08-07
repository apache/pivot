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
        assertNotNull("From parent", parent.getString("parentKeyString"));
        main = new Resources(parent, getClass().getName(), Locale.ENGLISH);
    }

    @Test
    public void testString() throws Exception {
        assertNotNull("In main only", main.getString("mainKeyString"));
        assertEquals("In parent", "From Parent", main.getParent().getString(
                "someMagicString"));
        assertEquals("In both", "From Main", main.getString("someMagicString"));
    }

    @Test
    public void testNumber() throws Exception {
        assertNotNull("In main only", main.getNumber("mainKeyNumber"));
        assertEquals("In parent", Integer.valueOf(100), main.getParent().getNumber(
                "someMagicNumber"));
        assertEquals("In both", Integer.valueOf(200), main
                .getNumber("someMagicNumber"));
    }

    @Test
    public void testInteger() throws Exception {
        assertNotNull("In main only", main.getInteger("mainKeyNumber"));
        assertEquals("In parent", Integer.valueOf(100), main.getParent()
                .getInteger("someMagicNumber"));
        assertEquals("In both", Integer.valueOf(200), main
                .getInteger("someMagicNumber"));
    }

    @Test
    public void testLong() throws Exception {
        assertNotNull("In main only", main.getLong("mainKeyNumber"));
        assertEquals("In parent", Long.valueOf(100), main.getParent().getLong(
                "someMagicNumber"));
        assertEquals("In both", Long.valueOf(200), main.getLong("someMagicNumber"));
    }

    @Test
    public void testShort() throws Exception {
        assertNotNull("In main only", main.getShort("mainKeyNumber"));
        assertEquals("In parent", Short.valueOf((short) 100), main.getParent()
                .getShort("someMagicNumber"));
        assertEquals("In both", Short.valueOf((short) 200), main
                .getShort("someMagicNumber"));
    }

    @Test
    public void testFloat() throws Exception {
        assertNotNull("In main only", main.getFloat("mainKeyNumber"));
        assertEquals("In parent", new Float(100), main.getParent().getFloat(
                "someMagicNumber"));
        assertEquals("In both", new Float(200), main
                .getFloat("someMagicNumber"));
    }

    @Test
    public void testDouble() throws Exception {
        assertNotNull("In main only", main.getDouble("mainKeyNumber"));
        assertEquals("In parent", new Double(100), main.getParent().getDouble(
                "someMagicNumber"));
        assertEquals("In both", new Double(200), main
                .getDouble("someMagicNumber"));
    }

    @Test
    public void testBoolean() throws Exception {
        assertNotNull("In main only", main.getBoolean("mainKeyBoolean"));
        assertEquals("In parent", false, main.getParent().getBoolean(
                "someMagicBoolean"));
        assertEquals("In both", true, main.getBoolean("someMagicBoolean"));
    }

    @Test
    public void testMyMap() {
        assertFalse("not contains noMap", main.containsKey("noMap"));
        assertTrue("contains myMap", main.containsKey("myMap"));
        assertFalse("empty map", main.isEmpty());
        assertNotNull("myMap as object not null", main.get("myMap"));
        assertNull("noMap as map is null", main.getMap("noMap"));
        assertNotNull("myMap as map not null", main.getMap("myMap"));
    }

    @Test
    public void testList() {

        List<?> list = main.getList("aList");
        assertNotNull("aList not null", list);
        assertEquals("6 items", 6, list.getLength());

    }

    @Test
    public void testLanguage() {

        assertEquals("Language", "This is specifically English", main
                .getString("languageKey"));

        assertEquals("Language in parent",
                "This is not a specific language and is in the parent.", parent
                        .getString("languageKey"));

    }

    @Test
    public void testConstructors() throws Exception {

        main = new Resources(parent, this);
        testString();

        main = new Resources(parent, getClass());
        testString();

        main = new Resources(parent, getClass().getName(), Charset
                .defaultCharset());
        testString();

        main = new Resources(parent, getClass().getName(), Charset
                .defaultCharset().name());
        testString();

        main = new Resources(parent, getClass().getName(), Locale.ENGLISH);
        testString();

        main = new Resources(parent, getClass().getName(), Locale.ENGLISH,
                Charset.defaultCharset());
        testString();

        main = new Resources(parent, getClass().getName(), Locale.ENGLISH,
                Charset.defaultCharset().name());
        testString();

    }

}
