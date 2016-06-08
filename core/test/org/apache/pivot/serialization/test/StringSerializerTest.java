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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.serialization.StringSerializer;
import org.junit.Test;

public class StringSerializerTest {
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    // Note: include a real Unicode character to test the UTF-8 encoding
    public static final String testString = "// \n" + "// Hello from "
        + StringSerializerTest.class.getSimpleName() + "\n" + "// \u03C0 r square \n"
        + "// \n";
    public static final byte[] testBytes = testString.getBytes(UTF_8);

    public void log(String msg) {
        System.out.println(msg);
    }

    public void logBytes(String msg, byte[] b) {
        StringBuilder buf = new StringBuilder(b.length * 4);
        buf.append('[');
        for (int i = 0; i < b.length; i++) {
            if (i > 0)
                buf.append(',');
            int ib = ((int)b[i]) & 0xFF;
            String hex = Integer.toHexString(ib).toUpperCase();
            if (hex.length() < 2)
                buf.append('0');
            buf.append(hex);
        }
        buf.append(']');
        log(msg + ": " + buf.toString() + "\n");
    }

    @Test
    public void readValues() throws IOException, SerializationException {
        log("readValues()");

        Serializer<String> serializer = new StringSerializer(UTF_8);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(testBytes);
        String result = serializer.readObject(inputStream);
        assertNotNull(result);
        assertEquals(result, testString);

        // dump content, but useful only for text resources ...
        String dump = result;
        byte[] dumpBytes = dump.getBytes();
        int dumpLength = dumpBytes.length;
        log("Result: " + dumpLength + " bytes \n" + dump);
        logBytes("Result bytes", dumpBytes);

        assertTrue(dumpLength > 0);
    }

    @Test
    public void writeValues() throws IOException, SerializationException {
        log("writeValues()");
log("test string = \"" + testString + "\"");
        // Note: assume the default Charset for StringSerializer is UTF-8, which we are using here
        Serializer<String> serializer = new StringSerializer();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.writeObject(testString, outputStream);

        outputStream.flush();
        outputStream.close();

        String result = outputStream.toString(UTF_8.name());
        assertNotNull(result);
        assertEquals(result, testString);

        byte[] resultBytes = outputStream.toByteArray();
        assertArrayEquals(resultBytes, testBytes);

        // dump content, but useful only for text resources ...
        log("Result: " + resultBytes.length + " bytes \n" + result);
        logBytes("Result bytes", resultBytes);
        logBytes("  Test bytes", testBytes);

        assertTrue(resultBytes.length > 0);
    }

}
