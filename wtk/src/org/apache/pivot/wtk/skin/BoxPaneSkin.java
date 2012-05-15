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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.BoxPaneListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Box pane skin.
 */
public class BoxPaneSkin extends ContainerSkin
    implements BoxPaneListener {
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
    private VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
    private Insets padding = Insets.NONE;
    private int spacing = 4;
    private boolean fill = false;

    @Override
    public void install(Component component) {
        super.install(component);

        BoxPane boxPane = (BoxPane)component;
        boxPane.getBoxPaneListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        BoxPane boxPane = (BoxPane)getComponent();

        int preferredWidth = 0;

        Orientation orientation = boxPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Include padding in constraint
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom), 0);
            }

            // Preferred width is the sum of the preferred widths of all components
            int j = 0;
            for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isVisible()) {
                    preferredWidth += component.getPreferredWidth(fill ? height : -1);
                    j++;
                }
            }

            // Include spacing
            if (j > 1) {
                preferredWidth += spacing * (j - 1);
            }
        } else {
            // Preferred width is the maximum preferred width of all components
            for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isVisible()) {
                    preferredWidth = Math.max(preferredWidth,
                        component.getPreferredWidth());
                }
            }
        }

        // Include left and right padding values
        preferredWidth += padding.left + padding.right;

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        BoxPane boxPane = (BoxPane)getComponent();

        int preferredHeight = 0;

        Orientation orientation = boxPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Preferred height is the maximum preferred height of all components
            for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isVisible()) {
                    preferredHeight = Math.max(preferredHeight,
                        component.getPreferredHeight());
                }
            }
        } else {
            // Include padding in constraint
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right), 0);
            }

            // Preferred height is the sum of the preferred heights of all components
            int j = 0;
            for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isVisible()) {
                    preferredHeight += component.getPreferredHeight(fill ? width : -1);
                    j++;
                }
            }

            // Include spacing
            if (j > 1) {
                preferredHeight += spacing * (j - 1);
            }
        }

        // Include top and bottom padding values
        preferredHeight += padding.top + padding.bottom;

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        BoxPane boxPane = (BoxPane)getComponent();

        int preferredWidth = 0;
        int preferredHeight = 0;

        switch (boxPane.getOrientation()) {
            case HORIZONTAL: {
                // Preferred width is the sum of the preferred widths of all components
                int j = 0;
                for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                    Component component = boxPane.get(i);

                    if (component.isVisible()) {
                        Dimensions preferredSize = component.getPreferredSize();
                        preferredWidth += preferredSize.width;
                        preferredHeight = Math.max(preferredSize.height, preferredHeight);
                        j++;
                    }
                }

                // Include spacing
                if (j > 1) {
                    preferredWidth += spacing * (j - 1);
                }

                break;
            }

            case VERTICAL: {
                // Preferred height is the sum of the preferred heights of all components
                int j = 0;
                for (int i = 0, n = boxPane.getLength(); i < n; i++) {
                    Component component = boxPane.get(i);

                    if (component.isVisible()) {
                        Dimensions preferredSize = component.getPreferredSize();
                        preferredWidth = Math.max(preferredSize.width, preferredWidth);
                        preferredHeight += preferredSize.height;
                        j++;
                    }
                }

                // Include spacing
                if (j > 1) {
                    preferredHeight += spacing * (j - 1);
                }

                break;
            }
        }

        // Include padding
        preferredWidth += padding.left + padding.right;
        preferredHeight += padding.top + padding.bottom;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        BoxPane boxPane = (BoxPane)getComponent();

        int baseline = -1;
        int contentHeight = 0;

        switch (boxPane.getOrientation()) {
            case HORIZONTAL: {
                if (fill) {
                    int clientHeight = Math.max(height - (padding.top + padding.bottom), 0);

                    for (Component component : boxPane) {
                        if (component.isVisible()) {
                            int componentWidth = component.getPreferredWidth(clientHeight);
                            baseline = Math.max(baseline, component.getBaseline(componentWidth, clientHeight));
                        }
                    }
                } else {
                    contentHeight = 0;
                    for (Component component : boxPane) {
                        if (component.isVisible()) {
                            contentHeight = Math.max(contentHeight, component.getPreferredHeight());
                        }
                    }

                    for (Component component : boxPane) {
                        if (component.isVisible()) {
                            Dimensions size = component.getPreferredSize();

                            int componentBaseline = component.getBaseline(size.width, size.height);

                            if (componentBaseline != -1) {
                                switch (verticalAlignment) {
                                    case CENTER: {
                                        componentBaseline += (contentHeight - size.height) / 2;
                                        break;
                                    }

                                    case BOTTOM: {
                                        componentBaseline += contentHeight - size.height;
                                        break;
                                    }
                                    case TOP:
                                        break;
                                }
                            }

                            baseline = Math.max(baseline, componentBaseline);
                        }
                    }
                }

                break;
            }

            case VERTICAL: {
                int clientWidth = Math.max(width - (padding.left + padding.right), 0);

                for (Component component : boxPane) {
                    if (component.isVisible()) {
                        Dimensions size;
                        if (fill) {
                            size = new Dimensions(clientWidth,
                                component.getPreferredHeight(clientWidth));
                        } else {
                            size = component.getPreferredSize();
                        }

                        if (baseline == -1) {
                            baseline = component.getBaseline(size.width, size.height);
                            if (baseline != -1) {
                                baseline += contentHeight;
                            }
                        }

                        contentHeight += size.height + spacing;
                    }
                }

                contentHeight -= spacing;

                break;
            }
        }

        if (baseline != -1) {
            if (fill) {
                baseline += padding.top;
            } else {
                switch (verticalAlignment) {
                    case TOP: {
                        baseline += padding.top;
                        break;
                    }

                    case CENTER: {
                        baseline += (height - contentHeight) / 2;
                        break;
                    }

                    case BOTTOM: {
                        baseline += height - (contentHeight + padding.bottom);
                        break;
                    }
                }
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        BoxPane boxPane = (BoxPane)getComponent();
        int n = boxPane.getLength();

        int width = getWidth();
        int height = getHeight();

        Orientation orientation = boxPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            int preferredWidth = getPreferredWidth(fill ? height : -1);

            // Determine the starting x-coordinate
            int x = 0;

            switch (horizontalAlignment) {
                case CENTER: {
                    x = (width - preferredWidth) / 2;
                    break;
                }

                case RIGHT: {
                    x = width - preferredWidth;
                    break;
                }
                case LEFT:
                    break;
            }

            x += padding.left;

            // Lay out the components
            for (int i = 0; i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isVisible()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int y = 0;

                    if (fill) {
                        componentHeight = Math.max(height - (padding.top
                            + padding.bottom), 0);

                        componentWidth = component.getPreferredWidth(componentHeight);
                    } else {
                        Dimensions preferredComponentSize = component.getPreferredSize();
                        componentWidth = preferredComponentSize.width;
                        componentHeight = preferredComponentSize.height;
                    }

                    switch (verticalAlignment) {
                        case TOP: {
                            y = padding.top;
                            break;
                        }

                        case CENTER: {
                            y = (height - componentHeight) / 2;
                            break;
                        }

                        case BOTTOM: {
                            y = height - padding.bottom - componentHeight;
                            break;
                        }
                    }

                    // Set the component's size and position
                    component.setSize(componentWidth, componentHeight);
                    component.setLocation(x, y);

                    // Increment the x-coordinate
                    x += componentWidth + spacing;
                }
            }
        } else {
            int preferredHeight = getPreferredHeight(fill ? width : -1);

            // Determine the starting y-coordinate
            int y = 0;

            switch (verticalAlignment) {
                case CENTER: {
                    y = (height - preferredHeight) / 2;
                    break;
                }

                case BOTTOM: {
                    y = height - preferredHeight;
                    break;
                }
                case TOP:
                    break;
            }

            y += padding.top;

            // Lay out the components
            for (int i = 0; i < n; i++) {
                Component component = boxPane.get(i);

                if (component.isVisible()) {
                    int componentWidth = 0;
                    int componentHeight = 0;
                    int x = 0;

                    if (fill) {
                        componentWidth = Math.max(width - (padding.left
                            + padding.right), 0);

                        componentHeight = component.getPreferredHeight(componentWidth);
                    } else {
                        Dimensions preferredComponentSize = component.getPreferredSize();
                        componentWidth = preferredComponentSize.width;
                        componentHeight = preferredComponentSize.height;
                    }

                    switch (horizontalAlignment) {
                        case LEFT: {
                            x = padding.left;
                            break;
                        }

                        case CENTER: {
                            x = (width - componentWidth) / 2;
                            break;
                        }

                        case RIGHT: {
                            x = width - padding.right - componentWidth;
                            break;
                        }
                    }

                    // Set the component's size and position
                    component.setSize(componentWidth, componentHeight);
                    component.setLocation(x, y);

                    // Increment the y-coordinate
                    y += componentHeight + spacing;
                }
            }
        }
    }

    /**
     * Returns the horizontal alignment of the BoxPane's components within the pane.
     */
    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the BoxPane's components within the pane.
     *
     * <p>If the orientation of the pane is HORIZONTAL, this means the collective alignment
     * all the components as group, which are still laid out close together according
     * to the value of the <code>spacing</code> style.
     *
     * <p>If the orientation of the pane is VERTICAL, this means the alignment of each
     * individual component within the pane.  It has no effect if the <code>fill</code>
     * style is true.
     */
    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        invalidateComponent();
    }

    /**
     * Returns the vertical alignment of the BoxPane's components within the pane.
     */
    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the BoxPane's components within the pane.
     *
     * <p>If the orientation of the pane is VERTICAL, this means the collective alignment
     * all the components as group, which are still laid out close together according
     * to the value of the <code>spacing</code> style.
     *
     * <p>If the orientation of the pane is HORIZONTAL, this means the alignment of each
     * individual component within the pane.  It has no effect if the <code>fill</code>
     * style is true.
     */
    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
        invalidateComponent();
    }

    /**
     * Returns the amount of space between the edge of the BoxPane and its components.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the BoxPane and its components.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the BoxPane and its components.
     *
     * @param padding A dictionary with keys in the set {left, top, bottom, right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the BoxPane and its components,
     * uniformly on all four edges.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the BoxPane and its components,
     * uniformly on all four edges.
     */
    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space to leave between the edge of the BoxPane and its components.
     *
     * @param padding A string containing an integer or a JSON dictionary with keys
     * left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    /**
     * Returns the amount of space between the BoxPane's components.
     */
    public int getSpacing() {
        return spacing;
    }

    /**
     * Sets the amount of space to leave between the BoxPane's components.
     */
    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("spacing is negative.");
        }
        this.spacing = spacing;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the BoxPane's components.
     */
    public final void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }

    /**
     * Returns a value indicating whether the BoxPane's components fill the
     * available space in the pane in the dimension orthogonal to its orientation.
     */
    public boolean getFill() {
        return fill;
    }

    /**
     * Sets whether the BoxPane's components fill to the edges of the pane.
     *
     * @param fill If <b>true</b>, the components are given all the available space
     * in the dimension orthogonal to the pane's orientation (e.g., vertically
     * in a BoxPane with orientation=horizontal).  It has no effect on the layout
     * of components in the direction of the pane's orientation, which are always
     * given exactly their preferred size.
     * If <b>false</b>, the pane's components are laid out to their preferred size,
     * regardless of the size of the BoxPane.  Their alignment along the axis orthogonal
     * to the pane's orientation is controlled by the corresponding alignment style
     * (e.g., verticalAlignment in a BoxPane with orientation=horizontal).
     */
    public void setFill(boolean fill) {
        this.fill = fill;
        invalidateComponent();
    }

    // Box pane events
    @Override
    public void orientationChanged(BoxPane boxPane) {
        invalidateComponent();
    }
}
