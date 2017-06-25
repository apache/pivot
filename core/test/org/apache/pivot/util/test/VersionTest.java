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

import org.junit.Test;

import org.apache.pivot.util.Version;


public class VersionTest {
    @Test
    public void testApplicationStartup() {
        // These are the things that happen right away for ApplicationContext
        // and therefore would break immediately if Version was broken (as did
        // happen for Java 8u131).
        // Get the JVM version
        Version jvmVersion = Version.decode(System.getProperty("java.vm.version"));
        System.out.format("JVM Version: %1$s%n", jvmVersion.toString());
        Version pivotVersion = null;
        Package corePackage = Version.class.getPackage();

        // Get the Java runtime version
        Version javaVersion = Version.decode(System.getProperty("java.version"));
        System.out.format("Java version: %1$s%n", javaVersion);

        // Get the Pivot version
        String version = corePackage.getImplementationVersion();
        if (version == null) {
            pivotVersion = new Version(0, 0, 0, 0);
            assertEquals("default Pivot version", "0.0.0_00", pivotVersion.toString());
        } else {
            pivotVersion = Version.decode(version);
            System.out.format("Pivot Version: %1$s%n", pivotVersion);
        }
    }

    @Test
    public void testVersionParsing() {
        String s1_8_131 = "1.8.0_131";
        Version v1 = Version.decode(s1_8_131);
        Version v8_131 = new Version(1, 8, 0, 131);
        assertEquals("version decode", v1, v8_131);
        assertEquals("version to string", s1_8_131, v8_131.toString());

        String s1_0_0 = "1.0.0_00";
        Version v0 = Version.decode(s1_0_0);
        Version v1_0 = new Version(1, 0, 0, 0);
        assertEquals("version 0 decode", v0, v1_0);
        assertEquals("version 0 to string", v1_0.toString(), s1_0_0);
    }

    @Test
    public void testLimits() {
        Version vMax = new Version(32767, 32767, 32767, 32767);
        String sMax = "32767.32767.32767_32767";
        assertEquals("max versions", vMax.toString(), sMax);
        IllegalArgumentException argFailure = null;
        try {
            Version vOverflow = new Version(32768, 0, 1, 0);
        }
        catch (IllegalArgumentException iae) {
            argFailure = iae;
        }
        assertNotNull("illegal argument exception", argFailure);
        assertEquals("exception message", argFailure.getMessage(), "majorRevision must be less than or equal 32767.");
    }

    @Test
    public void testNumber() {
        Version vNum = new Version(2, 1, 1, 100);
        long num = vNum.getNumber();
        System.out.format("test getNumber(): %1$s -> %2$d%n", vNum, num);
        assertEquals("long number", num, 562954248454244L);
    }

    @Test
    public void testOtherVersions() {
        // Taken from PIVOT-996 test case
        final String PIVOT_996_SUFFIX = "25.51-b14";
        final String PIVOT_996_INPUT  = "8.1.028 " + PIVOT_996_SUFFIX;
        final String PIVOT_996_OUTPUT = "8.1.28_00-" + PIVOT_996_SUFFIX;

        Version jvmVersionParsed = Version.decode(PIVOT_996_INPUT);
        Version jvmVersionExplicit = new Version(8, 1, 28, 0, PIVOT_996_SUFFIX);
        String parsedToString = jvmVersionParsed.toString();

        assertEquals("PIVOT-996 test case", jvmVersionParsed, jvmVersionExplicit);
        System.out.format("PIVOT-996 parsed/toString: %1$s, expected: %2$s%n", parsedToString, PIVOT_996_OUTPUT);
        assertEquals("PIVOT-996 toString", parsedToString, PIVOT_996_OUTPUT);

        String sysJavaVersion = System.getProperty("java.runtime.version");
        Version javaVersion = Version.decode(sysJavaVersion);
        String formattedJavaVersion = javaVersion.toString();
        System.out.format("Java Runtime version (parsed and formatted): %1$s, raw: %2$s%n", formattedJavaVersion, sysJavaVersion);
        assertEquals("Java Runtime version", sysJavaVersion, formattedJavaVersion);
    }
}
