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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.util.ListenerList;

/**
 * Tree view node listener interface.
 */
public interface TreeViewNodeListener {
    /**
     * Tree view node listener list.
     */
    public static class Listeners extends ListenerList<TreeViewNodeListener>
        implements TreeViewNodeListener {
        @Override
        public void nodeInserted(TreeView treeView, Path path, int index) {
            forEach(listener -> listener.nodeInserted(treeView, path, index));
        }

        @Override
        public void nodesRemoved(TreeView treeView, Path path, int index, int count) {
            forEach(listener -> listener.nodesRemoved(treeView, path, index, count));
        }

        @Override
        public void nodeUpdated(TreeView treeView, Path path, int index) {
            forEach(listener -> listener.nodeUpdated(treeView, path, index));
        }

        @Override
        public void nodesCleared(TreeView treeView, Path path) {
            forEach(listener -> listener.nodesCleared(treeView, path));
        }

        @Override
        public void nodesSorted(TreeView treeView, Path path) {
            forEach(listener -> listener.nodesSorted(treeView, path));
        }
    }

    /**
     * Tree view node listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TreeViewNodeListener {
        @Override
        public void nodeInserted(TreeView treeView, Path path, int index) {
            // empty block
        }

        @Override
        public void nodesRemoved(TreeView treeView, Path path, int index, int count) {
            // empty block
        }

        @Override
        public void nodeUpdated(TreeView treeView, Path path, int index) {
            // empty block
        }

        @Override
        public void nodesCleared(TreeView treeView, Path path) {
            // empty block
        }

        @Override
        public void nodesSorted(TreeView treeView, Path path) {
            // empty block
        }
    }

    /**
     * Called when a node has been inserted into the tree view.
     *
     * @param treeView The source of this event.
     * @param path     The path to the branch where the node was inserted.
     * @param index    The index of the newly inserted node within the branch.
     */
    default void nodeInserted(TreeView treeView, Path path, int index) {
    }

    /**
     * Called when nodes have been removed from the tree view.
     *
     * @param treeView The source of this event.
     * @param path     The path to the branch where the node(s) were removed.
     * @param index    The index to the first removed node within the branch.
     * @param count    The number of nodes that were removed, or <tt>-1</tt> if all
     * nodes were removed.
     */
    default void nodesRemoved(TreeView treeView, Path path, int index, int count) {
    }

    /**
     * Called when a node in the tree view has been updated.
     *
     * @param treeView The source of this event.
     * @param path     Path to the branch that is the parent of the updated node.
     * @param index    Index of the updated node within the branch.
     */
    default void nodeUpdated(TreeView treeView, Path path, int index) {
    }

    /**
     * Called when the nodes in a branch have been cleared.
     *
     * @param treeView The source of this event.
     * @param path     Path to the branch where the nodes were cleared.
     */
    default void nodesCleared(TreeView treeView, Path path) {
    }

    /**
     * Called when the nodes in a branch have been sorted.
     *
     * @param treeView The source of this event.
     * @param path     Path to the branch where the nodes were sorted.
     */
    default void nodesSorted(TreeView treeView, Path path) {
    }
}
