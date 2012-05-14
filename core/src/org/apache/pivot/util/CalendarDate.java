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
package org.apache.pivot.util;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;

/**
 * <tt>CalendarDate</tt> allows a specific day to be identified within the
 * Gregorian calendar system. This identification has no association with any
 * particular time zone and no notion of the time of day.
 */
public final class CalendarDate implements Comparable<CalendarDate>, Serializable {
    private static final long serialVersionUID = 3974393986540543704L;

    /**
     * Represents a range of calendar dates.
     */
    public static final class Range {
        public static final String START_KEY = "start";
        public static final String END_KEY = "end";

        public final CalendarDate start;
        public final CalendarDate end;

        public Range(CalendarDate calendarDate) {
            this(calendarDate, calendarDate);
        }

        public Range(CalendarDate start, CalendarDate end) {
            this.start = start;
            this.end = end;
        }

        public Range(String start, String end) {
            this.start = CalendarDate.decode(start);
            this.end = CalendarDate.decode(end);
        }

        public Range(Range range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            this.start = range.start;
            this.end = range.end;
        }

        public Range(Dictionary<String, ?> range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            Object startRange = range.get(START_KEY);
            Object endRange = range.get(END_KEY);

            if (startRange == null) {
                throw new IllegalArgumentException(START_KEY + " is required.");
            }

            if (endRange == null) {
                throw new IllegalArgumentException(END_KEY + " is required.");
            }

            if (startRange instanceof String) {
                this.start = CalendarDate.decode((String)startRange);
            } else {
                this.start = (CalendarDate)startRange;
            }

            if (endRange instanceof String) {
                this.end = CalendarDate.decode((String)endRange);
            } else {
                this.end = (CalendarDate)endRange;
            }
        }

        public int getLength() {
            return Math.abs(this.start.subtract(this.end)) + 1;
        }

        public boolean contains(Range range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            Range normalizedRange = range.normalize();

            boolean contains;
            if (this.start.compareTo(this.end) < 0) {
                contains = (this.start.compareTo(normalizedRange.start) <= 0
                    && this.end.compareTo(normalizedRange.end) >= 0);
            } else {
                contains = (this.end.compareTo(normalizedRange.start) <= 0
                    && this.start.compareTo(normalizedRange.end) >= 0);
            }

            return contains;
        }

        public boolean contains(CalendarDate calendarDate) {
            if (calendarDate == null) {
                throw new IllegalArgumentException("calendarDate is null.");
            }

            boolean contains;
            if (this.start.compareTo(this.end) < 0) {
                contains = (this.start.compareTo(calendarDate) <= 0
                    && this.end.compareTo(calendarDate) >= 0);
            } else {
                contains = (this.end.compareTo(calendarDate) <= 0
                    && this.start.compareTo(calendarDate) >= 0);
            }

            return contains;
        }

        public boolean intersects(Range range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            Range normalizedRange = range.normalize();

            boolean intersects;
            if (this.start.compareTo(this.end) < 0) {
                intersects = (this.start.compareTo(normalizedRange.end) <= 0
                    && this.end.compareTo(normalizedRange.start) >= 0);
            } else {
                intersects = (this.end.compareTo(normalizedRange.end) <= 0
                    && this.start.compareTo(normalizedRange.start) >= 0);
            }

            return intersects;
        }

        public Range normalize() {
            CalendarDate earlier = (this.start.compareTo(this.end) < 0 ? this.start : this.end);
            CalendarDate later = (earlier == this.start ? this.end : this.start);
            return new Range(earlier, later);
        }

        public static Range decode(String value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }

            Range range;
            if (value.startsWith("{")) {
                try {
                    range = new Range(JSONSerializer.parseMap(value));
                } catch (SerializationException exception) {
                    throw new IllegalArgumentException(exception);
                }
            } else {
                range = new Range(CalendarDate.decode(value));
            }

            return range;
        }
    }

    /**
     * The year field. (e.g. <tt>2008</tt>).
     */
    public final int year;

    /**
     * The month field, 0-based. (e.g. <tt>2</tt> for March).
     */
    public final int month;

    /**
     * The day of the month, 0-based. (e.g. <tt>14</tt> for the 15th).
     */
    public final int day;

    private static final int[] MONTH_LENGTHS = {
        31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31
    };

    private static final int GREGORIAN_CUTOVER_YEAR = 1582;
    private static final Pattern PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");

    /**
     * Creates a new <tt>CalendarDate</tt> representing the current day in the
     * default timezone and the default locale.
     */
    public CalendarDate() {
        this(new GregorianCalendar());
    }

    /**
     * Creates a new <tt>CalendarDate</tt> representing the day contained in
     * the specified Gregorian calendar (assuming the default locale and the
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
     * The year field. (e.g. <tt>2008</tt>)
     *
     * @param month
     * The month field, 0-based. (e.g. <tt>2</tt> for March)
     *
     * @param day
     * The day of the month, 0-based. (e.g. <tt>14</tt> for the 15th)
     */
    public CalendarDate(int year, int month, int day) {
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
     * Adds the specified number of days to this calendar date and returns the
     * resulting calendar date. The number of days may be negative, in which
     * case the result will be a date before this calendar date.
     * <p>
     * More formally, it is defined that given calendar dates <tt>c1</tt> and
     * <tt>c2</tt>, the following will return <tt>true</tt>:
     * <pre>
     *    c1.add(c2.subtract(c1)).equals(c2);
     * </pre>
     *
     * @param days
     * The number of days to add to (or subtract from if negative) this
     * calendar date.
     *
     * @return
     * The resulting calendar date.
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
     * calendar date represents a day before the specified calendar date, the
     * difference will be negative. If the two calendar dates represent the
     * same day, the difference will be zero.
     * <p>
     * More formally, it is defined that given calendar dates <tt>c1</tt> and
     * <tt>c2</tt>, the following will return <tt>true</tt>:
     * <pre>
     *    c1.add(c2.subtract(c1)).equals(c2);
     * </pre>
     *
     * @param calendarDate
     * The calendar date to subtract from this calendar date.
     *
     * @return
     * The number of days in between this calendar date and
     * <tt>calendarDate</tt>.
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
     * This calendar date as a <tt>GregorianCalendar</tt>.
     */
    public GregorianCalendar toCalendar() {
        return toCalendar(new Time(0, 0, 0));
    }

    /**
     * Translates this calendar date to an instance of
     * <tt>GregorianCalendar</tt>, with the <tt>year</tt>, <tt>month</tt>, and
     * <tt>dayOfMonth</tt> fields set in the default time zone with the default
     * locale.
     *
     * @param time
     * The time of day.
     *
     * @return
     * This calendar date as a <tt>GregorianCalendar</tt>.
     */
    public GregorianCalendar toCalendar(Time time) {
        GregorianCalendar calendar = new GregorianCalendar(this.year, this.month, this.day + 1,
            time.hour, time.minute, time.second);
        calendar.set(Calendar.MILLISECOND, time.millisecond);

        return calendar;
    }

    /**
     * Compares this calendar date with another calendar date.
     *
     * @param calendarDate
     * The calendar date against which to compare.
     *
     * @return
     * A negative number, zero, or a positive number if the specified calendar
     * date is less than, equal to, or greater than this calendar date,
     * respectively.
     */
    @Override
    public int compareTo(CalendarDate calendarDate) {
        int result = this.year - calendarDate.year;

        if (result == 0) {
            result = this.month - calendarDate.month;

            if (result == 0) {
                result = this.day - calendarDate.day;
            }
        }

        return result;
    }

    /**
     * Indicates whether some other object is "equal to" this one.
     * This is the case if the object is a calendar date that represents the
     * same day as this one.
     *
     * @param o
     * Reference to the object against which to compare.
     */
    @Override
    public boolean equals(Object o) {
        return (o instanceof CalendarDate
            && ((CalendarDate)o).year == this.year
            && ((CalendarDate)o).month == this.month
            && ((CalendarDate)o).day == this.day);
    }

    /**
     * Returns a hash code value for the object.
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.year;
        result = prime * result + this.month;
        result = prime * result + this.day;
        return result;
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

        buf.append(format.format(this.year));
        buf.append("-");

        format.setMinimumIntegerDigits(2);

        buf.append(format.format(this.month + 1));
        buf.append("-");
        buf.append(format.format(this.day + 1));

        return buf.toString();
    }

    /**
     * Creates a new date representing the specified date string. The date
     * string must be in the <tt>ISO 8601</tt> "calendar date" format,
     * which is <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param value
     * A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g. 2008-07-23).
     */
    public static CalendarDate decode(String value) {
        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid date format: " + value);
        }

        int year = Integer.parseInt(matcher.group(1));
        int month = Integer.parseInt(matcher.group(2)) - 1;
        int day = Integer.parseInt(matcher.group(3)) - 1;

        return new CalendarDate(year, month, day);
    }
}
