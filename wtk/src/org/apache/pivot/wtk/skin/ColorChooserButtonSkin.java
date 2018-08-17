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

import java.awt.Color;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ColorChooser;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.ColorChooserButtonSelectionListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for color chooser button skins.
 */
public abstract class ColorChooserButtonSkin extends ButtonSkin implements ColorChooserButton.Skin,
    ColorChooserButtonSelectionListener {
    /**
     * A focusable window class used by color chooser button skins.
     */
    public final class ColorChooserPopup extends Window {
        private ColorChooserPopup() {
            setSkin(new ColorChooserPopupSkin());
        }
    }

    /**
     * The color chooser popup skin.
     */
    public final class ColorChooserPopupSkin extends WindowSkin {
        private ColorChooserPopupSkin() {
        }

        @Override
        public boolean isFocusable() {
            return true;
        }

        @Override
        public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
            final int count) {
            component.requestFocus();
            return super.mouseClick(component, button, x, y, count);
        }
    }

    private ComponentKeyListener colorChooserPopupKeyListener = new ComponentKeyListener() {
        /**
         * {@link KeyCode#ESCAPE ESCAPE} Close the popup.<br>
         * {@link KeyCode#ENTER ENTER} Choose the selected color.<br>
         * {@link KeyCode#TAB TAB} Choose the selected color and transfer focus
         * forwards.<br> {@link KeyCode#TAB TAB} + {@link Modifier#SHIFT SHIFT}
         * Choose the selected color and transfer focus backwards.
         */
        @Override
        public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
            ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();

            switch (keyCode) {
                case KeyCode.ESCAPE:
                    colorChooserPopup.close();
                    break;

                case KeyCode.TAB:
                case KeyCode.ENTER:
                    colorChooserPopup.close();

                    if (keyCode == KeyCode.TAB) {
                        FocusTraversalDirection direction = (Keyboard.isPressed(Modifier.SHIFT))
                            ? FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;
                        colorChooserButton.transferFocus(direction);
                    }

                    Color color = colorChooser.getSelectedColor();
                    colorChooserButton.setSelectedColor(color);

                    break;

                default:
                    break;
            }

            return false;
        }
    };

    private WindowStateListener colorChooserPopupWindowStateListener = new WindowStateListener() {
        @Override
        public void windowOpened(final Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);

            window.requestFocus();
        }

        @Override
        public Vote previewWindowClose(final Window window) {
            if (window.containsFocus()) {
                getComponent().requestFocus();
            }

            return Vote.APPROVE;
        }

        @Override
        public void windowCloseVetoed(final Window window, final Vote reason) {
            if (reason == Vote.DENY) {
                window.requestFocus();
            }
        }

        @Override
        public void windowClosed(final Window window, final Display display, final Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);

            Window componentWindow = getComponent().getWindow();
            if (componentWindow != null && componentWindow.isOpen() && !componentWindow.isClosing()) {
                componentWindow.moveToFront();
            }
        }
    };

    private ComponentMouseButtonListener colorChooserMouseButtonListener = new ComponentMouseButtonListener() {
        @Override
        public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
            final int count) {
            ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();

            if (button == Mouse.Button.LEFT && count == 2) {
                colorChooserPopup.close();

                Color color = colorChooser.getSelectedColor();
                colorChooserButton.setSelectedColor(color);
            }

            return false;
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener() {
        @Override
        public boolean mouseDown(final Container container, final Mouse.Button button, final int x, final int y) {
            ColorChooserButton colorChooserButton = (ColorChooserButton) getComponent();

            Display display = (Display) container;
            Component descendant = display.getDescendantAt(x, y);

            if (!colorChooserPopup.isAncestor(descendant) && descendant != colorChooserButton) {
                colorChooserPopup.close();

                Color color = colorChooser.getSelectedColor();
                colorChooserButton.setSelectedColor(color);
            }

            return false;
        }

        @Override
        public boolean mouseWheel(final Container container, final Mouse.ScrollType scrollType,
            final int scrollAmount, final int wheelRotation, final int x, final int y) {
            return true;
        }
    };

    protected ColorChooser colorChooser;
    protected ColorChooserPopup colorChooserPopup;
    protected boolean pressed = false;

    public ColorChooserButtonSkin() {
        colorChooser = new ColorChooser();
        colorChooser.getComponentMouseButtonListeners().add(colorChooserMouseButtonListener);

        colorChooserPopup = new ColorChooserPopup();
        colorChooserPopup.getComponentKeyListeners().add(colorChooserPopupKeyListener);
        colorChooserPopup.getWindowStateListeners().add(colorChooserPopupWindowStateListener);
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        ColorChooserButton colorChooserButton = (ColorChooserButton) component;
        colorChooserButton.getColorChooserButtonSelectionListeners().add(this);
    }

    // ColorChooserButton.Skin methods

    @Override
    public Window getColorChooserPopup() {
        return colorChooserPopup;
    }

    // ComponentStateListener methods

    @Override
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);

        if (!component.isEnabled()) {
            pressed = false;
        }

        repaintComponent();

        colorChooserPopup.close();
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        repaintComponent();

        // Close the popup if focus was transferred to a component whose window is not the popup
        if (!component.isFocused()) {
            pressed = false;

            if (!colorChooserPopup.containsFocus()) {
                colorChooserPopup.close();
            }
        }
    }

    // ComponentMouseListener methods

    @Override
    public void mouseOut(final Component component) {
        super.mouseOut(component);

        pressed = false;
        repaintComponent();
    }

    // ComponentMouseButtonListener methods

    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        if (colorChooserPopup.isOpen()) {
            colorChooserPopup.close();
        } else {
            colorChooserPopup.open(component.getWindow());
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    // ComponentKeyListener methods

    /**
     * {@link KeyCode#SPACE SPACE} Repaints the component to reflect the pressed state.
     *
     * @see #keyReleased(Component, int, Keyboard.KeyLocation)
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == KeyCode.SPACE) {
            pressed = true;
            repaintComponent();

            if (colorChooserPopup.isOpen()) {
                colorChooserPopup.close();
            } else {
                colorChooserPopup.open(component.getWindow());
            }

            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} 'presses' the button.
     */
    @Override
    public boolean keyReleased(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == KeyCode.SPACE) {
            pressed = false;
            repaintComponent();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // ColorChooserButtonSelectionListener methods

    @Override
    public void selectedColorChanged(final ColorChooserButton colorChooserButton,
        final Color previousSelectedColor) {
        // Set the selected color as the button data
        Color selectedColor = colorChooserButton.getSelectedColor();

        colorChooserButton.setButtonData(selectedColor);
        colorChooser.setSelectedColor(selectedColor);
    }
}
