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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Abstract base class for listener lists.
 * <p>
 * NOTE This class is not inherently thread safe. Subclasses that require
 * thread-safe access should synchronize method access appropriately. Callers
 * must manually synchronize on the listener list instance to ensure thread
 * safety during iteration.
 */
public abstract class ListenerList<T> implements Iterable<T> {

    private static final int DEFAULT_SIZE = 5;

    private ArrayList<T> list = new ArrayList<T>(DEFAULT_SIZE);

    /**
     * Adds a listener to the list, if it has not previously been added.
     *
     * @param listener
     */
    public void add(T listener) {
        if (indexOf(listener) >= 0) {
            System.err.println("Duplicate listener " + listener + " added to " + this);
            return;
        }

        list.add(listener);
    }

    /**
     * Removes a listener from the list, if it has previously been added.
     *
     * @param listener
     */
    public void remove(T listener) {
        int index = indexOf(listener);

        if (index < 0) {
            System.err.println("Nonexistent listener " + listener + " removed from " + this);
            return;
        }

        list.remove(index);
    }

    /**
     * Returns the position of the given listener in the list,
     * or -1 if the listener is not in the list.
     */
    private int indexOf(T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }
        return list.indexOf(listener);
    }

    /**
     * Tests the existence of a listener in the list.
     *
     * @param listener
     *
     * @return
     * <tt>true</tt> if the listener exists in the list; <tt>false</tt>,
     * otherwise.
     */
    public boolean contains(T listener) {
        return indexOf(listener) >= 0;
    }

    /**
     * Tests the emptiness of the list.
     *
     * @return
     * <tt>true</tt> if the list contains no listeners; <tt>false</tt>,
     * otherwise.
     */
    public boolean isEmpty() {
        return list.isEmpty();
    }

    /**
     * Returns an iterator over the elements of the list.
     */
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

}
