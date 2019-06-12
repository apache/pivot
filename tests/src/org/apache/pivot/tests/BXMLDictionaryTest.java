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
package org.apache.pivot.tests;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.json.JSONSerializer;

public final class BXMLDictionaryTest {
    /** Hide utility class constructor. */
    private BXMLDictionaryTest() { }

    public static void main(String[] args) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        @SuppressWarnings("unchecked")
        HashMap<String, Object> hashMap =
            (HashMap<String, Object>) bxmlSerializer.readObject(
                BXMLDictionaryTest.class.getResource("bxml_dictionary_test.bxml"));
        System.out.println(JSONSerializer.toString(hashMap));
    }
}
