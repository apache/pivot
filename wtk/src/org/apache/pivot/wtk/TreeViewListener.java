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
import org.apache.pivot.util.ListenerList;

/**
 * Tree view listener interface.
 */
public interface TreeViewListener {
    /**
     * Tree view listener list.
     */
    public static class Listeners extends ListenerList<TreeViewListener> implements TreeViewListener {
        @Override
        public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
            forEach(listener -> listener.treeDataChanged(treeView, previousTreeData));
        }

        @Override
        public void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer) {
            forEach(listener -> listener.nodeRendererChanged(treeView, previousNodeRenderer));
        }

        @Override
        public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
            forEach(listener -> listener.nodeEditorChanged(treeView, previousNodeEditor));
        }

        @Override
        public void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode) {
            forEach(listener -> listener.selectModeChanged(treeView, previousSelectMode));
        }

        @Override
        public void checkmarksEnabledChanged(TreeView treeView) {
            forEach(listener -> listener.checkmarksEnabledChanged(treeView));
        }

        @Override
        public void showMixedCheckmarkStateChanged(TreeView treeView) {
            forEach(listener -> listener.showMixedCheckmarkStateChanged(treeView));
        }

        @Override
        public void disabledNodeFilterChanged(TreeView treeView,
            Filter<?> previousDisabledNodeFilter) {
            forEach(listener -> listener.disabledNodeFilterChanged(treeView, previousDisabledNodeFilter));
        }

        @Override
        public void disabledCheckmarkFilterChanged(TreeView treeView,
            Filter<?> previousDisabledCheckmarkFilter) {
            forEach(listener -> listener.disabledCheckmarkFilterChanged(treeView, previousDisabledCheckmarkFilter));
        }
    }

    /**
     * Tree view listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
     * @param treeView         The source of this event.
     * @param previousTreeData The previous data for this tree.
     */
    default void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
    }

    /**
     * Called when a tree view's node renderer has changed.
     *
     * @param treeView             The source of this event.
     * @param previousNodeRenderer The previous renderer for tree nodes.
     */
    default void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer) {
    }

    /**
     * Called when a tree view's node editor has changed.
     *
     * @param treeView           The source of this event.
     * @param previousNodeEditor The previous editor for tree nodes.
     */
    default void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
    }

    /**
     * Called when a tree view's select mode has changed.
     *
     * @param treeView           The source of this event.
     * @param previousSelectMode What the tree view's select mode was before the change.
     */
    default void selectModeChanged(TreeView treeView, TreeView.SelectMode previousSelectMode) {
    }

    /**
     * Called when a tree view's checkmarks enabled flag has changed.
     *
     * @param treeView The source of this event.
     */
    default void checkmarksEnabledChanged(TreeView treeView) {
    }

    /**
     * Called when a tree view's "show mixed checkmark state" flag has changed.
     *
     * @param treeView The tree view that has changed.
     */
    default void showMixedCheckmarkStateChanged(TreeView treeView) {
    }

    /**
     * Called when a tree view's disabled node filter has changed.
     *
     * @param treeView                   The tree view that has changed.
     * @param previousDisabledNodeFilter The previous filter that determines the disabled nodes.
     */
    default void disabledNodeFilterChanged(TreeView treeView, Filter<?> previousDisabledNodeFilter) {
    }

    /**
     * Called when a tree view's disabled checkmark filter has changed.
     *
     * @param treeView                        The source of this event.
     * @param previousDisabledCheckmarkFilter The previous filter that determined the disabled checkmarks.
     */
    default void disabledCheckmarkFilterChanged(TreeView treeView,
        Filter<?> previousDisabledCheckmarkFilter) {
    }
}
