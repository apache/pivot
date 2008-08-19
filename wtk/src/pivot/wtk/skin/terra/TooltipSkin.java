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
package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;

import pivot.wtk.Border;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseWheelListener;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Label;
import pivot.wtk.Mouse;
import pivot.wtk.Tooltip;
import pivot.wtk.TooltipListener;
import pivot.wtk.Window;
import pivot.wtk.skin.WindowSkin;

public class TooltipSkin extends WindowSkin
    implements ComponentMouseListener, ComponentMouseButtonListener, ComponentMouseWheelListener,
        ComponentKeyListener, TooltipListener {
    private Label label = new Label();
    private Border border = new Border();

    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(0xff, 0xff, 0xe0);
    private static final Color DEFAULT_BORDER_COLOR = Color.BLACK;
    private static final Insets DEFAULT_PADDING = new Insets(2);

    public TooltipSkin() {
        // Add the label to the border
        border.setContent(label);

        // Apply the default styles
        Component.StyleDictionary labelStyles = label.getStyles();
        labelStyles.put("font", DEFAULT_FONT);
        labelStyles.put("color", DEFAULT_COLOR);

        Component.StyleDictionary borderStyles = border.getStyles();
        borderStyles.put("backgroundColor", DEFAULT_BACKGROUND_COLOR);
        borderStyles.put("borderColor", DEFAULT_BORDER_COLOR);
        borderStyles.put("padding", DEFAULT_PADDING);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Tooltip.class);

        super.install(component);

        Tooltip tooltip = (Tooltip)component;
        tooltip.setContent(border);
        tooltip.getTooltipListeners().add(this);

        label.setText(tooltip.getTooltipText());
    }

    @Override
    public void uninstall() {
        Tooltip tooltip = (Tooltip)getComponent();
        tooltip.setContent(null);
        tooltip.getTooltipListeners().remove(this);
    }

    // Component mouse events
    public void mouseMove(Component component, int x, int y) {
        Tooltip tooltip = (Tooltip)getComponent();
        tooltip.close();
    }

    public void mouseOver(Component component) {
    }

    public void mouseOut(Component component) {
    }

    public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        Tooltip tooltip = (Tooltip)getComponent();
        tooltip.close();
    }

    public void mouseUp(Component component, Mouse.Button button, int x, int y) {
    }

    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
    }

    public void mouseWheel(Component component, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y) {
        Tooltip tooltip = (Tooltip)getComponent();
        tooltip.close();
    }

    // Component key events
    public void keyTyped(Component component, char character) {
    }

    public void keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        Tooltip tooltip = (Tooltip)getComponent();
        tooltip.close();
    }

    public void keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
    }

    // Window events
    public void windowOpened(Window window) {
        // Add this as a display mouse and key listener
        Display display = window.getDisplay();
        display.getComponentMouseListeners().add(this);
        display.getComponentMouseButtonListeners().add(this);
        display.getComponentMouseWheelListeners().add(this);
        display.getComponentKeyListeners().add(this);
    }

    public void windowClosed(Window window) {
        // Remove this as a display mouse and key listener
        Display display = window.getDisplay();
        display.getComponentMouseListeners().remove(this);
        display.getComponentMouseButtonListeners().remove(this);
        display.getComponentMouseWheelListeners().remove(this);
        display.getComponentKeyListeners().remove(this);
    }

    // Tooltip events
    public void tooltipTextChanged(Tooltip tooltip, String previousTooltipText) {
        label.setText(tooltip.getTooltipText());
    }
}
