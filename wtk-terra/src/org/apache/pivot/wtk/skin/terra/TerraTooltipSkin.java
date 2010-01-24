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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.TooltipListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Tooltip skin.
 */
public class TerraTooltipSkin extends WindowSkin implements TooltipListener {
    private Label label = new Label();
    private Border border = new Border();

    private boolean fade = true;

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener() {
        @Override
        public boolean mouseMove(Container container, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        @Override
        public boolean mouseUp(Container container, Mouse.Button button, int x, int y) {
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            fade = false;
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }
    };

    private ComponentKeyListener displayKeyListener = new ComponentKeyListener.Adapter() {
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            fade = false;
            Tooltip tooltip = (Tooltip)getComponent();
            tooltip.close();
            return false;
        }
    };

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
        labelStyles.put("wrapText", true);

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
    public void windowOpened(Window window) {
        super.windowOpened(window);

        // Add this as a display mouse and key listener
        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);
        display.getComponentKeyListeners().add(displayKeyListener);
    }

    @Override
    public Vote previewWindowClose(final Window window) {
        Vote vote = Vote.APPROVE;

        if (fade) {
            if (closeTransition == null) {
                closeTransition = new FadeWindowTransition(window,
                    CLOSE_TRANSITION_DURATION, CLOSE_TRANSITION_RATE,
                    dropShadowDecorator);

                closeTransition.start(new TransitionListener() {
                    @Override
                    public void transitionCompleted(Transition transition) {
                        window.close();
                    }
                });

                vote = Vote.DEFER;
            } else {
                vote = (closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
            }
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
    public void windowClosed(Window window, Display display, Window owner) {
        super.windowClosed(window, display, owner);

        // Remove this as a display mouse and key listener
        display.getContainerMouseListeners().remove(displayMouseListener);
        display.getComponentKeyListeners().remove(displayKeyListener);

        closeTransition = null;
    }

    @Override
    public void textChanged(Tooltip tooltip, String previousText) {
        label.setText(tooltip.getText());
    }
}
