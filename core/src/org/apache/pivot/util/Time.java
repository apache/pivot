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
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;

/**
 * Class representing a time of day, independent of any particular time zone.
 */
public final class Time implements Comparable<Time>, Serializable {
    private static final long serialVersionUID = -2813485511869915193L;

    public static final class Range {
        public static final String START_KEY = "start";
        public static final String END_KEY = "end";

        public final Time start;
        public final Time end;

        public Range(Time time) {
            this(time, time);
        }

        public Range(Time start, Time end) {
            this.start = start;
            this.end = end;
        }

        public Range(String start, String end) {
            this.start = Time.decode(start);
            this.end = Time.decode(end);
        }

        public Range(Range range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            start = range.start;
            end = range.end;
        }

        public Range(Dictionary<String, ?> range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            Object start = range.get(START_KEY);
            Object end = range.get(END_KEY);

            if (start == null) {
                throw new IllegalArgumentException(START_KEY + " is required.");
            }

            if (end == null) {
                throw new IllegalArgumentException(END_KEY + " is required.");
            }

            if (start instanceof String) {
                this.start = Time.decode((String)start);
            } else {
                this.start = (Time)start;
            }

            if (end instanceof String) {
                this.end = Time.decode((String)end);
            } else {
                this.end = (Time)end;
            }
        }

        public int getLength() {
            return Math.abs(start.subtract(end)) + 1;
        }

        public boolean contains(Range range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            Range normalizedRange = range.normalize();

            boolean contains;
            if (start.compareTo(end) < 0) {
                contains = (start.compareTo(normalizedRange.start) <= 0
                    && end.compareTo(normalizedRange.end) >= 0);
            } else {
                contains = (end.compareTo(normalizedRange.start) <= 0
                    && start.compareTo(normalizedRange.end) >= 0);
            }

            return contains;
        }

        public boolean contains(Time time) {
            if (time == null) {
                throw new IllegalArgumentException("time is null.");
            }

            boolean contains;
            if (start.compareTo(end) < 0) {
                contains = (start.compareTo(time) <= 0
                    && end.compareTo(time) >= 0);
            } else {
                contains = (end.compareTo(time) <= 0
                    && start.compareTo(time) >= 0);
            }

            return contains;
        }

        public boolean intersects(Range range) {
            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            Range normalizedRange = range.normalize();

            boolean intersects;
            if (start.compareTo(end) < 0) {
                intersects = (start.compareTo(normalizedRange.end) <= 0
                    && end.compareTo(normalizedRange.start) >= 0);
            } else {
                intersects = (end.compareTo(normalizedRange.end) <= 0
                    && start.compareTo(normalizedRange.start) >= 0);
            }

            return intersects;
        }

        public Range normalize() {
            Time earlier = (start.compareTo(end) < 0 ? start : end);
            Time later = (earlier == start ? end : start);
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
                range = new Range(Time.decode(value));
            }

            return range;
        }
    }

    /**
     * The hour value, in 24-hour format.
     */
    public final int hour;

    /**
     * The minute value.
     */
    public final int minute;

    /**
     * The second value.
     */
    public final int second;

    /**
     * The millisecond value.
     */
    public final int millisecond;

    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int MILLISECONDS_PER_MINUTE = 60 * MILLISECONDS_PER_SECOND;
    public static final int MILLISECONDS_PER_HOUR = 60 * MILLISECONDS_PER_MINUTE;
    public static final int MILLISECONDS_PER_DAY = 24 * MILLISECONDS_PER_HOUR;

    public Time() {
        this(new GregorianCalendar());
    }

    public Time(Calendar calendar) {
        if (calendar == null) {
            throw new IllegalArgumentException();
        }

        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
        this.millisecond = calendar.get(Calendar.MILLISECOND);
    }

    public Time(int hour, int minute, int second) {
        this(hour, minute, second, 0);
    }

    public Time(int hour, int minute, int second, int millisecond) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Invalid hour.");
        }

        if (minute < 0 || minute > 59) {
            throw new IllegalArgumentException("Invalid minute.");
        }

        if (second < 0 || second > 59) {
            throw new IllegalArgumentException("Invalid second.");
        }

        if (millisecond < 0 || millisecond > 999) {
            throw new IllegalArgumentException("Invalid millisecond.");
        }

        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
    }

    public Time(int milliseconds) {
        if (milliseconds < 0 || milliseconds > MILLISECONDS_PER_DAY - 1) {
            throw new IllegalArgumentException("Invalid milliseconds.");
        }

        hour = milliseconds / MILLISECONDS_PER_HOUR;
        milliseconds %= MILLISECONDS_PER_HOUR;

        minute = milliseconds / MILLISECONDS_PER_MINUTE;
        milliseconds %= MILLISECONDS_PER_MINUTE;

        second = milliseconds / MILLISECONDS_PER_SECOND;
        milliseconds %= MILLISECONDS_PER_SECOND;

        millisecond = milliseconds;
    }

    /**
     * Adds the specified milliseconds of days to this time and returns the
     * resulting time. The number of milliseconds may be negative, in which
     * case the result will be a time prior to this time.
     *
     * @param milliseconds
     * The number of milliseconds to add to this time.
     *
     * @return
     * The resulting time.
     */
    public Time add(int milliseconds) {
        milliseconds += toMilliseconds();

        milliseconds %= MILLISECONDS_PER_DAY;

        if (milliseconds < 0) {
            milliseconds += MILLISECONDS_PER_DAY;
        }

        return new Time(milliseconds);
    }

    /**
     * Gets the number of milliseconds in between this time and the specified
     * time. If this time represents a time later than the specified time, the
     * difference will be positive. If this time represents a time before the
     * specified time, the difference will be negative. If the two times represent
     * the same time, the difference will be zero.
     *
     * @param time
     * The time to subtract from this time.
     *
     * @return
     * The number of milliseconds in between this time and <tt>time</tt>.
     */
    public int subtract(Time time) {
        if (time == null) {
            throw new IllegalArgumentException();
        }

        return toMilliseconds() - time.toMilliseconds();
    }

    /**
     * Returns the number of milliseconds since midnight represented by
     * this time.
     *
     * @return
     * The number of milliseconds since midnight represented by this time.
     */
    public int toMilliseconds() {
        return hour * MILLISECONDS_PER_HOUR
            + minute * MILLISECONDS_PER_MINUTE
            + second * MILLISECONDS_PER_SECOND
            + millisecond;
    }

    @Override
    public int compareTo(Time time) {
        int result = hour - time.hour;

        if (result == 0) {
            result = minute - time.minute;

            if (result == 0) {
                result = second - time.second;

                if (result == 0) {
                    result = millisecond - time.millisecond;
                }
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Time
            && ((Time)o).hour == hour
            && ((Time)o).minute == minute
            && ((Time)o).second == second
            && ((Time)o).millisecond == millisecond);
    }

    @Override
    public int hashCode() {
        Integer hashKey = hour + minute + second + millisecond;
        return hashKey.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(2);

        buf.append(format.format(hour));
        buf.append(":");
        buf.append(format.format(minute));
        buf.append(":");
        buf.append(format.format(second));

        if (millisecond > 0) {
            buf.append(".");

            format.setMinimumIntegerDigits(3);
            buf.append(format.format(millisecond));
        }

        return buf.toString();
    }

    /**
     * Creates a new time representing the specified time string. The time
     * string must be in the full <tt>ISO 8601</tt> extended "time" format,
     * which  is <tt>[hh]:[mm]:[ss]</tt>. An optional millisecond suffix of
     * the form <tt>.[nnn]</tt> is also supported.
     *
     * @param value
     * A string in the form of <tt>[hh]:[mm]:[ss]</tt> or
     * <tt>[hh]:[mm]:[ss].[nnn]</tt> (e.g. 17:19:20 or 17:19:20.412).
     */

    public static Time decode(String value) {
        Pattern pattern = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?$");
        Matcher matcher = pattern.matcher(value);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format: " + value);
        }

        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        int second = Integer.parseInt(matcher.group(3));

        int millisecond;
        if (matcher.groupCount() == 5) {
            millisecond = Integer.parseInt(matcher.group(4).substring(1));
        } else {
            millisecond = 0;
        }

        return new Time(hour, minute, second, millisecond);
    }
}
