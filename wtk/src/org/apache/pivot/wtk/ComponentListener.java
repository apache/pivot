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
 * Component listener interface.
 */
public interface ComponentListener {
    /**
     * Component listener adapter.
     */
    public static class Adapter implements ComponentListener {
        @Override
        public void parentChanged(Component component, Container previousParent) {
        }

        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        }

        @Override
        public void preferredSizeChanged(Component component, int previousPreferredWidth,
            int previousPreferredHeight) {
        }

        @Override
        public void preferredWidthLimitsChanged(Component component, int previousMinimumPreferredWidth,
            int previousMaximumPreferredWidth) {
        }

        @Override
        public void preferredHeightLimitsChanged(Component component, int previousMinimumPreferredHeight,
            int previousMaximumPreferredHeight) {
        }

        @Override
        public void locationChanged(Component component, int previousX, int previousY) {
        }

        @Override
        public void visibleChanged(Component component) {
        }

        @Override
        public void styleUpdated(Component component, String styleKey, Object previousValue) {
        }

        @Override
        public void cursorChanged(Component component, Cursor previousCursor) {
        }

        @Override
        public void tooltipTextChanged(Component component, String previousTooltipText) {
        }

        @Override
        public void dragSourceChanged(Component component, DragSource previousDragSource) {
        }

        @Override
        public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
        }

        @Override
        public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
        }

        @Override
        public void nameChanged(Component component, String previousName) {
        }
    }

    /**
     * Called when a component's parent has changed (when the component is
     * either added to or removed from a container).
     *
     * @param component
     * @param previousParent
     */
    public void parentChanged(Component component, Container previousParent);

    /**
     * Called when a component's size has changed.
     *
     * @param component
     * @param previousWidth
     * @param previousHeight
     */
    public void sizeChanged(Component component, int previousWidth, int previousHeight);

    /**
     * Called when a component's preferred size has changed.
     *
     * @param component
     * @param previousPreferredWidth
     * @param previousPreferredHeight
     */
    public void preferredSizeChanged(Component component, int previousPreferredWidth,
        int previousPreferredHeight);

    /**
     * Called when a component's preferred width limits have changed.
     *
     * @param component
     * @param previousMinimumPreferredWidth
     * @param previousMaximumPreferredWidth
     */
    public void preferredWidthLimitsChanged(Component component, int previousMinimumPreferredWidth,
        int previousMaximumPreferredWidth);

    /**
     * Called when a component's preferred height limits have changed.
     *
     * @param component
     * @param previousMinimumPreferredHeight
     * @param previousMaximumPreferredHeight
     */
    public void preferredHeightLimitsChanged(Component component, int previousMinimumPreferredHeight,
        int previousMaximumPreferredHeight);

    /**
     * Called when a component's location has changed.
     *
     * @param component
     * @param previousX
     * @param previousY
     */
    public void locationChanged(Component component, int previousX, int previousY);

    /**
     * Called when a component's visible flag has changed.
     *
     * @param component
     */
    public void visibleChanged(Component component);

    /**
     * Called when a component style has been updated.
     *
     * @param component
     * @param styleKey
     * @param previousValue
     */
    public void styleUpdated(Component component, String styleKey, Object previousValue);

    /**
     * Called when a component's cursor has changed.
     *
     * @param component
     * @param previousCursor
     */
    public void cursorChanged(Component component, Cursor previousCursor);

    /**
     * Called when a component's tooltip text has changed.
     *
     * @param component
     * @param previousTooltipText
     */
    public void tooltipTextChanged(Component component, String previousTooltipText);

    /**
     * Called when a component's drag source has changed.
     *
     * @param component
     * @param previousDragSource
     */
    public void dragSourceChanged(Component component, DragSource previousDragSource);

    /**
     * Called when a component's drop target has changed.
     *
     * @param component
     * @param previousDropTarget
     */
    public void dropTargetChanged(Component component, DropTarget previousDropTarget);

    /**
     * Called when a component's context menu handler has changed.
     *
     * @param component
     * @param previousMenuHandler
     */
    public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler);

    /**
     * Called when a component's name has changed.
     * @param component
     * @param previousName
     */
    public void nameChanged(Component component, String previousName);
}
