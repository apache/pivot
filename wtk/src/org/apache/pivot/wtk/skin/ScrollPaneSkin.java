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
package org.apache.pivot.wtk.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Transparency;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollBarValueListener;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.ScrollPane.Corner;
import org.apache.pivot.wtk.ScrollPane.ScrollBarPolicy;
import org.apache.pivot.wtk.ScrollPaneListener;
import org.apache.pivot.wtk.Viewport;
import org.apache.pivot.wtk.ViewportListener;

/**
 * Scroll pane skin.
 */
public class ScrollPaneSkin extends ContainerSkin
    implements Viewport.Skin, ScrollPaneListener, ViewportListener,
               ScrollBarValueListener {

    private ScrollBar horizontalScrollBar = new ScrollBar(Orientation.HORIZONTAL);
    private ScrollBar verticalScrollBar = new ScrollBar(Orientation.VERTICAL);

    private Corner topLeftCorner = new Corner(Corner.Placement.TOP_LEFT);
    private Corner bottomLeftCorner = new Corner(Corner.Placement.BOTTOM_LEFT);
    private Corner bottomRightCorner = new Corner(Corner.Placement.BOTTOM_RIGHT);
    private Corner topRightCorner = new Corner(Corner.Placement.TOP_RIGHT);

    private int horizontalReveal = 30;
    private int verticalReveal = 30;

    private int cachedHorizontalScrollBarHeight = 0;
    private int cachedVerticalScrollBarWidth = 0;

    private boolean optimizeScrolling = true;

    private static final int DEFAULT_HORIZONTAL_INCREMENT = 10;
    private static final int DEFAULT_VERTICAL_INCREMENT = 10;

    public ScrollPaneSkin() {
        setBackgroundPaint(Color.WHITE);

        horizontalScrollBar.setUnitIncrement(DEFAULT_HORIZONTAL_INCREMENT);
        verticalScrollBar.setUnitIncrement(DEFAULT_VERTICAL_INCREMENT);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ScrollPane scrollPane = (ScrollPane)component;
        scrollPane.getViewportListeners().add(this);
        scrollPane.getScrollPaneListeners().add(this);

        scrollPane.add(horizontalScrollBar);
        scrollPane.add(verticalScrollBar);

        scrollPane.add(topLeftCorner);
        scrollPane.add(bottomLeftCorner);
        scrollPane.add(bottomRightCorner);
        scrollPane.add(topRightCorner);

        horizontalScrollBar.getScrollBarValueListeners().add(this);
        verticalScrollBar.getScrollBarValueListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        ScrollPane scrollPane = (ScrollPane)getComponent();
        Component view = scrollPane.getView();

        if (view != null) {
            int preferredRowHeaderWidth = 0;
            Component rowHeader = scrollPane.getRowHeader();
            if (rowHeader != null) {
                preferredRowHeaderWidth = rowHeader.getPreferredWidth(-1);
            }

            int preferredColumnHeaderHeight = 0;
            Component columnHeader = scrollPane.getColumnHeader();
            if (columnHeader != null) {
                preferredColumnHeaderHeight = columnHeader.getPreferredHeight(-1);
            }

            ScrollBarPolicy verticalPolicy = scrollPane.getVerticalScrollBarPolicy();

            if (verticalPolicy != ScrollBarPolicy.FILL) {
                // Get the unconstrained preferred size of the view
                Dimensions preferredViewSize = view.getPreferredSize();

                // If the policy is FILL_TO_CAPACITY, and the sum of the
                // unconstrained preferred heights of the view and the column
                // header is less than the height constraint, apply the FILL
                // policy; otherwise, apply the AUTO policy

                if (verticalPolicy == ScrollBarPolicy.FILL_TO_CAPACITY) {
                    if (height < 0) {
                        verticalPolicy = ScrollBarPolicy.AUTO;
                    } else {
                        int preferredHeight = preferredViewSize.height +
                            preferredColumnHeaderHeight;

                        if (preferredHeight < height) {
                            verticalPolicy = ScrollBarPolicy.FILL;
                        } else {
                            verticalPolicy = ScrollBarPolicy.AUTO;
                        }
                    }
                }

                // If the policy is ALWAYS, NEVER, or AUTO, the preferred
                // width is the sum of the unconstrained preferred widths of
                // the view and row header, plus the width of the scroll
                // bar if policy is ALWAYS or if the view's preferred height is
                // greater than the height constraint and the policy is AUTO

                if (verticalPolicy == ScrollBarPolicy.ALWAYS
                    || verticalPolicy == ScrollBarPolicy.NEVER
                    || verticalPolicy == ScrollBarPolicy.AUTO) {
                    preferredWidth = preferredViewSize.width +
                        preferredRowHeaderWidth;

                    // If the sum of the preferred heights of the view and the
                    // column header is greater than the height constraint,
                    // include the preferred width of the scroll bar in the
                    // preferred width calculation
                    if (verticalPolicy == ScrollBarPolicy.ALWAYS
                        || (verticalPolicy == ScrollBarPolicy.AUTO
                        && height > 0
                        && preferredViewSize.height + preferredColumnHeaderHeight > height)) {
                        preferredWidth += verticalScrollBar.getPreferredWidth(-1);
                    }
                }
            }

            if (verticalPolicy == ScrollBarPolicy.FILL) {
                // Preferred width is the sum of the constrained preferred
                // width of the view and the unconstrained preferred width of
                // the row header

                if (height >= 0) {
                    // Subtract the unconstrained preferred height of the
                    // column header from the height constraint
                    height = Math.max(height - preferredColumnHeaderHeight, 0);
                }

                preferredWidth = view.getPreferredWidth(height) +
                    preferredRowHeaderWidth;
            }
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        ScrollPane scrollPane = (ScrollPane)getComponent();
        Component view = scrollPane.getView();

        if (view != null) {
            int preferredRowHeaderWidth = 0;
            Component rowHeader = scrollPane.getRowHeader();
            if (rowHeader != null) {
                preferredRowHeaderWidth = rowHeader.getPreferredWidth(-1);
            }

            int preferredColumnHeaderHeight = 0;
            Component columnHeader = scrollPane.getColumnHeader();
            if (columnHeader != null) {
                preferredColumnHeaderHeight = columnHeader.getPreferredHeight(-1);
            }

            ScrollBarPolicy horizontalPolicy = scrollPane.getHorizontalScrollBarPolicy();

            if (horizontalPolicy != ScrollBarPolicy.FILL) {
                // Get the unconstrained preferred size of the view
                Dimensions preferredViewSize = view.getPreferredSize();

                // If the policy is FILL_TO_CAPACITY, and the sum of the
                // unconstrained preferred widths of the view and the row
                // header is less than the width constraint, apply the FILL
                // policy; otherwise, apply the AUTO policy

                if (horizontalPolicy == ScrollBarPolicy.FILL_TO_CAPACITY) {
                    if (width < 0) {
                        horizontalPolicy = ScrollBarPolicy.AUTO;
                    } else {
                        int preferredWidth = preferredViewSize.width +
                            preferredRowHeaderWidth;

                        if (preferredWidth < width) {
                            horizontalPolicy = ScrollBarPolicy.FILL;
                        } else {
                            horizontalPolicy = ScrollBarPolicy.AUTO;
                        }
                    }
                }

                // If the policy is ALWAYS, NEVER, or AUTO, the preferred
                // height is the sum of the unconstrained preferred heights of
                // the view and column header, plus the height of the scroll
                // bar if policy is ALWAYS or if the view's preferred width is
                // greater than the width constraint and the policy is AUTO

                if (horizontalPolicy == ScrollBarPolicy.ALWAYS
                    || horizontalPolicy == ScrollBarPolicy.NEVER
                    || horizontalPolicy == ScrollBarPolicy.AUTO) {
                    preferredHeight = preferredViewSize.height +
                        preferredColumnHeaderHeight;

                    // If the sum of the preferred widths of the view and the
                    // row header is greater than the width constraint, include
                    // the preferred height of the scroll bar in the preferred
                    // height calculation
                    if (horizontalPolicy == ScrollBarPolicy.ALWAYS
                        || (horizontalPolicy == ScrollBarPolicy.AUTO
                        && width > 0
                        && preferredViewSize.width + preferredRowHeaderWidth > width)) {
                        preferredHeight += horizontalScrollBar.getPreferredHeight(-1);
                    }
                }
            }

            if (horizontalPolicy == ScrollBarPolicy.FILL) {
                // Preferred height is the sum of the constrained preferred height
                // of the view and the unconstrained preferred height of the column
                // header

                if (width >= 0) {
                    // Subtract the unconstrained preferred width of the row header
                    // from the width constraint
                    width = Math.max(width - preferredRowHeaderWidth, 0);
                }

                preferredHeight = view.getPreferredHeight(width) +
                    preferredColumnHeaderHeight;
            }
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        ScrollPane scrollPane = (ScrollPane)getComponent();

        int preferredWidth = 0;
        int preferredHeight = 0;

        Component view = scrollPane.getView();
        if (view != null) {
            Dimensions preferredViewSize = view.getPreferredSize();

            preferredWidth += preferredViewSize.width;
            preferredHeight += preferredViewSize.height;

            Component rowHeader = scrollPane.getRowHeader();
            if (rowHeader != null) {
                preferredWidth += rowHeader.getPreferredWidth(-1);
            }

            Component columnHeader = scrollPane.getColumnHeader();
            if (columnHeader != null) {
                preferredHeight += columnHeader.getPreferredHeight(-1);
            }

            if (scrollPane.getHorizontalScrollBarPolicy() == ScrollBarPolicy.ALWAYS) {
                preferredHeight += horizontalScrollBar.getPreferredHeight(-1);
            }

            if (scrollPane.getVerticalScrollBarPolicy() == ScrollBarPolicy.ALWAYS) {
                preferredWidth += verticalScrollBar.getPreferredWidth(-1);
            }
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        ScrollPane scrollPane = (ScrollPane)getComponent();

        Component view = scrollPane.getView();
        Component rowHeader = scrollPane.getRowHeader();
        Component columnHeader = scrollPane.getColumnHeader();

        int baseline = -1;

        int clientWidth = width;
        int clientHeight = height;

        int rowHeaderWidth = 0;
        if (rowHeader != null) {
            rowHeaderWidth = rowHeader.getPreferredWidth(-1);
            clientWidth -= rowHeaderWidth;
        }

        int columnHeaderHeight = 0;
        if (columnHeader != null) {
            columnHeaderHeight = columnHeader.getPreferredHeight(-1);
            clientHeight -= columnHeaderHeight;

            baseline = columnHeader.getBaseline(clientWidth, columnHeaderHeight);
        }

        if (baseline == -1
            && rowHeader != null) {
            baseline = rowHeader.getBaseline(rowHeaderWidth, clientHeight);

            if (baseline != -1) {
                baseline += columnHeaderHeight;
            }
        }

        if (baseline == -1
            && view != null) {
            baseline = view.getBaseline(clientWidth, clientHeight);

            if (baseline != -1) {
                baseline += columnHeaderHeight;
            }
        }

        return baseline;
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = false;

        ScrollPane scrollPane = (ScrollPane)getComponent();
        Component view = scrollPane.getView();

        if (view != null) {
            // The scroll orientation is tied to whether the shift key was
            // pressed while the mouse wheel was scrolled
            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                // Treat the mouse wheel as a horizontal scroll event
                int previousScrollLeft = scrollPane.getScrollLeft();
                int newScrollLeft = previousScrollLeft + (scrollAmount * wheelRotation *
                    horizontalScrollBar.getUnitIncrement());

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

                scrollPane.setScrollLeft(newScrollLeft);
            } else {
                // Treat the mouse wheel as a vertical scroll event
                int previousScrollTop = scrollPane.getScrollTop();
                int newScrollTop = previousScrollTop + (scrollAmount * wheelRotation *
                    verticalScrollBar.getUnitIncrement());

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

                scrollPane.setScrollTop(newScrollTop);
            }
        }

        return consumed;
    }

    /**
     * Key presses have no effect if the event has already been consumed.<p>
     * {@link KeyCode#UP UP} Scroll up a single scroll unit.<br>
     * {@link KeyCode#DOWN DOWN} Scroll down a single scroll unit.<br>
     * {@link KeyCode#LEFT LEFT} Scroll left a single scroll unit.<br>
     * {@link KeyCode#RIGHT RIGHT} Scroll right a single scroll unit.<br>
     * {@link KeyCode#PAGE_UP PAGE_UP} Scroll up a single scroll block.<br>
     * {@link KeyCode#PAGE_DOWN PAGE_DOWN} Scroll down a single scroll block.
     *
     * @see ScrollBar#getBlockIncrement()
     * @see ScrollBar#getUnitIncrement()
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        if (!consumed) {
            ScrollPane scrollPane = (ScrollPane)getComponent();

            int scrollTop = scrollPane.getScrollTop();
            int scrollLeft = scrollPane.getScrollLeft();

            if (keyCode == Keyboard.KeyCode.UP) {
                int newScrollTop = Math.max(scrollTop -
                    verticalScrollBar.getUnitIncrement(), 0);

                scrollPane.setScrollTop(newScrollTop);

                consumed = (newScrollTop != scrollTop);
            } else if (keyCode == Keyboard.KeyCode.DOWN) {
                int newScrollTop = Math.min(scrollTop +
                    verticalScrollBar.getUnitIncrement(), getMaxScrollTop());

                scrollPane.setScrollTop(newScrollTop);

                consumed = (newScrollTop != scrollTop);
            } else if (keyCode == Keyboard.KeyCode.LEFT) {
                int newScrollLeft = Math.max(scrollLeft -
                    horizontalScrollBar.getUnitIncrement(), 0);

                scrollPane.setScrollLeft(newScrollLeft);

                consumed = (newScrollLeft != scrollLeft);
            } else if (keyCode == Keyboard.KeyCode.RIGHT) {
                int newScrollLeft = Math.min(scrollLeft +
                    horizontalScrollBar.getUnitIncrement(), getMaxScrollLeft());

                scrollPane.setScrollLeft(newScrollLeft);

                consumed = (newScrollLeft != scrollLeft);
            } else if (keyCode == Keyboard.KeyCode.PAGE_UP) {
                int increment = verticalScrollBar.getBlockIncrement();
                int newScrollTop = Math.max(scrollTop - increment, 0);

                scrollPane.setScrollTop(newScrollTop);

                consumed = (newScrollTop != scrollTop);
            } else if (keyCode == Keyboard.KeyCode.PAGE_DOWN) {
                int increment = verticalScrollBar.getBlockIncrement();
                int newScrollTop = Math.min(scrollTop + increment, getMaxScrollTop());

                scrollPane.setScrollTop(newScrollTop);

                consumed = (newScrollTop != scrollTop);
            }
        }

        return consumed;
    }

    /**
     * Gets the maximum legal <tt>scrollTop</tt> value this this skin imposes.
     * This is the largest value possible that still shows as much of the view
     * component as it can.
     *
     * @return
     * The maximum scrollTop value
     */
    private int getMaxScrollTop() {
        int maxScrollTop = 0;

        ScrollPane scrollPane = (ScrollPane)getComponent();
        Component view = scrollPane.getView();

        if (view != null) {
            int viewHeight = view.getHeight();
            int columnHeaderHeight = 0;
            int horizontalScrollBarHeight = 0;
            int height = getHeight();

            Component columnHeader = scrollPane.getColumnHeader();
            if (columnHeader != null) {
                columnHeaderHeight = columnHeader.getHeight();
            }

            if (horizontalScrollBar.isVisible()) {
                horizontalScrollBarHeight = horizontalScrollBar.getHeight();
            }

            maxScrollTop = Math.max(viewHeight + columnHeaderHeight +
                horizontalScrollBarHeight - height, 0);
        }

        return maxScrollTop;
    }

    /**
     * Gets the maximum legal <tt>scrollLeft</tt> value this this skin imposes.
     * This is the largest value possible that still shows as much of the view
     * component as it can.
     *
     * @return
     * The maximum scrollLeft value
     */
    private int getMaxScrollLeft() {
        int maxScrollLeft = 0;

        ScrollPane scrollPane = (ScrollPane)getComponent();
        Component view = scrollPane.getView();

        if (view != null) {
            int viewWidth = view.getWidth();
            int rowHeaderWidth = 0;
            int verticalScrollBarWidth = 0;
            int width = getWidth();

            Component rowHeader = scrollPane.getRowHeader();
            if (rowHeader != null) {
                rowHeaderWidth = rowHeader.getWidth();
            }

            if (verticalScrollBar.isVisible()) {
                verticalScrollBarWidth = verticalScrollBar.getWidth();
            }

            maxScrollLeft = Math.max(viewWidth + rowHeaderWidth +
                verticalScrollBarWidth - width, 0);
        }

        return maxScrollLeft;
    }

    @Override
    public void layout() {
        ScrollPane scrollPane = (ScrollPane)getComponent();

        ScrollBarPolicy horizontalPolicy = scrollPane.getHorizontalScrollBarPolicy();
        ScrollBarPolicy verticalPolicy = scrollPane.getVerticalScrollBarPolicy();

        boolean fillWidthToCapacity = false;
        boolean fillHeightToCapacity = false;

        // The FILL_TO_CAPACITY policy means that we try to use AUTO, and only
        // if it ends up not being wide or tall enough do we use FILL

        if (horizontalPolicy == ScrollBarPolicy.FILL_TO_CAPACITY) {
            horizontalPolicy = ScrollBarPolicy.AUTO;
            fillWidthToCapacity = true;
        }

        if (verticalPolicy == ScrollBarPolicy.FILL_TO_CAPACITY) {
            verticalPolicy = ScrollBarPolicy.AUTO;
            fillHeightToCapacity = true;
        }

        layoutHelper(horizontalPolicy, verticalPolicy);

        Component view = scrollPane.getView();
        if (view != null && (fillWidthToCapacity || fillHeightToCapacity)) {
            // We assumed AUTO. Now we check our assumption to see if we
            // need to adjust it to use FILL
            boolean adjustWidth = false, adjustHeight = false;

            if (fillWidthToCapacity) {
                Component rowHeader = scrollPane.getRowHeader();
                int rowHeaderWidth = rowHeader != null ? rowHeader.getWidth() : 0;

                int verticalScrollBarWidth = verticalScrollBar.isVisible() ?
                    verticalScrollBar.getWidth() : 0;
                int minViewWidth = getWidth() - rowHeaderWidth - verticalScrollBarWidth;

                if (view.getWidth() < minViewWidth) {
                    horizontalPolicy = ScrollBarPolicy.FILL;
                    adjustWidth = true;
                }
            }

            if (fillHeightToCapacity) {
                Component columnHeader = scrollPane.getColumnHeader();
                int columnHeaderHeight = columnHeader != null ?
                    columnHeader.getHeight() : 0;

                int horizontalScrollBarHeight = horizontalScrollBar.isVisible() ?
                    horizontalScrollBar.getHeight() : 0;
                int minViewHeight = getHeight() - columnHeaderHeight -
                    horizontalScrollBarHeight;

                if (view.getHeight() < minViewHeight) {
                    verticalPolicy = ScrollBarPolicy.FILL;
                    adjustHeight = true;
                }
            }

            if (adjustWidth || adjustHeight) {
                layoutHelper(horizontalPolicy, verticalPolicy);
            }
        }

        cachedHorizontalScrollBarHeight = horizontalScrollBar.getHeight();
        cachedVerticalScrollBarWidth = verticalScrollBar.getWidth();
    }

    /**
     * Layout helper method that assumes that the <tt>FILL_TO_CAPACITY</tt>
     * scroll policy doesn't exist.
     *
     * @param horizontalPolicy
     * The assumed horizontal scroll policy; musn't be <tt>FILL_TO_CAPACITY</tt>
     *
     * @param verticalPolicy
     * The assumed vertical scroll policy; musn't be <tt>FILL_TO_CAPACITY</tt>
     */
    private void layoutHelper(ScrollBarPolicy horizontalPolicy,
        ScrollBarPolicy verticalPolicy) {
        ScrollPane scrollPane = (ScrollPane)getComponent();

        int width = getWidth();
        int height = getHeight();

        boolean constrainWidth = (horizontalPolicy == ScrollBarPolicy.FILL);
        boolean constrainHeight = (verticalPolicy == ScrollBarPolicy.FILL);

        Component view = scrollPane.getView();
        Component columnHeader = scrollPane.getColumnHeader();
        Component rowHeader = scrollPane.getRowHeader();
        Component corner = scrollPane.getCorner();

        int rowHeaderWidth = 0;
        if (rowHeader != null) {
            rowHeaderWidth = rowHeader.getPreferredWidth(-1);
        }

        int columnHeaderHeight = 0;
        if (columnHeader != null) {
            columnHeaderHeight = columnHeader.getPreferredHeight(-1);
        }

        int previousViewWidth, viewWidth = 0;
        int previousViewHeight, viewHeight = 0;
        int previousHorizontalScrollBarHeight, horizontalScrollBarHeight = cachedHorizontalScrollBarHeight;
        int previousVerticalScrollBarWidth, verticalScrollBarWidth = cachedVerticalScrollBarWidth;
        int i = 0;

        do {
            previousViewWidth = viewWidth;
            previousViewHeight = viewHeight;
            previousHorizontalScrollBarHeight = horizontalScrollBarHeight;
            previousVerticalScrollBarWidth = verticalScrollBarWidth;

            if (view != null) {
                if (constrainWidth && constrainHeight) {
                    viewWidth = Math.max
                        (width - rowHeaderWidth - verticalScrollBarWidth, 0);
                    viewHeight = Math.max
                        (height - columnHeaderHeight - horizontalScrollBarHeight, 0);
                } else if (constrainWidth) {
                    viewWidth = Math.max
                        (width - rowHeaderWidth - verticalScrollBarWidth, 0);
                    viewHeight = view.getPreferredHeight(viewWidth);
                } else if (constrainHeight) {
                    viewHeight = Math.max
                        (height - columnHeaderHeight - horizontalScrollBarHeight, 0);
                    viewWidth = view.getPreferredWidth(viewHeight);
                } else {
                    Dimensions viewPreferredSize = view.getPreferredSize();
                    viewWidth = viewPreferredSize.width;
                    viewHeight = viewPreferredSize.height;
                }
            }

            if (horizontalPolicy == ScrollBarPolicy.ALWAYS
                || (horizontalPolicy == ScrollBarPolicy.AUTO
                && viewWidth > width - rowHeaderWidth - verticalScrollBarWidth)) {
                horizontalScrollBarHeight = horizontalScrollBar.getPreferredHeight(-1);
            } else {
                horizontalScrollBarHeight = 0;
            }

            if (verticalPolicy == ScrollBarPolicy.ALWAYS
                || (verticalPolicy == ScrollBarPolicy.AUTO
                && viewHeight > height - columnHeaderHeight - horizontalScrollBarHeight)) {
                verticalScrollBarWidth = verticalScrollBar.getPreferredWidth(-1);
            } else {
                verticalScrollBarWidth = 0;
            }

            if (++i > 4) {
                // Infinite loop protection
                System.err.println("Breaking out of potential infinite loop");
                break;
            }
        } while (viewWidth != previousViewWidth
            || viewHeight != previousViewHeight
            || horizontalScrollBarHeight != previousHorizontalScrollBarHeight
            || verticalScrollBarWidth != previousVerticalScrollBarWidth);

        int scrollTop = scrollPane.getScrollTop();
        int scrollLeft = scrollPane.getScrollLeft();

        if (view != null) {
            view.setSize(viewWidth, viewHeight);
            view.setLocation(rowHeaderWidth - scrollLeft, columnHeaderHeight - scrollTop);
        }

        if (columnHeader != null) {
            columnHeader.setSize(viewWidth, columnHeaderHeight);
            columnHeader.setLocation(rowHeaderWidth - scrollLeft, 0);
        }

        if (rowHeader != null) {
            rowHeader.setSize(rowHeaderWidth, viewHeight);
            rowHeader.setLocation(0, columnHeaderHeight - scrollTop);
        }

        if (horizontalScrollBarHeight > 0) {
            horizontalScrollBar.setVisible(true);

            int horizontalScrollBarWidth = Math.max
               (width - rowHeaderWidth - verticalScrollBarWidth, 0);
            horizontalScrollBar.setSize(horizontalScrollBarWidth,
                horizontalScrollBarHeight);
            horizontalScrollBar.setLocation(rowHeaderWidth,
                height - horizontalScrollBarHeight);
        } else {
            horizontalScrollBar.setVisible(false);
        }

        if (verticalScrollBarWidth > 0) {
            verticalScrollBar.setVisible(true);

            int verticalScrollBarHeight = Math.max
               (height - columnHeaderHeight - horizontalScrollBarHeight, 0);
            verticalScrollBar.setSize(verticalScrollBarWidth,
                verticalScrollBarHeight);
            verticalScrollBar.setLocation(width - verticalScrollBarWidth,
                columnHeaderHeight);
        } else {
            verticalScrollBar.setVisible(false);
        }

        // Handle corner components

        if (columnHeaderHeight > 0
            && rowHeaderWidth > 0) {
            if (corner != null) {
                corner.setVisible(true);
                corner.setSize(rowHeaderWidth, columnHeaderHeight);
                corner.setLocation(0, 0);

                topLeftCorner.setVisible(false);
            } else {
                topLeftCorner.setVisible(true);
                topLeftCorner.setSize(rowHeaderWidth, columnHeaderHeight);
                topLeftCorner.setLocation(0, 0);
            }
        } else {
            if (corner != null) {
                corner.setVisible(false);
            }

            topLeftCorner.setVisible(false);
        }

        if (rowHeaderWidth > 0
            && horizontalScrollBarHeight > 0) {
            bottomLeftCorner.setVisible(true);
            bottomLeftCorner.setSize(rowHeaderWidth, horizontalScrollBarHeight);
            bottomLeftCorner.setLocation(0, height - horizontalScrollBarHeight);
        } else {
            bottomLeftCorner.setVisible(false);
        }

        if (verticalScrollBarWidth > 0
            && horizontalScrollBarHeight > 0) {
            bottomRightCorner.setVisible(true);
            bottomRightCorner.setSize(verticalScrollBarWidth, horizontalScrollBarHeight);
            bottomRightCorner.setLocation(width - verticalScrollBarWidth,
                height - horizontalScrollBarHeight);
        } else {
            bottomRightCorner.setVisible(false);
        }

        if (columnHeaderHeight > 0
            && verticalScrollBarWidth > 0) {
            topRightCorner.setVisible(true);
            topRightCorner.setSize(verticalScrollBarWidth, columnHeaderHeight);
            topRightCorner.setLocation(width - verticalScrollBarWidth, 0);
        } else {
            topRightCorner.setVisible(false);
        }

        // Perform bounds checking on the scrollTop and scrollLeft values,
        // and adjust them as necessary. Make sure to do this after we've laid
        // everything out, since our ViewPortListener methods rely on valid
        // sizes from our components.

        int maxScrollTop = getMaxScrollTop();
        if (scrollTop > maxScrollTop) {
            scrollPane.setScrollTop(maxScrollTop);
        }

        int maxScrollLeft = getMaxScrollLeft();
        if (scrollLeft > maxScrollLeft) {
            scrollPane.setScrollLeft(maxScrollLeft);
        }

        // Adjust the structure of our scroll bars. Make sure to do this after
        // we adjust the scrollTop and scrollLeft values; otherwise we might
        // try to set structure values that are out of bounds.

        int viewportWidth = Math.max(width - rowHeaderWidth - verticalScrollBarWidth, 0);
        horizontalScrollBar.setScope(0, viewWidth, Math.min(viewWidth, viewportWidth));
        horizontalScrollBar.setBlockIncrement(Math.max(1, viewportWidth - horizontalReveal));

        int viewportHeight = Math.max(height - columnHeaderHeight - horizontalScrollBarHeight, 0);
        verticalScrollBar.setScope(0, viewHeight, Math.min(viewHeight, viewportHeight));
        verticalScrollBar.setBlockIncrement(Math.max(1, viewportHeight - verticalReveal));
    }

    @Override
    public void setBackgroundPaint(Paint backgroundPaint) {
        super.setBackgroundPaint(backgroundPaint);

        optimizeScrolling = (backgroundPaint != null
            && backgroundPaint.getTransparency() == Transparency.OPAQUE);
    }

    public int getHorizontalIncrement() {
        return horizontalScrollBar.getUnitIncrement();
    }

    public void setHorizontalIncrement(int horizontalIncrement) {
        horizontalScrollBar.setUnitIncrement(horizontalIncrement);
    }

    public int getVerticalIncrement() {
        return verticalScrollBar.getUnitIncrement();
    }

    public void setVerticalIncrement(int verticalIncrement) {
        verticalScrollBar.setUnitIncrement(verticalIncrement);
    }

    public int getHorizontalReveal() {
        return horizontalReveal;
    }

    public void setHorizontalReveal(int horizontalReveal) {
        this.horizontalReveal = horizontalReveal;
    }

    public int getVerticalReveal() {
        return verticalReveal;
    }

    public void setVerticalReveal(int verticalReveal) {
        this.verticalReveal = verticalReveal;
    }

    private boolean isOptimizeScrolling() {
        boolean optimizeScrollingLocal = this.optimizeScrolling;

        if (optimizeScrollingLocal) {
            // Due to Sun bug #6293145, we cannot call copyArea if scaling is
            // applied to the graphics context.

            // Due to Sun bug #4033851, we cannot call copyArea if the display
            // host is obscured. For a full description of why this is the case,
            // see http://people.apache.org/~tvolkert/tests/scrolling/

            ScrollPane scrollPane = (ScrollPane)getComponent();
            ApplicationContext.DisplayHost displayHost = scrollPane.getDisplay().getDisplayHost();

            optimizeScrollingLocal = (displayHost.getScale() == 1
                && (DesktopApplicationContext.isActive()
                && displayHost.isDisplayable()));
        }

        return optimizeScrollingLocal;
    }

    // Viewport.Skin methods

    @Override
    public Bounds getViewportBounds() {
        int x = 0;
        int y = 0;
        int width = getWidth();
        int height = getHeight();

        ScrollPane scrollPane = (ScrollPane)getComponent();

        Component rowHeader = scrollPane.getRowHeader();
        if (rowHeader != null) {
            int rowHeaderWidth = rowHeader.getWidth();

            x += rowHeaderWidth;
            width -= rowHeaderWidth;
        }

        Component columnHeader = scrollPane.getColumnHeader();
        if (columnHeader != null) {
            int columnHeaderHeight = columnHeader.getHeight();

            y += columnHeaderHeight;
            height -= columnHeaderHeight;
        }

        if (horizontalScrollBar.isVisible()) {
            height -= horizontalScrollBar.getHeight();
        }

        if (verticalScrollBar.isVisible()) {
            width -= verticalScrollBar.getWidth();
        }

        return new Bounds(x, y, width, height);
    }

    // ScrollPaneListener methods

    @Override
    public void horizontalScrollBarPolicyChanged(ScrollPane scrollPane,
        ScrollBarPolicy previousHorizontalScrollBarPolicy) {
        invalidateComponent();
    }

    @Override
    public void verticalScrollBarPolicyChanged(ScrollPane scrollPane,
        ScrollBarPolicy previousVerticalScrollBarPolicy) {
        invalidateComponent();
    }

    @Override
    public void rowHeaderChanged(ScrollPane scrollPane, Component previousRowHeader) {
        invalidateComponent();
    }

    @Override
    public void columnHeaderChanged(ScrollPane scrollPane, Component previousColumnHeader) {
        invalidateComponent();
    }

    @Override
    public void cornerChanged(ScrollPane scrollPane, Component previousCorner) {
        invalidateComponent();
    }

    // ViewportListener methods

    @Override
    public void scrollTopChanged(Viewport viewport, int previousScrollTop) {
        // NOTE we don't invalidate the component here because we need only
        // reposition the view and row header. Invalidating would yield
        // the correct positioning, but it would do much more work than needed.
        ScrollPane scrollPane = (ScrollPane)viewport;

        Component view = scrollPane.getView();
        Component rowHeader = scrollPane.getRowHeader();
        Component columnHeader = scrollPane.getColumnHeader();

        int columnHeaderHeight = 0;
        if (columnHeader != null) {
            columnHeaderHeight = columnHeader.getHeight();
        }

        int scrollTop = scrollPane.getScrollTop();

        if (view != null
            && view.isShowing()
            && isOptimizeScrolling()) {
            Bounds blitArea = view.getVisibleArea();

            int blitX = blitArea.x + view.getX();
            int blitY = blitArea.y + view.getY();
            int blitWidth = blitArea.width;
            int blitHeight = blitArea.height;

            if (rowHeader != null) {
                // Blit the row header as well
                int rowHeaderWidth = rowHeader.getWidth();
                blitX -= rowHeaderWidth;
                blitWidth += rowHeaderWidth;
            }

            int deltaScrollTop = scrollTop - previousScrollTop;
            blitY += Math.max(deltaScrollTop, 0);
            blitHeight -= Math.abs(deltaScrollTop);

            Graphics2D graphics = scrollPane.getGraphics();
            graphics.copyArea(blitX, blitY, blitWidth, blitHeight, 0, -deltaScrollTop);

            scrollPane.setConsumeRepaint(true);

            try {
                view.setLocation(view.getX(), columnHeaderHeight - scrollTop);

                if (rowHeader != null) {
                    rowHeader.setLocation(0, columnHeaderHeight - scrollTop);
                }
            } finally {
                scrollPane.setConsumeRepaint(false);
            }

            boolean repaintAllViewport = scrollPane.isRepaintAllViewport();
            if (!repaintAllViewport) {
                scrollPane.repaint(blitX, (columnHeaderHeight + (deltaScrollTop > 0 ? blitHeight : 0)),
                    blitWidth, Math.abs(deltaScrollTop), true);
            } else {
                Bounds viewportBounds = getViewportBounds();
                scrollPane.repaint(viewportBounds.x, viewportBounds.y,
                    viewportBounds.width, viewportBounds.height, true);
            }

        } else {
            if (view != null) {
                view.setLocation(view.getX(), columnHeaderHeight - scrollTop);
            }

            if (rowHeader != null) {
                rowHeader.setLocation(0, columnHeaderHeight - scrollTop);
            }
        }

        if (scrollTop >= 0 && scrollTop <= getMaxScrollTop()) {
            verticalScrollBar.setValue(scrollTop);
        }
    }

    @Override
    public void scrollLeftChanged(Viewport viewport, int previousScrollLeft) {
        // NOTE we don't invalidate the component here because we need only
        // reposition the view and column header. Invalidating would yield
        // the correct positioning, but it would do much more work than needed.
        ScrollPane scrollPane = (ScrollPane)viewport;

        Component view = scrollPane.getView();
        Component rowHeader = scrollPane.getRowHeader();
        Component columnHeader = scrollPane.getColumnHeader();

        int rowHeaderWidth = 0;
        if (rowHeader != null) {
            rowHeaderWidth = rowHeader.getWidth();
        }

        int scrollLeft = scrollPane.getScrollLeft();

        if (view != null
            && view.isShowing()
            && isOptimizeScrolling()) {
            Bounds blitArea = view.getVisibleArea();

            int blitX = blitArea.x + view.getX();
            int blitY = blitArea.y + view.getY();
            int blitWidth = blitArea.width;
            int blitHeight = blitArea.height;

            if (columnHeader != null) {
                // Blit the column header as well
                int columnHeaderHeight = columnHeader.getHeight();
                blitY -= columnHeaderHeight;
                blitHeight += columnHeaderHeight;
            }

            int deltaScrollLeft = scrollLeft - previousScrollLeft;
            blitX += Math.max(deltaScrollLeft, 0);
            blitWidth -= Math.abs(deltaScrollLeft);

            Graphics2D graphics = scrollPane.getGraphics();
            graphics.copyArea(blitX, blitY, blitWidth, blitHeight, -deltaScrollLeft, 0);

            scrollPane.setConsumeRepaint(true);
            try {
                view.setLocation(rowHeaderWidth - scrollLeft, view.getY());

                if (columnHeader != null) {
                    columnHeader.setLocation(rowHeaderWidth - scrollLeft, 0);
                }
            } finally {
                scrollPane.setConsumeRepaint(false);
            }

            scrollPane.repaint(rowHeaderWidth + (deltaScrollLeft > 0 ? blitWidth : 0), blitY,
                Math.abs(deltaScrollLeft), blitHeight, true);
        } else {
            if (view != null) {
                view.setLocation(rowHeaderWidth - scrollLeft, view.getY());
            }

            if (columnHeader != null) {
                columnHeader.setLocation(rowHeaderWidth - scrollLeft, 0);
            }
        }

        if (scrollLeft >= 0 && scrollLeft <= getMaxScrollLeft()) {
            horizontalScrollBar.setValue(scrollLeft);
        }
    }

    @Override
    public void viewChanged(Viewport viewport, Component previousView) {
        invalidateComponent();
    }

    // ScrollBarValueListener methods

    @Override
    public void valueChanged(ScrollBar scrollBar, int previousValue) {
        ScrollPane scrollPane = (ScrollPane)getComponent();

        int value = scrollBar.getValue();

        if (scrollBar == horizontalScrollBar) {
            scrollPane.setScrollLeft(value);
        } else {
            scrollPane.setScrollTop(value);
        }
    }
}
