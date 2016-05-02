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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;

/**
 * Tree view selection listener.
 */
public interface TreeViewSelectionListener {
    /**
     * Tree view selection listener adapter.
     */
    public static class Adapter implements TreeViewSelectionListener {
        @Override
        public void selectedPathAdded(TreeView treeView, Path path) {
            // empty block
        }

        @Override
        public void selectedPathRemoved(TreeView treeView, Path path) {
            // empty block
        }

        @Override
        public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths) {
            // empty block
        }

        @Override
        public void selectedNodeChanged(TreeView treeView, Object previousSelectedNode) {
            // empty block
        }
    }

    /**
     * Called when a selected path has been added to a tree view.
     *
     * @param treeView The source of this event.
     * @param path     The path that has been added to the selection.
     */
    public void selectedPathAdded(TreeView treeView, Path path);

    /**
     * Called when a selected path has been removed from a tree view.
     *
     * @param treeView The source of this event.
     * @param path     The path that was removed from the selection.
     */
    public void selectedPathRemoved(TreeView treeView, Path path);

    /**
     * Called when a tree view's selection state has been reset.
     *
     * @param treeView              The source of this event.
     * @param previousSelectedPaths The list of paths that were previously selected.
     */
    public void selectedPathsChanged(TreeView treeView, Sequence<Path> previousSelectedPaths);

    /**
     * Called when a tree view's selected node has changed.
     *
     * @param treeView             The source of this event.
     * @param previousSelectedNode The node that used to be selected.
     */
    public void selectedNodeChanged(TreeView treeView, Object previousSelectedNode);
}
