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
 * See the License for the lmecific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.pivot.wtk.Limits;


/**
 * Tests the {@link Limits} class which is used extensively
 * in the "wtk" source, and deserves good tests.
 */
public class LimitsTest {

    @Test
    public void test() {
        Limits lm_1 = new Limits(-1, 0);
        Limits lm0 = new Limits(0);
        Limits lm1 = new Limits(0, 1);
        Limits lm2 = Limits.decode("[0, 1]");
        Limits lm3 = Limits.decode("{minimum:2, maximum:3}");
        Limits lm3a = new Limits(2, 3);
        Limits lm4 = new Limits(4);
        Limits lm5 = new Limits(3, 4);
        Limits lm5a = new Limits(3, 4);
        Limits lm5b = Limits.decode("[3, 4]");
        Limits lmN = new Limits(0, 4);
        Limits lm6 = Limits.decode("5; 6");
        Limits lm6a = new Limits(5, 6);
        Limits lm7 = Limits.decode("9 - 10");
        Limits lm7a = new Limits(9, 10);

        assertEquals(lm_1.range(), 2);
        assertEquals(lm0.range(), 1);

        assertTrue(lm1.contains(0));
        assertTrue(lm1.contains(1));
        assertFalse(lm1.contains(2));

        assertFalse(lm0.equals(lm1));
        assertTrue(lm1.equals(lm2));
        assertEquals(lm3, lm3a);

        assertTrue(lm5.equals(lm5a));

        assertEquals(lm2.minimum, 0);
        assertEquals(lm2.maximum, 1);
        assertTrue(lm5a.equals(lm5b));

        assertEquals(lmN.constrain(5), 4);
        assertEquals(lmN.constrain(-2), 0);

        assertEquals(lm6, lm6a);
        assertEquals(lm6.toString(), "Limits [5-6]");
        assertEquals(lm7, lm7a);
        assertEquals(lm7.toString(), "Limits [9-10]");
    }

}
