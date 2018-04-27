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

import org.apache.pivot.util.ListenerList;

/**
 * Calendar binding listener interface.
 */
public interface CalendarBindingListener {
    /**
     * Calendar binding listeners.
     */
    public static class Listeners extends ListenerList<CalendarBindingListener>
        implements CalendarBindingListener {
        @Override
        public void selectedDateKeyChanged(Calendar calendar, String previousSelectedDateKey) {
            forEach(listener -> listener.selectedDateKeyChanged(calendar, previousSelectedDateKey));
        }

        @Override
        public void selectedDateBindTypeChanged(Calendar calendar,
            BindType previousSelectedDateBindType) {
            forEach(listener -> listener.selectedDateBindTypeChanged(calendar, previousSelectedDateBindType));
        }

        @Override
        public void selectedDateBindMappingChanged(Calendar calendar,
            Calendar.SelectedDateBindMapping previousSelectedDateBindMapping) {
            forEach(listener -> listener.selectedDateBindMappingChanged(calendar, previousSelectedDateBindMapping));
        }
    }

    /**
     * Calendar binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements CalendarBindingListener {
        @Override
        public void selectedDateKeyChanged(Calendar calendar, String previousSelectedDateKey) {
            // empty block
        }

        @Override
        public void selectedDateBindTypeChanged(Calendar calendar,
            BindType previousSelectedDateBindType) {
            // empty block
        }

        @Override
        public void selectedDateBindMappingChanged(Calendar calendar,
            Calendar.SelectedDateBindMapping previousSelectedDateBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a calendar's selected date key has changed.
     *
     * @param calendar                The calendar that was updated.
     * @param previousSelectedDateKey The previous bind key for the calendar's selected date.
     */
    default void selectedDateKeyChanged(Calendar calendar, String previousSelectedDateKey) {
    }

    /**
     * Called when a calendar's selected date bind type has changed.
     *
     * @param calendar                     The calendar that has changed.
     * @param previousSelectedDateBindType The previous bind type for the selected date.
     */
    default void selectedDateBindTypeChanged(Calendar calendar, BindType previousSelectedDateBindType) {
    }

    /**
     * Called when a calendar's selected date bind mapping has changed.
     *
     * @param calendar                        The calendar that changed.
     * @param previousSelectedDateBindMapping The previous bind mapping for the selected date.
     */
    default void selectedDateBindMappingChanged(Calendar calendar,
        Calendar.SelectedDateBindMapping previousSelectedDateBindMapping) {
    }

}
