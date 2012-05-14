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
 * Stack listener interface.
 */
public interface StackListener<T> {
    /**
     * StackListener adapter.
     */
    public static class Adapter<T> implements StackListener<T> {
        @Override
        public void itemPushed(Stack<T> stack, T item) {
            // empty block
        }

        @Override
        public void itemPopped(Stack<T> stack, T item) {
            // empty block
        }

        @Override
        public void stackCleared(Stack<T> stack) {
            // empty block
        }

        @Override
        public void comparatorChanged(Stack<T> stack, Comparator<T> previousComparator) {
            // empty block
        }
    }

    /**
     * Called when an item has been pushed onto a stack.
     *
     * @param stack
     * @param item
     */
    public void itemPushed(Stack<T> stack, T item);

    /**
     * Called when an item has been popped off of a stack.
     *
     * @param stack
     * @param item
     */
    public void itemPopped(Stack<T> stack, T item);

    /**
     * Called when a stack has been cleared.
     *
     * @param stack
     */
    public void stackCleared(Stack<T> stack);

    /**
     * Called when a stack's comparator has changed.
     *
     * @param stack
     * @param previousComparator
     */
    public void comparatorChanged(Stack<T> stack, Comparator<T> previousComparator);
}
