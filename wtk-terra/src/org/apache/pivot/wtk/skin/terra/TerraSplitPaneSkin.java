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

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.SplitPaneListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Split pane skin.
 */
public class TerraSplitPaneSkin extends ContainerSkin implements SplitPaneListener {

    /**
     * Split pane splitter component.
     */
    protected class Splitter extends Component {
        public Splitter() {
            setSkin(new SplitterSkin());
        }
    }

    /**
     * Split pane splitter component skin.
     */
    protected class SplitterSkin extends ComponentSkin {
        private int dragOffset;
        private SplitterShadow shadow = null;

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public int getPreferredWidth(int height) {
            // This will never get called since the size of the splitter is set
            // automatically by SplitPaneSkin using the the size of the
            // SplitPane and the split thickness
            return 0;
        }

        @Override
        public int getPreferredHeight(int width) {
            // This will never get called since the size of the splitter is set
            // automatically by SplitPaneSkin using the the size of the
            // SplitPane and the split thickness
            return 0;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            SplitPane splitPane = (SplitPane)TerraSplitPaneSkin.this.getComponent();

            Orientation orientation = splitPane.getOrientation();

            int width = getWidth();
            int height = getHeight();

            int imageWidth, imageHeight;
            if (orientation == Orientation.HORIZONTAL) {
                imageWidth = width - 4;
                imageHeight = Math.min(height - 4, 8);
            } else {
                imageWidth = Math.min(width - 4, 8);
                imageHeight = height - 4;
            }

            if (imageWidth > 0
                && imageHeight > 0) {
                int translateX = (width - imageWidth) / 2;
                int translateY = (height - imageHeight) / 2;
                graphics.translate(translateX, translateY);

                Color dark = splitterHandlePrimaryColor;
                Color light = splitterHandleSecondaryColor;

                if (orientation == Orientation.HORIZONTAL) {
                    graphics.setStroke(new BasicStroke());

                    graphics.setPaint(dark);
                    graphics.drawLine(0, 0, imageWidth - 1, 0);
                    graphics.drawLine(0, 3, imageWidth - 1, 3);
                    graphics.drawLine(0, 6, imageWidth - 1, 6);

                    graphics.setPaint(light);
                    graphics.drawLine(0, 1, imageWidth - 1, 1);
                    graphics.drawLine(0, 4, imageWidth - 1, 4);
                    graphics.drawLine(0, 7, imageWidth - 1, 7);
                } else {
                    int half = imageHeight / 2;

                    graphics.setPaint(dark);
                    graphics.fillRect(0, 0, 2, half);
                    graphics.fillRect(3, 0, 2, half);
                    graphics.fillRect(6, 0, 2, half);

                    graphics.setPaint(light);
                    graphics.fillRect(0, half, 2, half);
                    graphics.fillRect(3, half, 2, half);
                    graphics.fillRect(6, half, 2, half);
                }
            }
        }

        @Override
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (Mouse.getCapturer() == component) {
                SplitPane splitPane = (SplitPane)TerraSplitPaneSkin.this.getComponent();
                Orientation orientation = splitPane.getOrientation();

                // Calculate the would-be new split location
                int splitLocation;
                float splitRatio;
                if (orientation == Orientation.HORIZONTAL) {
                    splitLocation = limitSplitLocation(component.getX() + x - dragOffset);
                    splitRatio = (float)splitLocation / splitPane.getWidth();
                } else {
                    splitLocation = limitSplitLocation(component.getY() + y - dragOffset);
                    splitRatio = (float)splitLocation / splitPane.getHeight();
                }

                if (shadow == null) {
                    // Update the split location immediately
                    splitPane.setSplitRatio(splitRatio);
                } else {
                    // Move the shadow to the split location
                    if (orientation == Orientation.HORIZONTAL) {
                        shadow.setLocation(splitLocation, 0);
                    } else {
                        shadow.setLocation(0, splitLocation);
                    }
                }
            }

            return consumed;
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            SplitPane splitPane = (SplitPane)TerraSplitPaneSkin.this.getComponent();

            if (button == Mouse.Button.LEFT
                && !splitPane.isLocked()) {
                Orientation orientation = splitPane.getOrientation();

                if (useShadow) {
                    // Add the shadow to the split pane and lay it out
                    shadow = new SplitterShadow();
                    splitPane.add(shadow);

                    if (orientation == Orientation.HORIZONTAL) {
                        shadow.setLocation(component.getX(), 0);
                    } else {
                        shadow.setLocation(0, component.getY());
                    }

                    shadow.setSize(getWidth(), getHeight());
                }

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
                if (shadow != null) {
                    SplitPane splitPane = (SplitPane)TerraSplitPaneSkin.this.getComponent();

                    // Update the split location and remove the shadow
                    int splitLocation;
                    float splitRatio;
                    if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                        splitLocation = shadow.getX();
                        splitRatio = (float)splitLocation / splitPane.getWidth();
                    } else {
                        splitLocation = shadow.getY();
                        splitRatio = (float)splitLocation / splitPane.getHeight();
                    }

                    splitPane.setSplitRatio(splitRatio);

                    splitPane.remove(shadow);
                    shadow = null;
                }

                Mouse.release();
            }

            return consumed;
        }
    }

    /**
     * Split pane splitter shadow component.
     */
    protected class SplitterShadow extends Component {
        public SplitterShadow() {
            setSkin(new SplitterShadowSkin());
        }
    }

    /**
     * Split pane splitter shadow component skin.
     */
    protected class SplitterShadowSkin extends ComponentSkin {
        @Override
        public int getPreferredWidth(int height) {
            // This will never get called since the splitter will always just
            // set the size of its shadow to match its own size
            return 0;
        }

        @Override
        public int getPreferredHeight(int width) {
            // This will never get called since the splitter will always just
            // set the size of its shadow to match its own size
            return 0;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            graphics.setPaint(new Color(0, 0, 0, 64));
            graphics.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private Splitter splitter = new Splitter();

    private Color splitterHandlePrimaryColor;
    private Color splitterHandleSecondaryColor;
    private int splitterThickness;
    private boolean useShadow;

    public TerraSplitPaneSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        splitterHandlePrimaryColor = theme.getColor(9);
        splitterHandleSecondaryColor = theme.getColor(10);
        splitterThickness = 6;
        useShadow = false;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        SplitPane splitPane = (SplitPane)component;
        splitPane.getSplitPaneListeners().add(this);

        splitPane.add(splitter);
        updateSplitterCursor();
    }

    @Override
    public void setSize(int width, int height) {
        int previousWidth = getWidth();
        int previousHeight = getHeight();

        super.setSize(width, height);

        SplitPane splitPane = (SplitPane)getComponent();
        Orientation orientation = splitPane.getOrientation();

        if (splitPane.getResizeMode() == SplitPane.ResizeMode.PRIMARY_REGION
            && ((previousWidth != width && orientation == Orientation.HORIZONTAL)
            || (previousHeight != height && orientation == Orientation.VERTICAL))) {
            SplitPane.Region primaryRegion = splitPane.getPrimaryRegion();
            float splitRatio = splitPane.getSplitRatio();

            if (orientation == Orientation.HORIZONTAL) {
                int splitLocation = (int)(splitRatio * previousWidth);

                if (primaryRegion == SplitPane.Region.BOTTOM_RIGHT) {
                    // Move the split location to maintain size on the right
                    splitLocation += (width - previousWidth);
                }

                splitRatio = (float)limitSplitLocation(splitLocation) / width;
            } else {
                int splitLocation = (int)(splitRatio * previousHeight);

                if (primaryRegion == SplitPane.Region.BOTTOM_RIGHT) {
                    // Move the split location to maintain size on the bottom
                    splitLocation += (height - previousHeight);
                }

                splitRatio = (float)limitSplitLocation(splitLocation) / height;
            }

            splitPane.setSplitRatio(splitRatio);
        }
    }

    @Override
    public int getPreferredWidth(int height) {
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        return 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(0, 0);
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        SplitPane splitPane = (SplitPane)getComponent();

        float splitRatio = splitPane.getSplitRatio();
        Component topLeft = splitPane.getTopLeft();
        Component bottomRight = splitPane.getBottomRight();

        if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
            int splitLocation = limitSplitLocation((int)(splitRatio * width));
            int rightStart = splitLocation + splitterThickness;
            splitter.setLocation(splitLocation, 0);
            splitter.setSize(splitterThickness, height);

            if (topLeft != null) {
                topLeft.setLocation(0, 0);
                topLeft.setSize(splitLocation, height);
            }

            if (bottomRight != null) {
                bottomRight.setLocation(rightStart, 0);
                bottomRight.setSize(Math.max(width - rightStart, 0), height);
            }
        } else {
            int splitLocation = limitSplitLocation((int)(splitRatio * height));
            int bottomStart = splitLocation + splitterThickness;
            splitter.setLocation(0, splitLocation);
            splitter.setSize(width, splitterThickness);

            if (topLeft != null) {
                topLeft.setLocation(0, 0);
                topLeft.setSize(width, splitLocation);
            }

            if (bottomRight != null) {
                bottomRight.setLocation(0, bottomStart);
                bottomRight.setSize(width, Math.max(height - bottomStart, 0));
            }
        }
    }

    public Color getSplitterHandlePrimaryColor() {
        return splitterHandlePrimaryColor;
    }

    public void setSplitterHandlePrimaryColor(Color splitterHandlePrimaryColor) {
        if (splitterHandlePrimaryColor == null) {
            throw new IllegalArgumentException("splitterHandlePrimaryColor is null.");
        }

        this.splitterHandlePrimaryColor = splitterHandlePrimaryColor;
        splitter.repaint();
    }

    public final void setSplitterHandlePrimaryColor(String splitterHandlePrimaryColor) {
        if (splitterHandlePrimaryColor == null) {
            throw new IllegalArgumentException("splitterHandlePrimaryColor is null.");
        }

        setSplitterHandlePrimaryColor(GraphicsUtilities.decodeColor(splitterHandlePrimaryColor));
    }

    public final void setSplitterHandlePrimaryColor(int splitterHandlePrimaryColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSplitterHandlePrimaryColor(theme.getColor(splitterHandlePrimaryColor));
    }

    public Color getSplitterHandleSecondaryColor() {
        return splitterHandleSecondaryColor;
    }

    public void setSplitterHandleSecondaryColor(Color splitterHandleSecondaryColor) {
        if (splitterHandleSecondaryColor == null) {
            throw new IllegalArgumentException("splitterHandleSecondaryColor is null.");
        }

        this.splitterHandleSecondaryColor = splitterHandleSecondaryColor;
        splitter.repaint();
    }

    public final void setSplitterHandleSecondaryColor(String splitterHandleSecondaryColor) {
        if (splitterHandleSecondaryColor == null) {
            throw new IllegalArgumentException("splitterHandleSecondaryColor is null.");
        }

        setSplitterHandleSecondaryColor(GraphicsUtilities.decodeColor(splitterHandleSecondaryColor));
    }

    public final void setSplitterHandleSecondaryColor(int splitterHandleSecondaryColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSplitterHandleSecondaryColor(theme.getColor(splitterHandleSecondaryColor));
    }

    public int getSplitterThickness() {
        return splitterThickness;
    }

    public void setSplitterThickness(int splitterThickness) {
        if (splitterThickness < 0) {
            throw new IllegalArgumentException("splitterThickness is negative.");
        }
        this.splitterThickness = splitterThickness;
        invalidateComponent();
    }

    public boolean getUseShadow() {
        return useShadow;
    }

    public void setUseShadow(boolean useShadow) {
        if (Mouse.getCapturer() == getComponent()) {
            throw new IllegalStateException("Cannot set useShadow while the splitter is being dragged.");
        }

        this.useShadow = useShadow;
    }

    @Override
    public void topLeftChanged(SplitPane splitPane, Component previousTopLeft) {
        invalidateComponent();
    }

    @Override
    public void bottomRightChanged(SplitPane splitPane, Component previousBottomRight) {
        invalidateComponent();
    }

    @Override
    public void orientationChanged(SplitPane splitPane) {
        updateSplitterCursor();
        invalidateComponent();
    }

    @Override
    public void primaryRegionChanged(SplitPane splitPane) {
        updateSplitterCursor();
    }

    @Override
    public void splitRatioChanged(SplitPane splitPane, float previousSplitLocation) {
        invalidateComponent();
    }

    @Override
    public void lockedChanged(SplitPane splitPane) {
        updateSplitterCursor();
    }

    @Override
    public void resizeModeChanged(SplitPane splitPane, SplitPane.ResizeMode previousResizeMode) {
        // No-op
    }

    private void updateSplitterCursor() {
        Cursor cursor = Cursor.DEFAULT;
        SplitPane splitPane = (SplitPane)getComponent();

        if (!splitPane.isLocked()) {
            switch (splitPane.getOrientation()) {
                case HORIZONTAL: {
                    switch (splitPane.getPrimaryRegion()) {
                        case TOP_LEFT: {
                            cursor = Cursor.RESIZE_EAST;
                            break;
                        }
                        case BOTTOM_RIGHT: {
                            cursor = Cursor.RESIZE_WEST;
                            break;
                        }
                        default: {
                            break;
                        }
                    }

                    break;
                }

                case VERTICAL: {
                    switch (splitPane.getPrimaryRegion()) {
                        case TOP_LEFT: {
                            cursor = Cursor.RESIZE_SOUTH;
                            break;
                        }
                        case BOTTOM_RIGHT: {
                            cursor = Cursor.RESIZE_NORTH;
                            break;
                        }
                        default: {
                            break;
                        }
                    }

                    break;
                }

                default: {
                    break;
                }
            }
        }

        splitter.setCursor(cursor);
    }

    private int limitSplitLocation(int splitLocation) {
        SplitPane splitPane = (SplitPane)getComponent();

        Component topLeft = splitPane.getTopLeft();
        Component bottomRight = splitPane.getBottomRight();

        int lower, upper;

        if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
            lower = 0;
            upper = Math.max(getWidth() - splitterThickness, 0);

            if (topLeft  != null) {
                int leftLimit = topLeft.getMinimumWidth();
                if (leftLimit >= 0) {
                    lower = Math.min(leftLimit, upper);
                }
            }

            if (bottomRight != null) {
                int rightLimit = bottomRight.getMinimumWidth();
                if (rightLimit >= 0) {
                    upper = Math.max(upper - rightLimit, lower);
                }
            }
        } else {
            lower = 0;
            upper = Math.max(getHeight() - splitterThickness, 0);

            if (topLeft  != null) {
                int topLimit = topLeft.getMinimumHeight();
                if (topLimit >= 0) {
                    lower = Math.min(topLimit, upper);
                }
            }

            if (bottomRight != null) {
                int bottomLimit = bottomRight.getMinimumHeight();
                if (bottomLimit >= 0) {
                    upper = Math.max(upper - bottomLimit, lower);
                }
            }
        }

        if (splitLocation < lower) {
            return lower;
        } else if (splitLocation > upper) {
            return upper;
        }

        return splitLocation;
    }
}
