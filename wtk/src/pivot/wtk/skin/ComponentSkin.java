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

import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentStateListener;
import pivot.wtk.Direction;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Bounds;
import pivot.wtk.Skin;
import pivot.wtk.Tooltip;

/**
 * Abstract base class for WTK skins.
 *
 * @author gbrown
 */
public abstract class ComponentSkin implements Skin, ComponentStateListener {
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

    public void install(Component component) {
        assert(this.component == null) : "Skin is already installed on a component.";

        this.component = component;

        component.getComponentStateListeners().add(this);
    }

    public void uninstall() {
        component.getComponentStateListeners().remove(this);

        component = null;
    }

    public boolean mouseMove(int x, int y) {
        ApplicationContext.clearTimeout(showTooltipTimeoutID);

        if (getComponent().getTooltipText() != null) {
            showTooltipTimeoutID = ApplicationContext.setTimeout(showTooltipCallback,
                SHOW_TOOLTIP_TIMEOUT);
        }

        return false;
    }

    public void mouseOver() {
    }

    public void mouseOut() {
        ApplicationContext.clearTimeout(showTooltipTimeoutID);
    }

    public boolean mouseDown(Mouse.Button button, int x, int y) {
        return false;
    }

    public boolean mouseUp(Mouse.Button button, int x, int y) {
        return false;
    }

    public void mouseClick(Mouse.Button button, int x, int y, int count) {
    }

    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        return false;
    }

    /**
     * By default, components are focusable.
     */
    public boolean isFocusable() {
        return true;
    }

    public void keyTyped(char character) {
    }

    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.TAB) {
            Direction direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                Direction.BACKWARD : Direction.FORWARD;

            Component previousFocusedComponent = Component.getFocusedComponent();
            Component.transferFocus(direction);
            Component focusedComponent = Component.getFocusedComponent();

            consumed = (previousFocusedComponent != focusedComponent);
        }

        return consumed;
    }

    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        return false;
    }

    public Component getComponent() {
        return component;
    }

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

    /**
     * Verifies that a component is of the correct type.
     *
     * @param component
     * @param type
     */
    protected static final void validateComponentType(Component component,
        Class<?> type) {
        if (!type.isInstance(component)) {
            throw new IllegalArgumentException("Component must be an instance of "
                + type);
        }
    }

    /**
     * Verifies that a style property is of the correct type.
     *
     * @param key
     * @param value
     * @param type
     * @param nullable
     *
     * @throws IllegalArgumentException
     * If the type of <tt>value</tt> does not match the given type.
     */
    protected static final void validatePropertyType(String key, Object value,
        Class<?> type, boolean nullable) {
        if (value == null) {
            if (!nullable) {
                throw new IllegalArgumentException(key + " must not be null.");
            }
        }
        else {
            if (!type.isInstance(value)) {
                throw new IllegalArgumentException(key + " must be an instance of " + type);
            }
        }
    }

    // ComponentStateListener methods

    public boolean previewEnabledChange(Component component) {
        return true;
    }

    public void enabledChanged(Component component) {
    }

    public boolean previewFocusedChange(Component component, boolean temporary) {
        return true;
    }

    public void focusedChanged(Component component, boolean temporary) {
        // Ensure that the component is visible if it is in a viewport
        if (component.isFocused()
            && !temporary) {
            component.scrollAreaToVisible(0, 0, getWidth(), getHeight());
        }
    }
}
