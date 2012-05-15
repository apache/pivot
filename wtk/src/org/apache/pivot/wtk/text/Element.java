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
package org.apache.pivot.wtk.text;

import java.awt.Color;
import java.awt.Font;
import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Theme;

/**
 * Abstract base class for elements.
 * <p>
 * TODO Add style properties.
 * <p>
 * TODO Add style class property.
 */
public abstract class Element extends Node
    implements Sequence<Node>, Iterable<Node> {
    private static class ElementListenerList extends ListenerList<ElementListener>
        implements ElementListener {
        @Override
        public void nodeInserted(Element element, int index) {
            for (ElementListener listener : this) {
                listener.nodeInserted(element, index);
            }
        }

        @Override
        public void nodesRemoved(Element element, int index,
            Sequence<Node> nodes) {
            for (ElementListener listener : this) {
                listener.nodesRemoved(element, index, nodes);
            }
        }

        @Override
        public void fontChanged(Element element, Font previousFont) {
            for (ElementListener listener : this) {
                listener.fontChanged(element, previousFont);
            }
        }
        @Override
        public void backgroundColorChanged(Element element, Color previousBackgroundColor) {
            for (ElementListener listener : this) {
                listener.backgroundColorChanged(element, previousBackgroundColor);
            }
        }
        @Override
        public void foregroundColorChanged(Element element, Color previousForegroundColor) {
            for (ElementListener listener : this) {
                listener.foregroundColorChanged(element, previousForegroundColor);
            }
        }
        @Override
        public void underlineChanged(Element element) {
            for (ElementListener listener : this) {
                listener.underlineChanged(element);
            }
        }
        @Override
        public void strikethroughChanged(Element element) {
            for (ElementListener listener : this) {
                listener.strikethroughChanged(element);
            }
        }
    }

    private int characterCount = 0;
    private ArrayList<Node> nodes = new ArrayList<Node>();
    private java.awt.Font font;
    private Color foregroundColor;
    private Color backgroundColor;
    private boolean underline;
    private boolean strikethrough;

    private ElementListenerList elementListeners = new ElementListenerList();

    public Element() {
    }

    public Element(Element element, boolean recursive) {
        this.font = element.getFont();
        this.foregroundColor = element.getForegroundColor();
        this.backgroundColor = element.getBackgroundColor();
        this.underline = element.isUnderline();
        this.strikethrough = element.isStrikethrough();
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

        Element element = (Element)range;
        int n = element.getLength();

        if (n > 0) {
            // Clear the range content, since the child nodes will become children
            // of this element
            Sequence<Node> nodesLocal = element.remove(0, n);

            if (offset == characterCount) {
                // Append the range contents to the end of this element
                for (int i = 0; i < n; i++) {
                    add(nodesLocal.get(i));
                }
            } else {
                // Merge the range contents into this element
                int index = getNodeAt(offset);
                Node leadingSegment = get(index);

                Node trailingSegment;
                int spliceOffset = offset - leadingSegment.getOffset();
                if (spliceOffset > 0) {
                    trailingSegment = leadingSegment.removeRange(spliceOffset,
                        leadingSegment.getCharacterCount() - spliceOffset);
                    index++;
                } else {
                    trailingSegment = null;
                }

                for (int i = 0; i < n; i++) {
                    insert(nodesLocal.get(i), index + i);
                }

                // Insert the remainder of the node
                if (trailingSegment != null) {
                    insert(trailingSegment, index + n);
                }
            }
        }
    }

    @Override
    public Node removeRange(int offset, int characterCountArgument) {
        if (characterCountArgument < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        if (offset < 0
            || offset + characterCountArgument > this.characterCount) {
            throw new IndexOutOfBoundsException();
        }

        // Create a copy of this element
        Node range = duplicate(false);

        if (characterCountArgument > 0) {
            Element element = (Element)range;

            int start = getNodeAt(offset);
            int end = getNodeAt(offset + characterCountArgument - 1);

            if (start == end) {
                // The range is entirely contained by one child node
                Node node = get(start);
                int nodeOffset = node.getOffset();
                int nodeCharacterCount = node.getCharacterCount();

                Node segment;
                if (offset == nodeOffset
                    && characterCountArgument == nodeCharacterCount) {
                    // Remove the entire node
                    segment = node;
                    remove(start, 1);
                } else {
                    // Remove a segment of the node
                    segment = node.removeRange(offset - node.getOffset(), characterCountArgument);
                }

                element.add(segment);
            } else {
                // The range spans multiple child nodes
                Node startNode = get(start);
                int leadingSegmentOffset = offset - startNode.getOffset();

                Node endNode = get(end);
                int trailingSegmentCharacterCount = (offset + characterCountArgument)
                    - endNode.getOffset();

                // Extract the leading segment
                Node leadingSegment = null;
                if (leadingSegmentOffset > 0) {
                    leadingSegment = startNode.removeRange(leadingSegmentOffset,
                        startNode.getCharacterCount() - leadingSegmentOffset);
                    start++;
                }

                // Extract the trailing segment
                Node trailingSegment = null;
                if (trailingSegmentCharacterCount < endNode.getCharacterCount()) {
                    trailingSegment = endNode.removeRange(0, trailingSegmentCharacterCount);
                    end--;
                }

                // Remove the intervening nodes
                int count = (end - start) + 1;
                Sequence<Node> removed = remove(start, count);

                // Add the removed segments and nodes to the range
                if (leadingSegment != null
                    && leadingSegment.getCharacterCount() > 0) {
                    element.add(leadingSegment);
                }

                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    element.add(removed.get(i));
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
    public Element getRange(int offset, int characterCountArgument) {
        if (characterCountArgument < 0) {
            throw new IllegalArgumentException("characterCount is negative.");
        }

        if (offset < 0) {
            throw new IndexOutOfBoundsException("offset < 0, offset=" + offset);
        }
        if (offset + characterCountArgument > this.characterCount) {
            throw new IndexOutOfBoundsException("offset+characterCount>this.characterCount offset=" + offset
                + " characterCount=" + characterCountArgument + " this.characterCount=" + this.characterCount);
        }

        // Create a copy of this element
        Element range = duplicate(false);

        if (characterCountArgument > 0) {

            int start = getNodeAt(offset);
            int end = getNodeAt(offset + characterCountArgument - 1);

            if (start == end) {
                // The range is entirely contained by one child node
                Node node = get(start);
                Node segment = node.getRange(offset - node.getOffset(), characterCountArgument);
                range.add(segment);
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

                    int trailingSegmentCharacterCount = (offset + characterCountArgument)
                        - endNode.getOffset();
                    trailingSegment = endNode.getRange(0, trailingSegmentCharacterCount);
                }

                // Add the leading segment to the range
                if (leadingSegment != null
                    && leadingSegment.getCharacterCount() > 0) {
                    range.add(leadingSegment);
                    start++;
                }

                // Duplicate the intervening nodes
                for (int i = start; i < end; i++) {
                    range.add(get(i).duplicate(true));
                }

                // Add the trailing segment to the range
                if (trailingSegment != null
                    && trailingSegment.getCharacterCount() > 0) {
                    range.add(trailingSegment);
                }
            }
        }

        return range;
    }

    @Override
    public abstract Element duplicate(boolean recursive);

    @Override
    public char getCharacterAt(int offset) {
        Node node = nodes.get(getNodeAt(offset));
        return node.getCharacterAt(offset - node.getOffset());
    }

    @Override
    public int getCharacterCount() {
        return characterCount;
    }

    @Override
    public int add(Node node) {
        int index = nodes.getLength();
        insert(node, index);

        return index;
    }

    @Override
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

        // Add the node
        nodes.insert(node, index);

        // Update the character count and node offsets
        int nodeCharacterCount = node.getCharacterCount();
        characterCount += nodeCharacterCount;

        if (index == 0) {
            node.setOffset(0);
        } else {
            Node previousNode = nodes.get(index - 1);
            node.setOffset(previousNode.getOffset() + previousNode.getCharacterCount());
        }

        for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
            Node nextNode = nodes.get(i);
            nextNode.setOffset(nextNode.getOffset() + nodeCharacterCount);
        }

        // Notify parent
        super.rangeInserted(node.getOffset(), nodeCharacterCount);
        super.nodeInserted(node.getOffset());

        // Fire event
        elementListeners.nodeInserted(this, index);
    }

    @Override
    public Node update(int index, Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(Node node) {
        int index = indexOf(node);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public Sequence<Node> remove(int index, int count) {
        if (index < 0
            || index + count > nodes.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        // Remove the nodes
        Sequence<Node> removed = nodes.remove(index, count);
        count = removed.getLength();

        if (count > 0) {
            int removedCharacterCount = 0;
            for (int i = 0; i < count; i++) {
                Node node = removed.get(i);
                node.setParent(null);
                removedCharacterCount += node.getCharacterCount();
            }

            // Update the character count
            characterCount -= removedCharacterCount;

            // Update the offsets of consecutive nodes
            int n = nodes.getLength();
            for (int i = index; i < n; i++) {
                Node nextNode = nodes.get(i);
                nextNode.setOffset(nextNode.getOffset() - removedCharacterCount);
            }

            // Determine the affected offset within this element
            int offset;
            if (index < n) {
                Node node = get(index);
                offset = node.getOffset();
            } else {
                offset = characterCount;
            }

            // Notify parent
            super.rangeRemoved(offset, removedCharacterCount);
            super.nodesRemoved(removed, offset);

            // Fire event
            elementListeners.nodesRemoved(this, index, removed);
        }

        return removed;
    }

    @Override
    public Node get(int index) {
        if (index < 0
            || index > nodes.getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        return nodes.get(index);
    }

    @Override
    public int indexOf(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        return nodes.indexOf(node);
    }

    @Override
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
    public int getNodeAt(int offset) {
        if (offset < 0
            || offset >= characterCount) {
            throw new IndexOutOfBoundsException("offset " + offset + " out of range [0," + characterCount + "]");
        }

        int i = nodes.getLength() - 1;
        Node node = nodes.get(i);

        while (node.getOffset() > offset) {
            node = nodes.get(--i);
        }

        return i;
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

        int index = getNodeAt(offset);
        Node node = get(index);

        if (node instanceof Element) {
            Element element = (Element)node;
            path = element.getPathAt(offset - element.getOffset());
        } else {
            path = new ArrayList<Integer>();
        }

        path.insert(index, 0);

        return path;
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
        Node descendant = nodes.get(getNodeAt(offset));

        if (descendant instanceof Element) {
            Element element = (Element)descendant;
            descendant = element.getDescendantAt(offset - element.getOffset());
        }

        return descendant;
    }

    @Override
    protected void rangeInserted(int offset, int characterCountArgument) {
        this.characterCount += characterCountArgument;

        // Update the offsets of consecutive nodes
        int index = getNodeAt(offset);

        for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
            Node node = nodes.get(i);
            node.setOffset(node.getOffset() + characterCountArgument);
        }

        super.rangeInserted(offset, characterCountArgument);
    }

    @Override
    protected void rangeRemoved(int offset, int characterCountArgument) {
        this.characterCount -= characterCountArgument;

        // Update the offsets of consecutive nodes, if any
        if (offset < this.characterCount) {
            int index = getNodeAt(offset);

            for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
                Node node = nodes.get(i);
                node.setOffset(node.getOffset() - characterCountArgument);
            }
        }

        super.rangeRemoved(offset, characterCountArgument);
    }

    @Override
    public Iterator<Node> iterator() {
        return new ImmutableIterator<Node>(nodes.iterator());
    }

    public void dumpOffsets() {
        for (int i = 0, n = getLength(); i < n; i++) {
            Node node = get(i);
            System.out.println("[" + i + "] " + node.getOffset()
                + ":" + node.getCharacterCount());
        }

        System.out.println();
    }

    public java.awt.Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        Font previousFont = this.font;
        if (previousFont != font) {
            this.font = font;
            elementListeners.fontChanged(this, previousFont);
        }
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        if (font.startsWith("{")) {
            try {
                setFont(Theme.deriveFont(JSONSerializer.parseMap(font)));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            setFont(Font.decode(font));
        }
    }

    /**
     * Gets the currently foreground color, or <tt>null</tt> if no color is
     * foreground.
     */
    public Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Sets the currently foreground color.
     *
     * @param foregroundColor
     * The foreground color, or <tt>null</tt> to specify no selection
     */
    public void setForegroundColor(Color foregroundColor) {
        Color previousForegroundColor = this.foregroundColor;

        if (foregroundColor != previousForegroundColor) {
            this.foregroundColor = foregroundColor;
            elementListeners.foregroundColorChanged(this, previousForegroundColor);
        }
    }

    /**
     * Sets the currently foreground color.
     *
     * @param foregroundColor
     * The foreground color
     */
    public void setForegroundColor(String foregroundColor) {
        if (foregroundColor == null) {
            throw new IllegalArgumentException("foregroundColor is null.");
        }

        setForegroundColor(GraphicsUtilities.decodeColor(foregroundColor));
    }

    /**
     * Gets the currently background color, or <tt>null</tt> if no color is
     * background.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the currently background color.
     *
     * @param backgroundColor
     * The background color, or <tt>null</tt> to specify no selection
     */
    public void setBackgroundColor(Color backgroundColor) {
        Color previousBackgroundColor = this.backgroundColor;

        if (backgroundColor != previousBackgroundColor) {
            this.backgroundColor = backgroundColor;
            elementListeners.backgroundColorChanged(this, previousBackgroundColor);
        }
    }

    /**
     * Sets the currently background color.
     *
     * @param backgroundColor
     * The background color
     */
    public void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        boolean previousUnderline = this.underline;
        if (previousUnderline != underline) {
            this.underline = underline;
            elementListeners.underlineChanged(this);
        }
    }

    public boolean isStrikethrough() {
        return strikethrough;
    }

    public void setStrikethrough(boolean strikethrough) {
        boolean previousStrikethrough = this.strikethrough;
        if (previousStrikethrough != strikethrough) {
            this.strikethrough = strikethrough;
            elementListeners.strikethroughChanged(this);
        }
    }

    public ListenerList<ElementListener> getElementListeners() {
        return elementListeners;
    }

}
