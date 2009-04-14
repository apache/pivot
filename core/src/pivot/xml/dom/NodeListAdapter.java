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
package pivot.xml.dom;

import java.util.Comparator;
import java.util.Iterator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Wraps a W3C DOM {@link org.w3c.dom.NodeList} in a list collection.
 * <p>
 * NOTE This class is incomplete.
 *
 * @author gbrown
 */
public class NodeListAdapter implements List<Node> {
    private NodeList nodeList;

    private ListListenerList<Node> listListeners = new ListListenerList<Node>();

    public NodeListAdapter(NodeList nodeList) {
        if (nodeList == null) {
            throw new IllegalArgumentException();
        }

        this.nodeList = nodeList;
    }

    public int add(Node node) {
        throw new UnsupportedOperationException();
    }

    public void insert(Node node, int index) {
        throw new UnsupportedOperationException();
    }

    public int remove(Node node) {
        throw new UnsupportedOperationException();
    }

    public Sequence<Node> remove(int index, int count) {
        throw new UnsupportedOperationException();
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public Node update(int index, Node node) {
        throw new UnsupportedOperationException();
    }

    public Node get(int index) {
        return nodeList.item(index);
    }

    public int indexOf(Node node) {
        // TODO
        return -1;
    }

    public int getLength() {
        return nodeList.getLength();
    }

    public Comparator<Node> getComparator() {
        return null;
    }

    public void setComparator(Comparator<Node> comparator) {
        throw new UnsupportedOperationException();
    }

    public Iterator<Node> iterator() {
        // TODO
        return null;
    }

    public ListenerList<ListListener<Node>> getListListeners() {
        return listListeners;
    }
}
