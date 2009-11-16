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

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface whose keys are backed by a set
 * of enum values.
 */
public class EnumMap<E extends Enum<E>, V> implements Map<E, V>, Serializable {
    private static final long serialVersionUID = -8446839779287452516L;

    private EnumSet<E> keySet;
    private Object[] values;

    private transient MapListenerList<E, V> mapListeners = new MapListenerList<E, V>();

    public EnumMap(Class<E> enumClass) {
        keySet = new EnumSet<E>(enumClass);

        E[] constants = enumClass.getEnumConstants();
        values = new Object[constants.length];
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(E key) {
        verifyNotNull("key", key);

        return (V)values[key.ordinal()];
    }

    @SuppressWarnings("unchecked")
    @Override
    public V put(E key, V value) {
        verifyNotNull("key", key);

        int ordinal = key.ordinal();
        V previousValue = (V)values[ordinal];
        values[ordinal] = value;

        if (keySet.contains(key)) {
            mapListeners.valueUpdated(this, key, previousValue);
        } else {
            keySet.add(key);
            mapListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public V remove(E key) {
        verifyNotNull("key", key);

        V value = null;
        if (keySet.contains(key)) {
            int ordinal = key.ordinal();
            value = (V)values[ordinal];
            values[ordinal] = null;
            keySet.remove(key);
            mapListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    @Override
    public void clear() {
        if (!keySet.isEmpty()) {
            values = new Object[values.length];
            keySet.clear();
            mapListeners.mapCleared(this);
        }
    }

    @Override
    public boolean containsKey(E key) {
        return keySet.contains(key);
    }

    @Override
    public boolean isEmpty() {
        return keySet.isEmpty();
    }

    @Override
    public int getCount() {
        return keySet.getCount();
    }

    @Override
    public Comparator<E> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<E> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<E> iterator() {
        return keySet.iterator();
    }

    @Override
    public ListenerList<MapListener<E, V>> getMapListeners() {
        return mapListeners;
    }

    private static void verifyNotNull(String argument, Object value) {
        if (value == null) {
            throw new IllegalArgumentException(argument + " cannot be null.");
        }
    }
}
