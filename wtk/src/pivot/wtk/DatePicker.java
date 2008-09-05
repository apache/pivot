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

import java.util.Calendar;
import java.util.GregorianCalendar;

import pivot.collections.Dictionary;
import pivot.util.CalendarDate;
import pivot.util.ListenerList;

/**
 * Displays a calendar, optionally allowing the user to select a date.
 *
 * @author tvolkert
 */
public class DatePicker extends Container {
    /**
     * Date picker listener list.
     *
     * @author tvolkert
     */
    private static class DatePickerListenerList extends ListenerList<DatePickerListener>
        implements DatePickerListener {

        public void yearChanged(DatePicker datePicker, int previousYear) {
            for (DatePickerListener listener : this) {
                listener.yearChanged(datePicker, previousYear);
            }
        }

        public void monthChanged(DatePicker datePicker, int previousMonth) {
            for (DatePickerListener listener : this) {
                listener.monthChanged(datePicker, previousMonth);
            }
        }

        public void selectedDateKeyChanged(DatePicker datePicker,
            String previousSelectedDateKey) {
            for (DatePickerListener listener : this) {
                listener.selectedDateKeyChanged(datePicker, previousSelectedDateKey);
            }
        }
    }

    /**
     * Date picker selection listener list.
     *
     * @author tvolkert
     */
    private static class DatePickerSelectionListenerList
        extends ListenerList<DatePickerSelectionListener>
        implements DatePickerSelectionListener {

        public void selectedDateChanged(DatePicker datePicker,
            CalendarDate previousSelectedDate) {
            for (DatePickerSelectionListener listener : this) {
                listener.selectedDateChanged(datePicker, previousSelectedDate);
            }
        }
    }

    private int year;
    private int month;

    private CalendarDate selectedDate = null;

    private String selectedDateKey = null;

    private DatePickerListenerList datePickerListeners = new DatePickerListenerList();
    private DatePickerSelectionListenerList datePickerSelectionListeners =
        new DatePickerSelectionListenerList();

    /**
     * Creates a date picker set to the current month and year in the default
     * timezone in the default locale.
     */
    public DatePicker() {
        this(new GregorianCalendar());
    }

    /**
     * Creates a new date picker set to the specified calendar's year and
     * month.
     */
    private DatePicker(Calendar calendar) {
        this(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    }

    /**
     * Creates a new date picker set to the specified year and month.
     *
     * @param year
     * The year (e.g. 2008)
     * @param month
     * The month, 0-based (e.g. 2 for March)
     */
    public DatePicker(int year, int month) {
        this.year = year;
        this.month = month;

        installSkin(DatePicker.class);
    }

    /**
     * Gets the year to which this date picker is currently set.
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets this date picker's year.
     */
    public void setYear(int year) {
        int previousYear = this.year;

        if (previousYear != year) {
            this.year = year;
            datePickerListeners.yearChanged(this, previousYear);
        }
    }

    /**
     * Gets the month to which this date picker is currently set.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets this date picker's month.
     */
    public void setMonth(int month) {
        int previousMonth = this.month;

        if (previousMonth != month) {
            this.month = month;
            datePickerListeners.monthChanged(this, previousMonth);
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

        if ((selectedDate == null ^ previousSelectedDate == null)
            || (selectedDate != null && !selectedDate.equals(previousSelectedDate))) {
            this.selectedDate = selectedDate;
            datePickerSelectionListeners.selectedDateChanged(this, previousSelectedDate);
        }
    }

    /**
     * Sets the selected date to the date represented by the specified date
     * string. The date string must be in the <tt>ISO 8601</tt> "calendar date"
     * format, which is <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param date
     * A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g. 2008-07-23)
     */
    public final void setSelectedDate(String selectedDate) {
        if (selectedDate == null) {
            throw new IllegalArgumentException("selectedDate is null.");
        }

        setSelectedDate(new CalendarDate(selectedDate));
    }

    /**
     * Gets the data binding key that is set on this date picker.
     */
    public String getSelectedDateKey() {
        return selectedDateKey;
    }

    /**
     * Sets this date picker's data binding key.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        String previousSelectedDateKey = this.selectedDateKey;

        if ((selectedDateKey == null ^ previousSelectedDateKey == null)
            || (selectedDateKey != null && !selectedDateKey.equals(previousSelectedDateKey))) {
            this.selectedDateKey = selectedDateKey;
            datePickerListeners.selectedDateKeyChanged(this, previousSelectedDateKey);
        }
    }

    /**
     * Loads the selected date from the specified bind context using this date
     * picker's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, Object> context) {
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
    public void store(Dictionary<String, Object> context) {
        if (selectedDateKey != null) {
            context.put(selectedDateKey, selectedDate);
        }
    }

    /**
     * Gets this date picker's <tt>DatePickerListener</tt> collection.
     */
    public ListenerList<DatePickerListener> getDatePickerListeners() {
        return datePickerListeners;
    }

    /**
     * Gets this date picker's <tt>DatePickerSelectionListener</tt> collection.
     */
    public ListenerList<DatePickerSelectionListener> getDatePickerSelectionListeners() {
        return datePickerSelectionListeners;
    }
}
