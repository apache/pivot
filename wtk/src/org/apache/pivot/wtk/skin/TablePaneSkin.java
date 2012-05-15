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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TablePaneAttributeListener;
import org.apache.pivot.wtk.TablePaneListener;

/**
 * Table pane skin.
 */
public class TablePaneSkin extends ContainerSkin implements TablePane.Skin,
    TablePaneListener, TablePaneAttributeListener {
    private Insets padding = Insets.NONE;
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private boolean showHorizontalGridLines = false;
    private boolean showVerticalGridLines = false;
    private Color horizontalGridColor = Color.BLACK;
    private Color verticalGridColor = Color.BLACK;
    private Color highlightBackgroundColor = Color.GRAY;

    private int[] columnWidths = null;
    private int[] rowHeights = null;

    @Override
    public void install(Component component) {
        super.install(component);

        TablePane tablePane = (TablePane)component;
        tablePane.getTablePaneListeners().add(this);
        tablePane.getTablePaneAttributeListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        TablePane tablePane = (TablePane)getComponent();
        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int[] columnWidthsLocal = new int[columnCount];
        int[] relativeWeights = new int[columnCount];
        boolean[] defaultWidthColumns = new boolean[columnCount];

        int totalRelativeWeight = 0;

        // First, we calculate the base widths of the columns, giving relative
        // columns their preferred width

        for (int i = 0; i < columnCount; i++) {
            TablePane.Column column = columns.get(i);
            int columnWidth = column.getWidth();
            boolean isRelative = column.isRelative();

            defaultWidthColumns[i] = (columnWidth < 0);

            if (isRelative) {
                relativeWeights[i] = columnWidth;
                totalRelativeWeight += columnWidth;
            }

            if (columnWidth < 0 || isRelative) {
                columnWidth = getPreferredColumnWidth(i);
            }

            columnWidthsLocal[i] = columnWidth;
        }

        // Next, we adjust the widths of the relative columns upwards where
        // necessary to reconcile their widths relative to one another while
        // ensuring that they still get at least their preferred width

        if (totalRelativeWeight > 0) {
            int totalRelativeWidth = 0;

            // Calculate the total relative width after the required upward
            // adjustments

            for (int i = 0; i < columnCount; i++) {
                int columnWidth = columnWidthsLocal[i];
                int relativeWeight = relativeWeights[i];

                if (relativeWeight > 0) {
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    totalRelativeWidth = Math.max(totalRelativeWidth,
                        (int)(columnWidth / weightPercentage));
                }
            }

            // Perform the upward adjustments using the total relative width

            for (int i = 0; i < columnCount; i++) {
                int relativeWeight = relativeWeights[i];

                if (relativeWeight > 0) {
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    columnWidthsLocal[i] = (int)(weightPercentage * totalRelativeWidth);
                }
            }
        }

        // Finally, we account for spanning cells, which have been ignored thus
        // far. If any spanned cell is default-width (including relative width
        // columns), then we ensure that the sum of the widths of the spanned
        // cells is enough to satisfy the preferred width of the spanning
        // content

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    int columnSpan = TablePane.getColumnSpan(component);

                    if (columnSpan > 1) {
                        // We might need to adjust column widths to accomodate
                        // this spanning cell. First, we find out if any of the
                        // spanned cells are default width and how much space
                        // we've allocated thus far for those cells

                        int spannedDefaultWidthCellCount = 0;
                        int spannedRelativeWeight = 0;
                        int spannedWidth = 0;

                        for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                            if (defaultWidthColumns[j + k]) {
                                spannedDefaultWidthCellCount++;
                            }

                            spannedRelativeWeight += relativeWeights[j + k];
                            spannedWidth += columnWidthsLocal[j + k];
                        }

                        if (spannedRelativeWeight > 0
                            || spannedDefaultWidthCellCount > 0) {
                            int rowHeight = row.isRelative() ? -1 : row.getHeight();
                            int componentPreferredWidth = component.getPreferredWidth(rowHeight);

                            if (componentPreferredWidth > spannedWidth) {
                                // The component's preferred width is larger
                                // than the width we've allocated thus far, so
                                // an adjustment is necessary
                                int adjustment = componentPreferredWidth - spannedWidth;

                                if (spannedRelativeWeight > 0) {
                                    // We'll distribute the adjustment across
                                    // the spanned relative columns and adjust
                                    // other relative column widths to keep all
                                    // relative column widths reconciled
                                    float unitAdjustment = adjustment /
                                        (float)spannedRelativeWeight;

                                    for (int k = 0; k < columnCount; k++) {
                                        int relativeWeight = relativeWeights[k];

                                        if (relativeWeight > 0) {
                                            int columnAdjustment =
                                                Math.round(unitAdjustment * relativeWeight);

                                            columnWidthsLocal[k] += columnAdjustment;
                                        }
                                    }
                                } else {
                                    // We'll distribute the adjustment evenly
                                    // among the default-width columns
                                    for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                                        if (defaultWidthColumns[j + k]) {
                                            int columnAdjustment = adjustment /
                                                spannedDefaultWidthCellCount;

                                            columnWidthsLocal[j + k] += columnAdjustment;

                                            // Adjust these to avoid rounding errors
                                            adjustment -= columnAdjustment;
                                            spannedDefaultWidthCellCount--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // The preferred width of the table pane is the sum of the column
        // widths, plus padding and spacing

        boolean[][] occupiedCells = getOccupiedCells();
        int visibleColumnCount = 0;

        int preferredWidth = padding.left + padding.right;

        for (int j = 0; j < columnCount; j++) {
            boolean columnVisible = false;

            for (int i = 0; i < rowCount; i++) {
                if (occupiedCells[i][j]) {
                    columnVisible = true;
                    break;
                }
            }

            if (columnVisible) {
                preferredWidth += columnWidthsLocal[j];
                visibleColumnCount++;
            }
        }

        if (visibleColumnCount > 1) {
            preferredWidth += (visibleColumnCount - 1) * horizontalSpacing;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        TablePane tablePane = (TablePane)getComponent();
        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int[] rowHeightsLocal = new int[rowCount];
        int[] relativeWeights = new int[rowCount];
        boolean[] defaultHeightRows = new boolean[rowCount];

        int totalRelativeWeight = 0;

        if (width < 0) {
            width = getPreferredWidth(-1);
        }

        int[] columnWidthsLocal = getColumnWidths(width);

        // First, we calculate the base heights of the rows, giving relative
        // rows their preferred height

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);
            int rowHeight = row.getHeight();
            boolean isRelative = row.isRelative();

            defaultHeightRows[i] = (rowHeight < 0);

            if (isRelative) {
                relativeWeights[i] = rowHeight;
                totalRelativeWeight += rowHeight;
            }

            if (rowHeight < 0 || isRelative) {
                rowHeight = getPreferredRowHeight(i, columnWidthsLocal);
            }

            rowHeightsLocal[i] = rowHeight;
        }

        // Next, we adjust the heights of the relative rows upwards where
        // necessary to reconcile their heights relative to one another while
        // ensuring that they still get at least their preferred height

        if (totalRelativeWeight > 0) {
            int totalRelativeHeight = 0;

            // Calculate the total relative height after the required upward
            // adjustments

            for (int i = 0; i < rowCount; i++) {
                int rowHeight = rowHeightsLocal[i];
                int relativeWeight = relativeWeights[i];

                if (relativeWeight > 0) {
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    totalRelativeHeight = Math.max(totalRelativeHeight,
                        (int)(rowHeight / weightPercentage));
                }
            }

            // Perform the upward adjustments using the total relative height

            for (int i = 0; i < rowCount; i++) {
                int relativeWeight = relativeWeights[i];

                if (relativeWeight > 0) {
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    rowHeightsLocal[i] = (int)(weightPercentage * totalRelativeHeight);
                }
            }
        }

        // Finally, we account for spanning cells, which have been ignored thus
        // far. If any spanned cell is default-height (including relative height
        // rows), then we ensure that the sum of the heights of the spanned
        // cells is enough to satisfy the preferred height of the spanning
        // content

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    int rowSpan = TablePane.getRowSpan(component);

                    if (rowSpan > 1) {
                        // We might need to adjust row heights to accomodate
                        // this spanning cell. First, we find out if any of the
                        // spanned cells are default height and how much space
                        // we've allocated thus far for those cells

                        int spannedDefaultHeightCellCount = 0;
                        int spannedRelativeWeight = 0;
                        int spannedHeight = 0;

                        for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                            if (defaultHeightRows[i + k]) {
                                spannedDefaultHeightCellCount++;
                            }

                            spannedRelativeWeight += relativeWeights[i + k];
                            spannedHeight += rowHeightsLocal[i + k];
                        }

                        if (spannedRelativeWeight > 0
                            || spannedDefaultHeightCellCount > 0) {
                            int componentPreferredHeight =
                                component.getPreferredHeight(columnWidthsLocal[j]);

                            if (componentPreferredHeight > spannedHeight) {
                                // The component's preferred height is larger
                                // than the height we've allocated thus far, so
                                // an adjustment is necessary
                                int adjustment = componentPreferredHeight - spannedHeight;

                                if (spannedRelativeWeight > 0) {
                                    // We'll distribute the adjustment across
                                    // the spanned relative rows and adjust
                                    // other relative row heights to keep all
                                    // relative row heights reconciled
                                    float unitAdjustment = adjustment /
                                        (float)spannedRelativeWeight;

                                    for (int k = 0; k < rowCount; k++) {
                                        int relativeWeight = relativeWeights[k];

                                        if (relativeWeight > 0) {
                                            int rowAdjustment =
                                                Math.round(unitAdjustment * relativeWeight);

                                            rowHeightsLocal[k] += rowAdjustment;
                                        }
                                    }
                                } else {
                                    // We'll distribute the adjustment evenly
                                    // among the default-height rows
                                    for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                                        if (defaultHeightRows[i + k]) {
                                            int rowAdjustment = adjustment /
                                                spannedDefaultHeightCellCount;

                                            rowHeightsLocal[i + k] += rowAdjustment;

                                            // Adjust these to avoid rounding errors
                                            adjustment -= rowAdjustment;
                                            spannedDefaultHeightCellCount--;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // The preferred height of the table pane is the sum of the row
        // heights, plus padding and spacing

        boolean[][] occupiedCells = getOccupiedCells();
        int visibleRowCount = 0;

        int preferredHeight = padding.top + padding.bottom;

        for (int i = 0; i < rowCount; i++) {
            boolean rowVisible = false;

            for (int j = 0; j < columnCount; j++) {
                if (occupiedCells[i][j]) {
                    rowVisible = true;
                    break;
                }
            }

            if (rowVisible) {
                preferredHeight += rowHeightsLocal[i];
                visibleRowCount++;
            }
        }

        if (visibleRowCount > 1) {
            preferredHeight += (visibleRowCount - 1) * verticalSpacing;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize by performing calculations here
        int preferredWidth = getPreferredWidth(-1);
        int preferredHeight = getPreferredHeight(preferredWidth);
        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        TablePane tablePane = (TablePane)getComponent();

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int[] columnWidthsLocal = getColumnWidths(width);
        int[] rowHeightsLocal = getRowHeights(height, columnWidthsLocal);
        boolean[][] occupiedCells = getOccupiedCells();

        int baseline = -1;

        int rowY = padding.top;

        for (int i = 0; i < rowCount && baseline == -1; i++) {
            TablePane.Row row = rows.get(i);
            boolean rowVisible = false;

            for (int j = 0, n = row.getLength(); j < n && j < columnCount && baseline == -1; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    int columnSpan = Math.min(TablePane.getColumnSpan(component), columnCount - j);
                    int componentWidth = (columnSpan - 1) * horizontalSpacing;
                    for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                        componentWidth += columnWidthsLocal[j + k];
                    }

                    int rowSpan = Math.min(TablePane.getRowSpan(component), rowCount  - i);
                    int componentHeight = (rowSpan - 1) * verticalSpacing;
                    for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                        componentHeight += rowHeightsLocal[i + k];
                    }

                    baseline = component.getBaseline(Math.max(componentWidth, 0),
                        Math.max(componentHeight, 0));

                    if (baseline != -1) {
                        baseline += rowY;
                    }
                }

                rowVisible |= occupiedCells[i][j];
            }

            if (rowVisible) {
                rowY += (rowHeightsLocal[i] + verticalSpacing);
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        TablePane tablePane = (TablePane)getComponent();

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int width = getWidth();
        int height = getHeight();

        // NOTE We cache column widths and row heights to make getColumnAt()
        // and getRowAt() more efficient
        columnWidths = getColumnWidths(width);
        rowHeights = getRowHeights(height, columnWidths);

        // Determine which rows and column should be visible so we know which
        // ones should be collapsed
        boolean[] visibleRows = new boolean[rowCount];
        boolean[] visibleColumns = new boolean[columnCount];

        boolean[][] occupiedCells = getOccupiedCells();
        for (int i = 0; i < rowCount; i++) {
            for (int j = 0; j < columnCount; j++) {
                if (occupiedCells[i][j]) {
                    visibleRows[i] = true;
                    visibleColumns[j] = true;
                }
            }
        }

        int componentY = padding.top;
        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            int componentX = padding.left;
            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component child = row.get(j);

                if (child != null
                    && child.isVisible()) {
                    child.setLocation(componentX, componentY);

                    int columnSpan = TablePane.getColumnSpan(child);
                    columnSpan = Math.min(columnSpan, columnCount - j);
                    int childWidth = (columnSpan - 1) * horizontalSpacing;
                    for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                        childWidth += columnWidths[j + k];
                    }

                    int rowSpan = TablePane.getRowSpan(child);
                    rowSpan = Math.min(rowSpan,rowCount  - i);
                    int childHeight = (rowSpan - 1) * verticalSpacing;
                    for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                        childHeight += rowHeights[i + k];
                    }

                    // Set the component's size
                    child.setSize(Math.max(childWidth, 0), Math.max(childHeight, 0));
                }

                if (visibleColumns[j]) {
                    componentX += (columnWidths[j] + horizontalSpacing);
                }
            }

            if (visibleRows[i]) {
                componentY += (rowHeights[i] + verticalSpacing);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        TablePane tablePane = (TablePane)getComponent();

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(highlightBackgroundColor);

        // Paint the highlighted rows
        for (int i = 0, rowY = padding.top; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            if (row.isHighlighted()) {
                graphics.fillRect(0, rowY, width, rowHeights[i]);
            }

            rowY += rowHeights[i] + verticalSpacing;
        }

        // Paint the highlighted columns
        for (int j = 0, columnX = padding.left; j < columnCount; j++) {
            TablePane.Column column = columns.get(j);

            if (column.isHighlighted()) {
                graphics.fillRect(columnX, 0, columnWidths[j], height);
            }

            columnX += columnWidths[j] + horizontalSpacing;
        }

        // Paint the grid lines
        if ((showHorizontalGridLines && verticalSpacing > 0)
            || (showVerticalGridLines && horizontalSpacing > 0)) {
            Graphics2D gridGraphics = (Graphics2D)graphics.create();

            gridGraphics.setStroke(new BasicStroke());
            gridGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // Find any components that span multiple rows or columns, and
            // ensure that the grid lines don't get painted through their
            // cells. We'll only instantiate gridClip if we find such cells
            Area gridClip = null;

            for (int i = 0, componentY = padding.top; i < rowCount; i++) {
                for (int j = 0, componentX = padding.left; j < columnCount; j++) {
                    Component component = tablePane.getCellComponent(i, j);

                    if (component != null) {
                        int rowSpan = TablePane.getRowSpan(component);
                        int columnSpan = TablePane.getColumnSpan(component);

                        if (rowSpan > 1
                            || columnSpan > 1) {
                            int rowY = componentY;
                            int columnX = componentX;

                            int rowHeight = rowHeights[i];
                            int columnWidth = columnWidths[j];

                            for (int k = i + 1; k < i + rowSpan && k < rowCount; k++) {
                                rowHeight += rowHeights[k] + verticalSpacing;
                            }

                            for (int k = j + 1; k < j + columnSpan && k < columnCount; k++) {
                                columnWidth += columnWidths[k] + horizontalSpacing;
                            }

                            if (gridClip == null) {
                                gridClip = new Area(graphics.getClip());
                            }

                            if (horizontalSpacing > 1) {
                                columnWidth += horizontalSpacing - 1;
                                columnX -= (int)((horizontalSpacing * 0.5f) - 0.5f);
                            }

                            if (verticalSpacing > 1) {
                                rowHeight += verticalSpacing - 1;
                                rowY -= (int)((verticalSpacing * 0.5f) - 0.5f);
                            }

                            Rectangle2D.Float bounds = new Rectangle2D.Float(columnX, rowY,
                                columnWidth, rowHeight);
                            gridClip.subtract(new Area(bounds));
                        }
                    }

                    componentX += columnWidths[j] + horizontalSpacing;
                }

                componentY += rowHeights[i] + verticalSpacing;
            }

            if (gridClip != null) {
                gridGraphics.clip(gridClip);
            }

            boolean[][] occupiedCells = getOccupiedCells();

            if (showHorizontalGridLines
                && verticalSpacing > 0
                && rowCount > 1) {
                gridGraphics.setPaint(horizontalGridColor);

                int rowY = padding.top;
                int visibleRowCount = 0;

                for (int i = 0; i < rowCount; i++) {
                    boolean rowVisible = false;

                    for (int j = 0; j < columnCount; j++) {
                        if (occupiedCells[i][j]) {
                            rowVisible = true;
                            break;
                        }
                    }

                    if (rowVisible) {
                       if (visibleRowCount++ > 0) {
                           int gridY = Math.max(rowY - (int)Math.ceil(verticalSpacing * 0.5f), 0);
                           GraphicsUtilities.drawLine(gridGraphics, 0, gridY,
                               width, Orientation.HORIZONTAL);
                       }

                       rowY += (rowHeights[i] + verticalSpacing);
                    }
                }
            }

            if (showVerticalGridLines
                && horizontalSpacing > 0
                && columnCount > 1) {
                gridGraphics.setPaint(verticalGridColor);

                int columnX = padding.left;
                int visibleColumnCount = 0;

                for (int j = 0; j < columnCount; j++) {
                    boolean columnVisible = false;

                    for (int i = 0; i < rowCount; i++) {
                        if (occupiedCells[i][j]) {
                            columnVisible = true;
                            break;
                        }
                    }

                    if (columnVisible) {
                        if (visibleColumnCount++ > 0) {
                            int gridX = Math.max(columnX -
                                (int)Math.ceil(horizontalSpacing * 0.5), 0);
                            GraphicsUtilities.drawLine(gridGraphics, gridX, 0,
                                height, Orientation.VERTICAL);
                        }

                        columnX += (columnWidths[j] + horizontalSpacing);
                    }
                }
            }

            gridGraphics.dispose();
        }
    }

    /**
     * Returns the amount of space that will be reserved around the inside edges of the
     * table pane.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space that will be reserved around the inside edges of the
     * table pane.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space that will be reserved around the inside edges of the
     * table pane.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space that will be reserved around the inside edges of the
     * table pane.
     * @param padding A dictionary with keys in the set {left, top, bottom, right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space that will be reserved around the inside edges of the
     * table pane.
     */
    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space that will be reserved around the inside edges of the
     * table pane.
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
     * Gets the spacing that will be applied between the table pane's
     * columns during layout.
     */
    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Sets the spacing that will be applied between the table pane's
     * columns during layout.
     */
    public void setHorizontalSpacing(int horizontalSpacing) {
        if (horizontalSpacing < 0) {
            throw new IllegalArgumentException("horizontalSpacing is negative");
        }

        this.horizontalSpacing = horizontalSpacing;
        invalidateComponent();
    }

    /**
     * Gets the spacing that will be applied in between the table pane's rows
     * during layout.
     */
    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    /**
     * Sets the spacing that will be applied in between the table pane's rows
     * during layout.
     */
    public void setVerticalSpacing(int verticalSpacing) {
        if (verticalSpacing < 0) {
            throw new IllegalArgumentException("verticalSpacing is negative");
        }

        this.verticalSpacing = verticalSpacing;
        invalidateComponent();
    }

    /**
     * Tells whether or not horizontal grid lines will be painted in between
     * the table pane's rows.
     */
    public boolean getShowHorizontalGridLines() {
        return showHorizontalGridLines;
    }

    /**
     * Sets whether or not horizontal grid lines will be painted in between
     * the table pane's rows.
     */
    public void setShowHorizontalGridLines(boolean showHorizontalGridLines) {
        this.showHorizontalGridLines = showHorizontalGridLines;
        repaintComponent();
    }

    /**
     * Tells whether or not vertical grid lines will be painted in between
     * the table pane's columns.
     */
    public boolean getShowVerticalGridLines() {
        return showVerticalGridLines;
    }

    /**
     * Sets whether or not vertical grid lines will be painted in between
     * the table pane's columns.
     */
    public void setShowVerticalGridLines(boolean showVerticalGridLines) {
        this.showVerticalGridLines = showVerticalGridLines;
        repaintComponent();
    }

    /**
     * Gets the color used to paint the table pane's horizontal grid lines.
     */
    public Color getHorizontalGridColor() {
        return horizontalGridColor;
    }

    /**
     * Sets the color used to paint the table pane's horizontal grid lines.
     */
    public void setHorizontalGridColor(Color horizontalGridColor) {
        if (horizontalGridColor == null) {
            throw new IllegalArgumentException("horizontalGridColor is null.");
        }

        this.horizontalGridColor = horizontalGridColor;

        if (showHorizontalGridLines || showVerticalGridLines) {
            repaintComponent();
        }
    }

    /**
     * Sets the color used to paint the table pane's horizontal grid lines.
     * @param horizontalGridColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setHorizontalGridColor(String horizontalGridColor) {
        if (horizontalGridColor == null) {
            throw new IllegalArgumentException("horizontalGridColor is null.");
        }

        setHorizontalGridColor(GraphicsUtilities.decodeColor(horizontalGridColor));
    }

    /**
     * Gets the color used to paint the table pane's vertical grid lines.
     */
    public Color getVerticalGridColor() {
        return verticalGridColor;
    }

    /**
     * Sets the color used to paint the table pane's vertical grid lines.
     */
    public void setVerticalGridColor(Color verticalGridColor) {
        if (verticalGridColor == null) {
            throw new IllegalArgumentException("verticalGridColor is null.");
        }

        this.verticalGridColor = verticalGridColor;

        if (showHorizontalGridLines || showVerticalGridLines) {
            repaintComponent();
        }
    }

    /**
     * Sets the color used to paint the table pane's vertical grid lines.
     * @param verticalGridColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setVerticalGridColor(String verticalGridColor) {
        if (verticalGridColor == null) {
            throw new IllegalArgumentException("verticalGridColor is null.");
        }

        setVerticalGridColor(GraphicsUtilities.decodeColor(verticalGridColor));
    }

    /**
     * Gets the background color used to paint the highlighted rows and columns.
     */
    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    /**
     * Sets the background color used to paint the highlighted rows and columns.
     */
    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    /**
     * Sets the background color used to paint the highlighted rows and columns.
     * @param highlightBackgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by Pivot}.
     */
    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
    }

    /**
     * Returns a grid indicating which cells are occupied. A component is said
     * to occupy a cell if it is visible and either lives in the cell directly
     * or spans the cell. Conversely, vacant cells do not have visible
     * components within them or spanning them.
     *
     * @return
     * A grid of booleans, where occupied cells are denoted by <tt>true</tt>,
     * and vacant cells are denoted by <tt>false</tt>
     */
    private boolean[][] getOccupiedCells() {
        TablePane tablePane = (TablePane)getComponent();

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        boolean[][] occupiedCells = new boolean[rowCount][columnCount];

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {

                    int rowSpan = TablePane.getRowSpan(component);
                    int columnSpan = TablePane.getColumnSpan(component);

                    for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                        for (int l = 0; l < columnSpan && j + l < columnCount; l++) {
                            occupiedCells[i + k][j + l] = true;
                        }
                    }
                }
            }
        }

        return occupiedCells;
    }

    /**
     * Gets the preferred width of a table pane column, which is defined as the
     * maximum preferred width of the column's visible components.
     * <p>
     * Because their preferred width relates to the preferred widths of other
     * columns, components that span multiple columns will not be considered in
     * this calculation (even if they live in the column directly). It is up to
     * the caller to factor such components into the column widths calculation.
     *
     * @param columnIndex
     * The index of the column whose preferred width we're calculating
     */
    private int getPreferredColumnWidth(int columnIndex) {
        TablePane tablePane = (TablePane)getComponent();

        TablePane.RowSequence rows = tablePane.getRows();

        int preferredWidth = 0;

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            TablePane.Row row = rows.get(i);

            if (row.getLength() > columnIndex) {
                Component component = row.get(columnIndex);

                if (component != null
                    && component.isVisible()
                    && TablePane.getColumnSpan(component) == 1) {
                    preferredWidth = Math.max(preferredWidth,
                        component.getPreferredWidth(-1));
                }
            }
        }

        return preferredWidth;
    }

    /**
     * Tells whether or not the specified column is visible. A column is
     * visible if and only if one or more visible components occupies it. A
     * component is said to occupy a cell if it either lives in the cell
     * directly or spans the cell.
     *
     * @param columnIndex
     * The index of the column within the table pane
     *
     * @return
     * <tt>true</tt> if the column is visible; <tt>false</tt> otherwise
     */
    private boolean isColumnVisible(int columnIndex) {
        boolean visible = false;

        boolean[][] occupiedCells = getOccupiedCells();

        for (int i = 0; i < occupiedCells.length; i++) {
            if (occupiedCells[i][columnIndex]) {
                visible = true;
                break;
            }
        }

        return visible;
    }

    /**
     * Gets the preferred height of a table pane row, which is defined as the
     * maximum preferred height of the row's visible components. The
     * preferred height of each constituent component will be constrained by
     * the width of the column that the component occupies (as specified in the
     * array of column widths).
     * <p>
     * Because their preferred height relates to the preferred heights of other
     * rows, components that span multiple rows will not be considered in
     * this calculation (even if they live in the column directly). It is up to
     * the caller to factor such components into the row heights calculation.
     *
     * @param rowIndex
     * The index of the row whose preferred height we're calculating
     *
     * @param columnWidthsArgument
     * An array of column width values corresponding to the columns of the
     * table pane
     */
    private int getPreferredRowHeight(int rowIndex, int[] columnWidthsArgument) {
        if (columnWidthsArgument == null) {
            throw new IllegalArgumentException("columnWidths is null");
        }

        TablePane tablePane = (TablePane)getComponent();

        TablePane.ColumnSequence columns = tablePane.getColumns();
        TablePane.Row row = tablePane.getRows().get(rowIndex);

        int preferredHeight = 0;

        for (int j = 0, n = row.getLength(), m = columns.getLength(); j < n && j < m; j++) {
            Component component = row.get(j);

            if (component != null
                && component.isVisible()
                && TablePane.getRowSpan(component) == 1) {
                preferredHeight = Math.max(preferredHeight,
                    component.getPreferredHeight(columnWidthsArgument[j]));
            }
        }

        return preferredHeight;
    }

    /**
     * Tells whether or not the specified row is visible. A row is visible if
     * and only if one or more visible components occupies it. A component is
     * said to occupy a cell if it either lives in the cell directly or spans
     * the cell.
     *
     * @param rowIndex
     * The index of the row within the table pane
     *
     * @return
     * <tt>true</tt> if the row is visible; <tt>false</tt> otherwise
     */
    private boolean isRowVisible(int rowIndex) {
        boolean visible = false;

        boolean[][] occupiedCells = getOccupiedCells();

        for (int j = 0; j < occupiedCells[rowIndex].length; j++) {
            if (occupiedCells[rowIndex][j]) {
                visible = true;
                break;
            }
        }

        return visible;
    }

    /**
     * Gets the width of each table pane column given the specified table pane
     * width.
     *
     * @param width
     * The width constraint of the table pane
     *
     * @return
     * An array containing the width of each column in the table pane given the
     * specified constraint
     */
    private int[] getColumnWidths(int width) {
        TablePane tablePane = (TablePane)getComponent();

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int[] columnWidthsLocal = new int[columnCount];

        boolean[] defaultWidthColumns = new boolean[columnCount];
        int totalRelativeWeight = 0;
        int visibleColumnCount = 0;

        int reservedWidth = padding.left + padding.right;

        // First, we allocate the widths of non-relative columns. We store the
        // widths of relative columns as negative values for later processing

        for (int j = 0; j < columnCount; j++) {
            if (isColumnVisible(j)) {
                TablePane.Column column = columns.get(j);
                int columnWidth = column.getWidth();

                if (column.isRelative()) {
                    columnWidthsLocal[j] = -columnWidth;
                    totalRelativeWeight += columnWidth;
                } else {
                    if (columnWidth < 0) {
                        // Default width column; we must calculate the width
                        columnWidth = getPreferredColumnWidth(j);
                        defaultWidthColumns[j] = true;
                    }

                    columnWidthsLocal[j] = columnWidth;
                    reservedWidth += columnWidth;
                }

                visibleColumnCount++;
            } else {
                columnWidthsLocal[j] = 0;
            }
        }

        if (visibleColumnCount > 1) {
            reservedWidth += (visibleColumnCount - 1) * horizontalSpacing;
        }

        // Next, we we account for default-width columns containing spanning
        // cells, which have been ignored thus far. We ensure that the sum of
        // the widths of the spanned cells is enough to satisfy the preferred
        // width of the spanning content.

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    int columnSpan = TablePane.getColumnSpan(component);

                    if (columnSpan > 1) {
                        // We might need to adjust column widths to accomodate
                        // this spanning cell. First, we find out if any of the
                        // spanned cells are default width and how much space
                        // we've allocated thus far for those cells

                        boolean adjustCells = true;
                        int spannedDefaultWidthCellCount = 0;
                        int spannedWidth = 0;

                        for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                            if (columnWidthsLocal[j + k] < 0) {
                                adjustCells = false;
                                break;
                            }

                            if (defaultWidthColumns[j + k]) {
                                spannedDefaultWidthCellCount++;
                            }

                            spannedWidth += columnWidthsLocal[j + k];
                        }

                        // If we span any relative-width columns, we assume
                        // that we'll achieve the desired spanning width when
                        // we divvy up the remaining space, so there's no need
                        // to make an adjustment here. This assumption is safe
                        // because our preferred width policy is to *either*
                        // divide the adjustment among the relative-width
                        // columns *or* among the default-width columns if we
                        // don't span any relative-width columns

                        if (adjustCells
                            && spannedDefaultWidthCellCount > 0) {
                            int componentPreferredWidth = component.getPreferredWidth(-1);

                            if (componentPreferredWidth > spannedWidth) {
                                // The component's preferred width is larger
                                // than the width we've allocated thus far, so
                                // an adjustment is necessary
                                int adjustment = componentPreferredWidth - spannedWidth;

                                // We'll distribute the adjustment evenly
                                // among the default-width columns
                                for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                                    if (defaultWidthColumns[j + k]) {
                                        int columnAdjustment = adjustment /
                                            spannedDefaultWidthCellCount;

                                        columnWidthsLocal[j + k] += columnAdjustment;
                                        reservedWidth += columnAdjustment;

                                        // Adjust these to avoid rounding errors
                                        adjustment -= columnAdjustment;
                                        spannedDefaultWidthCellCount--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Finally, we allocate the widths of the relative columns by divvying
        // up the remaining width

        int remainingWidth = Math.max(width - reservedWidth, 0);
        if (totalRelativeWeight > 0
            && remainingWidth > 0) {
            for (int j = 0; j < columnCount; j++) {
                if (columnWidthsLocal[j] < 0) {
                    int relativeWeight = -columnWidthsLocal[j];
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    int columnWidth = (int)(remainingWidth * weightPercentage);

                    columnWidthsLocal[j] = columnWidth;

                    // NOTE we adjust remainingWidth and totalRelativeWeight as we go
                    // to avoid potential rounding errors in the columnWidth
                    // calculation
                    remainingWidth -= columnWidth;
                    totalRelativeWeight -= relativeWeight;
                }
            }
        }

        return columnWidthsLocal;
    }

    /**
     * Gets the height of each row of a table pane given the specified
     * constraints.
     *
     * @param height
     * The height constraint of the table pane
     *
     * @param columnWidthsArgument
     * The widths of the table pane's columns, which will be used as width
     * constraints to the row heights when necessary, or <tt>null</tt> if the
     * column widths are not yet known (the row heights will be unconstrained)
     *
     * @return
     * An array containing the height of each row in the table pane given the
     * specified constraints
     */
    private int[] getRowHeights(int height, int[] columnWidthsArgument) {
        if (columnWidthsArgument == null) {
            throw new IllegalArgumentException("columnWidths is null");
        }

        TablePane tablePane = (TablePane)getComponent();
        TablePane.RowSequence rows = tablePane.getRows();

        int rowCount = tablePane.getRows().getLength();
        int columnCount = tablePane.getColumns().getLength();

        int rowHeightsLocal[] = new int[rowCount];

        boolean[] defaultHeightRows = new boolean[rowCount];
        int totalRelativeWeight = 0;
        int visibleRowCount = 0;

        int reservedHeight = padding.top + padding.bottom;

        // First, we allocate the heights of non-relative rows. We store the
        // heights of relative rows as negative values for later processing

        for (int i = 0; i < rowCount; i++) {
            if (isRowVisible(i)) {
                TablePane.Row row = rows.get(i);
                int rowHeight = row.getHeight();

                if (row.isRelative()) {
                    rowHeightsLocal[i] = -rowHeight;
                    totalRelativeWeight += rowHeight;
                } else {
                    if (rowHeight < 0) {
                        // Default height row; we must calculate the height
                        rowHeight = getPreferredRowHeight(i, columnWidthsArgument);
                        defaultHeightRows[i] = true;
                    }

                    rowHeightsLocal[i] = rowHeight;
                    reservedHeight += rowHeight;
                }

                visibleRowCount++;
            } else {
                rowHeightsLocal[i] = 0;
            }
        }

        if (visibleRowCount > 1) {
            reservedHeight += (visibleRowCount - 1) * verticalSpacing;
        }

        // Next, we we account for default-width columns containing spanning
        // cells, which have been ignored thus far. We ensure that the sum of
        // the widths of the spanned cells is enough to satisfy the preferred
        // width of the spanning content.

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    int rowSpan = TablePane.getRowSpan(component);

                    if (rowSpan > 1) {
                        // We might need to adjust row heights to accommodate
                        // this spanning cell. First, we find out if any of the
                        // spanned cells are default height and how much space
                        // we've allocated thus far for those cells

                        boolean adjustCells = true;
                        int spannedDefaultHeightCellCount = 0;
                        int spannedHeight = 0;

                        for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                            if (rowHeightsLocal[i + k] < 0) {
                                adjustCells = false;
                                break;
                            }

                            if (defaultHeightRows[i + k]) {
                                spannedDefaultHeightCellCount++;
                            }

                            spannedHeight += rowHeightsLocal[i + k];
                        }

                        // If we span any relative-height rows, we assume
                        // that we'll achieve the desired spanning height when
                        // we divvy up the remaining space, so there's no need
                        // to make an adjustment here. This assumption is safe
                        // because our preferred height policy is to *either*
                        // divide the adjustment among the relative-height
                        // rows *or* among the default-height rows if we
                        // don't span any relative-height rows

                        if (adjustCells
                            && spannedDefaultHeightCellCount > 0) {
                            int componentPreferredHeight =
                                component.getPreferredHeight(columnWidthsArgument[j]);

                            if (componentPreferredHeight > spannedHeight) {
                                // The component's preferred height is larger
                                // than the height we've allocated thus far, so
                                // an adjustment is necessary
                                int adjustment = componentPreferredHeight - spannedHeight;

                                // We'll distribute the adjustment evenly
                                // among the default-height rows
                                for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                                    if (defaultHeightRows[i + k]) {
                                        int rowAdjustment = adjustment /
                                            spannedDefaultHeightCellCount;

                                        rowHeightsLocal[i + k] += rowAdjustment;
                                        reservedHeight += rowAdjustment;

                                        // Adjust these to avoid rounding errors
                                        adjustment -= rowAdjustment;
                                        spannedDefaultHeightCellCount--;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Finally, we allocate the heights of the relative rows by divvying
        // up the remaining height

        int remainingHeight = height - reservedHeight;
        if (totalRelativeWeight > 0
            && remainingHeight > 0) {
            for (int i = 0; i < rowCount; i++) {
                if (rowHeightsLocal[i] < 0) {
                    int relativeWeight = -rowHeightsLocal[i];
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    int rowHeight = (int)(remainingHeight * weightPercentage);

                    rowHeightsLocal[i] = rowHeight;

                    // NOTE we adjust remainingHeight and totalRelativeWeight as we
                    // go to avoid potential rounding errors in the rowHeight
                    // calculation
                    remainingHeight -= rowHeight;
                    totalRelativeWeight -= relativeWeight;
                }
            }
        }

        // If we have don't actually have enough height available

        boolean progress = true; // prevent infinite loop
        while (remainingHeight < 0 && progress) {
            progress = false;
            for (int i = 0; i < rowCount; i++) {
                if (isRowVisible(i)) {
                    TablePane.Row row = rows.get(i);
                    if (!row.isRelative()) {
                        if (rowHeightsLocal[i] > 0) {
                            rowHeightsLocal[i]--;
                            remainingHeight++;
                            progress = true;
                            if (remainingHeight >= 0) break;
                        }
                    }
                }
            }
        }

        return rowHeightsLocal;
    }

    // TablePane.Skin methods

    @Override
    public int getRowAt(int y) {
        if (rowHeights == null) {
            return -1;
        }

        int rowIndex = -1;

        for (int i = 0, rowY = padding.top; rowY <= y && i < rowHeights.length; i++) {
            int rowHeight = rowHeights[i];

            if (y < rowY + rowHeight) {
                rowIndex = i;
                break;
            }

            rowY += rowHeight + verticalSpacing;
        }

        return rowIndex;
    }

    @Override
    public Bounds getRowBounds(int row) {
        if (rowHeights == null) {
            return new Bounds(0, 0, 0, 0);
        }

        if (row < 0
            || row >= rowHeights.length) {
            throw new IndexOutOfBoundsException(String.valueOf(row));
        }

        int rowY = padding.top;

        for (int i = 0; i < row; i++) {
            rowY += rowHeights[i] + verticalSpacing;
        }

        return new Bounds(0, rowY, getWidth(), rowHeights[row]);
    }

    @Override
    public int getColumnAt(int x) {
        if (columnWidths == null) {
            return -1;
        }

        int columnIndex = -1;

        for (int j = 0, columnX = padding.left; columnX <= x && j < columnWidths.length; j++) {
            int columnWidth = columnWidths[j];

            if (x < columnX + columnWidth) {
                columnIndex = j;
                break;
            }

            columnX += columnWidth + horizontalSpacing;
        }

        return columnIndex;
    }

    @Override
    public Bounds getColumnBounds(int column) {
        if (columnWidths == null) {
            return new Bounds(0, 0, 0, 0);
        }

        if (column < 0
            || column >= columnWidths.length) {
            throw new IndexOutOfBoundsException(String.valueOf(column));
        }

        int columnX = padding.left;

        for (int j = 0; j < column; j++) {
            columnX += columnWidths[j] + horizontalSpacing;
        }

        return new Bounds(columnX, 0, columnWidths[column], getHeight());
    }

    // TablePaneListener methods

    @Override
    public void rowInserted(TablePane tablePane, int index) {
        invalidateComponent();
    }

    @Override
    public void rowsRemoved(TablePane tablePane, int index, Sequence<TablePane.Row> rows) {
        invalidateComponent();
    }

    @Override
    public void rowHeightChanged(TablePane.Row row, int previousHeight, boolean previousRelative) {
        invalidateComponent();
    }

    @Override
    public void rowHighlightedChanged(TablePane.Row row) {
        TablePane tablePane = row.getTablePane();
        repaintComponent(getRowBounds(tablePane.getRows().indexOf(row)));
    }

    @Override
    public void columnInserted(TablePane tablePane, int index) {
        invalidateComponent();
    }

    @Override
    public void columnsRemoved(TablePane tablePane, int index, Sequence<TablePane.Column> columns) {
        invalidateComponent();
    }

    @Override
    public void columnWidthChanged(TablePane.Column column, int previousWidth,
        boolean previousRelative) {
        invalidateComponent();
    }

    @Override
    public void columnHighlightedChanged(TablePane.Column column) {
        TablePane tablePane = column.getTablePane();
        repaintComponent(getColumnBounds(tablePane.getColumns().indexOf(column)));
    }

    @Override
    public void cellInserted(TablePane.Row row, int column) {
        invalidateComponent();
    }

    @Override
    public void cellsRemoved(TablePane.Row row, int column, Sequence<Component> removed) {
        invalidateComponent();
    }

    @Override
    public void cellUpdated(TablePane.Row row, int column, Component previousComponent) {
        invalidateComponent();
    }

    // TablePaneAttribute events

    @Override
    public void rowSpanChanged(TablePane tablePane, Component component, int previousRowSpan) {
        invalidateComponent();
    }

    @Override
    public void columnSpanChanged(TablePane tablePane, Component component,
        int previousColumnSpan) {
        invalidateComponent();
    }
}
