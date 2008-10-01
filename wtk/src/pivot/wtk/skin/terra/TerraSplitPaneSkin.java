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

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.Point;
import pivot.wtk.Span;
import pivot.wtk.SplitPane;
import pivot.wtk.SplitPaneListener;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Split pane skin.
 *
 * @author tvolkert
 */
public class TerraSplitPaneSkin extends ContainerSkin
    implements SplitPaneListener {

    /**
     * Split pane splitter component.
     *
     * @author tvolkert
     */
    public static class Splitter extends Component {
        private SplitPane splitPane;

        public Splitter(SplitPane splitPane) {
            super();

            this.splitPane = splitPane;

            installSkin(Splitter.class);
        }

        public SplitPane getSplitPane() {
            return splitPane;
        }

        public void updateCursor() {
            if (splitPane.isLocked()) {
                setCursor(Cursor.DEFAULT);
            } else {
                if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                    if (splitPane.getPrimaryRegion() == SplitPane.Region.TOP_LEFT) {
                        setCursor(Cursor.RESIZE_EAST);
                    } else {
                        setCursor(Cursor.RESIZE_WEST);
                    }
                } else {
                    if (splitPane.getPrimaryRegion() == SplitPane.Region.TOP_LEFT) {
                        setCursor(Cursor.RESIZE_SOUTH);
                    } else {
                        setCursor(Cursor.RESIZE_NORTH);
                    }
                }
            }
        }
    }

    /**
     * Split pane splitter component skin.
     *
     * @author tvolkert
     */
    public static class SplitterSkin extends ComponentSkin {
        private class DragHandler
            implements ComponentMouseListener, ComponentMouseButtonListener {
            public boolean mouseMove(Component component, int x, int y) {
                Splitter splitter = (Splitter)getComponent();
                SplitPane splitPane = splitter.getSplitPane();

                // Calculate the would-be new split location
                int splitLocation;
                if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                    splitLocation = x - dragOffset;
                } else {
                    splitLocation = y - dragOffset;
                }

                splitLocation = boundSplitLocation(splitLocation);

                if (shadow == null) {
                    // Update the split location immediately
                    splitPane.setSplitLocation(splitLocation);
                } else {
                    // Move the shadow
                    if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                        shadow.setLocation(splitLocation, 0);
                    } else {
                        shadow.setLocation(0, splitLocation);
                    }
                }

                return false;
            }

            public void mouseOver(Component component) {
            }

            public void mouseOut(Component component) {
            }


            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
                if (shadow != null) {
                    Splitter splitter = (Splitter)getComponent();
                    SplitPane splitPane = splitter.getSplitPane();

                    // Update the split location and remove the shadow
                    int splitLocation;
                    if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                        splitLocation = shadow.getX();
                    } else {
                        splitLocation = shadow.getY();
                    }

                    splitPane.setSplitLocation(splitLocation);

                    splitPane.remove(shadow);
                    shadow = null;
                }

                assert (component instanceof Display);
                component.getComponentMouseListeners().remove(this);
                component.getComponentMouseButtonListeners().remove(this);

                return false;
            }

            public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            }
        }

        private int dragOffset;
        private SplitterShadow shadow = null;
        private DragHandler dragHandler = new DragHandler();

        @Override
        public boolean isFocusable() {
            return false;
        }

        public int getPreferredWidth(int height) {
            // This will never get called since the size of the splitter is set
            // automatically by SplitPaneSkin using the the size of the
            // SplitPane and the split thickness
            return 0;
        }

        public int getPreferredHeight(int width) {
            // This will never get called since the size of the splitter is set
            // automatically by SplitPaneSkin using the the size of the
            // SplitPane and the split thickness
            return 0;
        }

        public Dimensions getPreferredSize() {
            // This will never get called since the size of the splitter is set
            // automatically by SplitPaneSkin using the the size of the
            // SplitPane and the split thickness
            return null;
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            Splitter splitter = (Splitter)getComponent();
            SplitPane splitPane = splitter.getSplitPane();

            int width = getWidth();
            int height = getHeight();

            int imageWidth, imageHeight;
            if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                imageWidth = width - 4;
                imageHeight = Math.min(height - 4, 8);
            } else {
                imageWidth = Math.min(width - 4, 8);
                imageHeight = height - 4;
            }

            if (imageWidth > 0 && imageHeight > 0) {
                int translateX = (width - imageWidth) / 2;
                int translateY = (height - imageHeight) / 2;
                graphics.translate(translateX, translateY);

                Color dark = new Color(0xCC, 0xCC, 0xCC);
                Color light = new Color(0xE0, 0xE0, 0xE0);

                if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
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
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            Splitter splitter = (Splitter)getComponent();
            SplitPane splitPane = splitter.getSplitPane();

            if (!splitPane.isLocked()) {
                int splitLocation = splitPane.getSplitLocation();

                Display display = splitPane.getWindow().getDisplay();
                Point displayCoordinates = splitter.mapPointToAncestor(display, x, y);

                if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                    dragOffset = displayCoordinates.x - splitLocation;
                } else {
                    dragOffset = displayCoordinates.y - splitLocation;
                }

                boolean useShadow = (Boolean)splitPane.getStyles().get("useShadow");
                if (useShadow) {
                    shadow = new SplitterShadow();
                    splitPane.add(shadow);

                    if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                        shadow.setLocation(splitter.getX(), 0);
                    } else {
                        shadow.setLocation(0, splitter.getY());
                    }
                    shadow.setSize(getWidth(), getHeight());
                }

                display.getComponentMouseListeners().add(dragHandler);
                display.getComponentMouseButtonListeners().add(dragHandler);
            }

            return false;
        }

        private int boundSplitLocation(int splitLocation) {
            Splitter splitter = (Splitter)getComponent();
            SplitPane splitPane = splitter.getSplitPane();

            int splitterThickness = (Integer)splitPane.getStyles().get("splitterThickness");

            int lower = 0;
            int upper;
            if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
                upper = splitPane.getWidth() - splitterThickness;
            } else {
                upper = splitPane.getHeight() - splitterThickness;
            }

            Span bounds = splitPane.getSplitBounds();
            if (bounds != null) {
                lower = Math.max(lower, bounds.getStart());
                upper = Math.min(upper, bounds.getEnd());
            }

            if (splitLocation < lower) {
                splitLocation = lower;
            } else if (splitLocation > upper) {
                splitLocation = upper;
            }

            return splitLocation;
        }
    }

    /**
     * Split pane splitter shadow component.
     *
     * @author tvolkert
     */
    public static class SplitterShadow extends Component {
        public SplitterShadow() {
            super();

            installSkin(SplitterShadow.class);
        }
    }

    /**
     * Split pane splitter shadow component skin.
     *
     * @author tvolkert
     */
    public static class SplitterShadowSkin extends ComponentSkin {
        public int getPreferredWidth(int height) {
            // This will never get called since the splitter will always just
            // set the size of its shadow to match its own size
            return 0;
        }

        public int getPreferredHeight(int width) {
            // This will never get called since the splitter will always just
            // set the size of its shadow to match its own size
            return 0;
        }

        public Dimensions getPreferredSize() {
            // This will never get called since the splitter will always just
            // set the size of its shadow to match its own size
            return null;
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            graphics.setStroke(new BasicStroke());
            graphics.setPaint(Color.BLACK);

            float opacity = 0.25f;
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                opacity));

            graphics.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private Splitter splitter;

    private int splitterThickness = 6;
    private boolean useShadow = false;

    public TerraSplitPaneSkin() {
        super();
    }

    @Override
    public void install(Component component) {
        super.install(component);

        SplitPane splitPane = (SplitPane)component;
        splitPane.getSplitPaneListeners().add(this);

        splitter = new Splitter(splitPane);
        splitPane.add(splitter);
        splitter.updateCursor();
    }

    @Override
    public void uninstall() {
        SplitPane splitPane = (SplitPane)getComponent();
        splitPane.getSplitPaneListeners().remove(this);

        splitPane.remove(splitter);
        splitter = null;

        super.uninstall();
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

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        SplitPane splitPane = (SplitPane)getComponent();
        int splitLocation = splitPane.getSplitLocation();
        Component leftComponent = splitPane.getTopLeftComponent();
        Component rightComponent = splitPane.getBottomRightComponent();

        int rightStart = splitLocation + splitterThickness;

        if (splitPane.getOrientation() == Orientation.HORIZONTAL) {
            splitter.setLocation(splitLocation, 0);
            splitter.setSize(splitterThickness, height);

            if (leftComponent != null) {
                leftComponent.setLocation(0, 0);
                leftComponent.setSize(splitLocation, height);
            }

            if (rightComponent != null) {
                rightComponent.setLocation(rightStart, 0);
                rightComponent.setSize(Math.max(width - rightStart, 0), height);
            }
        } else {
            splitter.setLocation(0, splitLocation);
            splitter.setSize(width, splitterThickness);

            if (leftComponent != null) {
                leftComponent.setLocation(0, 0);
                leftComponent.setSize(width, splitLocation);
            }

            if (rightComponent != null) {
                rightComponent.setLocation(0, rightStart);
                rightComponent.setSize(width, Math.max(height - rightStart, 0));
            }
        }
    }

    public int getSplitterThickness() {
        return splitterThickness;
    }

    public void setSplitterThickness(int splitterThickness) {
        this.splitterThickness = splitterThickness;
        invalidateComponent();
    }

    public final void setSplitterThickness(String splitterThickness) {
        if (splitterThickness == null) {
            throw new IllegalArgumentException("splitterThickness is null.");
        }

        setSplitterThickness(Integer.parseInt(splitterThickness));
    }

    public boolean getUseShadow() {
        return useShadow;
    }

    public void setUseShadow(boolean useShadow) {
        this.useShadow = useShadow;
    }

    public final void setUseShadow(String useShadow) {
        if (useShadow == null) {
            throw new IllegalArgumentException("useShadow is null.");
        }

        setUseShadow(Boolean.parseBoolean(useShadow));
    }

    public void topLeftComponentChanged(SplitPane splitPane, Component previousTopLeftComponent) {
        // No-op
    }

    public void bottomRightComponentChanged(SplitPane splitPane, Component previousBottomRightComponent) {
        // No-op
    }

    public void orientationChanged(SplitPane splitPane) {
        splitter.updateCursor();
        invalidateComponent();
    }

    public void primaryRegionChanged(SplitPane splitPane) {
        splitter.updateCursor();
    }

    public void splitLocationChanged(SplitPane splitPane, int previousSplitLocation) {
        invalidateComponent();
    }

    public void splitBoundsChanged(SplitPane splitPane, Span previousSplitBounds) {
        invalidateComponent();
    }

    public void lockedChanged(SplitPane splitPane) {
        splitter.updateCursor();
    }
}
