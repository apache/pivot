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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.PropertiesSerializer;
import org.apache.pivot.serialization.Serializer;


public class PropertiesSerializerTest
{
    @SuppressWarnings({"unchecked"})
    public static void main(String[] args) {
        Serializer serializer = new PropertiesSerializer();

        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("hello",   "Hello World");
        testMap.put("number",  123.456);
        testMap.put("boolean", true);
        testMap.put("i18n",    "Â€ & ××˜×œ×¢×©");  // test some chars to encode ...
        testMap.put("object",  new Object());

        ByteArrayOutputStream outputStream = null;
        try {
            try {
                outputStream = new ByteArrayOutputStream();
                serializer.writeObject(testMap, outputStream);
            } finally {
                outputStream.close();

                String dump = new String(outputStream.toByteArray());
                System.out.println("Succesfully Written");
                System.out.println(dump);
            }
        } catch(Exception exception) {
            System.out.println(exception);
        }

        ByteArrayInputStream inputStream = null;
        Map<String, Object> readData = null;
        try {
            try {
                inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                readData = (Map<String, Object>) serializer.readObject(inputStream);

                System.out.println("Succesfully Read");
                for (String key : readData) {
                    System.out.println(key + "=" + readData.get(key));
                }
            } finally {
                inputStream.close();
            }
        } catch(Exception exception) {
            System.out.println(exception);
        }
    }

}