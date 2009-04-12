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

import pivot.collections.Sequence;

/**
 * Tab pane listener interface.
 *
 * @author gbrown
 */
public interface TabPaneListener {
    /**
     * Adapts the <tt>TabPaneListener</tt> interface.
     *
     * @author tvolkert
     */
    public static class Adapter implements TabPaneListener {
        public void tabOrientationChanged(TabPane tabPane) {
        }

        public void collapsibleChanged(TabPane tabPane) {
        }

        public void tabInserted(TabPane tabPane, int index) {
        }

        public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs) {
        }

        public void cornerChanged(TabPane tabPane, Component previousCorner) {
        }
    }

    /**
     * Called when a tab pane's orientation has changed.
     *
     * @param tabPane
     */
    public void tabOrientationChanged(TabPane tabPane);

    /**
     * Called when a tab pane's collapsible flag has changed.
     *
     * @param tabPane
     */
    public void collapsibleChanged(TabPane tabPane);

    /**
     * Called when a tab has been inserted into a tab pane's tab sequence.
     *
     * @param tabPane
     * @param index
     */
    public void tabInserted(TabPane tabPane, int index);

    /**
     * Called when a tab has been removed from a tab pane's tab sequence.
     *
     * @param tabPane
     * @param index
     * @param tabs
     */
    public void tabsRemoved(TabPane tabPane, int index, Sequence<Component> tabs);

    /**
     * Called when a tab pane's corner component (the component in the free
     * space next to the tabs) has changed.
     *
     * @param tabPane
     * @param previousCorner
     */
    public void cornerChanged(TabPane tabPane, Component previousCorner);
}
