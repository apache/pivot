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

/**
 * Tooltip skin.
 *
 * @author gbrown
 */
public class TerraTooltipSkin extends WindowSkin implements TooltipListener {
    private class CloseHandler implements ComponentMouseListener,
        ComponentMouseButtonListener, ComponentMouseWheelListener,
        ComponentKeyListener {
        // Component mouse events
        public boolean mouseMove(Component component, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        }

        public boolean mouseWheel(Component component, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        // Component key events
        public void keyTyped(Component component, char character) {
        }

        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            return false;
        }
    }

    private Label label = new Label();
    private Border border = new Border();

    private CloseHandler closeHandler = new CloseHandler();

    private static final Font FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color COLOR = Color.BLACK;
    private static final Color BACKGROUND_COLOR = new Color(0xff, 0xff, 0xe0);
    private static final Color BORDER_COLOR = Color.BLACK;
    private static final Insets PADDING = new Insets(2);


    public TerraTooltipSkin() {
        // Add the label to the border
        border.setContent(label);

        // Apply the default styles
        Component.StyleDictionary labelStyles = label.getStyles();
        labelStyles.put("font", FONT);
        labelStyles.put("color", COLOR);

        Component.StyleDictionary borderStyles = border.getStyles();
        borderStyles.put("backgroundColor", BACKGROUND_COLOR);
        borderStyles.put("color", BORDER_COLOR);
        borderStyles.put("padding", PADDING);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Tooltip tooltip = (Tooltip)component;
        tooltip.setContent(border);
        tooltip.getTooltipListeners().add(this);

        label.setText(tooltip.getText());
    }

    @Override
    public void uninstall() {
        Tooltip tooltip = (Tooltip)getComponent();
        tooltip.setContent(null);
        tooltip.getTooltipListeners().remove(this);
    }

    @Override
    public void windowOpened(Window window) {
        // Add this as a display mouse and key listener
        Display display = window.getDisplay();
        display.getComponentMouseListeners().add(closeHandler);
        display.getComponentMouseButtonListeners().add(closeHandler);
        display.getComponentMouseWheelListeners().add(closeHandler);
        display.getComponentKeyListeners().add(closeHandler);
    }

    @Override
    public void windowClosed(Window window, Display display) {
        // Remove this as a display mouse and key listener
        display.getComponentMouseListeners().remove(closeHandler);
        display.getComponentMouseButtonListeners().remove(closeHandler);
        display.getComponentMouseWheelListeners().remove(closeHandler);
        display.getComponentKeyListeners().remove(closeHandler);
    }

    public void textChanged(Tooltip tooltip, String previousText) {
        label.setText(tooltip.getText());
    }
}
