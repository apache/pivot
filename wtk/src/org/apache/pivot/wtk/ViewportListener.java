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
 * Viewport listener interface.
 */
public interface ViewportListener {
    /**
     * Viewport listener adapter.
     */
    public static class Adapter implements ViewportListener {
        @Override
        public void scrollTopChanged(Viewport scrollPane, int previousScrollTop) {
            // empty block
        }

        @Override
        public void scrollLeftChanged(Viewport scrollPane, int previousScrollLeft) {
            // empty block
        }

        @Override
        public void viewChanged(Viewport scrollPane, Component previousView) {
            // empty block
        }
    }

    /**
     * Called when a viewport's scroll top has changed.
     *
     * @param scrollPane
     * @param previousScrollTop
     */
    public void scrollTopChanged(Viewport scrollPane, int previousScrollTop);

    /**
     * Called when a viewport's scroll left has changed.
     *
     * @param scrollPane
     * @param previousScrollLeft
     */
    public void scrollLeftChanged(Viewport scrollPane, int previousScrollLeft);

    /**
     * Called when a viewport's view component has changed.
     *
     * @param scrollPane
     * @param previousView
     */
    public void viewChanged(Viewport scrollPane, Component previousView);
}
