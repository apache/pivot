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
package org.apache.pivot.collections.concurrent;

import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.Stack;
import org.apache.pivot.collections.StackListener;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Synchronized implementation of the {@link Stack} interface.
 */
public class SynchronizedStack<T> implements Stack<T> {
    private static class SynchronizedStackListenerList<T>
        extends StackListenerList<T> {
        @Override
        public synchronized void add(StackListener<T> listener) {
            super.add(listener);
        }

        @Override
        public synchronized void remove(StackListener<T> listener) {
            super.remove(listener);
        }

        @Override
        public synchronized void itemPushed(Stack<T> stack, T item) {
            super.itemPushed(stack, item);
        }

        @Override
        public synchronized void itemPopped(Stack<T> stack, T item) {
            super.itemPopped(stack, item);
        }
    }

    private Stack<T> stack;
    private SynchronizedStackListenerList<T> stackListeners = new SynchronizedStackListenerList<T>();

    public SynchronizedStack(Stack<T> stack) {
        if (stack == null) {
            throw new IllegalArgumentException("stack cannot be null.");
        }

        this.stack = stack;
    }

    @Override
    public synchronized void push(T item) {
        stack.push(item);
        stackListeners.itemPushed(this, item);

        notify();
    }

    @Override
    public synchronized T pop() {
        T item = null;
        try {
            while (stack.isEmpty()) {
                wait();
            }

            item = stack.pop();
            stackListeners.itemPopped(this, item);
        }
        catch(InterruptedException exception) {
            // empty block
        }

        return item;
    }

    @Override
    public synchronized T peek() {
        return stack.peek();
    }

    @Override
    public synchronized void clear() {
        if (!stack.isEmpty()) {
            stack.clear();
            stackListeners.stackCleared(this);
        }
    }

    @Override
    public synchronized boolean isEmpty() {
        return stack.isEmpty();
    }

    @Override
    public synchronized int getDepth() {
        return stack.getDepth();
    }

    @Override
    public synchronized Comparator<T> getComparator() {
        return stack.getComparator();
    }

    @Override
    public synchronized void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = getComparator();
        stack.setComparator(comparator);
        stackListeners.comparatorChanged(this, previousComparator);
    }

    /**
     * NOTE Callers must manually synchronize on the SynchronizedStack
     * instance to ensure thread safety during iteration.
     */
    @Override
    public Iterator<T> iterator() {
        return new ImmutableIterator<T>(stack.iterator());
    }

    @Override
    public ListenerList<StackListener<T>> getStackListeners() {
        return stackListeners;
    }
}
