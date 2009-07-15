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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
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
import org.apache.pivot.wtk.TableViewRowStateListener;
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
 *
 * @author gbrown
 */
public class TerraTableViewSkin extends ComponentSkin implements TableView.Skin,
    TableViewListener, TableViewColumnListener, TableViewRowListener,
    TableViewRowStateListener, TableViewSelectionListener {
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
    private Color gridColor;
    private boolean showHorizontalGridLines;
    private boolean showVerticalGridLines;
    private boolean showHighlight;
    private boolean includeTrailingVerticalGridLine;

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
        gridColor = theme.getColor(11);
        showHorizontalGridLines = true;
        showVerticalGridLines = true;
        showHighlight = true;
        includeTrailingVerticalGridLine = false;
    }

    public void install(Component component) {
        super.install(component);

        TableView tableView = (TableView)component;
        tableView.getTableViewListeners().add(this);
        tableView.getTableViewColumnListeners().add(this);
        tableView.getTableViewRowListeners().add(this);
        tableView.getTableViewRowStateListeners().add(this);
        tableView.getTableViewSelectionListeners().add(this);
    }

    public void uninstall() {
        TableView tableView = (TableView)getComponent();
        tableView.getTableViewListeners().remove(this);
        tableView.getTableViewColumnListeners().remove(this);
        tableView.getTableViewRowListeners().remove(this);
        tableView.getTableViewRowStateListeners().remove(this);
        tableView.getTableViewSelectionListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TableView tableView = (TableView)getComponent();
        TableView.ColumnSequence columns = tableView.getColumns();

        int n = columns.getLength();
        int gridLineStop = includeTrailingVerticalGridLine ? n : n - 1;

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (!column.isRelative()) {
                preferredWidth += column.getWidth();

                // Include space for vertical gridlines; even if we are
                // not painting them, the header does
                if (i < gridLineStop) {
                    preferredWidth++;
                }
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        TableView tableView = (TableView)getComponent();

        int n = tableView.getTableData().getLength();
        preferredHeight = getRowHeight() * n;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    @SuppressWarnings("unchecked")
    public void paint(Graphics2D graphics) {
        TableView tableView = (TableView)getComponent();
        List<Object> tableData = (List<Object>)tableView.getTableData();
        TableView.ColumnSequence columns = tableView.getColumns();

        int width = getWidth();
        int height = getHeight();

        int rowHeight = getRowHeight();
        Sequence<Integer> columnWidths = getColumnWidths();

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        // Paint the list contents
        int rowStart = 0;
        int rowEnd = tableData.getLength() - 1;

        // Ensure that we only paint items that are visible
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            rowStart = Math.max(rowStart, (int)Math.floor(clipBounds.y
                / (double)rowHeight));
            rowEnd = Math.min(rowEnd, (int)Math.ceil((clipBounds.y
                + clipBounds.height) / (double)rowHeight) - 1);
        }

        int rowY = rowStart * rowHeight;

        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
            Object rowData = tableData.get(rowIndex);
            boolean rowHighlighted = (rowIndex == highlightedIndex
                && tableView.getSelectMode() != TableView.SelectMode.NONE);
            boolean rowSelected = tableView.isRowSelected(rowIndex);
            boolean rowDisabled = tableView.isRowDisabled(rowIndex);

            Color rowBackgroundColor = null;

            if (rowSelected) {
                rowBackgroundColor = (tableView.isFocused())
                    ? this.selectionBackgroundColor : inactiveSelectionBackgroundColor;
            } else {
                if (rowHighlighted && showHighlight && !rowDisabled) {
                    rowBackgroundColor = highlightBackgroundColor;
                } else {
                    if (alternateRowColor != null
                        && rowIndex % 2 > 0) {
                        rowBackgroundColor = alternateRowColor;
                    }
                }
            }

            if (rowBackgroundColor != null) {
                graphics.setPaint(rowBackgroundColor);
                graphics.fillRect(0, rowY, width, rowHeight);
            }

            // Paint the cells
            int cellX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                TableView.Column column = columns.get(columnIndex);

                TableView.CellRenderer cellRenderer = column.getCellRenderer();

                int columnWidth = columnWidths.get(columnIndex);

                Graphics2D rendererGraphics = (Graphics2D)graphics.create(cellX, rowY,
                    columnWidth, rowHeight);

                cellRenderer.render(rowData, tableView, column, rowSelected,
                    rowHighlighted, rowDisabled);
                cellRenderer.setSize(columnWidth, rowHeight - 1);
                cellRenderer.paint(rendererGraphics);

                rendererGraphics.dispose();

                cellX += columnWidth + 1;
            }

            rowY += rowHeight;
        }

        // Set the grid stroke and color
        graphics.setPaint(gridColor);

        // Paint the vertical grid lines
        if (showVerticalGridLines) {
            int gridX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                gridX += columnWidths.get(columnIndex);

                GraphicsUtilities.drawLine(graphics, gridX, 0, height, Orientation.VERTICAL);
                gridX++;
            }
        }

        // Paint the horizontal grid line
        if (showHorizontalGridLines) {
            for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                if (rowIndex > 0) {
                    int gridY = rowIndex * rowHeight;
                    GraphicsUtilities.drawLine(graphics, 0, gridY, width, Orientation.HORIZONTAL);
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
        int rowHeight = 0;

        TableView tableView = (TableView)getComponent();
        TableView.ColumnSequence columns = tableView.getColumns();

        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);
            TableView.CellRenderer cellRenderer = column.getCellRenderer();
            cellRenderer.render(null, tableView, column, false, false, false);

            rowHeight = Math.max(rowHeight, cellRenderer.getPreferredHeight(-1));
        }

        rowHeight++;

        return rowHeight;
    }

    /**
     * Returns the column widths for this table.
     *
     * @return
     * The widths of all columns based on the current overall width.
     */
    public Sequence<Integer> getColumnWidths() {
        TableView tableView = (TableView)getComponent();

        return getColumnWidths(tableView.getColumns(), getWidth());
    }

    /**
     * Returns the column widths, determined by applying relative size values
     * to the available width.
     *
     * TODO Cache these values and recalculate only when size changes.
     *
     * @param columns
     * The columns whose widths are to be determined.
     *
     * @param width
     * The total available width for the columns.
     *
     * @return
     * The widths of all columns based on the current overall width.
     */
    public static Sequence<Integer> getColumnWidths(TableView.ColumnSequence columns, int width) {
        int fixedWidth = 0;
        int relativeWidth = 0;

        int n = columns.getLength();

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.isRelative()) {
                relativeWidth += column.getWidth();
            } else {
                fixedWidth += column.getWidth();
            }
        }

        fixedWidth += n - 1;
        int variableWidth = Math.max(width - fixedWidth, 0);

        ArrayList<Integer> columnWidths = new ArrayList<Integer>(columns.getLength());

        for (int i = 0; i < n; i++) {
            TableView.Column column = columns.get(i);

            if (column.isRelative()) {
                int columnWidth = (int)Math.round((double)(column.getWidth()
                    * variableWidth) / (double)relativeWidth);
                columnWidths.add(columnWidth);
            } else {
                columnWidths.add(column.getWidth());
            }
        }

        return columnWidths;
    }

    // Table view skin methods
    @SuppressWarnings("unchecked")
    public int getRowAt(int y) {
        if (y < 0) {
            throw new IllegalArgumentException("y is negative");
        }

        TableView tableView = (TableView)getComponent();
        List<Object> tableData = (List<Object>)tableView.getTableData();

        int rowIndex = (y / getRowHeight());

        if (rowIndex >= tableData.getLength()) {
            rowIndex = -1;
        }

        return rowIndex;
    }

    public int getColumnAt(int x) {
        if (x < 0) {
            throw new IllegalArgumentException("x is negative");
        }

        Sequence<Integer> columnWidths = getColumnWidths();

        int i = 0;
        int n = columnWidths.getLength();
        int columnX = 0;
        while (i < n
            && x > columnX) {
            columnX += (columnWidths.get(i) + 1);
            i++;
        }

        int columnIndex = -1;

        if (x <= columnX) {
            columnIndex = i - 1;
        }

        return columnIndex;
    }

    public Bounds getRowBounds(int rowIndex) {
        int rowHeight = getRowHeight();
        return new Bounds(0, rowIndex * rowHeight, getWidth(), rowHeight);
    }

    public Bounds getColumnBounds(int columnIndex) {
        Sequence<Integer> columnWidths = getColumnWidths();
        int columnCount = columnWidths.getLength();

        if (columnIndex < 0
            || columnIndex >= columnCount) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " +
                columnIndex);
        }

        int columnX = 0;
        for (int i = 0; i < columnIndex; i++) {
            columnX += (columnWidths.get(i) + 1);
        }

        return new Bounds(columnX, 0, columnWidths.get(columnIndex), getHeight());
    }

    @SuppressWarnings("unchecked")
    public Bounds getCellBounds(int rowIndex, int columnIndex) {
        TableView tableView = (TableView)getComponent();
        List<Object> tableData = (List<Object>)tableView.getTableData();
        Sequence<Integer> columnWidths = getColumnWidths();

        if (rowIndex < 0
            || rowIndex >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        if (columnIndex < 0
            || columnIndex >= columnWidths.getLength()) {
            throw new IndexOutOfBoundsException();
        }


        int rowHeight = getRowHeight();

        int cellX = 0;
        for (int i = 0; i < columnIndex; i++) {
            cellX += (columnWidths.get(i) + 1);
        }

        return new Bounds(cellX, rowIndex * rowHeight,
            columnWidths.get(columnIndex), rowHeight);
    }

    @Override
    public boolean isFocusable() {
        TableView tableView = (TableView)getComponent();
        return (tableView.getSelectMode() != TableView.SelectMode.NONE);
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

        setFont(Font.decode(font));
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
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

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

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        this.gridColor = gridColor;
        repaintComponent();
    }

    public final void setGridColor(String gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        setGridColor(GraphicsUtilities.decodeColor(gridColor));
    }

    public final void setGridColor(int gridColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setGridColor(theme.getColor(gridColor));
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

    public boolean getShowHighlight() {
        return showHighlight;
    }

    public void setShowHighlight(boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    public boolean getIncludeTrailingVerticalGridLine() {
        return includeTrailingVerticalGridLine;
    }

    public void setIncludeTrailingVerticalGridLine(boolean includeTrailingVerticalGridLine) {
        this.includeTrailingVerticalGridLine = includeTrailingVerticalGridLine;
        invalidateComponent();
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        int previousHighlightedIndex = this.highlightedIndex;
        highlightedIndex = getRowAt(y);

        if (previousHighlightedIndex != highlightedIndex) {
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

        if (highlightedIndex != -1) {
            Bounds rowBounds = getRowBounds(highlightedIndex);
            repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
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

            if (Keyboard.isPressed(Keyboard.Modifier.SHIFT)
                && selectMode == TableView.SelectMode.MULTI) {
                // Select the range
                int startIndex = tableView.getFirstSelectedIndex();
                int endIndex = tableView.getLastSelectedIndex();
                Span selectedRange = (rowIndex > startIndex) ?
                    new Span(startIndex, rowIndex) : new Span(rowIndex, endIndex);

                ArrayList<Span> selectedRanges = new ArrayList<Span>();
                Sequence<Integer> disabledIndexes = tableView.getDisabledIndexes();
                if (disabledIndexes.getLength() == 0) {
                    selectedRanges.add(selectedRange);
                } else {
                    // TODO Split the range by the disabled indexes; for now,
                    // just return
                    return consumed;
                }

                tableView.setSelectedRanges(selectedRanges);
            } else if (Keyboard.isPressed(Keyboard.Modifier.CTRL)
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

            if (rowEditor != null) {
                rowEditor.edit(tableView, editIndex, getColumnAt(x));
            }
        }

        editIndex = -1;

        return consumed;
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        if (highlightedIndex != -1) {
            Bounds rowBounds = getRowBounds(highlightedIndex);
            repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
        }

        highlightedIndex = -1;

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
        if (highlightedIndex != -1) {
            highlightedIndex = -1;
            repaintComponent(getRowBounds(highlightedIndex));
        }

        return consumed;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        repaintComponent(!component.isFocused());
    }

    // Table view events
    public void tableDataChanged(TableView tableView, List<?> previousTableData) {
        invalidateComponent();
    }

    public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
        // No-op
    }

    public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
        repaintComponent();
    }

    // Table view column events
    public void columnInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
        invalidateComponent();
    }

    public void columnNameChanged(TableView.Column column, String previousName) {
        invalidateComponent();
    }

    public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
        // No-op
    }

    public void columnWidthChanged(TableView.Column column, int previousWidth, boolean previousRelative)  {
        invalidateComponent();
    }

    public void columnSortDirectionChanged(TableView.Column column, SortDirection previousSortDirection) {
        // TODO Repaint; paint a "selection" color for the sorted column
    }

    public void columnFilterChanged(TableView.Column column, Object previousFilter) {
        // No-op
    }

    public void columnCellRendererChanged(TableView.Column column, TableView.CellRenderer previousCellRenderer) {
        invalidateComponent();
    }

    // Table view row events
    public void rowInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    public void rowsRemoved(TableView tableView, int index, int count) {
        invalidateComponent();
    }

    public void rowUpdated(TableView tableView, int index) {
        repaintComponent(getRowBounds(index));
    }

    public void rowsCleared(TableView listView) {
        repaintComponent();
    }

    public void rowsSorted(TableView tableView) {
        repaintComponent();
    }

    // Table view row state events
    public void rowDisabledChanged(TableView tableView, int index) {
        repaintComponent(getRowBounds(index));
    }

    // Table view selection detail events
    public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the added selection
        int rowHeight = getRowHeight();
        repaintComponent(0, rangeStart * rowHeight,
            getWidth(), (rangeEnd - rangeStart + 1) * rowHeight);
    }

    public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
        // Repaint the area containing the removed selection
        int rowHeight = getRowHeight();
        repaintComponent(0, rangeStart * rowHeight,
            getWidth(), (rangeEnd - rangeStart + 1) * rowHeight);
    }

    public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
        // TODO Repaint only the area that changed (intersection of previous
        // and new selection)
        repaintComponent();
    }
}
