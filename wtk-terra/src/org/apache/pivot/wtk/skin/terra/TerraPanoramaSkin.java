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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseListener;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Viewport;
import org.apache.pivot.wtk.ViewportListener;
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.ButtonSkin;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Panorama skin.
 */
public class TerraPanoramaSkin extends ContainerSkin implements Viewport.Skin, ViewportListener {
    /**
     * Abstract base class for button images.
     */
    protected abstract class ScrollButtonImage extends Image {
        @Override
        public int getWidth() {
            return BUTTON_SIZE;
        }

        @Override
        public int getHeight() {
            return BUTTON_SIZE;
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.setStroke(new BasicStroke(0));
            graphics.setPaint(buttonColor);
        }
    }

    /**
     * North button image.
     */
    protected class NorthButtonImage extends ScrollButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {0, 3, 6};
            int[] yPoints = {5, 1, 5};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * South button image.
     */
    protected class SouthButtonImage extends ScrollButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {0, 3, 6};
            int[] yPoints = {1, 5, 1};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * East button image.
     */
    protected class EastButtonImage extends ScrollButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {1, 5, 1};
            int[] yPoints = {0, 3, 6};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * West button image.
     */
    protected class WestButtonImage extends ScrollButtonImage {
        @Override
        public void paint(Graphics2D graphics) {
            super.paint(graphics);

            int[] xPoints = {5, 1, 5};
            int[] yPoints = {0, 3, 6};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    protected class ScrollButton extends Button {
        public ScrollButton(Object buttonData) {
            super(buttonData);

            setDataRenderer(DEFAULT_DATA_RENDERER);
            setSkin(new ScrollButtonSkin());
        }

        @Override
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException("Link buttons cannot be toggle buttons.");
        }
    }

    public class ScrollButtonSkin extends ButtonSkin {
        @Override
        public int getPreferredWidth(int height) {
            return BUTTON_SIZE + buttonPadding;
        }

        @Override
        public int getPreferredHeight(int width) {
            return BUTTON_SIZE + buttonPadding;
        }

        @Override
        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        @Override
        public void paint(Graphics2D graphics) {
            ScrollButton scrollButton = (ScrollButton)getComponent();
            int width = getWidth();
            int height = getHeight();

            if (buttonBackgroundColor != null) {
                graphics.setColor(buttonBackgroundColor);
                graphics.fillRect(0, 0, width, height);
            }

            Button.DataRenderer dataRenderer = scrollButton.getDataRenderer();
            dataRenderer.render(scrollButton.getButtonData(), scrollButton, false);
            dataRenderer.setSize(width - buttonPadding * 2, height - buttonPadding * 2);

            graphics.translate(buttonPadding, buttonPadding);
            dataRenderer.paint(graphics);
        }

        /**
         * @return
         * <tt>false</tt>; link buttons are not focusable.
         */
        @Override
        public boolean isFocusable() {
            return false;
        }
    }

    private class ScrollCallback implements Runnable {
        @Override
        public void run() {
            Panorama panorama = (Panorama)getComponent();

            if (northButton.isMouseOver()) {
                int scrollTop = Math.max(panorama.getScrollTop()
                    - (int)scrollDistance, 0);
                if (scrollTop == 0
                    && scheduledScrollCallback != null) {
                    scheduledScrollCallback.cancel();
                    scheduledScrollCallback = null;
                }

                panorama.setScrollTop(scrollTop);
            } else if (southButton.isMouseOver()) {
                int maxScrollTop = getMaxScrollTop();
                int scrollTop = Math.min(panorama.getScrollTop()
                    + (int)scrollDistance, maxScrollTop);
                if (scrollTop == maxScrollTop
                    && scheduledScrollCallback != null) {
                    scheduledScrollCallback.cancel();
                    scheduledScrollCallback = null;
                }

                panorama.setScrollTop(scrollTop);
            } else if (eastButton.isMouseOver()) {
                int maxScrollLeft = getMaxScrollLeft();
                int scrollLeft = Math.min(panorama.getScrollLeft()
                    + (int)scrollDistance, maxScrollLeft);
                if (scrollLeft == maxScrollLeft
                    && scheduledScrollCallback != null) {
                    scheduledScrollCallback.cancel();
                    scheduledScrollCallback = null;
                }

                panorama.setScrollLeft(scrollLeft);
            } else if (westButton.isMouseOver()) {
                int scrollLeft = Math.max(panorama.getScrollLeft()
                    - (int)scrollDistance, 0);
                if (scrollLeft == 0
                    && scheduledScrollCallback != null) {
                    scheduledScrollCallback.cancel();
                    scheduledScrollCallback = null;
                }

                panorama.setScrollLeft(scrollLeft);
            }

            scrollDistance = Math.min(scrollDistance * SCROLL_ACCELERATION,
                MAXIMUM_SCROLL_DISTANCE);
        }
    }

    private Color buttonColor;
    private Color buttonBackgroundColor;
    private int buttonPadding;
    private boolean alwaysShowScrollButtons = false;

    private ScrollButton northButton = new ScrollButton(new NorthButtonImage());
    private ScrollButton southButton = new ScrollButton(new SouthButtonImage());
    private ScrollButton eastButton = new ScrollButton(new EastButtonImage());
    private ScrollButton westButton = new ScrollButton(new WestButtonImage());

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    public TerraPanoramaSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        buttonColor = theme.getColor(1);
        buttonBackgroundColor = null;
        buttonPadding = 4;
    }

    private ComponentMouseListener buttonMouseListener = new ComponentMouseListener.Adapter() {
        @Override
        public void mouseOver(Component component) {
            // Start scroll timer
            scrollDistance = INITIAL_SCROLL_DISTANCE;
            scheduledScrollCallback =
                ApplicationContext.scheduleRecurringCallback(scrollCallback, SCROLL_RATE);
        }

        @Override
        public void mouseOut(Component component) {
            // Stop scroll timer
            if (scheduledScrollCallback != null) {
                scheduledScrollCallback.cancel();
                scheduledScrollCallback = null;
            }
        }
    };

    private float scrollDistance = 0;
    private ScrollCallback scrollCallback = new ScrollCallback();
    private ApplicationContext.ScheduledCallback scheduledScrollCallback = null;

    private static final int SCROLL_RATE = 50;
    private static final float INITIAL_SCROLL_DISTANCE = 10;
    private static final float SCROLL_ACCELERATION = 1.06f;
    private static final float MAXIMUM_SCROLL_DISTANCE = 150f;

    private static final int BUTTON_SIZE = 7;

    @Override
    public void install(Component component) {
        super.install(component);

        Panorama panorama = (Panorama)component;
        panorama.getViewportListeners().add(this);

        // Add scroll arrow link buttons and attach mouse listeners
        // to them; the mouse handlers should call setScrollTop() and
        // setScrollLeft() on the panorama as appropriate
        panorama.add(northButton);
        northButton.getComponentMouseListeners().add(buttonMouseListener);

        panorama.add(southButton);
        southButton.getComponentMouseListeners().add(buttonMouseListener);

        panorama.add(eastButton);
        eastButton.getComponentMouseListeners().add(buttonMouseListener);

        panorama.add(westButton);
        westButton.getComponentMouseListeners().add(buttonMouseListener);

        updateScrollButtonVisibility();
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        // The panorama's preferred width is the preferred width of the view
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view != null) {
            preferredWidth = view.getPreferredWidth(height);
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        // The panorama's preferred height is the preferred height of the view
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view != null) {
            preferredHeight = view.getPreferredHeight(width);
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        Dimensions preferredSize = null;

        // The panorama's preferred size is the preferred size of the view
        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();
        if (view == null) {
            preferredSize = new Dimensions(0, 0);
        } else {
            preferredSize = view.getPreferredSize();
        }

        return preferredSize;
    }

    @Override
    public void layout() {
        Panorama panorama = (Panorama)getComponent();
        int width = getWidth();
        int height = getHeight();

        Component view = panorama.getView();
        if (view != null) {
            Dimensions viewSize = view.getPreferredSize();
            view.setSize(Math.max(width, viewSize.width), Math.max(height, viewSize.height));
            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();

            int maxScrollTop = getMaxScrollTop();
            if (panorama.getScrollTop() > maxScrollTop) {
                panorama.setScrollTop(maxScrollTop);
            }

            int maxScrollLeft = getMaxScrollLeft();
            if (panorama.getScrollLeft() > maxScrollLeft) {
                panorama.setScrollLeft(maxScrollLeft);
            }

            if (width < viewWidth) {
                // Show east/west buttons
                eastButton.setSize(eastButton.getPreferredWidth(), height);
                eastButton.setLocation(width - eastButton.getWidth(), 0);

                westButton.setSize(westButton.getPreferredWidth(), height);
                westButton.setLocation(0, 0);
            }

            if (height < viewHeight) {
                // Show north/south buttons
                northButton.setSize(width, northButton.getPreferredHeight());
                northButton.setLocation(0, 0);

                southButton.setSize(width, southButton.getPreferredHeight());
                southButton.setLocation(0, height - southButton.getHeight());
            }
        }

        updateScrollButtonVisibility();
    }

    @Override
    public Bounds getViewportBounds() {
        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();

        if (buttonBackgroundColor != null) {
            if (northButton.isVisible()) {
                int northButtonHeight = northButton.getHeight();
                y += northButtonHeight;
                height -= northButtonHeight;
            }

            if (southButton.isVisible()) {
                height -= southButton.getHeight();
            }

            if (eastButton.isVisible()) {
                width -= eastButton.getWidth();
            }

            if (westButton.isVisible()) {
                int westButtonWidth = westButton.getWidth();
                x += westButtonWidth;
                width -= westButtonWidth;
            }
        }

        return new Bounds(x, y, width, height);
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        Panorama panorama = (Panorama)getComponent();
        Component view = panorama.getView();

        if (view != null) {
            // The scroll orientation is tied to whether the shift key was
            // presssed while the mouse wheel was scrolled
            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Treat the mouse wheel as a horizontal scroll event
                int previousScrollLeft = panorama.getScrollLeft();
                int newScrollLeft = previousScrollLeft + (scrollAmount * wheelRotation *
                    (int)INITIAL_SCROLL_DISTANCE);

                if (wheelRotation > 0) {
                    int maxScrollLeft = getMaxScrollLeft();
                    newScrollLeft = Math.min(newScrollLeft, maxScrollLeft);

                    if (previousScrollLeft < maxScrollLeft) {
                        consumed = true;
                    }
                } else {
                    newScrollLeft = Math.max(newScrollLeft, 0);

                    if (previousScrollLeft > 0) {
                        consumed = true;
                    }
                }

                panorama.setScrollLeft(newScrollLeft);
            } else {
                // Treat the mouse wheel as a vertical scroll event
                int previousScrollTop = panorama.getScrollTop();
                int newScrollTop = previousScrollTop + (scrollAmount * wheelRotation *
                    (int)INITIAL_SCROLL_DISTANCE);

                if (wheelRotation > 0) {
                    int maxScrollTop = getMaxScrollTop();
                    newScrollTop = Math.min(newScrollTop, maxScrollTop);

                    if (previousScrollTop < maxScrollTop) {
                        consumed = true;
                    }
                } else {
                    newScrollTop = Math.max(newScrollTop, 0);

                    if (previousScrollTop > 0) {
                        consumed = true;
                    }
                }

                panorama.setScrollTop(newScrollTop);
            }
        }

        return consumed;
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public void setButtonColor(Color buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        this.buttonColor = buttonColor;
        repaintComponent();
    }

    public final void setButtonColor(String buttonColor) {
        if (buttonColor == null) {
            throw new IllegalArgumentException("buttonColor is null.");
        }

        setButtonColor(GraphicsUtilities.decodeColor(buttonColor));
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(Color buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null.");
        }

        this.buttonBackgroundColor = buttonBackgroundColor;
        repaintComponent();
    }

    public final void setButtonBackgroundColor(String buttonBackgroundColor) {
        if (buttonBackgroundColor == null) {
            throw new IllegalArgumentException("buttonBackgroundColor is null.");
        }

        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColor));
    }

    public final void setButtonBackgroundColor(int buttonBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setButtonBackgroundColor(theme.getColor(buttonBackgroundColor));
    }

    public int getButtonPadding() {
        return buttonPadding;
    }

    public void setButtonPadding(int buttonPadding) {
        if (buttonPadding < 0) {
            throw new IllegalArgumentException("buttonPadding is negative.");
        }

        this.buttonPadding = buttonPadding;
        invalidateComponent();
    }

    public final void setButtonPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("buttonPadding is null.");
        }

        setButtonPadding(padding.intValue());
    }

    public boolean getAlwaysShowScrollButtons() {
        return alwaysShowScrollButtons;
    }

    public void setAlwaysShowScrollButtons(boolean alwaysShowScrollButtons) {
        this.alwaysShowScrollButtons = alwaysShowScrollButtons;
        updateScrollButtonVisibility();
    }

    protected int getMaxScrollTop() {
        int maxScrollTop = 0;

        Panorama panorama = (Panorama)getComponent();
        int height = getHeight();

        Component view = panorama.getView();
        if (view != null) {
            maxScrollTop = Math.max(view.getHeight() - height, 0);
        }

        return maxScrollTop;
    }

    protected int getMaxScrollLeft() {
        int maxScrollLeft = 0;

        Panorama panorama = (Panorama)getComponent();
        int width = getWidth();

        Component view = panorama.getView();
        if (view != null) {
            maxScrollLeft = Math.max(view.getWidth() - width, 0);
        }

        return maxScrollLeft;
    }

    protected void updateScrollButtonVisibility() {
        Panorama panorama = (Panorama)getComponent();
        boolean mouseOver = panorama.isMouseOver();

        int scrollTop = panorama.getScrollTop();
        int maxScrollTop = getMaxScrollTop();
        northButton.setVisible((alwaysShowScrollButtons
            || mouseOver) && scrollTop > 0);
        southButton.setVisible((alwaysShowScrollButtons
            || mouseOver) && scrollTop < maxScrollTop);

        int scrollLeft = panorama.getScrollLeft();
        int maxScrollLeft = getMaxScrollLeft();
        westButton.setVisible((alwaysShowScrollButtons
            || mouseOver) && scrollLeft > 0);
        eastButton.setVisible((alwaysShowScrollButtons
            || mouseOver) && scrollLeft < maxScrollLeft);
    }

    // User input
    @Override
    public void mouseOver(Component component) {
        super.mouseOver(component);
        updateScrollButtonVisibility();
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);
        updateScrollButtonVisibility();
    }

    // Viewport events
    @Override
    public void scrollTopChanged(Viewport panorama, int previousScrollTop) {
        Component view = panorama.getView();
        if (view != null) {
            int maxScrollTop = getMaxScrollTop();
            int scrollTop = Math.min(panorama.getScrollTop(), maxScrollTop);
            view.setLocation(view.getX(), -scrollTop);
            updateScrollButtonVisibility();
        }
    }

    @Override
    public void scrollLeftChanged(Viewport panorama, int previousScrollLeft) {
        Component view = panorama.getView();
        if (view != null) {
            int maxScrollLeft = getMaxScrollLeft();
            int scrollLeft = Math.min(panorama.getScrollLeft(), maxScrollLeft);
            view.setLocation(-scrollLeft, view.getY());
            updateScrollButtonVisibility();
        }
    }

    @Override
    public void viewChanged(Viewport panorama, Component previousView) {
        invalidateComponent();
    }
}
