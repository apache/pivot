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

/**
 * Node listener interface.
 */
public interface NodeListener {
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
        public void rangeRemoved(Node node, int offset, int characterCount) {
            // empty block
        }
    }

    /**
     * Called when a node's parent has changed, either as a result of being
     * added to or removed from an element.
     *
     * @param node
     * @param previousParent
     */
    public void parentChanged(Node node, Element previousParent);

    /**
     * Called when a node's offset has changed within it's parent element.
     *
     * @param node
     * @param previousOffset
     */
    public void offsetChanged(Node node, int previousOffset);

    /**
     * Called when a child node has been inserted into a node.
     *
     * @param node
     * @param offset
     */
    public void nodeInserted(Node node, int offset);

    /**
     * Called when child nodes have been removed from a node.
     *
     * @param node
     * @param removed
     * @param offset
     */
    public void nodesRemoved(Node node, Sequence<Node> removed, int offset);

    /**
     * Called when a text range has been inserted into a node.
     *
     * @param node
     * @param offset
     * @param span
     */
    public void rangeInserted(Node node, int offset, int span);

    /**
     * Called when a text range has been removed from a node.
     *
     * @param node
     * @param offset
     * @param characterCount
     */
    public void rangeRemoved(Node node, int offset, int characterCount);
}
