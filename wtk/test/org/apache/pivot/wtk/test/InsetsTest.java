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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Insets;


/**
 * Tests the {@link Insets} class which is used extensively
 * in the "wtk" source, and deserves good tests.
 */
public class InsetsTest {

    @Test
    public void test() {
        Insets i0 = Insets.NONE;
        Insets i0a = new Insets(0);
        Insets i1 = new Insets(1);
        Insets i1a = new Insets(1, 1, 1, 1);
        Insets i2 = new Insets(2);
        Insets i2a = new Insets(2.0f);
        Insets i2b = Insets.decode("[ 2, 2, 2, 2 ]");

        Insets i3 = new Insets(1, 2, 3, 4);
        Insets i3a = Insets.decode("{top:1, left:2, bottom:3, right:4}");

        Insets i4 = new Insets(5, 6, 7, 8);
        Insets i4a = Insets.decode("5, 6; 7, 8");

        Dimensions dim5 = new Dimensions(5);
        Insets i5 = new Insets(dim5);
        Insets i5a = new Insets(2, 2, 3, 3);
        Dimensions dim5a = i5.getSize();
        Dimensions dim5b = i5a.getSize();

        assertEquals(i0, i0a);
        assertEquals(i1, i1a);
        assertEquals(i2, i2a);
        assertEquals(i2, i2b);

        assertEquals(i3, i3a);
        assertEquals(i3.toString(), "Insets [1, 2, 3, 4]");

        assertEquals(i0.getWidth(), 0);
        assertEquals(i0.getHeight(), 0);
        assertEquals(i3.getWidth(), 6);
        assertEquals(i3.getHeight(), 4);

        assertEquals(i4, i4a);
        assertEquals(i4a.getWidth(), 14);
        assertEquals(i4a.getHeight(), 12);
        assertEquals(i4a.toString(), "Insets [5, 6, 7, 8]");

        assertEquals(i5, i5a);
        assertEquals(dim5, dim5a);
        assertEquals(dim5a, dim5b);
        assertEquals(i5.toString(), "Insets [2, 2, 3, 3]");
    }

}
