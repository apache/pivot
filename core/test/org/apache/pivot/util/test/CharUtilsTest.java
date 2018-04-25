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
import static org.junit.Assert.assertNull;

import org.junit.Test;

import org.apache.pivot.text.CharSpan;
import org.apache.pivot.util.CharUtils;

/**
 * Test the word selection and navigation methods in {@link CharUtils}.
 */
public class CharUtilsTest {
    private static final String TEST_STRING = "A successful man is one who can lay a firm foundation with the bricks others have thrown at him. -David Brinkley\n";

    @Test
    public void testSelectWord() {
        CharSpan nullSpan = CharUtils.selectWord(TEST_STRING, -1);
        assertNull("null span from negative start", nullSpan);

        CharSpan firstWordSpan = new CharSpan(0, 1);
        CharSpan firstBlankSpan = new CharSpan(1, 1);
        CharSpan span0 = CharUtils.selectWord(TEST_STRING, 0);
        CharSpan span1 = CharUtils.selectWord(TEST_STRING, 1);
        assertEquals("one letter word", firstWordSpan, span0);
        assertEquals("first blank", firstBlankSpan, span1);

        CharSpan longWordSpan = new CharSpan(2, 10);
        CharSpan spanLong1 = CharUtils.selectWord(TEST_STRING, 5);
        CharSpan spanLong2 = CharUtils.selectWord(TEST_STRING, 11);
        assertEquals("long word", longWordSpan, spanLong1);
        assertEquals("same long word", spanLong1, spanLong2);

        CharSpan lastWordSpan = new CharSpan(TEST_STRING.length() - 9, 8);
        CharSpan spanLast = CharUtils.selectWord(TEST_STRING, TEST_STRING.length());
        assertEquals("last word", lastWordSpan, spanLast);
    }

    @Test
    public void testFindNextWord() {
       int length = TEST_STRING.length();
       int end = CharUtils.findNextWord(TEST_STRING, length - 4);
       assertEquals("next word at end", length, end);

       int midWord = CharUtils.findNextWord(TEST_STRING, 45);
       assertEquals("next word", 54, midWord);
    }

    @Test
    public void testFindPriorWord() {
        int first = CharUtils.findPriorWord(TEST_STRING, 2);
        assertEquals("first prior word", 0, first);

        int third = CharUtils.findPriorWord(TEST_STRING, 14);
        assertEquals("third prior word", 13, third);
    }
}
