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

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pivot.collections.Dictionary;
import pivot.util.ListenerList;

/**
 * Displays a calendar, optionally allowing the user to select a date.
 *
 * @author tvolkert
 */
public class DatePicker extends Container {
    /**
     * Simple "struct" class that represents a specific day capable of being
     * picked by a <tt>DatePicker</tt>.
     *
     * @author tvolkert
     */
    public static final class Date {
        private int year;
        private int month;
        private int day;

        /**
         * Creates a new date representing the current day in the default
         * timezone and the default locale.
         */
        public Date() {
            GregorianCalendar calendar = new GregorianCalendar();
            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH) - 1);
        }

        /**
         * Creates a new date representing the specified year, month, and day
         * of month.
         *
         * @param year
         * The year field (e.g. <tt>2008</tt>)
         * @param month
         * The month field, 0-based (e.g. <tt>2</tt> for March)
         * @param day
         * The day of the month, 0-based (e.g. <tt>14</tt> for the 15th)
         */
        public Date(int year, int month, int day) {
            set(year, month, day);
        }

        /**
         * Creates a new date representing the specified date string. The date
         * string must be in the <tt>ISO 8601</tt> "calendar date" format,
         * which is <tt>[YYYY]-[MM]-[DD]</tt>.
         *
         * @param date
         * A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g. 2008-07-23)
         */
        public Date(String date) {
            Pattern pattern = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");
            Matcher matcher = pattern.matcher(date);

            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid date format: " + date);
            }

            String year = matcher.group(1);
            String month = matcher.group(2);
            String day = matcher.group(3);

            set(Integer.parseInt(year), Integer.parseInt(month) - 1,
                Integer.parseInt(day) - 1);
        }

        /**
         * Sets this date's inner fields.
         */
        private void set(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        /**
         * Gets the year field (e.g. <tt>2008</tt>).
         */
        public int getYear() {
            return year;
        }

        /**
         * Gets the month field, 0-based (e.g. <tt>2</tt> for March).
         */
        public int getMonth() {
            return month;
        }

        /**
         * Gets the day of the month, 0-based (e.g. <tt>14</tt> for the 15th).
         */
        public int getDay() {
            return day;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         * This is the case if the object is a date that represents the same
         * day as this one.
         *
         * @param o
         * Reference to the object against which to compare
         */
        @Override
        public boolean equals(Object o) {
            return (o instanceof Date
                && ((Date)o).year == year
                && ((Date)o).month == month
                && ((Date)o).day == day);
        }

        /**
         * Returns a hash code value for the object.
         */
        @Override
        public int hashCode() {
            Integer hashKey = year + month + day;
            return hashKey.hashCode();
        }

        @Override
        public String toString() {
            StringBuilder buf = new StringBuilder();

            NumberFormat format = NumberFormat.getIntegerInstance();
            format.setGroupingUsed(false);
            format.setMinimumIntegerDigits(4);

            buf.append(format.format(year));
            buf.append("-");

            format.setMinimumIntegerDigits(2);

            buf.append(format.format(month + 1));
            buf.append("-");
            buf.append(format.format(day + 1));

            return buf.toString();
        }
    }

    /**
     * Date picker listener list.
     *
     * @author tvolkert
     */
    private class DatePickerListenerList extends ListenerList<DatePickerListener>
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
    private class DatePickerSelectionListenerList
        extends ListenerList<DatePickerSelectionListener>
        implements DatePickerSelectionListener {

        public void selectedDateChanged(DatePicker datePicker,
            DatePicker.Date previousSelectedDate) {
            for (DatePickerSelectionListener listener : this) {
                listener.selectedDateChanged(datePicker, previousSelectedDate);
            }
        }
    }

    private int year;
    private int month;

    private Date selectedDate = null;

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
    public Date getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets the currently selected date.
     *
     * @param selectedDate
     * The selected date, or <tt>null</tt> to specify no selection
     */
    public void setSelectedDate(Date selectedDate) {
        Date previousSelectedDate = this.selectedDate;

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

        setSelectedDate(new Date(selectedDate));
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

            if (value instanceof Date) {
                setSelectedDate((Date)value);
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
