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

import org.apache.pivot.util.Time;
import org.junit.Test;

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
        System.out.println(time.subtract(new Time(0, 0, 1)));
        System.out.println(time.subtract(new Time(23, 59, 59, 999)));

        time = new Time(0, 0, 0);
        System.out.println(time.add(1));
        System.out.println(time.add(Time.MILLISECONDS_PER_DAY + 1));
        System.out.println(time.add(-1));
        System.out.println(time.add(-Time.MILLISECONDS_PER_DAY - 1));
        System.out.println(time.add(1000));
        System.out.println(time.add(-1000));

        time = Time.decode("00:00:00");
        System.out.println(time);

        time = Time.decode("00:00:00.000");
        System.out.println(time);

        try {
            time = Time.decode("00:00");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception);
        }

        try {
            time = Time.decode("00:00:00.00");
        } catch (IllegalArgumentException exception) {
            System.out.println(exception);
        }
    }
}
