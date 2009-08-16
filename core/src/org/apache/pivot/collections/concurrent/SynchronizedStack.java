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

import org.apache.pivot.collections.Stack;
import org.apache.pivot.collections.StackListener;
import org.apache.pivot.util.ListenerList;


/**
 * Synchronized implementation of the {@link Stack} interface.
 */
public class SynchronizedStack<T> extends SynchronizedCollection<T>
    implements Stack<T> {
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

    private SynchronizedStackListenerList<T> stackListeners = new SynchronizedStackListenerList<T>();

    public SynchronizedStack(Stack<T> stack) {
        super(stack);
    }

    public synchronized void push(T item) {
        ((Stack<T>)collection).push(item);
        stackListeners.itemPushed(this, item);

        notify();
    }

    public synchronized T pop() {
        T item = null;
        try {
            while (isEmpty()) {
                wait();
            }

            item = ((Stack<T>)collection).pop();
            stackListeners.itemPopped(this, item);
        }
        catch(InterruptedException exception) {
        }

        return item;
    }

    public synchronized T peek() {
        return ((Stack<T>)collection).peek();
    }

    public synchronized boolean isEmpty() {
        return ((Stack<T>)collection).isEmpty();
    }

    public ListenerList<StackListener<T>> getStackListeners() {
        return stackListeners;
    }
}
