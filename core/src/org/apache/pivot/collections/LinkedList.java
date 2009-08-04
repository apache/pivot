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
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.util.ListenerList;


/**
 * Implementation of the {@link List} interface that is backed by a linked
 * list.
 * <p>
 * NOTE This class is not thread-safe. For concurrent access, use a
 * {@link org.apache.pivot.collections.concurrent.SynchronizedList}.
 */
public class LinkedList<T> implements List<T>, Serializable {
    // Node containing an item in the list
    private class Node implements Serializable {
        private static final long serialVersionUID = 0;
        private Node previous;
        private Node next;
        private T item;

        public Node(Node previous, Node next, T item) {
            this.previous = previous;
            this.next = next;
            this.item = item;
        }
    }

    // Node iterator
    private class NodeIterator implements Iterator<T> {
        private Node node;

        public NodeIterator() {
            this.node = first;
        }

        public boolean hasNext() {
            return (node != null);
        }

        public T next() {
            if (node == null) {
                throw new NoSuchElementException();
            }

            T item = node.item;
            node = node.next;

            return item;
        }

        public void remove() {
            // Unlink this node from its predecessor and successor
            if (node.previous != null) {
                node.previous.next = node.next;
            }

            if (node.next != null) {
                node.next.previous = node.previous;
            }
        }
    }

    private static final long serialVersionUID = 0;

    private Node first = null;
    private Node last = null;
    private int length = 0;

    private Comparator<T> comparator = null;
    private transient ListListenerList<T> listListeners = new ListListenerList<T>();

    public LinkedList() {
    }

    public LinkedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    public LinkedList(T... items) {
        for (int i = 0; i < items.length; i++) {
            add(items[i]);
        }
    }

    public LinkedList(Sequence<T> items) {
        if (items == null) {
            throw new IllegalArgumentException();
        }

        for (int i = 0, n = items.getLength(); i < n; i++) {
            add(items.get(i));
        }
    }

    public int add(T item) {
        int index;
        if (comparator == null) {
            // Append to the tail
            index = length;
            insert(item, index);
        } else {
            // Find the insertion point
            index = 0;
            NodeIterator nodeIterator = new NodeIterator();
            while (nodeIterator.hasNext()
                && comparator.compare(item, nodeIterator.next()) > 0) {
                index++;
            }

            if (nodeIterator.hasNext()
                && index > 0) {
                // Insert the new node here
                Node node = new Node(nodeIterator.node, nodeIterator.node.next, item);
                nodeIterator.node.next = node;
                node.next.previous = node;
                length++;
            } else {
                // Insert at the head or append to the tail
                insert(item, index, false);
            }
        }

        return index;
    }

    public void insert(T item, int index) {
        insert(item, index, true);
    }

    private void insert(T item, int index, boolean validate) {
        if (index < 0
            || index > length) {
            throw new IndexOutOfBoundsException();
        }

        if (length == 0) {
            Node node = new Node(null, null, item);
            first = node;
            last = node;
        } else {
            Node next = (index == length) ? null : getNode(index);
            Node previous = (next == null) ? last : next.previous;

            if (comparator != null) {
                // Ensure that the new item is greater or equal to its
                // predecessor and less than or equal to its successor
                if ((previous != null
                        && comparator.compare(item, previous.item) == -1)
                    || (next != null
                        && comparator.compare(item, next.item) == 1)) {
                    throw new IllegalArgumentException("Illegal item modification.");
                }
            }

            Node node = new Node(previous, next, item);
            if (previous == null) {
                first = node;
            } else {
                previous.next = node;
            }

            if (next == null) {
                last = node;
            } else {
                next.previous = node;
            }
        }

        // Update length and notify listeners
        length++;
        listListeners.itemInserted(this, index);
    }

    public T update(int index, T item) {
        if (index < 0
            || index >= length) {
            throw new IndexOutOfBoundsException();
        }

        // Get the previous item at index
        Node node = getNode(index);
        T previousItem = node.item;

        if (previousItem != item) {
            if (comparator != null) {
                // Ensure that the new item is greater or equal to its
                // predecessor and less than or equal to its successor
                if ((node.previous != null
                        && comparator.compare(item, node.previous.item) == -1)
                    || (node.next != null
                        && comparator.compare(item, node.next.item) == 1)) {
                    throw new IllegalArgumentException("Illegal item modification.");
                }
            }

            // Update the item
            node.item = item;
        }

        listListeners.itemUpdated(this, index, previousItem);

        return previousItem;
    }

    public int remove(T item) {
        int index = 0;

        NodeIterator nodeIterator = new NodeIterator();
        while (nodeIterator.hasNext()) {
            if (nodeIterator.next() == item) {
                nodeIterator.remove();
                break;
            } else {
                index++;
            }
        }

        if (!nodeIterator.hasNext()) {
            index = -1;
        }

        return index;
    }

    public Sequence<T> remove(int index, int count) {
        if (index < 0
            || index + count > length) {
            throw new IndexOutOfBoundsException();
        }

        LinkedList<T> removed = new LinkedList<T>();

        if (count > 0) {
            // Identify the bounding nodes and build the removed item list
            Node start = getNode(index);
            Node end = start;
            for (int i = 0; i < count; i++) {
                removed.add(end.item);
                end = end.next;
            }

            if (end == null) {
                end = last;
            } else {
                end = end.previous;
            }

            // Decouple the nodes from the list
            if (start.previous != null) {
                start.previous.next = end.next;
            }

            if (index + count == length) {
                last = start.previous;
            }

            if (end.next != null) {
                end.next.previous = start.previous;
            }

            if (index == 0) {
                first = end.next;
            }

            start.previous = null;
            end.next = null;

            // Update length and notify listeners
            length -= count;
            listListeners.itemsRemoved(this, index, removed);
        }

        return removed;
    }

    public void clear() {
        if (length > 0) {
            first = null;
            last = null;
            length = 0;

            listListeners.listCleared(this);
        }
    }

    public T get(int index) {
        if (index < 0
            || index >= length) {
            throw new IndexOutOfBoundsException();
        }

        Node node = getNode(index);
        return node.item;
    }

    private Node getNode(int index) {
        Node node;
        if (index == 0) {
            node = first;
        } else if (index == length - 1) {
            node = last;
        } else {
            if (index < length / 2) {
                node = first;
                for (int i = 0; i < index; i++) {
                    node = node.next;
                }
            } else {
                node = last;
                for (int i = length - 1; i > index; i--) {
                    node = node.previous;
                }
            }
        }

        return node;
    }

    public int indexOf(T item) {
        int index = 0;

        NodeIterator nodeIterator = new NodeIterator();
        while (nodeIterator.hasNext()) {
            if (nodeIterator.next() == item) {
                break;
            } else {
                index++;
            }
        }

        if (!nodeIterator.hasNext()) {
            index = -1;
        }

        return index;
    }

    public int getLength() {
        return length;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    public void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = this.comparator;

        if (previousComparator != comparator) {
            if (comparator != null) {
                // Copy the nodes into an array and sort
                T[] array = (T[])new Object[length];

                int i = 0;
                for (T item : this) {
                    array[i++] = item;
                }

                Arrays.sort(array, comparator);

                // Rebuild the node list
                first = null;

                Node node = null;
                for (i = 0; i < length; i++) {
                    Node previousNode = node;
                    node = new Node(previousNode, null, array[i]);

                    if (previousNode == null) {
                        first = node;
                    } else {
                        previousNode.next = node;
                    }
                }

                last = node;
            }

            // Set the new comparator
            this.comparator = comparator;

            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    public Iterator<T> iterator() {
        return new NodeIterator();
    }

    public ListenerList<ListListener<T>> getListListeners() {
        return listListeners;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object o) {
        boolean equals = false;

        if (o instanceof LinkedList<?>) {
            LinkedList<T> linkedList = (LinkedList<T>)o;

            Iterator<T> iterator = iterator();
            Iterator<T> linkedListIterator = linkedList.iterator();

            while (iterator.hasNext()
                && linkedListIterator.hasNext()
                && iterator.next().equals(linkedListIterator.next()));

            equals = (!iterator.hasNext()
                && !linkedListIterator.hasNext());
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hashCode = 1;
        for (T item : this) {
            hashCode = 31 * hashCode + (item == null ? 0 : item.hashCode());
        }

        return hashCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getClass().getName());
        sb.append(" [");

        int i = 0;
        for (T item : this) {
            if (i > 0) {
                sb.append(", ");
            }

            sb.append(item);
            i++;
        }

        sb.append("]");

        return sb.toString();
    }
}
