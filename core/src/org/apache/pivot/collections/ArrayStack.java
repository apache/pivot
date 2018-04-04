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
import org.apache.pivot.util.Utils;

/**
 * Implementation of the {@link Stack} interface that is backed by an array.
 */
public class ArrayStack<T> implements Stack<T>, Serializable {
    private static final long serialVersionUID = 3175064065273930731L;

    private ArrayList<T> arrayList = new ArrayList<>();
    private int maxDepth = 0;
    private transient StackListener.Listeners<T> stackListeners = new StackListener.Listeners<>();

    public ArrayStack() {
        this(null);
    }

    public ArrayStack(Comparator<T> comparator) {
        setComparator(comparator);
    }

    public ArrayStack(int capacity) {
        ensureCapacity(capacity);
    }

    public ArrayStack(int capacity, int maxDepth) {
        ensureCapacity(capacity);
        setMaxDepth(maxDepth);
    }

    public ArrayStack(int capacity, int maxDepth, Comparator<T> comparator) {
        ensureCapacity(capacity);
        setMaxDepth(maxDepth);
        setComparator(comparator);
    }

    /**
     * @return The maximum depth this stack is permitted to reach,
     * where 0 means unlimited.
     */
    @Override
    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     * Set the maximum depth permitted for this stack, 0 means unlimited.
     *
     * @param maxDepth The new maximum depth for this stack.
     */
    @Override
    public void setMaxDepth(int maxDepth) {
        Utils.checkNonNegative(maxDepth, "maxDepth");
        this.maxDepth = maxDepth;
    }

    @Override
    public void push(T item) {
        arrayList.add(item);
        stackListeners.itemPushed(this, item);

        // Now check for too many items on this stack
        if (maxDepth > 0 && arrayList.getLength() > maxDepth) {
            arrayList.remove(0, 1);
        }
    }

    @Override
    public T pop() {
        int length = arrayList.getLength();
        if (length == 0) {
            throw new IllegalStateException((getComparator() == null ? "stack" : "queue") + " is empty");
        }

        T item = arrayList.remove(length - 1, 1).get(0);
        stackListeners.itemPopped(this, item);

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
            stackListeners.stackCleared(this);
        }
    }

    @Override
    public boolean isEmpty() {
        return (arrayList.getLength() == 0);
    }

    @Override
    public int getDepth() {
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

        stackListeners.comparatorChanged(this, previousComparator);
    }

    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<>(arrayList.iterator());
    }

    @Override
    public ListenerList<StackListener<T>> getStackListeners() {
        return stackListeners;
    }
}
