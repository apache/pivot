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
package org.apache.pivot.util;

import org.apache.pivot.text.CharSpan;

/**
 * A set of static methods that perform various character-based operations.
 */
public class CharUtils {

    /**
     * Return a {@link CharSpan} describing a "word" which contains the given
     * starting location in the character sequence.
     * <p> "Words" are defined as sequences of "Unicode Identifier Part" characters
     * or single characters that are not part of this class (and are not whitespace).
     *
     * @param sequence The sequence of characters to examine.
     * @param start The starting location from which to get a "word" selection.
     * @return The {@link CharSpan} (start and length) that describes the selected
     * word around the given starting point, or {@code null} if a word selection
     * cannot be made.
     */
    public static CharSpan selectWord(CharSequence sequence, int start) {
        int length = sequence.length();
        int adjustedStart = start;
        char ch;

        // Adjust the start position to put it within the sequence length
        // and skip any trailing line endings at that point
        if (adjustedStart >= length) {
            adjustedStart = length - 1;
            if (adjustedStart < 0) {
                return null;
            }
            while ((ch = sequence.charAt(adjustedStart)) == '\r' || ch == '\n') {
                adjustedStart--;
            }
        }
        if (adjustedStart < 0) {
            return null;
        }

        int selectionStart = adjustedStart;
        int selectionLength = 1;
        ch = sequence.charAt(selectionStart);
        if (Character.isWhitespace(ch)) {
            // Move backward to beginning of whitespace block
            // but not before the beginning of the text.
            do {
                selectionStart--;
            } while (selectionStart >= 0
                && Character.isWhitespace(sequence.charAt(selectionStart)));
            selectionStart++;
            selectionLength = start - selectionStart;

            // Move forward to end of whitespace block
            // but not past the end of the text.
            do {
                selectionLength++;
            } while (selectionStart + selectionLength < length
                && Character.isWhitespace(sequence.charAt(selectionStart + selectionLength)));
        } else if (Character.isUnicodeIdentifierPart(ch)) {
            // Move backward to beginning of identifier block
            do {
                selectionStart--;
            } while (selectionStart >= 0
                && Character.isUnicodeIdentifierPart(sequence.charAt(selectionStart)));
            selectionStart++;
            selectionLength = adjustedStart - selectionStart;

            // Move forward to end of identifier block
            // but not past end of text.
            do {
                selectionLength++;
            } while (selectionStart + selectionLength < length
                && Character.isUnicodeIdentifierPart(sequence.charAt(selectionStart
                    + selectionLength)));
        } else {
            return null;
        }

        return new CharSpan(selectionStart, selectionLength);
    }

    /**
     * Find the start of the "word" prior to the given starting point in the sequence.
     *
     * @param sequence The character sequence to search.
     * @param start The starting point to find the start of the word prior to.
     * @return The index of the prior word start.
     */
    public static int findPriorWord(CharSequence sequence, int start) {
        int wordStart = start;

        // Skip over any space immediately to the left
        while (wordStart > 0 && Character.isWhitespace(sequence.charAt(wordStart - 1))) {
            wordStart--;
        }

        // Skip over any word-letters to the left, or just skip one character for other stuff
        if (wordStart > 0) {
            if (Character.isUnicodeIdentifierPart(sequence.charAt(wordStart - 1))) {
                while (wordStart > 0
                    && Character.isUnicodeIdentifierPart(sequence.charAt(wordStart - 1))) {
                    wordStart--;
                }
            } else {
                wordStart--;
            }
        }

        return wordStart;
    }

    /**
     * Find the start of the "word" after the given starting point in the sequence.
     *
     * @param sequence The character sequence to search.
     * @param start The starting point to find the start of the word after.
     * @return The index of the next word start.
     */
    public static int findNextWord(CharSequence sequence, int start) {
        int wordStart = start;
        int count = sequence.length();

        // Skip over any word-letters to the right, or move one character for other stuff
        if (wordStart < count) {
            if (Character.isUnicodeIdentifierPart(sequence.charAt(wordStart))) {
                while (wordStart < count
                    && Character.isUnicodeIdentifierPart(sequence.charAt(wordStart))) {
                    wordStart++;
                }
            } else {
                wordStart++;
            }

            // Skip over any space immediately to the right to the beginning of the next word
            while (wordStart < count
                && Character.isWhitespace(sequence.charAt(wordStart))) {
                wordStart++;
            }
        }

        return wordStart;
    }
}

