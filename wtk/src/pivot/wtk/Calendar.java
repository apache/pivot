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
package pivot.wtk;

import java.util.Locale;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.ListenerList;


/**
 * Component that allows the user to select a date.
 *
 * @author tvolkert
 * @author gbrown
 */
public class Calendar extends Container {
    /**
     * Calendar listener list.
     *
     * @author tvolkert
     */
    private static class CalendarListenerList extends ListenerList<CalendarListener>
        implements CalendarListener {

        public void yearChanged(Calendar calendar, int previousYear) {
            for (CalendarListener listener : this) {
                listener.yearChanged(calendar, previousYear);
            }
        }

        public void monthChanged(Calendar calendar, int previousMonth) {
            for (CalendarListener listener : this) {
                listener.monthChanged(calendar, previousMonth);
            }
        }

        public void selectedDateKeyChanged(Calendar calendar,
            String previousSelectedDateKey) {
            for (CalendarListener listener : this) {
                listener.selectedDateKeyChanged(calendar, previousSelectedDateKey);
            }
        }

        public void localeChanged(Calendar calendar, Locale previousLocale) {
            for (CalendarListener listener : this) {
                listener.localeChanged(calendar, previousLocale);
            }
        }
    }

    /**
     * Calendar selection listener list.
     *
     * @author tvolkert
     */
    private static class CalendarSelectionListenerList
        extends ListenerList<CalendarSelectionListener>
        implements CalendarSelectionListener {

        public void selectedDateChanged(Calendar calendar,
            CalendarDate previousSelectedDate) {
            for (CalendarSelectionListener listener : this) {
                listener.selectedDateChanged(calendar, previousSelectedDate);
            }
        }
    }

    private int year;
    private int month;

    private CalendarDate selectedDate = null;
    private String selectedDateKey = null;
    private Locale locale = Locale.getDefault();

    private CalendarListenerList calendarListeners = new CalendarListenerList();
    private CalendarSelectionListenerList calendarSelectionListeners =
        new CalendarSelectionListenerList();

    public static final String LANGUAGE_KEY = "language";
    public static final String COUNTRY_KEY = "country";
    public static final String VARIANT_KEY = "variant";

    public Calendar() {
        this(new CalendarDate());
    }

    private Calendar(CalendarDate calendarDate) {
        this(calendarDate.getYear(), calendarDate.getMonth());
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

        if (((previousSelectedDate == null) ^ (selectedDate == null))
            || (previousSelectedDate != null
                && !previousSelectedDate.equals(selectedDate))) {
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

        setSelectedDate(new CalendarDate(selectedDate));
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

        if ((selectedDateKey == null ^ previousSelectedDateKey == null)
            || (selectedDateKey != null && !selectedDateKey.equals(previousSelectedDateKey))) {
            this.selectedDateKey = selectedDateKey;
            calendarListeners.selectedDateKeyChanged(this, previousSelectedDateKey);
        }
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

        setLocale(JSONSerializer.parseMap(locale));
    }

    /**
     * Loads the selected date from the specified bind context using this date
     * picker's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, ?> context) {
        if (selectedDateKey != null
            && context.containsKey(selectedDateKey)) {
            Object value = context.get(selectedDateKey);

            if (value instanceof CalendarDate) {
                setSelectedDate((CalendarDate)value);
            } else if (value instanceof String) {
                setSelectedDate((String)value);
            } else {
                throw new IllegalArgumentException("Invalid date type: " +
                    value.getClass().getName());
            }
        }
    }

    /**
     * Stores the selected date into the specified bind context using this date
     * picker's bind key, if one is set.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void store(Dictionary<String, ?> context) {
        if (selectedDateKey != null) {
            ((Dictionary<String, CalendarDate>)context).put(selectedDateKey, selectedDate);
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
}
