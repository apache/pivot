/*
 * Copyright (c) 2008 VMware, Inc.
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
