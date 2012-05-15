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

/**
 * Abstract base class for document nodes.
*/
public abstract class Node {
    private static class NodeListenerList extends ListenerList<NodeListener> implements
        NodeListener {
        @Override
        public void parentChanged(Node node, Element previousParent) {
            for (NodeListener listener : this) {
                listener.parentChanged(node, previousParent);
            }
        }

        @Override
        public void offsetChanged(Node node, int previousOffset) {
            for (NodeListener listener : this) {
                listener.offsetChanged(node, previousOffset);
            }
        }

        @Override
        public void nodeInserted(Node node, int offset) {
            for (NodeListener listener : this) {
                listener.nodeInserted(node, offset);
            }
        }

        @Override
        public void nodesRemoved(Node node, Sequence<Node> removed, int offset) {
            for (NodeListener listener : this) {
                listener.nodesRemoved(node, removed, offset);
            }
        }

        @Override
        public void rangeInserted(Node node, int offset, int span) {
            for (NodeListener listener : this) {
                listener.rangeInserted(node, offset, span);
            }
        }

        @Override
        public void rangeRemoved(Node node, int offset, int characterCount) {
            for (NodeListener listener : this) {
                listener.rangeRemoved(node, offset, characterCount);
            }
        }
    }

    private Element parent = null;
    private int offset = 0;

    private NodeListenerList nodeListeners = new NodeListenerList();

    /**
     * Returns the parent element of this node.
     *
     * @return
     * The node's parent, or <tt>null</tt> if the node does not have a parent.
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
     * Returns the node's offset within its parent.
     *
     * @return
     * The integer offset of the node's first character within its parent
     * element.
     */
    public int getOffset() {
        return offset;
    }

    protected void setOffset(int offset) {
        int previousOffset = this.offset;

        if (previousOffset != offset) {
            this.offset = offset;
            nodeListeners.offsetChanged(this, previousOffset);
        }
    }

    /**
     * Returns the node's offset within the document.
     */
    public int getDocumentOffset() {
        return (parent == null) ? 0 : parent.getDocumentOffset() + offset;
    }

    /**
     * Inserts a range into the node. Note that the contents of the range,
     * rather than the range itself, is added to the node.
     *
     * @param range
     * @param offsetArgument
     */
    public abstract void insertRange(Node range, int offsetArgument);

    /**
     * Removes a range from the node.
     *
     * @param offsetArgument
     * @param characterCount
     *
     * @return
     * The removed range. This will be a copy of the node structure relative
     * to this node.
     */
    public abstract Node removeRange(int offsetArgument, int characterCount);

    /**
     * Replaces an existing range with a new range.
     *
     * @param offsetArgument
     * @param characterCount
     * @param range
     *
     * @return
     * The removed range. This will be a copy of the node structure relative
     * to this node.
     */
    public Node replaceRange(int offsetArgument, int characterCount, Node range) {
        Node removed = removeRange(offsetArgument, characterCount);
        insertRange(range, offsetArgument);

        return removed;
    }

    /**
     * Returns a range from the node.
     *
     * @param offsetArgument
     * @param characterCount
     *
     * @return
     * A node containing a copy of the node structure spanning the given range,
     * relative to this node.
     */
    public abstract Node getRange(int offsetArgument, int characterCount);

    /**
     * Returns the character at the given offset.
     *
     * @param offsetArgument
     */
    public abstract char getCharacterAt(int offsetArgument);

    /**
     * Returns the number of characters in this node.
     */
    public abstract int getCharacterCount();

    /**
     * Creates a copy of this node.
     *
     * @param recursive
     */
    public abstract Node duplicate(boolean recursive);

    /**
     * Called to notify a node that a range has been inserted.
     *
     * @param offsetArgument
     * @param characterCount
     */
    protected void rangeInserted(int offsetArgument, int characterCount) {
        if (parent != null) {
            parent.rangeInserted(offsetArgument + this.offset, characterCount);
        }

        nodeListeners.rangeInserted(this, offsetArgument, characterCount);
    }

    /**
     * Called to notify a node that a range has been removed.
     *
     * @param offsetArgument
     * @param characterCount
     */
    protected void rangeRemoved(int offsetArgument, int characterCount) {
        if (parent != null) {
            parent.rangeRemoved(offsetArgument + this.offset, characterCount);
        }

        nodeListeners.rangeRemoved(this, offsetArgument, characterCount);
    }

    /**
     * Called to notify a node that some child nodes has been removed.
     *
     * @param removed
     * @param offsetArgument
     */
    protected void nodesRemoved(Sequence<Node> removed, int offsetArgument) {
        if (parent != null) {
            parent.nodesRemoved(removed, offsetArgument + this.offset);
        }

        nodeListeners.nodesRemoved(this, removed, offsetArgument);
    }

    /**
     * Called to notify a node that a child node has been inserted.
     *
     * @param offsetArgument
     */
    protected void nodeInserted(int offsetArgument) {
        if (parent != null) {
            parent.nodeInserted(offsetArgument + this.offset);
        }

        nodeListeners.nodeInserted(this, offsetArgument);
    }

    /**
     * Returns the node listener list.
     */
    public ListenerList<NodeListener> getNodeListeners() {
        return nodeListeners;
    }
}
