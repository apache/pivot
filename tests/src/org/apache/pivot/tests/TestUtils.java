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
 * Test utilities methods.
 */
public final class TestUtils {

    private static final String NA = "not available";

    private TestUtils() {
    }

    static final void testJavaSecurity() {
        try {
            System.out.println("The current SecurityManager is: " + System.getSecurityManager());

            System.out.println("Your operating system is: " + System.getProperty("os.name", NA));
            System.out.println("The JVM you are running is: "
                + System.getProperty("java.version", NA));
            System.out.println("Your user home directory is: "
                + System.getProperty("user.home", NA));
            System.out.println("Your JRE installation directory is: "
                + System.getProperty("java.home", NA));
        } catch (Exception e) {
            System.err.println("Caught exception: " + e.toString());
        }
    }

}
