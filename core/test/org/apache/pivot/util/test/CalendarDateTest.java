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
package org.apache.pivot.util.test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Time;


public class CalendarDateTest {
    private static final String d1 = "1941-12-07";
    private static final String d2 = "1929-10-29";
    private static final String d3 = "2008-09-29";

    @Test
    public void test1() {
        CalendarDate.Range r1 = new CalendarDate.Range(d1);
        CalendarDate.Range r1a = new CalendarDate.Range(d1, d1);
        CalendarDate.Range r2 = new CalendarDate.Range(d2, d3);
        CalendarDate.Range r3 = CalendarDate.Range.decode("{ \"start\" : \"1929-10-29\", \"end\" : \"2008-09-29\"}");
        CalendarDate.Range r3a = CalendarDate.Range.decode("[ \"1929-10-29\", \"2008-09-29\" ]");
        CalendarDate.Range r3b = CalendarDate.Range.decode("1929-10-29, 2008-09-29");

        CalendarDate cd1 = CalendarDate.decode(d1);
        CalendarDate cd2 = CalendarDate.decode(d2);
        CalendarDate cd3 = CalendarDate.decode(d3);

        assertTrue(r2.contains(r1));
        assertEquals(r1, r1a);
        assertEquals(r1.getLength(), 1);
        assertTrue(r2.normalize().equals(r2));
        // TODO: more tests of range methods: intersects, etc.

        assertEquals(r3, r3a);
        assertEquals(r3, r3b);
        assertEquals(r3a, r3b);

        assertEquals(cd1.year, 1941);
        assertEquals(cd1.month, 11);
        assertEquals(cd1.day, 6);
        assertEquals(cd1.toString(), d1);
    }

    @Test
    public void test2() {
        // PIVOT-1010: test interaction with LocalDate, etc. (new Java 8 classes)
        LocalDate ld1 = LocalDate.of(1941, 12, 7);
        CalendarDate cd1 = new CalendarDate(ld1);
        CalendarDate cd1a = CalendarDate.decode(d1);
        LocalDate ld1a = cd1a.toLocalDate();

        assertEquals(cd1, cd1a);
        assertEquals(ld1, ld1a);

        Time t1 = Time.decode("07:48:00");
        LocalDateTime dt1 = LocalDateTime.of(1941, 12, 7, 7, 48, 0);
        LocalDateTime dt1a = cd1.toLocalDateTime(t1);

        assertEquals(dt1, dt1a);
    }
}
