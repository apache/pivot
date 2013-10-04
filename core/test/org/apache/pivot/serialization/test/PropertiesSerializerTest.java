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

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.PropertiesSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.serialization.Serializer;
import org.junit.Test;

public class PropertiesSerializerTest {
    public static Map<String, Object> testMap = null;

    static {
        testMap = new HashMap<>();
        testMap.put("hello", "Hello World");
        testMap.put("number", 123.456);
        testMap.put("boolean", true);
        testMap.put("date", new java.util.Date());
        testMap.put("object", new Object());
    }

    public void log(String msg) {
        System.out.println(msg);
    }

    // utility method to transform the given Map to Properties,
    // and then to byte array
    protected byte[] mapToPropertiesToBytes(Map<String, Object> testMap2) throws IOException,
        SerializationException {
        Serializer<Map<?, ?>> serializer = new PropertiesSerializer();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        serializer.writeObject(testMap2, outputStream);
        outputStream.flush();
        outputStream.close();

        String result = outputStream.toString();

        return result.getBytes();
    }

    @Test
    public void writeValues() throws IOException, SerializationException {
        log("writeValues()");

        assertNotNull(testMap);

        Serializer<Map<?, ?>> serializer = new PropertiesSerializer();
        log("serializer instance created: " + serializer);

        byte[] testBytes = mapToPropertiesToBytes(testMap);
        assertNotNull(testBytes);

        // dump content, but useful only for text resources ...
        int dumpLength = testBytes.length;
        log("Result: "
            + dumpLength
            + " bytes"
            + ", dump written content now (could have different ordering of rows, and without comment lines):\n"
            + new String(testBytes));

        assertTrue(dumpLength > 0);
    }

    @Test
    // run writeValues before readValues, important
    public void readValues() throws IOException, SerializationException {
        log("readValues()");

        assertNotNull(testMap);

        // prepare test data, but without using a static variable
        byte[] testBytes = mapToPropertiesToBytes(testMap);
        assertNotNull(testBytes);

        Serializer<Map<?, ?>> serializer = new PropertiesSerializer();
        log("serializer instance created: " + serializer);

        ByteArrayInputStream inputStream = new ByteArrayInputStream(testBytes);
        @SuppressWarnings("unchecked")
        Map<String, Object> readData = (Map<String, Object>) serializer.readObject(inputStream);
        assertNotNull(readData);

        log("Succesfully Read");
        for (String key : readData) {
            log(key + "=" + readData.get(key));
        }

        inputStream.close();
    }

}
