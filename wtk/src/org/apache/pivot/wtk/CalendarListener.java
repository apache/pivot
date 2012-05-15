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
 * Calendar listener interface.
 */
public interface CalendarListener {
    /**
     * Calendar listener adapter.
     */
    public static class Adapter implements CalendarListener {
        @Override
        public void yearChanged(Calendar calendar, int previousYear) {
            // empty block
        }

        @Override
        public void monthChanged(Calendar calendar, int previousMonth) {
            // empty block
        }

        @Override
        public void localeChanged(Calendar calendar, Locale previousLocale) {
            // empty block
        }

        @Override
        public void disabledDateFilterChanged(Calendar calendar, Filter<CalendarDate> previousDisabledDateFilter) {
            // empty block
        }
    }

    /**
     * Called when a calendar's year value has changed.
     *
     * @param calendar
     * @param previousYear
     */
    public void yearChanged(Calendar calendar, int previousYear);

    /**
     * Called when a calendar's month value has changed.
     *
     * @param calendar
     * @param previousMonth
     */
    public void monthChanged(Calendar calendar, int previousMonth);

    /**
     * Called when a calendar's locale has changed.
     *
     * @param calendar
     * @param previousLocale
     */
    public void localeChanged(Calendar calendar, Locale previousLocale);

    /**
     * Called when a calendar's disabled date filter has changed.
     *
     * @param calendar
     * @param previousDisabledDateFilter
     */
    public void disabledDateFilterChanged(Calendar calendar, Filter<CalendarDate> previousDisabledDateFilter);
}
