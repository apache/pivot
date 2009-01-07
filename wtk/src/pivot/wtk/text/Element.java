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

import java.util.Comparator;
import java.util.Iterator;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * Abstract base class for elements.
 * <p>
 * TODO Add style properties.
 *
 * @author gbrown
 */
public abstract class Element extends Node
    implements Sequence<Node>, Iterable<Node> {
    /**
     * Private node class that simply represents an offset value. Used to
     * perform binary searches using offsets rather than actual nodes.
     *
     * @author gbrown
     */
    private static class NullNode extends Node {
        public void insertRange(Node range, int offset) {
            throw new UnsupportedOperationException();
        }

        public Node removeRange(int offset, int characterCount) {
            throw new UnsupportedOperationException();
        }

        public Node getRange(int offset, int characterCount) {
            return duplicate(false);
        }

        public int getCharacterCount() {
            return 0;
        }

        public Node duplicate(boolean recursive) {
            return new NullNode();
        }
    }

    /**
     * Comparator used to perform binary searches on child nodes.
     *
     * @author gbrown
     */
    private static class OffsetComparator implements Comparator<Node> {
        public int compare(Node leftNode, Node rightNode) {
            int leftOffset = leftNode.getOffset();
            int leftCharacterCount = leftNode.getCharacterCount();

            int rightOffset = rightNode.getOffset();

            int result;
            if (leftOffset > rightOffset) {
                result = 1;
            } else if (leftOffset + leftCharacterCount <= rightOffset) {
                result = -1;
            } else {
                result = 0;
            }

            return result;
        }
    }

    /**
     * Element listener list.
     *
     * @author gbrown
     */
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

    private static NullNode nullNode = new NullNode();

    private static final OffsetComparator OFFSET_COMPARATOR = new OffsetComparator();

    public Element() {
    }

    public Element(Element element, boolean recursive) {
        if (recursive) {
            for (Node node : element) {
                add(node.duplicate(true));
            }
        }
    }

    @Override
    public void insertRange(Node range, int offset) {
        if (!(range instanceof Element)) {
            throw new IllegalArgumentException("range is not an element.");
        }

        if (offset < 0
            || offset > characterCount) {
            throw new IndexOutOfBoundsException();
        }

        if (range.getCharacterCount() > 0) {
            Element element = (Element)range;
            int n = element.getLength();

            // Remove the nodes from the range element
            Sequence<Node> nodes = element.remove(0, n);

            if (offset == characterCount) {
                // Append the range contents
                for (int i = 0; i < n; i++) {
                    add(nodes.get(i));
                }
            } else {
                // Merge the range contents
                int index = getIndexAt(offset);
                Node leadingSegment = get(index);

                int spliceOffset = offset - leadingSegment.getOffset();
                Node trailingSegment = leadingSegment.removeRange(spliceOffset,
                    leadingSegment.getCharacterCount() - spliceOffset);

                for (int i = 0; i < n; i++) {
                    insert(nodes.get(i), index + i + 1);
                }

                // Insert the remainder of the node
                insert(trailingSegment, index + n + 1);
            }
        }
    }

    @Override
    public Node removeRange(int offset, int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        if (offset < 0
            || offset + characterCount > this.characterCount) {
            throw new IndexOutOfBoundsException();
        }

        // Create a copy of this element
        Node range = duplicate(false);

        if (characterCount > 0) {
            Element element = (Element)range;

            int start = getIndexAt(offset);
            int end = getIndexAt(offset + characterCount - 1);

            if (start == end) {
                // The range is entirely contained by one child node
                Node node = get(start);
                element.add(node.removeRange(offset - node.getOffset(), characterCount));
            } else {
                // The range spans multiple child nodes; locate the first node
                Node leadingSegment = null;
                Node startNode = get(start);

                int leadingSegmentOffset = offset - startNode.getOffset();
                leadingSegment = startNode.removeRange(leadingSegmentOffset,
                    startNode.getCharacterCount() - leadingSegmentOffset);

                characterCount -= leadingSegment.getCharacterCount();
                start++;

                // Locate the last node
                Node trailingSegment = null;
                Node endNode = get(end);

                int trailingSegmentCharacterCount = (offset + characterCount)
                    - endNode.getOffset();
                trailingSegment = endNode.removeRange(0, trailingSegmentCharacterCount);

                // Remove the intervening nodes
                int count = end - start;
                Sequence<Node> removedNodes = remove(start, count);

                // Add the removed segments and nodes to the range
                if (leadingSegment != null
                    && leadingSegment.getCharacterCount() > 0) {
                    element.add(leadingSegment);
                }

                for (int i = 0, n = removedNodes.getLength(); i < n; i++) {
                    element.add(removedNodes.get(i));
                }

                if (trailingSegment != null
                    && trailingSegment.getCharacterCount() > 0) {
                    element.add(trailingSegment);
                }
            }
        }

        return range;
    }

    @Override
    public Node getRange(int offset, int characterCount) {
        if (characterCount < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        if (offset < 0
            || offset + characterCount > this.characterCount) {
            throw new IndexOutOfBoundsException();
        }

        // Create a copy of this element
        Node range = duplicate(false);

        if (characterCount > 0) {
            Element element = (Element)range;

            int start = getIndexAt(offset);
            int end = getIndexAt(offset + characterCount - 1);

            if (start == end) {
                // The range is entirely contained by one child node
                Node node = get(start);
                element.add(node.getRange(offset - node.getOffset(), characterCount));
            } else {
                // The range spans multiple child nodes
                Node leadingSegment = null;

                if (start < 0) {
                    start = -(start + 1);
                } else {
                    Node startNode = get(start);

                    int leadingSegmentOffset = offset - startNode.getOffset();
                    leadingSegment = startNode.getRange(leadingSegmentOffset,
                        startNode.getCharacterCount() - leadingSegmentOffset);
                }

                Node trailingSegment = null;

                if (end < 0) {
                    end = -(end + 1);
                } else {
                    Node endNode = get(end);

                    int trailingSegmentCharacterCount = (offset + characterCount)
                        - endNode.getOffset();
                    trailingSegment = endNode.getRange(0, trailingSegmentCharacterCount);
                }

                // Add the leading segment to the range
                if (leadingSegment != null
                    && leadingSegment.getCharacterCount() > 0) {
                    element.add(leadingSegment);
                    start++;
                }

                // Duplicate the intervening nodes
                for (int i = start; i < end; i++) {
                    element.add(get(i).duplicate(true));
                }

                // Add the trailing segment to the range
                if (trailingSegment != null
                    && trailingSegment.getCharacterCount() > 0) {
                    element.add(trailingSegment);
                }
            }
        }

        return range;
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
        if (index < 0
            || index > nodes.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        if (node.getParent() != null) {
            throw new IllegalArgumentException("node already has a parent.");
        }

        if (node == this) {
            throw new IllegalArgumentException("Cannot add an element to itself.");
        }

        // Set this as the node's parent
        node.setParent(this);

        // Set the node's offset
        if (index > 0) {
            Node previousNode = nodes.get(index - 1);
            node.setOffset(previousNode.getOffset() + previousNode.getCharacterCount());
        }

        // Add the node
        nodes.insert(node, index);

        // Update the offsets of consecutive nodes
        int offsetShift = node.getCharacterCount();
        for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
            Node consecutiveNode = nodes.get(i);
            consecutiveNode.setOffset(consecutiveNode.getOffset() + offsetShift);
        }

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
        if (index < 0
            || index + count > nodes.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        Sequence<Node> removed = nodes.remove(index, count);

        Node range = duplicate(false);
        Element element = (Element)range;

        int offsetShift = 0;
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Node node = removed.get(i);
            node.setParent(null);
            node.setOffset(0);
            element.add(node);
            offsetShift += node.getCharacterCount();
        }

        // Update the offsets of consecutive nodes
        for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
            Node consecutiveNode = nodes.get(i);
            consecutiveNode.setOffset(consecutiveNode.getOffset() - offsetShift);
        }

        if (removed.getLength() > 0) {
            rangeRemoved(index, range);
        }

        return removed;
    }

    public Node get(int index) {
        if (index < 0
            || index > nodes.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        return nodes.get(index);
    }

    public int indexOf(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        return Sequence.Search.binarySearch(nodes, node, OFFSET_COMPARATOR);
    }

    public int getLength() {
        return nodes.getLength();
    }

    /**
     * Determines the index of the child node at a given offset.
     *
     * @param offset
     *
     * @return
     * The index of the child node at the given offset.
     */
    public int getIndexAt(int offset) {
        if (offset < 0
            || offset >= characterCount) {
            throw new IndexOutOfBoundsException();
        }

        nullNode.setOffset(offset);
        return indexOf(nullNode);
    }

    /**
     * Determines the path of the descendant node at a given offset.
     *
     * @param offset
     *
     * @return
     * The path to the descendant node at the given offset.
     */
    public Sequence<Integer> getPathAt(int offset) {
        Sequence<Integer> path;

        int index = getIndexAt(offset);

        if (index >= 0) {
            Node node = get(index);

            if (node instanceof Element) {
                Element element = (Element)node;
                path = element.getPathAt(offset - element.getOffset());
            } else {
                path = new ArrayList<Integer>();
            }
        } else {
            path = new ArrayList<Integer>();
        }

        path.insert(index, 0);

        return path;
    }

    /**
     * Determines the child node at a given offset.
     *
     * @param offset
     *
     * @return
     * The child node at the given offset.
     */
    public Node getNodeAt(int offset) {
        if (offset < 0
            || offset >= characterCount) {
            throw new IndexOutOfBoundsException();
        }

        Node node = null;

        int index = getIndexAt(offset);
        if (index != -1) {
            node = nodes.get(index);
        }

        return node;
    }

    /**
     * Determines the descendant node at a given offset.
     *
     * @param offset
     *
     * @return
     * The descendant node at the given offset.
     */
    public Node getDescendantAt(int offset) {
        Node descendant = getNodeAt(offset);

        if (descendant instanceof Element) {
            Element element = (Element)descendant;
            descendant = element.getDescendantAt(offset - element.getOffset());
        }

        return descendant;
    }

    /**
     * Recursively consolidates all contiguous text nodes.
     */
    public void normalize() {
        int i = getLength() - 1;

        while (i >= 0) {
            Node node = get(i--);

            if (node instanceof TextNode) {
                // Determine the bounds of any contiguous text nodes
                int j = i;
                while (j >= 0
                    && (get(j) instanceof TextNode)) {
                    j--;
                }

                Sequence<Node> removed = remove(j + 1, i - j);

                // Create a new text node containing the consolidated text
                TextNode textNode = new TextNode();
                for (int k = 0, n = removed.getLength(); k < n; k++) {
                    textNode.insertRange(removed.get(k), textNode.getCharacterCount());
                }

                insert(textNode, j + 1);

                i = j;
            } else {
                Element element = (Element)node;
                element.normalize();
            }
        }
    }

    @Override
    protected void rangeInserted(Node range, int offset) {
        characterCount += range.getCharacterCount();
        super.rangeInserted(range, offset);
    }

    @Override
    protected void rangeRemoved(int offset, Node range) {
        characterCount -= range.getCharacterCount();
        super.rangeRemoved(offset, range);
    }

    public Iterator<Node> iterator() {
        return new ImmutableIterator<Node>(nodes.iterator());
    }

    /**
     * Returns the element listener list.
     */
    public ListenerList<ElementListener> getElementListeners() {
        return elementListeners;
    }
}
