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
 * Class representing a time of day, independent of any particular time zone.
 */
public final class Time implements Comparable<Time>, Serializable {
    private static final long serialVersionUID = -2813485511869915193L;

    /**
     * Represents a range of times.
     */
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
                this.start = Time.decode((String)startRange);
            } else {
                this.start = (Time)startRange;
            }

            if (endRange instanceof String) {
                this.end = Time.decode((String)endRange);
            } else {
                this.end = (Time)endRange;
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

        public boolean contains(Time time) {
            if (time == null) {
                throw new IllegalArgumentException("time is null.");
            }

            boolean contains;
            if (this.start.compareTo(this.end) < 0) {
                contains = (this.start.compareTo(time) <= 0
                    && this.end.compareTo(time) >= 0);
            } else {
                contains = (this.end.compareTo(time) <= 0
                    && this.start.compareTo(time) >= 0);
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
            Time earlier = (this.start.compareTo(this.end) < 0 ? this.start : this.end);
            Time later = (earlier == this.start ? this.end : this.start);
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

    private static final Pattern PATTERN = Pattern.compile("^(\\d{2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?$");

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
        int msec = milliseconds;

        msec %= MILLISECONDS_PER_DAY;
        msec = (msec + MILLISECONDS_PER_DAY) % MILLISECONDS_PER_DAY;

        this.hour = msec / MILLISECONDS_PER_HOUR;
        msec %= MILLISECONDS_PER_HOUR;

        this.minute = msec / MILLISECONDS_PER_MINUTE;
        msec %= MILLISECONDS_PER_MINUTE;

        this.second = msec / MILLISECONDS_PER_SECOND;
        msec %= MILLISECONDS_PER_SECOND;

        this.millisecond = msec;
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
        return new Time(toMilliseconds() + milliseconds);
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
        return this.hour * MILLISECONDS_PER_HOUR
            + this.minute * MILLISECONDS_PER_MINUTE
            + this.second * MILLISECONDS_PER_SECOND
            + this.millisecond;
    }

    @Override
    public int compareTo(Time time) {
        int result = this.hour - time.hour;

        if (result == 0) {
            result = this.minute - time.minute;

            if (result == 0) {
                result = this.second - time.second;

                if (result == 0) {
                    result = this.millisecond - time.millisecond;
                }
            }
        }

        return result;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof Time
            && ((Time)o).hour == this.hour
            && ((Time)o).minute == this.minute
            && ((Time)o).second == this.second
            && ((Time)o).millisecond == this.millisecond);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.hour;
        result = prime * result + this.minute;
        result = prime * result + this.second;
        result = prime * result + this.millisecond;
        return result;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();

        NumberFormat format = NumberFormat.getIntegerInstance();
        format.setGroupingUsed(false);
        format.setMinimumIntegerDigits(2);

        buf.append(format.format(this.hour));
        buf.append(":");
        buf.append(format.format(this.minute));
        buf.append(":");
        buf.append(format.format(this.second));

        if (this.millisecond > 0) {
            buf.append(".");

            format.setMinimumIntegerDigits(3);
            buf.append(format.format(this.millisecond));
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
        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format: " + value);
        }

        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        int second = Integer.parseInt(matcher.group(3));

        String millisecondSequence = matcher.group(4);
        int millisecond = (millisecondSequence == null) ?
            0 : Integer.parseInt(millisecondSequence.substring(1));

        return new Time(hour, minute, second, millisecond);
    }
}
