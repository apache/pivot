/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.Dictionary;
import pivot.serialization.JSONSerializer;
import pivot.util.CalendarDate;
import pivot.util.ListenerList;
import pivot.wtk.content.CalendarButtonDataRenderer;

/**
 * A component that allows a user to select a calendar date. The calendar
 * is hidden until the user pushes the button.
 *
 * @author tvolkert
 * @author gbrown
 */
public class CalendarButton extends Button {
    /**
     * Calendar button listener list.
     *
     * @author tvolkert
     */
    private static class CalendarButtonListenerList
        extends ListenerList<CalendarButtonListener>
        implements CalendarButtonListener {

        public void yearChanged(CalendarButton calendarButton, int previousYear) {
            for (CalendarButtonListener listener : this) {
                listener.yearChanged(calendarButton, previousYear);
            }
        }

        public void monthChanged(CalendarButton calendarButton, int previousMonth) {
            for (CalendarButtonListener listener : this) {
                listener.monthChanged(calendarButton, previousMonth);
            }
        }

        public void selectedDateKeyChanged(CalendarButton calendarButton,
            String previousSelectedDateKey) {
            for (CalendarButtonListener listener : this) {
                listener.selectedDateKeyChanged(calendarButton, previousSelectedDateKey);
            }
        }

        public void localeChanged(CalendarButton calendarButton, Locale previousLocale) {
            for (CalendarButtonListener listener : this) {
                listener.localeChanged(calendarButton, previousLocale);
            }
        }
    }

    /**
     * Calendar button selection listener list.
     *
     * @author tvolkert
     */
    private static class CalendarButtonSelectionListenerList
        extends ListenerList<CalendarButtonSelectionListener>
        implements CalendarButtonSelectionListener {

        public void selectedDateChanged(CalendarButton calendarButton,
            CalendarDate previousSelectedDate) {
            for (CalendarButtonSelectionListener listener : this) {
                listener.selectedDateChanged(calendarButton, previousSelectedDate);
            }
        }
    }

    private int year;
    private int month;

    private CalendarDate selectedDate = null;
    private String selectedDateKey = null;
    private Locale locale = Locale.getDefault();

    private CalendarButtonListenerList calendarButtonListeners =
        new CalendarButtonListenerList();
    private CalendarButtonSelectionListenerList calendarButtonSelectionListeners =
        new CalendarButtonSelectionListenerList();

    public static final String LANGUAGE_KEY = "language";
    public static final String COUNTRY_KEY = "country";
    public static final String VARIANT_KEY = "variant";

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new CalendarButtonDataRenderer();

    public CalendarButton() {
        this(null, new CalendarDate());
    }

    public CalendarButton(Object buttonData) {
        this(buttonData, new CalendarDate());
    }

    public CalendarButton(CalendarDate calendarDate) {
        this(null, calendarDate);
    }

    public CalendarButton(int year, int month) {
        this(null, year, month);
    }

    public CalendarButton(Object buttonData, CalendarDate calendarDate) {
        this(buttonData, calendarDate.getYear(), calendarDate.getMonth());
    }

    public CalendarButton(Object buttonData, int year, int month) {
        super(buttonData);

        this.year = year;
        this.month = month;

        setDataRenderer(DEFAULT_DATA_RENDERER);

        installSkin(CalendarButton.class);
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported by CalendarButton.
     */
    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Calendar buttons cannot be toggle buttons.");
    }

    /**
     * Returns the year associated with this calendar button.
     *
     * @return
     * The calendar year.
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets the year associated with this calendar button.
     * <p>
     * Fires {@link CalendarButtonListener#yearChanged(CalendarButton, int)}.
     *
     * @param year
     * The year
     */
    public void setYear(int year) {
        int previousYear = this.year;

        if (previousYear != year) {
            this.year = year;
            calendarButtonListeners.yearChanged(this, previousYear);
        }
    }

    /**
     * Returns the month associated with this calendar button.
     *
     * @return
     * The calendar month.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets the month associated with this calendar button.
     * <p>
     * Fires {@link CalendarButtonListener#monthChanged(CalendarButton, int)}.
     *
     * @param month
     * The month
     */
    public void setMonth(int month) {
        int previousMonth = this.month;

        if (previousMonth != month) {
            this.month = month;
            calendarButtonListeners.monthChanged(this, previousMonth);
        }
    }

    /**
     * Returns the currently selected date.
     *
     * @return
     * The currently selected date, or <tt>null</tt> if nothing is selected.
     */
    public CalendarDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets the selected date.
     *
     * @param selectedDate
     * The date to select, or <tt>null</tt> to clear the selection.
     */
    public void setSelectedDate(CalendarDate selectedDate) {
        CalendarDate previousSelectedDate = this.selectedDate;

        if (previousSelectedDate != selectedDate) {
            this.selectedDate = selectedDate;
            calendarButtonSelectionListeners.selectedDateChanged(this,
                previousSelectedDate);
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
     * Gets the data binding key that is set on this calendar button.
     */
    public String getSelectedDateKey() {
        return selectedDateKey;
    }

    /**
     * Sets this calendar button's data binding key.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        String previousSelectedDateKey = this.selectedDateKey;

        if (previousSelectedDateKey != selectedDateKey) {
            this.selectedDateKey = selectedDateKey;
            calendarButtonListeners.selectedDateKeyChanged(this,
                previousSelectedDateKey);
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
            calendarButtonListeners.localeChanged(this, previousLocale);
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
     * picker button's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, Object> context) {
        String selectedDateKey = getSelectedDateKey();

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
     * picker button's bind key, if one is set.
     */
    @Override
    public void store(Dictionary<String, Object> context) {
        String selectedDateKey = getSelectedDateKey();

        if (selectedDateKey != null) {
            context.put(selectedDateKey, getSelectedDate());
        }
    }

    /**
     * Returns the calendar button listener list.
     */
    public ListenerList<CalendarButtonListener> getCalendarButtonListeners() {
        return calendarButtonListeners;
    }

    /**
     * Adds a listener to the calendar button listener list.
     *
     * @param listener
     */
    public void setCalendarButtonListener(CalendarButtonListener listener) {
        calendarButtonListeners.add(listener);
    }

    /**
     * Returns the calendar button selection listener list.
     */
    public ListenerList<CalendarButtonSelectionListener> getCalendarButtonSelectionListeners() {
        return calendarButtonSelectionListeners;
    }

    /**
     * Adds a listener to the calendar button selection listener list.
     *
     * @param listener
     */
    public void setCalendarButtonSelectionListener(CalendarButtonSelectionListener listener) {
        calendarButtonSelectionListeners.add(listener);
    }
}
