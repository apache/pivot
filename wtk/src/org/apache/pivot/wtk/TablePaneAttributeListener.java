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
 * Table pane attribute listener interface.
 */
public interface TablePaneAttributeListener {
    /**
     * Table pane attribute listener adapter.
     */
    public static class Adapter implements TablePaneAttributeListener {
        @Override
        public void rowSpanChanged(TablePane tablePane, Component component,
            int previousRowSpan) {
            // empty block
        }

        @Override
        public void columnSpanChanged(TablePane tablePane, Component component,
            int previousColumnSpan) {
            // empty block
        }
    }
    /**
     * Called when a component's row span attribute has changed.
     *
     * @param tablePane
     * @param component
     * @param previousRowSpan
     */
    public void rowSpanChanged(TablePane tablePane, Component component,
        int previousRowSpan);

    /**
     * Called when a component's column span attribute has changed.
     *
     * @param tablePane
     * @param component
     * @param previousColumnSpan
     */
    public void columnSpanChanged(TablePane tablePane, Component component,
        int previousColumnSpan);
}
