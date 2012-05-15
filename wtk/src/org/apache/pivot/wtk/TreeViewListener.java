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

import org.apache.pivot.collections.List;
import org.apache.pivot.util.Filter;

/**
 * Tree view listener interface.
 */
public interface TreeViewListener {
    /**
     * Tree view listener adapter.
     */
    public static class Adapter implements TreeViewListener {
        @Override
        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            // empty block
        }

        @Override
        public void nodeRendererChanged(TreeView treeView,
            TreeView.NodeRenderer previousNodeRenderer) {
            // empty block
        }

        @Override
        public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
            // empty block
        }

        @Override
        public void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode) {
            // empty block
        }

        @Override
        public void checkmarksEnabledChanged(TreeView treeView) {
            // empty block
        }

        @Override
        public void showMixedCheckmarkStateChanged(TreeView treeView) {
            // empty block
        }

        @Override
        public void disabledNodeFilterChanged(TreeView treeView,
            Filter<?> previousDisabledNodeFilter) {
            // empty block
        }

        @Override
        public void disabledCheckmarkFilterChanged(TreeView treeView,
            Filter<?> previousDisabledCheckmarkFilter) {
            // empty block
        }
    }

    /**
     * Called when a tree view's data has changed.
     *
     * @param treeView
     * @param previousTreeData
     */
    public void treeDataChanged(TreeView treeView, List<?> previousTreeData);

    /**
     * Called when a tree view's node renderer has changed.
     *
     * @param treeView
     * @param previousNodeRenderer
     */
    public void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer);

    /**
     * Called when a tree view's node editor has changed.
     *
     * @param treeView
     * @param previousNodeEditor
     */
    public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor);

    /**
     * Called when a tree view's select mode has changed.
     *
     * @param treeView
     * @param previousSelectMode
     */
    public void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode);

    /**
     * Called when a tree view's checkmarks enabled flag has changed.
     *
     * @param treeView
     */
    public void checkmarksEnabledChanged(TreeView treeView);

    /**
     * Called when a tree view's "show mixed checkmark state" flag has changed.
     *
     * @param treeView
     */
    public void showMixedCheckmarkStateChanged(TreeView treeView);

    /**
     * Called when a tree view's disabled node filter has changed.
     *
     * @param treeView
     * @param previousDisabledNodeFilter
     */
    public void disabledNodeFilterChanged(TreeView treeView, Filter<?> previousDisabledNodeFilter);

    /**
     * Called when a tree view's disabled checkmark filter has changed.
     *
     * @param treeView
     * @param previousDisabledCheckmarkFilter
     */
    public void disabledCheckmarkFilterChanged(TreeView treeView,
        Filter<?> previousDisabledCheckmarkFilter);
}
