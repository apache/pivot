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
 * Tree view node state listener interface.
 */
public interface TreeViewNodeStateListener {
    /**
     * Tree view node state listener list.
     */
    public static class Listeners extends ListenerList<TreeViewNodeStateListener>
        implements TreeViewNodeStateListener {
        @Override
        public void nodeCheckStateChanged(TreeView treeView, Path path,
            TreeView.NodeCheckState previousCheckState) {
            forEach(listener -> listener.nodeCheckStateChanged(treeView, path, previousCheckState));
        }
    }

    /**
     * Called when a node's checked state has changed.
     *
     * @param treeView           The source of this event.
     * @param path               Path to the node whose state has changed.
     * @param previousCheckState The previous check state of this node.
     */
    public void nodeCheckStateChanged(TreeView treeView, Path path,
        TreeView.NodeCheckState previousCheckState);
}
