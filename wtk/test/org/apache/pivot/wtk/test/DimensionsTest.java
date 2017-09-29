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


/**
 * Tests the {@link Dimensions} class which is used extensively
 * in the "wtk" source, and deserves good tests.
 */
public class DimensionsTest {

    @Test
    public void test() {
        Dimensions zero = Dimensions.ZERO;
        Dimensions zero_a = new Dimensions(0, 0);
        Dimensions one = new Dimensions(1, 1);
        Dimensions one_a = zero.expand(1);
        Dimensions zero_b = one_a.expand(-1, -1);

        Dimensions a = Dimensions.decode("2 x 3");
        Dimensions a_1 = new Dimensions(2, 3);
        Dimensions b = Dimensions.decode("{width:4, height:5}");
        Dimensions b_1 = new Dimensions(4, 5);
        Dimensions c = Dimensions.decode("1  ,  2");
        Dimensions c_1 = new Dimensions(1, 2);
        Dimensions d = Dimensions.decode("[  17,   23]");
        Dimensions d_1 = new Dimensions(17, 23);
        Dimensions e = Dimensions.decode("23 ; 45");
        Dimensions e_1 = new Dimensions(23, 45);

        assertEquals(zero, zero_a);
        assertEquals(one, one_a);
        assertEquals(zero, zero_b);

        assertEquals(a, a_1);
        assertEquals(b, b_1);
        assertEquals(c, c_1);
        assertEquals(d, d_1);
        assertEquals(e, e_1);
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