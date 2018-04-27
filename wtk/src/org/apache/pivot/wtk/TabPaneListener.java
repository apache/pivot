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
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.VoteResult;

/**
 * Tab pane listener interface.
 */
public interface TabPaneListener {
    /**
     * Tab pane listeners.
     */
    public static class Listeners extends ListenerList<TabPaneListener> implements TabPaneListener {
        @Override
        public void tabInserted(TabPane tabPane, int index) {
            forEach(listener -> listener.tabInserted(tabPane, index));
        }

        @Override
        public Vote previewRemoveTabs(TabPane tabPane, int index, int count) {
            VoteResult result = new VoteResult();

            forEach(listener -> result.tally(listener.previewRemoveTabs(tabPane, index, count)));

            return result.get();
        }

        @Override
        public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
            forEach(listener -> listener.tabsRemoved(tabPane, index, tabs));
        }

        @Override
        public void removeTabsVetoed(TabPane tabPane, Vote reason) {
            forEach(listener -> listener.removeTabsVetoed(tabPane, reason));
        }

        @Override
        public void cornerChanged(TabPane tabPane, Component previousCorner) {
            forEach(listener -> listener.cornerChanged(tabPane, previousCorner));
        }

        @Override
        public void tabDataRendererChanged(TabPane tabPane,
            Button.DataRenderer previousTabDataRenderer) {
            forEach(listener -> listener.tabDataRendererChanged(tabPane, previousTabDataRenderer));
        }

        @Override
        public void closeableChanged(TabPane tabPane) {
            forEach(listener -> listener.closeableChanged(tabPane));
        }

        @Override
        public void collapsibleChanged(TabPane tabPane) {
            forEach(listener -> listener.collapsibleChanged(tabPane));
        }
    }

    /**
     * Tab pane listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
        public void tabDataRendererChanged(TabPane tabPane,
            Button.DataRenderer previousTabDataRenderer) {
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
     * @param tabPane The source of this event.
     * @param index Where the newly inserted tab was placed.
     */
    default void tabInserted(TabPane tabPane, int index) {
    }

    /**
     * Called to preview a tab removal.
     *
     * @param tabPane The component that wants to change.
     * @param index The proposed starting index of the tab or tabs to be removed.
     * @param count The count of tabs to remove.
     * @return Whether or not to accept this tab removal (or defer it).
     */
    default Vote previewRemoveTabs(TabPane tabPane, int index, int count) {
        return Vote.APPROVE;
    }

    /**
     * Called when a tab removal has been vetoed.
     *
     * @param tabPane The source of this event.
     * @param reason The vote result that vetoed the tab removal.
     */
    default void removeTabsVetoed(TabPane tabPane, Vote reason) {
    }

    /**
     * Called when a tab has been removed from a tab pane's tab sequence.
     *
     * @param tabPane The source of this event.
     * @param index The starting location of the tabs that were removed.
     * @param tabs The actual sequence of tab components that were removed.
     */
    default void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
    }

    /**
     * Called when a tab pane's corner component has changed.
     *
     * @param tabPane The component that changed.
     * @param previousCorner What the corner component used to be.
     */
    default void cornerChanged(TabPane tabPane, Component previousCorner) {
    }

    /**
     * Called when a tab pane's tab data renderer has changed.
     *
     * @param tabPane The source of this event.
     * @param previousTabDataRenderer The previous renderer for the tab data.
     */
    default void tabDataRendererChanged(TabPane tabPane,
        Button.DataRenderer previousTabDataRenderer) {
    }

    /**
     * Called when a tab pane's closeable property has changed.
     *
     * @param tabPane The component that changed.
     */
    default void closeableChanged(TabPane tabPane) {
    }

    /**
     * Called when a tab pane's collapsible property has changed.
     *
     * @param tabPane The source of this event.
     */
    default void collapsibleChanged(TabPane tabPane) {
    }
}
