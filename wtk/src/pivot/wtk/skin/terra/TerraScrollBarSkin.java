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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollBarListener;
import pivot.wtk.ScrollBarValueListener;
import pivot.wtk.Theme;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Scroll bar skin.
 *
 * @author tvolkert
 */
public class TerraScrollBarSkin extends ContainerSkin
    implements ScrollBarListener, ScrollBarValueListener {

    /**
     * The types of scroll that are supported.
     *
     * @author tvolkert
     */
    protected enum IncrementType {
        UNIT,
        BLOCK;
    }

    /**
     * Encapsulates the code needed to perform timer-controlled scrolling. This
     * class is used by <tt>TerraScrollBarSkin</tt> (automatic block increment
     * scrolling) and <tt>ScrollButtonSkin</tt> (automatic unit increment
     * scrolling).
     *
     * @author tvolkert
     */
    protected class AutomaticScroller {
        public int direction;
        public IncrementType incrementType;
        public int stopValue;

        private ApplicationContext.ScheduledCallback scheduledScrollCallback = null;

        /**
         * Starts scrolling this skin's scroll bar, stopping the scroll when
         * the specified value has been reached.
         *
         * @param direction
         * <tt>1</tt> to adjust the scroll bar's value larger; <tt>-1</tt> to
         * adjust it smaller
         *
         * @param incrementType
         * Determines whether we'll use the scroll bar's unit increment or the
         * block increment when scrolling
         *
         * @param stopValue
         * The value which, once reached, will stop the automatic scrolling.
         * Use <tt>-1</tt> to specify no stop value
         *
         * @exception IllegalStateException
         * If automatic scrolling of any scroll bar is already in progress.
         * Only one scroll bar may be automatically scrolled at one time
         */
        public void start(int direction, IncrementType incrementType, int stopValue) {
            if (scheduledScrollCallback != null) {
                throw new IllegalStateException("Already running");
            }

            this.direction = direction;
            this.incrementType = incrementType;
            this.stopValue = stopValue;

            // Wait a timeout period, then begin rapidly scrolling
            scheduledScrollCallback = ApplicationContext.scheduleRecurringCallback(new Runnable() {
                public void run() {
                    scroll();
                }
            }, 400, 30);

            // We initially scroll once to register that we've started
            scroll();
        }

        /**
         * Stops any automatic scrolling in progress.
         */
        public void stop() {
            if (scheduledScrollCallback != null) {
                scheduledScrollCallback.cancel();
                scheduledScrollCallback = null;
            }
        }

        private void scroll() {
            ScrollBar scrollBar = (ScrollBar)TerraScrollBarSkin.this.getComponent();

            int rangeStart = scrollBar.getRangeStart();
            int rangeEnd = scrollBar.getRangeEnd();
            int extent = scrollBar.getExtent();
            int value = scrollBar.getValue();

            int adjustment;

            if (incrementType == IncrementType.UNIT) {
                adjustment = direction * scrollBar.getUnitIncrement();
            } else {
                adjustment = direction * scrollBar.getBlockIncrement();
            }

            if (adjustment < 0) {
                int newValue = Math.max(value + adjustment, rangeStart);
                scrollBar.setValue(newValue);

                if (stopValue != -1
                    && newValue < stopValue) {
                    // We've reached the explicit stop value
                    stop();
                }

                if (newValue == rangeStart) {
                    // We implicit stop at the minimum scroll bar value
                    stop();
                }
            } else {
                int newValue = Math.min(value + adjustment, rangeEnd - extent);
                scrollBar.setValue(newValue);

                if (stopValue != -1
                    && newValue > stopValue) {
                    // We've reached the explicit stop value
                    stop();
                }

                if (newValue == rangeEnd - extent) {
                    // We implicitly stop at the maximum scroll bar value
                    stop();
                }
            }
        }
    }

    /**
     * Scroll bar scroll button component.
     *
     * @author tvolkert
     */
    protected class ScrollButton extends Component {
        private int direction;
        private ScrollButtonImage buttonImage;

        public ScrollButton(int direction, ScrollButtonImage buttonImage) {
            this.direction = direction;
            this.buttonImage = buttonImage;
            setSkin(new ScrollButtonSkin());
        }

        public int getDirection() {
            return direction;
        }

        public ScrollButtonImage getButtonImage() {
            return buttonImage;
        }
    }

    /**
     * Scroll bar scroll button component skin.
     *
     * @author tvolkert
     */
    protected class ScrollButtonSkin extends ComponentSkin {
        private boolean highlighted = false;
        private boolean pressed = false;

        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            return 15;
        }

        public int getPreferredHeight(int width) {
            return 15;
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            // Apply scroll bar styles to the button
            ScrollButton scrollButton = (ScrollButton)getComponent();
            ScrollBar scrollBar = (ScrollBar)TerraScrollBarSkin.this.getComponent();
            Orientation orientation = scrollBar.getOrientation();

            int width = getWidth();
            int height = getHeight();

            Color backgroundColor;
            if (scrollButton.isEnabled()) {
                if (pressed) {
                    backgroundColor = scrollButtonPressedBackgroundColor;
                } else if (highlighted) {
                    backgroundColor = scrollButtonHighlightedBackgroundColor;
                } else {
                    backgroundColor = scrollButtonBackgroundColor;
                }
            } else {
                backgroundColor = scrollButtonDisabledBackgroundColor;
            }

            Color brightBackgroundColor = TerraTheme.brighten(backgroundColor);

            // Paint the background
            Color gradientStartColor = pressed ? backgroundColor : brightBackgroundColor;
            Color gradientEndColor = pressed ? brightBackgroundColor : backgroundColor;

            if (orientation == Orientation.HORIZONTAL) {
                graphics.setPaint(new GradientPaint(0, 1, gradientStartColor,
                    0, height - 2, gradientEndColor));
            } else {
                graphics.setPaint(new GradientPaint(1, 0, gradientStartColor,
                    width - 2, 0, gradientEndColor));
            }

            graphics.fillRect(1, 1, width - 2, height - 2);

            // Paint the border
            graphics.setPaint(borderColor);
            graphics.setStroke(new BasicStroke());
            graphics.drawRect(0, 0, width - 1, height - 1);

            // Determine the button image size
            ScrollButtonImage buttonImage = scrollButton.getButtonImage();
            int buttonImageWidth = buttonImage.getWidth();
            int buttonImageHeight = buttonImage.getHeight();

            // Paint the image
            Graphics2D imageGraphics = (Graphics2D)graphics.create();
            int buttonImageX = (width - buttonImageWidth) / 2;
            int buttonImageY = (height - buttonImageHeight) / 2;
            imageGraphics.translate(buttonImageX, buttonImageY);
            imageGraphics.clipRect(0, 0, buttonImageWidth, buttonImageHeight);
            buttonImage.paint(imageGraphics);
            imageGraphics.dispose();
        }

        @Override
        public void enabledChanged(Component component) {
            super.enabledChanged(component);

            automaticScroller.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public void mouseOver(Component component) {
            super.mouseOver(component);

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            automaticScroller.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                ScrollButton scrollButton = (ScrollButton)getComponent();

                // Start the automatic scroller. It'll be stopped when we
                // mouse up or mouse out
                automaticScroller.start(scrollButton.getDirection(), IncrementType.UNIT, -1);

                pressed = true;
                repaintComponent();

                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                automaticScroller.stop();

                pressed = false;
                repaintComponent();
            }

            return consumed;
        }
    }

    protected abstract class ScrollButtonImage extends Image {
        public int getWidth() {
            ScrollBar scrollBar = (ScrollBar)getComponent();
            Orientation orientation = scrollBar.getOrientation();
            return (orientation == Orientation.HORIZONTAL ? 5 : 7);
        }

        public int getHeight() {
            ScrollBar scrollBar = (ScrollBar)getComponent();
            Orientation orientation = scrollBar.getOrientation();
            return (orientation == Orientation.HORIZONTAL ? 7 : 5);
        }
    }

    protected class ScrollUpImage extends ScrollButtonImage {
        public void paint(Graphics2D graphics) {
            ScrollBar scrollBar = (ScrollBar)getComponent();

            int width = getWidth();
            int height = getHeight();

            graphics.setPaint(scrollButtonImageColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                arrow.moveTo((float)width + 0.5f, 0);
                arrow.lineTo(0, (float)height / 2.0f);
                arrow.lineTo((float)width + 0.5f, height);
            } else {
                arrow.moveTo(0, (float)height + 0.5f);
                arrow.lineTo((float)width / 2.0f, 0);
                arrow.lineTo(width, (float)height + 0.5f);
            }

            arrow.closePath();
            // TODO Use Graphics#fillPolygon() as optimization?
            graphics.fill(arrow);
        }
    }

    protected class ScrollDownImage extends ScrollButtonImage {
        public void paint(Graphics2D graphics) {
            ScrollBar scrollBar = (ScrollBar)getComponent();

            int width = getWidth();
            int height = getHeight();

            graphics.setPaint(scrollButtonImageColor);
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            GeneralPath arrow = new GeneralPath(GeneralPath.WIND_EVEN_ODD);

            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                arrow.moveTo(0, 0);
                arrow.lineTo((float)width + 0.5f, (float)height / 2.0f);
                arrow.lineTo(0, height);
            } else {
                arrow.moveTo(0, 0);
                arrow.lineTo((float)width / 2.0f, (float)height + 0.5f);
                arrow.lineTo(width, 0);
            }

            arrow.closePath();
            // TODO Use Graphics#fillPolygon() as optimization?
            graphics.fill(arrow);
        }
    }

    /**
     * Scroll bar handle component.
     *
     * @author tvolkert
     */
    protected class Handle extends Component {
        public Handle() {
            setSkin(new HandleSkin());
        }
    }

    /**
     * Scroll bar scroll handle component skin.
     *
     * @author tvolkert
     */
    protected class HandleSkin extends ComponentSkin {
        private int dragOffset;
        private boolean highlighted = false;

        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            throw new UnsupportedOperationException();
        }

        public int getPreferredHeight(int width) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Dimensions getPreferredSize() {
            throw new UnsupportedOperationException();
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            ScrollBar scrollBar = (ScrollBar)TerraScrollBarSkin.this.getComponent();
            Orientation orientation = scrollBar.getOrientation();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            Color backgroundColor = highlighted ?
                scrollButtonHighlightedBackgroundColor :
                scrollButtonBackgroundColor;

            Color brightBackgroundColor = TerraTheme.brighten(backgroundColor);
            Color darkBackgroundColor = TerraTheme.darken(backgroundColor);

            if (orientation == Orientation.HORIZONTAL) {
                graphics.setPaint(new GradientPaint(0, 1, brightBackgroundColor,
                    0, height - 2, backgroundColor));
            } else {
                graphics.setPaint(new GradientPaint(1, 0, brightBackgroundColor,
                    width - 2, 0, backgroundColor));
            }

            graphics.fillRect(1, 1, width - 2, height - 2);

            // Paint the border
            graphics.setPaint(borderColor);
            graphics.setStroke(new BasicStroke());
            graphics.drawRect(0, 0, width - 1, height - 1);

            // Paint the hash marks
            if (orientation == Orientation.HORIZONTAL) {
                int middle = width / 2;
                graphics.setPaint(darkBackgroundColor);
                graphics.drawLine(middle - 3, 4, middle - 3, height - 5);
                graphics.drawLine(middle, 4, middle, height - 5);
                graphics.drawLine(middle + 3, 4, middle + 3, height - 5);
                graphics.setPaint(brightBackgroundColor);
                graphics.drawLine(middle - 2, 4, middle - 2, height - 5);
                graphics.drawLine(middle + 1, 4, middle + 1, height - 5);
                graphics.drawLine(middle + 4, 4, middle + 4, height - 5);
            } else {
                int middle = height / 2;
                graphics.setPaint(darkBackgroundColor);
                graphics.drawLine(4, middle - 3, width - 5, middle - 3);
                graphics.drawLine(4, middle, width - 5, middle);
                graphics.drawLine(4, middle + 3, width - 5, middle + 3);
                graphics.setPaint(brightBackgroundColor);
                graphics.drawLine(4, middle - 2, width - 5, middle - 2);
                graphics.drawLine(4, middle + 1, width - 5, middle + 1);
                graphics.drawLine(4, middle + 4, width - 5, middle + 4);
            }
        }

        @Override
        public void mouseOver(Component component) {
            super.mouseOver(component);

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (Mouse.getCapturer() == component) {
                ScrollBar scrollBar = (ScrollBar)TerraScrollBarSkin.this.getComponent();
                Orientation orientation = scrollBar.getOrientation();

                // Calculate the new scroll bar value
                int pixelValue;
                if (orientation == Orientation.HORIZONTAL) {
                    pixelValue = component.getX() - scrollUpButton.getWidth() + x - dragOffset;
                } else {
                    pixelValue = component.getY() - scrollUpButton.getHeight() + y - dragOffset;
                }

                int realValue = (int)((float)pixelValue / getValueScale());

                // Bound the value
                int rangeEnd = scrollBar.getRangeEnd();
                int extent = scrollBar.getExtent();
                realValue = Math.min(Math.max(realValue, 0), rangeEnd - extent);

                // Update the scroll bar
                scrollBar.setValue(realValue);
            }

            return consumed;
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                ScrollBar scrollBar = (ScrollBar)TerraScrollBarSkin.this.getComponent();
                Orientation orientation = scrollBar.getOrientation();

                dragOffset = (orientation == Orientation.HORIZONTAL ? x : y);
                Mouse.capture(component);
                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (button == Mouse.Button.LEFT
                && Mouse.getCapturer() == component) {
                Mouse.release();
            }

            return consumed;
        }
    }

    private static final int DEFAULT_THICKNESS = 15;
    private static final int DEFAULT_LENGTH = 100;

    private AutomaticScroller automaticScroller = new AutomaticScroller();

    private ScrollButton scrollUpButton = new ScrollButton(-1, new ScrollUpImage());
    private ScrollButton scrollDownButton = new ScrollButton(1, new ScrollDownImage());
    private Handle handle = new Handle();

    private int minimumHandleLength;
    private Color borderColor;
    private Color scrollButtonImageColor;
    private Color scrollButtonBackgroundColor;
    private Color scrollButtonDisabledBackgroundColor;
    private Color scrollButtonPressedBackgroundColor;
    private Color scrollButtonHighlightedBackgroundColor;

    public TerraScrollBarSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        minimumHandleLength = 31;
        borderColor = theme.getColor(7);
        scrollButtonImageColor = theme.getColor(1);
        scrollButtonBackgroundColor = theme.getColor(10);
        scrollButtonDisabledBackgroundColor = theme.getColor(10);
        scrollButtonPressedBackgroundColor = theme.getColor(9);
        scrollButtonHighlightedBackgroundColor = theme.getColor(11);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ScrollBar scrollBar = (ScrollBar)component;
        scrollBar.getScrollBarListeners().add(this);
        scrollBar.getScrollBarValueListeners().add(this);

        scrollBar.add(scrollUpButton);
        scrollBar.add(scrollDownButton);
        scrollBar.add(handle);

        setBackgroundColor(9);

        enabledChanged(scrollBar);
    }

    @Override
    public void uninstall() {
        ScrollBar scrollBar = (ScrollBar)getComponent();
        scrollBar.getScrollBarListeners().remove(this);
        scrollBar.getScrollBarValueListeners().remove(this);

        scrollBar.remove(scrollUpButton);
        scrollBar.remove(scrollDownButton);
        scrollBar.remove(handle);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        ScrollBar scrollBar = (ScrollBar)getComponent();

        int preferredWidth = 0;

        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
           preferredWidth = DEFAULT_LENGTH;
        } else {
           preferredWidth = DEFAULT_THICKNESS;
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        ScrollBar scrollBar = (ScrollBar)getComponent();

        int preferredHeight = 0;

        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
            preferredHeight = DEFAULT_THICKNESS;
        } else {
            preferredHeight = DEFAULT_LENGTH;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        ScrollBar scrollBar = (ScrollBar)getComponent();

        int preferredWidth = 0;
        int preferredHeight = 0;

        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
           preferredWidth = DEFAULT_LENGTH;
           preferredHeight = DEFAULT_THICKNESS;
        } else {
           preferredWidth = DEFAULT_THICKNESS;
           preferredHeight = DEFAULT_LENGTH;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        ScrollBar scrollBar = (ScrollBar)getComponent();

        int width = getWidth();
        int height = getHeight();

        int rangeStart = scrollBar.getRangeStart();
        int rangeEnd = scrollBar.getRangeEnd();
        int extent = scrollBar.getExtent();
        int value = scrollBar.getValue();

        int maxLegalRealValue = rangeEnd - extent;
        int numLegalRealValues = maxLegalRealValue - rangeStart + 1;
        float extentPercentage = (float)extent / (float)(rangeEnd - rangeStart);

        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
            scrollUpButton.setSize(scrollUpButton.getPreferredWidth(-1), height);
            scrollUpButton.setLocation(0, 0);

            scrollDownButton.setSize(scrollDownButton.getPreferredWidth(-1), height);
            scrollDownButton.setLocation(width - scrollDownButton.getWidth(), 0);

            if (scrollBar.isEnabled()) {
                // Calculate the handle width first, as it dictates how much
                // room is left to represent the range of legal values. Note
                // that the handle may overlap each scroll button by 1px so
                // that its borders merge into the borders of the scroll buttons
                int availableWidth = width - scrollUpButton.getWidth() -
                    scrollDownButton.getWidth() + 2;
                int handleWidth = Math.max(minimumHandleLength,
                    Math.round(extentPercentage * (float)availableWidth));

                // Calculate the position of the handle by calculating the
                // scale that maps logical value to pixel value
                int numLegalPixelValues = availableWidth - handleWidth + 1;
                float valueScale = (float)numLegalPixelValues / (float)numLegalRealValues;
                int handleX = (int)((float)value * valueScale) +
                    scrollUpButton.getWidth() - 1;

                if (handleWidth > availableWidth) {
                    // If we can't fit the handle, we hide it
                    handle.setVisible(false);
                } else {
                    handle.setVisible(true);

                    handle.setSize(handleWidth, height);
                    handle.setLocation(handleX, 0);
                }
            } else {
                handle.setVisible(false);
            }
        } else {
            scrollUpButton.setSize(width, scrollUpButton.getPreferredHeight(-1));
            scrollUpButton.setLocation(0, 0);

            scrollDownButton.setSize(width, scrollDownButton.getPreferredHeight(-1));
            scrollDownButton.setLocation(0, height - scrollDownButton.getHeight());

            if (scrollBar.isEnabled()) {
                // Calculate the handle height first, as it dictates how much
                // room is left to represent the range of legal values. Note
                // that the handle may overlap each scroll button by 1px so
                // that its borders merge into the borders of the scroll buttons
                int availableHeight = height - scrollUpButton.getHeight() -
                    scrollDownButton.getHeight() + 2;
                int handleHeight = Math.max(minimumHandleLength,
                    Math.round(extentPercentage * (float)availableHeight));

                // Calculate the position of the handle by calculating the
                // scale maps logical value to pixel value
                int numLegalPixelValues = availableHeight - handleHeight + 1;
                float valueScale = (float)numLegalPixelValues / (float)numLegalRealValues;
                int handleY = (int)((float)value * valueScale) +
                    scrollUpButton.getHeight() - 1;

                if (handleHeight > availableHeight) {
                    // If we can't fit the handle, we hide it
                    handle.setVisible(false);
                } else {
                    handle.setVisible(true);

                    handle.setSize(width, handleHeight);
                    handle.setLocation(0, handleY);
                }
            } else {
                handle.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        ScrollBar scrollBar = (ScrollBar)getComponent();

        int width = getWidth();
        int height = getHeight();

        graphics.setStroke(new BasicStroke());
        graphics.setPaint(borderColor);

        // Paint the scroll bar border lines
        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
            int scrollUpButtonWidth = scrollUpButton.getWidth();
            int scrollDownButtonWidth = scrollDownButton.getWidth();

            graphics.drawLine(scrollUpButtonWidth, 0,
                width - scrollDownButtonWidth - 1, 0);
            graphics.drawLine(scrollUpButtonWidth, height - 1,
                width - scrollDownButtonWidth - 1, height - 1);
        } else {
            int scrollUpButtonHeight = scrollUpButton.getHeight();
            int scrollDownButtonHeight = scrollDownButton.getHeight();

            graphics.drawLine(0, scrollUpButtonHeight, 0,
                height - scrollDownButtonHeight - 1);
            graphics.drawLine(width - 1, scrollUpButtonHeight, width - 1,
                height - scrollDownButtonHeight - 1);
        }
    }

    @Override
    public void setBackgroundColor(Color backgroundColor) {
        ScrollBar scrollBar = (ScrollBar)getComponent();

        Color brightBackgroundColor = TerraTheme.brighten(backgroundColor);
        GradientPaint gradientPaint;

        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
            gradientPaint = new GradientPaint(0, 1, backgroundColor,
                0, DEFAULT_THICKNESS - 2, brightBackgroundColor);
        } else {
            gradientPaint = new GradientPaint(1, 0, backgroundColor,
                DEFAULT_THICKNESS - 2, 0, brightBackgroundColor);
        }

        setBackgroundPaint(gradientPaint);
    }

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public int getMinimumHandleLength() {
        return minimumHandleLength;
    }

    public void setMinimumHandleLength(int minimumHandleLength) {
        if (minimumHandleLength != this.minimumHandleLength) {
            this.minimumHandleLength = minimumHandleLength;
            repaintComponent();
        }
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null");
        }

        setBorderColor(decodeColor(borderColor));
    }

    public final void setBorderColor(int borderColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBorderColor(theme.getColor(borderColor));
    }

    public Color getScrollButtonImageColor() {
        return scrollButtonImageColor;
    }

    public void setScrollButtonImageColor(Color scrollButtonImageColor) {
        if (scrollButtonImageColor == null) {
            throw new IllegalArgumentException("scrollButtonImageColor is null");
        }

        this.scrollButtonImageColor = scrollButtonImageColor;
        repaintComponent();
    }

    public final void setScrollButtonImageColor(String scrollButtonImageColor) {
        if (scrollButtonImageColor == null) {
            throw new IllegalArgumentException("scrollButtonImageColor is null");
        }

        setScrollButtonImageColor(decodeColor(scrollButtonImageColor));
    }

    public final void setScrollButtonImageColor(int scrollButtonImageColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setScrollButtonImageColor(theme.getColor(scrollButtonImageColor));
    }

    public Color getScrollButtonBackgroundColor() {
        return scrollButtonBackgroundColor;
    }

    public void setScrollButtonBackgroundColor(Color scrollButtonBackgroundColor) {
        if (scrollButtonBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonBackgroundColor is null");
        }

        this.scrollButtonBackgroundColor = scrollButtonBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonBackgroundColor(String scrollButtonBackgroundColor) {
        if (scrollButtonBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonBackgroundColor is null");
        }

        setScrollButtonBackgroundColor(decodeColor(scrollButtonBackgroundColor));
    }

    public final void setScrollButtonBackgroundColor(int scrollButtonBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setScrollButtonBackgroundColor(theme.getColor(scrollButtonBackgroundColor));
    }

    public Color getScrollButtonDisabledBackgroundColor() {
        return scrollButtonDisabledBackgroundColor;
    }

    public void setScrollButtonDisabledBackgroundColor(Color scrollButtonDisabledBackgroundColor) {
        if (scrollButtonDisabledBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonDisabledBackgroundColor is null");
        }

        this.scrollButtonDisabledBackgroundColor = scrollButtonDisabledBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonDisabledBackgroundColor(String scrollButtonDisabledBackgroundColor) {
        if (scrollButtonDisabledBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonDisabledBackgroundColor is null");
        }

        setScrollButtonDisabledBackgroundColor(decodeColor(scrollButtonDisabledBackgroundColor));
    }

    public final void setScrollButtonDisabledBackgroundColor(int scrollButtonDisabledBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setScrollButtonDisabledBackgroundColor(theme.getColor(scrollButtonDisabledBackgroundColor));
    }

    public Color getScrollButtonPressedBackgroundColor() {
        return scrollButtonPressedBackgroundColor;
    }

    public void setScrollButtonPressedBackgroundColor(Color scrollButtonPressedBackgroundColor) {
        if (scrollButtonPressedBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonPressedBackgroundColor is null");
        }

        this.scrollButtonPressedBackgroundColor = scrollButtonPressedBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonPressedBackgroundColor(String scrollButtonPressedBackgroundColor) {
        if (scrollButtonPressedBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonPressedBackgroundColor is null");
        }

        setScrollButtonPressedBackgroundColor(decodeColor(scrollButtonPressedBackgroundColor));
    }

    public final void setScrollButtonPressedBackgroundColor(int scrollButtonPressedBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setScrollButtonPressedBackgroundColor(theme.getColor(scrollButtonPressedBackgroundColor));
    }

    public Color getScrollButtonHighlightedBackgroundColor() {
        return scrollButtonHighlightedBackgroundColor;
    }

    public void setScrollButtonHighlightedBackgroundColor(Color scrollButtonHighlightedBackgroundColor) {
        if (scrollButtonHighlightedBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonHighlightedBackgroundColor is null");
        }

        this.scrollButtonHighlightedBackgroundColor = scrollButtonHighlightedBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonHighlightedBackgroundColor(String scrollButtonHighlightedBackgroundColor) {
        if (scrollButtonHighlightedBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonHighlightedBackgroundColor is null");
        }

        setScrollButtonHighlightedBackgroundColor(decodeColor(scrollButtonHighlightedBackgroundColor));
    }

    public final void setScrollButtonHighlightedBackgroundColor(int scrollButtonHighlightedBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setScrollButtonHighlightedBackgroundColor(theme.getColor(scrollButtonHighlightedBackgroundColor));
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        automaticScroller.stop();
    }

    /**
     * Initiates automatic block scrolling. This only happens if the handle is
     * visible since whether the user clicked before or after the handle
     * determines the direction of the scrolling.
     *
     * @param component
     * The scroll bar.
     *
     * @param button
     * The button that was pressed.
     *
     * @param x
     * The x-coordinate of the event in the scroll bar's coordinate space.
     *
     * @param y
     * The y-coordinate of the event in the scroll bar's coordinate space.
     */
    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (button == Mouse.Button.LEFT
            && handle.isVisible()) {
            ScrollBar scrollBar = (ScrollBar)getComponent();

            // Begin automatic block scrolling. Calculate the direction of
            // the scroll by checking to see if the user pressed the mouse
            // in the area "before" the handle or "after" it.
            int direction;
            int realStopValue;

            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                direction = x < handle.getX() ? -1 : 1;

                int pixelStopValue = x - scrollUpButton.getWidth() + 1;

                if (direction == 1) {
                    // If we're scrolling down, account for the width of the
                    // handle in our pixel stop value so that we stop as soon
                    // as the *bottom* of the handle reaches our click point
                    pixelStopValue -= handle.getWidth();
                }

                realStopValue = (int)((float)pixelStopValue / getValueScale());
            } else {
                direction = y < handle.getY() ? -1 : 1;

                int pixelStopValue = y - scrollUpButton.getHeight() + 1;

                if (direction == 1) {
                    // If we're scrolling down, account for the height of the
                    // handle in our pixel stop value so that we stop as soon
                    // as the *bottom* of the handle reaches our click point
                    pixelStopValue -= handle.getHeight();
                }

                realStopValue = (int)((float)pixelStopValue / getValueScale());
            }

            // Start the automatic scroller
            automaticScroller.start(direction, IncrementType.BLOCK, realStopValue);
            consumed = true;
        }

        return consumed;
    }

    /**
     * Cancels automatic block scrolling.
     *
     * @param component
     * The scroll bar.
     *
     * @param button
     * The button that was released.
     *
     * @param x
     * The x-coordinate of the event in the scroll bar's coordinate space.
     *
     * @param y
     * The y-coordinate of the event in the scroll bar's coordinate space.
     */
    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        if (button == Mouse.Button.LEFT) {
            automaticScroller.stop();
        }

        return consumed;
    }

    /**
     * Updates the scroll bar's value.
     *
     * @param component
     * The scroll bar.
     *
     * @param scrollType
     * Unit or block scrolling.
     *
     * @param scrollAmount
     * The amount of scrolling.
     *
     * @param wheelRotation
     * <tt>-1</tt> or <tt>1</tt> for backward or forward scrolling,
     * respectively.
     *
     * @param x
     * The x-coordinate of the event in the scroll bar's coordinate space.
     *
     * @param y
     * The y-coordinate of the event in the scroll bar's coordinate space.
     */
    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        ScrollBar scrollBar = (ScrollBar)getComponent();

        int previousValue = scrollBar.getValue();
        int newValue = previousValue + (scrollAmount * wheelRotation *
            scrollBar.getUnitIncrement());

        if (wheelRotation > 0) {
            int maxValue = scrollBar.getRangeEnd() - scrollBar.getExtent();
            newValue = Math.min(newValue, maxValue);

            if (previousValue < maxValue) {
                consumed = true;
            }
        } else {
            newValue = Math.max(newValue, 0);

            if (previousValue > 0) {
                consumed = true;
            }
        }

        scrollBar.setValue(newValue);

        return consumed;
    }

    /**
     * Gets the scale factor that allows us to translate pixel values to scroll
     * bar values and vice versa. This assumes that the range of pixels spans
     * from the last pixel of <tt>scrollUpButton</tt> to the first pixel of
     * <tt>scrollDownButton</tt> and excludes the pixels taken up by
     * <tt>handle</tt>.
     * <p>
     * To map from scroll bar values (<i>real values</i>) to pixel values, you
     * multiply by the value scale. To map from pixel values back to real
     * values, you divide by the value scale.
     *
     * @return
     * <tt>&lt;number of legal pixel values&gt; / &lt;number of legal real values&gt;</tt>
     */
    private float getValueScale() {
        ScrollBar scrollBar = (ScrollBar)getComponent();

        float valueScale;

        int rangeStart = scrollBar.getRangeStart();
        int rangeEnd = scrollBar.getRangeEnd();
        int extent = scrollBar.getExtent();
        int maxLegalRealValue = rangeEnd - extent;

        int numLegalRealValues = maxLegalRealValue - rangeStart + 1;
        int numLegalPixelValues;

        if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
            int availableWidth = getWidth() - scrollUpButton.getWidth() -
                scrollDownButton.getWidth() + 2;
            numLegalPixelValues = availableWidth - handle.getWidth() + 1;
        } else {
            int availableHeight = getHeight() - scrollUpButton.getHeight() -
                scrollDownButton.getHeight() + 2;
            numLegalPixelValues = availableHeight - handle.getHeight() + 1;
        }

        valueScale = (float)numLegalPixelValues / (float)numLegalRealValues;

        return valueScale;
    }

    /**
     * Propagates the scroll bar's enabled state to the scroll buttons.
     *
     * @param component
     * The scroll bar.
     */
    @Override
    public void enabledChanged(Component component) {
        boolean enabled = component.isEnabled();

        scrollUpButton.setEnabled(enabled);
        scrollDownButton.setEnabled(enabled);

        invalidateComponent();
    }

    // ScrollBarListener methods

    public void orientationChanged(ScrollBar scrollBar, Orientation previousOrientation) {
        invalidateComponent();
    }

    public void scopeChanged(ScrollBar scrollBar, int previousRangeStart,
        int previousRangeEnd, int previousExtent) {
        invalidateComponent();
    }

    public void unitIncrementChanged(ScrollBar scrollBar, int previousUnitIncrement) {
        // No-op
    }

    public void blockIncrementChanged(ScrollBar scrollBar, int previousBlockIncrement) {
        // No-op
    }

    // ScrollBarValueListener methods

    public void valueChanged(ScrollBar scrollBar, int previousValue) {
        // Invalidating the component would yield the correct behavior but
        // would be overkill. If all that has changed is the value, we can just
        // update the handle's location and save the work of full invalidation.
        if (handle.isVisible()) {
           int value = scrollBar.getValue();

           if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
              int handleX = (int)((float)value * getValueScale()) +
                 scrollUpButton.getWidth() - 1;

              handle.setLocation(handleX, 0);
           } else {
              int handleY = (int)((float)value * getValueScale()) +
                 scrollUpButton.getHeight() - 1;

              handle.setLocation(0, handleY);
           }
        }
    }
}
