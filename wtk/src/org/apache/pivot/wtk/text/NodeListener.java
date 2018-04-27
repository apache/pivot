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
 * Node listener interface.
 */
public interface NodeListener {
    /**
     * The node listeners.
     */
    public static class Listeners extends ListenerList<NodeListener> implements NodeListener {
        @Override
        public void parentChanged(Node node, Element previousParent) {
            forEach(listener -> listener.parentChanged(node, previousParent));
        }

        @Override
        public void offsetChanged(Node node, int previousOffset) {
            forEach(listener -> listener.offsetChanged(node, previousOffset));
        }

        /**
         * @param offset Offset relative to this node.
         */
        @Override
        public void nodeInserted(Node node, int offset) {
            forEach(listener -> listener.nodeInserted(node, offset));
        }

        /**
         * @param offset Offset relative to this node.
         */
        @Override
        public void nodesRemoved(Node node, Sequence<Node> removed, int offset) {
            forEach(listener -> listener.nodesRemoved(node, removed, offset));
        }

        /**
         * @param offset Offset relative to this node.
         */
        @Override
        public void rangeInserted(Node node, int offset, int characterCount) {
            forEach(listener -> listener.rangeInserted(node, offset, characterCount));
        }

        /**
         * @param offset Offset relative to this node.
         */
        @Override
        public void rangeRemoved(Node node, int offset, int characterCount, CharSequence removedChars) {
            forEach(listener -> listener.rangeRemoved(node, offset, characterCount, removedChars));
        }
    }

    /**
     * Default implementation of the {@link NodeListener} interface.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public class Adapter implements NodeListener {
        @Override
        public void offsetChanged(Node node, int previousOffset) {
            // empty block
        }

        @Override
        public void parentChanged(Node node, Element previousParent) {
            // empty block
        }

        @Override
        public void nodeInserted(Node node, int offset) {
            // empty block
        }

        @Override
        public void nodesRemoved(Node node, Sequence<Node> removed, int offset) {
            // empty block
        }

        @Override
        public void rangeInserted(Node node, int offset, int span) {
            // empty block
        }

        @Override
        public void rangeRemoved(Node node, int offset, int characterCount, CharSequence removedChars) {
            // empty block
        }
    }

    /**
     * Called when a node's parent has changed, either as a result of being
     * added to or removed from an element.
     *
     * @param node           The node that moved.
     * @param previousParent What the node's parent used to be.
     */
    default void parentChanged(Node node, Element previousParent) {
    }

    /**
     * Called when a node's offset has changed within its parent element.
     *
     * @param node           The node that has been updated.
     * @param previousOffset The previous offset of this node.
     */
    default void offsetChanged(Node node, int previousOffset) {
    }

    /**
     * Called when a child node has been inserted into a node.
     *
     * @param node   The parent node that changed.
     * @param offset The offset where the child node was inserted.
     */
    default void nodeInserted(Node node, int offset) {
    }

    /**
     * Called when child nodes have been removed from a node.
     *
     * @param node    The parent node.
     * @param removed The sequence of child nodes that were removed.
     * @param offset  The starting offset of the removed nodes.
     */
    default void nodesRemoved(Node node, Sequence<Node> removed, int offset) {
    }

    /**
     * Called when a text range has been inserted into a node.
     *
     * @param node   The node where text was inserted.
     * @param offset The starting offset of the insertion.
     * @param span   Count of characters inserted.
     */
    default void rangeInserted(Node node, int offset, int span) {
    }

    /**
     * Called when a text range has been removed from a node.
     *
     * @param node           The node where text was removed.
     * @param offset         Starting offset of the text removal.
     * @param characterCount Count of characters removed.
     * @param removedChars   (optional) Actual characters that were removed
     *                       if the removal was directly from a text node,
     *                       otherwise this will be null.
     */
    default void rangeRemoved(Node node, int offset, int characterCount, CharSequence removedChars) {
    }
}
