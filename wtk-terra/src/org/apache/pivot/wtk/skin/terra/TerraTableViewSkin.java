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
    public void install(Component component) {
        super.install(component);

        TableView tableView = (TableView) component;
        tableView.getTableViewListeners().add(this);
        tableView.getTableViewColumnListeners().add(this);
        tableView.getTableViewRowListeners().add(this);
        tableView.getTableViewSelectionListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        return getPreferredWidth((TableView) getComponent(), includeTrailingVerticalGridLine);
    }

    public static int getPreferredWidth(TableView tableView, boolean includeTrailingVerticalGridLine) {
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
    public int getPreferredHeight(int width) {
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
    public int getBaseline(int width, int height) {
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
     */
    private static int calculateFixedRowHeight(TableView tableView) {
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
    public void paint(Graphics2D graphics) {
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
            boolean rowHighlighted = (rowIndex == highlightIndex && tableView.getSelectMode() != TableView.SelectMode.NONE);
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

    private int getRowY(int rowIndex) {
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

    private int getRowHeight(int rowIndex) {
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

    protected int getVariableRowHeight(int rowIndex, ArrayList<Integer> columnWidthsArgument) {
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
    public int getRowAt(int y) {
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
    public int getColumnAt(int x) {
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
    public Bounds getRowBounds(int rowIndex) {
        return new Bounds(0, getRowY(rowIndex), getWidth(), getRowHeight(rowIndex));
    }

    @Override
    public Bounds getColumnBounds(int columnIndex) {
        int columnX = 0;
        for (int i = 0; i < columnIndex; i++) {
            columnX += (columnWidths.get(i).intValue() + 1);
        }

        return new Bounds(columnX, 0, columnWidths.get(columnIndex).intValue(), getHeight());
    }

    @Override
    public Bounds getCellBounds(int rowIndex, int columnIndex) {
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

    public static ArrayList<Integer> getColumnWidths(TableView tableView, int width) {
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
    public boolean isFocusable() {
        TableView tableView = (TableView) getComponent();
        return (tableView.getSelectMode() != TableView.SelectMode.NONE);
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(int color) {
        Theme theme = Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public final void setDisabledColor(int disabledColor) {
        Theme theme = Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        // We allow a null background color here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(int backgroundColor) {
        Theme theme = Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        Utils.checkNull(selectionColor, "selectionColor");

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor, "selectionColor"));
    }

    public final void setSelectionColor(int selectionColor) {
        Theme theme = Theme.getTheme();
        setSelectionColor(theme.getColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        Utils.checkNull(selectionBackgroundColor, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        setSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(selectionBackgroundColor, "selectionBackgroundColor"));
    }

    public final void setSelectionBackgroundColor(int selectionBackgroundColor) {
        Theme theme = Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        Utils.checkNull(inactiveSelectionColor, "inactiveSelectionColor");

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        setInactiveSelectionColor(
            GraphicsUtilities.decodeColor(inactiveSelectionColor, "inactiveSelectionColor"));
    }

    public final void setInactiveSelectionColor(int inactiveSelectionColor) {
        Theme theme = Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        Utils.checkNull(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor");

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        setInactiveSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor, "inactiveSelectionBackgroundColor"));
    }

    public final void setInactiveSelectionBackgroundColor(int inactiveSelectionBackgroundColor) {
        Theme theme = Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        Utils.checkNull(highlightBackgroundColor, "highlightBackgroundColor");

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        setHighlightBackgroundColor(
            GraphicsUtilities.decodeColor(highlightBackgroundColor, "highlightBackgroundColor"));
    }

    public final void setHighlightBackgroundColor(int highlightBackgroundColor) {
        Theme theme = Theme.getTheme();
        setHighlightBackgroundColor(theme.getColor(highlightBackgroundColor));
    }

    public Color getAlternateRowBackgroundColor() {
        return alternateRowBackgroundColor;
    }

    public void setAlternateRowBackgroundColor(Color alternateRowBackgroundColor) {
        this.alternateRowBackgroundColor = alternateRowBackgroundColor;
        repaintComponent();
    }

    public final void setAlternateRowBackgroundColor(String alternateRowBackgroundColor) {
        setAlternateRowBackgroundColor(
            GraphicsUtilities.decodeColor(alternateRowBackgroundColor, "alternateRowBackgroundColor"));
    }

    public final void setAlternateRowBackgroundColor(int alternateRowBackgroundColor) {
        Theme theme = Theme.getTheme();
        setAlternateRowBackgroundColor(theme.getColor(alternateRowBackgroundColor));
    }

    public Color getColumnSelectionColor() {
        return columnSelectionColor;
    }

    public void setColumnSelectionColor(Color columnSelectionColor) {
        this.columnSelectionColor = columnSelectionColor;
        repaintComponent();
    }

    public final void setColumnSelectionColor(String columnSelectionColor) {
        setColumnSelectionColor(GraphicsUtilities.decodeColor(columnSelectionColor, "columnSelectionColor"));
    }

    public final void setColumnSelectionColor(int columnSelectionColor) {
        Theme theme = Theme.getTheme();
        setColumnSelectionColor(theme.getColor(columnSelectionColor));
    }

    public Color getColumnSelectionHorizontalGridColor() {
        return columnSelectionHorizontalGridColor;
    }

    public void setColumnSelectionHorizontalGridColor(Color columnSelectionHorizontalGridColor) {
        Utils.checkNull(columnSelectionHorizontalGridColor, "columnSelectionHorizontalGridColor");

        this.columnSelectionHorizontalGridColor = columnSelectionHorizontalGridColor;
        repaintComponent();
    }

    public final void setColumnSelectionHorizontalGridColor(
        String columnSelectionHorizontalGridColor) {
        setColumnSelectionHorizontalGridColor(
            GraphicsUtilities.decodeColor(columnSelectionHorizontalGridColor, "columnSelectionHorizontalGridColor"));
    }

    public final void setColumnSelectionHorizontalGridColor(int columnSelectionHorizontalGridColor) {
        Theme theme = Theme.getTheme();
        setColumnSelectionHorizontalGridColor(theme.getColor(columnSelectionHorizontalGridColor));
    }

    public Color getHorizontalGridColor() {
        return horizontalGridColor;
    }

    public void setHorizontalGridColor(Color horizontalGridColor) {
        Utils.checkNull(horizontalGridColor, "horizontalGridColor");

        this.horizontalGridColor = horizontalGridColor;
        repaintComponent();
    }

    public final void setHorizontalGridColor(String horizontalGridColor) {
        setHorizontalGridColor(GraphicsUtilities.decodeColor(horizontalGridColor, "horizontalGridColor"));
    }

    public final void setHorizontalGridColor(int horizontalGridColor) {
        Theme theme = Theme.getTheme();
        setHorizontalGridColor(theme.getColor(horizontalGridColor));
    }

    public Color getVerticalGridColor() {
        return verticalGridColor;
    }

    public void setVerticalGridColor(Color verticalGridColor) {
        Utils.checkNull(verticalGridColor, "verticalGridColor");

        this.verticalGridColor = verticalGridColor;
        repaintComponent();
    }

    public final void setVerticalGridColor(String verticalGridColor) {
        setVerticalGridColor(GraphicsUtilities.decodeColor(verticalGridColor, "verticalGridColor"));
    }

    public final void setVerticalGridColor(int verticalGridColor) {
        Theme theme = Theme.getTheme();
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

    public boolean isVariableRowHeight() {
        return variableRowHeight;
    }

    public void setVariableRowHeight(boolean variableRowHeight) {
        this.variableRowHeight = variableRowHeight;
        this.rowBoundaries = null;
        this.fixedRowHeight = -1;
        invalidateComponent();
    }

    /**
     * @return Is this {@link TableView} going into edit mode on a mouse down or
     * on a mouse double click?
     */
    public boolean isEditOnMouseDown() {
        return editOnMouseDown;
    }

    /**
     * Set whether this {@link TableView} will go into edit mode on a mouse down or
     * on a mouse double click (the default).
     * <p> Setting this to <tt>true</tt> can reduce the number of mouse clicks
     * necessary to rapidly edit a table view.
     * @param editOnMouseDown The new setting.
     */
    public void setEditOnMouseDown(boolean editOnMouseDown) {
        this.editOnMouseDown = editOnMouseDown;
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        TableView tableView = (TableView) getComponent();

        int previousHighlightIndex = this.highlightIndex;
        highlightIndex = getRowAt(y);

        if (previousHighlightIndex != highlightIndex
            && tableView.getSelectMode() != TableView.SelectMode.NONE && showHighlight) {
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
    public void mouseOut(Component component) {
        super.mouseOut(component);

        TableView tableView = (TableView) getComponent();

        if (highlightIndex != -1 && tableView.getSelectMode() != TableView.SelectMode.NONE
            && showHighlight) {
            repaintComponent(getRowBounds(highlightIndex));
        }

        highlightIndex = -1;
        selectIndex = -1;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        TableView tableView = (TableView) getComponent();
        int rowIndex = getRowAt(y);

        if (rowIndex >= 0 && !tableView.isRowDisabled(rowIndex)) {
            TableView.SelectMode selectMode = tableView.getSelectMode();

            if (button == Mouse.Button.LEFT) {
                Keyboard.Modifier commandModifier = Platform.getCommandModifier();

                if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                    && selectMode == TableView.SelectMode.MULTI) {
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
                    && selectMode == TableView.SelectMode.MULTI) {
                    // Toggle the item's selection state
                    if (tableView.isRowSelected(rowIndex)) {
                        tableView.removeSelectedIndex(rowIndex);
                    } else {
                        tableView.addSelectedIndex(rowIndex);
                    }
                } else if (Keyboard.isPressed(commandModifier)
                    && selectMode == TableView.SelectMode.SINGLE) {
                    // Toggle the item's selection state
                    if (tableView.isRowSelected(rowIndex)) {
                        tableView.setSelectedIndex(-1);
                    } else {
                        tableView.setSelectedIndex(rowIndex);
                    }
                } else {
                    if (selectMode != TableView.SelectMode.NONE) {
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
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
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
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
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
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        TableView tableView = (TableView) getComponent();

        if (highlightIndex != -1) {
            Bounds rowBounds = getRowBounds(highlightIndex);

            highlightIndex = -1;

            if (tableView.getSelectMode() != TableView.SelectMode.NONE && showHighlight) {
                repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height, true);
            }
        }

        return super.mouseWheel(component, scrollType, scrollAmount, wheelRotation, x, y);
    }

    /**
     * {@link KeyCode#UP UP} Selects the previous enabled row when select mode
     * is not {@link SelectMode#NONE}<br> {@link KeyCode#DOWN DOWN} Selects the
     * next enabled row when select mode is not {@link SelectMode#NONE}<p>
     * {@link Modifier#SHIFT SHIFT} + {@link KeyCode#UP UP} Increases the
     * selection size by including the previous enabled row when select mode is
     * {@link SelectMode#MULTI}<br> {@link Modifier#SHIFT SHIFT} +
     * {@link KeyCode#DOWN DOWN} Increases the selection size by including the
     * next enabled row when select mode is {@link SelectMode#MULTI}<br>
     * {@code Cmd/Ctrl-A} in {@link SelectMode#MULTI} select mode to select everything<br>
     * {@link KeyCode#SPACE SPACE} wil select/unselect the "current" location
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        TableView tableView = (TableView) getComponent();
        TableView.SelectMode selectMode = tableView.getSelectMode();

        switch (keyCode) {
            case Keyboard.KeyCode.UP: {
                if (selectMode != TableView.SelectMode.NONE) {
                    int index = tableView.getFirstSelectedIndex();

                    do {
                        index--;
                    } while (index >= 0 && tableView.isRowDisabled(index));

                    if (index >= 0) {
                        if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                            && tableView.getSelectMode() == TableView.SelectMode.MULTI) {
                            tableView.addSelectedIndex(index);
                        } else {
                            tableView.setSelectedIndex(index);
                        }
                        lastKeyboardSelectIndex = index;
                    }

                    consumed = true;
                }

                break;
            }

            case Keyboard.KeyCode.DOWN: {
                if (selectMode != TableView.SelectMode.NONE) {
                    int index = tableView.getLastSelectedIndex();
                    int count = tableView.getTableData().getLength();

                    do {
                        index++;
                    } while (index < count && tableView.isRowDisabled(index));

                    if (index < count) {
                        if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                            && tableView.getSelectMode() == TableView.SelectMode.MULTI) {
                            tableView.addSelectedIndex(index);
                        } else {
                            tableView.setSelectedIndex(index);
                        }
                        lastKeyboardSelectIndex = index;
                    }

                    consumed = true;
                }

                break;
            }

            case Keyboard.KeyCode.SPACE: {
                if (lastKeyboardSelectIndex != -1 && selectMode != TableView.SelectMode.NONE) {
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
                        }
                        consumed = true;
                    }
                }
                break;
            }

            case Keyboard.KeyCode.A: {
                Modifier cmdModifier = Platform.getCommandModifier();
                if (Keyboard.isPressed(cmdModifier)) {
                    if (selectMode == TableView.SelectMode.MULTI) {
                        tableView.selectAll();
                        lastKeyboardSelectIndex = tableView.getTableData().getLength() - 1; // TODO: what should it be?
                        consumed = true;
                    }
                }
                break;
            }

            default: {
                break;
            }
        }

        // Clear the highlight
        if (highlightIndex != -1 && tableView.getSelectMode() != TableView.SelectMode.NONE
            && showHighlight && consumed) {
            repaintComponent(getRowBounds(highlightIndex));
        }

        highlightIndex = -1;

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
        TableView.Column column = tableView.getColumns().get(index);

        if (column.getWidth() == -1) {
            defaultWidthColumnCount++;
        }

        invalidateComponent();
    }

    @Override
    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.getWidth() == -1) {
                defaultWidthColumnCount--;
            }
        }

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
    public void columnHeaderDataRendererChanged(TableView.Column column,
        TableView.HeaderDataRenderer previousHeaderDataRenderer) {
        // No-op
    }

    @Override
    public void columnWidthChanged(TableView.Column column, int previousWidth,
        boolean previousRelative) {
        if (column.getWidth() == -1) {
            defaultWidthColumnCount++;
        } else {
            defaultWidthColumnCount--;
        }

        invalidateComponent();
    }

    @Override
    public void columnWidthLimitsChanged(TableView.Column column, int previousMinimumWidth,
        int previousMaximumWidth) {
        invalidateComponent();
    }

    @Override
    public void columnFilterChanged(TableView.Column column, Object previousFilter) {
        // No-op
    }

    @Override
    public void columnCellRendererChanged(TableView.Column column,
        TableView.CellRenderer previousCellRenderer) {
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
        if (variableRowHeight || defaultWidthColumnCount > 0) {
            invalidateComponent();
        } else {
            repaintComponent(getRowBounds(index));
        }
    }

    @Override
    public void rowsCleared(TableView listView) {
        invalidateComponent();
    }

    @Override
    public void rowsSorted(TableView tableView) {
        if (variableRowHeight) {
            invalidateComponent();
        } else {
            repaintComponent();
        }
    }

    // Table view selection detail events
    @Override
    public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
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
    public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the removed selection
        if (tableView.isValid()) {
            Bounds selectionBounds = getRowBounds(rangeStart);
            selectionBounds = selectionBounds.union(getRowBounds(rangeEnd));
            repaintComponent(selectionBounds);
        }
    }

    @Override
    public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
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
    public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
        // No-op
    }
}
