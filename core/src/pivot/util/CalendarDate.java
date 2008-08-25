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
package pivot.util;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO Manually implement internal functionality that currently delegates
 * to GregorianCalendar to gain performance.
 *
 * @author tvolkert
 */
public class CalendarDate implements Comparable<CalendarDate>, Serializable {
    public static final long serialVersionUID = 0;

    private int year;
    private int month;
    private int day;

    private static final int[] MONTH_LENGTHS = {
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static final int GREGORIAN_CUTOVER_YEAR = 1582;

    /**
     * Creates a new <tt>CalendarDate</tt> representing the current day in the
     * default timezone and the default locale.
     */
    public CalendarDate() {
        this(new GregorianCalendar());
    }

    /**
     * Creates a new <tt>CalendarDate</tt> representing the day contained in
     * the specified gregorian calendar (assuming the default locale and the
     * default timezone).
     *
     * @param calendar
     * The calendar containing the year, month, and day fields.
     */
    public CalendarDate(GregorianCalendar calendar) {
        this(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH) - 1);
    }

    /**
     * Creates a new <tt>CalendarDate</tt> representing the specified year,
     * month, and day of month.
     *
     * @param year
     * The year field (e.g. <tt>2008</tt>)
     * @param month
     * The month field, 0-based (e.g. <tt>2</tt> for March)
     * @param day
     * The day of the month, 0-based (e.g. <tt>14</tt> for the 15th)
     */
    public CalendarDate(int year, int month, int day) {
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
    public CalendarDate(String date) {
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
        if (year <= GREGORIAN_CUTOVER_YEAR || year > 9999) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }

        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }

        int daysInMonth = MONTH_LENGTHS[month];

        boolean isLeapYear = ((year & 3) == 0 && (year % 100 != 0 || year % 400 == 0));
        if (isLeapYear && month == 1) {
            daysInMonth++;
        }

        if (day < 0 || day >= daysInMonth) {
            throw new IllegalArgumentException("Invalid day: " + day);
        }

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
     * Compares this calendar date with another calendar date.
     */
    public int compareTo(CalendarDate calendarDate) {
        int result = year - calendarDate.year;

        if (result == 0) {
            result = month - calendarDate.month;

            if (result == 0) {
                result = day - calendarDate.day;
            }
        }

        return result;
    }

    /**
     * Adds the specified number of days to this calendar date and returns the
     * resulting calendar date. The number of days may be negative, in which
     * case the result will be a date before this calendar date.
     *
     * @param days
     * The number of days to add to (or subtract from if negative) this
     * calendar date
     *
     * @return
     * The resulting calendar date
     */
    public CalendarDate add(int days) {
        GregorianCalendar calendar = toCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return new CalendarDate(calendar);
    }

    /**
     * Gets the number of days in between this calendar date and the specified
     * calendar date. If this calendar date represents a day after the
     * specified calendar date, the difference will be positive. If this
     * calendardate represents a day before the specified calendar date, the
     * difference will be negative. If the two calendar dates represent the
     * same day, the difference will be zero.
     */
    public int subtract(CalendarDate calendarDate) {
        GregorianCalendar c1 = toCalendar();
        GregorianCalendar c2 = calendarDate.toCalendar();

        long t1 = c1.getTimeInMillis();
        long t2 = c2.getTimeInMillis();

        return (int)((t1 - t2) / (1000l * 60 * 60 * 24));
    }

    /**
     * Translates this calendar date to an instance of
     * <tt>GregorianCalendar</tt>, with the <tt>year</tt>, <tt>month</tt>, and
     * <tt>dayOfMonth</tt> fields set in the default time zone with the default
     * locale.
     *
     * @return
     * This calendar date as a <tt>GregorianCalendar</tt>
     */
    public GregorianCalendar toCalendar() {
        return new GregorianCalendar(year, month, day + 1);
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
        return (o instanceof CalendarDate
            && ((CalendarDate)o).year == year
            && ((CalendarDate)o).month == month
            && ((CalendarDate)o).day == day);
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        Integer hashKey = year + month + day;
        return hashKey.hashCode();
    }

    /**
     * Returns a string representation of this calendar date in the <tt>ISO
     * 8601</tt> "calendar date" format, which is <tt>[YYYY]-[MM]-[DD]</tt>.
     */
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
