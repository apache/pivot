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
package org.apache.pivot.wtk;

import java.util.Iterator;

import org.apache.pivot.util.ListenerList;

/**
 * This is a customised subclass of ListenerList that adds thread-safety checks
 * for the WTK components.
 */
public class WTKListenerList<T> extends ListenerList<T> {

    /**
     * Adds a listener to the list, if it has not previously been added.
     *
     * @param listener
     */
    @Override
    public void add(T listener) {
        Container.assertEventDispatchThread();
        super.add(listener);
    }

    /**
     * Removes a listener from the list, if it has previously been added.
     *
     * @param listener
     */
    @Override
    public void remove(T listener) {
        Container.assertEventDispatchThread();
        super.remove(listener);
    }

    /**
     * Tests the existence of a listener in the list.
     *
     * @param listener
     *
     * @return <tt>true</tt> if the listener exists in the list; <tt>false</tt>,
     *         otherwise.
     */
    @Override
    public boolean contains(T listener) {
        Container.assertEventDispatchThread();
        return super.contains(listener);
    }

    /**
     * Tests the emptiness of the list.
     *
     * @return <tt>true</tt> if the list contains no listeners; <tt>false</tt>,
     *         otherwise.
     */
    @Override
    public boolean isEmpty() {
        Container.assertEventDispatchThread();
        return super.isEmpty();
    }

    @Override
    public Iterator<T> iterator() {
        Container.assertEventDispatchThread();
        return super.iterator();
    }
}
