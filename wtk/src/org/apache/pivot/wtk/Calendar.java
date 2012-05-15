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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * Component that allows the user to select a date.
 */
public class Calendar extends Container {
    /**
     * Translates between calendar date and context data during data binding.
     */
    public interface SelectedDateBindMapping {
        /**
         * Converts a context value to a calendar date.
         *
         * @param value
         */
        public CalendarDate toDate(Object value);

        /**
         * Converts a calendar date to a context value.
         *
         * @param calendarDate
         */
        public Object valueOf(CalendarDate calendarDate);
    }

    private static class CalendarListenerList extends WTKListenerList<CalendarListener>
        implements CalendarListener {
        @Override
        public void yearChanged(Calendar calendar, int previousYear) {
            for (CalendarListener listener : this) {
                listener.yearChanged(calendar, previousYear);
            }
        }

        @Override
        public void monthChanged(Calendar calendar, int previousMonth) {
            for (CalendarListener listener : this) {
                listener.monthChanged(calendar, previousMonth);
            }
        }

        @Override
        public void localeChanged(Calendar calendar, Locale previousLocale) {
            for (CalendarListener listener : this) {
                listener.localeChanged(calendar, previousLocale);
            }
        }

        @Override
        public void disabledDateFilterChanged(Calendar calendar, Filter<CalendarDate> previousDisabledDateFilter) {
            for (CalendarListener listener : this) {
                listener.disabledDateFilterChanged(calendar, previousDisabledDateFilter);
            }
        }
    }

    private static class CalendarSelectionListenerList extends WTKListenerList<CalendarSelectionListener>
        implements CalendarSelectionListener {

        @Override
        public void selectedDateChanged(Calendar calendar,
            CalendarDate previousSelectedDate) {
            for (CalendarSelectionListener listener : this) {
                listener.selectedDateChanged(calendar, previousSelectedDate);
            }
        }
    }

    private static class CalendarBindingListenerList extends WTKListenerList<CalendarBindingListener>
        implements CalendarBindingListener {
        @Override
        public void selectedDateKeyChanged(Calendar calendar,
            String previousSelectedDateKey) {
            for (CalendarBindingListener listener : this) {
                listener.selectedDateKeyChanged(calendar, previousSelectedDateKey);
            }
        }

        @Override
        public void selectedDateBindTypeChanged(Calendar calendar, BindType previousSelectedDateBindType) {
            for (CalendarBindingListener listener : this) {
                listener.selectedDateBindTypeChanged(calendar, previousSelectedDateBindType);
            }
        }

        @Override
        public void selectedDateBindMappingChanged(Calendar calendar, SelectedDateBindMapping previousSelectedDateBindMapping) {
            for (CalendarBindingListener listener : this) {
                listener.selectedDateBindMappingChanged(calendar, previousSelectedDateBindMapping);
            }
        }
    }

    private int year;
    private int month;

    private CalendarDate selectedDate = null;
    private Locale locale = Locale.getDefault();
    private Filter<CalendarDate> disabledDateFilter = null;

    private String selectedDateKey = null;
    private BindType selectedDateBindType = BindType.BOTH;
    private SelectedDateBindMapping selectedDateBindMapping = null;

    private CalendarListenerList calendarListeners = new CalendarListenerList();
    private CalendarSelectionListenerList calendarSelectionListeners =
        new CalendarSelectionListenerList();
    private CalendarBindingListenerList calendarBindingListeners = new CalendarBindingListenerList();

    public static final String LANGUAGE_KEY = "language";
    public static final String COUNTRY_KEY = "country";
    public static final String VARIANT_KEY = "variant";

    public Calendar() {
        this(new CalendarDate());
    }

    private Calendar(CalendarDate calendarDate) {
        this(calendarDate.year, calendarDate.month);
    }

    public Calendar(int year, int month) {
        this.year = year;
        this.month = month;

        installSkin(Calendar.class);
    }

    /**
     * Gets the year to which this calendar is currently set.
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets this calendar's year.
     */
    public void setYear(int year) {
        int previousYear = this.year;

        if (previousYear != year) {
            this.year = year;
            calendarListeners.yearChanged(this, previousYear);
        }
    }

    /**
     * Gets the month to which this calendar is currently set.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets this calendar's month.
     */
    public void setMonth(int month) {
        int previousMonth = this.month;

        if (previousMonth != month) {
            this.month = month;
            calendarListeners.monthChanged(this, previousMonth);
        }
    }

    /**
     * Gets the currently selected date, or <tt>null</tt> if no date is
     * selected.
     */
    public CalendarDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets the currently selected date.
     *
     * @param selectedDate
     * The selected date, or <tt>null</tt> to specify no selection
     */
    public void setSelectedDate(CalendarDate selectedDate) {
        CalendarDate previousSelectedDate = this.selectedDate;

        if (previousSelectedDate != selectedDate) {
            this.selectedDate = selectedDate;
            calendarSelectionListeners.selectedDateChanged(this, previousSelectedDate);
        }
    }

    /**
     * Sets the selected date to the date represented by the specified date
     * string. The date string must be in the <tt>ISO 8601</tt> "calendar date"
     * format, which is <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param selectedDate
     * A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g. 2008-07-23)
     */
    public final void setSelectedDate(String selectedDate) {
        if (selectedDate == null) {
            throw new IllegalArgumentException("selectedDate is null.");
        }

        setSelectedDate(CalendarDate.decode(selectedDate));
    }

    /**
     * Returns the locale used to present calendar data.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale
     */
    public void setLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        Locale previousLocale = this.locale;
        if (previousLocale != locale) {
            this.locale = locale;
            calendarListeners.localeChanged(this, previousLocale);
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale
     * An dictionary containing values for language, country, and variant.
     * Country and variant are optional but the must adhere to the following
     * rules:
     *
     * <ul>
     * <li>If variant is specified, language and country are required;</li>
     * <li>Otherwise, if country is specified, language is required;</li>
     * <li>Otherwise, language is required.</li>
     * </ul>
     */
    public void setLocale(Dictionary<String, ?> locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        String language = (String)locale.get(LANGUAGE_KEY);
        String country = (String)locale.get(COUNTRY_KEY);
        String variant = (String)locale.get(VARIANT_KEY);

        if (variant != null) {
            setLocale(new Locale(language, country, variant));
        } else if (country != null) {
            setLocale(new Locale(language, country));
        } else {
            setLocale(new Locale(language));
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale
     * A JSON map containing values for language, country, and variant.
     *
     * @see #setLocale(Dictionary)
     */
    public void setLocale(String locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale is null.");
        }

        try {
            setLocale(JSONSerializer.parseMap(locale));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public Filter<CalendarDate> getDisabledDateFilter() {
        return disabledDateFilter;
    }

    public void setDisabledDateFilter(Filter<CalendarDate> disabledDateFilter) {
        Filter<CalendarDate> previousDisabledDateFilter = this.disabledDateFilter;

        if (previousDisabledDateFilter != disabledDateFilter) {
            this.disabledDateFilter = disabledDateFilter;
            calendarListeners.disabledDateFilterChanged(this, previousDisabledDateFilter);
        }
    }

    /**
     * Gets the data binding key that is set on this calendar.
     */
    public String getSelectedDateKey() {
        return selectedDateKey;
    }

    /**
     * Sets this calendar's data binding key.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        String previousSelectedDateKey = this.selectedDateKey;

        if (selectedDateKey != previousSelectedDateKey) {
            this.selectedDateKey = selectedDateKey;
            calendarBindingListeners.selectedDateKeyChanged(this, previousSelectedDateKey);
        }
    }

    public BindType getSelectedDateBindType() {
        return selectedDateBindType;
    }

    public void setSelectedDateBindType(BindType selectedDateBindType) {
        if (selectedDateBindType == null) {
            throw new IllegalArgumentException();
        }

        BindType previousSelectedDateBindType = this.selectedDateBindType;

        if (previousSelectedDateBindType != selectedDateBindType) {
            this.selectedDateBindType = selectedDateBindType;
            calendarBindingListeners.selectedDateBindTypeChanged(this, previousSelectedDateBindType);
        }
    }

    public SelectedDateBindMapping getSelectedDateBindMapping() {
        return selectedDateBindMapping;
    }

    public void setSelectedDateBindMapping(SelectedDateBindMapping selectedDateBindMapping) {
        SelectedDateBindMapping previousSelectedDateBindMapping = this.selectedDateBindMapping;

        if (previousSelectedDateBindMapping != selectedDateBindMapping) {
            this.selectedDateBindMapping = selectedDateBindMapping;
            calendarBindingListeners.selectedDateBindMappingChanged(this, previousSelectedDateBindMapping);
        }
    }

    @Override
    public void load(Object context) {
        if (selectedDateKey != null
            && JSON.containsKey(context, selectedDateKey)
            && selectedDateBindType != BindType.STORE) {
            Object value = JSON.get(context, selectedDateKey);

            CalendarDate selectedDateLocal = null;

            if (value instanceof CalendarDate) {
                selectedDateLocal = (CalendarDate)value;
            } else if (selectedDateBindMapping == null) {
                if (value != null) {
                    selectedDateLocal = CalendarDate.decode(value.toString());
                }
            } else {
                selectedDateLocal = selectedDateBindMapping.toDate(value);
            }

            setSelectedDate(selectedDateLocal);
        }
    }

    @Override
    public void store(Object context) {
        if (selectedDateKey != null
            && selectedDateBindType != BindType.LOAD) {
            JSON.put(context, selectedDateKey, (selectedDateBindMapping == null) ?
                selectedDate : selectedDateBindMapping.valueOf(selectedDate));
        }
    }

    @Override
    public void clear() {
        if (selectedDateKey != null) {
            setSelectedDate((CalendarDate)null);
        }
    }

    /**
     * Returns the calendar listener list.
     */
    public ListenerList<CalendarListener> getCalendarListeners() {
        return calendarListeners;
    }

    /**
     * Returns the calendar selection listener list.
     */
    public ListenerList<CalendarSelectionListener> getCalendarSelectionListeners() {
        return calendarSelectionListeners;
    }

    /**
     * Returns the calendar binding listener list.
     */
    public ListenerList<CalendarBindingListener> getCalendarBindingListeners() {
        return calendarBindingListeners;
    }
}
