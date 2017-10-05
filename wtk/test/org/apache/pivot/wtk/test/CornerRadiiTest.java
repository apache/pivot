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
import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import org.apache.pivot.wtk.CornerRadii;

/**
 * Some very basic tests of the {@link CornerRadii} class used in
 * several places in the Pivot code (buttons, tabs, etc.).
 */
public class CornerRadiiTest {
    @Test
    public void test() {
        CornerRadii r0 = CornerRadii.NONE;
        CornerRadii r0a = new CornerRadii(0);
        CornerRadii r1 = new CornerRadii(1);
        CornerRadii r1a = CornerRadii.decode("[1, 1, 1, 1]");
        CornerRadii r2 = new CornerRadii(2, 2, 2, 2);
        CornerRadii r2a = new CornerRadii(2);
        CornerRadii r2b = CornerRadii.decode("{topLeft:2, bottomLeft:2, topRight:2, bottomRight:2}");
        CornerRadii r3 = new CornerRadii(2, 3, 4, 5);
        CornerRadii r3a = CornerRadii.decode("2, 3; 4, 5");

        assertEquals(r0, r0a);
        assertEquals(r1, r1a);
        assertNotEquals(r0, r1);
        assertEquals(r2, r2a);
        assertEquals(r2a, r2b);
        assertEquals(r3, r3a);
        assertEquals(r3a.toString(), "CornerRadii [2,3; 4,5]");
    }

}

