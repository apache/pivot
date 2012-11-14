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
import java.awt.Toolkit;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetStateListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Mouse.Button;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.TranslationDecorator;
import org.apache.pivot.wtk.effects.easing.Quadratic;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Sheet skin class.
 */
public class TerraSheetSkin extends WindowSkin implements SheetStateListener {

    public enum SheetPlacement {
        NORTH, EAST, SOUTH, WEST
    }

    public class OpenTransition extends Transition {
        private int dx = 0;
        private int dy = 0;

        public OpenTransition(boolean reversed) {
            super(stateTransitionDuration, stateTransitionRate, false, reversed);
        }

        @Override
        public void start(TransitionListener transitionListener) {
            Sheet sheet = (Sheet)getComponent();
            sheet.getDecorators().add(translationDecorator);

            dx = 0;
            dy = 0;

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            Sheet sheet = (Sheet)getComponent();
            sheet.getDecorators().remove(translationDecorator);

            super.stop();
        }

        @Override
        public void update() {
            Sheet sheet = (Sheet)getComponent();

            float scale;
            if (isReversed()) {
                scale = easing.easeIn(getElapsedTime(), 1, -1, getDuration());
            } else {
                scale = easing.easeOut(getElapsedTime(), 1, -1, getDuration());
            }

            Display display = sheet.getDisplay();
            if (display != null) {
                Bounds decoratedBounds = sheet.getDecoratedBounds();
                display.repaint(decoratedBounds.x, decoratedBounds.y,
                    decoratedBounds.width + dx, decoratedBounds.height + dy);

                Dimensions size = sheet.getPreferredSize();
                switch (slideSource) {
                    case NORTH:
                        dy = -(int)(size.height * scale);
                        break;
                    case EAST:
                        dx = (int)(size.width * scale);
                        break;
                    case SOUTH:
                        dy = (int)(size.height * scale);
                        break;
                    case WEST:
                        dx = -(int)(size.width * scale);
                        break;
                    default:
                        throw new IllegalStateException(
                            "slideSource is null or an unexpected value");
                }

                translationDecorator.setX(dx);
                translationDecorator.setY(dy);

                display.repaint(decoratedBounds.x, decoratedBounds.y,
                    decoratedBounds.width + dx, decoratedBounds.height + dy);
            }
        }
    }

    /**
     * Resize button image.
     */
    protected class ResizeImage extends Image {
        public static final int ALPHA = 64;

        @Override
        public int getWidth() {
            return 5;
        }

        @Override
        public int getHeight() {
            return 5;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(new Color(0, 0, 0, ALPHA));
            graphics.fillRect(3, 0, 2, 1);
            graphics.fillRect(0, 3, 2, 1);
            graphics.fillRect(3, 3, 2, 1);

            graphics.setPaint(new Color(borderColor.getRed(),
                borderColor.getGreen(), borderColor.getBlue(),
                ALPHA));
            graphics.fillRect(3, 1, 2, 1);
            graphics.fillRect(0, 4, 2, 1);
            graphics.fillRect(3, 4, 2, 1);
        }
    }

    private Image resizeImage = new ResizeImage();
    private ImageView resizeHandle = new ImageView(resizeImage);
    private Point resizeOffset = null;
    private Color borderColor;
    private Insets padding;
    private boolean resizable;
    private SheetPlacement slideSource = SheetPlacement.NORTH;

    private int stateTransitionDuration = DEFAULT_STATE_TRANSITION_DURATION;
    private int stateTransitionRate = DEFAULT_STATE_TRANSITION_RATE;

    private Color bevelColor;

    private OpenTransition openTransition = null;
    private Quadratic easing = new Quadratic();
    private TranslationDecorator translationDecorator = new TranslationDecorator(true);

    private boolean closingResult;
    private boolean doingFinalClose = false;

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
            return isMouseOverOwnerClientArea(display, x, y);
        }

        @Override
        public boolean mouseDown(Container display, Mouse.Button button, int x, int y) {
            boolean consumed = false;

            Sheet sheet = (Sheet)getComponent();
            if (isMouseOverOwnerClientArea(display, x, y)) {
                Window rootOwner = sheet.getRootOwner();
                rootOwner.moveToFront();
                consumed = true;

                Toolkit.getDefaultToolkit().beep();
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Container display, Mouse.Button button, int x, int y) {
            return isMouseOverOwnerClientArea(display, x, y);
        }

        @Override
        public boolean mouseWheel(Container display, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return isMouseOverOwnerClientArea(display, x, y);
        }

        private boolean isMouseOverOwnerClientArea(Container display, int x, int y) {
            boolean mouseOverOwnerClientArea = false;

            Sheet sheet = (Sheet)getComponent();
            Component descendant = display.getDescendantAt(x, y);

            if (descendant != display) {
                Window window = descendant.getWindow();

                if (sheet.getOwner() == window) {
                    Bounds clientArea = window.getClientArea();

                    Point location = window.mapPointFromAncestor(display, x, y);
                    mouseOverOwnerClientArea = clientArea.contains(location);
                }
            }

            return mouseOverOwnerClientArea;
        }
    };

    private DropShadowDecorator dropShadowDecorator = null;

    private static final int DEFAULT_STATE_TRANSITION_DURATION = 300;
    private static final int DEFAULT_STATE_TRANSITION_RATE = 30;

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

        sheet.add(resizeHandle);
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

        Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);

        return preferredSize;
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Sheet sheet = (Sheet)getComponent();

        // Size/position resize handle
        resizeHandle.setSize(resizeHandle.getPreferredSize());
        resizeHandle.setLocation(width - resizeHandle.getWidth() - 2,
            height - resizeHandle.getHeight() - 2);
        resizeHandle.setVisible(resizable
            && !sheet.isMaximized()
            && (sheet.isPreferredWidthSet()
                || sheet.isPreferredHeightSet()));

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
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (Mouse.getCapturer() == component) {
            Sheet sheet = (Sheet)getComponent();
            Display display = sheet.getDisplay();

            Point location = sheet.mapPointToAncestor(display, x, y);

            // Pretend that the mouse can't move off screen (off the display)
            location = new Point(Math.min(Math.max(location.x, 0), display.getWidth() - 1),
                Math.min(Math.max(location.y, 0), display.getHeight() - 1));

            if (resizeOffset != null) {
                // Resize the frame
                int preferredWidth = -1;
                int preferredHeight = -1;
                boolean preferredWidthSet = component.isPreferredWidthSet();
                boolean preferredHeightSet = component.isPreferredHeightSet();
                boolean noPreferredSet = !(preferredWidthSet || preferredHeightSet);

                if (preferredWidthSet || noPreferredSet) {
                    preferredWidth = Math.max(location.x - sheet.getX() + resizeOffset.x, 2);
                    preferredWidth = Math.min(preferredWidth, sheet.getMaximumWidth());
                    preferredWidth = Math.max(preferredWidth, sheet.getMinimumWidth());
                }

                if (preferredHeightSet || noPreferredSet) {
                    preferredHeight = Math.max(location.y - sheet.getY() + resizeOffset.y,
                        resizeHandle.getHeight() + 7);
                    preferredHeight = Math.min(preferredHeight, sheet.getMaximumHeight());
                    preferredHeight = Math.max(preferredHeight, sheet.getMinimumHeight());
                }

                sheet.setPreferredSize(preferredWidth, preferredHeight);
            }
        } else {
            Cursor cursor = null;
            Bounds resizeHandleBounds = resizeHandle.getBounds();

            if (resizable && resizeHandleBounds.contains(x, y)) {
                boolean preferredWidthSet = component.isPreferredWidthSet();
                boolean preferredHeightSet = component.isPreferredHeightSet();

                if (preferredWidthSet
                    && preferredHeightSet) {
                    cursor = Cursor.RESIZE_SOUTH_EAST;
                } else if (preferredWidthSet) {
                    cursor = Cursor.RESIZE_EAST;
                } else if (preferredHeightSet) {
                    cursor = Cursor.RESIZE_SOUTH;
                } else {
                    cursor = Cursor.RESIZE_SOUTH_EAST;
                }
            }

            component.setCursor(cursor);
        }

        return consumed;
    }

    @Override
    public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
        Sheet sheet = (Sheet)container;
        if (!sheet.isTopMost()) {
            Window owner = sheet.getOwner();
            owner.moveToFront();
        }

        boolean consumed = super.mouseDown(container, button, x, y);

        if (resizable && button == Mouse.Button.LEFT) {
            Bounds resizeHandleBounds = resizeHandle.getBounds();

            if (resizeHandleBounds.contains(x, y)) {
                resizeOffset = new Point(getWidth() - x, getHeight() - y);
                Mouse.capture(container);
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (Mouse.getCapturer() == component) {
            resizeOffset = null;
            Mouse.release();
        }

        return consumed;
    }

    /**
     * {@link KeyCode#ENTER ENTER} Close the sheet with a 'result' of true.<br>
     * {@link KeyCode#ESCAPE ESCAPE} Close the sheet with a 'result' of false.
     */
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

    public SheetPlacement getSlideSource() {
        return slideSource;
    }

    public void setSlideSource(SheetPlacement slideSource) {
        if (slideSource == null) {
            throw new IllegalArgumentException("slideSource is null.");
        }
        this.slideSource = slideSource;
    }

    public int getStateTransitionDuration() {
        return stateTransitionDuration;
    }

    public void setStateTransitionDuration(int stateTransitionDuration) {
        this.stateTransitionDuration = stateTransitionDuration;
    }

    public int getStateTransitionRate() {
        return stateTransitionRate;
    }

    public void setStateTransitionRate(int stateTransitionRate) {
        this.stateTransitionRate = stateTransitionRate;
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);
        display.reenterMouse();

        dropShadowDecorator.setShadowOpacity(DropShadowDecorator.DEFAULT_SHADOW_OPACITY);

        alignToOwner();

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
    public Vote previewSheetClose(Sheet sheet, boolean result) {
        Vote vote = Vote.APPROVE;

        // Don't start the transition if the sheet is being closed as a result
        // of the owner closing
        Window owner = sheet.getOwner();
        if (!(owner.isClosing()
            || owner.isClosed()
            || doingFinalClose)) {
            if (openTransition == null) {
                // Setup for the close transition
                // Don't start it until we know that everyone
                // else is okay with it
                openTransition = new OpenTransition(true);
                closingResult = result;
            } else {
                // Reverse the open transition
                if (openTransition.isRunning()) {
                    openTransition.reverse();
                }
            }

            vote = (openTransition != null) ? Vote.DEFER : Vote.APPROVE;
        }

        return vote;
    }

    @Override
    public void sheetCloseVetoed(final Sheet sheet, Vote reason) {
        if (reason == Vote.DENY
            && openTransition != null) {
            openTransition.stop();
            openTransition = null;
        } else
        if (reason == Vote.DEFER
            && openTransition != null
            && !openTransition.isRunning()) {
            openTransition.start(new TransitionListener() {
                @Override
                public void transitionCompleted(Transition transition) {
                    openTransition = null;
                    doingFinalClose = true;
                    sheet.close(closingResult);
                    doingFinalClose = false;
                }
            });
        }
    }

    @Override
    public void sheetClosed(Sheet sheet) {
        // No-op
    }

    public void alignToOwner() {
        Sheet sheet = (Sheet)getComponent();

        Window owner = sheet.getOwner();
        if (owner != null) {
            Bounds clientArea = owner.getClientArea();

            Point location = owner.mapPointToAncestor(owner.getDisplay(), clientArea.x,
                clientArea.y);
            int x = location.x;
            int y = location.y;

            switch (slideSource) {
                case NORTH:
                    x = location.x + (clientArea.width - getWidth()) / 2;
                    y = location.y;
                    break;
                case SOUTH:
                    x = location.x + (clientArea.width - getWidth()) / 2;
                    y = location.y + (clientArea.height - getHeight());
                    break;
                case WEST:
                    x = location.x;
                    y = location.y + (clientArea.height - getHeight()) / 2;
                    break;
                case EAST:
                    x = location.x + (clientArea.width - getWidth());
                    y = location.y + (clientArea.height - getHeight()) / 2;
                    break;
                default:
                    throw new IllegalStateException("slideSource is null or an unexpected value");
            }

            sheet.setLocation(x, y);
        }
    }
}
