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
import static org.junit.Assert.assertTrue;

import org.apache.pivot.collections.EnumMap;
import org.junit.Test;

public class EnumMapTest {
    public enum TestEnum {
        A, B, C
    }

    @Test
    public void basicTest() {
        EnumMap<TestEnum, String> enumMap = new EnumMap<>(TestEnum.class);

        enumMap.put(TestEnum.A, "a");
        assertTrue(enumMap.containsKey(TestEnum.A));
        assertEquals(enumMap.get(TestEnum.A), "a");

        enumMap.put(TestEnum.C, "c");
        assertTrue(enumMap.containsKey(TestEnum.C));
        assertEquals(enumMap.get(TestEnum.C), "c");

        for (TestEnum key : enumMap) {
            assertEquals(key.toString().toLowerCase(), enumMap.get(key));
        }

        enumMap.remove(TestEnum.A);
        assertTrue(!enumMap.containsKey(TestEnum.A));

        enumMap.remove(TestEnum.C);
        assertTrue(enumMap.isEmpty());
    }
}
