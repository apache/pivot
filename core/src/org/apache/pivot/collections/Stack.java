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

import org.apache.pivot.util.ListenerList;

/**
 * Interface representing a last-in, first-out (LIFO) stack when unsorted, and
 * a priority stack when sorted.
 */
public interface Stack<T> extends Collection<T> {
    /**
     * Stack listener list.
     */
    public static class StackListenerList<T> extends ListenerList<StackListener<T>>
        implements StackListener<T> {
        @Override
        public void itemPushed(Stack<T> stack, T item) {
            for (StackListener<T> listener : this) {
                listener.itemPushed(stack, item);
            }
        }

        @Override
        public void itemPopped(Stack<T> stack, T item) {
            for (StackListener<T> listener : this) {
                listener.itemPopped(stack, item);
            }
        }

        @Override
        public void stackCleared(Stack<T> stack) {
            for (StackListener<T> listener : this) {
                listener.stackCleared(stack);
            }
        }

        @Override
        public void comparatorChanged(Stack<T> stack, Comparator<T> previousComparator) {
            for (StackListener<T> listener : this) {
                listener.comparatorChanged(stack, previousComparator);
            }
        }
    }

    /**
     * "Pushes" an item onto the stack. If the stack is unsorted, the item is
     * added at the top of the stack (<tt>getLength()</tt>). Otherwise, it is
     * inserted at the appropriate index.
     *
     * @param item
     * The item to push onto the stack.
     */
    public void push(T item);

    /**
     * Removes the top item from the stack and returns it.
     *
     * @throws IllegalStateException
     * If the stack contains no items.
     */
    public T pop();

    /**
     * Returns the item on top of the stack without removing it from the stack.
     * Returns null if the stack contains no items. Will also return null if the
     * top item in the stack is null. <tt>getLength()</tt> can be used to
     * distinguish between these two cases.
     */
    public T peek();

    /**
     * Tests the emptiness of the stack.
     *
     * @return
     * <tt>true</tt> if the stack contains no items; <tt>false</tt>,
     * otherwise.
     */
    @Override
    public boolean isEmpty();

    /**
     * Returns the stack depth.
     */
    public int getDepth();

    /**
     * Returns the stack listener list.
     */
    public ListenerList<StackListener<T>> getStackListeners();
}
