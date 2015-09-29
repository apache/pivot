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
            // empty block
        }

        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            // empty block
        }

        @Override
        public void preferredSizeChanged(Component component, int previousPreferredWidth,
            int previousPreferredHeight) {
            // empty block
        }

        @Override
        public void widthLimitsChanged(Component component, int previousMinimumWidth,
            int previousMaximumWidth) {
            // empty block
        }

        @Override
        public void heightLimitsChanged(Component component, int previousMinimumHeight,
            int previousMaximumHeight) {
            // empty block
        }

        @Override
        public void locationChanged(Component component, int previousX, int previousY) {
            // empty block
        }

        @Override
        public void visibleChanged(Component component) {
            // empty block
        }

        @Override
        public void cursorChanged(Component component, Cursor previousCursor) {
            // empty block
        }

        @Override
        public void tooltipTextChanged(Component component, String previousTooltipText) {
            // empty block
        }

        @Override
        public void tooltipDelayChanged(Component component, int previousTooltipDelay) {
            // empty block
        }

        @Override
        public void dragSourceChanged(Component component, DragSource previousDragSource) {
            // empty block
        }

        @Override
        public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
            // empty block
        }

        @Override
        public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
            // empty block
        }

        @Override
        public void nameChanged(Component component, String previousName) {
            // empty block
        }
    }

    /**
     * Called when a component's parent has changed (when the component is
     * either added to or removed from a container).
     *
     * @param component The component whose parent changed.
     * @param previousParent Previous parent of this component.
     */
    public void parentChanged(Component component, Container previousParent);

    /**
     * Called when a component's size has changed.
     *
     * @param component Component that changed size.
     * @param previousWidth Previous width of this component.
     * @param previousHeight Previous height of this component.
     */
    public void sizeChanged(Component component, int previousWidth, int previousHeight);

    /**
     * Called when a component's preferred size has changed.
     *
     * @param component Component that changed.
     * @param previousPreferredWidth Previous value of the preferred width for this component.
     * @param previousPreferredHeight Previous preferred height for this component.
     */
    public void preferredSizeChanged(Component component, int previousPreferredWidth,
        int previousPreferredHeight);

    /**
     * Called when a component's preferred width limits have changed.
     *
     * @param component Component that changed.
     * @param previousMinimumWidth Previously specified preferred minimum width.
     * @param previousMaximumWidth Previous value of the preferred maximum width.
     */
    public void widthLimitsChanged(Component component, int previousMinimumWidth,
        int previousMaximumWidth);

    /**
     * Called when a component's preferred height limits have changed.
     *
     * @param component Component that has changed.
     * @param previousMinimumHeight Previously given minimum height value.
     * @param previousMaximumHeight Previous maximum height value.
     */
    public void heightLimitsChanged(Component component, int previousMinimumHeight,
        int previousMaximumHeight);

    /**
     * Called when a component's location has changed.
     *
     * @param component Component that has moved.
     * @param previousX The previous X position of the component.
     * @param previousY The previous Y position.
     */
    public void locationChanged(Component component, int previousX, int previousY);

    /**
     * Called when a component's visible flag has changed.
     *
     * @param component Component that has changed visibility.
     */
    public void visibleChanged(Component component);

    /**
     * Called when a component's cursor has changed.
     *
     * @param component Component whose cursor has changed.
     * @param previousCursor Previous cursor for this component.
     */
    public void cursorChanged(Component component, Cursor previousCursor);

    /**
     * Called when a component's tooltip text has changed.
     *
     * @param component Component that changed.
     * @param previousTooltipText Previous value of this component's tooltip text.
     */
    public void tooltipTextChanged(Component component, String previousTooltipText);

    /**
     * Called when a component's tooltip delay has changed.
     *
     * @param component The component we're dealing with.
     * @param previousTooltipDelay The previous tooltip delay for this component.
     */
    public void tooltipDelayChanged(Component component, int previousTooltipDelay);

    /**
     * Called when a component's drag source has changed.
     *
     * @param component The component in question.
     * @param previousDragSource The previous value of the {@link DragSource} for this component.
     */
    public void dragSourceChanged(Component component, DragSource previousDragSource);

    /**
     * Called when a component's drop target has changed.
     *
     * @param component The component that is changing.
     * @param previousDropTarget The previous value of the {@link DropTarget} for this component.
     */
    public void dropTargetChanged(Component component, DropTarget previousDropTarget);

    /**
     * Called when a component's context menu handler has changed.
     *
     * @param component The component that has changed.
     * @param previousMenuHandler The previous menu handler object for this component.
     */
    public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler);

    /**
     * Called when a component's name has changed.
     *
     * @param component Component whose name changed.
     * @param previousName Previous name for this component.
     */
    public void nameChanged(Component component, String previousName);
}
