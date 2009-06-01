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
package pivot.serialization.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import pivot.serialization.Serializer;
import pivot.serialization.BinarySerializer;

public class BinarySerializerTest {

    public static void main(String[] args) {
        Serializer<Object> serializer = new BinarySerializer();

        Object[] testData = {
            "Hello World",
            123.456,
            true
        };

        ByteArrayOutputStream outputStream = null;
        try {
            try {
                outputStream = new ByteArrayOutputStream();
                serializer.writeObject(testData, outputStream);
            } finally {
                outputStream.close();
            }
        } catch(Exception exception) {
            System.out.println(exception);
        }

        ByteArrayInputStream inputStream = null;
        try {
            try {
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                testData = (Object[])serializer.readObject(inputStream);

                for (int i = 0, n = testData.length; i < n; i++) {
                    System.out.println("[" + i + "] " + testData[i]);
                }
            } finally {
                inputStream.close();
            }
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }
}
