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
package org.apache.pivot.text;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Class representing a span of characters. The range includes all values
 * in the interval <i><tt>[start, start+length-1]</tt></i> inclusive.  This is the paradigm
 * used in a lot of places (notably the text controls) to indicate a selection.
 * <p> A zero-length span indicates a single caret position at the given start.
 * <p> Negative lengths are not supported and will throw exceptions, as will
 * negative start positions.
 */
public final class CharSpan {
    public final int start;
    public final int length;

    public static final String START_KEY = "start";
    public static final String LENGTH_KEY = "length";

    /**
     * Construct a new char span of length zero at the given location.
     *
     * @param start The start of this char span.
     */
    public CharSpan(int start) {
        Utils.checkNonNegative(start, "start");
        this.start = start;
        this.length = 0;
    }

    /**
     * Construct a new char span with the given values.
     * @param start The start of this char span.
     * @param length The length of this char span.
     */
    public CharSpan(int start, int length) {
        Utils.checkNonNegative(start, "start");
        Utils.checkNonNegative(length, "length");
        this.start = start;
        this.length = length;
    }

    /**
     * Construct a new char span from another one (a "copy constructor").
     *
     * @param charSpan An existing char span (which must not be {@code null}).
     * @throws IllegalArgumentException if the given char span is {@code null}.
     */
    public CharSpan(CharSpan charSpan) {
        Utils.checkNull(charSpan, "charSpan");

        this.start = charSpan.start;
        this.length = charSpan.length;
    }

    /**
     * Construct a new char span from the given dictionary which must
     * contain the {@link #START_KEY} and {@link #LENGTH_KEY} keys.
     *
     * @param charSpan A dictionary containing start and end values.
     * @throws IllegalArgumentException if the given char span is {@code null}
     * or if the dictionary does not contain the start and length keys.
     */
    public CharSpan(Dictionary<String, ?> charSpan) {
        Utils.checkNull(charSpan, "charSpan");

        if (!charSpan.containsKey(START_KEY)) {
            throw new IllegalArgumentException(START_KEY + " is required.");
        }

        if (!charSpan.containsKey(LENGTH_KEY)) {
            throw new IllegalArgumentException(LENGTH_KEY + " is required.");
        }

        int start = charSpan.getInt(START_KEY);
        int length = charSpan.getInt(LENGTH_KEY);

        Utils.checkNonNegative(start, "start");
        Utils.checkNonNegative(length, "length");

        this.start = start;
        this.length = length;
    }

    /**
     * Construct a new char span from the given sequence with two
     * numeric values corresponding to the start and length values
     * respectively.
     *
     * @param charSpan A sequence containing the start and length values.
     * @throws IllegalArgumentException if the given char span is {@code null}.
     */
    public CharSpan(Sequence<?> charSpan) {
        Utils.checkNull(charSpan, "charSpan");

        int start = ((Number)charSpan.get(0)).intValue();
        int length = ((Number)charSpan.get(1)).intValue();

        Utils.checkNonNegative(start, "start");
        Utils.checkNonNegative(length, "length");

        this.start = start;
        this.length = length;
    }

    /**
     * Returns the inclusive end value of this char span, which is the
     * <tt>start + length - 1</tt>.  So, if the length is zero,
     * then the end will be less that the start.
     *
     * @return The computed inclusive end value of this char span.
     */
    public int getEnd() {
        return start + length - 1;
    }

    /**
     * Returns a new {@link CharSpan} with the start value offset by the given amount.
     *
     * @param offset The positive or negative amount by which to "move" this
     * char span (the start value).
     * @return A new {@link CharSpan} with the updated value.
     * @throws IllegalArgumentException if the updated start value goes negative.
     */
    public CharSpan offset(int offset) {
        return new CharSpan(this.start + offset, this.length);
    }

    /**
     * Returns a new {@link CharSpan} with the length value offset by the given amount
     * (either positive to lengthen the span or negative to shorten the span).
     *
     * @param offset The positive or negative amount by which to "lengthen" this
     * char span (the length value).
     * @return A new {@link CharSpan} with the updated value.
     * @throws IllegalArgumentException if the updated length value goes negative.
     */
    public CharSpan lengthen(int offset) {
        return new CharSpan(this.start, this.length + offset);
    }

    @Override
    public boolean equals(Object o) {
        boolean equal = false;

        if (o instanceof CharSpan) {
            CharSpan span = (CharSpan) o;
            equal = (start == span.start && length == span.length);
        }

        return equal;
    }

    @Override
    public int hashCode() {
        return 31 * start + length;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " {start:" + start + ", length:" + length + "}";
    }

    /**
     * Convert a string into a char span.
     * <p> If the string value is a JSON map, then parse the map
     * and construct using the {@link #CharSpan(Dictionary)} method.
     * <p> If the string value is a JSON list, then parse the list
     * and construct using the first two values as start and end
     * respectively, using the {@link #CharSpan(int, int)} constructor.
     * <p> Also accepted is a simple list of two integer values
     * separated by comma or semicolon.
     * <p> Otherwise the string should be a single integer value
     * that will be used to construct the char span using the {@link #CharSpan(int)}
     * constructor (just the start value, with a zero length).
     *
     * @param value The string value to decode into a new char span.
     * @return The decoded char span.
     * @throws IllegalArgumentException if the value is {@code null} or
     * if the string starts with <code>"{"</code> but it cannot be parsed as
     * a JSON map, or if it starts with <code>"["</code> but cannot be parsed
     * as a JSON list.
     */
    public static CharSpan decode(String value) {
        Utils.checkNullOrEmpty(value, "value");

        CharSpan charSpan;
        if (value.startsWith("{")) {
            try {
                charSpan = new CharSpan(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                charSpan = new CharSpan(JSONSerializer.parseList(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            String[] parts = value.split("\\s*[,;]\\s*");
            try {
                if (parts.length == 2) {
                    charSpan = new CharSpan(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
                } else if (parts.length == 1) {
                    charSpan = new CharSpan(Integer.parseInt(value));
                } else {
                    throw new IllegalArgumentException("Unknown format for CharSpan: " + value);
                }
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException(ex);
            }
        }

        return charSpan;
    }
}
