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

import org.apache.pivot.wtk.Span;


/**
 * Tests the {@link Span} class which is used extensively
 * in the "wtk" source, and deserves good tests.
 */
public class SpanTest {

    @Test
    public void test() {
        Span sp_1 = new Span(-1, 0);
        Span sp0 = new Span(0);
        Span sp1 = new Span(0, 1);
        Span sp2 = Span.decode("[1, 0]");
        Span sp3 = Span.decode("{start:2, end:3}");
        Span sp3a = new Span(2, 3);
        Span sp4 = new Span(4);
        Span sp5 = sp3a.offset(1);
        Span sp5a = new Span(3, 4);
        Span sp5b = new Span(4, 3);
        Span spN = new Span(0, 4);
        Span spAll = sp1.union(sp0).union(sp2).union(sp3).union(sp4);

        assertEquals(sp_1.getLength(), 2);
        assertEquals(sp0.getLength(), 1);

        assertTrue(sp1.contains(sp0));

        assertTrue(sp_1.intersects(sp0));
        assertTrue(sp0.intersects(sp1));
        assertEquals(sp0.intersect(sp1), sp0);
        assertTrue(sp5b.intersects(sp3a));
        assertTrue(sp0.union(sp1).equals(sp1));

        assertFalse(sp0.equals(sp1));
        assertTrue(sp1.equals(sp2.normalize()));
        assertEquals(sp3, sp3a);

        assertFalse(sp0.adjacentTo(sp2));
        assertTrue(sp1.adjacentTo(sp3));
        assertTrue(sp3.adjacentTo(sp1));
        assertTrue(sp4.adjacentTo(sp3a));
        assertTrue(sp3.adjacentTo(sp4));

        assertTrue(sp5.equals(sp5a));
        assertEquals(spN, spAll);

        assertEquals(sp2.normalStart(), 0);
        assertEquals(sp2.normalEnd(), 1);
        assertTrue(sp5a.normalEquals(sp5b));

        assertTrue(sp4.after(sp1));
        assertTrue(sp3a.before(sp4));
    }

}