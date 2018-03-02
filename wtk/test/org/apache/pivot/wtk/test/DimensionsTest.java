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
package org.apache.pivot.wtk.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Insets;


/**
 * Tests the {@link Dimensions} class which is used extensively
 * in the "wtk" source, and deserves good tests.
 */
public class DimensionsTest {

    @Test
    public void test() {
        Dimensions zero = Dimensions.ZERO;
        Dimensions zeroA = new Dimensions(0, 0);
        Dimensions zeroB = new Dimensions(0);
        Dimensions one = new Dimensions(1, 1);
        Dimensions oneA = zero.expand(1);
        Dimensions zeroC = oneA.expand(-1, -1);

        Dimensions seven = new Dimensions(7);
        Dimensions sevenA = new Dimensions(7, 7);
        Dimensions sevenB = zero.expand(7);
        Dimensions sevenC = zeroA.expand(7, 7);

        Dimensions a = Dimensions.decode("2 x 3");
        Dimensions a1 = new Dimensions(2, 3);
        Dimensions b = Dimensions.decode("{width:4, height:5}");
        Dimensions b1 = new Dimensions(4, 5);
        Dimensions c = Dimensions.decode("1  ,  2");
        Dimensions c1 = new Dimensions(1, 2);
        Dimensions d = Dimensions.decode("[  17,   23]");
        Dimensions d1 = new Dimensions(17, 23);
        Dimensions e = Dimensions.decode("23 ; 45");
        Dimensions e1 = new Dimensions(23, 45);

        Insets i1 = new Insets(1, 2, 1, 2);
        Dimensions f = e1.expand(i1);
        Dimensions f1 = new Dimensions(27, 47);

        assertEquals(zero, zeroA);
        assertEquals(one, oneA);
        assertEquals(zeroA, zeroB);
        assertEquals(zero, zeroC);

        assertEquals(seven, sevenA);
        assertEquals(seven, sevenB);
        assertEquals(sevenB, sevenC);

        assertEquals(a, a1);
        assertEquals(b, b1);
        assertEquals(c, c1);
        assertEquals(d, d1);
        assertEquals(e, e1);
        assertEquals(f, f1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void test1() {
        Dimensions ugly = Dimensions.decode("a x 3");
    }

    @Test(expected = IllegalArgumentException.class)
    public void test2() {
        Dimensions ugly = Dimensions.decode("1 : 3");
    }

}

