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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Span;

/**
 * Abstract base class for document nodes.
 */
public abstract class Node {
    private Element parent = null;
    private int offset = 0;
    private Object userData = null;

    private NodeListener.Listeners nodeListeners = new NodeListener.Listeners();

    /**
     * Returns the parent element of this node.
     *
     * @return The node's parent, or <tt>null</tt> if the node does not have a
     * parent.
     */
    public Element getParent() {
        return parent;
    }

    protected void setParent(Element parent) {
        Element previousParent = this.parent;

        if (previousParent != parent) {
            this.parent = parent;
            nodeListeners.parentChanged(this, previousParent);
        }
    }

    /**
     * Returns the node's offset relative to its parent.
     *
     * @return The integer offset of the node's first character within its
     * parent element.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Set the offset of this node relative to its parent.
     *
     * @param offset The new offset for this node.
     */
    protected void setOffset(int offset) {
        int previousOffset = this.offset;

        if (previousOffset != offset) {
            this.offset = offset;
            nodeListeners.offsetChanged(this, previousOffset);
        }
    }

    /**
     * @return The node's offset within the document, which will be the
     * offset of our parent (if any) added to our own offset.
     */
    public int getDocumentOffset() {
        return ((parent == null) ? 0 : parent.getDocumentOffset()) + offset;
    }

    /**
     * @return A {@link Span} that describes the content range of this node
     * relative to the whole document.
     */
    public Span getDocumentSpan() {
        int docOffset = getDocumentOffset();
        int nodeLength = getCharacterCount();
        // The "end" of a Span is inclusive, so subtract one here
        return new Span(docOffset, docOffset + nodeLength - 1);
    }

    /**
     * Inserts a range into the node. Note that the contents of the range,
     * rather than the range itself, is added to the node.
     *
     * @param range The node containing the text to insert.
     * @param offsetArgument Offset relative to this node.
     */
    public abstract void insertRange(Node range, int offsetArgument);

    /**
     * Removes a range from the node.
     *
     * @param offsetArgument Offset relative to this node.
     * @param characterCount Count of characters to remove.
     * @return The removed range. This will be a copy of the node structure
     * relative to this node.
     */
    public abstract Node removeRange(int offsetArgument, int characterCount);

    /**
     * Replaces an existing range with a new range.
     *
     * @param offsetArgument Offset relative to this node.
     * @param characterCount Count of characters to replace.
     * @param range The new range to insert.
     * @return The removed range. This will be a copy of the node structure
     * relative to this node.
     */
    public Node replaceRange(int offsetArgument, int characterCount, Node range) {
        Node removed = removeRange(offsetArgument, characterCount);
        insertRange(range, offsetArgument);

        return removed;
    }

    /**
     * Returns a range from the node.
     *
     * @param offsetArgument Offset relative to this node.
     * @param characterCount Count of characters to get.
     * @return A node containing a copy of the node structure spanning the given
     * range, relative to this node.
     */
    public abstract Node getRange(int offsetArgument, int characterCount);

    /**
     * @return The character at the given offset.
     *
     * @param offsetArgument Offset relative to this node.
     */
    public abstract char getCharacterAt(int offsetArgument);

    /**
     * @return The number of characters in this node.
     */
    public abstract int getCharacterCount();

    /**
     * @return The character sequence in this node.
     */
    public abstract CharSequence getCharacters();

    /**
     * Creates a copy of this node.
     *
     * @param recursive Whether to duplicate the children also.
     * @return A copy of the current node.
     */
    public abstract Node duplicate(boolean recursive);

    /**
     * Called to notify parent nodes and other listeners for the node
     * that a range has been inserted.  All parents are notified first.
     * <p> Note: The offset used to notify parents is the given offset
     * added to the offset of this node (that is, it will be parent-relative).
     * Therefore the topmost node will be given the offset into the whole document.
     * Listeners for this node will just be given the offset relative to this node.
     *
     * @param offsetArgument Offset relative to this node.
     * @param characterCount Count of characters inserted.
     */
    protected void rangeInserted(int offsetArgument, int characterCount) {
        if (parent != null) {
            parent.rangeInserted(offsetArgument + this.offset, characterCount);
        }

        nodeListeners.rangeInserted(this, offsetArgument, characterCount);
    }

    /**
     * Called to notify parent nodes and other listeners for the node
     * that a range has been removed.  All parents are notified first.
     * <p> Note: The offset used to notify parents is the given offset
     * added to the offset of this node (that is, it will be parent-relative).
     * Therefore the topmost node will be given the offset into the whole document.
     * Listeners for this node will just be given the offset relative to this node.
     *
     * @param node The <em>original</em> node (that is, NOT the parent) where the
     * range was removed.
     * @param offsetArgument Offset relative to the current node.
     * @param characterCount Count of characters removed.
     * @param removedChars The optional actual characters removed (only in the case
     * of direct removal from a text node).
     */
    protected void rangeRemoved(Node node, int offsetArgument, int characterCount,
        CharSequence removedChars) {
        if (parent != null) {
            parent.rangeRemoved(node, offsetArgument + this.offset, characterCount, removedChars);
        }

        nodeListeners.rangeRemoved(node, offsetArgument, characterCount, removedChars);
    }

    /**
     * Called to notify parent nodes and other listeners for the node
     * that child nodes have been removed.  All parents are notified first.
     * <p> Note: The offset used to notify parents is the given offset
     * added to the offset of this node (that is, it will be parent-relative).
     * Therefore the topmost node will be given the offset into the whole document.
     * Listeners for this node will just be given the offset relative to this node.
     *
     * @param node The <em>original</em> node (that is, NOT the parent) where the
     * nodes were removed.
     * @param removed The actual sequence of nodes removed from that node.
     * @param offsetArgument Offset relative to this node.
     */
    protected void nodesRemoved(Node node, Sequence<Node> removed, int offsetArgument) {
        if (parent != null) {
            parent.nodesRemoved(node, removed, offsetArgument + this.offset);
        }

        nodeListeners.nodesRemoved(node, removed, offsetArgument);
    }

    /**
     * Called to notify parent nodes and other listeners for the node
     * that a child node has been inserted.  All parents are notified first.
     * <p> Note: The offset used to notify parents is the given offset
     * added to the offset of this node (that is, it will be parent-relative).
     * Therefore the topmost node will be given the offset into the whole document.
     * Listeners for this node will just be given the offset relative to this node.
     *
     * @param offsetArgument Offset relative to this node.
     */
    protected void nodeInserted(int offsetArgument) {
        if (parent != null) {
            parent.nodeInserted(offsetArgument + this.offset);
        }

        nodeListeners.nodeInserted(this, offsetArgument);
    }

    /**
     * @return The node listener list.
     */
    public ListenerList<NodeListener> getNodeListeners() {
        return nodeListeners;
    }

    /**
     * @return The user data associated with this node.
     */
    public Object getUserData() {
        return this.userData;
    }

    /**
     * Set the user data associated with this node.  This can be any
     * piece of data that has meaning to the application, and is meant
     * to link any underlying data structure used to build the document
     * with the document itself.
     *
     * @param userData Any piece of data that has meaning to the user
     * application (can be {@code null}).
     */
    public void setUserData(Object userData) {
        this.userData = userData;
    }

}
