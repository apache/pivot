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
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableView.SelectMode;
import org.apache.pivot.wtk.TableViewColumnListener;
import org.apache.pivot.wtk.TableViewListener;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * Table view skin. <p> TODO Add disableMouseSelection style to support the case
 * where selection should be enabled but the caller wants to implement the
 * management of it; e.g. changing a message's flag state in an email client.
 */
public class TerraTableViewSkin extends ComponentSkin implements TableView.Skin, TableViewListener,
    TableViewColumnListener, TableViewRowListener, TableViewSelectionListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightBackgroundColor;
    private Color alternateRowBackgroundColor;
    private Color columnSelectionColor;
    private Color columnSelectionHorizontalGridColor;
    private Color horizontalGridColor;
    private Color verticalGridColor;
    private boolean showHighlight;
    private boolean showHorizontalGridLines;
    private boolean showVerticalGridLines;
    private boolean includeTrailingVerticalGridLine;
    private boolean includeTrailingHorizontalGridLine;
    private boolean variableRowHeight;
    private boolean editOnMouseDown;

    private ArrayList<Integer> columnWidths = null;
    private ArrayList<Integer> rowBoundaries = null;
    private int fixedRowHeight = -1;
    private int defaultWidthColumnCount = 0;

    private int highlightIndex = -1;
    private int selectIndex = -1;
    private int lastKeyboardSelectIndex = -1;

    private boolean validateSelection = false;

    public TerraTableViewSkin() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(4);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(14);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);
        highlightBackgroundColor = theme.getColor(10);
        alternateRowBackgroundColor = theme.getColor(11);
        columnSelectionColor = null;
        columnSelectionHorizontalGridColor = null;
        horizontalGridColor = theme.getColor(11);
        verticalGridColor = theme.getColor(11);

        showHighlight = true;
        showHorizontalGridLines = true;
        showVerticalGridLines = true;
        includeTrailingVerticalGridLine = false;
        includeTrailingHorizontalGridLine = false;

        editOnMouseDown = false;
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        TableView tableView = (TableView) component;
        tableView.getTableViewListeners().add(this);
        tableView.getTableViewColumnListeners().add(this);
        tableView.getTableViewRowListeners().add(this);
        tableView.getTableViewSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(final int height) {
        return getPreferredWidth((TableView) getComponent(), includeTrailingVerticalGridLine);
    }

    public static int getPreferredWidth(final TableView tableView, final boolean includeTrailingVerticalGridLine) {
        int preferredWidth = 0;

        TableView.ColumnSequence columns = tableView.getColumns();
        List<?> tableData = tableView.getTableData();

        int n = columns.getLength();
        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (!column.isRelative()) {
                int columnWidth = column.getWidth();

                if (columnWidth == -1) {
                    // Calculate the maximum cell width
                    columnWidth = 0;

                    TableView.CellRenderer cellRenderer = column.getCellRenderer();

                    int rowIndex = 0;
                    for (Object rowData : tableData) {
                        cellRenderer.render(rowData, rowIndex++, i, tableView, column.getName(),
                            false, false, false);
                        columnWidth = Math.max(cellRenderer.getPreferredWidth(-1), columnWidth);
                    }
                }

                preferredWidth += Math.min(Math.max(columnWidth, column.getMinimumWidth()),
                    column.getMaximumWidth());
            } else {
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
    public int getPreferredHeight(final int width) {
        int preferredHeight = 0;

        TableView tableView = (TableView) getComponent();

        int n = tableView.getTableData().getLength();

        if (variableRowHeight) {
            ArrayList<Integer> columnWidthsLocal = getColumnWidths(tableView, width);

            for (int i = 0; i < n; i++) {
                preferredHeight += getVariableRowHeight(i, columnWidthsLocal);
            }
        } else {
            int fixedRowHeightLocal = calculateFixedRowHeight(tableView);
            preferredHeight = fixedRowHeightLocal * n;
        }

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
    public int getBaseline(final int width, final int height) {
        TableView tableView = (TableView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> tableData = (List<Object>) tableView.getTableData();

        int baseline = -1;

        TableView.ColumnSequence columns = tableView.getColumns();
        ArrayList<Integer> columnWidthsLocal = getColumnWidths(tableView, width);

        if (variableRowHeight) {
            int rowHeight = getVariableRowHeight(0, columnWidthsLocal);
            Object rowData = tableData.get(0);

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                TableView.Column column = columns.get(i);
                TableView.CellRenderer cellRenderer = column.getCellRenderer();
                cellRenderer.render(rowData, 0, i, tableView, column.getName(), false, false, false);
                baseline = Math.max(baseline,
                    cellRenderer.getBaseline(columnWidthsLocal.get(i).intValue(), rowHeight));
            }

        } else {
            int rowHeight = calculateFixedRowHeight(tableView);

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                TableView.Column column = columns.get(i);
                TableView.CellRenderer cellRenderer = column.getCellRenderer();
                cellRenderer.render(null, -1, i, tableView, column.getName(), false, false, false);
                baseline = Math.max(baseline,
                    cellRenderer.getBaseline(columnWidthsLocal.get(i).intValue(), rowHeight));
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        columnWidths = getColumnWidths((TableView) getComponent(), getWidth());

        TableView tableView = (TableView) getComponent();
        TableView.ColumnSequence columns = tableView.getColumns();

        if (variableRowHeight) {
            @SuppressWarnings("unchecked")
            List<Object> tableData = (List<Object>) tableView.getTableData();

            int n = tableData.getLength();
            rowBoundaries = new ArrayList<>(n);

            int rowY = 0;
            for (int i = 0; i < n; i++) {
                Object rowData = tableData.get(i);

                int rowHeight = 0;
                for (int columnIndex = 0, columnCount = columns.getLength(); columnIndex < columnCount; columnIndex++) {
                    TableView.Column column = columns.get(columnIndex);

                    TableView.CellRenderer cellRenderer = column.getCellRenderer();

                    int columnWidth = columnWidths.get(columnIndex).intValue();

                    cellRenderer.render(rowData, i, columnIndex, tableView, column.getName(),
                        false, false, false);
                    rowHeight = Math.max(rowHeight, cellRenderer.getPreferredHeight(columnWidth));
                }

                rowY += rowHeight;
                rowBoundaries.add(Integer.valueOf(rowY));
                rowY++;
            }
        } else {
            fixedRowHeight = calculateFixedRowHeight(tableView);
        }

        if (validateSelection) {
            // Ensure that the selection is visible
            Sequence<Span> selectedRanges = tableView.getSelectedRanges();

            if (selectedRanges.getLength() > 0) {
                int rangeStart = selectedRanges.get(0).start;
                int rangeEnd = selectedRanges.get(selectedRanges.getLength() - 1).end;

                Bounds selectionBounds = getRowBounds(rangeStart);
                selectionBounds = selectionBounds.union(getRowBounds(rangeEnd));

                Bounds visibleSelectionBounds = tableView.getVisibleArea(selectionBounds);
                if (visibleSelectionBounds != null
                    && visibleSelectionBounds.height < selectionBounds.height) {
                    tableView.scrollAreaToVisible(selectionBounds);
                }
            }
        }

        validateSelection = false;
    }

    /**
     * Calculates the table row height, which is determined as the maximum
     * preferred height of all cell renderers.
     * @param tableView The table to calculate for.
     * @return The calculated row height.
     */
    private static int calculateFixedRowHeight(final TableView tableView) {
        int fixedRowHeight = 0;
        TableView.ColumnSequence columns = tableView.getColumns();

        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);
            TableView.CellRenderer cellRenderer = column.getCellRenderer();
            cellRenderer.render(null, -1, i, tableView, column.getName(), false, false, false);

            fixedRowHeight = Math.max(fixedRowHeight, cellRenderer.getPreferredHeight(-1));
        }

        return fixedRowHeight;
    }

    @Override
    public void paint(final Graphics2D graphics) {
        TableView tableView = (TableView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> tableData = (List<Object>) tableView.getTableData();
        TableView.ColumnSequence columns = tableView.getColumns();

        int width = getWidth();
        int height = getHeight();

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
            if (variableRowHeight) {
                rowStart = getRowAt(clipBounds.y);
                if (rowStart == -1) {
                    rowStart = tableData.getLength();
                }

                if (rowEnd != -1) {
                    int clipBottom = clipBounds.y + clipBounds.height - 1;
                    clipBottom = Math.min(clipBottom, rowBoundaries.get(rowEnd).intValue() - 1);
                    rowEnd = getRowAt(clipBottom);
                }
            } else {
                rowStart = Math.max(rowStart,
                    (int) Math.floor(clipBounds.y / (double) (fixedRowHeight + 1)));
                rowEnd = Math.min(
                    rowEnd,
                    (int) Math.ceil((clipBounds.y + clipBounds.height)
                        / (double) (fixedRowHeight + 1)) - 1);
            }
        }

        // Paint the row background
        if (alternateRowBackgroundColor != null) {
            for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                int rowY = getRowY(rowIndex);
                int rowHeight = getRowHeight(rowIndex);
                if (rowIndex % 2 > 0) {
                    graphics.setPaint(alternateRowBackgroundColor);
                    graphics.fillRect(0, rowY, width, rowHeight + 1);
                }
            }
        }

        // Paint the column backgrounds
        int columnX = 0;
        if (columnSelectionColor != null) {
            graphics.setColor(columnSelectionColor);

            columnX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength(); columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);
                int columnWidth = columnWidths.get(columnIndex).intValue();

                String columnName = column.getName();
                SortDirection sortDirection = tableView.getSort().get(columnName);
                if (sortDirection != null) {
                    graphics.fillRect(columnX, 0, columnWidth, height);
                }

                columnX += columnWidth + 1;
            }
        }

        // Paint the row content
        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
            Object rowData = tableData.get(rowIndex);
            boolean rowHighlighted = (rowIndex == highlightIndex && tableView.getSelectMode() != SelectMode.NONE);
            boolean rowSelected = tableView.isRowSelected(rowIndex);
            boolean rowDisabled = tableView.isRowDisabled(rowIndex);
            int rowY = getRowY(rowIndex);
            int rowHeight = getRowHeight(rowIndex);

            // Paint selection state
            Color rowBackgroundColor = null;
            if (rowSelected) {
                rowBackgroundColor = (tableView.isFocused()) ? this.selectionBackgroundColor
                    : inactiveSelectionBackgroundColor;
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

            for (int columnIndex = 0, columnCount = columns.getLength(); columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);

                TableView.CellRenderer cellRenderer = column.getCellRenderer();

                int columnWidth = columnWidths.get(columnIndex).intValue();

                Graphics2D rendererGraphics = (Graphics2D) graphics.create(columnX, rowY,
                    columnWidth, rowHeight);

                cellRenderer.render(rowData, rowIndex, columnIndex, tableView, column.getName(),
                    rowSelected, rowHighlighted, rowDisabled);
                cellRenderer.setSize(columnWidth, rowHeight);
                cellRenderer.paint(rendererGraphics);

                rendererGraphics.dispose();

                columnX += columnWidth + 1;
            }
        }

        // Paint the vertical grid lines
        graphics.setPaint(verticalGridColor);

        if (showVerticalGridLines) {
            columnX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength(); columnIndex < columnCount; columnIndex++) {
                columnX += columnWidths.get(columnIndex).intValue();

                if (columnIndex < columnCount - 1 || includeTrailingVerticalGridLine) {
                    if (!themeIsFlat()) {
                        GraphicsUtilities.drawLine(graphics, columnX, 0, height, Orientation.VERTICAL);
                    }
                }

                columnX++;
            }
        }

        // Paint the horizontal grid lines
        graphics.setPaint(horizontalGridColor);

        if (showHorizontalGridLines) {
            int rowCount = tableData.getLength();

            for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                int gridY = getRowY(rowIndex + 1) - 1;

                if (rowIndex < rowCount - 1 || includeTrailingHorizontalGridLine) {
                    if (!themeIsFlat()) {
                        GraphicsUtilities.drawLine(graphics, 0, gridY, width, Orientation.HORIZONTAL);
                    }
                }
            }

            if (columnSelectionHorizontalGridColor != null) {
                graphics.setColor(columnSelectionHorizontalGridColor);

                columnX = 0;

                for (int columnIndex = 0, columnCount = columns.getLength(); columnIndex < columnCount; columnIndex++) {
                    TableView.Column column = columns.get(columnIndex);
                    int columnWidth = columnWidths.get(columnIndex).intValue();

                    String columnName = column.getName();
                    SortDirection sortDirection = tableView.getSort().get(columnName);
                    if (sortDirection != null) {
                        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                            int gridY = getRowY(rowIndex + 1) - 1;

                            if (rowIndex < rowCount - 1 || includeTrailingHorizontalGridLine) {
                                if (!themeIsFlat()) {
                                    GraphicsUtilities.drawLine(graphics, columnX, gridY, columnWidth,
                                        Orientation.HORIZONTAL);
                                }
                            }
                        }
                    }

                    columnX += columnWidth + 1;
                }
            }
        }
    }

    private int getRowY(final int rowIndex) {
        int rowY;
        if (variableRowHeight) {
            if (rowIndex == 0) {
                rowY = 0;
            } else {
                rowY = rowBoundaries.get(rowIndex - 1).intValue();
            }
        } else {
            rowY = rowIndex * (fixedRowHeight + 1);
        }
        return rowY;
    }

    private int getRowHeight(final int rowIndex) {
        int rowHeight;
        if (variableRowHeight) {
            rowHeight = rowBoundaries.get(rowIndex).intValue();

            if (rowIndex > 0) {
                rowHeight -= rowBoundaries.get(rowIndex - 1).intValue();
            }
        } else {
            rowHeight = fixedRowHeight;
        }

        return rowHeight;
    }

    protected int getVariableRowHeight(final int rowIndex, final ArrayList<Integer> columnWidthsArgument) {
        TableView tableView = (TableView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> tableData = (List<Object>) tableView.getTableData();

        TableView.ColumnSequence columns = tableView.getColumns();
        Object rowData = tableData.get(rowIndex);

        int rowHeight = 0;
        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);
            TableView.CellRenderer cellRenderer = column.getCellRenderer();
            cellRenderer.render(rowData, rowIndex, i, tableView, column.getName(), false, false,
                false);

            rowHeight = Math.max(rowHeight,
                cellRenderer.getPreferredHeight(columnWidthsArgument.get(i).intValue()));
        }

        return rowHeight;
    }

    // Table view skin methods
    @Override
    public int getRowAt(final int y) {
        Utils.checkNonNegative(y, "y");

        TableView tableView = (TableView) getComponent();

        int rowIndex;
        if (variableRowHeight) {
            if (y == 0) {
                rowIndex = 0;
            } else {
                rowIndex = ArrayList.binarySearch(rowBoundaries, Integer.valueOf(y));
                if (rowIndex < 0) {
                    rowIndex = -(rowIndex + 1);
                }
            }
        } else {
            rowIndex = (y / (fixedRowHeight + 1));
        }

        @SuppressWarnings("unchecked")
        List<Object> tableData = (List<Object>) tableView.getTableData();
        if (rowIndex >= tableData.getLength()) {
            rowIndex = -1;
        }

        return rowIndex;
    }

    @Override
    public int getColumnAt(final int x) {
        Utils.checkNonNegative(x, "x");

        TableView tableView = (TableView) getComponent();

        int columnIndex = -1;

        int i = 0;
        int n = tableView.getColumns().getLength();
        int columnX = 0;
        while (i < n && x > columnX) {
            columnX += (columnWidths.get(i).intValue() + 1);
            i++;
        }

        if (x <= columnX) {
            columnIndex = i - 1;
        }

        return columnIndex;
    }

    @Override
    public Bounds getRowBounds(final int rowIndex) {
        return new Bounds(0, getRowY(rowIndex), getWidth(), getRowHeight(rowIndex));
    }

    @Override
    public Bounds getColumnBounds(final int columnIndex) {
        int columnX = 0;
        for (int i = 0; i < columnIndex; i++) {
            columnX += (columnWidths.get(i).intValue() + 1);
        }

        return new Bounds(columnX, 0, columnWidths.get(columnIndex).intValue(), getHeight());
    }

    @Override
    public Bounds getCellBounds(final int rowIndex, final int columnIndex) {
        TableView tableView = (TableView) getComponent();
        @SuppressWarnings("unchecked")
        List<Object> tableData = (List<Object>) tableView.getTableData();

        Utils.checkZeroBasedIndex(rowIndex, tableData.getLength());

        int cellX = 0;
        for (int i = 0; i < columnIndex; i++) {
            cellX += (columnWidths.get(i).intValue() + 1);
        }

        int rowHeight = getRowHeight(rowIndex);

        return new Bounds(cellX, rowIndex * (rowHeight + 1), columnWidths.get(columnIndex).intValue(),
            rowHeight);
    }

    public static ArrayList<Integer> getColumnWidths(final TableView tableView, final int width) {
        int fixedWidth = 0;
        int relativeWidth = 0;

        TableView.ColumnSequence columns = tableView.getColumns();
        int n = columns.getLength();

        ArrayList<Integer> columnWidths = new ArrayList<>(n);

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.isRelative()) {
                columnWidths.add(Integer.valueOf(0));
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

                columnWidth = Math.min(Math.max(columnWidth, column.getMinimumWidth()),
                    column.getMaximumWidth());
                columnWidths.add(Integer.valueOf(columnWidth));
                fixedWidth += columnWidth;
            }
        }

        fixedWidth += n - 1;
        int variableWidth = Math.max(width - fixedWidth, 0);

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.isRelative()) {
                int columnWidth = (int) Math.round((double) (column.getWidth() * variableWidth)
                    / (double) relativeWidth);
                columnWidths.update(i, Integer.valueOf(
                    Math.min(Math.max(columnWidth, column.getMinimumWidth()),
                        column.getMaximumWidth())));
            }
        }

        return columnWidths;
    }

    @Override
    public final boolean isFocusable() {
        TableView tableView = (TableView) getComponent();
        return (tableView.getSelectMode() != SelectMode.NONE);
    }

    @Override
    public final boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public final Font getFont() {
        return font;
    }

    public final void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(final String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(final Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public final Color getColor() {
        return color;
    }

    public final void setColor(final Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(final int color) {
        Theme theme = Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public final Color getDisabledColor() {
        return disabledColor;
    }

    public final void setDisabledColor(final Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(final String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public final void setDisabledColor(final int disabledColor) {
        Theme theme = Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(final Color backgroundColor) {
        // We allow a null background color here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(final int backgroundColor) {
        Theme theme = Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public final Color getSelectionColor() {
        return selectionColor;
    }

    public final void setSelectionColor(final Color selectionColor) {
        Utils.checkNull(selectionColor, "selectionColor");

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(final String selectionColor) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor, "selectionColor"));
    }

    public final void setSelectionColor(final int selectionColor) {
        Theme theme = Theme.getTheme();
        setSelectionColor(theme.getColor(selectionColor));
    }

    public final Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public final void setSelectionBackgroundColor(final Color selectionBackgroundColor) {
        Utils.checkNull(selectionBackgroundColor, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(final String selectionBackgroundColor) {
        setSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(selectionBackgroundColor, "selectionBackgroundColor"));
    }

    public final void setSelectionBackgroundColor(final int selectionBackgroundColor) {
        Theme theme = Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(selectionBackgroundColor));
    }

    public final Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public final void setInactiveSelectionColor(final Color inactiveSelectionColor) {
        Utils.checkNull(inactiveSelectionColor, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(final String inactiveSelectionColor) {
        setInactiveSelectionColor(
            GraphicsUtilities.decodeColor(inactiveSelectionColor, "inactiveSelectionColor"));
    }

    public final void setInactiveSelectionColor(final int inactiveSelectionColor) {
        Theme theme = Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(inactiveSelectionColor));
    }

    public final Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public final void setInactiveSelectionBackgroundColor(final Color inactiveSelectionBackgroundColor) {
        Utils.checkNull(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor");

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(final String inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor"));
    }

    public final void setInactiveSelectionBackgroundColor(final int inactiveSelectionBackgroundColor) {
        Theme theme = Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(inactiveSelectionBackgroundColor));
    }

    public final Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public final void setHighlightBackgroundColor(final Color highlightBackgroundColor) {
        Utils.checkNull(highlightBackgroundColor, "highlightBackgroundColor");

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(final String highlightBackgroundColor) {
        setHighlightBackgroundColor(
            GraphicsUtilities.decodeColor(highlightBackgroundColor, "highlightBackgroundColor"));
    }

    public final void setHighlightBackgroundColor(final int highlightBackgroundColor) {
        Theme theme = Theme.getTheme();
        setHighlightBackgroundColor(theme.getColor(highlightBackgroundColor));
    }

    public final Color getAlternateRowBackgroundColor() {
        return alternateRowBackgroundColor;
    }

    public final void setAlternateRowBackgroundColor(final Color alternateRowBackgroundColor) {
        this.alternateRowBackgroundColor = alternateRowBackgroundColor;
        repaintComponent();
    }

    public final void setAlternateRowBackgroundColor(final String alternateRowBackgroundColor) {
        setAlternateRowBackgroundColor(
            GraphicsUtilities.decodeColor(alternateRowBackgroundColor, "alternateRowBackgroundColor"));
    }

    public final void setAlternateRowBackgroundColor(final int alternateRowBackgroundColor) {
        Theme theme = Theme.getTheme();
        setAlternateRowBackgroundColor(theme.getColor(alternateRowBackgroundColor));
    }

    public final Color getColumnSelectionColor() {
        return columnSelectionColor;
    }

    public final void setColumnSelectionColor(final Color columnSelectionColor) {
        this.columnSelectionColor = columnSelectionColor;
        repaintComponent();
    }

    public final void setColumnSelectionColor(final String columnSelectionColor) {
        setColumnSelectionColor(GraphicsUtilities.decodeColor(columnSelectionColor, "columnSelectionColor"));
    }

    public final void setColumnSelectionColor(final int columnSelectionColor) {
        Theme theme = Theme.getTheme();
        setColumnSelectionColor(theme.getColor(columnSelectionColor));
    }

    public final Color getColumnSelectionHorizontalGridColor() {
        return columnSelectionHorizontalGridColor;
    }

    public final void setColumnSelectionHorizontalGridColor(final Color columnSelectionHorizontalGridColor) {
        Utils.checkNull(columnSelectionHorizontalGridColor, "columnSelectionHorizontalGridColor");

        this.columnSelectionHorizontalGridColor = columnSelectionHorizontalGridColor;
        repaintComponent();
    }

    public final void setColumnSelectionHorizontalGridColor(
        String columnSelectionHorizontalGridColor) {
        setColumnSelectionHorizontalGridColor(
            GraphicsUtilities.decodeColor(columnSelectionHorizontalGridColor, "columnSelectionHorizontalGridColor"));
    }

    public final void setColumnSelectionHorizontalGridColor(final int columnSelectionHorizontalGridColor) {
        Theme theme = Theme.getTheme();
        setColumnSelectionHorizontalGridColor(theme.getColor(columnSelectionHorizontalGridColor));
    }

    public final Color getHorizontalGridColor() {
        return horizontalGridColor;
    }

    public final void setHorizontalGridColor(final Color horizontalGridColor) {
        Utils.checkNull(horizontalGridColor, "horizontalGridColor");

        this.horizontalGridColor = horizontalGridColor;
        repaintComponent();
    }

    public final void setHorizontalGridColor(final String horizontalGridColor) {
        setHorizontalGridColor(GraphicsUtilities.decodeColor(horizontalGridColor, "horizontalGridColor"));
    }

    public final void setHorizontalGridColor(final int horizontalGridColor) {
        Theme theme = Theme.getTheme();
        setHorizontalGridColor(theme.getColor(horizontalGridColor));
    }

    public final Color getVerticalGridColor() {
        return verticalGridColor;
    }

    public final void setVerticalGridColor(final Color verticalGridColor) {
        Utils.checkNull(verticalGridColor, "verticalGridColor");

        this.verticalGridColor = verticalGridColor;
        repaintComponent();
    }

    public final void setVerticalGridColor(final String verticalGridColor) {
        setVerticalGridColor(GraphicsUtilities.decodeColor(verticalGridColor, "verticalGridColor"));
    }

    public final void setVerticalGridColor(final int verticalGridColor) {
        Theme theme = Theme.getTheme();
        setVerticalGridColor(theme.getColor(verticalGridColor));
    }

    public final boolean getShowHighlight() {
        return showHighlight;
    }

    public final void setShowHighlight(final boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    public final boolean getShowHorizontalGridLines() {
        return showHorizontalGridLines;
    }

    public final void setShowHorizontalGridLines(final boolean showHorizontalGridLines) {
        this.showHorizontalGridLines = showHorizontalGridLines;
        repaintComponent();
    }

    public final boolean getShowVerticalGridLines() {
        return showVerticalGridLines;
    }

    public final void setShowVerticalGridLines(final boolean showVerticalGridLines) {
        this.showVerticalGridLines = showVerticalGridLines;
        repaintComponent();
    }

    public final boolean getIncludeTrailingVerticalGridLine() {
        return includeTrailingVerticalGridLine;
    }

    public final void setIncludeTrailingVerticalGridLine(final boolean includeTrailingVerticalGridLine) {
        this.includeTrailingVerticalGridLine = includeTrailingVerticalGridLine;
        invalidateComponent();
    }

    public final boolean getIncludeTrailingHorizontalGridLine() {
        return includeTrailingHorizontalGridLine;
    }

    public final void setIncludeTrailingHorizontalGridLine(final boolean includeTrailingHorizontalGridLine) {
        this.includeTrailingHorizontalGridLine = includeTrailingHorizontalGridLine;
        invalidateComponent();
    }

    public final boolean isVariableRowHeight() {
        return variableRowHeight;
    }

    public final void setVariableRowHeight(final boolean variableRowHeight) {
        this.variableRowHeight = variableRowHeight;
        this.rowBoundaries = null;
        this.fixedRowHeight = -1;
        invalidateComponent();
    }

    /**
     * @return Is this {@link TableView} going into edit mode on a mouse down or
     * on a mouse double click?
     */
    public final boolean isEditOnMouseDown() {
        return editOnMouseDown;
    }

    /**
     * Set whether this {@link TableView} will go into edit mode on a mouse down or
     * on a mouse double click (the default).
     * <p> Setting this to <tt>true</tt> can reduce the number of mouse clicks
     * necessary to rapidly edit a table view.
     * @param editOnMouseDown The new setting.
     */
    public final void setEditOnMouseDown(final boolean editOnMouseDown) {
        this.editOnMouseDown = editOnMouseDown;
    }

    @Override
    public boolean mouseMove(final Component component, final int x, final int y) {
        boolean consumed = super.mouseMove(component, x, y);

        TableView tableView = (TableView) getComponent();

        int previousHighlightIndex = this.highlightIndex;
        highlightIndex = getRowAt(y);

        if (previousHighlightIndex != highlightIndex
            && tableView.getSelectMode() != SelectMode.NONE && showHighlight) {
            if (previousHighlightIndex != -1) {
                repaintComponent(getRowBounds(previousHighlightIndex));
            }

            if (highlightIndex != -1) {
                repaintComponent(getRowBounds(highlightIndex));
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(final Component component) {
        super.mouseOut(component);

        TableView tableView = (TableView) getComponent();

        if (highlightIndex != -1 && tableView.getSelectMode() != SelectMode.NONE
            && showHighlight) {
            repaintComponent(getRowBounds(highlightIndex));
        }

        highlightIndex = -1;
        selectIndex = -1;
    }

    @Override
    public boolean mouseDown(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        TableView tableView = (TableView) getComponent();
        int rowIndex = getRowAt(y);

        if (rowIndex >= 0 && !tableView.isRowDisabled(rowIndex)) {
            SelectMode selectMode = tableView.getSelectMode();

            if (button == Mouse.Button.LEFT) {
                Modifier commandModifier = Platform.getCommandModifier();

                if (Keyboard.isPressed(Modifier.SHIFT)
                    && selectMode == SelectMode.MULTI) {
                    Filter<?> disabledRowFilter = tableView.getDisabledRowFilter();

                    if (disabledRowFilter == null) {
                        // Select the range
                        int startIndex = tableView.getFirstSelectedIndex();
                        int endIndex = tableView.getLastSelectedIndex();
                        // if there is nothing currently selected, selected the
                        // indicated row
                        if (startIndex == -1) {
                            tableView.addSelectedIndex(rowIndex);
                        } else {
                            // otherwise select the range of rows
                            Span selectedRange = (rowIndex > startIndex) ? new Span(startIndex,
                                rowIndex) : new Span(rowIndex, endIndex);

                            ArrayList<Span> selectedRanges = new ArrayList<>();
                            selectedRanges.add(selectedRange);

                            tableView.setSelectedRanges(selectedRanges);
                        }
                    }
                } else if (Keyboard.isPressed(commandModifier)
                    && selectMode == SelectMode.MULTI) {
                    // Toggle the item's selection state
                    if (tableView.isRowSelected(rowIndex)) {
                        tableView.removeSelectedIndex(rowIndex);
                    } else {
                        tableView.addSelectedIndex(rowIndex);
                    }
                } else if (Keyboard.isPressed(commandModifier)
                    && selectMode == SelectMode.SINGLE) {
                    // Toggle the item's selection state
                    if (tableView.isRowSelected(rowIndex)) {
                        tableView.setSelectedIndex(-1);
                    } else {
                        tableView.setSelectedIndex(rowIndex);
                    }
                } else {
                    if (selectMode != SelectMode.NONE) {
                        if (!tableView.isRowSelected(rowIndex)) {
                            tableView.setSelectedIndex(rowIndex);
                        }
                        selectIndex = rowIndex;
                    }
                }
            }
        }

        tableView.requestFocus();

        if (editOnMouseDown) {
            if (selectIndex != -1 && button == Mouse.Button.LEFT) {
                TableView.RowEditor rowEditor = tableView.getRowEditor();

                if (rowEditor != null) {
                    if (rowEditor.isEditing()) {
                        rowEditor.endEdit(true);
                    }

                    rowEditor.beginEdit(tableView, selectIndex, getColumnAt(x));
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseUp(final Component component, final Mouse.Button button, final int x, final int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        TableView tableView = (TableView) getComponent();
        if (selectIndex != -1
            && tableView.getFirstSelectedIndex() != tableView.getLastSelectedIndex()) {
            tableView.setSelectedIndex(selectIndex);
            selectIndex = -1;
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(final Component component, final Mouse.Button button, final int x, final int y,
        final int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        if (!editOnMouseDown) {
            TableView tableView = (TableView) getComponent();
            if (selectIndex != -1 && count == 2 && button == Mouse.Button.LEFT) {
                TableView.RowEditor rowEditor = tableView.getRowEditor();

                if (rowEditor != null) {
                    if (rowEditor.isEditing()) {
                        rowEditor.endEdit(true);
                    }

                    rowEditor.beginEdit(tableView, selectIndex, getColumnAt(x));
                }
            }
        }

        selectIndex = -1;

        return consumed;
    }

    @Override
    public boolean mouseWheel(final Component component, final Mouse.ScrollType scrollType, final int scrollAmount,
        final int wheelRotation, final int x, final int y) {
        TableView tableView = (TableView) getComponent();

        if (highlightIndex != -1) {
            Bounds rowBounds = getRowBounds(highlightIndex);

            highlightIndex = -1;

            if (tableView.getSelectMode() != SelectMode.NONE && showHighlight) {
                repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height, true);
            }
        }

        return super.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
    }

    /**
     * Keyboard handling (arrow keys with modifiers and a few others).
     * <ul>
     * <li>{@link KeyCode#UP UP} Selects the previous enabled row when select mode
     * is not {@link SelectMode#NONE}</li>
     * <li>{@link KeyCode#DOWN DOWN} Selects the next enabled row when select mode
     * is not {@link SelectMode#NONE}</li>
     * <li>{@link Modifier#SHIFT SHIFT} + {@link KeyCode#UP UP} Increases the
     * selection size by including the previous enabled row when select mode is
     * {@link SelectMode#MULTI}</li>
     * <li>{@link Modifier#SHIFT SHIFT} + {@link KeyCode#DOWN DOWN} Increases the
     * selection size by including the next enabled row when select mode is
     * {@link SelectMode#MULTI}</li>
     * <li>{@code Cmd/Ctrl-A} in {@link SelectMode#MULTI} select mode to select everything</li>
     * <li>{@code Cmd/Ctrl-U} will unselect whatever is selected</li>
     * <li>{@link KeyCode#SPACE SPACE} wil select/unselect the "current" location</li>
     * </ul>
     */
    @Override
    public boolean keyPressed(final Component component, final int keyCode, final KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        TableView tableView = (TableView) getComponent();
        SelectMode selectMode = tableView.getSelectMode();
        Modifier cmdModifier = Platform.getCommandModifier();

        switch (keyCode) {
            case KeyCode.UP:
                if (selectMode != SelectMode.NONE) {
                    int index = tableView.getFirstSelectedIndex();

                    do {
                        index--;
                    } while (index >= 0 && tableView.isRowDisabled(index));

                    if (index >= 0) {
                        if (Keyboard.isPressed(Modifier.SHIFT)
                            && tableView.getSelectMode() == SelectMode.MULTI) {
                            tableView.addSelectedIndex(index);
                        } else {
                            tableView.setSelectedIndex(index);
                        }
                        lastKeyboardSelectIndex = index;
                    }

                    consumed = true;
                }

                break;

            case KeyCode.DOWN:
                if (selectMode != SelectMode.NONE) {
                    int index = tableView.getLastSelectedIndex();
                    int count = tableView.getTableData().getLength();

                    do {
                        index++;
                    } while (index < count && tableView.isRowDisabled(index));

                    if (index < count) {
                        if (Keyboard.isPressed(Modifier.SHIFT)
                            && tableView.getSelectMode() == SelectMode.MULTI) {
                            tableView.addSelectedIndex(index);
                        } else {
                            tableView.setSelectedIndex(index);
                        }
                        lastKeyboardSelectIndex = index;
                    }

                    consumed = true;
                }

                break;

            case KeyCode.SPACE:
                if (lastKeyboardSelectIndex != -1 && selectMode != SelectMode.NONE) {
                    if (!tableView.isRowDisabled(lastKeyboardSelectIndex)) {
                        switch (selectMode) {
                            case SINGLE:
                                if (tableView.isRowSelected(lastKeyboardSelectIndex)) {
                                    tableView.setSelectedIndex(-1);
                                } else {
                                    tableView.setSelectedIndex(lastKeyboardSelectIndex);
                                }
                                break;
                            case MULTI:
                                if (tableView.isRowSelected(lastKeyboardSelectIndex)) {
                                    tableView.removeSelectedIndex(lastKeyboardSelectIndex);
                                } else {
                                    tableView.addSelectedIndex(lastKeyboardSelectIndex);
                                }
                                break;
                            default:
                                break;
                        }
                        consumed = true;
                    }
                }
                break;

            case KeyCode.A:
                if (Keyboard.isPressed(cmdModifier)) {
                    if (selectMode == SelectMode.MULTI) {
                        tableView.selectAll();
                        lastKeyboardSelectIndex = tableView.getTableData().getLength() - 1; // TODO: what should it be?
                        consumed = true;
                    }
                }
                break;

            case KeyCode.U:
                if (Keyboard.isPressed(cmdModifier)) {
                    switch (selectMode) {
                        case NONE:
                        default:
                            break;
                        case SINGLE:
                        case MULTI:
                            tableView.clearSelection();
                            lastKeyboardSelectIndex = 0; // TODO: what should it be?
                            consumed = true;
                            break;
                    }
                }
                break;

            default:
                break;
        }

        // Clear the highlight
        if (highlightIndex != -1 && tableView.getSelectMode() != SelectMode.NONE
            && showHighlight && consumed) {
            repaintComponent(getRowBounds(highlightIndex));
        }

        highlightIndex = -1;

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(final Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(final Component component, final Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        repaintComponent();
    }

    // Table view events
    @Override
    public void tableDataChanged(final TableView tableView, final List<?> previousTableData) {
        invalidateComponent();
    }

    @Override
    public void columnSourceChanged(final TableView tableView, final TableView previousColumnSource) {
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
    public void rowEditorChanged(final TableView tableView, final TableView.RowEditor previousRowEditor) {
        // No-op
    }

    @Override
    public void selectModeChanged(final TableView tableView, final SelectMode previousSelectMode) {
        repaintComponent();
    }

    @Override
    public void disabledRowFilterChanged(final TableView tableView, final Filter<?> previousDisabledRowFilter) {
        repaintComponent();
    }

    // Table view column events
    @Override
    public void columnInserted(final TableView tableView, final int index) {
        TableView.Column column = tableView.getColumns().get(index);

        if (column.getWidth() == -1) {
            defaultWidthColumnCount++;
        }

        invalidateComponent();
    }

    @Override
    public void columnsRemoved(final TableView tableView, final int index, final Sequence<TableView.Column> columns) {
        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.getWidth() == -1) {
                defaultWidthColumnCount--;
            }
        }

        invalidateComponent();
    }

    @Override
    public void columnNameChanged(final TableView.Column column, final String previousName) {
        invalidateComponent();
    }

    @Override
    public void columnHeaderDataChanged(final TableView.Column column, final Object previousHeaderData) {
        // No-op
    }

    @Override
    public void columnHeaderDataRendererChanged(final TableView.Column column,
        final TableView.HeaderDataRenderer previousHeaderDataRenderer) {
        // No-op
    }

    @Override
    public void columnWidthChanged(final TableView.Column column, final int previousWidth,
        final boolean previousRelative) {
        if (column.getWidth() == -1) {
            defaultWidthColumnCount++;
        } else {
            defaultWidthColumnCount--;
        }

        invalidateComponent();
    }

    @Override
    public void columnWidthLimitsChanged(final TableView.Column column, final int previousMinimumWidth,
        final int previousMaximumWidth) {
        invalidateComponent();
    }

    @Override
    public void columnFilterChanged(final TableView.Column column, final Object previousFilter) {
        // No-op
    }

    @Override
    public void columnCellRendererChanged(final TableView.Column column,
        final TableView.CellRenderer previousCellRenderer) {
        invalidateComponent();
    }

    // Table view row events
    @Override
    public void rowInserted(final TableView tableView, final int index) {
        invalidateComponent();
    }

    @Override
    public void rowsRemoved(final TableView tableView, final int index, final int count) {
        invalidateComponent();
    }

    @Override
    public void rowUpdated(final TableView tableView, final int index) {
        if (variableRowHeight || defaultWidthColumnCount > 0) {
            invalidateComponent();
        } else {
            repaintComponent(getRowBounds(index));
        }
    }

    @Override
    public void rowsCleared(final TableView listView) {
        invalidateComponent();
    }

    @Override
    public void rowsSorted(final TableView tableView) {
        if (variableRowHeight) {
            invalidateComponent();
        } else {
            repaintComponent();
        }
    }

    // Table view selection detail events
    @Override
    public void selectedRangeAdded(final TableView tableView, final int rangeStart, final int rangeEnd) {
        if (tableView.isValid()) {
            Bounds selectionBounds = getRowBounds(rangeStart);
            selectionBounds = selectionBounds.union(getRowBounds(rangeEnd));
            repaintComponent(selectionBounds);

            // Ensure that the selection is visible
            Bounds visibleSelectionBounds = tableView.getVisibleArea(selectionBounds);
            if (visibleSelectionBounds.height < selectionBounds.height) {
                tableView.scrollAreaToVisible(selectionBounds);
            }
        } else {
            validateSelection = true;
        }
    }

    @Override
    public void selectedRangeRemoved(final TableView tableView, final int rangeStart, final int rangeEnd) {
        // Repaint the area containing the removed selection
        if (tableView.isValid()) {
            Bounds selectionBounds = getRowBounds(rangeStart);
            selectionBounds = selectionBounds.union(getRowBounds(rangeEnd));
            repaintComponent(selectionBounds);
        }
    }

    @Override
    public void selectedRangesChanged(final TableView tableView, final Sequence<Span> previousSelectedRanges) {
        if (previousSelectedRanges != null
            && previousSelectedRanges != tableView.getSelectedRanges()) {
            if (tableView.isValid()) {
                // Repaint the area occupied by the previous selection
                if (previousSelectedRanges.getLength() > 0) {
                    int rangeStart = previousSelectedRanges.get(0).start;
                    int rangeEnd = previousSelectedRanges.get(previousSelectedRanges.getLength() - 1).end;

                    Bounds previousSelectionBounds = getRowBounds(rangeStart);
                    previousSelectionBounds = previousSelectionBounds.union(getRowBounds(rangeEnd));

                    repaintComponent(previousSelectionBounds);
                }

                // Repaint the area occupied by the current selection
                Sequence<Span> selectedRanges = tableView.getSelectedRanges();
                if (selectedRanges.getLength() > 0) {
                    int rangeStart = selectedRanges.get(0).start;
                    int rangeEnd = selectedRanges.get(selectedRanges.getLength() - 1).end;

                    Bounds selectionBounds = getRowBounds(rangeStart);
                    selectionBounds = selectionBounds.union(getRowBounds(rangeEnd));

                    repaintComponent(selectionBounds);

                    // Ensure that the selection is visible
                    Bounds visibleSelectionBounds = tableView.getVisibleArea(selectionBounds);
                    if (visibleSelectionBounds != null
                        && visibleSelectionBounds.height < selectionBounds.height) {
                        // TODO Repainting the entire component is a workaround
                        // for PIVOT-490
                        repaintComponent();

                        tableView.scrollAreaToVisible(selectionBounds);
                    }
                }
            } else {
                validateSelection = true;
            }
        }
    }

    @Override
    public void selectedRowChanged(final TableView tableView, final Object previousSelectedRow) {
        // No-op
    }
}
