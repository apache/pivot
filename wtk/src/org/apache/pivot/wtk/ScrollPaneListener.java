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
 * Scroll pane listener interface.
 */
public interface ScrollPaneListener {
    /**
     * Scroll pane listener adapter.
     */
    public static class Adapter implements ScrollPaneListener {
        @Override
        public void horizontalScrollBarPolicyChanged(ScrollPane scrollPane,
            ScrollPane.ScrollBarPolicy previousPolicy) {
            // empty block
        }

        @Override
        public void verticalScrollBarPolicyChanged(ScrollPane scrollPane,
            ScrollPane.ScrollBarPolicy previousPolicy) {
            // empty block
        }

        @Override
        public void rowHeaderChanged(ScrollPane scrollPane, Component previousRowHeader) {
            // empty block
        }

        @Override
        public void columnHeaderChanged(ScrollPane scrollPane, Component previousColumnHeader) {
            // empty block
        }

        @Override
        public void cornerChanged(ScrollPane scrollPane, Component previousCorner) {
            // empty block
        }
    }

    /**
     * Called when the scroll pane's horizontal scroll bar policy changed.
     *
     * @param scrollPane The source of the event.
     * @param previousPolicy The previous horizontal scroll bar policy.
     */
    public void horizontalScrollBarPolicyChanged(ScrollPane scrollPane,
        ScrollPane.ScrollBarPolicy previousPolicy);

    /**
     * Called when the scroll pane's vertical scroll bar policy changed.
     *
     * @param scrollPane The source of the event.
     * @param previousPolicy The previous vertical scroll bar policy.
     */
    public void verticalScrollBarPolicyChanged(ScrollPane scrollPane,
        ScrollPane.ScrollBarPolicy previousPolicy);

    /**
     * Called when the scroll pane's row header changed.
     *
     * @param scrollPane The source of the event.
     * @param previousRowHeader The previous row header for this scroll pane.
     */
    public void rowHeaderChanged(ScrollPane scrollPane, Component previousRowHeader);

    /**
     * Called when the scroll pane's column header changed.
     *
     * @param scrollPane The source of the event.
     * @param previousColumnHeader The previous column header for this scroll pane.
     */
    public void columnHeaderChanged(ScrollPane scrollPane, Component previousColumnHeader);

    /**
     * Called when the scroll pane's corner component changed.
     *
     * @param scrollPane The source of the event.
     * @param previousCorner The previous corner component.
     */
    public void cornerChanged(ScrollPane scrollPane, Component previousCorner);
}
