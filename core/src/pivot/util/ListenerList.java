/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Abstract base class for listener lists.
 * <p>
 * NOTE This class is not thread safe. For thread-safe management of events,
 * use {@link pivot.util.concurrent.SynchronizedListenerList}.
 *
 * @author gbrown
 */
public abstract class ListenerList<T> implements Iterable<T> {
    /**
     * Represents a node in the linked list of event listeners.
     *
     * @author gbrown
     */
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

    /**
     * Listener list iterator.
     *
     * @author gbrown
     */
    private class NodeIterator implements Iterator<T> {
        private Node node;

        public NodeIterator(Node node) {
            this.node = node;
        }

        public boolean hasNext() {
            return (node != null);
        }

        public T next() {
            if (node == null) {
                throw new NoSuchElementException();
            }

            T listener = node.listener;
            node = node.next;

            return listener;
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * The first node in the list, or <tt>null</tt> if the list is empty.
     */
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
                System.out.println("Duplicate listener " + listener + " added to " + this);
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
            System.out.println("Nonexistent listener " + listener + " removed from " + this);
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

    public Iterator<T> iterator() {
        return new NodeIterator(first);
    }
}
