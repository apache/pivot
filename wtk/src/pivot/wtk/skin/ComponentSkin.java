/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin;

import pivot.util.Vote;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentLayoutListener;
import pivot.wtk.ComponentListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseWheelListener;
import pivot.wtk.ComponentStateListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Direction;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Bounds;
import pivot.wtk.Skin;
import pivot.wtk.Tooltip;

/**
 * Abstract base class for component skins.
 *
 * @author gbrown
 */
public abstract class ComponentSkin implements Skin, ComponentListener,
    ComponentLayoutListener, ComponentStateListener, ComponentMouseListener,
    ComponentMouseButtonListener, ComponentMouseWheelListener,
    ComponentKeyListener {
    private class ShowTooltipCallback implements Runnable {
        public void run() {
            Component component = getComponent();
            String tooltipText = component.getTooltipText();

            // The tooltip text may have been cleared while the timeout was
            // outstanding; if so, don't display the tooltip
            if (tooltipText != null) {
                // TODO Re-use a static tooltip?
                Tooltip tooltip = new Tooltip(tooltipText);

                // TODO Ensure that the tooltip stays on screen
                tooltip.setLocation(Mouse.getX() + 16, Mouse.getY());
                tooltip.open(component.getWindow());
            }
        }
    }

    private Component component = null;

    private int width = 0;
    private int height = 0;

    private ShowTooltipCallback showTooltipCallback = new ShowTooltipCallback();
    private int showTooltipTimeoutID = -1;

    public static final int SHOW_TOOLTIP_TIMEOUT = 1000;

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void install(Component component) {
        assert(this.component == null) : "Skin is already installed on a component.";

        component.getComponentListeners().add(this);
        component.getComponentLayoutListeners().add(this);
        component.getComponentStateListeners().add(this);
        component.getComponentMouseListeners().add(this);
        component.getComponentMouseButtonListeners().add(this);
        component.getComponentMouseWheelListeners().add(this);
        component.getComponentKeyListeners().add(this);

        this.component = component;
    }

    public void uninstall() {
        component.getComponentListeners().remove(this);
        component.getComponentLayoutListeners().remove(this);
        component.getComponentStateListeners().remove(this);
        component.getComponentMouseListeners().remove(this);
        component.getComponentMouseButtonListeners().remove(this);
        component.getComponentMouseWheelListeners().remove(this);
        component.getComponentKeyListeners().remove(this);

        component = null;
    }

    public Component getComponent() {
        return component;
    }

    /**
     * By default, components are focusable.
     */
    public boolean isFocusable() {
        return true;
    }

    // Component events
    public void parentChanged(Component component, Container previousParent) {
        // No-op
    }

    public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        // No-op
    }

    public void locationChanged(Component component, int previousX, int previousY) {
        // No-op
    }

    public void visibleChanged(Component component) {
        // No-op
    }

    public void styleUpdated(Component component, String styleKey, Object previousValue) {
        // No-op
    }

    public void cursorChanged(Component component, Cursor previousCursor) {
        // No-op
    }

    public void tooltipTextChanged(Component component, String previousTooltipText) {
        // TODO Handle change here instead of in ShowTooltipCallback?
    }

    // Component layout events
    public void preferredSizeChanged(Component component,
        int previousPreferredWidth, int previousPreferredHeight) {
        // No-op
    }

    public void displayableChanged(Component component) {
        // No-op
    }

    // Component state events
    public Vote previewEnabledChange(Component component) {
        return Vote.APPROVE;
    }

    public void enabledChangeVetoed(Component component, Vote reason) {
        // No-op
    }

    public void enabledChangeVetoed(Component component) {
        // No-op
    }

    public void enabledChanged(Component component) {
        // No-op
    }

    public Vote previewFocusedChange(Component component, boolean temporary) {
        return Vote.APPROVE;
    }

    public void focusedChangeVetoed(Component component, Vote reason) {
        // No-op
    }

    public void focusedChangeVetoed(Component component) {
        // No-op
    }

    public void focusedChanged(Component component, boolean temporary) {
        // Ensure that the component is visible if it is in a viewport
        if (component.isFocused()
            && !temporary) {
            component.scrollAreaToVisible(0, 0, getWidth(), getHeight());
        }
    }

    // Component mouse events
    public boolean mouseMove(Component component, int x, int y) {
        ApplicationContext.clearTimeout(showTooltipTimeoutID);

        if (getComponent().getTooltipText() != null) {
            showTooltipTimeoutID = ApplicationContext.setTimeout(showTooltipCallback,
                SHOW_TOOLTIP_TIMEOUT);
        }

        return false;
    }

    public void mouseOver(Component component) {
    }

    public void mouseOut(Component component) {
        ApplicationContext.clearTimeout(showTooltipTimeoutID);
    }

    // Component mouse button events
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        return false;
    }

    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
    }

    // Component mouse wheel events
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        return false;
    }

    // Component key events
    public void keyTyped(Component component, char character) {
    }

    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.TAB
            && getComponent().isFocused()) {
            Direction direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                Direction.BACKWARD : Direction.FORWARD;

            Component previousFocusedComponent = Component.getFocusedComponent();
            previousFocusedComponent.transferFocus(direction);

            Component focusedComponent = Component.getFocusedComponent();

            consumed = (previousFocusedComponent != focusedComponent);
        }

        return consumed;
    }

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
        if (component != null) {
            component.repaint();
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
}
