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
package org.apache.pivot.tests;

/**
 * Test the speed of doing integer increments.
 */
public final class IterationTest {
    /** Private constructor since we use only static methods. */
    private IterationTest() {
    }

    /** The number of iterations we want to perform. */
    static final int COUNT = 100000000;

    /** Run the test.
     * @param args The command line arguments (which are ignored here).
     */
    public static void main(final String[] args) {
        long t0 = System.currentTimeMillis();

        int i = 0;
        while (i < COUNT) {
            i++;
        }

        long t1 = System.currentTimeMillis();

        System.out.println(Math.log10(COUNT) + " " + (t1 - t0) + "; " + Math.log10(COUNT)
            / Math.log10(2));
    }
}
