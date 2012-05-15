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
 * Split pane listener interface.
 */
public interface SplitPaneListener {
    /**
     * Split pane listener adapter.
     */
    public static class Adapter implements SplitPaneListener {
        @Override
        public void topLeftChanged(SplitPane splitPane, Component previousTopLeft) {
            // empty block
        }

        @Override
        public void bottomRightChanged(SplitPane splitPane, Component previousBottomRight) {
            // empty block
        }

        @Override
        public void orientationChanged(SplitPane splitPane) {
            // empty block
        }

        @Override
        public void primaryRegionChanged(SplitPane splitPane) {
            // empty block
        }

        @Override
        public void splitRatioChanged(SplitPane splitPane, float previousSplitRatio) {
            // empty block
        }

        @Override
        public void lockedChanged(SplitPane splitPane) {
            // empty block
        }

        @Override
        public void resizeModeChanged(SplitPane splitPane,
            SplitPane.ResizeMode previousResizeMode) {
            // empty block
        }
    }

    /**
     * Called when a split pane's top left component has changed.
     *
     * @param splitPane
     * The source of the event.
     *
     * @param previousTopLeft
     * The previous component. The new component can be found inside the splitPane.
     */
    public void topLeftChanged(SplitPane splitPane, Component previousTopLeft);

    /**
     * Called when a split pane's bottom right component has changed.
     *
     * @param splitPane
     * The source of the event.
     *
     * @param previousBottomRight
     * The previous component. The new component can be found inside the splitPane.
     */
    public void bottomRightChanged(SplitPane splitPane, Component previousBottomRight);

    /**
     * Called when a split pane's orientation has changed.
     *
     * @param splitPane
     * The source of the event.
     */
    public void orientationChanged(SplitPane splitPane);

    /**
     * Called when a split pane's primary region has changed.
     *
     * @param splitPane
     * The source of the event.
     */
    public void primaryRegionChanged(SplitPane splitPane);

    /**
     * Called when a split pane's split location has changed.
     *
     * @param splitPane
     * The source of the event.
     *
     * @param previousSplitRatio
     * The previous setting of the splitRatio.
     */
    public void splitRatioChanged(SplitPane splitPane, float previousSplitRatio);

    /**
     * Called when a split pane's locked flag has changed.
     *
     * @param splitPane
     * The source of the event.
     */
    public void lockedChanged(SplitPane splitPane);

    /**
     * Called when a split pane's split location has changed.
     *
     * @param splitPane
     * The source of the event.
     *
     * @param previousResizeMode
     * The previous setting of the resizeMode.
     */
    public void resizeModeChanged(SplitPane splitPane, SplitPane.ResizeMode previousResizeMode);
}
