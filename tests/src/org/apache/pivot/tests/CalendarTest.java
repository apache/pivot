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

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.CalendarButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;

public class CalendarTest implements Application {
    private Window window = null;

    @BXML private Calendar calendar = null;
    @BXML private CalendarButton calendarButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        window = (Window) bxmlSerializer.readObject(CalendarTest.class, "calendar_test.bxml");
        bxmlSerializer.bind(this, CalendarTest.class);

        Filter<CalendarDate> todayFilter = new Filter<CalendarDate>() {
            @Override
            public boolean include(CalendarDate date) {
                CalendarDate today = new CalendarDate();
                return (!date.equals(today));
            }
        };

        calendar.setDisabledDateFilter(todayFilter);

        calendarButton.getCalendarButtonListeners().add(new CalendarButtonListener() {
            @Override
            public void yearChanged(CalendarButton calendarButtonArgument, int previousYear) {
                disable();
            }

            @Override
            public void monthChanged(CalendarButton calendarButtonArgument, int previousMonth) {
                disable();
            }

            private void disable() {
                calendarButton.setDisabledDateFilter(new Filter<CalendarDate>() {
                    @Override
                    public boolean include(CalendarDate date) {
                        return true;
                    }
                });

                Task<Void> task = new Task<Void>() {
                    @Override
                    public Void execute() {
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException exception) {
                            // ignore exception
                        }

                        return null;
                    }
                };

                System.out.println("STARTING TASK");

                task.execute(new TaskAdapter<>(new TaskListener<Void>() {
                    @Override
                    public void taskExecuted(Task<Void> taskArgument) {
                        System.out.println("EXECUTED");
                        calendarButton.setDisabledDateFilter(null);
                    }

                    @Override
                    public void executeFailed(Task<Void> taskArgument) {
                        System.out.println("FAILED");
                        calendarButton.setDisabledDateFilter(null);
                    }
                }));
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CalendarTest.class, args);
    }

}
