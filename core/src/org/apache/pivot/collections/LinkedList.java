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
    private static class Node<T> implements Serializable {
        private static final long serialVersionUID = 0;
        private Node<T> previous;
        private Node<T> next;
        private T item;

        public Node(Node<T> previous, Node<T> next, T item) {
            this.previous = previous;
            this.next = next;
            this.item = item;
        }
    }

    private class LinkedListItemIterator implements ItemIterator<T> {
        private boolean reverse;
        private int index = -1;
        private Node<T> current;
        private Node<T> next;

        public LinkedListItemIterator() {
            this(false);
        }

        public LinkedListItemIterator(boolean reverse) {
            this.reverse = reverse;

            current = null;
            next = (reverse) ? last : first;
        }

        public boolean hasNext() {
            return (next != null);
        }

        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            T item = next.item;
            current = next;
            index++;
            next = (reverse) ? next.previous : next.next;

            return item;
        }

        public boolean hasPrevious() {
            return (next != null);
        }

        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            T item = next.item;
            current = next;
            index--;
            next = (reverse) ? next.next: next.previous;

            return item;
        }

        public void insert(T item) {
            if (current == null) {
                throw new IllegalStateException();
            }

            // Insert a new node immediately prior to the current node
            Node<T> node = new Node<T>(current.previous, current, item);

            if (current.previous == null) {
                first = node;
            } else {
                current.previous.next = node;
            }

            current.previous = node;
            length++;

            if (listListeners != null) {
                listListeners.itemInserted(LinkedList.this, index);
            }
        }

        public void update(T item) {
            if (current == null) {
                throw new IllegalStateException();
            }

            T previousItem = current.item;
            current.item = item;

            if (listListeners != null) {
                listListeners.itemUpdated(LinkedList.this, index, previousItem);
            }
        }

        public void remove() {
            if (current == null) {
                throw new IllegalStateException();
            }

            // Unlink the current node from its predecessor and successor
            T item = current.item;

            if (current.previous == null) {
                first = current.next;
            } else {
                current.previous.next = current.next;
            }

            if (current.next == null) {
                last = current.previous;
            } else {
                current.next.previous = current.previous;
            }

            length--;

            if (listListeners != null) {
                LinkedList<T> removed = new LinkedList<T>();
                removed.add(item);

                listListeners.itemsRemoved(LinkedList.this, index, removed);
            }
        }
    }

    private static final long serialVersionUID = 0;

    private Node<T> first = null;
    private Node<T> last = null;
    private int length = 0;

    private Comparator<T> comparator = null;
    private transient ListListenerList<T> listListeners;

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
            LinkedListItemIterator nodeIterator = new LinkedListItemIterator();
            while (nodeIterator.hasNext()
                && comparator.compare(item, nodeIterator.next()) > 0) {
                index++;
            }

            if (nodeIterator.hasNext()
                && index > 0) {
                // Insert the new node here
                Node<T> node = new Node<T>(nodeIterator.next, nodeIterator.next.next, item);
                nodeIterator.next.next = node;
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
            Node<T> node = new Node<T>(null, null, item);
            first = node;
            last = node;
        } else {
            Node<T> next = (index == length) ? null : getNode(index);
            Node<T> previous = (next == null) ? last : next.previous;

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

            Node<T> node = new Node<T>(previous, next, item);
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

        if (listListeners != null) {
            listListeners.itemInserted(this, index);
        }
    }

    public T update(int index, T item) {
        if (index < 0
            || index >= length) {
            throw new IndexOutOfBoundsException();
        }

        // Get the previous item at index
        Node<T> node = getNode(index);
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

        if (listListeners != null) {
            listListeners.itemUpdated(this, index, previousItem);
        }

        return previousItem;
    }

    public int remove(T item) {
        int index = 0;

        LinkedListItemIterator nodeIterator = new LinkedListItemIterator();
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
            Node<T> start = getNode(index);
            Node<T> end = start;
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

            if (listListeners != null) {
                listListeners.itemsRemoved(this, index, removed);
            }
        }

        return removed;
    }

    public void clear() {
        if (length > 0) {
            first = null;
            last = null;
            length = 0;

            if (listListeners != null) {
                listListeners.listCleared(this);
            }
        }
    }

    public T get(int index) {
        if (index < 0
            || index >= length) {
            throw new IndexOutOfBoundsException();
        }

        Node<T> node = getNode(index);
        return node.item;
    }

    private Node<T> getNode(int index) {
        Node<T> node;
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

        LinkedListItemIterator nodeIterator = new LinkedListItemIterator();
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

                Node<T> node = null;
                for (i = 0; i < length; i++) {
                    Node<T> previousNode = node;
                    node = new Node<T>(previousNode, null, array[i]);

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

            if (listListeners != null) {
                listListeners.comparatorChanged(this, previousComparator);
            }
        }
    }

    public ItemIterator<T> iterator() {
        return iterator(false);
    }

    public ItemIterator<T> iterator(boolean reverse) {
        return new LinkedListItemIterator(reverse);
    }

    public ListenerList<ListListener<T>> getListListeners() {
        if (listListeners == null) {
            listListeners = new ListListenerList<T>();
        }

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
