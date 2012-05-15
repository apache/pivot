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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FillPane;
import org.apache.pivot.wtk.FillPaneListener;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;

/**
 * Fill pane skin.
 */
public class FillPaneSkin extends ContainerSkin
    implements FillPaneListener {
    private Insets padding = Insets.NONE;
    private int spacing = 4;

    @Override
    public void install(Component component) {
        super.install(component);

        FillPane fillPane = (FillPane)component;
        fillPane.getFillPaneListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        FillPane fillPane = (FillPane)getComponent();

        int preferredWidth = 0;

        Orientation orientation = fillPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Include padding in constraint
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom), 0);
            }

            // Preferred width is the sum of the preferred widths of all components
            int j = 0;
            for (int i = 0, n = fillPane.getLength(); i < n; i++) {
                Component component = fillPane.get(i);

                if (component.isVisible()) {
                    preferredWidth += component.getPreferredWidth(height);
                    j++;
                }
            }

            // Include spacing
            if (j > 1) {
                preferredWidth += spacing * (j - 1);
            }
        } else {
            // Preferred width is the maximum preferred width of all components
            for (int i = 0, n = fillPane.getLength(); i < n; i++) {
                Component component = fillPane.get(i);

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
        FillPane fillPane = (FillPane)getComponent();

        int preferredHeight = 0;

        Orientation orientation = fillPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Preferred height is the maximum preferred height of all components
            for (int i = 0, n = fillPane.getLength(); i < n; i++) {
                Component component = fillPane.get(i);

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
            for (int i = 0, n = fillPane.getLength(); i < n; i++) {
                Component component = fillPane.get(i);

                if (component.isVisible()) {
                    preferredHeight += component.getPreferredHeight(width);
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
        FillPane fillPane = (FillPane)getComponent();

        int preferredWidth = 0;
        int preferredHeight = 0;

        switch (fillPane.getOrientation()) {
            case HORIZONTAL: {
                // Preferred width is the sum of the preferred widths of all components
                int j = 0;
                for (int i = 0, n = fillPane.getLength(); i < n; i++) {
                    Component component = fillPane.get(i);

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
                for (int i = 0, n = fillPane.getLength(); i < n; i++) {
                    Component component = fillPane.get(i);

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
        FillPane fillPane = (FillPane)getComponent();

        int baseline = -1;
        int contentHeight = 0;

        switch (fillPane.getOrientation()) {
            case HORIZONTAL: {
                int clientHeight = Math.max(height - (padding.top + padding.bottom), 0);

                for (Component component : fillPane) {
                    if (component.isVisible()) {
                        int componentWidth = component.getPreferredWidth(clientHeight);
                        baseline = Math.max(baseline, component.getBaseline(componentWidth, clientHeight));
                    }
                }

                break;
            }

            case VERTICAL: {
                int clientWidth = Math.max(width - (padding.left + padding.right), 0);

                for (Component component : fillPane) {
                    if (component.isVisible()) {
                        Dimensions size;
                        size = new Dimensions(clientWidth,
                            component.getPreferredHeight(clientWidth));

                        if (baseline == -1) {
                            baseline = component.getBaseline(size.width, size.height);
                            if (baseline != -1) {
                                baseline += contentHeight;
                            }
                        }

                        contentHeight += size.height + spacing;
                    }
                }

                break;
            }
        }

        if (baseline != -1) {
            baseline += padding.top;
        }

        return baseline;
    }

    @Override
    public void layout() {
        FillPane fillPane = (FillPane)getComponent();
        // n is the number of 'visible' components
        // len is the total number of components
        int n = 0;
        int len = fillPane.getLength();
        for (int i = 0; i < len; i++) {
            Component component = fillPane.get(i);
            if (component.isVisible()) {
                n++;
            }
        }

        int width = getWidth();
        int height = getHeight();
        if (width <= 0)
            width = getPreferredWidth(-1);
        if (height <= 0)
            height = getPreferredHeight(-1);
        Orientation orientation = fillPane.getOrientation();
        if (orientation == Orientation.HORIZONTAL) {
            // Determine the starting x-coordinate
            int x = padding.left;
            int totalWidth = width - (padding.left + padding.right);
            if (n > 1)
                totalWidth -= spacing * (n - 1);
            int dividedWidth = n == 0 ? 0 : totalWidth / n;
            int leftoverWidth = totalWidth - (dividedWidth * n);

            // Lay out the components
            for (int i = 0, j = 0; i < len; i++) {
                Component component = fillPane.get(i);

                if (component.isVisible()) {
                    int componentWidth = dividedWidth;
                    if (j == n - 1)
                        componentWidth += leftoverWidth;

                    int componentHeight = Math.max(height - (padding.top
                        + padding.bottom), 0);

                    int y = padding.top;

                    // Set the component's size and position
                    component.setSize(componentWidth, componentHeight);
                    component.setLocation(x, y);

                    // Increment the x-coordinate
                    x += componentWidth + spacing;
                    j++;
                }
            }
        } else {
            // Determine the starting y-coordinate
            int y = padding.top;
            int totalHeight = height - (padding.top + padding.bottom);
            if (n > 1)
                totalHeight -= spacing * (n - 1);
            int dividedHeight = n == 0 ? 0 : totalHeight / n;
            int leftoverHeight = totalHeight - (dividedHeight * n);

            // Lay out the components
            for (int i = 0, j = 0; i < len; i++) {
                Component component = fillPane.get(i);

                if (component.isVisible()) {
                    int componentHeight = dividedHeight;
                    if (j == n - 1)
                        componentHeight += leftoverHeight;

                    int componentWidth = Math.max(width - (padding.left
                        + padding.right), 0);

                    int x = padding.left;

                    // Set the component's size and position
                    component.setSize(componentWidth, componentHeight);
                    component.setLocation(x, y);

                    // Increment the y-coordinate
                    y += componentHeight + spacing;
                    j++;
                }
            }
        }
    }

    /**
     * Returns the amount of space between the edge of the FillPane and its components.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the FillPane and its components.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the FillPane and its components.
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
     * Sets the amount of space to leave between the edge of the FillPane and its components,
     * uniformly on all four edges.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the FillPane and its components,
     * uniformly on all four edges.
     */
    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space to leave between the edge of the FillPane and its components.
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

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("spacing is negative.");
        }
        this.spacing = spacing;
        invalidateComponent();
    }

    public final void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }

    // Fill pane events
    @Override
    public void orientationChanged(FillPane fillPane) {
        invalidateComponent();
    }
}
