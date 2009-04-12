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
package pivot.wtk;

import pivot.collections.Sequence;

/**
 * Tree view node listener interface.
 *
 * @author gbrown
 */
public interface TreeViewNodeListener {
    /**
     * Adapts the <tt>TreeViewNodeListener</tt> interface.
     *
     * @author tvolkert
     */
    public static class Adapter implements TreeViewNodeListener {
        public void nodeInserted(TreeView treeView, Sequence<Integer> path, int index) {
        }

        public void nodesRemoved(TreeView treeView, Sequence<Integer> path, int index, int count) {
        }

        public void nodeUpdated(TreeView treeView, Sequence<Integer> path, int index) {
        }

        public void nodesSorted(TreeView treeView, Sequence<Integer> path) {
        }
    }

    /**
     * Called when a node has been inserted into the tree view.
     *
     * @param treeView
     * @param path
     * @param index
     */
    public void nodeInserted(TreeView treeView, Sequence<Integer> path, int index);

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
    public void nodesRemoved(TreeView treeView, Sequence<Integer> path, int index, int count);

    /**
     * Called when a node in the tree view has been updated.
     *
     * @param treeView
     * @param path
     * @param index
     */
    public void nodeUpdated(TreeView treeView, Sequence<Integer> path, int index);

    /**
     * Called when the nodes in a branch have been sorted.
     *
     * @param treeView
     * @param path
     */
    public void nodesSorted(TreeView treeView, Sequence<Integer> path);
}
