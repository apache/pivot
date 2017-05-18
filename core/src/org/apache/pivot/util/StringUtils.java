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
 * A set of static methods that perform various string manipulation
 * functions.
 */
public class StringUtils {

    /**
     * Make a string the consists of "n" copies of the given character.
     * <p> Note: "n" must be positive and less than 512K (arbitrary).
     *
     * @param ch The character to copy.
     * @param n  The number of times to copy this character.
     * @return   The resulting string.
     */
    public static String fromNChars(char ch, int n) {
        if (n == 0) {
            return "";
        }
        if (n < 0 || n > Integer.MAX_VALUE / 4) {
           throw new IllegalArgumentException("Requested string size " + n + " is out of range.");
        }

        // Nothing fancy here, but allocate the space and set length upfront
        // because we know how big the result should be.
        StringBuilder builder = new StringBuilder(n);
        builder.setLength(n);
        if (ch != '\0') {
            for (int i = 0; i < n; i++) {
                builder.setCharAt(i, ch);
            }
        }
        return builder.toString();
    }

}

