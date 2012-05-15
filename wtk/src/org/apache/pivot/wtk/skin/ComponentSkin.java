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
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.ComponentMouseWheelListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.ComponentTooltipListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Skin;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.Modifier;

/**
 * Abstract base class for component skins.
 */
public abstract class ComponentSkin implements Skin, ComponentListener,
    ComponentStateListener, ComponentMouseListener, ComponentMouseButtonListener,
    ComponentMouseWheelListener, ComponentKeyListener, ComponentTooltipListener {
    private Component component = null;

    private int width = 0;
    private int height = 0;

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
    public int getBaseline(int widthArgument, int heightArgument) {
        return -1;
    }

    @Override
    public void install(Component componentArgument) {
        assert(this.component == null) : "Skin is already installed on a component.";

        componentArgument.getComponentListeners().add(this);
        componentArgument.getComponentStateListeners().add(this);
        componentArgument.getComponentMouseListeners().add(this);
        componentArgument.getComponentMouseButtonListeners().add(this);
        componentArgument.getComponentMouseWheelListeners().add(this);
        componentArgument.getComponentKeyListeners().add(this);
        componentArgument.getComponentTooltipListeners().add(this);

        this.component = componentArgument;
    }

    @Override
    public Component getComponent() {
        return component;
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
    public void parentChanged(Component componentArgument, Container previousParent) {
        // No-op
    }

    @Override
    public void sizeChanged(Component componentArgument, int previousWidth, int previousHeight) {
        // No-op
    }

    @Override
    public void preferredSizeChanged(Component componentArgument,
        int previousPreferredWidth, int previousPreferredHeight) {
        // No-op
    }

    @Override
    public void widthLimitsChanged(Component componentArgument, int previousMinimumWidth,
        int previousMaximumWidth) {
        // No-op
    }

    @Override
    public void heightLimitsChanged(Component componentArgument, int previousMinimumHeight,
        int previousMaximumHeight) {
        // No-op
    }

    @Override
    public void locationChanged(Component componentArgument, int previousX, int previousY) {
        // No-op
    }

    @Override
    public void visibleChanged(Component componentArgument) {
        // No-op
    }

    @Override
    public void cursorChanged(Component componentArgument, Cursor previousCursor) {
        // No-op
    }

    @Override
    public void tooltipTextChanged(Component componentArgument, String previousTooltipText) {
        // No-op
    }

    @Override
    public void tooltipDelayChanged(Component componentArgument, int previousTooltipDelay) {
        // No-op
    }

    @Override
    public void dragSourceChanged(Component componentArgument, DragSource previousDragSource) {
        // No-op
    }

    @Override
    public void dropTargetChanged(Component componentArgument, DropTarget previousDropTarget) {
        // No-op
    }

    @Override
    public void menuHandlerChanged(Component componentArgument, MenuHandler previousMenuHandler) {
        // No-op
    }

    @Override
    public void nameChanged(Component componentArgument, String previousName) {
        // No-op
    }

    // Component state events
    @Override
    public void enabledChanged(Component componentArgument) {
        // No-op
    }

    @Override
    public void focusedChanged(Component componentArgument, Component obverseComponent) {
        // No-op
    }

    // Component mouse events
    @Override
    public boolean mouseMove(Component componentArgument, int x, int y) {
        return false;
    }

    @Override
    public void mouseOver(Component componentArgument) {
        // No-op
    }

    @Override
    public void mouseOut(Component componentArgument) {
        // No-op
    }

    // Component mouse button events
    @Override
    public boolean mouseDown(Component componentArgument, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseUp(Component componentArgument, Mouse.Button button, int x, int y) {
        return false;
    }

    @Override
    public boolean mouseClick(Component componentArgument, Mouse.Button button, int x, int y, int count) {
        return false;
    }

    // Component mouse wheel events
    @Override
    public boolean mouseWheel(Component componentArgument, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        return false;
    }

    // Component key events
    @Override
    public boolean keyTyped(Component componentArgument, char character) {
        return false;
    }

    /**
     * {@link KeyCode#TAB TAB} Transfers focus forwards<br>
     * {@link KeyCode#TAB TAB} + {@link Modifier#SHIFT SHIFT} Transfers focus
     * backwards
     */
    @Override
    public boolean keyPressed(Component componentArgument, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.TAB
            && getComponent().isFocused()) {
            FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;

            // Transfer focus to the next component
            Component focusedComponent = component.transferFocus(direction);

            // Ensure that the focused component is visible
            if (component != focusedComponent
                && focusedComponent != null) {
                focusedComponent.scrollAreaToVisible(0, 0, focusedComponent.getWidth(),
                    focusedComponent.getHeight());
            }

            consumed = true;
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component componentArgument, int keyCode, Keyboard.KeyLocation keyLocation) {
        return false;
    }

    @Override
    public void tooltipTriggered(Component componentArgument, int x, int y) {
        String tooltipText = component.getTooltipText();

        if (tooltipText != null) {
            Tooltip tooltip = new Tooltip(new Label(tooltipText));

            Display display = component.getDisplay();
            Point location = component.mapPointToAncestor(display, x, y);

            // Ensure that the tooltip stays on screen
            int tooltipX = location.x + 16;
            int tooltipY = location.y;

            int tooltipWidth = tooltip.getPreferredWidth();
            int tooltipHeight = tooltip.getPreferredHeight();
            if (tooltipX + tooltipWidth > display.getWidth()) {
                // Try to just fit it inside the display if
                // there would be room to shift it above the
                // cursor, otherwise move it to the left of
                // the cursor
                if (tooltipY > tooltipHeight) {
                    tooltipX = display.getWidth() - tooltipWidth;
                } else {
                    tooltipX = location.x - tooltipWidth - 16;
                }
                if (tooltipX < 0) {
                    tooltipX = 0;
                }
                // Adjust the y location if the tip ends up
                // being behind the mouse cursor because of
                // these x adjustments
                if (tooltipX < location.x && tooltipX + tooltipWidth > location.x) {
                    tooltipY -= tooltipHeight;
                    if (tooltipY < 0) {
                        tooltipY = 0;
                    }
                }
            }
            if (tooltipY + tooltipHeight > display.getHeight()) {
                tooltipY -= tooltipHeight;
            }

            tooltip.setLocation(tooltipX, tooltipY);
            tooltip.open(component.getWindow());
        }
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

    protected void repaintComponent(int x, int y, int widthArgument, int heightArgument) {
        if (component != null) {
            component.repaint(x, y, widthArgument, heightArgument);
        }
    }

    protected void repaintComponent(int x, int y, int widthArgument, int heightArgument, boolean immediate) {
        if (component != null) {
            component.repaint(x, y, widthArgument, heightArgument, immediate);
        }
    }

    /**
     * Interpret a string as a font specification
     * @param value Either a JSON dictionary
     * {@link Theme#deriveFont describing a font relative to the current theme}, or
     * one of the {@link Font#decode(String) standard Java font specifications}.
     */
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
