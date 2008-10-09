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
package pivot.wtk.skin;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Bounds;
import pivot.wtk.TablePane;
import pivot.wtk.TablePaneListener;
import pivot.wtk.TablePaneAttributeListener;

/**
 * Table pane skin.
 *
 * @author tvolkert
 */
public class TablePaneSkin extends ContainerSkin implements TablePane.Skin,
    TablePaneListener, TablePaneAttributeListener {
    private Insets padding = DEFAULT_PADDING;
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private boolean showHorizontalGridLines = false;
    private boolean showVerticalGridLines = false;
    private Color gridColor = Color.BLACK;
    private Color selectionBackgroundColor = Color.GRAY;

    private static final Insets DEFAULT_PADDING = new Insets(0);

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
    public void uninstall() {
        TablePane tablePane = (TablePane)getComponent();
        tablePane.getTablePaneListeners().remove(this);
        tablePane.getTablePaneAttributeListeners().remove(this);

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        TablePane tablePane = (TablePane)getComponent();
        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int[] columnWidths = new int[columnCount];
        int[] relativeWeights = new int[columnCount];
        boolean[] defaultWidthColumns = new boolean[columnCount];

        int totalRelativeWeight = 0;

        // First, we calculate the base widths of the columns, giving relative
        // columns their preferred width

        for (int i = 0; i < columnCount; i++) {
            TablePane.Column column = columns.get(i);
            int columnWidth = column.getWidth();
            boolean isRelative = column.isRelative();

            if (isRelative) {
                relativeWeights[i] = columnWidth;
                totalRelativeWeight += columnWidth;
            }

            if (columnWidth < 0 || isRelative) {
                columnWidth = getPreferredColumnWidth(tablePane, i, null);
            }

            columnWidths[i] = columnWidth;
            defaultWidthColumns[i] = (columnWidth < 0);
        }

        // Next, we adjust the widths of the relative columns upwards where
        // necessary to reconcile their widths relative to one another while
        // ensuring that they still get at least their preferred width

        if (totalRelativeWeight > 0) {
            int totalRelativeWidth = 0;

            // Calculate the total relative width after the required upward
            // adjustments

            for (int i = 0; i < columnCount; i++) {
                int columnWidth = columnWidths[i];
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
                    columnWidths[i] = (int)(weightPercentage * totalRelativeWidth);
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
                    && component.isDisplayable()) {
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
                            spannedWidth += columnWidths[j + k];
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

                                            columnWidths[k] += columnAdjustment;
                                        }
                                    }
                                } else {
                                    // We'll distribute the adjustment evenly
                                    // among the default-width columns
                                    for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                                        if (defaultWidthColumns[j + k]) {
                                            int columnAdjustment = adjustment /
                                                spannedDefaultWidthCellCount;

                                            columnWidths[j + k] += columnAdjustment;

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

        int preferredWidth = padding.left + padding.right + (columnCount - 1) * horizontalSpacing;

        for (int i = 0; i < columnCount; i++) {
            preferredWidth += columnWidths[i];
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

        int[] rowHeights = new int[rowCount];
        int[] relativeWeights = new int[rowCount];
        boolean[] defaultHeightRows = new boolean[rowCount];

        int totalRelativeWeight = 0;

        if (width < 0) {
            width = getPreferredWidth(-1);
        }

        int[] columnWidths = getColumnWidths(tablePane, width, null);

        // First, we calculate the base heights of the rows, giving relative
        // rows their preferred height

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);
            int rowHeight = row.getHeight();
            boolean isRelative = row.isRelative();

            if (isRelative) {
                relativeWeights[i] = rowHeight;
                totalRelativeWeight += rowHeight;
            }

            if (rowHeight < 0 || isRelative) {
                rowHeight = getPreferredRowHeight(tablePane, i, columnWidths);
            }

            rowHeights[i] = rowHeight;
            defaultHeightRows[i] = (rowHeight < 0);
        }

        // Next, we adjust the heights of the relative rows upwards where
        // necessary to reconcile their heights relative to one another while
        // ensuring that they still get at least their preferred height

        if (totalRelativeWeight > 0) {
            int totalRelativeHeight = 0;

            // Calculate the total relative height after the required upward
            // adjustments

            for (int i = 0; i < rowCount; i++) {
                int rowHeight = rowHeights[i];
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
                    rowHeights[i] = (int)(weightPercentage * totalRelativeHeight);
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
                    && component.isDisplayable()) {
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
                            spannedHeight += rowHeights[i + k];
                        }

                        if (spannedRelativeWeight > 0
                            || spannedDefaultHeightCellCount > 0) {
                            int componentPreferredHeight =
                                component.getPreferredHeight(columnWidths[j]);

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

                                            rowHeights[k] += rowAdjustment;
                                        }
                                    }
                                } else {
                                    // We'll distribute the adjustment evenly
                                    // among the default-height rows
                                    for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                                        if (defaultHeightRows[i + k]) {
                                            int rowAdjustment = adjustment /
                                                spannedDefaultHeightCellCount;

                                            rowHeights[i + k] += rowAdjustment;

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

        int preferredHeight = padding.top + padding.bottom + (rowCount - 1) * verticalSpacing;

        for (int i = 0; i < rowCount; i++) {
            preferredHeight += rowHeights[i];
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
        columnWidths = getColumnWidths(tablePane, width, null);
        rowHeights = getRowHeights(tablePane, height, columnWidths);

        int componentY = padding.top;
        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            int componentX = padding.left;
            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component child = row.get(j);

                if (child != null) {
                    if (child.isDisplayable()) {
                        child.setLocation(componentX, componentY);

                        int columnSpan = TablePane.getColumnSpan(child);
                        int childWidth = (columnSpan - 1) * horizontalSpacing;
                        for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                            childWidth += columnWidths[j + k];
                        }

                        int rowSpan = TablePane.getRowSpan(child);
                        int childHeight = (rowSpan - 1) * verticalSpacing;
                        for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                            childHeight += rowHeights[i + k];
                        }

                        // Set the component's size
                        child.setSize(Math.max(childWidth, 0), Math.max(childHeight, 0));

                        // Show the component
                        child.setVisible(true);
                    } else {
                        // Hide the component
                        child.setVisible(false);
                    }
                }

                componentX += (columnWidths[j] + horizontalSpacing);
            }

            componentY += (rowHeights[i] + verticalSpacing);
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

        graphics.setPaint(selectionBackgroundColor);

        // Paint the selected rows
        for (int i = 0, rowY = padding.top; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);

            if (row.isSelected()) {
                graphics.fillRect(0, rowY, width, rowHeights[i]);
            }

            rowY += rowHeights[i] + verticalSpacing;
        }

        // Paint the selected columns
        for (int j = 0, columnX = padding.left; j < columnCount; j++) {
            TablePane.Column column = columns.get(j);

            if (column.isSelected()) {
                graphics.fillRect(columnX, 0, columnWidths[j], height);
            }

            columnX += columnWidths[j] + horizontalSpacing;
        }

        // Paint the grid lines
        if ((showHorizontalGridLines && verticalSpacing > 0)
            || (showVerticalGridLines && horizontalSpacing > 0)) {
            Graphics2D gridGraphics = (Graphics2D)graphics.create();

            gridGraphics.setStroke(new BasicStroke());
            gridGraphics.setPaint(gridColor);

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

                            for (int k = i + 1; k < i + rowSpan; k++) {
                                rowHeight += rowHeights[k] + verticalSpacing;
                            }

                            for (int k = j + 1; k < j + columnSpan; k++) {
                                columnWidth += columnWidths[k] + horizontalSpacing;
                            }

                            if (gridClip == null) {
                                gridClip = new Area(graphics.getClip());
                            }

                            if (horizontalSpacing > 1) {
                                columnWidth += horizontalSpacing - 1;
                                columnX -= (int)(((float)horizontalSpacing * 0.5f) - 0.5f);
                            }

                            if (verticalSpacing > 1) {
                                rowHeight += verticalSpacing - 1;
                                rowY -= (int)(((float)verticalSpacing * 0.5f) - 0.5f);
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

            if (showHorizontalGridLines
                && verticalSpacing > 0) {
                int rowY = padding.top + (rowHeights[0] + verticalSpacing);

                for (int i = 1; i < rowCount; i++) {
                    int gridY = Math.max(rowY - (int)Math.ceil(verticalSpacing * 0.5f), 0);
                    int gridWidth = Math.max(width - (padding.left + padding.right), 0);
                    gridGraphics.drawLine(padding.left, gridY, gridWidth - 1, gridY);

                    rowY += (rowHeights[i] + verticalSpacing);
                }
            }

            if (showVerticalGridLines
                && horizontalSpacing > 0) {
                int columnX = padding.left + (columnWidths[0] + horizontalSpacing);

                for (int j = 1; j < columnCount; j++) {
                    int gridX = Math.max(columnX - (int)Math.ceil(horizontalSpacing * 0.5), 0);
                    int gridHeight = Math.max(height - (padding.top + padding.bottom), 0);
                    gridGraphics.drawLine(gridX, padding.top, gridX, gridHeight - 1);

                    columnX += (columnWidths[j] + horizontalSpacing);
                }
            }

            gridGraphics.dispose();
        }
    }

    /**
     * Gets the padding that will be reserved around the table pane during
     * layout.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the padding that will be reserved around the table pane during
     * layout.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the padding that will be reserved around the table pane during
     * layout.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the table pane during
     * layout.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Gets the spacing that will be applied in between the table pane's
     * columns during layout.
     */
    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Sets the spacing that will be applied in between the table pane's
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
     * Gets the color used to paint the table pane's grid lines.
     */
    public Color getGridColor() {
        return gridColor;
    }

    /**
     * Sets the color used to paint the table pane's grid lines.
     */
    public void setGridColor(Color gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        this.gridColor = gridColor;

        if (showHorizontalGridLines || showVerticalGridLines) {
            repaintComponent();
        }
    }

    /**
     * Sets the color used to paint the table pane's grid lines.
     */
    public final void setGridColor(String gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        setGridColor(decodeColor(gridColor));
    }

    /**
     * Gets the background color used to paint the selected rows and columns.
     */
    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    /**
     * Sets the background color used to paint the selected rows and columns.
     */
    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    /**
     * Sets the background color used to paint the selected rows and columns.
     */
    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(decodeColor(selectionBackgroundColor));
    }

    /**
     * Gets the preferred width of a table pane column, which is defined as the
     * maximum preferred width of the column's displayable components. The
     * preferred width of each constituent component will be constrained by the
     * height of the row that the component occupies (as specified in the array
     * of row heights).
     * <p>
     * Components that span multiple columns will not be considered in the
     * calculation. It is up to the caller to factor such components into the
     * column widths calculation.
     *
     * @param tablePane
     * The table pane
     *
     * @param columnIndex
     * The index of the column whose preferred width we're calculating
     *
     * @param rowHeights
     * An array of row height values corresponding to the rows of the table
     * pane, or <tt>null</tt> if these heights are not yet known
     */
    private static int getPreferredColumnWidth(TablePane tablePane, int columnIndex,
        int[] rowHeights) {
        TablePane.RowSequence rows = tablePane.getRows();

        int preferredWidth = 0;

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            TablePane.Row row = rows.get(i);

            if (row.getLength() > columnIndex) {
                Component component = row.get(columnIndex);

                if (component != null
                    && component.isDisplayable()
                    && TablePane.getColumnSpan(component) == 1) {
                    int rowHeight = -1;

                    if (rowHeights != null) {
                        rowHeight = rowHeights[i];
                    } else if (!row.isRelative()) {
                        rowHeight = row.getHeight();
                    }

                    preferredWidth = Math.max(preferredWidth,
                        component.getPreferredWidth(rowHeight));
                }
            }
        }

        return preferredWidth;
    }

    /**
     * Gets the preferred height of a table pane row, which is defined as the
     * maximum preferred height of the row's displayable components. The
     * preferred height of each constituent component will be constrained by
     * the width of the column that the component occupies (as specified in the
     * array of column widths).
     * <p>
     * Components that span multiple rows will not be considered in the
     * calculation. It is up to the caller to factor such components into the
     * row heights calculation.
     *
     * @param tablePane
     * The table pane
     *
     * @param rowIndex
     * The index of the row whose preferred height we're calculating
     *
     * @param columnWidths
     * An array of column width values corresponding to the columns of the
     * table pane, or <tt>null</tt> if these widths are not yet known
     */
    private static int getPreferredRowHeight(TablePane tablePane, int rowIndex,
        int[] columnWidths) {
        TablePane.ColumnSequence columns = tablePane.getColumns();
        TablePane.Row row = tablePane.getRows().get(rowIndex);

        int preferredHeight = 0;

        for (int i = 0, n = row.getLength(), m = columns.getLength(); i < n && i < m; i++) {
            Component component = row.get(i);

            if (component != null
                && component.isDisplayable()
                && TablePane.getRowSpan(component) == 1) {
                TablePane.Column column = columns.get(i);
                int columnWidth = -1;

                if (columnWidths != null) {
                    columnWidth = columnWidths[i];
                } else if (!column.isRelative()) {
                    columnWidth = column.getWidth();
                }

                preferredHeight = Math.max(preferredHeight,
                    component.getPreferredHeight(columnWidth));
            }
        }

        return preferredHeight;
    }

    /**
     * Gets the width of each column of a table pane given the specified
     * constraints. This method is static to allow other skins (such as
     * <tt>TablePaneHeaderSkin</tt>) to hook into it.
     *
     * @param tablePane
     * The table pane
     *
     * @param width
     * The width constraint of the table pane
     *
     * @param rowHeights
     * The heights of the table pane's rows, which will be used as height
     * constraints to the column widths when necessary, or <tt>null</tt> if the
     * row heights are not yet known (the column widths will be unconstrained)
     *
     * @return
     * An array containing the width of each column in the table pane given the
     * specified constraints
     */
    public static int[] getColumnWidths(TablePane tablePane, int width, int[] rowHeights) {
        assert(width >= 0) : "Width must be greater than or equal to zero.";

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int[] columnWidths = new int[columnCount];
        boolean[] defaultWidthColumns = new boolean[columnCount];

        Insets padding = (Insets)tablePane.getStyles().get("padding");
        int horizontalSpacing = (Integer)tablePane.getStyles().get("horizontalSpacing");

        int reservedWidth = padding.left + padding.right +
            Math.max(columnCount - 1, 0) * horizontalSpacing;
        int totalRelativeWeight = 0;

        // First, we allocate the widths of non-relative columns. We store the
        // widths of relative columns as negative values for later processing

        for (int i = 0; i < columnCount; i++) {
            TablePane.Column column = columns.get(i);
            int columnWidth = column.getWidth();

            if (column.isRelative()) {
                columnWidths[i] = -columnWidth;
                totalRelativeWeight += columnWidth;
            } else {
                if (columnWidth < 0) {
                    // Default width column; we must calculate the width
                    columnWidth = getPreferredColumnWidth(tablePane, i, rowHeights);
                    defaultWidthColumns[i] = true;
                }

                columnWidths[i] = columnWidth;
                reservedWidth += columnWidth;
            }
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
                    && component.isDisplayable()) {
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

                            if (columnWidths[j + k] < 0) {
                                spannedRelativeWeight += -columnWidths[j + k];
                            } else {
                                spannedWidth += columnWidths[j + k];
                            }
                        }

                        // If we span any relative-width columns, we assume
                        // that we'll achieve the desired spanning width when
                        // we divvy up the remaining space, so there's no need
                        // to make an adjustment here. This assumption is safe
                        // because our preferred width policy is to *either*
                        // divide the adjustment among the relative-width
                        // columns *or* among the default-width columns if we
                        // don't span any relative-width columns

                        if (spannedRelativeWeight == 0
                            && spannedDefaultWidthCellCount > 0) {
                            int rowHeight = -1;

                            if (rowHeights != null) {
                                rowHeight = rowHeights[i];
                            } else if (!row.isRelative()) {
                                rowHeight = row.getHeight();
                            }

                            int componentPreferredWidth = component.getPreferredWidth(rowHeight);

                            if (componentPreferredWidth > spannedWidth) {
                                // The component's preferred width is larger
                                // than the width we've allocated thus far, so
                                // an adjustment is necessary
                                int adjustment = componentPreferredWidth - spannedWidth;
                                reservedWidth -= adjustment;

                                // We'll distribute the adjustment evenly
                                // among the default-width columns
                                for (int k = 0; k < columnSpan && j + k < columnCount; k++) {
                                    if (defaultWidthColumns[j + k]) {
                                        int columnAdjustment = adjustment /
                                            spannedDefaultWidthCellCount;

                                        columnWidths[j + k] += columnAdjustment;

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
        if (totalRelativeWeight > 0 && remainingWidth > 0) {
            for (int i = 0; i < columnCount; i++) {
                if (columnWidths[i] < 0) {
                    int relativeWeight = -columnWidths[i];
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    int columnWidth = (int)(remainingWidth * weightPercentage);

                    columnWidths[i] = columnWidth;

                    // NOTE we adjust remainingWidth and totalRelativeWeight as we go
                    // to avoid potential rounding errors in the columnWidth
                    // calculation
                    remainingWidth -= columnWidth;
                    totalRelativeWeight -= relativeWeight;
                }
            }
        }

        return columnWidths;
    }

    /**
     * Gets the height of each row of a table pane given the specified
     * constraints. This method is static to allow other skins (such as
     * <tt>TablePaneHeaderSkin</tt>) to hook into it.
     *
     * @param tablePane
     * The table pane
     *
     * @param height
     * The height constraint of the table pane
     *
     * @param columnWidths
     * The widths of the table pane's columns, which will be used as width
     * constraints to the row heights when necessary, or <tt>null</tt> if the
     * column widths are not yet known (the row heights will be unconstrained)
     *
     * @return
     * An array containing the height of each row in the table pane given the
     * specified constraints
     */
    public static int[] getRowHeights(TablePane tablePane, int height, int[] columnWidths) {
        assert(height >= 0) : "Height must be greater than or equal to zero.";

        TablePane.RowSequence rows = tablePane.getRows();
        TablePane.ColumnSequence columns = tablePane.getColumns();

        int rowCount = tablePane.getRows().getLength();
        int columnCount = tablePane.getColumns().getLength();

        int rowHeights[] = new int[rowCount];
        boolean[] defaultHeightRows = new boolean[rowCount];

        Insets padding = (Insets)tablePane.getStyles().get("padding");
        int verticalSpacing = (Integer)tablePane.getStyles().get("verticalSpacing");

        int reservedHeight = padding.top + padding.bottom +
            Math.max(rowCount - 1, 0) * verticalSpacing;
        int totalRelativeWeight = 0;

        // First, we allocate the heights of non-relative rows. We store the
        // heights of relative rows as negative values for later processing

        for (int i = 0; i < rowCount; i++) {
            TablePane.Row row = rows.get(i);
            int rowHeight = row.getHeight();

            if (row.isRelative()) {
                rowHeights[i] = -rowHeight;
                totalRelativeWeight += rowHeight;
            } else {
                if (rowHeight < 0) {
                    // Default height row; we must calculate the height
                    rowHeight = getPreferredRowHeight(tablePane, i, columnWidths);
                    defaultHeightRows[i] = true;
                }

                rowHeights[i] = rowHeight;
                reservedHeight += rowHeight;
            }
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
                    && component.isDisplayable()) {
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

                            if (rowHeights[i + k] < 0) {
                                spannedRelativeWeight += -rowHeights[i + k];
                            } else {
                                spannedHeight += rowHeights[i + k];
                            }
                        }

                        // If we span any relative-height rows, we assume
                        // that we'll achieve the desired spanning height when
                        // we divvy up the remaining space, so there's no need
                        // to make an adjustment here. This assumption is safe
                        // because our preferred height policy is to *either*
                        // divide the adjustment among the relative-height
                        // rows *or* among the default-height rows if we
                        // don't span any relative-height rows

                        if (spannedRelativeWeight == 0
                            && spannedDefaultHeightCellCount > 0) {
                            TablePane.Column column = columns.get(j);
                            int columnWidth = -1;

                            if (columnWidths != null) {
                                columnWidth = columnWidths[j];
                            } else if (!column.isRelative()) {
                                columnWidth = column.getWidth();
                            }

                            int componentPreferredHeight =
                                component.getPreferredHeight(columnWidth);

                            if (componentPreferredHeight > spannedHeight) {
                                // The component's preferred height is larger
                                // than the height we've allocated thus far, so
                                // an adjustment is necessary
                                int adjustment = componentPreferredHeight - spannedHeight;
                                reservedHeight -= adjustment;

                                // We'll distribute the adjustment evenly
                                // among the default-height rows
                                for (int k = 0; k < rowSpan && i + k < rowCount; k++) {
                                    if (defaultHeightRows[i + k]) {
                                        int rowAdjustment = adjustment /
                                            spannedDefaultHeightCellCount;

                                        rowHeights[i + k] += rowAdjustment;

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

        int remainingHeight = Math.max(height - reservedHeight, 0);
        if (totalRelativeWeight > 0
            && remainingHeight > 0) {
            for (int i = 0; i < rowCount; i++) {
                if (rowHeights[i] < 0) {
                    int relativeWeight = -rowHeights[i];
                    float weightPercentage = relativeWeight / (float)totalRelativeWeight;
                    int rowHeight = (int)(remainingHeight * weightPercentage);

                    rowHeights[i] = rowHeight;

                    // NOTE we adjust remainingHeight and totalRelativeWeight as we
                    // go to avoid potential rounding errors in the rowHeight
                    // calculation
                    remainingHeight -= rowHeight;
                    totalRelativeWeight -= relativeWeight;
                }
            }
        }

        return rowHeights;
    }

    // TablePane.Skin methods

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

    public Bounds getRowBounds(int row) {
        if (rowHeights == null) {
            return new Bounds(0, 0, 0, 0);
        }

        int rowY = padding.top;

        for (int i = 0; i < row; i++) {
            rowY += rowHeights[i] + verticalSpacing;
        }

        return new Bounds(0, rowY, getWidth(), rowHeights[row]);
    }

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

    public Bounds getColumnBounds(int column) {
        if (columnWidths == null) {
            return new Bounds(0, 0, 0, 0);
        }

        int columnX = padding.left;

        for (int j = 0; j < column; j++) {
            columnX += columnWidths[j] + horizontalSpacing;
        }

        return new Bounds(columnX, 0, columnWidths[column], getHeight());
    }

    // TablePaneListener methods

    public void rowInserted(TablePane tablePane, int index) {
        invalidateComponent();
    }

    public void rowsRemoved(TablePane tablePane, int index, Sequence<TablePane.Row> rows) {
        invalidateComponent();
    }

    public void rowHeightChanged(TablePane.Row row, int previousHeight,
        boolean previousRelative) {
        invalidateComponent();
    }

    public void rowSelectedChanged(TablePane.Row row) {
        TablePane tablePane = row.getTablePane();
        repaintComponent(getRowBounds(tablePane.getRows().indexOf(row)));
    }

    public void columnInserted(TablePane tablePane, int index) {
        invalidateComponent();
    }

    public void columnsRemoved(TablePane tablePane, int index,
        Sequence<TablePane.Column> columns) {
        invalidateComponent();
    }

    public void columnWidthChanged(TablePane.Column column, int previousWidth,
        boolean previousRelative) {
        invalidateComponent();
    }

    public void columnSelectedChanged(TablePane.Column column) {
        TablePane tablePane = column.getTablePane();
        repaintComponent(getColumnBounds(tablePane.getColumns().indexOf(column)));
    }

    public void cellInserted(TablePane.Row row, int column) {
        invalidateComponent();
    }

    public void cellsRemoved(TablePane.Row row, int column,
        Sequence<Component> removed) {
        invalidateComponent();
    }

    public void cellUpdated(TablePane.Row row, int column,
        Component previousComponent) {
        invalidateComponent();
    }

    // TablePaneAttribute events

    public void rowSpanChanged(TablePane tablePane, Component component,
        int previousRowSpan) {
        invalidateComponent();
    }

    public void columnSpanChanged(TablePane tablePane, Component component,
        int previousColumnSpan) {
        invalidateComponent();
    }
}
