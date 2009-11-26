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
package org.apache.pivot.demos.rss;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


/**
 * Wraps a W3C DOM {@link org.w3c.dom.NodeList} in a list collection.
 */
public class NodeListAdapter implements List<Node> {
    public class NodeListIterator implements Iterator<Node> {
        private int index = 0;

        @Override
        public boolean hasNext() {
            return (index < nodeList.getLength());
        }

        @Override
        public Node next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }

            return nodeList.item(index++);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private NodeList nodeList;

    private ListListenerList<Node> listListeners = new ListListenerList<Node>();

    public NodeListAdapter(NodeList nodeList) {
        if (nodeList == null) {
            throw new IllegalArgumentException();
        }

        this.nodeList = nodeList;
    }

    @Override
    public int add(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void insert(Node node, int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Sequence<Node> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node update(int index, Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Node get(int index) {
        return nodeList.item(index);
    }

    @Override
    public int indexOf(Node node) {
        int index = 0;
        while (index < nodeList.getLength()
            && node != nodeList.item(index)) {
            index++;
        }

        return (index < nodeList.getLength()) ? index : -1;
    }

    @Override
    public boolean isEmpty() {
        return (nodeList.getLength() == 0);
    }

    @Override
    public int getLength() {
        return nodeList.getLength();
    }

    @Override
    public Comparator<Node> getComparator() {
        return null;
    }

    @Override
    public void setComparator(Comparator<Node> comparator) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<Node> iterator() {
        return new NodeListIterator();
    }

    @Override
    public ListenerList<ListListener<Node>> getListListeners() {
        return listListeners;
    }
}
