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
 * Implementation of the {@link Stack} interface that is backed by a linked
 * list.
 */
public class LinkedStack<T> extends LinkedList<T> implements Stack<T> {
    private static final long serialVersionUID = 0;

    private transient StackListenerList<T> stackListeners = new StackListenerList<T>();

    public LinkedStack() {
        this(null);
    }

    public LinkedStack(Comparator<T> comparator) {
        super(comparator);
    }

    public void push(T item) {
        add(item);
        stackListeners.itemPushed(this, item);
    }

    public T pop() {
        int length = getLength();
        if (length == 0) {
            throw new IllegalStateException();
        }

        T item = remove(length - 1, 1).get(0);
        stackListeners.itemPopped(this, item);

        return item;
    }

    public T peek() {
        T item = null;
        int length = getLength();
        if (length > 0) {
            item = get(length - 1);
        }

        return item;
    }

    public boolean isEmpty() {
        return (getLength() == 0);
    }

    public ListenerList<StackListener<T>> getStackListeners() {
        return stackListeners;
    }
}
