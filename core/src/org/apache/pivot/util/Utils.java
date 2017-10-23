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

/**
 * Utility methods for other parts of the code.
 */
public class Utils {
    /**
     * Decide if two strings are the same content (not just the same reference).
     * <p> Works properly for either string being <tt>null</tt>.
     * @param s1 First string to compare (can be {@code null}).
     * @param s2 Second string to compare (can also be {@code null}).
     * @return  <tt>true</tt> if both strings are <tt>null</tt> or if
     * <code>s1.equals(s2)</code>.
     */
    public static boolean stringsAreEqual(String s1, String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if ((s1 != null && s2 == null) || (s1 == null && s2 != null)) {
            return false;
        }
        return s1.equals(s2);
    }

    /**
     * Check if the input argument is {@code null} and throw an
     * {@link IllegalArgumentException} if so, with an optional
     * message derived from the given string.
     *
     * @param value The argument value to check for {@code null}.
     * @param description A description for the value used to
     * construct a message like {@code "xxx must not be null."}. Can be
     * {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNull(Object value, String description) {
        if (value == null) {
            if (isNullOrEmpty(description)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(description + " must not be null.");
            }
        }
    }

    /**
     * Check if the input argument is {@code null} and throw an
     * {@link IllegalArgumentException} with an empty message if so.
     *
     * @param value The argument value to check for {@code null}.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNull(Object value) {
        checkNull(value, null);
    }

    /**
     * Check if the input string is {@code null} or empty (or all whitespace).
     *
     * @param value The string to check.
     * @return {@code true} if the input is {@code null} or empty, {@code false}
     * otherwise.
     */
    public static boolean isNullOrEmpty(String value) {
        if (value == null) {
            return true;
        }
        return value.trim().isEmpty();
    }

    /**
     * Check if the input value is {@code null} or if it is a string and is empty
     * (or all whitespace).
     *
     * @param value The object to check.
     * @return {@code true} if the input is {@code null} or an empty string,
     * {@code false} otherwise (which would include a non-null object other
     * than a string).
     */
    public static boolean isNullOrEmpty(Object value) {
        if (value == null) {
            return true;
        }
        return (value instanceof String) && ((String)value).trim().isEmpty();
    }

    /**
     * Check if the input argument is {@code null} and if it is a string
     * if it is empty, and throw an {@link IllegalArgumentException} if so,
     * with an optional message derived from the given string.
     *
     * @param value The argument value to check for {@code null} or empty.
     * @param argument A description for the argument, used to
     * construct a message like {@code "xxx must not be null or empty."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is {@code null}.
     */
    public static void checkNullOrEmpty(Object value, String argument) {
        if (value == null || (value instanceof String && isNullOrEmpty((String)value))) {
            if (isNullOrEmpty(argument)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(argument + " must not be null or empty.");
            }
        }
    }

    /**
     * Check if the input argument is negative (less than zero), and throw an
     * {@link IllegalArgumentException} with or without a descriptive message,
     * depending on the {@code argument} supplied.
     *
     * @param value The value to check.
     * @param argument A description for the argument, used to
     * construct a message like {@code "xxx must not be negative."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkNonNegative(int value, String argument) {
        if (value < 0) {
            if (isNullOrEmpty(argument)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(argument + " must not be negative.");
            }
        }
    }

    /**
     * Check if the input argument is negative (less than zero), and throw an
     * {@link IllegalArgumentException} with or without a descriptive message,
     * depending on the {@code argument} supplied.
     *
     * @param value The value to check.
     * @param argument A description for the argument, used to
     * construct a message like {@code "xxx must not be negative."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkNonNegative(float value, String argument) {
        if (value < 0.0f) {
            if (isNullOrEmpty(argument)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(argument + " must not be negative.");
            }
        }
    }

    /**
     * Check if the input argument is positive (greater than zero), and throw an
     * {@link IllegalArgumentException} if not, with or without a descriptive message,
     * depending on the {@code argument} supplied.
     *
     * @param value The value to check.
     * @param argument A description for the argument, used to
     * construct a message like {@code "xxx must be positive."}.
     * Can be {@code null} or an empty string, in which case a plain
     * {@link IllegalArgumentException} is thrown without any detail message.
     * @throws IllegalArgumentException if the value is negative.
     */
    public static void checkPositive(int value, String argument) {
        if (value <= 0) {
            if (isNullOrEmpty(argument)) {
                throw new IllegalArgumentException();
            } else {
                throw new IllegalArgumentException(argument + " must be positive.");
            }
        }
    }

    /**
     * Check that the given value falls within the range of a non-negative "short" value, that is
     * between 0 and 0x7FFF (inclusive).
     *
     * @param value The value to check.
     * @param argument The optional argument used to describe the value in case it is out of range
     * (used in the thrown exception).
     * @throws IllegalArgumentException if the value is out of range.
     */
    public static void checkInRangeOfShort(int value, String argument) {
        if (value < 0 || value > 0x7FFF) {
            String valueMsg = isNullOrEmpty(argument) ? "value" : argument;
            throw new IllegalArgumentException(valueMsg + " must be less than or equal "
                + 0x7FFF + ".");
        }
    }

    /**
     * Check that the given {@code index} is between the values of {@code start} and {@code end}.
     *
     * @param index  The candidate index into the range.
     * @param start  The start of the acceptable range (inclusive).
     * @param end    The end of the acceptable range (inclusive).
     *
     * @throws IllegalArgumentException if {@code end} is &lt; {@code start}.
     * @throws IndexOutOfBoundsException if {@code index} is &lt; {@code start} or &gt; {@code end}.
     */
    public static void checkIndexBounds(int index, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end (" + end + ") < " + "start (" + start + ")");
        }
        if (index < start || index > end) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds [" + start + ","
                + end + "].");
        }
    }

    /**
     * Special case of {@link #checkIndexBounds(int, int, int)} for the case that start is zero and therefore
     * the end case is usually size - 1.
     *
     * @param index   The candidate index into the zero-based range.
     * @param size    The size of the array/list/etc. (so the proper range is {@code 0 .. size - 1}).
     * @throws IndexOutOfBoundsException if the {@code index} is &lt; 0 or &gt;= {@code size}.
     */
    public static void checkZeroBasedIndex(int index, int size) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds [0," + (size-1) + "].");
        }
    }

    /**
     * Check that the given {@code index} plus {@code count} are between the values of {@code start} and {@code end}.
     *
     * @param index  The candidate index into the range.
     * @param count  The number of elements in the indexed selection.
     * @param start  The start of the acceptable range (inclusive).
     * @param end    The end of the acceptable range (inclusive).
     *
     * @throws IllegalArgumentException if {@code end} is &lt; {@code start}, or if {@code count} or {@code start}
     * are &lt; zero.
     * @throws IndexOutOfBoundsException if {@code index} is &lt; {@code start} or {@code index + start} is &gt; {@code end}.
     */
    public static void checkIndexBounds(int index, int count, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end (" + end + ") < " + "start (" + start + ")");
        }
        if (count < 0 || start < 0) {
            throw new IllegalArgumentException("count (" + count + ") < 0 or start (" + start
                + ") < 0");
        }
        if (index < start) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds [" + start + ","
                + end + "].");
        }

        if (index + count > end) {
            throw new IndexOutOfBoundsException("index + count " + index + "," + count
                + " out of bounds [" + start + "," + end + "].");
        }
    }

    /**
     * Get the default value of {@link Object#toString} for any given object.
     *
     * @param obj Any object.
     * @return The result of what {@link Object#toString} would return without
     * any alternative implementation of <tt>toString()</tt> that may be implemented
     * in the class or any intervening superclass.
     */
    public static String defaultToString(Object obj) {
        return obj.getClass().getName() + "@" +
            Integer.toHexString(System.identityHashCode(obj));
    }

    /**
     * Get the (simple) default value of {@link Object#toString} for any given object.
     *
     * @param obj Any object.
     * @return The result of what {@link Object#toString} would return without
     * any alternative implementation of <tt>toString()</tt> that may be implemented
     * in the class or any intervening superclass, except that the simple name
     * of the class is used (without any package designation).
     */
    public static String simpleDefaultToString(Object obj) {
        return obj.getClass().getSimpleName() + "@" +
            Integer.toHexString(System.identityHashCode(obj));
    }

}
