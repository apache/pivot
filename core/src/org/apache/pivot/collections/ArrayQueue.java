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

import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;


/**
 * Implementation of the {@link Queue} interface that is backed by an
 * array.
 */
public class ArrayQueue<T> implements Queue<T>, Serializable {
    private static final long serialVersionUID = -3856732506886968324L;

    private ArrayList<T> arrayList = new ArrayList<T>();
    private transient QueueListenerList<T> queueListeners = new QueueListenerList<T>();

    public ArrayQueue() {
        this(null);
    }

    public ArrayQueue(Comparator<T> comparator) {
        setComparator(comparator);
    }

    public ArrayQueue(int capacity) {
        ensureCapacity(capacity);
    }

    @Override
    public void enqueue(T item) {
        if (getComparator() == null) {
            arrayList.insert(item, 0);
        } else {
            arrayList.add(item);
        }

        queueListeners.itemEnqueued(this, item);
    }

    @Override
    public T dequeue() {
        int length = arrayList.getLength();
        if (length == 0) {
            throw new IllegalStateException("queue is empty");
        }

        T item = arrayList.remove(length - 1, 1).get(0);
        queueListeners.itemDequeued(this, item);

        return item;
    }

    @Override
    public T peek() {
        T item = null;
        int length = arrayList.getLength();
        if (length > 0) {
            item = arrayList.get(length - 1);
        }

        return item;
    }

    @Override
    public void clear() {
        if (arrayList.getLength() > 0) {
            arrayList.clear();
            queueListeners.queueCleared(this);
        }
    }

    @Override
    public boolean isEmpty() {
        return (arrayList.getLength() == 0);
    }

    @Override
    public int getLength() {
        return arrayList.getLength();
    }

    public void ensureCapacity(int capacity) {
        arrayList.ensureCapacity(capacity);
    }

    @Override
    public Comparator<T> getComparator() {
        return arrayList.getComparator();
    }

    @Override
    public void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = getComparator();
        arrayList.setComparator(comparator);

        queueListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(arrayList.iterator());
    }

    @Override
    public ListenerList<QueueListener<T>> getQueueListeners() {
        return queueListeners;
    }
}
