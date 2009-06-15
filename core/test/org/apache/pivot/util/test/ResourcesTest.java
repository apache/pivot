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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Locale;
import java.util.MissingResourceException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.SerializationException;
import pivot.util.Resources;

public class ResourcesTest {
    private Locale old;

    @Before
    public void setUp() throws Exception {
        old = Locale.getDefault();
        Locale.setDefault(Locale.UK);
    }

    @After
    public void tearDown() throws Exception {
        Locale.setDefault(old);
    };

    @Test
    public void testReadDefaultLocale() throws Exception {
        Resources res = new Resources("pivot.util.test.test1");
        assertResources(res, "SGML", "Standard Generalized Markup Language");
    }

    /**
     * The resource overrides the term for the country.
     *
     * @throws Exception
     */
    @Test
    public void testRead_GB_Locale() throws Exception {
        Resources res = new Resources("pivot.util.test.test2");
        assertResources(res, "SGML",
                "How Do, Youth, Standard Generalized Markup Language");
    }

    /**
     * The resource overrides the term for the country and the acronym for the
     * language.
     *
     * @throws Exception
     */
    @Test
    public void testRead_GB_en_Locale() throws Exception {
        assertEquals("Default locale should be en_GB", "en_GB", Locale
                .getDefault().toString());
        Resources res = new Resources("pivot.util.test.test3");
        assertResources(res, "XSGML",
                "How Do, Youth, Standard Generalized Markup Language");
    }

    /**
     * The resource overrides the term and the acronym for the country.
     *
     * @throws Exception
     */
    @Test
    public void testRead_GB_en_LocaleExtraOverride() throws Exception {
        assertEquals("Default locale should be en_GB", "en_GB", Locale
                .getDefault().toString());
        Resources res = new Resources("pivot.util.test.test6");
        assertResources(res, "XSGML",
                "eXtra Standard Generalized Markup Language");
    }

    @Test(expected=SerializationException.class)
    public void testSerialisationException() throws Exception {
        new Resources("pivot.util.test.test4");
    }

    @Test(expected=MissingResourceException.class)
    public void testMissingResource() throws Exception {
        // resource doesn't exist...
        new Resources("pivot.util.test.test5");
    }

    @Test(expected=IllegalArgumentException.class)
    public void testNullLocale() throws Exception {
        // resource exists, but locale is null
        new Resources("pivot.util.test.test1", (Locale) null);
    }

    @Test(expected=NullPointerException.class)
    public void testNullBaseName() throws Exception {
        new Resources((String)null);
    }

    @SuppressWarnings("unchecked")
    private static void assertResources(Resources res, String acronym, String term) {
        assertTrue(res.containsKey("glossary"));

        Map<String, Object> glossary = (Map<String, Object>) res
                .get("glossary");
        assertNotNull(glossary);
        assertTrue(glossary.containsKey("GlossDiv"));

        Map<String, Object> glossDiv = (Map<String, Object>) glossary
                .get("GlossDiv");
        assertNotNull(glossDiv);

        assertEquals("S", glossDiv.get("title"));

        assertTrue(glossDiv.containsKey("GlossList"));
        Map<String, Object> glossList = (Map<String, Object>) glossDiv
                .get("GlossList");
        assertNotNull(glossList);

        assertTrue(glossList.containsKey("GlossEntry"));
        Map<String, Object> glossEntry = (Map<String, Object>) glossList
                .get("GlossEntry");
        assertNotNull(glossEntry);

        assertEquals(acronym, glossEntry.get("Acronym"));
        assertEquals(term, glossEntry.get("GlossTerm"));

        assertTrue(glossEntry.containsKey("GlossDef"));
        Map<String, Object> glossDef = (Map<String, Object>) glossEntry
                .get("GlossDef");
        assertNotNull(glossDef);

        assertTrue(glossDef.containsKey("GlossSeeAlso"));
        List<String> list = (List<String>) glossDef.get("GlossSeeAlso");
        assertNotNull(list);
        assertEquals(2, list.getLength());
    }
}
