/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.text;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 * Abstract base class for elements.
 *
 * @author gbrown
 */
public abstract class Element extends Node implements Sequence<Node> {
    private static class ElementListenerList extends ListenerList<ElementListener>
        implements ElementListener {
        public void nodeInserted(Element element, int index) {
            for (ElementListener listener : this) {
                listener.nodeInserted(element, index);
            }
        }

        public void nodeUpdated(Element element, int index, Node previousNode) {
            for (ElementListener listener : this) {
                listener.nodeUpdated(element, index, previousNode);
            }
        }

        public void nodesRemoved(Element element, int index, Sequence<Node> nodes) {
            for (ElementListener listener : this) {
                listener.nodesRemoved(element, index, nodes);
            }
        }
    }

    private int characterCount = 0;
    private ArrayList<Node> nodes = new ArrayList<Node>();

    private ElementListenerList elementListeners = new ElementListenerList();

    public Element() {
    }

    public Element(Element element, boolean recursive) {
        // TODO
    }

    @Override
    public void insertRange(Node range, int offset) {
        if (!(range instanceof Element)) {
            throw new IllegalArgumentException("range is not an element.");
        }

        Element element = (Element)range;
        int n = element.getLength();

        // Get the index of the node at the given offset
        int index = getIndexAt(offset);

        if (index < 0) {
            // No node intersects with this offset; insert the range contents
            index = -(index + 1);

            for (int i = 0; i < n; i++) {
                insert(element.get(i), index + i);
            }
        } else {
            // The offset intersects with a node; splice the range into it
            Node node = get(index);

            // Split the node
            int spliceOffset = offset - node.getOffset();
            node = node.removeRange(spliceOffset, node.getCharacterCount()
                - spliceOffset);

            // Insert the range contents
            for (int i = 0; i < n; i++) {
                insert(element.get(i), index + i + 1);
            }

            // Insert the remainder of the node
            insert(node, index + n + 1);
        }

        // Fire event and notify parent
        rangeInserted(range, offset);
    }

    @Override
    public Node removeRange(int offset, int characterCount) {
        // Create a copy of this element
        Node range = duplicate(false);

        // TODO Get the nodes at offset and offset + characterCount; if defined,
        // split the nodes and add the segments to the element copy

        // TODO Remove all intervening nodes and add them to the copy

        // Fire event and notify parent
        rangeRemoved(offset, range);

        return range;
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        // TODO Create a copy of this element (possibly via a duplicate() call)

        // TODO Get the nodes at offset and offset + characterCount; if defined,
        // get ranges from the nodes and add the segments to the element copy

        // TODO Add all intervening nodes to the copy

        return null;
    }

    @Override
    public int getCharacterCount() {
        return characterCount;
    }

    public int add(Node node) {
        int index = nodes.getLength();
        insert(node, index);

        return index;
    }

    public void insert(Node node, int index) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        if (node.getParent() != null) {
            throw new IllegalArgumentException("node already has a parent.");
        }

        if (node == this) {
            throw new IllegalArgumentException("Cannot add an element to itself.");
        }

        node.setParent(this);
        nodes.insert(node, index);

        // TODO Set the node's offset

        // TODO Update the offsets of successive nodes

        rangeInserted(node, node.getOffset());
    }

    public Node update(int index, Node node) {
        throw new UnsupportedOperationException();
    }

    public int remove(Node node) {
        int index = indexOf(node);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    public Sequence<Node> remove(int index, int count) {
        Sequence<Node> removed = nodes.remove(index, count);

        Node range = duplicate(false);

        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Node node = removed.get(i);

            // TODO Clear the node's offset & parent

            // TODO Add the node to the range
        }

        // TODO Update the offsets of successive nodes

        if (removed.getLength() > 0) {
            rangeRemoved(index, range);
        }

        return removed;
    }

    public Node get(int index) {
        return nodes.get(index);
    }

    public int indexOf(Node node) {
        // TODO Perform a binary search based on the node's offset

        return -1;
    }

    public int getLength() {
        return nodes.getLength();
    }

    public int getIndexAt(int offset) {
        // TODO
        return -1;
    }

    public Sequence<Integer> getPathAt(int offset) {
        // TODO
        return null;
    }

    public Node getNodeAt(int offset) {
        Node node = null;

        int index = getIndexAt(offset);
        if (index != -1) {
            node = nodes.get(index);
        }

        return node;
    }

    public Node getDescendantAt(int offset) {
        // TODO
        return null;
    }

    public void normalize() {
        // TODO Recursively consolidate all contiguous text nodes
    }

    @Override
    protected void rangeInserted(Node range, int offset) {
        characterCount += range.getCharacterCount();
        super.rangeInserted(range, offset);
    }

    @Override
    protected void rangeRemoved(int offset, Node range) {
        characterCount -= range.getCharacterCount();
        super.rangeInserted(range, offset);
    }

    /**
     * Returns the element listener list.
     */
    public ListenerList<ElementListener> getElementListeners() {
        return elementListeners;
    }
}
