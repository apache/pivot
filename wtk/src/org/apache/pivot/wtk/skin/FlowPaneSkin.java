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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;

/**
 * Flow pane skin.
 */
public class FlowPaneSkin extends ContainerSkin {
    private HorizontalAlignment alignment;
    private Insets padding;
    private int horizontalSpacing;
    private int verticalSpacing;
    private boolean alignToBaseline;

    public FlowPaneSkin() {
        alignment = HorizontalAlignment.LEFT;
        padding = Insets.NONE;
        horizontalSpacing = 2;
        verticalSpacing = 2;
        alignToBaseline = true;
    }

    @Override
    public int getPreferredWidth(final int height) {
        FlowPane flowPane = (FlowPane) getComponent();

        int preferredWidth = 0;

        // Preferred width is the sum of the preferred widths of all components
        // (height constraint is ignored)
        int j = 0;
        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                preferredWidth += component.getPreferredWidth();
                j++;
            }
        }

        // Include spacing
        if (j > 1) {
            preferredWidth += horizontalSpacing * (j - 1);
        }

        // Include left and right padding values
        preferredWidth += padding.getWidth();

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(final int width) {
        FlowPane flowPane = (FlowPane) getComponent();

        int preferredHeight;

        if (width == -1) {
            if (alignToBaseline) {
                // Delegate to preferred size calculations
                Dimensions preferredSize = getPreferredSize();
                preferredHeight = preferredSize.height;
            } else {
                // Preferred height is the maximum preferred height of all
                // components
                preferredHeight = 0;

                for (int i = 0, n = flowPane.getLength(); i < n; i++) {
                    Component component = flowPane.get(i);

                    if (component.isVisible()) {
                        preferredHeight = Math.max(preferredHeight, component.getPreferredHeight());
                    }
                }
            }
        } else {
            // Break the components into multiple rows
            preferredHeight = 0;

            int contentWidth = Math.max(width - padding.getWidth(), 0);

            int rowCount = 0;

            int rowWidth = 0;
            int rowAscent = 0;
            int rowDescent = 0;

            for (int i = 0, n = flowPane.getLength(); i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isVisible()) {
                    Dimensions size = component.getPreferredSize();

                    if (rowWidth + size.width > contentWidth && rowWidth > 0) {
                        // The component is too big to fit in the remaining
                        // space,
                        // and it is not the only component in this row; wrap
                        preferredHeight += rowAscent + rowDescent;

                        rowCount++;
                        rowWidth = 0;
                        rowAscent = 0;
                        rowDescent = 0;
                    }

                    rowWidth += size.width + horizontalSpacing;

                    if (alignToBaseline) {
                        int baseline = component.getBaseline(size.width, size.height);
                        rowAscent = Math.max(rowAscent, baseline);
                        rowDescent = Math.max(rowDescent, size.height - baseline);
                    } else {
                        rowAscent = Math.max(rowAscent, size.height);
                    }
                }
            }

            // Add the last row
            int lastRowHeight = rowAscent + rowDescent;
            if (lastRowHeight > 0) {
                preferredHeight += lastRowHeight;
                rowCount++;
            }

            // Include spacing
            if (rowCount > 0) {
                preferredHeight += verticalSpacing * (rowCount - 1);
            }
        }

        // Include top and bottom padding values
        preferredHeight += padding.getHeight();

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        FlowPane flowPane = (FlowPane) getComponent();

        int preferredWidth = 0;

        int ascent = 0;
        int descent = 0;

        int j = 0;
        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                Dimensions size = component.getPreferredSize();
                preferredWidth += size.width;

                if (alignToBaseline) {
                    int baseline = component.getBaseline(size.width, size.height);
                    ascent = Math.max(ascent, baseline);
                    descent = Math.max(descent, size.height - baseline);
                } else {
                    ascent = Math.max(ascent, size.height);
                }

                j++;
            }
        }

        // Include horizontal spacing
        if (j > 1) {
            preferredWidth += horizontalSpacing * (j - 1);
        }

        // Include padding
        preferredWidth += padding.getWidth();

        return new Dimensions(preferredWidth, ascent + descent + padding.getHeight());
    }

    @Override
    public int getBaseline(final int width, final int height) {
        FlowPane flowPane = (FlowPane) getComponent();

        int baseline = -1;

        if (alignToBaseline) {
            int contentWidth = Math.max(width - padding.getWidth(), 0);

            // Break the components into multiple rows, and calculate the
            // baseline of the first row
            int rowWidth = 0;
            for (int i = 0, n = flowPane.getLength(); i < n; i++) {
                Component component = flowPane.get(i);

                if (component.isVisible()) {
                    Dimensions size = component.getPreferredSize();

                    if (rowWidth + size.width > contentWidth && rowWidth > 0) {
                        // The component is too big to fit in the remaining space,
                        // and it is not the only component in this row; wrap
                        break;
                    }

                    baseline = Math.max(baseline, component.getBaseline(size.width, size.height));
                    rowWidth += size.width + horizontalSpacing;
                }
            }

            // Include top padding value
            baseline += padding.top;
        }

        return baseline;
    }

    @Override
    public void layout() {
        FlowPane flowPane = (FlowPane) getComponent();
        int width = getWidth();
        int contentWidth = Math.max(width - padding.getWidth(), 0);

        // Break the components into multiple rows
        ArrayList<ArrayList<Component>> rows = new ArrayList<>();

        ArrayList<Component> row = new ArrayList<>();
        int rowWidth = 0;

        for (int i = 0, n = flowPane.getLength(); i < n; i++) {
            Component component = flowPane.get(i);

            if (component.isVisible()) {
                Dimensions componentSize = component.getPreferredSize();
                component.setSize(componentSize);

                if (rowWidth + componentSize.width > contentWidth && rowWidth > 0) {
                    // The component is too big to fit in the remaining space,
                    // and it is not the only component in this row
                    rows.add(row);
                    row = new ArrayList<>();
                    rowWidth = 0;
                }

                // Add the component to the row
                row.add(component);
                rowWidth += componentSize.width + horizontalSpacing;
            }
        }

        // Add the last row
        if (row.getLength() > 0) {
            rows.add(row);
        }

        // Lay out the rows
        int rowY = padding.top;

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            row = rows.get(i);

            // Determine the row dimensions
            rowWidth = 0;
            int rowHeight = 0;
            int baseline = -1;
            for (Component component : row) {
                rowWidth += component.getWidth();
                rowHeight = Math.max(rowHeight, component.getHeight());
                baseline = Math.max(baseline,
                    component.getBaseline(component.getWidth(), component.getHeight()));
            }

            rowWidth += horizontalSpacing * (row.getLength() - 1);

            int x = 0;
            switch (alignment) {
                case LEFT:
                    x = padding.left;
                    break;
                case CENTER:
                    x = (width - rowWidth) / 2;
                    break;
                case RIGHT:
                    x = width - rowWidth - padding.right;
                    break;
                default:
                    break;
            }

            for (Component component : row) {
                int y;
                int componentBaseline = component.getBaseline(component.getWidth(),
                    component.getHeight());
                if (alignToBaseline && baseline != -1 && componentBaseline != -1) {
                    // Align to baseline
                    y = baseline - componentBaseline;
                } else {
                    // Align to bottom
                    y = rowHeight - component.getHeight();
                }

                component.setLocation(x, y + rowY);
                x += (component.getWidth() + horizontalSpacing);
            }

            rowY += (rowHeight + verticalSpacing);
        }
    }

    public HorizontalAlignment getAlignment() {
        return alignment;
    }

    public void setAlignment(final HorizontalAlignment alignment) {
        Utils.checkNull(alignment, "alignment");

        this.alignment = alignment;
        invalidateComponent();
    }

    /**
     * @return The amount of space between the edge of the FlowPane and its
     * components.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the FlowPane and
     * its components.
     *
     * @param padding The individual padding values for each edge.
     */
    public void setPadding(final Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the FlowPane and
     * its components.
     *
     * @param padding A dictionary with keys in the set {top, left, bottom, right}.
     */
    public final void setPadding(final Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the FlowPane and
     * its components.
     *
     * @param padding A sequence with values in the order [top, left, bottom, right].
     */
    public final void setPadding(final Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the FlowPane and
     * its components, uniformly on all four edges.
     *
     * @param padding The single padding value for all four sides.
     */
    public final void setPadding(final int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the FlowPane and
     * its components, uniformly on all four edges.
     *
     * @param padding The single padding value for all four sides.
     */
    public final void setPadding(final Number padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the FlowPane and
     * its components.
     *
     * @param padding A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setPadding(final String padding) {
        setPadding(Insets.decode(padding));
    }

    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    public void setHorizontalSpacing(final int horizontalSpacing) {
        Utils.checkNonNegative(horizontalSpacing, "horizontalSpacing");

        this.horizontalSpacing = horizontalSpacing;
        invalidateComponent();
    }

    public final void setHorizontalSpacing(final Number horizontalSpacing) {
        Utils.checkNull(horizontalSpacing, "horizontalSpacing");

        setHorizontalSpacing(horizontalSpacing.intValue());
    }

    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    public void setVerticalSpacing(final int verticalSpacing) {
        Utils.checkNonNegative(verticalSpacing, "verticalSpacing");

        this.verticalSpacing = verticalSpacing;
        invalidateComponent();
    }

    public final void setVerticalSpacing(final Number verticalSpacing) {
        Utils.checkNull(verticalSpacing, "verticalSpacing");

        setVerticalSpacing(verticalSpacing.intValue());
    }

    public boolean getAlignToBaseline() {
        return alignToBaseline;
    }

    public void setAlignToBaseline(final boolean alignToBaseline) {
        this.alignToBaseline = alignToBaseline;
        invalidateComponent();
    }
}
