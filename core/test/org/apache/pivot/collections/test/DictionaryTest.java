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
package org.apache.pivot.collections.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.pivot.collections.HashMap;


public class DictionaryTest {
    @Test
    public void test() {
        HashMap<String, Integer> map = new HashMap<>();
        map.putIntValue("one", 1);
        map.putIntValue("two", 2);
        map.putIntValue("three", 300);
        assertEquals(map.getIntValue("one"), 1);
        assertEquals(map.getIntValue("two"), 2);
        assertEquals(map.getIntValue("three"), 300);
    }

}
