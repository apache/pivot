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
package org.apache.pivot.wtk.skin;

import java.awt.Font;

import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.ComponentMouseWheelListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Skin;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;

/**
 * Abstract base class for component skins.
 */
public abstract class ComponentSkin implements Skin, ComponentListener,
    ComponentStateListener, ComponentMouseListener, ComponentMouseButtonListener,
    ComponentMouseWheelListener, ComponentKeyListener {
    private class ShowTooltipCallback implements Runnable {
        @Override
        public void run() {
            Component component = getComponent();
            String tooltipText = component.getTooltipText();

            // The tooltip text may have been cleared while the timeout was
            // outstanding; if so, don't display the tooltip
            if (tooltipText != null) {
                final Tooltip tooltip = new Tooltip(new Label(tooltipText));

                Point location = component.getDisplay().getMouseLocation();
                int x = location.x;
                int y = location.y;

                // Ensure that the tooltip stays on screen
                Display display = component.getDisplay();
                int tooltipHeight = tooltip.getPreferredHeight();
                if (y + tooltipHeight > display.getHeight()) {
                    y -= tooltipHeight;
                }

                tooltip.setLocation(x + 16, y);
                tooltip.open(component.getWindow());
            }
        }
    }

    private Component component = null;

    private int width = 0;
    private int height = 0;

    private ShowTooltipCallback showTooltipCallback = new ShowTooltipCallback();
    private ApplicationContext.ScheduledCallback scheduledShowTooltipCallback = null;

    public static final int SHOW_TOOLTIP_TIMEOUT = 1000;

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public final int getBaseline() {
        return getBaseline(width, height);
    }

    @Override
    public int getBaseline(int width, int height) {
        return -1;
    }

    @Override
    public void install(Component component) {
        assert(this.component == null) : "Skin is already installed on a component.";

        component.getComponentListeners().add(this);
        component.getComponentStateListeners().add(this);
        component.getComponentMouseListeners().add(this);
        component.getComponentMouseButtonListeners().add(this);
        component.getComponentMouseWheelListeners().add(this);
        component.getComponentKeyListeners().add(this);

        this.component = component;
    }

    @Override
    public Component getComponent() {
        return component;
    }

    /**
     * By default, skins do not have a layout disposition.
     */
    @Override
    public Orientation getDisposition() {
        return null;
    }

    /**
     * By default, skins are focusable.
     */
    @Override
    public boolean isFocusable() {
        return true;
    }

    /**
     * By default, skins are assumed to be opaque.
     */
    @Override
    public boolean isOpaque() {
        return true;
    }

    // Component events
    @Override
    public void parentChanged(Component component, Container previousParent) {
        // No-op
    }

    @Override
    public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        // No-op
    }

    @Override
    public void preferredSizeChanged(Component component,
        int previousPreferredWidth, int previousPreferredHeight) {
        // No-op
    }

    @Override
    public void widthLimitsChanged(Component component, int previousMinimumWidth,
        int previousMaximumWidth) {
        // No-op
    }

    @Override
    public void heightLimitsChanged(Component component, int previousMinimumHeight,
        int previousMaximumHeight) {
        // No-op
    }

    @Override
    public void locationChanged(Component component, int previousX, int previousY) {
        // No-op
    }

    @Override
    public void visibleChanged(Component component) {
        // No-op
    }

    @Override
    public void styleUpdated(Component component, String styleKey, Object previousValue) {
        // No-op
    }

    @Override
    public void cursorChanged(Component component, Cursor previousCursor) {
        // No-op
    }

    @Override
    public void tooltipTextChanged(Component component, String previousTooltipText) {
        // TODO Handle change here instead of in ShowTooltipCallback?
    }

    @Override
    public void dragSourceChanged(Component component, DragSource previousDragSource) {
        // No-op
    }

    @Override
    public void dropTargetChanged(Component component, DropTarget previousDropTarget) {
        // No-op
    }

    @Override
    public void menuHandlerChanged(Component component, MenuHandler previousMenuHandler) {
        // No-op
    }

    @Override
    public void nameChanged(Component component, String previousName) {
        // No-op
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        // No-op
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        // No-op
    }

    // Component mouse events
    @Override
    public boolean mouseMove(Component component, int x, int y) {
        if (scheduledShowTooltipCallback != null) {
            scheduledShowTooltipCallback.cancel();
            scheduledShowTooltipCallback = null;
        }

        if (getComponent().getTooltipText() != null) {
            scheduledShowTooltipCallback =
                ApplicationContext.scheduleCallback(showTooltipCallback, SHOW_TOOLTIP_TIMEOUT);
        }

        return false;
    }

    @Override
    public void mouseOver(Component component) {
    }

    @Override
    public void mouseOut(Component component) {
        if (scheduledShowTooltipCallback != null) {
            scheduledShowTooltipCallback.cancel();
            scheduledShowTooltipCallback = null;
        }
    }

    // Component mouse button events
    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        if (scheduledShowTooltipCallback != null) {
            scheduledShowTooltipCallback.cancel();
            scheduledShowTooltipCallback = null;
        }

        return false;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        return false;
    }

    // Component mouse wheel events
    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        return false;
    }

    // Component key events
    @Override
    public boolean keyTyped(Component component, char character) {
        return false;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.TAB
            && getComponent().isFocused()) {
            FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;

            Component previousFocusedComponent = Component.getFocusedComponent();
            previousFocusedComponent.transferFocus(direction);

            Component focusedComponent = Component.getFocusedComponent();

            if (previousFocusedComponent != focusedComponent) {
                // Ensure that the focused component is visible if it is in a viewport
                focusedComponent.scrollAreaToVisible(0, 0, focusedComponent.getWidth(),
                    focusedComponent.getHeight());

                consumed = true;
            }
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        return false;
    }

    // Utility methods
    protected void invalidateComponent() {
        if (component != null) {
            component.invalidate();
            component.repaint();
        }
    }

    protected void repaintComponent() {
        repaintComponent(false);
    }

    protected void repaintComponent(boolean immediate) {
        if (component != null) {
            component.repaint(immediate);
        }
    }

    protected void repaintComponent(Bounds area) {
        assert (area != null) : "area is null.";

        if (component != null) {
            component.repaint(area.x, area.y, area.width, area.height);
        }
    }

    protected void repaintComponent(int x, int y, int width, int height) {
        if (component != null) {
            component.repaint(x, y, width, height);
        }
    }

    protected void repaintComponent(int x, int y, int width, int height, boolean immediate) {
        if (component != null) {
            component.repaint(x, y, width, height, immediate);
        }
    }

    public static Font decodeFont(String value) {
        Font font;
        if (value.startsWith("{")) {
            try {
                font = Theme.deriveFont(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            font = Font.decode(value);
        }

        return font;
    }
}
