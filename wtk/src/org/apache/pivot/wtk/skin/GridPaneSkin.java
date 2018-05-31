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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.GridPaneListener;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;

/**
 * Theme-independent skin for the {@link GridPane} component.
 */
public class GridPaneSkin extends ContainerSkin implements GridPane.Skin, GridPaneListener {
    /**
     * Provides metadata about the grid pane that skins use in performing
     * preferred size calculations and layout.
     */
    protected final class Metadata {
        public final int visibleRowCount;
        public final int visibleColumnCount;

        private boolean[] visibleRows;
        private boolean[] visibleColumns;

        public Metadata() {
            GridPane gridPane = (GridPane) getComponent();

            GridPane.RowSequence rows = gridPane.getRows();

            int columnCount = gridPane.getColumnCount();
            int rowCount = rows.getLength();

            visibleRows = new boolean[rowCount];
            visibleColumns = new boolean[columnCount];

            int visibleRowCountCalc = 0;
            int visibleColumnCountCalc = 0;

            for (int i = 0; i < rowCount; i++) {
                GridPane.Row row = rows.get(i);

                for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                    Component component = row.get(j);

                    if (component != null && component.isVisible()) {
                        if (!visibleRows[i]) {
                            visibleRowCountCalc++;
                            visibleRows[i] = true;
                        }

                        if (!visibleColumns[j]) {
                            visibleColumnCountCalc++;
                            visibleColumns[j] = true;
                        }
                    }
                }
            }

            this.visibleRowCount = visibleRowCountCalc;
            this.visibleColumnCount = visibleColumnCountCalc;
        }

        public boolean isRowVisible(int rowIndex) {
            return visibleRows[rowIndex];
        }

        public boolean isColumnVisible(int columnIndex) {
            return visibleColumns[columnIndex];
        }
    }

    private Insets padding = Insets.NONE;
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private boolean showHorizontalGridLines = false;
    private boolean showVerticalGridLines = false;
    private Color horizontalGridColor;
    private Color verticalGridColor;

    /**
     * These are cached computed values, for performance.
     */
    private int cellWidth = 0;
    private int cellHeight = 0;


    /**
     * Default constructor.
     */
    public GridPaneSkin() {
        horizontalGridColor = defaultForegroundColor();
        verticalGridColor = defaultForegroundColor();
    }

    @Override
    public void install(Component component) {
        super.install(component);

        GridPane gridPane = (GridPane) component;
        gridPane.getGridPaneListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        GridPane gridPane = (GridPane) getComponent();

        GridPane.RowSequence rows = gridPane.getRows();

        int columnCount = gridPane.getColumnCount();
        int rowCount = rows.getLength();

        Metadata metadata = new Metadata();

        int cellHeightValue = getCellHeight(height, metadata);

        int preferredCellWidth = 0;
        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null && component.isVisible()) {
                    preferredCellWidth = Math.max(preferredCellWidth,
                        component.getPreferredWidth(cellHeightValue));
                }
            }
        }

        // The preferred width of the grid pane is the sum of the column
        // widths, plus padding and spacing

        int preferredWidth = (metadata.visibleColumnCount * preferredCellWidth)
            + padding.getWidth();

        if (metadata.visibleColumnCount > 1) {
            preferredWidth += (metadata.visibleColumnCount - 1) * horizontalSpacing;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        GridPane gridPane = (GridPane) getComponent();

        GridPane.RowSequence rows = gridPane.getRows();

        int columnCount = gridPane.getColumnCount();
        int rowCount = rows.getLength();

        Metadata metadata = new Metadata();

        int cellWidthValue = getCellWidth(width, metadata);

        int preferredCellHeight = 0;
        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null && component.isVisible()) {
                    preferredCellHeight = Math.max(preferredCellHeight,
                        component.getPreferredHeight(cellWidthValue));
                }
            }
        }

        // The preferred height of the grid pane is the sum of the row
        // heights, plus padding and spacing

        int preferredHeight = (metadata.visibleRowCount * preferredCellHeight)
            + padding.getHeight();

        if (metadata.visibleRowCount > 1) {
            preferredHeight += (metadata.visibleRowCount - 1) * verticalSpacing;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        GridPane gridPane = (GridPane) getComponent();

        GridPane.RowSequence rows = gridPane.getRows();

        int columnCount = gridPane.getColumnCount();
        int rowCount = rows.getLength();

        Metadata metadata = new Metadata();

        // calculate the maximum preferred cellWidth and cellHeight
        int preferredCellHeight = 0;
        int preferredCellWidth = 0;
        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null && component.isVisible()) {
                    Dimensions d = component.getPreferredSize();
                    preferredCellHeight = Math.max(preferredCellHeight, d.height);
                    preferredCellWidth = Math.max(preferredCellWidth, d.width);
                }
            }
        }

        // The preferred width of the grid pane is the sum of the column
        // widths, plus padding and spacing

        int preferredWidth = (metadata.visibleColumnCount * preferredCellWidth)
            + padding.getWidth();

        if (metadata.visibleColumnCount > 1) {
            preferredWidth += (metadata.visibleColumnCount - 1) * horizontalSpacing;
        }

        // The preferred height of the grid pane is the sum of the row
        // heights, plus padding and spacing

        int preferredHeight = (metadata.visibleRowCount * preferredCellHeight)
            + padding.getHeight();

        if (metadata.visibleRowCount > 1) {
            preferredHeight += (metadata.visibleRowCount - 1) * verticalSpacing;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        GridPane gridPane = (GridPane) getComponent();

        GridPane.RowSequence rows = gridPane.getRows();

        int columnCount = gridPane.getColumnCount();
        int rowCount = rows.getLength();

        Metadata metadata = new Metadata();

        int cellWidthValue = getCellWidth(width, metadata);
        int cellHeightValue = getCellHeight(height, metadata);

        // Return the first available baseline by traversing cells top left to bottom right

        int baseline = -1;

        int rowY = padding.top;

        for (int i = 0; i < rowCount && baseline == -1; i++) {
            if (metadata.isRowVisible(i)) {
                GridPane.Row row = rows.get(i);

                for (int j = 0, n = row.getLength(); j < n && j < columnCount && baseline == -1; j++) {
                    Component component = row.get(j);

                    if (component != null && component.isVisible()) {
                        baseline = component.getBaseline(cellWidthValue, cellHeightValue);

                        if (baseline != -1) {
                            baseline += rowY;
                        }
                    }
                }

                rowY += (cellHeightValue + verticalSpacing);
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        GridPane gridPane = (GridPane) getComponent();

        GridPane.RowSequence rows = gridPane.getRows();

        int columnCount = gridPane.getColumnCount();
        int rowCount = rows.getLength();

        int width = getWidth();
        int height = getHeight();

        Metadata metadata = new Metadata();

        cellWidth = getCellWidth(width, metadata);
        cellHeight = getCellHeight(height, metadata);

        int componentY = padding.top;
        for (int i = 0; i < rowCount; i++) {
            if (metadata.isRowVisible(i)) {
                GridPane.Row row = rows.get(i);

                int componentX = padding.left;
                for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                    Component component = row.get(j);

                    if (component != null && component.isVisible()) {
                        component.setLocation(componentX, componentY);
                        component.setSize(cellWidth, cellHeight);
                    }

                    if (metadata.isColumnVisible(j)) {
                        componentX += (cellWidth + horizontalSpacing);
                    }
                }

                componentY += (cellHeight + verticalSpacing);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        GridPane gridPane = (GridPane) getComponent();

        GridPane.RowSequence rows = gridPane.getRows();

        int columnCount = gridPane.getColumnCount();
        int rowCount = rows.getLength();

        int width = getWidth();
        int height = getHeight();

        Metadata metadata = new Metadata();

        if (showHorizontalGridLines && verticalSpacing > 0 && rowCount > 1) {
            graphics.setPaint(horizontalGridColor);

            int rowY = padding.top + (cellHeight + verticalSpacing);

            for (int i = 1; i < rowCount; i++) {
                if (metadata.isRowVisible(i - 1)) {
                    int gridY = Math.max(rowY - (int) Math.ceil(verticalSpacing * 0.5f), 0);
                    GraphicsUtilities.drawLine(graphics, 0, gridY, width, Orientation.HORIZONTAL);

                    rowY += (cellHeight + verticalSpacing);
                }
            }
        }

        if (showVerticalGridLines && horizontalSpacing > 0 && columnCount > 1) {
            graphics.setPaint(verticalGridColor);

            int columnX = padding.left + (cellWidth + horizontalSpacing);

            for (int j = 1; j < columnCount; j++) {
                if (metadata.isColumnVisible(j - 1)) {
                    int gridX = Math.max(columnX - (int) Math.ceil(horizontalSpacing * 0.5f), 0);
                    GraphicsUtilities.drawLine(graphics, gridX, 0, height, Orientation.VERTICAL);

                    columnX += (cellWidth + horizontalSpacing);
                }
            }
        }
    }

    /**
     * @return The cell width given the specified grid pane width and metadata.
     *
     * @param width The overall grid pane width (can be -1).
     * @param metadata Information about visible rows and columns.
     */
    private int getCellWidth(int width, Metadata metadata) {
        int cellWidthCalc = -1;

        if (width != -1) {
            int clientWidth = width - padding.getWidth();

            if (metadata.visibleColumnCount > 1) {
                clientWidth -= (metadata.visibleColumnCount - 1) * horizontalSpacing;
            }

            clientWidth = Math.max(0, clientWidth);

            cellWidthCalc = (metadata.visibleColumnCount == 0) ? 0 : clientWidth
                / metadata.visibleColumnCount;
        }

        return cellWidthCalc;
    }

    /**
     * @return The cell height given the specified grid pane height and metadata.
     *
     * @param height The overall height of the grid pane (can be -1).
     * @param metadata Information about visible rows and columns.
     */
    private int getCellHeight(int height, Metadata metadata) {
        int cellHeightCalc = -1;

        if (height != -1) {
            int clientHeight = height - padding.getHeight();

            if (metadata.visibleRowCount > 1) {
                clientHeight -= (metadata.visibleRowCount - 1) * verticalSpacing;
            }

            clientHeight = Math.max(0, clientHeight);

            cellHeightCalc = (metadata.visibleRowCount == 0) ? 0 : clientHeight
                / metadata.visibleRowCount;
        }

        return cellHeightCalc;
    }

    /**
     * @return The padding that will be reserved around the grid pane during layout.
     */
    public final Insets getPadding() {
        return padding;
    }

    /**
     * Sets the padding that will be reserved around the grid pane during layout.
     *
     * @param padding The individual padding values for each side.
     */
    public final void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the padding that will be reserved around the grid pane during layout.
     *
     * @param padding The single value to use for all sides.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the grid pane during layout.
     *
     * @param padding The dictionary of padding values, containing {@code "top"},
     * {@code "bottom"}, {@code "left}, and {@code "right"} entries.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the grid pane during layout.
     *
     * @param padding A sequence with values in the order {top, left, bottom, right}.
     */
    public final void setPadding(Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the grid pane during layout.
     *
     * @param padding The single value to use for all sides.
     */
    public final void setPadding(Number padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the grid pane during layout.
     *
     * @param padding A JSON-format string containing the padding values.
     */
    public final void setPadding(String padding) {
        setPadding(Insets.decode(padding));
    }

    /**
     * @return The spacing that will be applied in between the grid pane's columns
     * during layout.
     */
    public final int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Sets the spacing that will be applied in between the grid pane's columns
     * during layout.
     *
     * @param horizontalSpacing The non-negative spacing value between columns.
     */
    public final void setHorizontalSpacing(int horizontalSpacing) {
        Utils.checkNonNegative(horizontalSpacing, "horizontalSpacing");

        this.horizontalSpacing = horizontalSpacing;
        invalidateComponent();
    }

    /**
     * @return The spacing that will be applied in between the grid pane's rows
     * during layout.
     */
    public final int getVerticalSpacing() {
        return verticalSpacing;
    }

    /**
     * Sets the spacing that will be applied in between the grid pane's rows
     * during layout.
     *
     * @param verticalSpacing The non-negative spacing value between rows.
     */
    public final void setVerticalSpacing(int verticalSpacing) {
        Utils.checkNonNegative(verticalSpacing, "verticalSpacing");

        this.verticalSpacing = verticalSpacing;
        invalidateComponent();
    }

    /**
     * @return Whether or not horizontal grid lines will be painted in between the
     * grid pane's rows.
     */
    public final boolean getShowHorizontalGridLines() {
        return showHorizontalGridLines;
    }

    /**
     * Sets whether or not horizontal grid lines will be painted in between the
     * grid pane's rows.
     *
     * @param showHorizontalGridLines Whether to show horizontal grid lines.
     */
    public final void setShowHorizontalGridLines(boolean showHorizontalGridLines) {
        this.showHorizontalGridLines = showHorizontalGridLines;
        repaintComponent();
    }

    /**
     * @return Whether or not vertical grid lines will be painted in between the
     * grid pane's columns.
     */
    public final boolean getShowVerticalGridLines() {
        return showVerticalGridLines;
    }

    /**
     * Sets whether or not vertical grid lines will be painted in between the
     * grid pane's columns.
     *
     * @param showVerticalGridLines Whether to show the vertical grid lines.
     */
    public final void setShowVerticalGridLines(boolean showVerticalGridLines) {
        this.showVerticalGridLines = showVerticalGridLines;
        repaintComponent();
    }

    /**
     * @return The color used to paint the grid pane's horizontal grid lines.
     */
    public final Color getHorizontalGridColor() {
        return horizontalGridColor;
    }

    /**
     * Sets the color used to paint the grid pane's horizontal grid lines.
     *
     * @param horizontalGridColor The new color for the horizontal grid lines.
     */
    public final void setHorizontalGridColor(Color horizontalGridColor) {
        Utils.checkNull(horizontalGridColor, "horizontalGridColor");

        this.horizontalGridColor = horizontalGridColor;

        if (showHorizontalGridLines || showVerticalGridLines) {
            repaintComponent();
        }
    }

    /**
     * Sets the color used to paint the grid pane's horizontal grid lines.
     *
     * @param horizontalGridColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public final void setHorizontalGridColor(String horizontalGridColor) {
        setHorizontalGridColor(GraphicsUtilities.decodeColor(horizontalGridColor, "horizontalGridColor"));
    }

    /**
     * @return The color used to paint the grid pane's vertical grid lines.
     */
    public final Color getVerticalGridColor() {
        return verticalGridColor;
    }

    /**
     * Sets the color used to paint the grid pane's vertical grid lines.
     *
     * @param verticalGridColor The new color for vertical grid lines.
     */
    public final void setVerticalGridColor(Color verticalGridColor) {
        Utils.checkNull(verticalGridColor, "verticalGridColor");

        this.verticalGridColor = verticalGridColor;

        if (showHorizontalGridLines || showVerticalGridLines) {
            repaintComponent();
        }
    }

    /**
     * Sets the color used to paint the grid pane's vertical grid lines.
     *
     * @param verticalGridColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public final void setVerticalGridColor(String verticalGridColor) {
        setVerticalGridColor(GraphicsUtilities.decodeColor(verticalGridColor, "verticalGridColor"));
    }

    // GridPane.Skin methods

    @Override
    public int getRowAt(int y) {
        GridPane gridPane = (GridPane) getComponent();
        GridPane.RowSequence rows = gridPane.getRows();

        int rowCount = rows.getLength();

        int rowIndex = -1;
        int rowY = padding.top;

        for (int i = 0; rowY <= y && i < rowCount; i++) {

            if (y < rowY + cellHeight) {
                rowIndex = i;
                break;
            }

            rowY += cellHeight + verticalSpacing;
        }

        return rowIndex;
    }

    @Override
    public Bounds getRowBounds(int row) {
        GridPane gridPane = (GridPane) getComponent();
        GridPane.RowSequence rows = gridPane.getRows();

        int rowCount = rows.getLength();

        if (row < 0 || row >= rowCount) {
            throw new IndexOutOfBoundsException(String.valueOf(row));
        }

        int rowY = padding.top;

        for (int i = 0; i < row; i++) {
            rowY += cellHeight + verticalSpacing;
        }

        return new Bounds(0, rowY, getWidth(), cellHeight);
    }

    @Override
    public int getColumnAt(int x) {
        GridPane gridPane = (GridPane) getComponent();

        int columnCount = gridPane.getColumnCount();
        int columnIndex = -1;

        for (int j = 0, columnX = padding.left; columnX <= x && j < columnCount; j++) {

            if (x < columnX + cellWidth) {
                columnIndex = j;
                break;
            }

            columnX += cellWidth + horizontalSpacing;
        }

        return columnIndex;
    }

    @Override
    public Bounds getColumnBounds(int column) {
        GridPane gridPane = (GridPane) getComponent();
        int columnCount = gridPane.getColumnCount();

        if (column < 0 || column >= columnCount) {
            throw new IndexOutOfBoundsException(String.valueOf(column));
        }

        int columnX = padding.left;

        for (int j = 0; j < column; j++) {
            columnX += cellWidth + horizontalSpacing;
        }

        return new Bounds(columnX, 0, cellWidth, getHeight());
    }

    // GridPaneListener methods
    @Override
    public void columnCountChanged(GridPane gridPane, int previousColumnCount) {
        invalidateComponent();
    }

    @Override
    public void rowInserted(GridPane gridPane, int index) {
        invalidateComponent();
    }

    @Override
    public void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows) {
        invalidateComponent();
    }

    @Override
    public void cellInserted(GridPane.Row row, int column) {
        invalidateComponent();
    }

    @Override
    public void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed) {
        invalidateComponent();
    }

    @Override
    public void cellUpdated(GridPane.Row row, int column, Component previousComponent) {
        invalidateComponent();
    }

}
