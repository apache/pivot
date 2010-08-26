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
package org.apache.pivot.collections;

import java.util.Comparator;

/**
 * Root interface in collection hierarchy. Defines operations common to all
 * collections.
 */
public interface Collection<T> extends Iterable<T> {
    /**
     * Removes all elements from the collection.
     */
    public void clear();

    /**
     * Tests the emptiness of the collection.
     *
     * @return
     * <tt>true</tt> if the collection contains no elements; <tt>false</tt>,
     * otherwise.
     */
    public boolean isEmpty();

    /**
     * Returns the collection's sort order.
     *
     * @return
     * The comparator used to order elements in the collection, or <tt>null</tt>
     * if the sort order is undefined.
     *
     * @see #setComparator(Comparator)
     */
    public Comparator<T> getComparator();

    /**
     * Sets the collection's sort order, re-ordering the collection's contents
     * and ensuring that new entries preserve the sort order.
     * <p>
     * Calling this method more than once with the same comparator will re-sort
     * the collection.
     *
     * @param comparator
     * The comparator used to order elements in the collection, or null if the
     * collection is unsorted.
     */
    public void setComparator(Comparator<T> comparator);
}
