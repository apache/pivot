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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.Point;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollBarListener;
import pivot.wtk.ScrollBarValueListener;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * <p>Scroll bar skin.</p>
 *
 * @author tvolkert
 */
public class ScrollBarSkin extends ContainerSkin
    implements ScrollBarListener, ScrollBarValueListener {

    /**
     * Encapsulates the code needed to perform timer-controlled scrolling. This
     * class is used by <tt>ScrollPaneSkin</tt> (automatic block increment
     * scrolling) and <tt>ScrollButtonSkin</tt> (automatic unit increment
     * scrolling). <tt>ScrollPaneSkin</tt> keeps a static instance of this
     * class to be used by all scroll bars, which is ok since only one scroll
     * bar is ever being automatically scrolled at any given time.
     */
    private static class AutomaticScroller {
        public enum IncrementType {
            UNIT,
            BLOCK
        }

        public ScrollBar scrollBar;
        public int direction;
        public IncrementType incrementType;
        public int stopValue;

        private int timeoutID = -1;
        private int intervalID = -1;

        /**
         * Starts scrolling the specified scroll bar with no stop value.
         *
         * @param scrollBar
         * The scroll bar to scroll
         *
         * @param direction
         * <tt>1</tt> to adjust the scroll bar's value larger; <tt>-1</tt> to
         * adjust it smaller
         *
         * @param incrementType
         * Determines whether we'll use the scroll bar's unit increment or the
         * block increment when scrolling
         *
         * @exception IllegalStateException
         * If automatic scrolling of any scroll bar is already in progress.
         * Only one scroll bar may be automatically scrolled at one time
         */
        public void start(ScrollBar scrollBar, int direction,
            IncrementType incrementType) {
            start(scrollBar, direction, incrementType, -1);
        }

        /**
         * Starts scrolling the specified scroll bar, stopping the scroll when
         * the specified value has been reached.
         *
         * @param scrollBar
         * The scroll bar to scroll
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
        public void start(ScrollBar scrollBar, int direction,
            IncrementType incrementType, int stopValue) {
            if (timeoutID != -1
                || intervalID != -1) {
                throw new IllegalStateException("Already running");
            }

            this.scrollBar = scrollBar;
            this.direction = direction;
            this.incrementType = incrementType;
            this.stopValue = stopValue;

            // Wait a timeout period, then begin repidly scrolling
            timeoutID = ApplicationContext.setTimeout(new Runnable() {
                public void run() {
                    intervalID = ApplicationContext.setInterval(new Runnable() {
                        public void run() {
                            scroll();
                        }
                    }, 30);

                    timeoutID = -1;
                }
            }, 400);

            // We initially scroll once to register that we've started
            scroll();
        }

        private void scroll() {
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

        /**
         * Stops any automatic scrolling in progress.
         */
        public void stop() {
            if (timeoutID != -1) {
                ApplicationContext.clearTimeout(timeoutID);
                timeoutID = -1;
            }

            if (intervalID != -1) {
                ApplicationContext.clearInterval(intervalID);
                intervalID = -1;
            }
        }
    }

    public static class ScrollButton extends Component {
        private ScrollBar scrollBar;
        private int direction;
        private Image buttonImage;

        public ScrollButton(ScrollBar scrollBar, int direction, Image buttonImage) {
            this.scrollBar = scrollBar;
            this.direction = direction;
            this.buttonImage = buttonImage;

            installSkin(ScrollButton.class);
        }

        public ScrollBar getScrollBar() {
            return scrollBar;
        }

        public int getDirection() {
            return direction;
        }

        public Image getButtonImage() {
            return buttonImage;
        }
    }

    public static class ScrollButtonSkin extends ComponentSkin {
        private boolean highlighted = false;
        private boolean pressed = false;

        @Override
        public void install(Component component) {
            validateComponentType(component, ScrollButton.class);

            super.install(component);
        }

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

        public Dimensions getPreferredSize() {
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            // Apply scroll bar styles to the button
            ScrollButton scrollButton = (ScrollButton)getComponent();
            ScrollBar scrollBar = scrollButton.getScrollBar();
            Component.StyleDictionary scrollBarStyles = scrollBar.getStyles();

            Color backgroundColor = null;

            if (scrollButton.isEnabled()) {
                if (pressed) {
                    backgroundColor = (Color)scrollBarStyles.get
                        ("scrollButtonPressedBackgroundColor");
                } else if (highlighted) {
                    backgroundColor = (Color)scrollBarStyles.get
                        ("scrollButtonHighlightedBackgroundColor");
                } else {
                    backgroundColor = (Color)scrollBarStyles.get
                        ("scrollButtonBackgroundColor");
                }
            } else {
                backgroundColor = (Color)scrollBarStyles.get
                    ("scrollButtonDisabledBackgroundColor");
            }

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            graphics.setPaint(backgroundColor);
            graphics.fillRect(1, 1, width - 2, height - 2);

            // Paint the border
            graphics.setPaint((Color)scrollBarStyles.get("borderColor"));
            graphics.setStroke(new BasicStroke());
            graphics.drawRect(0, 0, width - 1, height - 1);

            // Size the image to be proportional to our size
            int buttonImageWidth, buttonImageHeight;
            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                buttonImageWidth = (int)((float)width / 3.0f);
                buttonImageHeight = (int)Math.floor((float)height / 2.0f);
            } else {
                buttonImageWidth = (int)Math.floor((float)width / 2.0f);
                buttonImageHeight = (int)((float)height / 3.0f);
            }
            ScrollButtonImage buttonImage = (ScrollButtonImage)scrollButton.getButtonImage();
            buttonImage.setSize(buttonImageWidth, buttonImageHeight);

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
        public void mouseOver() {
            super.mouseOver();

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut() {
            super.mouseOut();

            automaticScroller.stop();

            pressed = false;
            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(button, x, y);

            if (button == Mouse.Button.LEFT) {
                ScrollButton scrollButton = (ScrollButton)getComponent();
                ScrollBar scrollBar = scrollButton.getScrollBar();

                // Start the automatic scroller. It'll be stopped when we
                // mouse up or mouse out
                automaticScroller.start(scrollBar, scrollButton.getDirection(),
                    AutomaticScroller.IncrementType.UNIT);

                pressed = true;
                repaintComponent();

                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(button, x, y);

            if (button == Mouse.Button.LEFT) {
                automaticScroller.stop();

                pressed = false;
                repaintComponent();
            }

            return consumed;
        }
    }

    protected abstract class ScrollButtonImage extends Image {
        private int width = 0;
        private int height = 0;

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public void setSize(int width, int height) {
            this.width = width;
            this.height = height;
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

    public static class ScrollHandle extends Component {
        private ScrollBar scrollBar;

        public ScrollHandle(ScrollBar scrollBar) {
            this.scrollBar = scrollBar;

            installSkin(ScrollHandle.class);
        }

        public ScrollBar getScrollBar() {
            return scrollBar;
        }
    }

    public static class ScrollHandleSkin extends ComponentSkin {
        private class DisplayMouseHandler
            implements ComponentMouseButtonListener {
            public void mouseDown(Component component, Mouse.Button button,
                int x, int y) {
                // No-op
            }

            public void mouseUp(Component component, Mouse.Button button, int x, int y) {
                if (button == Mouse.Button.LEFT) {
                    assert (component instanceof Display);
                    component.getComponentMouseButtonListeners().remove(this);

                    highlighted = false;
                    repaintComponent();
                }
            }

            public void mouseClick(Component component, Mouse.Button button,
                int x, int y, int count) {
                // No-op
            }
        }

        private DisplayMouseHandler displayMouseHandler = new DisplayMouseHandler();

        private boolean highlighted = false;

        @Override
        public void install(Component component) {
            validateComponentType(component, ScrollHandle.class);

            super.install(component);
        }

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

        public Dimensions getPreferredSize() {
            throw new UnsupportedOperationException();
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            ScrollHandle scrollHandle = (ScrollHandle)getComponent();
            ScrollBar scrollBar = scrollHandle.getScrollBar();
            Component.StyleDictionary scrollBarStyles = scrollBar.getStyles();

            Color backgroundColor = null;

            if (highlighted) {
                backgroundColor = (Color)scrollBarStyles.get
                    ("scrollButtonHighlightedBackgroundColor");
            } else {
                backgroundColor = (Color)scrollBarStyles.get
                    ("scrollButtonBackgroundColor");
            }

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            graphics.setPaint(backgroundColor);
            graphics.fillRect(1, 1, width - 2, height - 2);

            // Paint the border
            graphics.setPaint((Color)scrollBarStyles.get("borderColor"));
            graphics.setStroke(new BasicStroke());
            graphics.drawRect(0, 0, width - 1, height - 1);

            // Paint the hash marks
            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                int middle = width / 2;
                graphics.setPaint(new Color(0xB1, 0xA5, 0x94));
                graphics.drawLine(middle - 3, 4, middle - 3, height - 5);
                graphics.drawLine(middle, 4, middle, height - 5);
                graphics.drawLine(middle + 3, 4, middle + 3, height - 5);
                graphics.setPaint(Color.WHITE);
                graphics.drawLine(middle - 2, 4, middle - 2, height - 5);
                graphics.drawLine(middle + 1, 4, middle + 1, height - 5);
                graphics.drawLine(middle + 4, 4, middle + 4, height - 5);
            } else {
                int middle = height / 2;
                graphics.setPaint(new Color(0xB1, 0xA5, 0x94));
                graphics.drawLine(4, middle - 3, width - 5, middle - 3);
                graphics.drawLine(4, middle, width - 5, middle);
                graphics.drawLine(4, middle + 3, width - 5, middle + 3);
                graphics.setPaint(Color.WHITE);
                graphics.drawLine(4, middle - 2, width - 5, middle - 2);
                graphics.drawLine(4, middle + 1, width - 5, middle + 1);
                graphics.drawLine(4, middle + 4, width - 5, middle + 4);
            }
        }

        @Override
        public void mouseOver() {
            super.mouseOver();

            if (highlighted) {
                // If the handle is already highlighted when the mouse enters
                // it, it means that the handle is "grabbed", meaning that we
                // have registered our display mouse handler.  Unregister it
                // here so as to not register multiple times as we move our
                // mouse in and out of the handle
                Component component = getComponent();
                Display display = component.getWindow().getDisplay();
                display.getComponentMouseButtonListeners().remove(displayMouseHandler);
            } else {
                // The handle is highlighted as long as the mouse is over it or
                // we're dragging it
                highlighted = true;
                repaintComponent();
            }
        }

        @Override
        public void mouseOut() {
            super.mouseOut();

            if (Mouse.isPressed(Mouse.Button.LEFT)) {
                // The user is currently dragging the handle.  We don't
                // un-highlight it until the user releases the left mouse
                // button.  NOTE the code that actually sets the scroll bar's
                // value during the drag operation is handled by ScrollBarSkin
                // since it needs access to scroll bar layout information
                Component component = getComponent();
                Display display = component.getWindow().getDisplay();
                display.getComponentMouseButtonListeners().add(displayMouseHandler);
            } else {
                // If we're not dragging the handle, then we un-highlight it
                // as soon as the mouse exits
                highlighted = false;
                repaintComponent();
            }
        }
    }

    private class DisplayMouseHandler
        implements ComponentMouseListener, ComponentMouseButtonListener {
        public void mouseMove(Component component, int x, int y) {
            ScrollBar scrollBar = (ScrollBar)getComponent();

            int pixelValue;

            if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                pixelValue = x - dragOffset.x - scrollUpButton.getWidth() + 1;
            } else {
                pixelValue = y - dragOffset.y - scrollUpButton.getHeight() + 1;
            }

            int realValue = (int)((float)pixelValue / getValueScale());

            int rangeEnd = scrollBar.getRangeEnd();
            int extent = scrollBar.getExtent();

            scrollBar.setValue(Math.min(Math.max(realValue, 0), rangeEnd - extent));
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
            if (button == Mouse.Button.LEFT) {
                assert (component instanceof Display);
                component.getComponentMouseListeners().remove(this);
            }
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
        }
    }

    private static final int DEFAULT_THICKNESS = 15;
    private static final int DEFAULT_LENGTH = 100;

    private static AutomaticScroller automaticScroller = new AutomaticScroller();

    private DisplayMouseHandler displayMouseHandler = new DisplayMouseHandler();
    private Point dragOffset = null;

    private ScrollButton scrollUpButton = null;
    private ScrollButton scrollDownButton = null;
    private ScrollHandle scrollHandle = null;

    private int minimumHandleLength = 31;
    private Color borderColor = new Color(0x81, 0x76, 0x67);
    private Color scrollButtonImageColor = Color.BLACK;
    private Color scrollButtonBackgroundColor = new Color(0xF0, 0xEC, 0xE7);
    private Color scrollButtonDisabledBackgroundColor = new Color(0xF0, 0xEC, 0xE7);
    private Color scrollButtonPressedBackgroundColor = new Color(0xDF, 0xD7, 0xCD);
    private Color scrollButtonHighlightedBackgroundColor = new Color(0xFB, 0xFA, 0xF8);

    public ScrollBarSkin() {
        super();

        setBackgroundColor(new Color(0xD9, 0xD2, 0xC7));
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, ScrollBar.class);

        super.install(component);

        ScrollBar scrollBar = (ScrollBar)component;
        scrollBar.getScrollBarListeners().add(this);
        scrollBar.getScrollBarValueListeners().add(this);

        scrollUpButton = new ScrollButton(scrollBar, -1, new ScrollUpImage());
        scrollBar.add(scrollUpButton);

        scrollDownButton = new ScrollButton(scrollBar, 1, new ScrollDownImage());
        scrollBar.add(scrollDownButton);

        scrollHandle = new ScrollHandle(scrollBar);
        scrollBar.add(scrollHandle);

        enabledChanged(scrollBar);
    }

    @Override
    public void uninstall() {
        ScrollBar scrollBar = (ScrollBar)getComponent();
        scrollBar.getScrollBarListeners().remove(this);
        scrollBar.getScrollBarValueListeners().remove(this);

        scrollBar.remove(scrollUpButton);
        scrollBar.remove(scrollDownButton);
        scrollBar.remove(scrollHandle);

        scrollUpButton = null;
        scrollDownButton = null;
        scrollHandle = null;

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
                    scrollHandle.setVisible(false);
                } else {
                    scrollHandle.setVisible(true);

                    scrollHandle.setSize(handleWidth, height);
                    scrollHandle.setLocation(handleX, 0);
                }
            } else {
                scrollHandle.setVisible(false);
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
                    scrollHandle.setVisible(false);
                } else {
                    scrollHandle.setVisible(true);

                    scrollHandle.setSize(width, handleHeight);
                    scrollHandle.setLocation(0, handleY);
                }
            } else {
                scrollHandle.setVisible(false);
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
        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null");
        }

        setBorderColor(Color.decode(borderColor));
    }

    public Color getScrollButtonImageColor() {
        return scrollButtonImageColor;
    }

    public void setScrollButtonImageColor(Color scrollButtonImageColor) {
        this.scrollButtonImageColor = scrollButtonImageColor;
        repaintComponent();
    }

    public final void setScrollButtonImageColor(String scrollButtonImageColor) {
        if (scrollButtonImageColor == null) {
            throw new IllegalArgumentException("scrollButtonImageColor is null");
        }

        setScrollButtonImageColor(Color.decode(scrollButtonImageColor));
    }

    public Color getScrollButtonBackgroundColor() {
        return scrollButtonBackgroundColor;
    }

    public void setScrollButtonBackgroundColor(Color scrollButtonBackgroundColor) {
        this.scrollButtonBackgroundColor = scrollButtonBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonBackgroundColor(String scrollButtonBackgroundColor) {
        if (scrollButtonBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonBackgroundColor is null");
        }

        setScrollButtonBackgroundColor(Color.decode(scrollButtonBackgroundColor));
    }

    public Color getScrollButtonDisabledBackgroundColor() {
        return scrollButtonDisabledBackgroundColor;
    }

    public void setScrollButtonDisabledBackgroundColor(Color scrollButtonDisabledBackgroundColor) {
        this.scrollButtonDisabledBackgroundColor = scrollButtonDisabledBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonDisabledBackgroundColor(String scrollButtonDisabledBackgroundColor) {
        if (scrollButtonDisabledBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonDisabledBackgroundColor is null");
        }

        setScrollButtonDisabledBackgroundColor(Color.decode(scrollButtonDisabledBackgroundColor));
    }

    public Color getScrollButtonPressedBackgroundColor() {
        return scrollButtonPressedBackgroundColor;
    }

    public void setScrollButtonPressedBackgroundColor(Color scrollButtonPressedBackgroundColor) {
        this.scrollButtonPressedBackgroundColor = scrollButtonPressedBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonPressedBackgroundColor(String scrollButtonPressedBackgroundColor) {
        if (scrollButtonPressedBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonPressedBackgroundColor is null");
        }

        setScrollButtonPressedBackgroundColor(Color.decode(scrollButtonPressedBackgroundColor));
    }

    public Color getScrollButtonHighlightedBackgroundColor() {
        return scrollButtonHighlightedBackgroundColor;
    }

    public void setScrollButtonHighlightedBackgroundColor(Color scrollButtonHighlightedBackgroundColor) {
        this.scrollButtonHighlightedBackgroundColor = scrollButtonHighlightedBackgroundColor;
        repaintComponent();
    }

    public final void setScrollButtonHighlightedBackgroundColor(String scrollButtonHighlightedBackgroundColor) {
        if (scrollButtonHighlightedBackgroundColor == null) {
            throw new IllegalArgumentException("scrollButtonHighlightedBackgroundColor is null");
        }

        setScrollButtonHighlightedBackgroundColor(Color.decode(scrollButtonHighlightedBackgroundColor));
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        if (scrollHandle.isVisible()
            && button == Mouse.Button.LEFT) {
            ScrollBar scrollBar = (ScrollBar)getComponent();

            Component mouseDownComponent = scrollBar.getComponentAt(x, y);

            if (mouseDownComponent == scrollHandle) {
                // Begin dragging the scroll handle. Register our display
                // mouse handler to do the actual work
                Display display = scrollBar.getWindow().getDisplay();

                dragOffset = scrollBar.mapPointToAncestor(display, x, y);
                dragOffset.translate(-scrollHandle.getX(), -scrollHandle.getY());

                display.getComponentMouseListeners().add(displayMouseHandler);
                display.getComponentMouseButtonListeners().add(displayMouseHandler);
            } else if (mouseDownComponent == null) {
                // Begin automatic block scrolling. Calculate the direction of
                // the scroll by checking to see if the user pressed the mouse
                // in the area "before" the handle or "after" it.

                int direction;
                int realStopValue;

                if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
                    direction = x < scrollHandle.getX() ? -1 : 1;

                    int pixelStopValue = x - scrollUpButton.getWidth() + 1;

                    if (direction == 1) {
                        // If we're scrolling down, account for the width of the
                        // handle in our pixel stop value so that we stop as soon
                        // as the *bottom* of the handle reaches our click point
                        pixelStopValue -= scrollHandle.getWidth();
                    }

                    realStopValue = (int)((float)pixelStopValue / getValueScale());
                } else {
                    direction = y < scrollHandle.getY() ? -1 : 1;

                    int pixelStopValue = y - scrollUpButton.getHeight() + 1;

                    if (direction == 1) {
                        // If we're scrolling down, account for the height of the
                        // handle in our pixel stop value so that we stop as soon
                        // as the *bottom* of the handle reaches our click point
                        pixelStopValue -= scrollHandle.getHeight();
                    }

                    realStopValue = (int)((float)pixelStopValue / getValueScale());
                }

                // Start the automatic scroller; we'll stop it upon mouse out or
                // mouse up
                automaticScroller.start(scrollBar, direction,
                    AutomaticScroller.IncrementType.BLOCK, realStopValue);
            }

            consumed = true;
        }

        return consumed;
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        automaticScroller.stop();
    }

    @Override
    public boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(button, x, y);

        if (button == Mouse.Button.LEFT) {
            automaticScroller.stop();
        }

        return consumed;
    }

    @Override
    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
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
     * <tt>scrollHandle</tt>.
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
            numLegalPixelValues = availableWidth - scrollHandle.getWidth() + 1;
        } else {
            int availableHeight = getHeight() - scrollUpButton.getHeight() -
                scrollDownButton.getHeight() + 2;
            numLegalPixelValues = availableHeight - scrollHandle.getHeight() + 1;
        }

        valueScale = (float)numLegalPixelValues / (float)numLegalRealValues;

        return valueScale;
    }

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
        if (scrollHandle.isVisible()) {
           int value = scrollBar.getValue();

           if (scrollBar.getOrientation() == Orientation.HORIZONTAL) {
              int handleX = (int)((float)value * getValueScale()) +
                 scrollUpButton.getWidth() - 1;

              scrollHandle.setLocation(handleX, 0);
           } else {
              int handleY = (int)((float)value * getValueScale()) +
                 scrollUpButton.getHeight() - 1;

              scrollHandle.setLocation(0, handleY);
           }
        }
    }
}
