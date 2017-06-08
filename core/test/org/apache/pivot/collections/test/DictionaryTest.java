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

import java.awt.Color;
import org.apache.pivot.collections.HashMap;


public class DictionaryTest {
    @Test
    public void test() {
        HashMap<String, Integer> map = new HashMap<>();
        map.putInt("one", 1);
        map.putInt("two", 2);
        map.putInt("three", 300);
        assertEquals(map.getInt("one"), 1);
        assertEquals(map.getInt("two"), 2);
        assertEquals(map.getInt("three"), 300);
    }

    @Test
    public void boolTest() {
        HashMap<String, Boolean> map = new HashMap<>();
        map.putBoolean("true", false);
        map.putBoolean("false", true);
        map.putBoolean("other", true);
        assertEquals(map.getBoolean("true"), false);
        assertEquals(map.getBoolean("false"), true);
        assertEquals(map.getBoolean("other"), true);
    }

    @Test
    public void colorTest() {
        HashMap<String, Color> map = new HashMap<>();
        map.put("black", Color.BLACK);
        assertEquals(map.getColor("black"), Color.BLACK);
    }

}
