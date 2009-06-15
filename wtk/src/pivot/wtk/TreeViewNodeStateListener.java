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

import org.apache.pivot.collections.Sequence.Tree.Path;

/**
 * Tree view node state listener interface.
 *
 * @author gbrown
 * @author tvolkert
 */
public interface TreeViewNodeStateListener {
    /**
     * Tree view node state listener adapter.
     *
     * @author tvolkert
     */
    public static class Adapter implements TreeViewNodeStateListener {
        public void nodeDisabledChanged(TreeView treeView, Path path) {
        }

        public void nodeCheckStateChanged(TreeView treeView, Path path,
            TreeView.NodeCheckState previousCheckState) {
        }
    }

    /**
     * Called when a node's disabled state has changed.
     *
     * @param treeView
     * @param path
     */
    public void nodeDisabledChanged(TreeView treeView, Path path);

    /**
     * Called when a node's checked state has changed.
     *
     * @param treeView
     * @param path
     * @param previousCheckState
     */
    public void nodeCheckStateChanged(TreeView treeView, Path path,
        TreeView.NodeCheckState previousCheckState);
}
