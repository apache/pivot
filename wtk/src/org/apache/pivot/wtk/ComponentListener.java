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
 * Component listener interface.
 */
public interface ComponentListener {
    /**
     * Component listeners.
     */
    public static class Listeners extends ListenerList<ComponentListener> implements
        ComponentListener {
        @Override
        public void parentChanged(Component component, Container previousParent) {
            forEach(listener -> listener.parentChanged(component, previousParent));
        }

        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            forEach(listener -> listener.sizeChanged(component, previousWidth, previousHeight));
        }

        @Override
        public void preferredSizeChanged(Component component, int previousPreferredWidth,
            int previousPreferredHeight) {
            forEach(listener -> listener.preferredSizeChanged(component, previousPreferredWidth,
                    previousPreferredHeight));
        }

        @Override
        public void widthLimitsChanged(Component component, int previousMinimumWidth,
            int previousMaximumWidth) {
            forEach(listener -> listener.widthLimitsChanged(component, previousMinimumWidth, previousMaximumWidth));
        }

        @Override
        public void heightLimitsChanged(Component component, int previousMinimumHeight,
            int previousMaximumHeight) {
            forEach(listener -> listener.heightLimitsChanged(component, previousMinimumHeight,
                    previousMaximumHeight));
        }

        @Override
        public void locationChanged(Component component, int previousX, int previousY) {
            forEach(listener -> listener.locationChanged(component, previousX, previousY));
        }

        @Override
        public void visibleChanged(Component component) {
            forEach(listener -> listener.visibleChanged(component));
        }

        @Override
        public void cursorChanged(Component component, Cursor previousCursor) {
            forEach(listener -> listener.cursorChanged(component, previousCursor));
        }

        @Override
        public void tooltipTextChanged(Component component, String previousTooltipText) {
            forEach(listener -> listener.tooltipTextChanged(component, previousTooltipText));
        }

        @Override
        public void tooltipDelayChanged(Component component, int previousTooltipDelay) {
            forEach(listener -> listener.tooltipDelayChanged(component, previousTooltipDelay));
        }

        @Override
        public void dragSourceChanged(Component component, DragSource previousDragSource) {
            forEach(listener -> listener.dragSourceChanged(component, previousDragSource));
        }

        @Override
        public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
            forEach(listener -> listener.dropTargetChanged(component, previousDropTarget));
        }

        @Override
        public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
            forEach(listener -> listener.menuHandlerChanged(component, previousMenuHandler));
        }

        @Override
        public void nameChanged(Component component, String previousName) {
            forEach(listener -> listener.nameChanged(component, previousName));
        }
    }

    /**
     * Component listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
    default void parentChanged(Component component, Container previousParent) {
    }

    /**
     * Called when a component's size has changed.
     *
     * @param component Component that changed size.
     * @param previousWidth Previous width of this component.
     * @param previousHeight Previous height of this component.
     */
    default void sizeChanged(Component component, int previousWidth, int previousHeight) {
    }

    /**
     * Called when a component's preferred size has changed.
     *
     * @param component Component that changed.
     * @param previousPreferredWidth Previous value of the preferred width for this component.
     * @param previousPreferredHeight Previous preferred height for this component.
     */
    default void preferredSizeChanged(Component component, int previousPreferredWidth,
        int previousPreferredHeight) {
    }

    /**
     * Called when a component's preferred width limits have changed.
     *
     * @param component Component that changed.
     * @param previousMinimumWidth Previously specified preferred minimum width.
     * @param previousMaximumWidth Previous value of the preferred maximum width.
     */
    default void widthLimitsChanged(Component component, int previousMinimumWidth,
        int previousMaximumWidth) {
    }

    /**
     * Called when a component's preferred height limits have changed.
     *
     * @param component Component that has changed.
     * @param previousMinimumHeight Previously given minimum height value.
     * @param previousMaximumHeight Previous maximum height value.
     */
    default void heightLimitsChanged(Component component, int previousMinimumHeight,
        int previousMaximumHeight) {
    }

    /**
     * Called when a component's location has changed.
     *
     * @param component Component that has moved.
     * @param previousX The previous X position of the component.
     * @param previousY The previous Y position.
     */
    default void locationChanged(Component component, int previousX, int previousY) {
    }

    /**
     * Called when a component's visible flag has changed.
     *
     * @param component Component that has changed visibility.
     */
    default void visibleChanged(Component component) {
    }

    /**
     * Called when a component's cursor has changed.
     *
     * @param component Component whose cursor has changed.
     * @param previousCursor Previous cursor for this component.
     */
    default void cursorChanged(Component component, Cursor previousCursor) {
    }

    /**
     * Called when a component's tooltip text has changed.
     *
     * @param component Component that changed.
     * @param previousTooltipText Previous value of this component's tooltip text.
     */
    default void tooltipTextChanged(Component component, String previousTooltipText) {
    }

    /**
     * Called when a component's tooltip delay has changed.
     *
     * @param component The component we're dealing with.
     * @param previousTooltipDelay The previous tooltip delay for this component.
     */
    default void tooltipDelayChanged(Component component, int previousTooltipDelay) {
    }

    /**
     * Called when a component's drag source has changed.
     *
     * @param component The component in question.
     * @param previousDragSource The previous value of the {@link DragSource} for this component.
     */
    default void dragSourceChanged(Component component, DragSource previousDragSource) {
    }

    /**
     * Called when a component's drop target has changed.
     *
     * @param component The component that is changing.
     * @param previousDropTarget The previous value of the {@link DropTarget} for this component.
     */
    default void dropTargetChanged(Component component, DropTarget previousDropTarget) {
    }

    /**
     * Called when a component's context menu handler has changed.
     *
     * @param component The component that has changed.
     * @param previousMenuHandler The previous menu handler object for this component.
     */
    default void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
    }

    /**
     * Called when a component's name has changed.
     *
     * @param component Component whose name changed.
     * @param previousName Previous name for this component.
     */
    default void nameChanged(Component component, String previousName) {
    }
}
