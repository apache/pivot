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

import java.util.Locale;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;

/**
 * Calendar button listener interface.
 */
public interface CalendarButtonListener {
    /**
     * Calendar button listener adapter.
     */
    public static class Adapter implements CalendarButtonListener {
        @Override
        public void yearChanged(CalendarButton calendarButton, int previousYear) {
            // empty block
        }

        @Override
        public void monthChanged(CalendarButton calendarButton, int previousMonth) {
            // empty block
        }

        @Override
        public void localeChanged(CalendarButton calendarButton, Locale previousLocale) {
            // empty block
        }

        @Override
        public void disabledDateFilterChanged(CalendarButton calendarButton,
            Filter<CalendarDate> previousDisabledDateFilter) {
            // empty block
        }
    }

    /**
     * Called when a calendar button's year value has changed.
     *
     * @param calendarButton
     * @param previousYear
     */
    public void yearChanged(CalendarButton calendarButton, int previousYear);

    /**
     * Called when a calendar button's month value has changed.
     *
     * @param calendarButton
     * @param previousMonth
     */
    public void monthChanged(CalendarButton calendarButton, int previousMonth);

    /**
     * Called when a calendar button's locale has changed.
     *
     * @param calendarButton
     * @param previousLocale
     */
    public void localeChanged(CalendarButton calendarButton, Locale previousLocale);

    /**
     * Called when a calendar button's disabled date filter has changed.
     *
     * @param calendarButton
     * @param previousDisabledDateFilter
     */
    public void disabledDateFilterChanged(CalendarButton calendarButton,
        Filter<CalendarDate> previousDisabledDateFilter);
}
