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
 * An object holding a boolean value that can be used with a <tt>forEach</tt>
 * or lambda expressionm, where the value used during the iteration must be
 * final or effectively final.
 */
public class BooleanResult {
    /** The current boolean value. */
    private boolean result;

    /**
     * Construct one of these and set the initial value to the given value.
     *
     * @param initialValue The initial boolean value.
     */
    public BooleanResult(boolean initialValue) {
        result = initialValue;
    }

    /**
     * Construct one of these and set the initial boolean value to
     * {@code false}.
     */
    public BooleanResult() {
        this(false);
    }

    /**
     * Update the saved value by <tt>OR</tt>ing this new value
     * with the saved value.
     *
     * @param value The new value to OR into the saved one.
     */
    public void or(boolean value) {
        result |= value;
    }

    /**
     * Update the saved value by <tt>AND</tt>ing this new value
     * with the saved value.
     *
     * @param value The new value to AND into the saved one.
     */
    public void and(boolean value) {
        result &= value;
    }

    /**
     * Update the saved value by <tt>XOR</tt>ing this new value
     * with the saved value.
     *
     * @param value The new value to XOR into the saved one.
     */
    public void xor(boolean value) {
        result ^= value;
    }

    /**
     * Negate the saved value.
     */
    public void not() {
        result = !result;
    }

    /**
     * Clear the boolean result to the default value
     * of {@code false}.
     */
    public void clear() {
        result = false;
    }

    /**
     * Set the result value to the given value.
     *
     * @param value The new value to set.
     */
    public void set(boolean value) {
        result = value;
    }

    /**
     * @return The final boolean value.
     */
    public boolean get() {
        return result;
    }

}
