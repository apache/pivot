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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.GridPaneListener;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;

/**
 * Grid pane skin.
 */
public class GridPaneSkin extends ContainerSkin implements GridPane.Skin,
    GridPaneListener {
    private Insets padding = Insets.NONE;
    private int horizontalSpacing = 0;
    private int verticalSpacing = 0;
    private boolean showHorizontalGridLines = false;
    private boolean showVerticalGridLines = false;
    private Color horizontalGridColor = Color.BLACK;
    private Color verticalGridColor = Color.BLACK;
    private Color highlightBackgroundColor = Color.GRAY;

    private int cellWidth = 0;
    private int cellHeight = 0;

    @Override
    public void install(Component component) {
        super.install(component);

        GridPane gridPane = (GridPane)component;
        gridPane.getGridPaneListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        GridPane gridPane = (GridPane)getComponent();
        GridPane.RowSequence rows = gridPane.getRows();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();
        boolean[] visibleColumns = new boolean[columnCount];
        boolean[] visibleRows = new boolean[rowCount];

        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    visibleColumns[j] = true;
                    visibleRows[i] = true;
                }
            }
        }

        int visibleRowCount = 0;
        int clientHeight = height - padding.top - padding.bottom;
        for (int i = 0; i < rowCount; i++) {
            if (visibleRows[i]) {
                visibleRowCount++;
            }
        }

        if (visibleRowCount > 1) {
            clientHeight -= (visibleRowCount - 1) * verticalSpacing;
        }
        int cellHeight = 0;
        if (visibleRowCount > 0) {
            cellHeight = clientHeight / visibleRowCount;
        }

        int cellPreferredWidth = 0;
        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    cellPreferredWidth = Math.max(cellPreferredWidth, component.getPreferredWidth(cellHeight));
                }
            }
        }

        // The preferred width of the grid pane is the sum of the column
        // widths, plus padding and spacing

        int visibleColumnCount = 0;
        int preferredWidth = padding.left + padding.right;

        for (int j = 0; j < columnCount; j++) {
            if (visibleColumns[j]) {
                preferredWidth += cellPreferredWidth;
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
        GridPane gridPane = (GridPane)getComponent();
        GridPane.RowSequence rows = gridPane.getRows();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();
        boolean[] visibleColumns = new boolean[columnCount];
        boolean[] visibleRows = new boolean[rowCount];

        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    visibleColumns[j] = true;
                    visibleRows[i] = true;
                }
            }
        }

        int visibleColumnCount = 0;
        int clientWidth = width - padding.left - padding.right;
        for (int i = 0; i < columnCount; i++) {
            if (visibleColumns[i]) {
                visibleColumnCount++;
            }
        }

        if (visibleColumnCount > 1) {
            clientWidth -= (visibleColumnCount - 1) * horizontalSpacing;
        }
        int cellWidth = 0;
        if (visibleColumnCount > 0) {
            cellWidth = clientWidth / visibleColumnCount;
        }

        // The preferred height of the grid pane is the sum of the row
        // heights, plus padding and spacing

        int visibleRowCount = 0;
        int preferredHeight = padding.top + padding.bottom;

        for (int i = 0; i < rowCount; i++) {

            if (visibleRows[i]) {
                preferredHeight += cellWidth;
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
        GridPane gridPane = (GridPane)getComponent();
        GridPane.RowSequence rows = gridPane.getRows();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();
        boolean[] visibleColumns = new boolean[columnCount];
        boolean[] visibleRows = new boolean[rowCount];

        // calculate the maximum preferred cellWidth and cellHeight
        int preferredCellHeight = 0;
        int preferredCellWidth = 0;
        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component component = row.get(j);

                if (component != null
                    && component.isVisible()) {
                    visibleColumns[j] = true;
                    visibleRows[i] = true;
                    Dimensions d = component.getPreferredSize();
                    preferredCellHeight = Math.max(preferredCellHeight, d.height);
                    preferredCellWidth = Math.max(preferredCellWidth, d.width);
                }
            }
        }

        // The preferred width of the grid pane is the sum of the column
        // widths, plus padding and spacing

        int visibleColumnCount = 0;
        int preferredWidth = padding.left + padding.right;

        for (int j = 0; j < columnCount; j++) {
            if (visibleColumns[j]) {
                preferredWidth += preferredCellWidth;
                visibleColumnCount++;
            }
        }

        if (visibleColumnCount > 1) {
            preferredWidth += (visibleColumnCount - 1) * horizontalSpacing;
        }

        // The preferred height of the grid pane is the sum of the row
        // heights, plus padding and spacing

        int visibleRowCount = 0;
        int preferredHeight = padding.top + padding.bottom;
        for (int i = 0; i < rowCount; i++) {
            if (visibleRows[i]) {
                visibleRowCount++;
            }
        }

        if (visibleRowCount > 1) {
            preferredHeight += (visibleRowCount - 1) * verticalSpacing;
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        // TODO Return the first available baseline by traversing cells top left to bottom right

        // Include top padding value
        if (baseline != -1) {
            baseline += padding.top;
        }

        return baseline;
    }

    @Override
    public void layout() {
        GridPane gridPane = (GridPane)getComponent();

        GridPane.RowSequence rows = gridPane.getRows();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int width = getWidth();
        int height = getHeight();

        // Determine which rows and column should be visible so we know which
        // ones should be collapsed
        boolean[] visibleRows = new boolean[rowCount];
        boolean[] visibleColumns = new boolean[columnCount];
        int cellPreferredHeight = 0;
        int cellPreferredWidth = 0;

        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component child = row.get(j);

                if (child != null
                    && child.isVisible()) {
                    visibleRows[i] = true;
                    visibleColumns[j] = true;
                    Dimensions d = child.getPreferredSize();
                    cellPreferredHeight = Math.max(cellPreferredHeight, d.height);
                    cellPreferredWidth = Math.max(cellPreferredWidth, d.width);
                }
            }
        }


        // Calculate cell width

        int visibleColumnCount = 0;
        int clientWidth = width - padding.left - padding.right;
        for (int i = 0; i < columnCount; i++) {
            if (visibleColumns[i]) {
                visibleColumnCount++;
            }
        }

        if (visibleColumnCount > 1) {
            clientWidth -= (visibleColumnCount - 1) * horizontalSpacing;
        }
        cellWidth = 0;
        if (visibleColumnCount > 0) {
            cellWidth = clientWidth / visibleColumnCount;
        }


        // Calculate cell height

        int visibleRowCount = 0;
        int clientHeight = height - padding.top - padding.bottom;
        for (int i = 0; i < rowCount; i++) {
            if (visibleRows[i]) {
                visibleRowCount++;
            }
        }

        if (visibleRowCount > 1) {
            clientHeight -= (visibleRowCount - 1) * verticalSpacing;
        }
        cellHeight = 0;
        if (visibleRowCount > 0) {
            cellHeight = clientHeight / visibleRowCount;
        }

        int componentY = padding.top;
        for (int i = 0; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            int componentX = padding.left;
            for (int j = 0, n = row.getLength(); j < n && j < columnCount; j++) {
                Component child = row.get(j);

                if (child != null
                    && child.isVisible()) {
                    child.setLocation(componentX, componentY);

                    child.setSize(cellWidth, cellHeight);
                }

                if (visibleColumns[j]) {
                    componentX += (cellWidth + horizontalSpacing);
                }
            }

            if (visibleRows[i]) {
                componentY += (cellHeight + verticalSpacing);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        GridPane gridPane = (GridPane)getComponent();

        GridPane.RowSequence rows = gridPane.getRows();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int rowCount = rows.getLength();
        int columnCount = columns.getLength();

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(highlightBackgroundColor);

        // Paint the highlighted rows
        for (int i = 0, rowY = padding.top; i < rowCount; i++) {
            GridPane.Row row = rows.get(i);

            if (row.isHighlighted()) {
                graphics.fillRect(0, rowY, width, cellHeight);
            }

            rowY += cellHeight + verticalSpacing;
        }

        // Paint the highlighted columns
        for (int j = 0, columnX = padding.left; j < columnCount; j++) {
            GridPane.Column column = columns.get(j);

            if (column.isHighlighted()) {
                graphics.fillRect(columnX, 0, cellWidth, height);
            }

            columnX += cellWidth + horizontalSpacing;
        }

        // Paint the grid lines
        if ((showHorizontalGridLines && verticalSpacing > 0)
            || (showVerticalGridLines && horizontalSpacing > 0)) {
            Graphics2D gridGraphics = (Graphics2D)graphics.create();

            gridGraphics.setStroke(new BasicStroke());
            gridGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            if (showHorizontalGridLines
                && verticalSpacing > 0
                && rowCount > 1) {
                gridGraphics.setPaint(horizontalGridColor);

                int rowY = padding.top + (cellHeight + verticalSpacing);

                for (int i = 1; i < rowCount; i++) {
                    int gridY = Math.max(rowY - (int)Math.ceil(verticalSpacing * 0.5f), 0);
                    GraphicsUtilities.drawLine(gridGraphics, 0, gridY,
                        width, Orientation.HORIZONTAL);

                    rowY += (cellHeight + verticalSpacing);
                }
            }

            if (showVerticalGridLines
                && horizontalSpacing > 0
                && columnCount > 1) {
                gridGraphics.setPaint(verticalGridColor);

                int columnX = padding.left + (cellWidth + horizontalSpacing);

                for (int j = 1; j < columnCount; j++) {
                    int gridX = Math.max(columnX - (int)Math.ceil(horizontalSpacing * 0.5), 0);
                    GraphicsUtilities.drawLine(gridGraphics, gridX, 0,
                        height, Orientation.VERTICAL);

                    columnX += (cellWidth + horizontalSpacing);
                }
            }

            gridGraphics.dispose();
        }
    }

    /**
     * Gets the padding that will be reserved around the grid pane during
     * layout.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the padding that will be reserved around the grid pane during
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
     * Sets the padding that will be reserved around the grid pane during
     * layout.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the grid pane during
     * layout.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Sets the padding that will be reserved around the grid pane during
     * layout.
     */
    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }

    /**
     * Gets the spacing that will be applied in between the grid pane's
     * columns during layout.
     */
    public int getHorizontalSpacing() {
        return horizontalSpacing;
    }

    /**
     * Sets the spacing that will be applied in between the grid pane's
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
     * Gets the spacing that will be applied in between the grid pane's rows
     * during layout.
     */
    public int getVerticalSpacing() {
        return verticalSpacing;
    }

    /**
     * Sets the spacing that will be applied in between the grid pane's rows
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
     * the grid pane's rows.
     */
    public boolean getShowHorizontalGridLines() {
        return showHorizontalGridLines;
    }

    /**
     * Sets whether or not horizontal grid lines will be painted in between
     * the grid pane's rows.
     */
    public void setShowHorizontalGridLines(boolean showHorizontalGridLines) {
        this.showHorizontalGridLines = showHorizontalGridLines;
        repaintComponent();
    }

    /**
     * Tells whether or not vertical grid lines will be painted in between
     * the grid pane's columns.
     */
    public boolean getShowVerticalGridLines() {
        return showVerticalGridLines;
    }

    /**
     * Sets whether or not vertical grid lines will be painted in between
     * the grid pane's columns.
     */
    public void setShowVerticalGridLines(boolean showVerticalGridLines) {
        this.showVerticalGridLines = showVerticalGridLines;
        repaintComponent();
    }

    /**
     * Gets the color used to paint the grid pane's horizontal grid lines.
     */
    public Color getHorizontalGridColor() {
        return horizontalGridColor;
    }

    /**
     * Sets the color used to paint the grid pane's horizontal grid lines.
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
     * Sets the color used to paint the grid pane's horizontal grid lines.
     */
    public final void setHorizontalGridColor(String horizontalGridColor) {
        if (horizontalGridColor == null) {
            throw new IllegalArgumentException("horizontalGridColor is null.");
        }

        setHorizontalGridColor(GraphicsUtilities.decodeColor(horizontalGridColor));
    }

    /**
     * Gets the color used to paint the grid pane's vertical grid lines.
     */
    public Color getVerticalGridColor() {
        return verticalGridColor;
    }

    /**
     * Sets the color used to paint the grid pane's vertical grid lines.
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
     * Sets the color used to paint the grid pane's vertical grid lines.
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
     */
    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
    }

    // GridPane.Skin methods

    @Override
    public int getRowAt(int y) {
        GridPane gridPane = (GridPane)getComponent();
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
        GridPane gridPane = (GridPane)getComponent();
        GridPane.RowSequence rows = gridPane.getRows();

        int rowCount = rows.getLength();

        if (row < 0
            || row >= rowCount) {
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
        GridPane gridPane = (GridPane)getComponent();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int columnCount = columns.getLength();

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
        GridPane gridPane = (GridPane)getComponent();
        GridPane.ColumnSequence columns = gridPane.getColumns();

        int columnCount = columns.getLength();

        if (column < 0
            || column >= columnCount) {
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
    public void rowInserted(GridPane gridPane, int index) {
        invalidateComponent();
    }

    @Override
    public void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows) {
        invalidateComponent();
    }

    @Override
    public void rowHighlightedChanged(GridPane.Row row) {
        GridPane gridPane = row.getGridPane();
        repaintComponent(getRowBounds(gridPane.getRows().indexOf(row)));
    }

    @Override
    public void columnInserted(GridPane gridPane, int index) {
        invalidateComponent();
    }

    @Override
    public void columnsRemoved(GridPane gridPane, int index,
        Sequence<GridPane.Column> columns) {
        invalidateComponent();
    }

    @Override
    public void columnHighlightedChanged(GridPane.Column column) {
        GridPane gridPane = column.getGridPane();
        repaintComponent(getColumnBounds(gridPane.getColumns().indexOf(column)));
    }

    @Override
    public void cellInserted(GridPane.Row row, int column) {
        invalidateComponent();
    }

    @Override
    public void cellsRemoved(GridPane.Row row, int column,
        Sequence<Component> removed) {
        invalidateComponent();
    }

    @Override
    public void cellUpdated(GridPane.Row row, int column,
        Component previousComponent) {
        invalidateComponent();
    }

}
