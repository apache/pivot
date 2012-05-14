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
package org.apache.pivot.collections.adapter;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.Set;
import org.apache.pivot.collections.SetListener;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Implementation of the {@link Set} interface that is backed by an instance of
 * {@link java.util.Set}.
 */
public class SetAdapter<E> implements Set<E>, Serializable {
    private static final long serialVersionUID = -816891924416727900L;

    private java.util.Set<E> set = null;
    private transient SetListenerList<E> setListeners = new SetListenerList<E>();

    public SetAdapter(java.util.Set<E> set) {
        if (set == null) {
            throw new IllegalArgumentException("set is null.");
        }

        this.set = set;
    }

    public java.util.Set<E> getSet() {
        return set;
    }

    @Override
    public boolean add(E element) {
        boolean added = false;

        if (!contains(element)) {
            set.add(element);
            added = true;

            setListeners.elementAdded(this, element);
        }

        return added;
    }

    @Override
    public boolean remove(E element) {
        boolean removed = false;

        if (contains(element)) {
            set.remove(element);
            removed = false;

            setListeners.elementRemoved(this, element);
        }

        return removed;
    }

    @Override
    public void clear() {
        if (!isEmpty()) {
            set.clear();
            setListeners.setCleared(this);
        }
    }

    @Override
    public boolean contains(E element) {
        return set.contains(element);
    }

    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    @Override
    public int getCount() {
        return set.size();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Comparator<E> getComparator() {
        if (this.set instanceof java.util.SortedSet<?>) {
            return (Comparator<E>)((java.util.SortedSet<?>)this.set).comparator();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setComparator(Comparator<E> comparator) {
        Comparator<E> previousComparator = getComparator();

        // If the adapted set supports it, construct a new sorted set
        if (this.set instanceof java.util.SortedSet<?>) {
            try {
                Constructor<?> constructor = this.set.getClass().getConstructor(Comparator.class);
                if (constructor != null) {
                    java.util.SortedSet<E> setLocal = (java.util.SortedSet<E>)constructor.newInstance(comparator);
                    setLocal.addAll(this.set);
                    this.set = setLocal;
                }
            } catch (SecurityException exception) {
                throw new RuntimeException(exception);
            } catch (NoSuchMethodException exception) {
                throw new RuntimeException(exception);
            } catch (IllegalArgumentException exception) {
                throw new RuntimeException(exception);
            } catch (InstantiationException exception) {
                throw new RuntimeException(exception);
            } catch (IllegalAccessException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }

        setListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<E> iterator() {
        return new ImmutableIterator<E>(set.iterator());
    }

    @Override
    public ListenerList<SetListener<E>> getSetListeners() {
        return setListeners;
    }

    @Override
    public String toString() {
        return set.toString();
    }
}
