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

import pivot.collections.List;

/**
 * Tree view listener interface.
 *
 * @author gbrown
 * @author tvolkert
 */
public interface TreeViewListener {
    /**
     * Tree view listener adapter.
     *
     * @author tvolkert
     */
    public static class Adapter implements TreeViewListener {
        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
        }

        public void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer) {
        }

        public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
        }

        public void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode) {
        }

        public void checkmarksEnabledChanged(TreeView treeView) {
        }

        public void showMixedCheckmarkStateChanged(TreeView treeView) {
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
}
