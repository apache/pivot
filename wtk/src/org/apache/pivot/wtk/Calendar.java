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
import org.apache.pivot.util.Utils;

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
         * @param value The value retrieved from the bound object.
         * @return The value converted to a calendar date.
         */
        public CalendarDate toDate(Object value);

        /**
         * Converts a calendar date to a context value.
         *
         * @param calendarDate The current calendar date value from the component.
         * @return The converted object value suitable for persistence in the bound object.
         */
        public Object valueOf(CalendarDate calendarDate);
    }

    private int year;
    private int month;

    private CalendarDate selectedDate = null;
    private Locale locale = Locale.getDefault();
    private Filter<CalendarDate> disabledDateFilter = null;

    private String selectedDateKey = null;
    private BindType selectedDateBindType = BindType.BOTH;
    private SelectedDateBindMapping selectedDateBindMapping = null;

    private CalendarListener.Listeners calendarListeners = new CalendarListener.Listeners();
    private CalendarSelectionListener.Listeners calendarSelectionListeners = new CalendarSelectionListener.Listeners();
    private CalendarBindingListener.Listeners calendarBindingListeners = new CalendarBindingListener.Listeners();

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
     *
     * @return The current year value.
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets this calendar's year.
     *
     * @param year The new year to set this calendar to.
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
     *
     * @return The current month value.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets this calendar's month.
     *
     * @param month The new month value to set this calendar to.
     */
    public void setMonth(int month) {
        int previousMonth = this.month;

        if (previousMonth != month) {
            this.month = month;
            calendarListeners.monthChanged(this, previousMonth);
        }
    }

    /**
     * @return The currently selected date, or <tt>null</tt> if no date is
     * selected.
     */
    public CalendarDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets the currently selected date.
     *
     * @param selectedDate The selected date, or <tt>null</tt> to specify no
     * selection
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
     * @param selectedDate A string in the form of <tt>[YYYY]-[MM]-[DD]</tt>
     * (e.g. 2008-07-23)
     * @throws IllegalArgumentException if the given date is {@code null}.
     */
    public final void setSelectedDate(String selectedDate) {
        setSelectedDate(CalendarDate.decode(selectedDate));
    }

    /**
     * @return The locale used to present calendar data.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale The new locale for this calendar.
     * @throws IllegalArgumentException if the locale argument is {@code null}.
     */
    public void setLocale(Locale locale) {
        Utils.checkNull(locale, "locale");

        Locale previousLocale = this.locale;
        if (previousLocale != locale) {
            this.locale = locale;
            calendarListeners.localeChanged(this, previousLocale);
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale An dictionary containing values for language, country, and
     * variant. Country and variant are optional but the must adhere to the
     * following rules: <ul> <li>If variant is specified, language and country
     * are required;</li> <li>Otherwise, if country is specified, language is
     * required;</li> <li>Otherwise, language is required.</li> </ul>
     * @throws IllegalArgumentException if the given locale dictionary is {@code null}.
     */
    public void setLocale(Dictionary<String, ?> locale) {
        Utils.checkNull(locale, "locale");

        String language = (String) locale.get(LANGUAGE_KEY);
        String country = (String) locale.get(COUNTRY_KEY);
        String variant = (String) locale.get(VARIANT_KEY);

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
     * @param locale A JSON map containing values for language, country, and
     * variant.
     * @throws IllegalArgumentException if the locale string is {@code null}
     * or if it cannot be parsed successfully.
     * @see #setLocale(Dictionary)
     */
    public void setLocale(String locale) {
        Utils.checkNullOrEmpty(locale, "locale");

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
     *
     * @return The current value of the selected date binding key.
     */
    public String getSelectedDateKey() {
        return selectedDateKey;
    }

    /**
     * Sets this calendar's data binding key.
     *
     * @param selectedDateKey The new key to use for binding to the selected date.
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
        Utils.checkNull(selectedDateBindType, "selectedDateBindType");

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
            calendarBindingListeners.selectedDateBindMappingChanged(this,
                previousSelectedDateBindMapping);
        }
    }

    @Override
    public void load(Object context) {
        if (selectedDateKey != null && JSON.containsKey(context, selectedDateKey)
            && selectedDateBindType != BindType.STORE) {
            Object value = JSON.get(context, selectedDateKey);

            CalendarDate selectedDateLocal = null;

            if (value instanceof CalendarDate) {
                selectedDateLocal = (CalendarDate) value;
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
        if (selectedDateKey != null && selectedDateBindType != BindType.LOAD) {
            JSON.put(context, selectedDateKey, (selectedDateBindMapping == null) ? selectedDate
                : selectedDateBindMapping.valueOf(selectedDate));
        }
    }

    @Override
    public void clear() {
        if (selectedDateKey != null) {
            setSelectedDate((CalendarDate) null);
        }
    }

    /**
     * @return The calendar listener list.
     */
    public ListenerList<CalendarListener> getCalendarListeners() {
        return calendarListeners;
    }

    /**
     * @return The calendar selection listener list.
     */
    public ListenerList<CalendarSelectionListener> getCalendarSelectionListeners() {
        return calendarSelectionListeners;
    }

    /**
     * @return The calendar binding listener list.
     */
    public ListenerList<CalendarBindingListener> getCalendarBindingListeners() {
        return calendarBindingListeners;
    }
}
