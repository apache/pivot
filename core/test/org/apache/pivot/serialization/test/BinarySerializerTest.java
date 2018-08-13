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
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pivot.serialization.BinarySerializer;
import org.apache.pivot.serialization.Serializer;
import org.junit.Test;

public class BinarySerializerTest {
    @Test
    public void testBinarySerializer() {
        Serializer<Object> serializer = new BinarySerializer();

        Object[] outputData = {"Hello World", 123.456, true};
        Object[] inputData;

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                serializer.writeObject(outputData, outputStream);
            } finally {
                outputStream.close();
            }

            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            try {
                inputData = (Object[]) serializer.readObject(inputStream);
            } finally {
                inputStream.close();
            }

            assertArrayEquals(outputData, inputData);
        } catch (Exception exception) {
            fail(exception.getMessage());
        }
    }
}
