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

import pivot.util.ListenerList;

/**
 * Abstract base class for document nodes.
 *
 * @author gbrown
 */
public abstract class Node {
    private static class NodeListenerList extends ListenerList<NodeListener>
        implements NodeListener {
        public void parentChanged(Node node, Element previousParent) {
            for (NodeListener listener : this) {
                listener.parentChanged(node, previousParent);
            }
        }

        public void offsetChanged(Node node, int previousOffset) {
            for (NodeListener listener : this) {
                listener.offsetChanged(node, previousOffset);
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
     * Inserts a range into the node. Note that the contents of the range,
     * rather than the range itself, is added to the node.
     *
     * @param range
     * @param offset
     */
    public abstract void insertRange(Node range, int offset);

    /**
     * Removes a range from the node.
     *
     * @param offset
     * @param characterCount
     *
     * @return
     * The removed range. This will be a copy of the node structure relative
     * to this node.
     */
    public abstract Node removeRange(int offset, int characterCount);

    /**
     * Returns a range from the node.
     *
     * @param offset
     * @param characterCount
     *
     * @return
     * A node containing a copy of the node structure spanning the given range,
     * relative to this node.
     */
    public abstract Node getRange(int offset, int characterCount);

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
     * @param offset
     * @param characterCount
     */
    protected void rangeInserted(int offset, int characterCount) {
        if (parent != null) {
            parent.rangeInserted(offset + this.offset, characterCount);
        }
    }

    /**
     * Called to notify a node that a range has been removed.
     *
     * @param offset
     * @param characterCount
     */
    protected void rangeRemoved(int offset, int characterCount) {
        if (parent != null) {
            parent.rangeRemoved(offset + this.offset, characterCount);
        }
    }

    /**
     * Returns the node listener list.
     */
    public ListenerList<NodeListener> getNodeListeners() {
        return nodeListeners;
    }
}
