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
package pivot.collections.concurrent;

import pivot.collections.Stack;

/**
 * Synchronized implementation of the {@link Stack} interface.
 *
 * @author gbrown
 */
public class SynchronizedStack<T> extends SynchronizedCollection<T>
    implements Stack<T> {
    public SynchronizedStack(Stack<T> stack) {
        super(stack);
    }

    public void push(T item) {
        ((Stack<T>)collection).push(item);
    }

    /**
     * Removes an item from the top of the stack, blocking if the stack is
     * currently empty.
     *
     * @return
     * The item at the top of the stack, or null if the removing thread
     * was interrupted.
     */
    public T pop() {
        T item = null;

        try {
            while (isEmpty()) {
                wait();
            }

            item = ((Stack<T>)collection).pop();
        }
        catch(InterruptedException exception) {
        }

        return item;
    }

    public T peek() {
        return ((Stack<T>)collection).peek();
    }

    public T poke(T item) {
        return ((Stack<T>)collection).poke(item);
    }

    public synchronized boolean isEmpty() {
        return ((Stack<T>)collection).isEmpty();
    }
}
