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
import org.apache.pivot.util.ListenerList;

/**
 * Calendar listener interface.
 */
public interface CalendarListener {
    /**
     * Calendar listeners.
     */
    public static class Listeners extends ListenerList<CalendarListener> implements CalendarListener {
        @Override
        public void yearChanged(Calendar calendar, int previousYear) {
            forEach(listener -> listener.yearChanged(calendar, previousYear));
        }

        @Override
        public void monthChanged(Calendar calendar, int previousMonth) {
            forEach(listener -> listener.monthChanged(calendar, previousMonth));
        }

        @Override
        public void localeChanged(Calendar calendar, Locale previousLocale) {
            forEach(listener -> listener.localeChanged(calendar, previousLocale));
        }

        @Override
        public void disabledDateFilterChanged(Calendar calendar,
            Filter<CalendarDate> previousDisabledDateFilter) {
            forEach(listener -> listener.disabledDateFilterChanged(calendar, previousDisabledDateFilter));
        }
    }

    /**
     * Calendar listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
        public void disabledDateFilterChanged(Calendar calendar,
            Filter<CalendarDate> previousDisabledDateFilter) {
            // empty block
        }
    }

    /**
     * Called when a calendar's year value has changed.
     *
     * @param calendar     The calendar that changed.
     * @param previousYear The previously selected year.
     */
    default void yearChanged(Calendar calendar, int previousYear) {
    }

    /**
     * Called when a calendar's month value has changed.
     *
     * @param calendar      The calendar that changed.
     * @param previousMonth The previously selected month value.
     */
    default void monthChanged(Calendar calendar, int previousMonth) {
    }

    /**
     * Called when a calendar's locale has changed.
     *
     * @param calendar       The calendar that changed.
     * @param previousLocale The previously selected locale for the calendar.
     */
    default void localeChanged(Calendar calendar, Locale previousLocale) {
    }

    /**
     * Called when a calendar's disabled date filter has changed.
     *
     * @param calendar                   The calendar that changed.
     * @param previousDisabledDateFilter The previous disabled date filter.
     */
    default void disabledDateFilterChanged(Calendar calendar,
        Filter<CalendarDate> previousDisabledDateFilter) {
    }
}
