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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Transparency;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewColumnListener;
import org.apache.pivot.wtk.TableViewListener;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * Table view skin.
 * <p>
 * NOTE This skin assumes a fixed renderer height.
 * <p>
 * TODO Add disableMouseSelection style to support the case where selection
 * should be enabled but the caller wants to implement the management of it;
 * e.g. changing a message's flag state in an email client.
 */
public class TerraTableViewSkin extends ComponentSkin implements TableView.Skin,
    TableViewListener, TableViewColumnListener, TableViewRowListener,
    TableViewSelectionListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightBackgroundColor;
    private Color alternateRowColor;
    private Color columnSelectionColor;
    private Color columnSelectionHorizontalGridColor;
    private Color horizontalGridColor;
    private Color verticalGridColor;
    private boolean showHighlight;
    private boolean showHorizontalGridLines;
    private boolean showVerticalGridLines;
    private boolean includeTrailingVerticalGridLine;
    private boolean includeTrailingHorizontalGridLine;

    private ArrayList<Integer> columnWidths = null;
    private int rowHeight = -1;

    private int highlightedIndex = -1;
    private int editIndex = -1;

    public TerraTableViewSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(4);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(19);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);
        highlightBackgroundColor = theme.getColor(10);
        alternateRowColor = theme.getColor(11);
        columnSelectionColor = null;
        columnSelectionHorizontalGridColor = null;
        horizontalGridColor = theme.getColor(11);
        verticalGridColor = theme.getColor(11);

        showHighlight = true;
        showHorizontalGridLines = true;
        showVerticalGridLines = true;
        includeTrailingVerticalGridLine = false;
        includeTrailingHorizontalGridLine = false;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        TableView tableView = (TableView)component;
        tableView.getTableViewListeners().add(this);
        tableView.getTableViewColumnListeners().add(this);
        tableView.getTableViewRowListeners().add(this);
        tableView.getTableViewSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TableView tableView = (TableView)getComponent();
        TableView.ColumnSequence columns = tableView.getColumns();

        int n = columns.getLength();
        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (!column.isRelative()) {
                int columnWidth = column.getWidth();

                if (columnWidth == -1) {
                    // Calculate the maximum cell width
                    columnWidth = 0;

                    TableView.CellRenderer cellRenderer = column.getCellRenderer();
                    List<?> tableData = tableView.getTableData();

                    int rowIndex = 0;
                    for (Object rowData : tableData) {
                        cellRenderer.render(rowData, rowIndex++, i, tableView, column.getName(),
                            false, false, false);
                        columnWidth = Math.max(cellRenderer.getPreferredWidth(-1), columnWidth);
                    }
                }

                preferredWidth += Math.min(Math.max(columnWidth, column.getMinimumWidth()), column.getMaximumWidth());
            }
            else {
                preferredWidth += column.getMinimumWidth();
            }
        }

        // Include space for vertical gridlines; even if we are not painting them,
        // the header does
        preferredWidth += (n - 1);

        if (includeTrailingVerticalGridLine) {
            preferredWidth++;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        TableView tableView = (TableView)getComponent();

        int n = tableView.getTableData().getLength();

        int rowHeight = getRowHeight();
        preferredHeight = rowHeight * n;

        // Include space for horizontal grid lines
        preferredHeight += (n - 1);

        if (includeTrailingHorizontalGridLine) {
            preferredHeight++;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public void layout() {
        // Recalculate column widths
        TableView tableView = (TableView)getComponent();

        int width = getWidth();

        int fixedWidth = 0;
        int relativeWidth = 0;

        TableView.ColumnSequence columns = tableView.getColumns();
        int n = columns.getLength();

        columnWidths = new ArrayList<Integer>(n);

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.isRelative()) {
                columnWidths.add(0);
                relativeWidth += column.getWidth();
            } else {
                int columnWidth = column.getWidth();

                if (columnWidth == -1) {
                    // Calculate the maximum cell width
                    columnWidth = 0;

                    TableView.CellRenderer cellRenderer = column.getCellRenderer();
                    List<?> tableData = tableView.getTableData();

                    int rowIndex = 0;
                    for (Object rowData : tableData) {
                        cellRenderer.render(rowData, rowIndex++, i, tableView, column.getName(),
                            false, false, false);
                        columnWidth = Math.max(cellRenderer.getPreferredWidth(-1), columnWidth);
                    }
                }

                columnWidth = Math.min(Math.max(columnWidth, column.getMinimumWidth()), column.getMaximumWidth());
                columnWidths.add(columnWidth);
                fixedWidth += columnWidth;
            }
        }

        fixedWidth += n - 1;
        int variableWidth = Math.max(width - fixedWidth, 0);

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.isRelative()) {
                int columnWidth = (int)Math.round((double)(column.getWidth()
                    * variableWidth) / (double)relativeWidth);
                columnWidths.update(i, Math.min(Math.max(columnWidth, column.getMinimumWidth()),
                    column.getMaximumWidth()));
            }
        }

        // Recalculate row height
        rowHeight = 0;
        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);
            TableView.CellRenderer cellRenderer = column.getCellRenderer();
            cellRenderer.render(null, -1, i, tableView, column.getName(), false, false, false);

            rowHeight = Math.max(rowHeight, cellRenderer.getPreferredHeight(-1));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void paint(Graphics2D graphics) {
        TableView tableView = (TableView)getComponent();
        List<Object> tableData = (List<Object>)tableView.getTableData();
        TableView.ColumnSequence columns = tableView.getColumns();

        int width = getWidth();
        int height = getHeight();

        int rowHeight = getRowHeight();

        // Paint the background
        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Ensure that we only paint items that are visible
        int rowStart = 0;
        int rowEnd = tableData.getLength() - 1;

        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            rowStart = Math.max(rowStart, (int)Math.floor(clipBounds.y
                / (double)(rowHeight + 1)));
            rowEnd = Math.min(rowEnd, (int)Math.ceil((clipBounds.y
                + clipBounds.height) / (double)(rowHeight + 1)) - 1);
        }

        // Paint the row backgrounds
        int rowY = rowStart * (rowHeight + 1);

        if (alternateRowColor != null) {
            for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                if (rowIndex % 2 > 0) {
                    graphics.setPaint(alternateRowColor);
                    graphics.fillRect(0, rowY, width, rowHeight + 1);
                }

                rowY += rowHeight + 1;
            }
        }

        // Paint the column backgrounds
        int columnX = 0;
        if (columnSelectionColor != null) {
            graphics.setColor(columnSelectionColor);

            columnX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);
                int columnWidth = getColumnWidth(columnIndex);

                String columnName = column.getName();
                SortDirection sortDirection = tableView.getSort().get(columnName);
                if (sortDirection != null) {
                    graphics.fillRect(columnX, 0, columnWidth, height);
                }

                columnX += columnWidth + 1;
            }
        }

        // Paint the table contents
        rowY = rowStart * (rowHeight + 1);

        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
            Object rowData = tableData.get(rowIndex);
            boolean rowHighlighted = (rowIndex == highlightedIndex
                && tableView.getSelectMode() != TableView.SelectMode.NONE);
            boolean rowSelected = tableView.isRowSelected(rowIndex);
            boolean rowDisabled = tableView.isRowDisabled(rowIndex);

            // Paint selection state
            Color rowBackgroundColor = null;
            if (rowSelected) {
                rowBackgroundColor = (tableView.isFocused())
                    ? this.selectionBackgroundColor : inactiveSelectionBackgroundColor;
            } else {
                if (rowHighlighted && showHighlight && !rowDisabled) {
                    rowBackgroundColor = highlightBackgroundColor;
                }
            }

            if (rowBackgroundColor != null) {
                graphics.setPaint(rowBackgroundColor);
                graphics.fillRect(0, rowY, width, rowHeight);
            }

            // Paint the cells
            columnX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);

                TableView.CellRenderer cellRenderer = column.getCellRenderer();

                int columnWidth = getColumnWidth(columnIndex);

                Graphics2D rendererGraphics = (Graphics2D)graphics.create(columnX, rowY,
                    columnWidth, rowHeight);

                cellRenderer.render(rowData, rowIndex, columnIndex, tableView, column.getName(),
                    rowSelected, rowHighlighted, rowDisabled);
                cellRenderer.setSize(columnWidth, rowHeight);
                cellRenderer.paint(rendererGraphics);

                rendererGraphics.dispose();

                columnX += columnWidth + 1;
            }

            rowY += rowHeight + 1;
        }

        // Paint the vertical grid lines
        graphics.setPaint(verticalGridColor);

        if (showVerticalGridLines) {
            columnX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                columnX += getColumnWidth(columnIndex);

                if (columnIndex < columnCount - 1
                    || includeTrailingVerticalGridLine) {
                    GraphicsUtilities.drawLine(graphics, columnX, 0, height, Orientation.VERTICAL);
                }

                columnX++;
            }
        }

        // Paint the horizontal grid lines
        graphics.setPaint(horizontalGridColor);

        if (showHorizontalGridLines) {
            int rowCount = tableData.getLength();

            for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                int gridY = (rowIndex + 1) * (rowHeight + 1) - 1;

                if (rowIndex < rowCount - 1
                    || includeTrailingHorizontalGridLine) {
                    GraphicsUtilities.drawLine(graphics, 0, gridY, width, Orientation.HORIZONTAL);
                }
            }

            if (columnSelectionHorizontalGridColor != null) {
                graphics.setColor(columnSelectionHorizontalGridColor);

                columnX = 0;

                for (int columnIndex = 0, columnCount = columns.getLength();
                    columnIndex < columnCount; columnIndex++) {
                    TableView.Column column = columns.get(columnIndex);
                    int columnWidth = getColumnWidth(columnIndex);

                    String columnName = column.getName();
                    SortDirection sortDirection = tableView.getSort().get(columnName);
                    if (sortDirection != null) {
                        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                            int gridY = (rowIndex + 1) * (rowHeight + 1) - 1;

                            if (rowIndex < rowCount - 1
                                || includeTrailingHorizontalGridLine) {
                                GraphicsUtilities.drawLine(graphics, columnX, gridY, columnWidth,
                                    Orientation.HORIZONTAL);
                            }
                        }
                    }

                    columnX += columnWidth + 1;
                }
            }
        }
    }

    /**
     * Returns the table row height, which is determined as the maximum
     * preferred height of all cell renderers.
     *
     * @return
     * The height of one table row.
     */
    public int getRowHeight() {
        if (rowHeight == -1) {
            layout();
        }

        return rowHeight;
    }

    // Table view skin methods
    @Override
    @SuppressWarnings("unchecked")
    public int getRowAt(int y) {
        if (y < 0) {
            throw new IllegalArgumentException("y is negative");
        }

        TableView tableView = (TableView)getComponent();
        List<Object> tableData = (List<Object>)tableView.getTableData();

        int rowHeight = getRowHeight();
        int rowIndex = (y / (rowHeight + 1));

        if (rowIndex >= tableData.getLength()) {
            rowIndex = -1;
        }

        return rowIndex;
    }

    @Override
    public int getColumnAt(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("x is negative");
        }

        TableView tableView = (TableView)getComponent();

        int i = 0;
        int n = tableView.getColumns().getLength();
        int columnX = 0;
        while (i < n
            && x > columnX) {
            columnX += (getColumnWidth(i) + 1);
            i++;
        }

        int columnIndex = -1;

        if (x <= columnX) {
            columnIndex = i - 1;
        }

        return columnIndex;
    }

    @Override
    public Bounds getRowBounds(int rowIndex) {
        int rowHeight = getRowHeight();
        return new Bounds(0, rowIndex * (rowHeight + 1), getWidth(), rowHeight);
    }

    public int getColumnWidth(int columnIndex) {
        if (columnWidths == null) {
            layout();
        }

        if (columnIndex < 0
            || columnIndex >= columnWidths.getLength()) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " +
                columnIndex);
        }

        return columnWidths.get(columnIndex);
    }

    @Override
    public Bounds getColumnBounds(int columnIndex) {
        int columnX = 0;
        for (int i = 0; i < columnIndex; i++) {
            columnX += (getColumnWidth(i) + 1);
        }

        return new Bounds(columnX, 0, getColumnWidth(columnIndex), getHeight());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Bounds getCellBounds(int rowIndex, int columnIndex) {
        TableView tableView = (TableView)getComponent();
        List<Object> tableData = (List<Object>)tableView.getTableData();

        if (rowIndex < 0
            || rowIndex >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        int cellX = 0;
        for (int i = 0; i < columnIndex; i++) {
            cellX += (getColumnWidth(i) + 1);
        }

        int rowHeight = getRowHeight();

        return new Bounds(cellX, rowIndex * (rowHeight + 1), getColumnWidth(columnIndex), rowHeight);
    }

    @Override
    public boolean isFocusable() {
        TableView tableView = (TableView)getComponent();
        return (tableView.getSelectMode() != TableView.SelectMode.NONE);
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public final void setDisabledColor(int disabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
    }

    public final void setSelectionColor(int selectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionColor(theme.getColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
    }

    public final void setSelectionBackgroundColor(int selectionBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor));
    }

    public final void setInactiveSelectionColor(int inactiveSelectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor));
    }

    public final void setInactiveSelectionBackgroundColor(int inactiveSelectionBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
    }

    public final void setHighlightBackgroundColor(int highlightBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setHighlightBackgroundColor(theme.getColor(highlightBackgroundColor));
    }

    public Color getAlternateRowColor() {
        return alternateRowColor;
    }

    public void setAlternateRowColor(Color alternateRowColor) {
        this.alternateRowColor = alternateRowColor;
        repaintComponent();
    }

    public final void setAlternateRowColor(String alternateRowColor) {
        if (alternateRowColor == null) {
            throw new IllegalArgumentException("alternateRowColor is null.");
        }

        setAlternateRowColor(GraphicsUtilities.decodeColor(alternateRowColor));
    }

    public final void setAlternateRowColor(int alternateRowColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setAlternateRowColor(theme.getColor(alternateRowColor));
    }

    public Color getColumnSelectionColor() {
        return columnSelectionColor;
    }

    public void setColumnSelectionColor(Color columnSelectionColor) {
        this.columnSelectionColor = columnSelectionColor;
        repaintComponent();
    }

    public final void setColumnSelectionColor(String columnSelectionColor) {
        if (columnSelectionColor == null) {
            throw new IllegalArgumentException("columnSelectionColor is null.");
        }

        setColumnSelectionColor(GraphicsUtilities.decodeColor(columnSelectionColor));
    }

    public final void setColumnSelectionColor(int columnSelectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColumnSelectionColor(theme.getColor(columnSelectionColor));
    }

    public Color getColumnSelectionHorizontalGridColor() {
        return columnSelectionHorizontalGridColor;
    }

    public void setColumnSelectionHorizontalGridColor(Color columnSelectionHorizontalGridColor) {
        if (columnSelectionHorizontalGridColor == null) {
            throw new IllegalArgumentException("columnSelectionHorizontalGridColor is null.");
        }

        this.columnSelectionHorizontalGridColor = columnSelectionHorizontalGridColor;
        repaintComponent();
    }

    public final void setColumnSelectionHorizontalGridColor(String columnSelectionHorizontalGridColor) {
        if (columnSelectionHorizontalGridColor == null) {
            throw new IllegalArgumentException("columnSelectionHorizontalGridColor is null.");
        }

        setColumnSelectionHorizontalGridColor(GraphicsUtilities.decodeColor(columnSelectionHorizontalGridColor));
    }

    public final void setColumnSelectionHorizontalGridColor(int columnSelectionHorizontalGridColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColumnSelectionHorizontalGridColor(theme.getColor(columnSelectionHorizontalGridColor));
    }

    public Color getHorizontalGridColor() {
        return horizontalGridColor;
    }

    public void setHorizontalGridColor(Color horizontalGridColor) {
        if (horizontalGridColor == null) {
            throw new IllegalArgumentException("horizontalGridColor is null.");
        }

        this.horizontalGridColor = horizontalGridColor;
        repaintComponent();
    }

    public final void setHorizontalGridColor(String horizontalGridColor) {
        if (horizontalGridColor == null) {
            throw new IllegalArgumentException("horizontalGridColor is null.");
        }

        setHorizontalGridColor(GraphicsUtilities.decodeColor(horizontalGridColor));
    }

    public final void setHorizontalGridColor(int horizontalGridColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setHorizontalGridColor(theme.getColor(horizontalGridColor));
    }

    public Color getVerticalGridColor() {
        return verticalGridColor;
    }

    public void setVerticalGridColor(Color verticalGridColor) {
        if (verticalGridColor == null) {
            throw new IllegalArgumentException("verticalGridColor is null.");
        }

        this.verticalGridColor = verticalGridColor;
        repaintComponent();
    }

    public final void setVerticalGridColor(String verticalGridColor) {
        if (verticalGridColor == null) {
            throw new IllegalArgumentException("verticalGridColor is null.");
        }

        setVerticalGridColor(GraphicsUtilities.decodeColor(verticalGridColor));
    }

    public final void setVerticalGridColor(int verticalGridColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setVerticalGridColor(theme.getColor(verticalGridColor));
    }

    public boolean getShowHighlight() {
        return showHighlight;
    }

    public void setShowHighlight(boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    public boolean getShowHorizontalGridLines() {
        return showHorizontalGridLines;
    }

    public void setShowHorizontalGridLines(boolean showHorizontalGridLines) {
        this.showHorizontalGridLines = showHorizontalGridLines;
        repaintComponent();
    }

    public boolean getShowVerticalGridLines() {
        return showVerticalGridLines;
    }

    public void setShowVerticalGridLines(boolean showVerticalGridLines) {
        this.showVerticalGridLines = showVerticalGridLines;
        repaintComponent();
    }

    public boolean getIncludeTrailingVerticalGridLine() {
        return includeTrailingVerticalGridLine;
    }

    public void setIncludeTrailingVerticalGridLine(boolean includeTrailingVerticalGridLine) {
        this.includeTrailingVerticalGridLine = includeTrailingVerticalGridLine;
        invalidateComponent();
    }

    public boolean getIncludeTrailingHorizontalGridLine() {
        return includeTrailingHorizontalGridLine;
    }

    public void setIncludeTrailingHorizontalGridLine(boolean includeTrailingHorizontalGridLine) {
        this.includeTrailingHorizontalGridLine = includeTrailingHorizontalGridLine;
        invalidateComponent();
    }

    @Override
    protected void invalidateComponent() {
        super.invalidateComponent();
        columnWidths = null;
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        TableView tableView = (TableView)getComponent();

        int previousHighlightedIndex = this.highlightedIndex;
        highlightedIndex = getRowAt(y);

        if (previousHighlightedIndex != highlightedIndex
            && tableView.getSelectMode() != TableView.SelectMode.NONE
            && showHighlight) {
            if (previousHighlightedIndex != -1) {
                repaintComponent(getRowBounds(previousHighlightedIndex));
            }

            if (highlightedIndex != -1) {
                repaintComponent(getRowBounds(highlightedIndex));
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        TableView tableView = (TableView)getComponent();

        if (highlightedIndex != -1
            && tableView.getSelectMode() != TableView.SelectMode.NONE
            && showHighlight) {
            repaintComponent(getRowBounds(highlightedIndex));
        }

        highlightedIndex = -1;
        editIndex = -1;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        TableView tableView = (TableView)getComponent();
        int rowIndex = getRowAt(y);

        if (rowIndex >= 0
            && !tableView.isRowDisabled(rowIndex)) {
            TableView.SelectMode selectMode = tableView.getSelectMode();

            if (button == Mouse.Button.RIGHT) {
                if (!tableView.isRowSelected(rowIndex)
                    && selectMode != TableView.SelectMode.NONE) {
                    tableView.setSelectedIndex(rowIndex);
                }
            } else {
                Keyboard.Modifier commandModifier = Keyboard.getCommandModifier();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                    && selectMode == TableView.SelectMode.MULTI) {
                    Filter<?> disabledRowFilter = tableView.getDisabledRowFilter();

                    if (disabledRowFilter == null) {
                        // Select the range
                        int startIndex = tableView.getFirstSelectedIndex();
                        int endIndex = tableView.getLastSelectedIndex();
                        Span selectedRange = (rowIndex > startIndex) ?
                            new Span(startIndex, rowIndex) : new Span(rowIndex, endIndex);

                        ArrayList<Span> selectedRanges = new ArrayList<Span>();
                        selectedRanges.add(selectedRange);

                        tableView.setSelectedRanges(selectedRanges);
                    }
                } else if (Keyboard.isPressed(commandModifier)
                    && selectMode == TableView.SelectMode.MULTI) {
                    // Toggle the item's selection state
                    if (tableView.isRowSelected(rowIndex)) {
                        tableView.removeSelectedIndex(rowIndex);
                    } else {
                        tableView.addSelectedIndex(rowIndex);
                    }
                } else {
                    if (selectMode != TableView.SelectMode.NONE) {
                        if (tableView.isRowSelected(rowIndex)
                            && tableView.isFocused()) {
                            // Edit the row
                            editIndex = rowIndex;
                        }

                        // Select the row
                        tableView.setSelectedIndex(rowIndex);
                    }
                }
            }
        }

        tableView.requestFocus();

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        TableView tableView = (TableView)getComponent();
        if (editIndex != -1
            && count == 2) {
            TableView.RowEditor rowEditor = tableView.getRowEditor();

            if (rowEditor != null
                && !rowEditor.isEditing()) {
                rowEditor.editRow(tableView, editIndex, getColumnAt(x));
            }
        }

        editIndex = -1;

        return consumed;
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        TableView tableView = (TableView)getComponent();

        if (highlightedIndex != -1) {
            Bounds rowBounds = getRowBounds(highlightedIndex);

            highlightedIndex = -1;

            if (tableView.getSelectMode() != TableView.SelectMode.NONE
                && showHighlight) {
                repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height, true);
            }
        }

        return super.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        TableView tableView = (TableView)getComponent();

        switch (keyCode) {
            case Keyboard.KeyCode.UP: {
                int index = tableView.getFirstSelectedIndex();

                do {
                    index--;
                } while (index >= 0
                    && tableView.isRowDisabled(index));

                if (index >= 0) {
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                        && tableView.getSelectMode() == TableView.SelectMode.MULTI) {
                        tableView.addSelectedIndex(index);
                    } else {
                        tableView.setSelectedIndex(index);
                    }

                    tableView.scrollAreaToVisible(getRowBounds(index));
                }

                consumed = true;
                break;
            }

            case Keyboard.KeyCode.DOWN: {
                int index = tableView.getLastSelectedIndex();
                int count = tableView.getTableData().getLength();

                do {
                    index++;
                } while (index < count
                    && tableView.isRowDisabled(index));

                if (index < count) {
                    if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                        && tableView.getSelectMode() == TableView.SelectMode.MULTI) {
                        tableView.addSelectedIndex(index);
                    } else {
                        tableView.setSelectedIndex(index);
                    }

                    tableView.scrollAreaToVisible(getRowBounds(index));
                }

                consumed = true;
                break;
            }
        }

        // Clear the highlight
        if (highlightedIndex != -1
            && tableView.getSelectMode() != TableView.SelectMode.NONE
            && showHighlight) {
            repaintComponent(getRowBounds(highlightedIndex));
        }

        highlightedIndex = -1;

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        repaintComponent();
    }

    // Table view events
    @Override
    public void tableDataChanged(TableView tableView, List<?> previousTableData) {
        invalidateComponent();
    }

    @Override
    public void columnSourceChanged(TableView tableView, TableView previousColumnSource) {
        if (previousColumnSource != null) {
            previousColumnSource.getTableViewColumnListeners().remove(this);
        }

        TableView columnSource = tableView.getColumnSource();

        if (columnSource != null) {
            columnSource.getTableViewColumnListeners().add(this);
        }

        invalidateComponent();
    }

    @Override
    public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
        // No-op
    }

    @Override
    public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
        repaintComponent();
    }

    @Override
    public void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter) {
        repaintComponent();
    }

    // Table view column events
    @Override
    public void columnInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    @Override
    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
        invalidateComponent();
    }

    @Override
    public void columnNameChanged(TableView.Column column, String previousName) {
        invalidateComponent();
    }

    @Override
    public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
        // No-op
    }

    @Override
    public void columnWidthChanged(TableView.Column column, int previousWidth, boolean previousRelative)  {
        invalidateComponent();
    }

    @Override
    public void columnWidthLimitsChanged(TableView.Column column, int previousMinimumWidth, int previousMaximumWidth) {
        invalidateComponent();
    }

    @Override
    public void columnFilterChanged(TableView.Column column, Object previousFilter) {
        // No-op
    }

    @Override
    public void columnCellRendererChanged(TableView.Column column, TableView.CellRenderer previousCellRenderer) {
        invalidateComponent();
    }

    // Table view row events
    @Override
    public void rowInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    @Override
    public void rowsRemoved(TableView tableView, int index, int count) {
        invalidateComponent();
    }

    @Override
    public void rowUpdated(TableView tableView, int index) {
        repaintComponent(getRowBounds(index));
    }

    @Override
    public void rowsCleared(TableView listView) {
        invalidateComponent();
    }

    @Override
    public void rowsSorted(TableView tableView) {
        repaintComponent();
    }

    // Table view selection detail events
    @Override
    public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the added selection
        int rowHeight = getRowHeight();
        repaintComponent(0, rangeStart * (rowHeight + 1),
            getWidth(), (rangeEnd - rangeStart + 1) * (rowHeight + 1));
    }

    @Override
    public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the removed selection
        int rowHeight = getRowHeight();
        repaintComponent(0, rangeStart * (rowHeight + 1),
            getWidth(), (rangeEnd - rangeStart + 1) * (rowHeight + 1));
    }

    @Override
    public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
        // TODO Repaint only the area that changed (intersection of previous
        // and new selection)
        repaintComponent();
    }
}
