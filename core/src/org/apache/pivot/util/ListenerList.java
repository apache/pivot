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
package org.apache.pivot.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract base class for listener lists.
 * <p>
 * NOTE This class is not inherently thread safe. Subclasses that require
 * thread-safe access should synchronize method access appropriately. Callers
 * must manually synchronize on the listener list instance to ensure thread
 * safety during iteration.
 */
public abstract class ListenerList<T> implements Iterable<T> {
    // Node containing a listener in the list
    private class Node {
        private Node previous;
        private Node next;
        private T listener;

        public Node(Node previous, Node next, T listener) {
            this.previous = previous;
            this.next = next;
            this.listener = listener;
        }
    }

    // Node iterator
    private class NodeIterator implements Iterator<T> {
        private Node node;

        public NodeIterator() {
            this.node = first;
        }

        @Override
        public boolean hasNext() {
            return (node != null);
        }

        @Override
        public T next() {
            if (node == null) {
                throw new NoSuchElementException();
            }

            T listener = node.listener;
            node = node.next;

            return listener;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    // First node in the list (we don't maintain a reference to the last
    // node, since we need to walk the list looking for duplicates on add)
    private Node first = null;

    /**
     * Adds a listener to the list, if it has not previously been added.
     *
     * @param listener
     */
    public void add(T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }

        Node node = first;

        if (node == null) {
            first = new Node(null, null, listener);
        } else {
            while (node.next != null
                && node.listener != listener) {
                node = node.next;
            }

            if (node.next == null
                && node.listener != listener) {
                node.next = new Node(node, null, listener);
            } else {
                System.err.println("Duplicate listener " + listener + " added to " + this);
            }
        }
    }

    /**
     * Removes a listener from the list, if it has previously been added.
     *
     * @param listener
     */
    public void remove(T listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null.");
        }

        Node node = first;
        while (node != null
            && node.listener != listener) {
            node = node.next;
        }

        if (node == null) {
            System.err.println("Nonexistent listener " + listener + " removed from " + this);
        } else {
            if (node.previous == null) {
                first = node.next;

                if (first != null) {
                    first.previous = null;
                }
            } else {
                node.previous.next = node.next;

                if (node.next != null) {
                    node.next.previous = node.previous;
                }
            }
        }
    }

    public boolean isEmpty() {
        return (first == null);
    }

    @Override
    public Iterator<T> iterator() {
        return new NodeIterator();
    }
}
