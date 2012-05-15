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
import org.apache.pivot.util.Vote;

/**
 * Tab pane listener interface.
 */
public interface TabPaneListener {
    /**
     * Tab pane listener adapter.
     */
    public static class Adapter implements TabPaneListener {
        @Override
        public void tabInserted(TabPane tabPane, int index) {
            // empty block
        }

        @Override
        public Vote previewRemoveTabs(TabPane tabPane, int index, int count) {
            return Vote.APPROVE;
        }

        @Override
        public void removeTabsVetoed(TabPane tabPane, Vote vote) {
            // empty block
        }

        @Override
        public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
            // empty block
        }

        @Override
        public void cornerChanged(TabPane tabPane, Component previousCorner) {
            // empty block
        }

        @Override
        public void tabDataRendererChanged(TabPane tabPane, Button.DataRenderer previousTabDataRenderer) {
            // empty block
        }

        @Override
        public void closeableChanged(TabPane tabPane) {
            // empty block
        }

        @Override
        public void collapsibleChanged(TabPane tabPane) {
            // empty block
        }
    }

    /**
     * Called when a tab has been inserted into a tab pane's tab sequence.
     *
     * @param tabPane
     * @param index
     */
    public void tabInserted(TabPane tabPane, int index);

    /**
     * Called to preview a tab removal.
     *
     * @param tabPane
     * @param index
     * @param count
     */
    public Vote previewRemoveTabs(TabPane tabPane, int index, int count);

    /**
     * Called when a tab removal has been vetoed.
     *
     * @param tabPane
     * @param reason
     */
    public void removeTabsVetoed(TabPane tabPane, Vote reason);

    /**
     * Called when a tab has been removed from a tab pane's tab sequence.
     *
     * @param tabPane
     * @param index
     * @param tabs
     */
    public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs);

    /**
     * Called when a tab pane's corner component has changed.
     *
     * @param tabPane
     * @param previousCorner
     */
    public void cornerChanged(TabPane tabPane, Component previousCorner);

    /**
     * Called when a tab pane's tab data renderer has changed.
     *
     * @param tabPane
     * @param previousTabDataRenderer
     */
    public void tabDataRendererChanged(TabPane tabPane, Button.DataRenderer previousTabDataRenderer);

    /**
     * Called when a tab pane's closeable property has changed.
     *
     * @param tabPane
     */
    public void closeableChanged(TabPane tabPane);

    /**
     * Called when a tab pane's collapsible property has changed.
     *
     * @param tabPane
     */
    public void collapsibleChanged(TabPane tabPane);
}
