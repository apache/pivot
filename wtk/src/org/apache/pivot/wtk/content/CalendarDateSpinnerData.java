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
package org.apache.pivot.wtk.content;

import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.TimeZone;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.ListenerList;


/**
 * Spinner data model that presents a bounded list of calendar dates.
 * <p>
 * This is a lightweight class that spoofs the actual list data by using an
 * internal calendar instance from which <tt>CalendarDate</tt> instances are
 * created on demand.
 */
public class CalendarDateSpinnerData implements List<CalendarDate> {
    /**
     * Iterator that simply wraps calls to the list. Since the internal list
     * data is spoofed, each accessor runs in constant time, so there's no
     * performance hit in making the iterator delegate its implementation to
     * the list.
     */
    private class DataIterator implements Iterator<CalendarDate> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return (index < length);
        }

        @Override
        public CalendarDate next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return get(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private GregorianCalendar calendar;
    private int calendarIndex;

    // Calculated during construction
    private transient int length;

    private ListListenerList<CalendarDate> listListeners =
        new ListListenerList<CalendarDate>();

    /**
     * Creates a new <tt>CalendarDateSpinnerData</tt> bounded from
     * <tt>1900-01-01</tt> to <tt>2099-12-31</tt>.
     */
    public CalendarDateSpinnerData() {
        this(new CalendarDate(1900, 0, 0), new CalendarDate(2099, 11, 30));
    }

    /**
     * Creates a new <tt>CalendarDateSpinnerData</tt> bounded by the specified
     * calendar dates (inclusive).
     *
     * @param lowerBound
     * The earliest date to include in this spinner data.
     *
     * @param upperBound
     * The latest date to include in this spinner data.
     */
    public CalendarDateSpinnerData(CalendarDate lowerBound, CalendarDate upperBound) {
        if (lowerBound == null) {
            throw new IllegalArgumentException("lowerBound is null.");
        }

        if (upperBound == null) {
            throw new IllegalArgumentException("upperBound is null.");
        }

        if (lowerBound.compareTo(upperBound) > 0) {
            throw new IllegalArgumentException("lowerBound is after upperBound.");
        }

        calendar = new GregorianCalendar(lowerBound.year, lowerBound.month,
            lowerBound.day + 1);
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        calendarIndex = 0;

        // Calculate our length and cache it, since it is guaranteed to
        // remain fixed
        GregorianCalendar upperBoundCalendar = new GregorianCalendar(upperBound.year,
            upperBound.month, upperBound.day + 1);
        upperBoundCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        long lowerBoundMilliseconds = calendar.getTimeInMillis();
        long upperBoundMilliseconds = upperBoundCalendar.getTimeInMillis();
        long indexDiff = (upperBoundMilliseconds - lowerBoundMilliseconds) /
            (1000l * 60 * 60 * 24);
        length = (int)indexDiff + 1;
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public int add(CalendarDate item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public void insert(CalendarDate item, int index) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public CalendarDate update(int index, CalendarDate item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public int remove(CalendarDate item) {
        throw new UnsupportedOperationException();
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public Sequence<CalendarDate> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    /**
     * Gets the calendar date at the specified index.
     *
     * @param index
     * The index of the calendar date to retrieve.
     */
    @Override
    public CalendarDate get(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException("Index out of bounds: " + index);
        }

        // Move the calendar's fields to match the specified index
        calendar.add(Calendar.DAY_OF_YEAR, index - calendarIndex);
        calendarIndex = index;

        // Calculate the desired fields
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH) - 1;

        return new CalendarDate(year, month, day);
    }

    @Override
    public int indexOf(CalendarDate item) {
        long currentMilliseconds = calendar.getTimeInMillis();

        GregorianCalendar tmpCalendar = new GregorianCalendar(item.year, item.month, item.day + 1);
        tmpCalendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        long itemMilliseconds = tmpCalendar.getTimeInMillis();

        long indexDiff = (itemMilliseconds - currentMilliseconds) / (1000l * 60 * 60 * 24);
        int index = calendarIndex + (int)indexDiff;

        return (index < 0 || index >= length) ? -1 : index;
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return (length == 0);
    }

    /**
     * Gets the number of entries in this list.
     *
     * @return
     * The number of calendar dates in this list.
     */
    @Override
    public int getLength() {
        return length;
    }

    /**
     * Gets the comparator for this list, which is guaranteed to always be
     * <tt>null</tt>. This class does not support comparators since there's no
     * real data to sort (it's all spoofed).
     */
    @Override
    public Comparator<CalendarDate> getComparator() {
        return null;
    }

    /**
     * Throws <tt>UnsupportedOperationException</tt>.
     */
    @Override
    public void setComparator(Comparator<CalendarDate> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<CalendarDate> iterator() {
        return new DataIterator();
    }

    @Override
    public ListenerList<ListListener<CalendarDate>> getListListeners() {
        return listListeners;
    }
}
