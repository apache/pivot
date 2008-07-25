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
package pivot.wtk.content;

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Spinner data model that presents a bounded list of numbers. This is a
 * lightweight class that spoofs the actual list data (no data is stored in
 * the list).
 * <p>
 * TODO Add support for an increment property.
 *
 * @author tvolkert
 */
public class NumericSpinnerData implements List<Number> {
    int lowerBound;
    int upperBound;

    private ListListenerList<Number> listListeners = new ListListenerList<Number>();

    /**
     * Creates a new <tt>NumericSpinnerData</tt> instance bounded from
     * <tt>Short.MIN_VALUE</tt> to <tt>Short.MAX_VALUE</tt>.
     */
    public NumericSpinnerData() {
        this(Short.MIN_VALUE, Short.MAX_VALUE);
    }

    /**
     * Creates a new <tt>NumericSpinnerData</tt> with the specified bounded
     * range.
     */
    public NumericSpinnerData(int lowerBound, int upperBound) {
        if (lowerBound >= upperBound) {
            throw new IllegalArgumentException("Lower bound must be less than upper bound.");
        }

        long count = upperBound - lowerBound + 1;

        if (count > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Bounded range is too large.");
        }

        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    public int add(Number item) {
        throw new UnsupportedOperationException();
    }

    public void insert(Number item, int index) {
        throw new UnsupportedOperationException();
    }

    public Number update(int index, Number item) {
        throw new UnsupportedOperationException();
    }

    public int remove(Number item) {
        throw new UnsupportedOperationException();
    }

    public Sequence<Number> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public Number get(int index) {
        if (index < 0 || index > upperBound - lowerBound) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }

        return lowerBound + index;
    }

    public int indexOf(Number item) {
        int intValue = item.intValue();

        if (intValue < lowerBound || intValue > upperBound) {
            return -1;
        }

        return intValue - lowerBound;
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public int getLength() {
        return upperBound - lowerBound + 1;
    }

    public Comparator<Number> getComparator() {
        return null;
    }

    public void setComparator(Comparator<Number> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Number> iterator() {
        // TODO
        return null;
    }

    public ListenerList<ListListener<Number>> getListListeners() {
        return listListeners;
    }
}
