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
package pivot.wtk.skin.terra;

import java.awt.Color;

import org.apache.pivot.util.Vote;

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
import pivot.wtk.Theme;
import pivot.wtk.Tooltip;
import pivot.wtk.TooltipListener;
import pivot.wtk.Window;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
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

        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            return false;
        }

        public boolean mouseWheel(Component component, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        // Component key events
        public boolean keyTyped(Component component, char character) {
            return false;
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
    private Transition closeTransition = null;

    private DropShadowDecorator dropShadowDecorator = null;

    private static final int CLOSE_TRANSITION_DURATION = 500;
    private static final int CLOSE_TRANSITION_RATE = 30;

    public TerraTooltipSkin() {
        setBackgroundColor((Color)null);

        // Add the label to the border
        border.setContent(label);

        // Apply the default styles
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        Component.StyleDictionary labelStyles = label.getStyles();
        labelStyles.put("font", theme.getFont());
        labelStyles.put("color", Color.BLACK);

        Component.StyleDictionary borderStyles = border.getStyles();
        borderStyles.put("backgroundColor", new Color(0xff, 0xff, 0xe0, 0xf0));
        borderStyles.put("color", Color.BLACK);
        borderStyles.put("padding", new Insets(2));
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Tooltip tooltip = (Tooltip)component;

        dropShadowDecorator = new DropShadowDecorator(5, 2, 2);
        tooltip.getDecorators().add(dropShadowDecorator);

        tooltip.setContent(border);
        tooltip.getTooltipListeners().add(this);

        label.setText(tooltip.getText());
    }

    @Override
    public void uninstall() {
        Tooltip tooltip = (Tooltip)getComponent();

        tooltip.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        tooltip.setContent(null);
        tooltip.getTooltipListeners().remove(this);
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        // Add this as a display mouse and key listener
        Display display = window.getDisplay();
        display.getComponentMouseListeners().add(closeHandler);
        display.getComponentMouseButtonListeners().add(closeHandler);
        display.getComponentMouseWheelListeners().add(closeHandler);
        display.getComponentKeyListeners().add(closeHandler);
    }

    @Override
    public Vote previewWindowClose(final Window window) {
        Vote vote = Vote.APPROVE;

        if (closeTransition == null) {
            closeTransition = new FadeWindowTransition(window,
                CLOSE_TRANSITION_DURATION, CLOSE_TRANSITION_RATE,
                dropShadowDecorator);

            closeTransition.start(new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    window.close();
                }
            });

            vote = Vote.DEFER;
        } else {
            vote = (closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
        }

        return vote;
    }

    @Override
    public void windowCloseVetoed(Window window, Vote reason) {
        super.windowCloseVetoed(window, reason);

        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();
        }
    }

    @Override
    public void windowClosed(Window window, Display display) {
        super.windowClosed(window, display);

        // Remove this as a display mouse and key listener
        display.getComponentMouseListeners().remove(closeHandler);
        display.getComponentMouseButtonListeners().remove(closeHandler);
        display.getComponentMouseWheelListeners().remove(closeHandler);
        display.getComponentKeyListeners().remove(closeHandler);

        closeTransition = null;
    }

    public void textChanged(Tooltip tooltip, String previousText) {
        label.setText(tooltip.getText());
    }
}
