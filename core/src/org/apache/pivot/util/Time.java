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

        public Range(final Time time) {
            this(time, time);
        }

        public Range(final Time start, final Time end) {
            this.start = start;
            this.end = end;
        }

        public Range(final String time) {
            this.start = this.end = Time.decode(time);
        }

        public Range(final String start, final String end) {
            this.start = Time.decode(start);
            this.end = Time.decode(end);
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
                this.start = Time.decode((String) startRange);
            } else {
                this.start = (Time) startRange;
            }

            if (endRange instanceof String) {
                this.end = Time.decode((String) endRange);
            } else {
                this.end = (Time) endRange;
            }
        }

        public Range(final Sequence<?> range) {
            Utils.checkNull(range, "range");

            Object startRange = range.get(0);
            Object endRange = range.get(1);

            if (startRange instanceof String) {
                this.start = Time.decode((String) startRange);
            } else {
                this.start = (Time) startRange;
            }

            if (endRange instanceof String) {
                this.end = Time.decode((String) endRange);
            } else {
                this.end = (Time) endRange;
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

        public boolean contains(final Time time) {
            Utils.checkNull(time, "time");

            boolean contains;
            if (this.start.compareTo(this.end) < 0) {
                contains = (this.start.compareTo(time) <= 0 && this.end.compareTo(time) >= 0);
            } else {
                contains = (this.end.compareTo(time) <= 0 && this.start.compareTo(time) >= 0);
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
            Time earlier = (this.start.compareTo(this.end) < 0 ? this.start : this.end);
            Time later = (earlier == this.start ? this.end : this.start);
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
            Utils.checkNull(value, "value");

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

    public static final int NANOS_PER_MILLI = 1_000_000;

    private static final Pattern PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2}):(\\d{2})(\\.(\\d{3}))?$");

    public Time() {
        this(new GregorianCalendar());
    }

    public Time(final Calendar calendar) {
        Utils.checkNull(calendar, "calendar");

        this.hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.minute = calendar.get(Calendar.MINUTE);
        this.second = calendar.get(Calendar.SECOND);
        this.millisecond = calendar.get(Calendar.MILLISECOND);
    }

    public Time(final int hour, final int minute, final int second) {
        this(hour, minute, second, 0);
    }

    private void check(final int value, final int limit, final String part) {
        if (value < 0 || value > limit) {
            throw new IllegalArgumentException("Invalid " + part + ".");
        }
    }

    public Time(final int hour, final int minute, final int second, final int millisecond) {
        check(hour, 23, "hour");
        check(minute, 59, "minute");
        check(second, 59, "second");
        check(millisecond, 999, "millisecond");

        this.hour = hour;
        this.minute = minute;
        this.second = second;
        this.millisecond = millisecond;
    }

    public Time(final int milliseconds) {
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
     * Construct a <tt>Time</tt> from a {@link LocalTime}, rounding
     * up the nanosecond value to our milliseconds.
     *
     * @param localTime The local time to convert.
     * @see #NANOS_PER_MILLI
     */
    public Time(final LocalTime localTime) {
        this(localTime.getHour(),
             localTime.getMinute(),
             localTime.getSecond(),
             ((localTime.getNano() + (NANOS_PER_MILLI / 2)) / NANOS_PER_MILLI));
    }

    /**
     * Adds the specified milliseconds of days to this time and returns the
     * resulting time. The number of milliseconds may be negative, in which case
     * the result will be a time prior to this time.
     *
     * @param milliseconds The number of milliseconds to add to this time.
     * @return The resulting time.
     */
    public Time add(final int milliseconds) {
        return new Time(toMilliseconds() + milliseconds);
    }

    /**
     * Gets the number of milliseconds in between this time and the specified
     * time. If this time represents a time later than the specified time, the
     * difference will be positive. If this time represents a time before the
     * specified time, the difference will be negative. If the two times
     * represent the same time, the difference will be zero.
     *
     * @param time The time to subtract from this time.
     * @return The number of milliseconds in between this time and <tt>time</tt>.
     */
    public int subtract(final Time time) {
        Utils.checkNull(time, "time");

        return toMilliseconds() - time.toMilliseconds();
    }

    /**
     * @return The number of milliseconds since midnight represented by this time.
     */
    public int toMilliseconds() {
        return this.hour * MILLISECONDS_PER_HOUR + this.minute * MILLISECONDS_PER_MINUTE
            + this.second * MILLISECONDS_PER_SECOND + this.millisecond;
    }

    /**
     * @return This time converted to a {@link LocalTime}.
     */
    public LocalTime toLocalTime() {
        return LocalTime.of(this.hour, this.minute, this.second, this.millisecond * NANOS_PER_MILLI);
    }

    @Override
    public int compareTo(final Time time) {
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
    public boolean equals(final Object o) {
        return (o instanceof Time
            && ((Time) o).hour == this.hour
            && ((Time) o).minute == this.minute
            && ((Time) o).second == this.second
            && ((Time) o).millisecond == this.millisecond);
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
     * which is <tt>[hh]:[mm]:[ss]</tt>. An optional millisecond suffix of the
     * form <tt>.[nnn]</tt> is also supported.
     *
     * @param value A string in the form of <tt>[hh]:[mm]:[ss]</tt> or
     * <tt>[hh]:[mm]:[ss].[nnn]</tt> (e.g. 17:19:20 or 17:19:20.412).
     * @return The {@code Time} value corresponding to the input string.
     */
    public static Time decode(final String value) {
        Matcher matcher = PATTERN.matcher(value);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid time format: " + value);
        }

        int hour = Integer.parseInt(matcher.group(1));
        int minute = Integer.parseInt(matcher.group(2));
        int second = Integer.parseInt(matcher.group(3));

        String millisecondSequence = matcher.group(4);
        int millisecond = (millisecondSequence == null) ? 0
            : Integer.parseInt(millisecondSequence.substring(1));

        return new Time(hour, minute, second, millisecond);
    }
}
