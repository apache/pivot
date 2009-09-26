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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Quadratic;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Sheet skin class.
 */
public class TerraSheetSkin extends WindowSkin implements SheetStateListener {
    public class OpenTransition extends Transition {
        public OpenTransition(boolean reversed) {
            super(TRANSITION_DURATION, TRANSITION_RATE, false, reversed);
        }

        @Override
        public void update() {
            invalidateComponent();
        }
    }

    private Color borderColor;
    private Insets padding;
    private boolean resizable;

    // Derived colors
    private Color bevelColor;

    private OpenTransition openTransition = null;
    private Quadratic easing = new Quadratic();

    private ComponentListener ownerListener = new ComponentListener.Adapter() {
        @Override
        public void locationChanged(Component component, int previousX, int previousY) {
            alignToOwner();
        }

        @Override
        public void sizeChanged(Component component, int previousWidth, int previousHeight) {
            alignToOwner();
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener() {
        @Override
        public boolean mouseMove(Container display, int x, int y) {
            return isMouseOverClientArea(display, x, y);
        }

        @Override
        public boolean mouseDown(Container display, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            if (isMouseOverClientArea(display, x, y)) {
                Sheet sheet = (Sheet)getComponent();
                Window owner = sheet.getOwner();
                owner.moveToFront();
                consumed = true;

                ApplicationContext.beep();
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Container display, Mouse.Button button, int x, int y) {
            return isMouseOverClientArea(display, x, y);
        }

        @Override
        public boolean mouseWheel(Container display, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return isMouseOverClientArea(display, x, y);
        }

        private boolean isMouseOverClientArea(Container display, int x, int y) {
            boolean mouseOverClientArea = false;

            Sheet sheet = (Sheet)getComponent();
            Component descendant = display.getDescendantAt(x, y);

            if (descendant != display) {
                Window window = descendant.getWindow();

                if (sheet.getOwner() == window) {
                    Bounds clientArea = window.getClientArea();

                    Point location = window.mapPointFromAncestor(display, x, y);
                    mouseOverClientArea = clientArea.contains(location);
                }
            }

            return mouseOverClientArea;
        }
    };

    private DropShadowDecorator dropShadowDecorator = null;

    private static final int TRANSITION_DURATION = 300;
    private static final int TRANSITION_RATE = 30;

    public TerraSheetSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        Color backgroundColor = theme.getColor(11);
        backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(),
            backgroundColor.getBlue(), 235);
        setBackgroundColor(backgroundColor);

        borderColor = theme.getColor(7);
        padding = new Insets(8);
        resizable = false;

        // Set the derived colors
        bevelColor = TerraTheme.darken(backgroundColor);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Sheet sheet = (Sheet)component;
        sheet.getSheetStateListeners().add(this);

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        sheet.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom + 2), 0);
            }

            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right + 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right + 2), 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom + 2);
        preferredHeight = getEasedPreferredHeight(preferredHeight);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            Dimensions preferredContentSize = content.getPreferredSize();
            preferredWidth = preferredContentSize.width;
            preferredHeight = preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right + 2);
        preferredHeight += (padding.top + padding.bottom + 2);
        preferredHeight = getEasedPreferredHeight(preferredHeight);

        Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);

        return preferredSize;
    }

    public int getEasedPreferredHeight(int preferredHeight) {
        if (openTransition != null
            && openTransition.isRunning()) {
            float scale;
            if (openTransition.isReversed()) {
                scale = easing.easeIn(openTransition.getElapsedTime(), 0, 1,
                    openTransition.getDuration());
            } else {
                scale = easing.easeOut(openTransition.getElapsedTime(), 0, 1,
                    openTransition.getDuration());
            }

            preferredHeight = (int)(scale * preferredHeight);
        }

        return preferredHeight;
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            content.setLocation(padding.left + 1, padding.top + 1);

            int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
            int contentHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

            content.setSize(contentWidth, contentHeight);
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(borderColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);

        graphics.setPaint(bevelColor);
        GraphicsUtilities.drawLine(graphics, 1, height - 2, width - 2, Orientation.HORIZONTAL);
    }

    @Override
    public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        super.sizeChanged(component, previousWidth, previousHeight);

        alignToOwner();
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Sheet sheet = (Sheet)getComponent();

        if (keyCode == Keyboard.KeyCode.ENTER) {
            sheet.close(true);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
            sheet.close(false);
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        super.setBackgroundColor(backgroundColor);
        bevelColor = TerraTheme.darken(backgroundColor);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    public boolean isResizable() {
        return resizable;
    }

    public void setResizable(boolean resizable) {
        this.resizable = resizable;
        invalidateComponent();
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);

        dropShadowDecorator.setShadowOpacity(DropShadowDecorator.DEFAULT_SHADOW_OPACITY);

        Window owner = window.getOwner();
        owner.getComponentListeners().add(ownerListener);

        openTransition = new OpenTransition(false);
        openTransition.start(new TransitionListener() {
            @Override
            public void transitionCompleted(Transition transition) {
                openTransition = null;
            }
        });

        if (!window.requestFocus()) {
            Component.clearFocus();
        }
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        super.windowClosed(window, display, owner);

        display.getContainerMouseListeners().remove(displayMouseListener);

        owner.getComponentListeners().remove(ownerListener);
    }

    @Override
    public Vote previewSheetClose(final Sheet sheet, final boolean result) {
        // Start a close transition, return false, and close the window
        // when the transition is complete
        Vote vote = Vote.APPROVE;

        // Don't start the transition if the sheet is being closed as a result
        // of the owner closing
        Window owner = sheet.getOwner();
        if (!(owner.isClosing()
            || owner.isClosed())) {
            TransitionListener transitionListener = new TransitionListener() {
                @Override
                public void transitionCompleted(Transition transition) {
                    sheet.close(result);
                    openTransition = null;
                }
            };

            if (openTransition == null) {
                // Start the close transition
                openTransition = new OpenTransition(true);
                openTransition.start(transitionListener);
            } else {
                // Reverse the open transition
                if (!openTransition.isReversed()
                    && openTransition.isRunning()) {
                    openTransition.reverse(transitionListener);
                }
            }

            vote = (openTransition != null
                && openTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
        }

        return vote;
    }

    @Override
    public void sheetCloseVetoed(Sheet sheet, Vote reason) {
        if (reason == Vote.DENY
            && openTransition != null) {
            openTransition.stop();
            openTransition = null;
        }
    }

    @Override
    public void sheetClosed(Sheet sheet) {
        // No-op
    }

    public void alignToOwner() {
        Sheet sheet = (Sheet)getComponent();

        Window owner = sheet.getOwner();
        Bounds clientArea = owner.getClientArea();

        Point location = owner.mapPointToAncestor(owner.getDisplay(), clientArea.x, clientArea.y);
        sheet.setLocation(location.x + (clientArea.width - getWidth()) / 2, location.y);
    }
}
