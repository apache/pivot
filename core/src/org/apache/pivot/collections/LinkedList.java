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
import java.util.ConcurrentModificationException;
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
    private static final long serialVersionUID = 2100691224732602812L;

    private static class Node<T> implements Serializable {
        private static final long serialVersionUID = -848937850230412572L;

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
        private int index = 0;
        private Node<T> current = null;
        private boolean forward = false;

        private int modificationCountLocal;

        public LinkedListItemIterator() {
            modificationCountLocal = LinkedList.this.modificationCount;
        }

        @Override
        public boolean hasNext() {
            if (modificationCountLocal != LinkedList.this.modificationCount) {
                throw new ConcurrentModificationException();
            }

            return (index < length);
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            if (current == null) {
                current = first;
            } else {
                if (forward) {
                    current = current.next;
                }
            }

            index++;
            forward = true;

            return current.item;
        }

        @Override
        public boolean hasPrevious() {
            if (modificationCountLocal != LinkedList.this.modificationCount) {
                throw new ConcurrentModificationException();
            }

            return (index > 0);
        }

        @Override
        public T previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }

            if (!forward) {
                current = current.previous;
            }

            index--;
            forward = false;

            return current.item;
        }

        @Override
        public void toStart() {
            index = 0;
            current = null;
            forward = true;
        }

        @Override
        public void toEnd() {
            index = length;
            current = last;
            forward = true;
        }

        @Override
        public void insert(T item) {
            Node<T> next = null;
            Node<T> previous = null;

            if (length > 0) {
                if (index == 0) {
                    // Insert at head
                    next = first;
                    // previous = null;  // previous has already this value
                } else if (index < length) {
                    if (forward) {
                        // Insert after current
                        next = current.next;
                        previous = current;
                    } else {
                        // Insert before current
                        next = current;
                        previous = current.previous;
                    }
                } else {
                    // Insert at tail
                    // next = null;  // next has already this value
                    previous = last;
                }

                verifyLocation(item, previous, next);

                if (!forward) {
                    index++;
                }
            }

            LinkedList.this.insert(item, previous, next);

            length++;
            modificationCountLocal++;
            LinkedList.this.modificationCount++;

            if (listListeners != null) {
                listListeners.itemInserted(LinkedList.this, index);
            }
        }

        @Override
        public void update(T item) {
            if (current == null) {
                throw new IllegalStateException();
            }

            T previousItem = current.item;
            if (previousItem != item) {
                // Verify that the item is allowed at the specified index
                verifyLocation(item, current.previous, current.next);

                current.item = item;
                modificationCountLocal++;
                LinkedList.this.modificationCount++;

                if (listListeners != null) {
                    listListeners.itemUpdated(LinkedList.this, index, previousItem);
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
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

            if (forward) {
                current = current.previous;
                index--;
            }

            length--;
            modificationCountLocal++;
            LinkedList.this.modificationCount++;

            if (listListeners != null) {
                LinkedList<T> removed = new LinkedList<T>(item);

                listListeners.itemsRemoved(LinkedList.this, index, removed);
            }
        }
    }

    private Node<T> first = null;
    private Node<T> last = null;
    private int length = 0;

    private Comparator<T> comparator = null;

    private transient int modificationCount = 0;
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

    @Override
    public int add(T item) {
        int index;

        if (comparator == null) {
            index = length;
            insert(item, index);
        } else {
            index = 0;

            Node<T> next = null;
            Node<T> previous = null;

            if (length > 0) {
                next = first;
                while (next != null
                    && comparator.compare(item, next.item) > 0) {
                    next = next.next;
                    index++;
                }

                previous = (next == null) ? last : next.previous;
            }

            insert(item, previous, next);

            length++;
            modificationCount++;

            if (listListeners != null) {
                listListeners.itemInserted(this, index);
            }
        }

        return index;
    }

    @Override
    public void insert(T item, int index) {
        verifyIndexBounds(index, 0, length);

        Node<T> next = null;
        Node<T> previous = null;

        if (length > 0) {
            next = (index == length) ? null : getNode(index);
            previous = (next == null) ? last : next.previous;

            verifyLocation(item, previous, next);
        }

        insert(item, previous, next);

        length++;
        modificationCount++;

        if (listListeners != null) {
            listListeners.itemInserted(this, index);
        }
    }

    private void insert(T item, Node<T> previous, Node<T> next) {
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

    @Override
    public T update(int index, T item) {
        verifyIndexBounds(index, 0, length - 1);

        // Get the previous item at index
        Node<T> node = getNode(index);
        T previousItem = node.item;

        if (previousItem != item) {
            // Verify that the item is allowed at the specified index
            verifyLocation(item, node.previous, node.next);

            // Update the item
            node.item = item;
            modificationCount++;

            if (listListeners != null) {
                listListeners.itemUpdated(this, index, previousItem);
            }
        }

        return previousItem;
    }

    private void verifyLocation(T item, Node<T> previous, Node<T> next) {
        if (comparator != null) {
            // Ensure that the new item is greater or equal to its
            // predecessor and less than or equal to its successor
            if ((previous != null
                && comparator.compare(item, previous.item) < 0)
                || (next != null
                && comparator.compare(item, next.item) > 0)) {
                throw new IllegalArgumentException("Illegal item modification.");
            }
        }
    }

    @Override
    public int remove(T item) {
        int index = 0;

        LinkedListItemIterator nodeIterator = new LinkedListItemIterator();
        while (nodeIterator.hasNext()) {
            if (nodeIterator.next() == item) {
                nodeIterator.remove();
                break;
            }

            index++;
        }

        if (!nodeIterator.hasNext()) {
            index = -1;
        }

        return index;
    }

    @Override
    public Sequence<T> remove(int index, int count) {
        verifyIndexBounds(index, count, 0, length);

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
            modificationCount++;

            if (listListeners != null) {
                listListeners.itemsRemoved(this, index, removed);
            }
        }

        return removed;
    }

    @Override
    public void clear() {
        if (length > 0) {
            first = null;
            last = null;
            length = 0;
            modificationCount++;

            if (listListeners != null) {
                listListeners.listCleared(this);
            }
        }
    }

    @Override
    public T get(int index) {
        verifyIndexBounds(index, 0, length - 1);

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

    @Override
    public int indexOf(T item) {
        int index = 0;

        Node<T> node = first;
        while (node != null) {
            if (item == null) {
                if (node.item == null) {
                    break;
                }
            } else {
                if (item.equals(node.item)) {
                    break;
                }
            }

            node = node.next;
            index++;
        }

        if (node == null) {
            index = -1;
        }

        return index;
    }

    @Override
    public boolean isEmpty() {
        return (length == 0);
    }

    @Override
    public int getLength() {
        return length;
    }

    @Override
    public Comparator<T> getComparator() {
        return comparator;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setComparator(Comparator<T> comparator) {
        Comparator<T> previousComparator = this.comparator;

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

            modificationCount++;
        }

        // Set the new comparator
        this.comparator = comparator;

        if (listListeners != null) {
            listListeners.comparatorChanged(this, previousComparator);
        }
    }

    @Override
    public ItemIterator<T> iterator() {
        return new LinkedListItemIterator();
    }

    @Override
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

        if (this == o) {
            equals = true;
        } else if (o instanceof List) {
            List<T> otherList = (List<T>) o;

            if (length == otherList.getLength()) {
                Iterator<T> iterator = otherList.iterator();
                equals = true;

                for (T element : this) {
                    if (!(iterator.hasNext()
                        && element.equals(iterator.next()))) {
                        equals = false;
                        break;
                    }
                }
            }
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

    private static void verifyIndexBounds(int index, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end (" + end + ") < " + "start (" + start + ")");
        }
        if (index < start || index > end) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds.");
        }
    }

    private static void verifyIndexBounds(int index, int count, int start, int end) {
        if (end < start) {
            throw new IllegalArgumentException("end (" + end + ") < " + "start (" + start + ")");
        }
        if (count < 0 || start < 0) {
            throw new IllegalArgumentException("count (" + count + ") < 0 or start (" + start + ") < 0");
        }
        if (index < start) {
            throw new IndexOutOfBoundsException("index " + index + " out of bounds.");
        }
        if (index + count > end) {
            throw new IndexOutOfBoundsException("index + count " + index + "," + count + " out of range.");
        }
    }
}
