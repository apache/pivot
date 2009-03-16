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
import pivot.util.Vote;

/**
 * Tree view node state listener interface.
 *
 * @author gbrown
 * @author tvolkert
 */
public interface TreeViewNodeStateListener {
    /**
     * Called to preview a node disabled change event.
     *
     * @param treeView
     * @param path
     */
    public Vote previewNodeDisabledChange(TreeView treeView, Sequence<Integer> path);

    /**
     * Called when a node disabled change event has been vetoed.
     *
     * @param treeView
     * @param path
     * @param reason
     */
    public void nodeDisabledChangeVetoed(TreeView treeView, Sequence<Integer> path, Vote reason);

    /**
     * Called when a node's disabled state has changed.
     *
     * @param treeView
     * @param path
     */
    public void nodeDisabledChanged(TreeView treeView, Sequence<Integer> path);
}
