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

import org.apache.pivot.util.ListenerList;

/**
 * Tab pane attribute listener interface.
 */
public interface TabPaneAttributeListener {
    /**
     * Tab pane attribute listeners.
     */
    public static class Listeners extends ListenerList<TabPaneAttributeListener>
        implements TabPaneAttributeListener {
        @Override
        public void tabDataChanged(TabPane tabPane, Component component, Object previousTabData) {
            forEach(listener -> listener.tabDataChanged(tabPane, component, previousTabData));
        }

        @Override
        public void tooltipTextChanged(TabPane tabPane, Component component,
            String previousTooltipText) {
            forEach(listener -> listener.tooltipTextChanged(tabPane, component, previousTooltipText));
        }
    }

    /**
     * Tab pane attribute listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TabPaneAttributeListener {
        @Override
        public void tabDataChanged(TabPane tabPane, Component component, Object previousTabData) {
            // empty block
        }

        @Override
        public void tooltipTextChanged(TabPane tabPane, Component component,
            String previousTooltipText) {
            // empty block
        }
    }

    /**
     * Called when a tab's tab data attribute has changed.
     *
     * @param tabPane The source of this event.
     * @param component The component whose tab pane data has changed.
     * @param previousTabData What the tab data attribute used to be.
     */
    default void tabDataChanged(TabPane tabPane, Component component, Object previousTabData) {
    }

    /**
     * Called when a tab's tooltipText attribute has changed.
     *
     * @param tabPane The source of this event.
     * @param component The actual tab component whose tooltip was changed.
     * @param previousTooltipText What the text used to be.
     */
    default void tooltipTextChanged(TabPane tabPane, Component component, String previousTooltipText) {
    }
}
