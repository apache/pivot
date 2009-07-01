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

import java.io.StringReader;
import java.io.StringWriter;

import org.apache.pivot.collections.List;
import org.apache.pivot.serialization.CSVSerializer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CSVSerializerTest {
    public static String[] testStrings = {
        "\"Y\"\"HO,O\",26.11,-0.33,X",
        "0,1,2,3\nQ,5,6,7\n8,9,10,11",
        "a,b,c,d",
        "hello,world",
        "2,4,6,8,10"
    };

    @Test
    public void testCSVSerializer() {
        CSVSerializer csvSerializer = new CSVSerializer();
        csvSerializer.getKeys().add("A");
        csvSerializer.getKeys().add("B");
        csvSerializer.getKeys().add("C");
        csvSerializer.getKeys().add("D");

        try {
            for (int i = 0, n = testStrings.length; i < n; i++) {
                List<?> objects = csvSerializer.readObject(new StringReader(testStrings[i]));

                StringWriter writer = new StringWriter();
                csvSerializer.writeObject(objects, writer);

                assertEquals(testStrings[i], writer.toString());
            }
        } catch (Exception exception) {
            fail(exception.getMessage());
        }
    }
}
