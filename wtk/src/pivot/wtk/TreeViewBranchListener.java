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

import pivot.collections.Sequence;

/**
 * Tree view branch listener interface.
 *
 * @author gbrown
 */
public interface TreeViewBranchListener {
    /**
     * Called when a tree node is expanded. This event can be used to perform
     * lazy loading of tree node data.
     *
     * @param treeView
     * The source of the event.
     *
     * @param path
     * The path of the node that was shown.
     */
    public void branchExpanded(TreeView treeView, Sequence<Integer> path);

    /**
     * Called when a tree node is collapsed.
     *
     * @param treeView
     * The source of the event.
     *
     * @param path
     * The path of the node that was collapsed.
     */
    public void branchCollapsed(TreeView treeView, Sequence<Integer> path);
}
