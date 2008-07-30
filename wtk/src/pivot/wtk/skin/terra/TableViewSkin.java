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
package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Rectangle;
import pivot.wtk.SortDirection;
import pivot.wtk.Span;
import pivot.wtk.TableView;
import pivot.wtk.TableViewListener;
import pivot.wtk.TableViewColumnListener;
import pivot.wtk.TableViewRowListener;
import pivot.wtk.TableViewRowStateListener;
import pivot.wtk.TableViewSelectionDetailListener;
import pivot.wtk.skin.ComponentSkin;

/**
 * NOTE This skin assumes a fixed renderer height.
 *
 * TODO Support an "alternateRowBackgroundColor" style.
 *
 * TODO Support a "showHighlight" style?
 *
 * TODO Add disableMouseSelection style to support the case where selection
 * should be enabled but the caller wants to implement the management of it;
 * e.g. changing a message's flag state in an email client.
 *
 * @author gbrown
 */
public class TableViewSkin extends ComponentSkin implements TableView.Skin,
    TableViewListener, TableViewColumnListener, TableViewRowListener,
    TableViewRowStateListener, TableViewSelectionDetailListener {
    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color backgroundColor = Color.WHITE;
    private Color selectionColor = Color.WHITE;
    private Color selectionBackgroundColor = new Color(0x14, 0x53, 0x8B);
    private Color inactiveSelectionColor = Color.BLACK;
    private Color inactiveSelectionBackgroundColor = new Color(0xcc, 0xca, 0xc2);
    private Color highlightColor = Color.BLACK;
    private Color highlightBackgroundColor = new Color(0xe6, 0xe3, 0xda);
    private Color gridColor = new Color(0xF7, 0xF5, 0xEB);
    private boolean showHorizontalGridLines = true;
    private boolean showVerticalGridLines = true;

    private int highlightedIndex = -1;

    public void install(Component component) {
        validateComponentType(component, TableView.class);

        super.install(component);

        TableView tableView = (TableView)component;
        tableView.getTableViewListeners().add(this);
        tableView.getTableViewColumnListeners().add(this);
        tableView.getTableViewRowListeners().add(this);
        tableView.getTableViewRowStateListeners().add(this);
        tableView.getTableViewSelectionDetailListeners().add(this);
    }

    public void uninstall() {
        TableView tableView = (TableView)getComponent();
        tableView.getTableViewListeners().remove(this);
        tableView.getTableViewColumnListeners().remove(this);
        tableView.getTableViewRowListeners().remove(this);
        tableView.getTableViewRowStateListeners().remove(this);
        tableView.getTableViewSelectionDetailListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        TableView tableView = (TableView)getComponent();
        TableView.ColumnSequence columns = tableView.getColumns();

        for (int i = 0, n = columns.getLength(); i < n; i++) {
            TableView.Column column = columns.get(i);

            if (!column.isRelative()) {
                preferredWidth += column.getWidth();

                // Include space for vertical gridlines; even if we are
                // not painting them, the header does
                if (i > 0
                    && i < n - 1) {
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
        Shape clip = graphics.getClip();
        if (clip != null) {
            Rectangle2D clipBounds = clip.getBounds();
            rowStart = (int)Math.floor((double)clipBounds.getY() / (double)rowHeight);
            rowEnd = Math.min(rowEnd, (int)Math.ceil((double)(clipBounds.getY()
                + clipBounds.getHeight()) / (double)rowHeight) - 1);
        }

        int rowY = rowStart * rowHeight;

        for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
            Object rowData = tableData.get(rowIndex);
            boolean rowHighlighted = (rowIndex == highlightedIndex
                && Mouse.getButtons() == 0
                && tableView.getSelectMode() != TableView.SelectMode.NONE);
            boolean rowSelected = tableView.isIndexSelected(rowIndex);
            boolean rowDisabled = tableView.isRowDisabled(rowIndex);

            Color rowBackgroundColor = null;

            if (rowSelected) {
                rowBackgroundColor = (tableView.isFocused())
                    ? this.selectionBackgroundColor : inactiveSelectionBackgroundColor;
            } else {
                if (rowHighlighted && !rowDisabled) {
                    rowBackgroundColor = highlightBackgroundColor;
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

                cellX += columnWidth + 1;
            }

            rowY += rowHeight;
        }

        // Set the grid stroke and color
        graphics.setStroke(new BasicStroke());
        graphics.setPaint(gridColor);

        // Paint the vertical grid lines
        if (showVerticalGridLines) {
            int gridX = 0;

            for (int columnIndex = 0, columnCount = columns.getLength();
                columnIndex < columnCount; columnIndex++) {
                gridX += columnWidths.get(columnIndex);

                graphics.drawLine(gridX, 0, gridX, height);
                gridX++;
            }
        }

        // Paint the horizontal grid line
        if (showHorizontalGridLines) {
            for (int rowIndex = rowStart; rowIndex <= rowEnd; rowIndex++) {
                if (rowIndex > 0) {
                    int gridY = rowIndex * rowHeight;
                    graphics.drawLine(0, gridY, width, gridY);
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

    public Rectangle getRowBounds(int rowIndex) {
        int rowHeight = getRowHeight();
        return new Rectangle(0, rowIndex * rowHeight, getWidth(), rowHeight);
    }

    @SuppressWarnings("unchecked")
    public Rectangle getCellBounds(int rowIndex, int columnIndex) {
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

        return new Rectangle(cellX, rowIndex * rowHeight,
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

        setColor(Color.decode(color));
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

        setDisabledColor(Color.decode(disabledColor));
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

        setBackgroundColor(Color.decode(backgroundColor));
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

        setSelectionColor(Color.decode(selectionColor));
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

        setSelectionBackgroundColor(Color.decode(selectionBackgroundColor));
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

        setInactiveSelectionColor(Color.decode(inactiveSelectionColor));
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

        setInactiveSelectionBackgroundColor(Color.decode(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public final void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(Color.decode(highlightColor));
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

        setHighlightBackgroundColor(Color.decode(highlightBackgroundColor));
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

        setGridColor(Color.decode(gridColor));
    }

    public boolean getShowHorizontalGridLines() {
        return showHorizontalGridLines;
    }

    public void setShowHorizontalGridLines(boolean showHorizontalGridLines) {
        this.showHorizontalGridLines = showHorizontalGridLines;
        repaintComponent();
    }

    public final void setShowHorizontalGridLines(String showHorizontalGridLines) {
        if (showHorizontalGridLines == null) {
            throw new IllegalArgumentException("showHorizontalGridLines is null.");
        }

        setShowHorizontalGridLines(Boolean.parseBoolean(showHorizontalGridLines));
    }

    public boolean getShowVerticalGridLines() {
        return showVerticalGridLines;
    }

    public void setShowVerticalGridLines(boolean showVerticalGridLines) {
        this.showVerticalGridLines = showVerticalGridLines;
        repaintComponent();
    }

    public final void setShowVerticalGridLines(String showVerticalGridLines) {
        if (showVerticalGridLines == null) {
            throw new IllegalArgumentException("showVerticalGridLines is null.");
        }

        setShowVerticalGridLines(Boolean.parseBoolean(showVerticalGridLines));
    }

    @Override
    public boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

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
    public void mouseOut() {
        super.mouseOut();

        if (highlightedIndex != -1) {
            Rectangle rowBounds = getRowBounds(highlightedIndex);
            repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
        }

        highlightedIndex = -1;
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        TableView tableView = (TableView)getComponent();

        if (isFocusable()) {
            Component.setFocusedComponent(tableView);
        }

        int rowIndex = getRowAt(y);

        if (rowIndex >= 0
            && !tableView.isRowDisabled(rowIndex)) {
            TableView.SelectMode selectMode = tableView.getSelectMode();
            int keyboardModifiers = Keyboard.getModifiers();

            if ((keyboardModifiers & Keyboard.Modifier.SHIFT.getMask()) > 0
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
                    return;
                }

                tableView.setSelectedRanges(selectedRanges);
            } else if ((keyboardModifiers & Keyboard.Modifier.CTRL.getMask()) > 0
                && selectMode == TableView.SelectMode.MULTI) {
                // Toggle the item's selection state
                if (tableView.isIndexSelected(rowIndex)) {
                    tableView.removeSelectedIndex(rowIndex);
                } else {
                    tableView.addSelectedIndex(rowIndex);
                }
            } else {
                // Select the row
                if ((selectMode == TableView.SelectMode.SINGLE
                        && tableView.getSelectedIndex() != rowIndex)
                    || selectMode == TableView.SelectMode.MULTI) {
                    tableView.setSelectedIndex(rowIndex);
                }
            }
        }
    }

    @Override
    public boolean mouseWheel(Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        if (highlightedIndex != -1) {
            Rectangle rowBounds = getRowBounds(highlightedIndex);
            repaintComponent(rowBounds.x, rowBounds.y, rowBounds.width, rowBounds.height);
        }

        highlightedIndex = -1;

        return super.mouseWheel(scrollType, scrollAmount, wheelRotation, x, y);
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(keyCode, keyLocation);

        TableView tableView = (TableView)getComponent();

        switch (keyCode) {
            case Keyboard.KeyCode.UP: {
                int index = tableView.getFirstSelectedIndex();

                do {
                    index--;
                } while (index >= 0
                    && tableView.isRowDisabled(index));

                if (index >= 0) {
                    if ((Keyboard.getModifiers() & Keyboard.Modifier.SHIFT.getMask()) > 0
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
                    if ((Keyboard.getModifiers() & Keyboard.Modifier.SHIFT.getMask()) > 0
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

        repaintComponent();
    }

    // Table view events
    public void tableDataChanged(TableView tableView, List<?> previousTableData) {
        invalidateComponent();
    }

    public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
        // No-op
    }

    // Table view column events
    public void columnInserted(TableView tableView, int index) {
        invalidateComponent();
    }

    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
        invalidateComponent();
    }

    public void columnNameChanged(TableView tableView, int index, String previousName) {
        invalidateComponent();
    }

    public void columnHeaderDataChanged(TableView tableView, int index, Object previousHeaderData) {
        // No-op
    }

    public void columnWidthChanged(TableView tableView, int index, int previousWidth, boolean previousRelative)  {
        invalidateComponent();
    }

    public void columnSortDirectionChanged(TableView tableView, int index, SortDirection previousSortDirection) {
        // No-op
        // TODO Repaint; paint a "selection" color for the sorted column
    }

    public void columnFilterChanged(TableView tableView, int index, Object previousFilter) {
        // No-op
    }

    public void columnCellRendererChanged(TableView tableView, int index, TableView.CellRenderer previousCellRenderer) {
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

    public void selectionReset(TableView tableView, Sequence<Span> previousSelectedRanges) {
        // TODO Repaint only the area that changed (intersection of previous
        // and new selection)
        repaintComponent();
    }
}
