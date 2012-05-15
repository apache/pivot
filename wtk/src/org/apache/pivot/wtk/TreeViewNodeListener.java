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

/**
 * Tree view node listener interface.
 */
public interface TreeViewNodeListener {
    /**
     * Tree view node listener adapter.
     */
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
     * @param treeView
     * @param path
     * @param index
     */
    public void nodeInserted(TreeView treeView, Path path, int index);

    /**
     * Called when nodes have been removed from the tree view.
     *
     * @param treeView
     * @param path
     * @param index
     * @param count
     * The number of nodes that were removed, or <tt>-1</tt> if all nodes
     * were removed.
     */
    public void nodesRemoved(TreeView treeView, Path path, int index, int count);

    /**
     * Called when a node in the tree view has been updated.
     *
     * @param treeView
     * @param path
     * @param index
     */
    public void nodeUpdated(TreeView treeView, Path path, int index);

    /**
     * Called when the nodes in a branch have been cleared.
     *
     * @param treeView
     */
    public void nodesCleared(TreeView treeView, Path path);

    /**
     * Called when the nodes in a branch have been sorted.
     *
     * @param treeView
     * @param path
     */
    public void nodesSorted(TreeView treeView, Path path);
}
