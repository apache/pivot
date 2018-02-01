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
package org.apache.pivot.wtk;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.ListenerList;

/**
 * Calendar button selection listener interface.
 */
public interface CalendarButtonSelectionListener {
    /**
     * Calendar button selection listeners.
     */
    public static class Listeners extends ListenerList<CalendarButtonSelectionListener>
        implements CalendarButtonSelectionListener {
        @Override
        public void selectedDateChanged(CalendarButton calendarButton,
            CalendarDate previousSelectedDate) {
            forEach(listener -> listener.selectedDateChanged(calendarButton, previousSelectedDate));
        }
    }

    /**
     * Called when a calendar button's selected date has changed.
     *
     * @param calendarButton       The calendar button that changed.
     * @param previousSelectedDate The date that was previously selected in the calendar.
     */
    public void selectedDateChanged(CalendarButton calendarButton, CalendarDate previousSelectedDate);
}
