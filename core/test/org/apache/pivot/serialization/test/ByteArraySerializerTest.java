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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.pivot.serialization.ByteArraySerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.junit.Test;

public final class ByteArraySerializerTest {
    public static final String TEST_STRING = "// \n" + "// Hello from "
        + ByteArraySerializerTest.class.getName() + "\n" + "// \n";
    public static final byte[] TEST_BYTES = TEST_STRING.getBytes();

    public void log(final String msg) {
        System.out.println(msg);
    }

    @Test
    public void readValues() throws IOException, SerializationException {
        log("readValues()");

        Serializer<byte[]> serializer = new ByteArraySerializer();

        ByteArrayInputStream inputStream = new ByteArrayInputStream(TEST_BYTES);
        byte[] result = serializer.readObject(inputStream);
        assertNotNull(result);

        // dump content, but useful only for text resources ...
        String dump = new String(result);
        int dumpLength = dump.getBytes().length;
        log("Result: " + dumpLength + " bytes \n" + dump);

        assertTrue(dumpLength > 0);
    }

    @Test
    public void writeValues() throws IOException, SerializationException {
        log("writeValues()");

        Serializer<byte[]> serializer = new ByteArraySerializer();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.writeObject(TEST_BYTES, outputStream);

        outputStream.flush();
        outputStream.close();

        byte[] result = outputStream.toByteArray();
        assertNotNull(result);

        // dump content, but useful only for text resources ...
        String dump = new String(result);
        int dumpLength = dump.getBytes().length;
        log("Result: " + dumpLength + " bytes \n" + dump);

        assertTrue(dumpLength > 0);
    }

}
