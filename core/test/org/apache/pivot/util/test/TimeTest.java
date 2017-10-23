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

import java.time.LocalTime;

import org.apache.pivot.util.Time;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TimeTest {
    @Test
    public void basicTest() {
        Time time = new Time();
        System.out.println(time);

        time = new Time(time.toMilliseconds());
        System.out.println(time);

        time = Time.decode(time.toString());
        System.out.println(time);

        time = new Time(0, 0, 0);
        int i1 = time.subtract(new Time(0, 0, 1));
        System.out.println(i1);
        assertEquals(i1, -1000);

        int i2 = time.subtract(new Time(23, 59, 59, 999));
        System.out.println(i2);
        assertEquals(i2, -(Time.MILLISECONDS_PER_DAY - 1));

        Time time0 = new Time(0, 0, 0);
        time = time0;
        Time time1 = time.add(1);
        System.out.println(time1);
        assertEquals(time1.toString(), "00:00:00.001");

        Time time2 = time.add(Time.MILLISECONDS_PER_DAY + 1);
        System.out.println(time2);
        assertEquals(time1, time2);
        assertEquals(time2.toString(), "00:00:00.001");

        Time time3 = time.add(-1);
        System.out.println(time3);
        assertEquals(time3.toString(), "23:59:59.999");

        Time time4 = time.add(-Time.MILLISECONDS_PER_DAY - 1);
        System.out.println(time4);
        assertEquals(time4.toString(), "23:59:59.999");

        Time time5 = time.add(1000);
        Time time5a = new Time(0, 0, 1);
        System.out.println(time5);
        assertEquals(time5, time5a);
        assertEquals(time5.toString(), "00:00:01");

        Time time6 = time.add(-1000);
        Time time6a = new Time(23, 59, 59);
        System.out.println(time6);
        assertEquals(time6, time6a);
        assertEquals(time6.toString(), "23:59:59");

        time = Time.decode("00:00:00");
        System.out.println(time);
        assertEquals(time, time0);

        time = Time.decode("00:00:00.000");
        System.out.println(time);
        assertEquals(time, time0);

        try {
            time = Time.decode("00:00");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception);
            assertEquals(exception.toString(), "java.lang.IllegalArgumentException: Invalid time format: 00:00");
        }

        try {
            time = Time.decode("00:00:00.00");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception);
            assertEquals(exception.toString(), "java.lang.IllegalArgumentException: Invalid time format: 00:00:00.00");
        }
    }

    @Test
    public void localTimeTest() {
        LocalTime localTime = LocalTime.of(7, 48);
        Time t1 = new Time(localTime);
        Time t1a = Time.decode("07:48:00.000");

        assertEquals(t1, t1a);
        assertEquals(t1.toString(), "07:48:00");

        // Test rounding of nanoseconds to milliseconds
        LocalTime lt1 = LocalTime.of(7, 48, 10, 499999);
        LocalTime lt2 = LocalTime.of(7, 48, 10, 500000);
        Time t2 = new Time(lt1);
        Time t2a = new Time(7, 48, 10, 0);
        Time t3 = new Time(lt2);
        Time t3a = new Time(7, 48, 10, 1);
        assertEquals(t2, t2a);
        assertEquals(t3, t3a);
        assertEquals(t2.toString(), "07:48:10");
        assertEquals(t3.toString(), "07:48:10.001");
    }
}
