/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.core.test;

import java.io.StringReader;
import java.io.StringWriter;
import pivot.serialization.CSVSerializer;

public class CSVSerializerTest {
    public static String[] testStrings = {
        "\"YHOO\",26.11,-0.33,X",
        "0,1,2,3\nQ,5,6,7\n8,9,10,11",
        "a,b,c,d",
        "hello,world",
        "2,4,6,8,10"
    };

    public static void main(String[] args) {
        CSVSerializer csvSerializer = new CSVSerializer();
        csvSerializer.getKeys().add("A");
        csvSerializer.getKeys().add("B");
        csvSerializer.getKeys().add("C");
        csvSerializer.getKeys().add("D");

        for (int i = 0, n = testStrings.length; i < n; i++) {
            try {
                System.out.println("Input: " + testStrings[i]);
                Object object = csvSerializer.readObject(new StringReader(testStrings[i]));

                StringWriter writer = new StringWriter();
                csvSerializer.writeObject(object, writer);
                System.out.println("Output: " + writer);
            } catch(Exception exception) {
                System.out.println(exception);
            }
        }
    }
}
