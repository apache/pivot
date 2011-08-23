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
import java.awt.Graphics2D;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Tooltip skin.
 */
public class TerraTooltipSkin extends WindowSkin {
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
        /**
         * Close the Tooltip.
         */
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
    private boolean fade = true;

    private Color borderColor;
    private Insets padding;

    private int closeTransitionDuration = DEFAULT_CLOSE_TRANSITION_DURATION;
    private int closeTransitionRate = DEFAULT_CLOSE_TRANSITION_RATE;

    private static final int DEFAULT_CLOSE_TRANSITION_DURATION = 500;
    private static final int DEFAULT_CLOSE_TRANSITION_RATE = 30;

    public TerraTooltipSkin() {
        // Get theme icons/colors
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        setBackgroundColor(theme.getColor(19));

        borderColor = Color.BLACK;
        padding = new Insets(2);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Tooltip tooltip = (Tooltip)component;

        dropShadowDecorator = new DropShadowDecorator(5, 2, 2);
        tooltip.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Tooltip tooltip = (Tooltip)getComponent();
        Component content = tooltip.getContent();

        if (height != -1) {
            height -= (padding.top + padding.bottom + 2);
        }

        if (content != null) {
            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right + 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Tooltip tooltip = (Tooltip)getComponent();
        Component content = tooltip.getContent();

        if (width != -1) {
            width -= (padding.left + padding.right + 2);
        }

        if (content != null) {
            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom + 2);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Tooltip tooltip = (Tooltip)getComponent();
        Component content = tooltip.getContent();

        if (content != null) {
            Dimensions contentSize = content.getPreferredSize();
            preferredWidth = contentSize.width;
            preferredHeight = contentSize.height;
        }

        preferredWidth += (padding.left + padding.right + 2);
        preferredHeight += (padding.top + padding.bottom + 2);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        Tooltip tooltip = (Tooltip)getComponent();
        Component content = tooltip.getContent();

        if (content != null) {
            int contentWidth = Math.max(getWidth() - (padding.left + padding.right + 2), 0);
            int contentHeight = Math.max(getHeight() - (padding.top + padding.bottom + 2), 0);
            content.setSize(contentWidth, contentHeight);
            content.setLocation(padding.left + 1, padding.top + 1);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setColor(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
    }

    public int getCloseTransitionDuration() {
        return closeTransitionDuration;
    }

    public void setCloseTransitionDuration(int closeTransitionDuration) {
        this.closeTransitionDuration = closeTransitionDuration;
    }

    public int getCloseTransitionRate() {
        return closeTransitionRate;
    }

    public void setCloseTransitionRate(int closeTransitionRate) {
        this.closeTransitionRate = closeTransitionRate;
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
                    closeTransitionDuration, closeTransitionRate,
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
}
