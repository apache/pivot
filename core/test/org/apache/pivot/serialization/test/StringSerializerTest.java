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
package org.apache.pivot.serialization.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.serialization.StringSerializer;
import org.junit.Test;

public class StringSerializerTest {
    public static final String testString = "// \n" + "// Hello from "
        + StringSerializerTest.class.getName() + "\n" + "// \n";
    public static final byte[] testBytes = testString.getBytes();

    public void log(String msg) {
        System.out.println(msg);
    }

    @Test
    public void readValues() throws IOException, SerializationException {
        log("readValues()");

        Serializer<String> serializer = new StringSerializer();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(testBytes);
        String result = serializer.readObject(inputStream);
        assertNotNull(result);
        assertEquals(result, testString);

        // dump content, but useful only for text resources ...
        String dump = result;
        int dumpLength = dump.getBytes().length;
        log("Result: " + dumpLength + " bytes \n" + dump);

        assertTrue(dumpLength > 0);
    }

    @Test
    public void writeValues() throws IOException, SerializationException {
        log("writeValues()");

        Serializer<String> serializer = new StringSerializer();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.writeObject(testString, outputStream);

        outputStream.flush();
        outputStream.close();

        String result = outputStream.toString();
        assertNotNull(result);
        assertEquals(result, testString);

        // dump content, but useful only for text resources ...
        String dump = result;
        int dumpLength = dump.getBytes().length;
        log("Result: " + dumpLength + " bytes \n" + dump);

        assertTrue(dumpLength > 0);
    }

}
