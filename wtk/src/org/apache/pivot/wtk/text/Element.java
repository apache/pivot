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
import java.io.IOException;
import java.util.Iterator;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.FontUtilities;
import org.apache.pivot.wtk.GraphicsUtilities;

/**
 * Abstract base class for elements. <p> TODO Add style properties. <p> TODO Add
 * style class property.
 */
public abstract class Element extends Node implements Sequence<Node>, Iterable<Node> {
    private int characterCount = 0;
    private ArrayList<Node> nodes = new ArrayList<>();
    private java.awt.Font font;
    private Color foregroundColor;
    private Color backgroundColor;
    private boolean underline;
    private boolean strikethrough;

    private ElementListener.Listeners elementListeners = new ElementListener.Listeners();

    public Element() {
    }

    public Element(final Element element, final boolean recursive) {
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
    public void insertRange(final Node range, final int offset) {
        if (!(range instanceof Element)) {
            throw new IllegalArgumentException("Range node ("
                + range.getClass().getSimpleName() + ") is not an Element.");
        }

        Utils.checkIndexBounds(offset, 0, characterCount);

        Element element = (Element) range;
        int n = element.getLength();

        if (n > 0) {
            // Clear the range content, since the child nodes will become children
            // of this element
            Sequence<Node> localNodes = element.remove(0, n);

            if (offset == characterCount) {
                // Append the range contents to the end of this element
                for (int i = 0; i < n; i++) {
                    add(localNodes.get(i));
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
                    insert(localNodes.get(i), index + i);
                }

                // Insert the remainder of the node
                if (trailingSegment != null) {
                    insert(trailingSegment, index + n);
                }
            }
        }
    }

    private Paragraph getParagraphParent(final Node node) {
        Element parent = node.getParent();
        while ((parent != null) && !(parent instanceof Paragraph)) {
            parent = parent.getParent();
        }
        return (Paragraph) parent;
    }

    @Override
    public Node removeRange(final int offset, final int charCount) {
        Utils.checkIndexBounds(offset, charCount, 0, characterCount);

        // Create a copy of this element
        Node range = duplicate(false);

        if (charCount > 0) {
            Element element = (Element) range;

            int endOffset = offset + charCount - 1;
            int start = getNodeAt(offset);
            int end = (endOffset == offset) ? start : getNodeAt(endOffset);

            if (start == end) {
                // The range is entirely contained by one child node
                Node node = get(start);
                int nodeOffset = node.getOffset();
                int nodeCharacterCount = node.getCharacterCount();

                Node segment;
                if (offset == nodeOffset && charCount == nodeCharacterCount) {
                    // Special case:  we need to leave at least one text node
                    // underneath a paragraph to allow for composed text on an
                    // empty line, so check that condition and don't remove the
                    // entire node.
                    Paragraph paragraph;
                    if (node instanceof TextNode && (paragraph = getParagraphParent(node)) != null
                     && paragraph.getLength() == 1) {
                        segment = node.removeRange(0, charCount);
                    } else {
                        // Remove the entire node
                        segment = node;
                        remove(start, 1);
                    }
                } else {
                    // Remove a segment of the node
                    segment = node.removeRange(offset - node.getOffset(), charCount);
                }

                element.add(segment);
            } else {
                // The range spans multiple child nodes
                Node startNode = get(start);
                int leadingSegmentOffset = offset - startNode.getOffset();

                Node endNode = get(end);
                int trailingSegmentCharacterCount = (offset + charCount)
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
                if (leadingSegment != null && leadingSegment.getCharacterCount() > 0) {
                    element.add(leadingSegment);
                }

                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    element.add(removed.get(i));
                }

                if (trailingSegment != null && trailingSegment.getCharacterCount() > 0) {
                    element.add(trailingSegment);
                }
            }
        }

        return range;
    }

    @Override
    public Element getRange(final int offset, final int charCount) {
        Utils.checkIndexBounds(offset, charCount, 0, characterCount);

        // Create a copy of this element
        Element range = duplicate(false);

        if (charCount > 0) {

            int endOffset = offset + charCount - 1;
            int start = getNodeAt(offset);
            int end = (endOffset == offset) ? start : getNodeAt(endOffset);

            if (start == end) {
                // The range is entirely contained by one child node
                Node node = get(start);
                Node segment = node.getRange(offset - node.getOffset(), charCount);
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

                    int trailingSegmentCharacterCount = (offset + charCount)
                        - endNode.getOffset();
                    trailingSegment = endNode.getRange(0, trailingSegmentCharacterCount);
                }

                // Add the leading segment to the range
                if (leadingSegment != null && leadingSegment.getCharacterCount() > 0) {
                    range.add(leadingSegment);
                    start++;
                }

                // Duplicate the intervening nodes
                for (int i = start; i < end; i++) {
                    range.add(get(i).duplicate(true));
                }

                // Add the trailing segment to the range
                if (trailingSegment != null && trailingSegment.getCharacterCount() > 0) {
                    range.add(trailingSegment);
                }
            }
        }

        return range;
    }

    @Override
    public abstract Element duplicate(boolean recursive);

    @Override
    public char getCharacterAt(final int offset) {
        Node node = nodes.get(getNodeAt(offset));
        return node.getCharacterAt(offset - node.getOffset());
    }

    @Override
    public int getCharacterCount() {
        return characterCount;
    }

    private void addCharacters(final Appendable buf, final Element element) {
        try {
            for (Node child : element.nodes) {
                if (child instanceof Element) {
                    addCharacters(buf, (Element) child);
                } else {
                    buf.append(child.getCharacters());
                }
            }
            if (element instanceof Paragraph) {
                buf.append('\n');
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public String getText() {
        return getCharacters().toString();
    }

    public CharSequence getCharacters() {
        StringBuilder buf = new StringBuilder(characterCount);
        addCharacters(buf, this);
        return buf;
    }

    @Override
    public int add(final Node node) {
        int index = nodes.getLength();
        insert(node, index);

        return index;
    }

    @Override
    public void insert(final Node node, final int index) {
        Utils.checkNull(node, "node");
        Utils.checkIndexBounds(index, 0, nodes.getLength());

        if (node.getParent() != null) {
            throw new IllegalArgumentException(node.getClass().getSimpleName() + " node already has a parent.");
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
    @UnsupportedOperation
    public Node update(final int index, final Node node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int remove(final Node node) {
        int index = indexOf(node);
        if (index != -1) {
            remove(index, 1);
        }

        return index;
    }

    @Override
    public Sequence<Node> remove(final int index, final int count) {
        Utils.checkIndexBounds(index, count, 0, nodes.getLength());

        // Remove the nodes
        Sequence<Node> removed = nodes.remove(index, count);
        int len = removed.getLength();
        StringBuilder removedChars = new StringBuilder();

        if (len > 0) {
            int removedCharacterCount = 0;
            for (int i = 0; i < len; i++) {
                Node node = removed.get(i);
                node.setParent(null);
                removedCharacterCount += node.getCharacterCount();
                if (node instanceof Element) {
                    removedChars.append(((Element) node).getText());
                } else if (node instanceof TextNode) {
                    removedChars.append(((TextNode) node).getText());
                }
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
            super.rangeRemoved(this, offset, removedCharacterCount, removedChars);
            super.nodesRemoved(this, removed, offset);

            // Fire event
            elementListeners.nodesRemoved(this, index, removed);
        }

        return removed;
    }

    @Override
    public Node get(final int index) {
        Utils.checkZeroBasedIndex(index, nodes.getLength());

        return nodes.get(index);
    }

    @Override
    public int indexOf(final Node node) {
        Utils.checkNull(node, "node");

        return nodes.indexOf(node);
    }

    @Override
    public int getLength() {
        return nodes.getLength();
    }

    /**
     * Determines the index of the child node at a given offset.
     *
     * @param offset The text offset to search for.
     * @return The index of the child node at the given offset.
     */
    public int getNodeAt(final int offset) {
        Utils.checkZeroBasedIndex(offset, characterCount);

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
     * @param offset The text offset to search for.
     * @return The path to the descendant node at the given offset.
     */
    public Sequence<Integer> getPathAt(final int offset) {
        Sequence<Integer> path;

        int index = getNodeAt(offset);
        Node node = get(index);

        if (node instanceof Element) {
            Element element = (Element) node;
            path = element.getPathAt(offset - element.getOffset());
        } else {
            path = new ArrayList<>();
        }

        path.insert(index, 0);

        return path;
    }

    /**
     * Determines the descendant node at a given offset.
     *
     * @param offset The text offset to search for.
     * @return The descendant node at the given offset.
     */
    public Node getDescendantAt(final int offset) {
        Node descendant = nodes.get(getNodeAt(offset));

        if (descendant instanceof Element) {
            Element element = (Element) descendant;
            descendant = element.getDescendantAt(offset - element.getOffset());
        }

        return descendant;
    }

    @Override
    protected void rangeInserted(final int offset, final int charCount) {
        this.characterCount += charCount;

        // Update the offsets of consecutive nodes
        int index = getNodeAt(offset);

        for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
            Node node = nodes.get(i);
            node.setOffset(node.getOffset() + charCount);
        }

        super.rangeInserted(offset, charCount);
    }

    @Override
    protected void rangeRemoved(final Node originalNode, final int offset, final int charCount,
        final CharSequence removedChars) {
        // Update the offsets of consecutive nodes, if any
        if (offset < this.characterCount) {
            int index = getNodeAt(offset);

            for (int i = index + 1, n = nodes.getLength(); i < n; i++) {
                Node node = nodes.get(i);
                node.setOffset(node.getOffset() - charCount);
            }
        }
        // Adjust our count last, so we make sure to get all our children updated
        this.characterCount -= charCount;

        super.rangeRemoved(originalNode, offset, charCount, removedChars);
    }

    @Override
    public Iterator<Node> iterator() {
        return new ImmutableIterator<>(nodes.iterator());
    }

    public final Font getFont() {
        return font;
    }

    public final void setFont(final Font font) {
        Utils.checkNull(font, "font");

        Font previousFont = this.font;
        if (previousFont != font) {
            this.font = font;
            elementListeners.fontChanged(this, previousFont);
        }
    }

    public final void setFont(final String font) {
        setFont(FontUtilities.decodeFont(font));
    }

    /**
     * @return The current foreground color, or <tt>null</tt> if no color is
     * foreground.
     */
    public final Color getForegroundColor() {
        return foregroundColor;
    }

    /**
     * Sets the current foreground color.
     *
     * @param foregroundColor The foreground color, or <tt>null</tt> to specify
     * no selection.
     */
    public final void setForegroundColor(final Color foregroundColor) {
        Color previousForegroundColor = this.foregroundColor;

        if (foregroundColor != previousForegroundColor) {
            this.foregroundColor = foregroundColor;
            elementListeners.foregroundColorChanged(this, previousForegroundColor);
        }
    }

    /**
     * Sets the current foreground color.
     *
     * @param foregroundColor The foreground color.
     */
    public final void setForegroundColor(final String foregroundColor) {
        setForegroundColor(GraphicsUtilities.decodeColor(foregroundColor, "foregroundColor"));
    }

    /**
     * @return The current background color, or <tt>null</tt> if no color is
     * background.
     */
    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the current background color.
     *
     * @param backgroundColor The background color, or <tt>null</tt> to specify
     * no selection.
     */
    public final void setBackgroundColor(final Color backgroundColor) {
        Color previousBackgroundColor = this.backgroundColor;

        if (backgroundColor != previousBackgroundColor) {
            this.backgroundColor = backgroundColor;
            elementListeners.backgroundColorChanged(this, previousBackgroundColor);
        }
    }

    /**
     * Sets the current background color.
     *
     * @param backgroundColor The background color.
     */
    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final boolean isUnderline() {
        return underline;
    }

    public final void setUnderline(final boolean underline) {
        boolean previousUnderline = this.underline;
        if (previousUnderline != underline) {
            this.underline = underline;
            elementListeners.underlineChanged(this);
        }
    }

    public final boolean isStrikethrough() {
        return strikethrough;
    }

    public final void setStrikethrough(final boolean strikethrough) {
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
