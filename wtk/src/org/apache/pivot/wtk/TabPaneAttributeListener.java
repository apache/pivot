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

/**
 * Tab pane attribute listener interface.
 */
public interface TabPaneAttributeListener {
    /**
     * Tab pane attribute listener adapter.
     */
    public static class Adapter implements TabPaneAttributeListener {
        @Override
        public void tabDataChanged(TabPane tabPane, Component component, Object previousTabData) {
            // empty block
        }

        @Override
        public void tooltipTextChanged(TabPane tabPane, Component component, String previousTooltipText) {
            // empty block
        }
    }

    /**
     * Called when a tab's tab data attribute has changed.
     *
     * @param tabPane
     * @param component
     * @param previousTabData
     */
    public void tabDataChanged(TabPane tabPane, Component component, Object previousTabData);

    /**
     * Called when a tab's tooltipText attribute has changed.
     *
     * @param tabPane
     * @param component
     */
    public void tooltipTextChanged(TabPane tabPane, Component component, String previousTooltipText);
}
