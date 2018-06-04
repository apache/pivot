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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
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

        public Range(final CalendarDate calendarDate) {
            this(calendarDate, calendarDate);
        }

        public Range(final CalendarDate start, final CalendarDate end) {
            this.start = start;
            this.end = end;
        }

        public Range(final String date) {
            this.start = this.end = CalendarDate.decode(date);
        }

        public Range(final String start, final String end) {
            this.start = CalendarDate.decode(start);
            this.end = CalendarDate.decode(end);
        }

        public Range(final Range range) {
            Utils.checkNull(range, "range");

            this.start = range.start;
            this.end = range.end;
        }

        public Range(final Dictionary<String, ?> range) {
            Utils.checkNull(range, "range");

            Object startRange = range.get(START_KEY);
            Object endRange = range.get(END_KEY);

            if (startRange == null) {
                throw new IllegalArgumentException(START_KEY + " is required.");
            }

            if (endRange == null) {
                throw new IllegalArgumentException(END_KEY + " is required.");
            }

            if (startRange instanceof String) {
                this.start = CalendarDate.decode((String) startRange);
            } else {
                this.start = (CalendarDate) startRange;
            }

            if (endRange instanceof String) {
                this.end = CalendarDate.decode((String) endRange);
            } else {
                this.end = (CalendarDate) endRange;
            }
        }

        public Range(final Sequence<?> range) {
            Utils.checkNull(range, "range");

            Object startRange = range.get(0);
            Object endRange = range.get(1);

            if (startRange instanceof String) {
                this.start = CalendarDate.decode((String) startRange);
            } else {
                this.start = (CalendarDate) startRange;
            }

            if (endRange instanceof String) {
                this.end = CalendarDate.decode((String) endRange);
            } else {
                this.end = (CalendarDate) endRange;
            }
        }

        public int getLength() {
            return Math.abs(this.start.subtract(this.end)) + 1;
        }

        public boolean contains(final Range range) {
            Utils.checkNull(range, "range");

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

        public boolean contains(final CalendarDate calendarDate) {
            Utils.checkNull(calendarDate, "calendarDate");

            boolean contains;
            if (this.start.compareTo(this.end) < 0) {
                contains = (this.start.compareTo(calendarDate) <= 0 && this.end.compareTo(calendarDate) >= 0);
            } else {
                contains = (this.end.compareTo(calendarDate) <= 0 && this.start.compareTo(calendarDate) >= 0);
            }

            return contains;
        }

        public boolean intersects(final Range range) {
            Utils.checkNull(range, "range");

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

        @Override
        public boolean equals(final Object o) {
            if (o != null && o instanceof Range) {
                Range r = (Range) o;
                return r.start.equals(this.start) && r.end.equals(this.end);
            }
            return false;
        }

        @Override
        public int hashCode() {
            // TODO: is this is a good calculation?
            return start.hashCode() * end.hashCode();
        }

        public static Range decode(final String value) {
            Utils.checkNullOrEmpty(value, "value");

            Range range;
            if (value.startsWith("{")) {
                try {
                    range = new Range(JSONSerializer.parseMap(value));
                } catch (SerializationException exception) {
                    throw new IllegalArgumentException(exception);
                }
            } else if (value.startsWith("[")) {
                try {
                    range = new Range(JSONSerializer.parseList(value));
                } catch (SerializationException exception) {
                    throw new IllegalArgumentException(exception);
                }
            } else {
                String[] parts = value.split("\\s*[,;]\\s*");
                if (parts.length == 2) {
                    range = new Range(parts[0], parts[1]);
                } else if (parts.length == 1) {
                    range = new Range(value);
                } else {
                    throw new IllegalArgumentException("Invalid format for Range: " + value);
                }
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

    private static final int[] MONTH_LENGTHS = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};

    private static final Pattern PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");

    private static final int GREGORIAN_CUTOVER_YEAR = 1582;

    /** Minimum supported year (must be greater or equal). */
    public static final int MIN_CALENDAR_YEAR = GREGORIAN_CUTOVER_YEAR + 1;
    /** Maximum supported year (must be less or equal). */
    public static final int MAX_CALENDAR_YEAR = 9999;

    /**
     * Creates a new <tt>CalendarDate</tt> representing the current day in the
     * default timezone and the default locale.
     */
    public CalendarDate() {
        this(new GregorianCalendar());
    }

    /**
     * Creates a new <tt>CalendarDate</tt> representing the day contained in the
     * specified Gregorian calendar (assuming the default locale and the default
     * timezone).
     *
     * @param calendar The calendar containing the year, month, and day fields.
     */
    public CalendarDate(final GregorianCalendar calendar) {
        this(calendar.get(Calendar.YEAR),
             calendar.get(Calendar.MONTH),
             calendar.get(Calendar.DAY_OF_MONTH) - 1);
    }

    /**
     * Creates a new <tt>CalendarDate</tt> from the given {@link LocalDate}
     * (new in Java 8).  This does not represent a moment in time, but only
     * represents a date (as in year, month and day).
     *
     * @param localDate The date value containing year, month and day fields.
     */
    public CalendarDate(final LocalDate localDate) {
        this(localDate.getYear(),
             localDate.getMonthValue() - 1,
             localDate.getDayOfMonth() - 1);
    }

    /**
     * Creates a new <tt>CalendarDate</tt> representing the specified year,
     * month, and day of month.
     *
     * @param year The year field. (e.g. <tt>2008</tt>)
     * @param month The month field, 0-based. (e.g. <tt>2</tt> for March)
     * @param day The day of the month, 0-based. (e.g. <tt>14</tt> for the 15th)
     * @see #MIN_CALENDAR_YEAR
     * @see #MAX_CALENDAR_YEAR
     */
    public CalendarDate(final int year, final int month, final int day) {
        if (year < MIN_CALENDAR_YEAR || year > MAX_CALENDAR_YEAR) {
            throw new IllegalArgumentException("Invalid year: " + year);
        }

        if (month < 0 || month > 11) {
            throw new IllegalArgumentException("Invalid month: " + month);
        }

        int daysInMonth = MONTH_LENGTHS[month];

        if (isLeapYear(year) && month == 1) {
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
     * case the result will be a date before this calendar date. <p> More
     * formally, it is defined that given calendar dates <tt>c1</tt> and
     * <tt>c2</tt>, the following will return <tt>true</tt>: <pre>
     * c1.add(c2.subtract(c1)).equals(c2); </pre>
     *
     * @param days The number of days to add to (or subtract from if negative)
     * this calendar date.
     * @return The resulting calendar date.
     */
    public CalendarDate add(final int days) {
        GregorianCalendar calendar = toCalendar();
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return new CalendarDate(calendar);
    }

    /**
     * Adds the specified number of months to this calendar date and returns the
     * resulting calendar date. The number of months may be negative, in which
     * case the result will be a date before this calendar date. <p> More
     * formally, it is defined that given calendar dates <tt>c1</tt> and
     * <tt>c2</tt>, the following will return <tt>true</tt>: <pre>
     * c1.add(c2.subtract(c1)).equals(c2); </pre>
     *
     * @param months The number of months to add to (or subtract from if negative)
     * this calendar date.
     * @return The resulting calendar date.
     */
    public CalendarDate addMonths(final int months) {
        GregorianCalendar calendar = toCalendar();
        calendar.add(Calendar.MONTH, months);
        return new CalendarDate(calendar);
    }

    /**
     * Adds the specified number of years to this calendar date and returns the
     * resulting calendar date. The number of years may be negative, in which
     * case the result will be a date before this calendar date. <p> More
     * formally, it is defined that given calendar dates <tt>c1</tt> and
     * <tt>c2</tt>, the following will return <tt>true</tt>: <pre>
     * c1.add(c2.subtract(c1)).equals(c2); </pre>
     *
     * @param years The number of years to add to (or subtract from if negative)
     * this calendar date.
     * @return The resulting calendar date.
     */
    public CalendarDate addYears(final int years) {
        GregorianCalendar calendar = toCalendar();
        calendar.add(Calendar.YEAR, years);
        return new CalendarDate(calendar);
    }

    /**
     * Gets the number of days in between this calendar date and the specified
     * calendar date. If this calendar date represents a day after the specified
     * calendar date, the difference will be positive. If this calendar date
     * represents a day before the specified calendar date, the difference will
     * be negative. If the two calendar dates represent the same day, the
     * difference will be zero. <p> More formally, it is defined that given
     * calendar dates <tt>c1</tt> and <tt>c2</tt>, the following will return
     * <tt>true</tt>: <pre> c1.add(c2.subtract(c1)).equals(c2); </pre>
     *
     * @param calendarDate The calendar date to subtract from this calendar
     * date.
     * @return The number of days in between this calendar date and
     * <tt>calendarDate</tt>.
     */
    public int subtract(final CalendarDate calendarDate) {
        GregorianCalendar c1 = toCalendar();
        GregorianCalendar c2 = calendarDate.toCalendar();

        long t1 = c1.getTimeInMillis();
        long t2 = c2.getTimeInMillis();

        return (int) ((t1 - t2) / (1000L * 60 * 60 * 24));
    }

    /**
     * Translates this calendar date to an instance of
     * <tt>GregorianCalendar</tt>, with the <tt>year</tt>, <tt>month</tt>, and
     * <tt>dayOfMonth</tt> fields set in the default time zone with the default
     * locale.
     *
     * @return This calendar date as a <tt>GregorianCalendar</tt>.
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
     * @param time The time of day.
     * @return This calendar date as a <tt>GregorianCalendar</tt>.
     */
    public GregorianCalendar toCalendar(final Time time) {
        GregorianCalendar calendar = new GregorianCalendar(this.year, this.month, this.day + 1,
            time.hour, time.minute, time.second);
        calendar.set(Calendar.MILLISECOND, time.millisecond);

        return calendar;
    }

    /**
     * @return An equivalent {@link LocalDate} that represents the same calendar date
     * as this date.
     */
    public LocalDate toLocalDate() {
        return LocalDate.of(this.year, this.month + 1, this.day + 1);
    }

    /**
     * @return A date and time representing this calendar date along with the given
     * wall clock time.
     *
     * @param time The wall clock time to combine with this calendar date.
     */
    public LocalDateTime toLocalDateTime(final Time time) {
        LocalTime localTime = LocalTime.of(time.hour, time.minute, time.second, time.millisecond * 1_000_000);
        LocalDate localDate = toLocalDate();
        return localDate.atTime(localTime);
    }

    /**
     * Compares this calendar date with another calendar date.
     *
     * @param calendarDate The calendar date against which to compare.
     * @return A negative number, zero, or a positive number if the specified
     * calendar date is less than, equal to, or greater than this calendar date,
     * respectively.
     */
    @Override
    public int compareTo(final CalendarDate calendarDate) {
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
     * Indicates whether some other object is "equal to" this one. This is the
     * case if the object is a calendar date that represents the same day as
     * this one.
     *
     * @param o Reference to the object against which to compare.
     */
    @Override
    public boolean equals(final Object o) {
        return (o instanceof CalendarDate && ((CalendarDate) o).year == this.year
            && ((CalendarDate) o).month == this.month && ((CalendarDate) o).day == this.day);
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
     * @return Is the given year a leap year according to the standard definition?
     *
     * @param year The year to check.
     */
    public static boolean isLeapYear(final int year) {
        return ((year & 3) == 0 && (year % 100 != 0 || year % 400 == 0));
    }

    /**
     * Creates a new date representing the specified date string. The date
     * string must be in the <tt>ISO 8601</tt> "calendar date" format, which is
     * <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param value A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g.
     * 2008-07-23).
     * @return The {@code CalendarDate} corresponding to the input string.
     */
    public static CalendarDate decode(final String value) {
        Utils.checkNullOrEmpty(value, "calendarDate");

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
