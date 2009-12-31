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
package org.apache.pivot.tests;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class CalendarTest implements Application {
    private Window window = null;

    @WTKX private Calendar calendar = null;
    @WTKX private CalendarButton calendarButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        window = (Window)wtkxSerializer.readObject(this, "calendar_test.wtkx");
        wtkxSerializer.bind(this, CalendarTest.class);

        Filter<CalendarDate> disabledDateFilter = new Filter<CalendarDate>() {
            @Override
            public boolean include(CalendarDate date) {
                CalendarDate today = new CalendarDate();
                return (!date.equals(today));
            }
        };

        calendar.setDisabledDateFilter(disabledDateFilter);
        calendarButton.setDisabledDateFilter(disabledDateFilter);

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CalendarTest.class, args);
    }
}
