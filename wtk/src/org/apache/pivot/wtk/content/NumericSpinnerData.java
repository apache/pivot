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

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;


/**
 * Spinner data model that presents a bounded list of integers. This is a
 * lightweight class that spoofs the actual list data (no data is stored in
 * the list).
 * <p>
 * The iterator returned by this class's <tt>iterator</tt> method is
 * <i>fail-fast</i>: if the bounds of the enclosing spinner data change during
 * iteration, a <tt>ConcurrentModificationException</tt> will be thrown.
 */
public class NumericSpinnerData implements List<Integer> {

    private class DataIterator implements Iterator<Integer> {
        // Parity members to support ConcurrentModificationException check
        private int lowerBoundLocal = NumericSpinnerData.this.lowerBound;
        private int upperBoundLocal = NumericSpinnerData.this.upperBound;
        private int incrementLocal = NumericSpinnerData.this.increment;

        private int value = lowerBoundLocal;

        @Override
        public boolean hasNext() {
            return (value <= upperBoundLocal);
        }

        @Override
        public Integer next() {
            if (lowerBoundLocal != NumericSpinnerData.this.lowerBound
                || upperBoundLocal != NumericSpinnerData.this.upperBound
                || incrementLocal != NumericSpinnerData.this.increment) {
                throw new ConcurrentModificationException();
            }

            if (value > upperBoundLocal) {
                throw new NoSuchElementException();
            }

            int next = value;
            value += incrementLocal;
            return next;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private int lowerBound;
    private int upperBound;
    private int increment;

    private ListListenerList<Integer> listListeners = new ListListenerList<Integer>();

    /**
     * Creates a new <tt>NumericSpinnerData</tt> instance bounded from
     * <tt>Short.MIN_VALUE</tt> to <tt>Short.MAX_VALUE</tt> and an increment
     * of one.
     */
    public NumericSpinnerData() {
        this(Short.MIN_VALUE, Short.MAX_VALUE, 1);
    }

    /**
     * Creates a new <tt>NumericSpinnerData</tt> with the specified bounded
     * range and an increment of one.
     */
    public NumericSpinnerData(int lowerBound, int upperBound) {
        this(lowerBound, upperBound, 1);
    }

    /**
     * Creates a new <tt>NumericSpinnerData</tt> with the specified bounded
     * range and increment.
     */
    public NumericSpinnerData(int lowerBound, int upperBound, int increment) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound must be less than upper bound.");
        }

        long length = (((long)upperBound - (long)lowerBound) / increment) + 1;

        if (length > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Bounded range is too large.");
        }

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.increment = increment;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public void setLowerBound(int lowerBound) {
        this.lowerBound = lowerBound;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public void setUpperBound(int upperBound) {
        this.upperBound = upperBound;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    @Override
    public int add(Integer item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(Integer item, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer update(int index, Integer item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(Integer item) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Integer> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer get(int index) {
        if (index < 0 || index >= getLength()) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        return lowerBound + (index * increment);
    }

    @Override
    public int indexOf(Integer item) {
        int index = -1;

        if (item >= lowerBound && item <= upperBound) {
            int distance = item - lowerBound;

            // Ensure that our increment lands us on the item
            if (distance % increment == 0) {
                index = distance / increment;
            }
        }

        return index;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEmpty() {
        return (getLength() == 0);
    }

    @Override
    public int getLength() {
        return ((upperBound - lowerBound) / increment) + 1;
    }

    @Override
    public Comparator<Integer> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<Integer> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Integer> iterator() {
        return new DataIterator();
    }

    @Override
    public ListenerList<ListListener<Integer>> getListListeners() {
        return listListeners;
    }

}
