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
 *
 */
public interface SplitPaneListener {
    /**
     * Called when a split pane's top left component has changed.
     *
     * @param splitPane
     * @param previousTopLeft
     */
    public void topLeftChanged(SplitPane splitPane, Component previousTopLeft);

    /**
     * Called when a split pane's bottom right component has changed.
     *
     * @param splitPane
     * @param previousBottomRight
     */
    public void bottomRightChanged(SplitPane splitPane, Component previousBottomRight);

    /**
     * Called when a split pane's orientation has changed.
     *
     * @param splitPane
     */
    public void orientationChanged(SplitPane splitPane);

    /**
     * Called when a split pane's primary region has changed.
     *
     * @param splitPane
     */
    public void primaryRegionChanged(SplitPane splitPane);

    /**
     * Called when a split pane's split location has changed.
     *
     * @param splitPane
     * @param previousSplitRatio
     */
    public void splitRatioChanged(SplitPane splitPane, float previousSplitRatio);

    /**
     * Called when a split pane's locked flag has changed.
     *
     * @param splitPane
     */
    public void lockedChanged(SplitPane splitPane);
}
