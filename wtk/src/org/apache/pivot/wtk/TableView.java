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
package org.apache.pivot.wtk;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableList;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.TableViewCellRenderer;
import org.apache.pivot.wtk.content.TableViewHeaderDataRenderer;

/**
 * Component that displays a sequence of rows partitioned into columns,
 * optionally allowing a user to select one or more rows.
 */
@DefaultProperty("tableData")
public class TableView extends Component {
    /**
     * Contains information about a table column.
     */
    @DefaultProperty("cellRenderer")
    public static class Column {
        private TableView tableView = null;

        private String name = null;
        private Object headerData = null;
        private HeaderDataRenderer headerDataRenderer = DEFAULT_HEADER_DATA_RENDERER;
        private int width = 0;
        private int minimumWidth = 0;
        private int maximumWidth = Integer.MAX_VALUE;
        private boolean relative = false;
        private Object filter = null;
        private CellRenderer cellRenderer = DEFAULT_CELL_RENDERER;

        private static final CellRenderer DEFAULT_CELL_RENDERER = new TableViewCellRenderer();
        private static final HeaderDataRenderer DEFAULT_HEADER_DATA_RENDERER = new TableViewHeaderDataRenderer();

        /**
         * Default column width.
         */
        public static final int DEFAULT_WIDTH = 100;

        /**
         * Creates an empty column.
         */
        public Column() {
            this(null, null, DEFAULT_WIDTH, false);
        }

        /**
         * Creates a new column with no header data and a fixed default width.
         *
         * @param name
         * The column name.
         */
        public Column(String name) {
            this(name, null, DEFAULT_WIDTH, false);
        }

        /**
         * Creates a new column with a fixed default width.
         *
         * @param name
         * The column name.
         *
         * @param headerData
         * The column header data.
         */
        public Column(String name, Object headerData) {
            this(name, headerData, DEFAULT_WIDTH, false);
        }

        /**
         * Creates a new column with a fixed width.
         *
         * @param name
         * The column name.
         *
         * @param headerData
         * The column header data.
         *
         * @param width
         * The width of the column.
         */
        public Column(String name, Object headerData, int width) {
            this(name, headerData, width, false);
        }

        /**
         * Creates a new column.
         *
         * @param name
         * The column name.
         *
         * @param headerData
         * The column header data.
         *
         * @param width
         * The width of the column.
         *
         * @param relative
         * If <tt>true</tt>, specifies a relative column width; otherwise,
         * specifies a fixed column width.
         */
        public Column(String name, Object headerData, int width, boolean relative) {
            setName(name);
            setHeaderData(headerData);
            setWidth(width, relative);
        }

        /**
         * Returns the table view with which this column is associated.
         *
         * @return
         * The column's table view, or <tt>null</tt> if the column does not
         * currently belong to a table.
         */
        public TableView getTableView() {
            return tableView;
        }

        /**
         * Returns the column name.
         *
         * @return
         * The column name.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the column name.
         *
         * @param name
         * The column name.
         */
        public void setName(String name) {
            String previousName = this.name;

            if (previousName != name) {
                this.name = name;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnNameChanged(this,
                        previousName);
                }
            }
        }

        /**
         * Returns the column header data.
         *
         * @return
         * The column header data, or <tt>null</tt> if the column has no
         * header data.
         */
        public Object getHeaderData() {
            return headerData;
        }

        /**
         * Sets the column header data.
         *
         * @param headerData
         * The column header data, or <tt>null</tt> for no header data.
         */
        public void setHeaderData(Object headerData) {
            Object previousHeaderData = this.headerData;

            if (previousHeaderData != headerData) {
                this.headerData = headerData;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnHeaderDataChanged(this,
                        previousHeaderData);
                }
            }
        }

        /**
         * Returns the column header data renderer.
         */
        public HeaderDataRenderer getHeaderDataRenderer() {
            return headerDataRenderer;
        }

        /**
         * Sets the column header data renderer.
         *
         * @param headerDataRenderer
         */
        public void setHeaderDataRenderer(HeaderDataRenderer headerDataRenderer) {
            if (headerDataRenderer == null) {
                throw new IllegalArgumentException("headerDataRenderer is null.");
            }

            HeaderDataRenderer previousHeaderDataRenderer = this.headerDataRenderer;
            if (previousHeaderDataRenderer != headerDataRenderer) {
                this.headerDataRenderer = headerDataRenderer;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnHeaderDataRendererChanged(this,
                        previousHeaderDataRenderer);
                }
            }
        }

        /**
         * Returns the column width.
         *
         * @return
         * The width of the column.
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the relative flag.
         *
         * @return
         * <tt>true</tt> if the column width is relative, <tt>false</tt> if it
         * is fixed.
         */
        public boolean isRelative() {
            return relative;
        }

        /**
         * Set the column width.
         *
         * @param width
         * The absolute width of the column.
         */
        public void setWidth(int width) {
            setWidth(width, false);
        }

        /**
         * Set the column width.
         *
         * @param width
         * The encoded width of the row. If the string ends with the '*'
         * character, it is treated as a relative value. Otherwise, it is
         * considered an absolute value.
         */
        public void setWidth(String width) {
            boolean relativeLocal = false;

            if (width.endsWith("*")) {
                relativeLocal = true;
                width = width.substring(0, width.length() - 1);
            }

            setWidth(Integer.parseInt(width), relativeLocal);
        }

        /**
         * Sets the column width.
         *
         * @param width
         * The width of the column.
         *
         * @param relative
         * <tt>true</tt> if the column width is relative, <tt>false</tt> if it
         * is fixed.
         */
        public void setWidth(int width, boolean relative) {
            if (width < (relative ? 0 : -1)) {
                throw new IllegalArgumentException("illegal width " + width);
            }

            int previousWidth = this.width;
            boolean previousRelative = this.relative;

            if (previousWidth != width
                || previousRelative != relative) {
                this.width = width;
                this.relative = relative;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnWidthChanged(this,
                        previousWidth, previousRelative);
                }
            }
        }

        /**
         * Gets the minimum and maximum widths to which the column can size.
         */
        public Limits getWidthLimits() {
            return new Limits(minimumWidth, maximumWidth);
        }

        /**
         * Sets the minimum and maximum widths the column can size to.
         *
         * @param minimumWidth
         * Column width cannot be smaller than this size.
         *
         * @param maximumWidth
         * Column width cannot be greater than this size.
         */
        public void setWidthLimits(int minimumWidth, int maximumWidth) {
            if (minimumWidth < 0) {
                throw new IllegalArgumentException("Minimum width is negative, " + minimumWidth);
            }

            if (maximumWidth < minimumWidth) {
                throw new IllegalArgumentException("Maximum width is smaller than minimum width, "
                            + maximumWidth + "<" + minimumWidth);
            }

            int previousMinimumWidth = this.minimumWidth;
            int previousMaximumWidth = this.maximumWidth;

            if (minimumWidth != previousMinimumWidth
                || maximumWidth != previousMaximumWidth) {
                this.minimumWidth = minimumWidth;
                this.maximumWidth = maximumWidth;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnWidthLimitsChanged(this,
                        previousMinimumWidth, previousMaximumWidth);
                }
            }
        }

        /**
         * Sets the minimum and maximum widths to which the column can size.
         *
         * @param widthLimits
         * The new width limits.
         */
        public void setWidthLimits(Limits widthLimits) {
            setWidthLimits(widthLimits.minimum, widthLimits.maximum);
        }

        /**
         * Gets the minimum width the column is allowed to be.
         *
         * @return
         * Minimum column width.
         */
        public int getMinimumWidth() {
            return minimumWidth;
        }

        /**
         * Sets the minimum width the column is allowed to be.
         *
         * @param minimumWidth
         * Minimum column width.
         */
        public void setMinimumWidth(int minimumWidth) {
            setWidthLimits(minimumWidth, maximumWidth);
        }

        /**
         * Get the maximum width the column is allowed to be.
         *
         * @return
         * Maximum column width.
         */
        public int getMaximumWidth() {
            return maximumWidth;
        }

        /**
         * Set the maximum width the column is allowed to be.
         *
         * @param maximumWidth
         * Maximum column width.
         */
        public void setMaximumWidth(int maximumWidth) {
            setWidthLimits(minimumWidth, maximumWidth);
        }

        /**
         * Returns the column's filter.
         *
         * @return
         * The column's filter, or <tt>null</tt> if the column does not have
         * a filter.
         */
        public Object getFilter() {
            return filter;
        }

        /**
         * Sets the column's filter.
         *
         * @param filter
         * The column's filter, or <tt>null</tt> for no filter.
         */
        public void setFilter(Object filter) {
            Object previousFilter = this.filter;

            if (previousFilter != filter) {
                this.filter = filter;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnFilterChanged(this,
                        previousFilter);
                }
            }
        }

        /**
         * Returns the column's cell renderer.
         *
         * @return
         * The cell renderer that is used to draw the contents of this column.
         */
        public CellRenderer getCellRenderer() {
            return cellRenderer;
        }

        /**
         * Sets the column's cell renderer.
         *
         * @param cellRenderer
         * The cell renderer that is used to draw the contents of this column.
         */
        public void setCellRenderer(CellRenderer cellRenderer) {
            if (cellRenderer == null) {
                throw new IllegalArgumentException("cellRenderer is null.");
            }

            CellRenderer previousCellRenderer = this.cellRenderer;

            if (previousCellRenderer != cellRenderer) {
                this.cellRenderer = cellRenderer;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnCellRendererChanged(this,
                        previousCellRenderer);
                }
            }
        }
    }

    /**
     * Enumeration defining supported selection modes.
     */
    public enum SelectMode {
        /**
         * Selection is disabled.
         */
        NONE,

        /**
         * A single index may be selected at a time.
         */
        SINGLE,

        /**
         * Multiple indexes may be concurrently selected.
         */
        MULTI
    }

    /**
     * {@link Renderer} interface to customize the appearance of a cell in a TableView.
     */
    public interface CellRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param row
         * The row to render, or <tt>null</tt> if called to calculate preferred height for
         * skins that assume a fixed renderer height.
         *
         * @param rowIndex
         * The index of the row being rendered, or <tt>-1</tt> if <tt>value</tt>
         * is <tt>null</tt>.
         *
         * @param columnIndex
         * The index of the column being rendered.
         *
         * @param tableView
         * The host component.
         *
         * @param columnName
         * The name of the column being rendered.
         *
         * @param selected
         * If <tt>true</tt>, the row is selected.
         *
         * @param highlighted
         * If <tt>true</tt>, the row is highlighted.
         *
         * @param disabled
         * If <tt>true</tt>, the row is disabled.
         */
        public void render(Object row, int rowIndex, int columnIndex, TableView tableView,
            String columnName, boolean selected, boolean highlighted, boolean disabled);

        /**
         * Converts table view cell data to a string representation.
         *
         * @param row
         * @param columnName
         *
         * @return
         * The cell data's string representation, or <tt>null</tt> if the data does not
         * have a string representation.
         * <p>
         * Note that this method may be called often during keyboard navigation, so
         * implementations should avoid unnecessary string allocations.
         */
        public String toString(Object row, String columnName);
    }

    /**
     * {@link Renderer} interface to customize the appearance of the header of a TableView
     */
    public interface HeaderDataRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param data
         * The data to render, or <tt>null</tt> if called to calculate preferred
         * height for skins that assume a fixed renderer height.
         *
         * @param columnIndex
         * The index of the column being rendered.
         *
         * @param tableViewHeader
         * The host component.
         *
         * @param columnName
         * The name of the column being rendered.
         *
         * @param highlighted
         * If <tt>true</tt>, the item is highlighted.
         */
        public void render(Object data, int columnIndex, TableViewHeader tableViewHeader,
            String columnName, boolean highlighted);

        /**
         * Converts table view header data to a string representation.
         *
         * @param item
         *
         * @return
         * The data's string representation, or <tt>null</tt> if the data does not
         * have a string representation.
         * <p>
         * Note that this method may be called often during keyboard navigation, so
         * implementations should avoid unnecessary string allocations.
         */
        public String toString(Object item);
    }

    /**
     * Table view row editor interface.
     */
    public interface RowEditor {
        /**
         * Called to begin editing a table row.
         *
         * @param tableView
         * @param rowIndex
         * @param columnIndex
         */
        public void beginEdit(TableView tableView, int rowIndex, int columnIndex);

        /**
         * Terminates an edit operation.
         *
         * @param result
         * <tt>true</tt> to perform the edit; <tt>false</tt> to cancel it.
         */
        public void endEdit(boolean result);

        /**
         * Tests whether an edit is currently in progress.
         */
        public boolean isEditing();
    }

    /**
     * Table view skin interface. Table view skins must implement this.
     */
    public interface Skin {
        public int getRowAt(int y);
        public int getColumnAt(int x);
        public Bounds getRowBounds(int rowIndex);
        public Bounds getColumnBounds(int columnIndex);
        public Bounds getCellBounds(int rowIndex, int columnIndex);
    }

    /**
     * Translates between table and bind context data during data binding.
     */
    public interface TableDataBindMapping {
        /**
         * Converts a context value to table data.
         *
         * @param value
         */
        public List<?> toTableData(Object value);

        /**
         * Converts table data to a context value.
         *
         * @param tableData
         */
        public Object valueOf(List<?> tableData);
    }

    /**
     * Translates between selection and bind context data during data binding.
     */
    public interface SelectedRowBindMapping {
        /**
         * Returns the index of the row in the source list.
         *
         * @param tableData
         * The source table data.
         *
         * @param value
         * The value to locate.
         *
         * @return
         * The index of first occurrence of the value if it exists in the list;
         * <tt>-1</tt>, otherwise.
         */
        public int indexOf(List<?> tableData, Object value);

        /**
         * Retrieves the value at the given index.
         *
         * @param tableData
         * The source table data.
         *
         * @param index
         * The index of the value to retrieve.
         */
        public Object get(List<?> tableData, int index);
    }

    /**
     * Column sequence implementation.
     */
    public final class ColumnSequence implements Sequence<Column>, Iterable<Column> {
        @Override
        public int add(Column column) {
            int index = getLength();
            insert(column, index);

            return index;
        }

        @Override
        public void insert(Column column, int index) {
            if (column == null) {
                throw new IllegalArgumentException("column is null.");
            }

            if (column.tableView != null) {
                throw new IllegalArgumentException("column is already in use by another table view.");
            }

            columns.insert(column, index);
            column.tableView = TableView.this;

            tableViewColumnListeners.columnInserted(TableView.this, index);
        }

        @Override
        public Column update(int index, Column column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Column column) {
            int index = indexOf(column);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Column> remove(int index, int count) {
            Sequence<Column> removed = columns.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    removed.get(i).tableView = null;
                }

                tableViewColumnListeners.columnsRemoved(TableView.this, index, removed);
            }

            return removed;
        }

        @Override
        public Column get(int index) {
            return columns.get(index);
        }

        @Override
        public int indexOf(Column column) {
            return columns.indexOf(column);
        }

        @Override
        public int getLength() {
            return columns.getLength();
        }

        @Override
        public Iterator<Column> iterator() {
            return new ImmutableIterator<Column>(columns.iterator());
        }
    }

    /**
     * Sort dictionary implementation.
     */
    public final class SortDictionary implements Dictionary<String, SortDirection>, Iterable<String> {
        @Override
        public SortDirection get(String columnName) {
            return sortMap.get(columnName);
        }

        @Override
        public SortDirection put(String columnName, SortDirection sortDirection) {
            SortDirection previousSortDirection;

            if (sortDirection == null) {
                previousSortDirection = remove(columnName);
            } else {
                boolean update = containsKey(columnName);
                previousSortDirection = sortMap.put(columnName, sortDirection);

                if (update) {
                    tableViewSortListeners.sortUpdated(TableView.this, columnName, previousSortDirection);
                } else {
                    sortList.add(columnName);
                    tableViewSortListeners.sortAdded(TableView.this, columnName);
                }
            }

            return previousSortDirection;
        }

        @Override
        public SortDirection remove(String columnName) {
            SortDirection sortDirection = null;

            if (containsKey(columnName)) {
                sortDirection = sortMap.remove(columnName);
                sortList.remove(columnName);
                tableViewSortListeners.sortRemoved(TableView.this, columnName, sortDirection);
            }

            return sortDirection;
        }

        @Override
        public boolean containsKey(String columnName) {
            return sortMap.containsKey(columnName);
        }

        public boolean isEmpty() {
            return sortMap.isEmpty();
        }

        public Dictionary.Pair<String, SortDirection> get(int index) {
            String columnName = sortList.get(index);
            SortDirection sortDirection = sortMap.get(columnName);

            return new Dictionary.Pair<String, SortDirection>(columnName, sortDirection);
        }

        public int getLength() {
            return sortList.getLength();
        }

        @Override
        public Iterator<String> iterator() {
            return sortList.iterator();
        }
    }

    private static class TableViewListenerList extends WTKListenerList<TableViewListener>
        implements TableViewListener {
        @Override
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            for (TableViewListener listener : this) {
                listener.tableDataChanged(tableView, previousTableData);
            }
        }

        @Override
        public void columnSourceChanged(TableView tableView, TableView previousColumnSource) {
            for (TableViewListener listener : this) {
                listener.columnSourceChanged(tableView, previousColumnSource);
            }
        }

        @Override
        public void rowEditorChanged(TableView tableView,
            TableView.RowEditor previousRowEditor) {
            for (TableViewListener listener : this) {
                listener.rowEditorChanged(tableView, previousRowEditor);
            }
        }

        @Override
        public void selectModeChanged(TableView tableView, SelectMode previousSelectMode) {
            for (TableViewListener listener : this) {
                listener.selectModeChanged(tableView, previousSelectMode);
            }
        }

        @Override
        public void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter) {
            for (TableViewListener listener : this) {
                listener.disabledRowFilterChanged(tableView, previousDisabledRowFilter);
            }
        }
    }

    private static class TableViewColumnListenerList extends WTKListenerList<TableViewColumnListener>
        implements TableViewColumnListener {
        @Override
        public void columnInserted(TableView tableView, int index) {
            for (TableViewColumnListener listener : this) {
                listener.columnInserted(tableView, index);
            }
        }

        @Override
        public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
            for (TableViewColumnListener listener : this) {
                listener.columnsRemoved(tableView, index, columns);
            }
        }

        @Override
        public void columnNameChanged(Column column, String previousName) {
            for (TableViewColumnListener listener : this) {
                listener.columnNameChanged(column, previousName);
            }
        }

        @Override
        public void columnHeaderDataChanged(Column column, Object previousHeaderData) {
            for (TableViewColumnListener listener : this) {
                listener.columnHeaderDataChanged(column, previousHeaderData);
            }
        }

        @Override
        public void columnHeaderDataRendererChanged(TableView.Column column,
            TableView.HeaderDataRenderer previousHeaderDataRenderer) {
            for (TableViewColumnListener listener : this) {
                listener.columnHeaderDataRendererChanged(column, previousHeaderDataRenderer);
            }
        }

        @Override
        public void columnWidthChanged(Column column, int previousWidth, boolean previousRelative) {
            for (TableViewColumnListener listener : this) {
                listener.columnWidthChanged(column, previousWidth, previousRelative);
            }
        }

        @Override
        public void columnWidthLimitsChanged(Column column, int  previousMinimumWidth, int previousMaximumWidth) {
            for (TableViewColumnListener listener : this) {
                listener.columnWidthLimitsChanged(column, previousMinimumWidth, previousMaximumWidth);
            }
        }

        @Override
        public void columnFilterChanged(Column column, Object previousFilter) {
            for (TableViewColumnListener listener : this) {
                listener.columnFilterChanged(column, previousFilter);
            }
        }

        @Override
        public void columnCellRendererChanged(Column column, TableView.CellRenderer previousCellRenderer) {
            for (TableViewColumnListener listener : this) {
                listener.columnCellRendererChanged(column, previousCellRenderer);
            }
        }
    }

    private static class TableViewRowListenerList extends WTKListenerList<TableViewRowListener>
        implements TableViewRowListener {
        @Override
        public void rowInserted(TableView tableView, int index) {
            for (TableViewRowListener listener : this) {
                listener.rowInserted(tableView, index);
            }
        }

        @Override
        public void rowsRemoved(TableView tableView, int index, int count) {
            for (TableViewRowListener listener : this) {
                listener.rowsRemoved(tableView, index, count);
            }
        }

        @Override
        public void rowUpdated(TableView tableView, int index) {
            for (TableViewRowListener listener : this) {
                listener.rowUpdated(tableView, index);
            }
        }

        @Override
        public void rowsCleared(TableView tableView) {
            for (TableViewRowListener listener : this) {
                listener.rowsCleared(tableView);
            }
        }

        @Override
        public void rowsSorted(TableView tableView) {
            for (TableViewRowListener listener : this) {
                listener.rowsSorted(tableView);
            }
        }
    }

    private static class TableViewSelectionListenerList extends WTKListenerList<TableViewSelectionListener>
        implements TableViewSelectionListener {
        @Override
        public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRangeAdded(tableView, rangeStart, rangeEnd);
            }
        }

        @Override
        public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRangeRemoved(tableView, rangeStart, rangeEnd);
            }
        }

        @Override
        public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelection) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRangesChanged(tableView, previousSelection);
            }
        }

        @Override
        public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRowChanged(tableView, previousSelectedRow);
            }
        }
    }

    private static class TableViewSortListenerList extends WTKListenerList<TableViewSortListener>
        implements TableViewSortListener {
        @Override
        public void sortAdded(TableView tableView, String columnName) {
            for (TableViewSortListener listener : this) {
                listener.sortAdded(tableView, columnName);
            }
        }

        @Override
        public void sortUpdated(TableView tableView, String columnName,
            SortDirection previousSortDirection) {
            for (TableViewSortListener listener : this) {
                listener.sortUpdated(tableView, columnName, previousSortDirection);
            }
        }

        @Override
        public void sortRemoved(TableView tableView, String columnName,
            SortDirection sortDirection) {
            for (TableViewSortListener listener : this) {
                listener.sortRemoved(tableView, columnName, sortDirection);
            }
        }

        @Override
        public void sortChanged(TableView tableView) {
            for (TableViewSortListener listener : this) {
                listener.sortChanged(tableView);
            }
        }
    }

    private static class TableViewBindingListenerList extends WTKListenerList<TableViewBindingListener>
        implements TableViewBindingListener {
        @Override
        public void tableDataKeyChanged(TableView tableView, String previousTableDataKey) {
            for (TableViewBindingListener listener : this) {
                listener.tableDataKeyChanged(tableView, previousTableDataKey);
            }
        }

        @Override
        public void tableDataBindTypeChanged(TableView tableView, BindType previousTableDataBindType) {
            for (TableViewBindingListener listener : this) {
                listener.tableDataBindTypeChanged(tableView, previousTableDataBindType);
            }
        }

        @Override
        public void tableDataBindMappingChanged(TableView tableView,
            TableView.TableDataBindMapping previousTableDataBindMapping) {
            for (TableViewBindingListener listener : this) {
                listener.tableDataBindMappingChanged(tableView, previousTableDataBindMapping);
            }
        }

        @Override
        public void selectedRowKeyChanged(TableView tableView, String previousSelectedRowKey) {
            for (TableViewBindingListener listener : this) {
                listener.selectedRowKeyChanged(tableView, previousSelectedRowKey);
            }
        }

        @Override
        public void selectedRowBindTypeChanged(TableView tableView, BindType previousSelectedRowBindType) {
            for (TableViewBindingListener listener : this) {
                listener.selectedRowBindTypeChanged(tableView, previousSelectedRowBindType);
            }
        }

        @Override
        public void selectedRowBindMappingChanged(TableView tableView,
            TableView.SelectedRowBindMapping previousSelectedRowBindMapping) {
            for (TableViewBindingListener listener : this) {
                listener.selectedRowBindMappingChanged(tableView, previousSelectedRowBindMapping);
            }
        }

        @Override
        public void selectedRowsKeyChanged(TableView tableView, String previousSelectedRowsKey) {
            for (TableViewBindingListener listener : this) {
                listener.selectedRowsKeyChanged(tableView, previousSelectedRowsKey);
            }
        }

        @Override
        public void selectedRowsBindTypeChanged(TableView tableView, BindType previousSelectedRowsBindType) {
            for (TableViewBindingListener listener : this) {
                listener.selectedRowsBindTypeChanged(tableView, previousSelectedRowsBindType);
            }
        }

        @Override
        public void selectedRowsBindMappingChanged(TableView tableView,
            TableView.SelectedRowBindMapping previousSelectedRowsBindMapping) {
            for (TableViewBindingListener listener : this) {
                listener.selectedRowsBindMappingChanged(tableView, previousSelectedRowsBindMapping);
            }
        }
    }

    private ArrayList<Column> columns = new ArrayList<Column>();
    private ColumnSequence columnSequence = new ColumnSequence();

    private List<?> tableData = null;
    private TableView columnSource = null;

    private RowEditor rowEditor = null;

    private RangeSelection rangeSelection = new RangeSelection();
    private SelectMode selectMode = SelectMode.SINGLE;

    private HashMap<String, SortDirection> sortMap = new HashMap<String, SortDirection>();
    private ArrayList<String> sortList = new ArrayList<String>();
    private SortDictionary sortDictionary = new SortDictionary();

    private Filter<?> disabledRowFilter = null;

    private String tableDataKey = null;
    private BindType tableDataBindType = BindType.BOTH;
    private TableDataBindMapping tableDataBindMapping = null;

    private String selectedRowKey = null;
    private BindType selectedRowBindType = BindType.BOTH;
    private SelectedRowBindMapping selectedRowBindMapping = null;

    private String selectedRowsKey = null;
    private BindType selectedRowsBindType = BindType.BOTH;
    private SelectedRowBindMapping selectedRowsBindMapping = null;

    private ListListener<Object> tableDataListener = new ListListener<Object>() {
        @Override
        public void itemInserted(List<Object> list, int index) {
            // Increment selected ranges
            int updated = rangeSelection.insertIndex(index);

            // Notify listeners that items were inserted
            tableViewRowListeners.rowInserted(TableView.this, index);

            if (updated > 0) {
                tableViewSelectionListeners.selectedRangesChanged(TableView.this, getSelectedRanges());
            }
        }

        @Override
        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int count = items.getLength();

            int previousSelectedIndex;
            if (selectMode == SelectMode.SINGLE
                && rangeSelection.getLength() > 0) {
                previousSelectedIndex = rangeSelection.get(0).start;
            } else {
                previousSelectedIndex = -1;
            }

            // Decrement selected ranges
            int updated = rangeSelection.removeIndexes(index, count);

            // Notify listeners that items were removed
            tableViewRowListeners.rowsRemoved(TableView.this, index, count);

            if (updated > 0) {
                tableViewSelectionListeners.selectedRangesChanged(TableView.this, getSelectedRanges());

                if (selectMode == SelectMode.SINGLE
                    && getSelectedIndex() != previousSelectedIndex) {
                    tableViewSelectionListeners.selectedRowChanged(TableView.this, null);
                }
            }
        }

        @Override
        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            tableViewRowListeners.rowUpdated(TableView.this, index);
        }

        @Override
        public void listCleared(List<Object> list) {
            int cleared = rangeSelection.getLength();
            rangeSelection.clear();

            tableViewRowListeners.rowsCleared(TableView.this);

            if (cleared > 0) {
                tableViewSelectionListeners.selectedRangesChanged(TableView.this, getSelectedRanges());

                if (selectMode == SelectMode.SINGLE) {
                    tableViewSelectionListeners.selectedRowChanged(TableView.this, null);
                }
            }
        }

        @Override
        public void comparatorChanged(List<Object> list, Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int cleared = rangeSelection.getLength();
                rangeSelection.clear();
                tableViewRowListeners.rowsSorted(TableView.this);

                if (cleared > 0) {
                    tableViewSelectionListeners.selectedRangesChanged(TableView.this, getSelectedRanges());

                    if (selectMode == SelectMode.SINGLE) {
                        tableViewSelectionListeners.selectedRowChanged(TableView.this, null);
                    }
                }
            }
        }
    };

    private TableViewListenerList tableViewListeners = new TableViewListenerList();
    private TableViewColumnListenerList tableViewColumnListeners = new TableViewColumnListenerList();
    private TableViewRowListenerList tableViewRowListeners = new TableViewRowListenerList();
    private TableViewSelectionListenerList tableViewSelectionListeners = new TableViewSelectionListenerList();
    private TableViewSortListenerList tableViewSortListeners = new TableViewSortListenerList();
    private TableViewBindingListenerList tableViewBindingListeners = new TableViewBindingListenerList();

    public static final String COLUMN_NAME_KEY = "columnName";
    public static final String SORT_DIRECTION_KEY = "sortDirection";

    /**
     * Creates a new table view populated with an empty array list.
     */
    public TableView() {
        this(new ArrayList<Object>());
    }

    /**
     * Creates a new table view populated with the given table data.
     *
     * @param tableData
     */
    public TableView(List<?> tableData) {
        setTableData(tableData);
        installSkin(TableView.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        if (!(skin instanceof TableView.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TableView.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the table column sequence.
     *
     * @return
     * The table column sequence.
     */
    public ColumnSequence getColumns() {
        ColumnSequence columnSequenceLocal = this.columnSequence;

        if (columnSource != null) {
            columnSequenceLocal = columnSource.getColumns();
        }

        return columnSequenceLocal;
    }

    /**
     * Returns the table data.
     *
     * @return
     * The data currently presented by the table view.
     */
    public List<?> getTableData() {
        return this.tableData;
    }

    /**
     * Sets the table data.
     *
     * @param tableData
     * The data to be presented by the table view.
     */
    @SuppressWarnings("unchecked")
    public void setTableData(List<?> tableData) {
        if (tableData == null) {
            throw new IllegalArgumentException("tableData is null.");
        }

        List<?> previousTableData = this.tableData;

        if (previousTableData != tableData) {
            int cleared;
            if (previousTableData != null) {
                // Clear any existing selection
                cleared = rangeSelection.getLength();
                rangeSelection.clear();

                ((List<Object>)previousTableData).getListListeners().remove(tableDataListener);
            } else {
                cleared = 0;
            }

            ((List<Object>)tableData).getListListeners().add(tableDataListener);

            // Update the list data and fire change event
            this.tableData = tableData;
            tableViewListeners.tableDataChanged(this, previousTableData);

            if (cleared > 0) {
                tableViewSelectionListeners.selectedRangesChanged(this, getSelectedRanges());

                if (selectMode == SelectMode.SINGLE) {
                    tableViewSelectionListeners.selectedRowChanged(this, null);
                }
            }
        }
    }

    /**
     * Sets the table data.
     *
     * @param tableData
     * A JSON string (must begin with <tt>[</tt> and end with <tt>]</tt>)
     * denoting the data to be presented by the table view.
     */
    public final void setTableData(String tableData) {
        if (tableData == null) {
            throw new IllegalArgumentException("tableData is null.");
        }

        try {
            setTableData(JSONSerializer.parseList(tableData));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Sets the table data.
     *
     * @param tableData
     * A URL referring to a JSON file containing the data to be presented by
     * the table view.
     */
    public void setTableData(URL tableData) {
        if (tableData == null) {
            throw new IllegalArgumentException("tableData is null.");
        }

        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            setTableData((List<?>)jsonSerializer.readObject(tableData.openStream()));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public TableView getColumnSource() {
        return columnSource;
    }

    public void setColumnSource(TableView columnSource) {
        TableView previousColumnSource = this.columnSource;

        if (previousColumnSource != columnSource) {
            this.columnSource = columnSource;
            tableViewListeners.columnSourceChanged(this, previousColumnSource);
        }
    }

    /**
     * Returns the editor used to edit rows in this table.
     *
     * @return
     * The row editor, or <tt>null</tt> if no editor is installed.
     */
    public RowEditor getRowEditor() {
        return rowEditor;
    }

    /**
     * Sets the editor used to edit rows in this table.
     *
     * @param rowEditor
     * The row editor for the list.
     */
    public void setRowEditor(RowEditor rowEditor) {
        RowEditor previousRowEditor = this.rowEditor;

        if (previousRowEditor != rowEditor) {
            this.rowEditor = rowEditor;
            tableViewListeners.rowEditorChanged(this, previousRowEditor);
        }
    }

    /**
     * Returns the currently selected index, even when in multi-select mode.
     *
     * @return
     * The currently selected index.
     */
    public int getSelectedIndex() {
        return getFirstSelectedIndex();
    }

    /**
     * Sets the selection to a single index.
     *
     * @param index
     * The index to select, or <tt>-1</tt> to clear the selection.
     */
    public void setSelectedIndex(int index) {
        if (index == -1) {
            clearSelection();
        } else {
            setSelectedRange(index, index);
        }
    }

    /**
     * Sets the selection to a single range.
     *
     * @param start
     * @param end
     */
    public void setSelectedRange(int start, int end) {
        ArrayList<Span> selectedRanges = new ArrayList<Span>();
        selectedRanges.add(new Span(start, end));

        setSelectedRanges(selectedRanges);
    }

    /**
     * Returns the currently selected ranges.
     *
     * @return
     * An immutable list containing the currently selected ranges. Note that the returned
     * list is a wrapper around the actual selection, not a copy. Any changes made to the
     * selection state will be reflected in the list, but events will not be fired.
     */
    public ImmutableList<Span> getSelectedRanges() {
        return rangeSelection.getSelectedRanges();
    }

    /**
     * Sets the selection to the given range sequence. Any overlapping or
     * connecting ranges will be consolidated, and the resulting selection will
     * be sorted in ascending order.
     *
     * @param selectedRanges
     *
     * @return
     * The ranges that were actually set.
     */
    public Sequence<Span> setSelectedRanges(Sequence<Span> selectedRanges) {
        if (selectedRanges == null) {
            throw new IllegalArgumentException("selectedRanges is null.");
        }

        // When we're in mode NONE, the only thing we can do is to clear the selection
        if (selectMode == SelectMode.NONE
            && selectedRanges.getLength() > 0) {
            throw new IllegalArgumentException("Selection is not enabled.");
        }

        // Update the selection
        Sequence<Span> previousSelectedRanges = this.rangeSelection.getSelectedRanges();
        Object previousSelectedRow = (selectMode == SelectMode.SINGLE) ? getSelectedRow() : null;

        RangeSelection listSelection = new RangeSelection();
        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span range = selectedRanges.get(i);

            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            if (range.start < 0) {
                throw new IndexOutOfBoundsException("range.start < 0, " + range.start);
            }
            if (range.end >= tableData.getLength()) {
                throw new IndexOutOfBoundsException("range.end >= tableData length, "
                            + range.end + " >= " + tableData.getLength());
            }

            listSelection.addRange(range.start, range.end);
        }

        this.rangeSelection = listSelection;

        // Notify listeners
        tableViewSelectionListeners.selectedRangesChanged(this, previousSelectedRanges);

        if (selectMode == SelectMode.SINGLE) {
            tableViewSelectionListeners.selectedRowChanged(this, previousSelectedRow);
        }

        return getSelectedRanges();
    }

    /**
     * Sets the selection to the given range sequence.
     *
     * @param selectedRanges
     * A JSON-formatted string containing the ranges to select.
     *
     * @return
     * The ranges that were actually set.
     *
     * @see #setSelectedRanges(Sequence)
     */
    public final Sequence<Span> setSelectedRanges(String selectedRanges) {
        if (selectedRanges == null) {
            throw new IllegalArgumentException("selectedRanges is null.");
        }

        try {
            setSelectedRanges(parseSelectedRanges(selectedRanges));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return getSelectedRanges();
    }

    @SuppressWarnings("unchecked")
    private static Sequence<Span> parseSelectedRanges(String json)
        throws SerializationException {
        ArrayList<Span> selectedRanges = new ArrayList<Span>();

        List<?> list = JSONSerializer.parseList(json);
        for (Object item : list) {
            Map<String, ?> map = (Map<String, ?>)item;
            selectedRanges.add(new Span(map));
        }

        return selectedRanges;
    }

    /**
     * Returns the first selected index.
     *
     * @return
     * The first selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getFirstSelectedIndex() {
        return (rangeSelection.getLength() > 0) ? rangeSelection.get(0).start : -1;
    }

    /**
     * Returns the last selected index.
     *
     * @return
     * The last selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getLastSelectedIndex() {
        return (rangeSelection.getLength() > 0) ?
            rangeSelection.get(rangeSelection.getLength() - 1).end : -1;
    }

    /**
     * Adds a single index to the selection.
     *
     * @param index
     * The index to add.
     *
     * @return
     * <tt>true</tt> if the index was added to the selection; <tt>false</tt>,
     * otherwise.
     */
    public boolean addSelectedIndex(int index) {
        Sequence<Span> addedRanges = addSelectedRange(index, index);
        return (addedRanges.getLength() > 0);
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param start
     * The first index in the range.
     *
     * @param end
     * The last index in the range.
     *
     * @return
     * The ranges that were added to the selection.
     */
    public Sequence<Span> addSelectedRange(int start, int end) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Table view is not in multi-select mode.");
        }

        if (start < 0) {
            throw new IndexOutOfBoundsException("start < 0, " + start);
        }
        if (end >= tableData.getLength()) {
            throw new IndexOutOfBoundsException("end >= tableData.getLength(), "
                  + end + " >= " + tableData.getLength());
        }

        Sequence<Span> addedRanges = rangeSelection.addRange(start, end);

        int n = addedRanges.getLength();
        for (int i = 0; i < n; i++) {
            Span addedRange = addedRanges.get(i);
            tableViewSelectionListeners.selectedRangeAdded(this, addedRange.start, addedRange.end);
        }

        if (n > 0) {
            tableViewSelectionListeners.selectedRangesChanged(this, null);
        }

        return addedRanges;
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param range
     * The range to add.
     *
     * @return
     * The ranges that were added to the selection.
     */
    public Sequence<Span> addSelectedRange(Span range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        return addSelectedRange(range.start, range.end);
    }

    /**
     * Removes a single index from the selection.
     *
     * @param index
     * The index to remove.
     *
     * @return
     * <tt>true</tt> if the index was removed from the selection;
     * <tt>false</tt>, otherwise.
     */
    public boolean removeSelectedIndex(int index) {
        Sequence<Span> removedRanges = removeSelectedRange(index, index);
        return (removedRanges.getLength() > 0);
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param start
     * The start of the range to remove.
     *
     * @param end
     * The end of the range to remove.
     *
     * @return
     * The ranges that were removed from the selection.
     */
    public Sequence<Span> removeSelectedRange(int start, int end) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Table view is not in multi-select mode.");
        }

        if (start < 0) {
            throw new IndexOutOfBoundsException("start < 0, " + start);
        }
        if (end >= tableData.getLength()) {
            throw new IndexOutOfBoundsException("end >= tableData.getLength(), "
                  + end + " >= " + tableData.getLength());
        }

        Sequence<Span> removedRanges = rangeSelection.removeRange(start, end);

        int n = removedRanges.getLength();
        for (int i = 0; i < n; i++) {
            Span removedRange = removedRanges.get(i);
            tableViewSelectionListeners.selectedRangeRemoved(this, removedRange.start, removedRange.end);
        }

        if (n > 0) {
            tableViewSelectionListeners.selectedRangesChanged(this, null);
        }

        return removedRanges;
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param range
     * The range to remove.
     *
     * @return
     * The ranges that were removed from the selection.
     */
    public Sequence<Span> removeSelectedRange(Span range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        return removeSelectedRange(range.start, range.end);
    }

    /**
     * Selects all rows in the table.
     */
    public void selectAll() {
        setSelectedRange(0, tableData.getLength() - 1);
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (rangeSelection.getLength() > 0) {
            setSelectedRanges(new ArrayList<Span>(0));
        }
    }

    /**
     * Returns the selection state of a given index.
     *
     * @param index
     * The index whose selection state is to be tested.
     *
     * @return <tt>true</tt> if the index is selected; <tt>false</tt>,
     * otherwise.
     */
    public boolean isRowSelected(int index) {
        indexBoundsCheck("index", index, 0, tableData.getLength() - 1);

        return rangeSelection.containsIndex(index);
    }

    public Object getSelectedRow() {
        int index = getSelectedIndex();
        Object row = null;

        if (index >= 0) {
            row = tableData.get(index);
        }

        return row;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedRow(Object row) {
        setSelectedIndex((row == null) ? -1 : ((List<Object>)tableData).indexOf(row));
    }

    public Sequence<?> getSelectedRows() {
        ArrayList<Object> rows = new ArrayList<Object>();

        for (int i = 0, n = rangeSelection.getLength(); i < n; i++) {
            Span range = rangeSelection.get(i);

            for (int index = range.start; index <= range.end; index++) {
                Object row = tableData.get(index);
                rows.add(row);
            }
        }

        return rows;
    }

    @SuppressWarnings("unchecked")
    public void setSelectedRows(Sequence<Object> rows) {
        if (rows == null) {
            throw new IllegalArgumentException("rows is null");
        }

        ArrayList<Span> selectedRanges = new ArrayList<Span>();

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            Object row = rows.get(i);
            if (row == null) {
                throw new IllegalArgumentException("item is null");
            }

            int index = ((List<Object>)tableData).indexOf(row);
            if (index == -1) {
                throw new IllegalArgumentException("\"" + row + "\" is not a valid selection.");
            }

            selectedRanges.add(new Span(index));
        }

        setSelectedRanges(selectedRanges);
    }

    /**
     * Returns the current selection mode.
     */
    public SelectMode getSelectMode() {
        return selectMode;
    }

    /**
     * Sets the selection mode. Clears the selection if the mode has changed.
     *
     * @param selectMode
     * The new selection mode.
     */
    public void setSelectMode(SelectMode selectMode) {
        if (selectMode == null) {
            throw new IllegalArgumentException("selectMode is null.");
        }

        SelectMode previousSelectMode = this.selectMode;

        if (previousSelectMode != selectMode) {
            // Clear any current selection
            clearSelection();

            // Update the selection mode
            this.selectMode = selectMode;

            // Fire select mode change event
            tableViewListeners.selectModeChanged(this, previousSelectMode);
        }
    }

    /**
     * Returns the table view's sort dictionary.
     */
    public SortDictionary getSort() {
        return sortDictionary;
    }

    /**
     * Sets the table view's sort.
     *
     * @param columnName
     * @param sortDirection
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, SortDirection> setSort(String columnName, SortDirection sortDirection) {
        Dictionary.Pair<String, SortDirection> sort =
            new Dictionary.Pair<String, SortDirection>(columnName, sortDirection);

        setSort(new ArrayList<Dictionary.Pair<String, SortDirection>>(sort));

        return getSort();
    }

    /**
     * Sets the table view's sort.
     *
     * @param sort
     * A sequence of key/value pairs representing the sort. Keys represent column names and
     * values represent sort direction.
     */
    public Dictionary<String, SortDirection> setSort(Sequence<Dictionary.Pair<String, SortDirection>> sort) {
        if (sort == null) {
            throw new IllegalArgumentException("sort is null");
        }

        sortMap.clear();
        sortList.clear();

        for (int i = 0, n = sort.getLength(); i < n; i++) {
            Dictionary.Pair<String, SortDirection> pair = sort.get(i);

            if (!sortMap.containsKey(pair.key)) {
                sortMap.put(pair.key, pair.value);
                sortList.add(pair.key);
            }
        }

        tableViewSortListeners.sortChanged(this);

        return getSort();
    }

    /**
     * Sets the table view's sort.
     *
     * @param sort
     * A JSON list containing JSON objects representing the sort.
     *
     * @see #setSort(Sequence)
     */
    public final Dictionary<String, SortDirection> setSort(String sort) {
        if (sort == null) {
            throw new IllegalArgumentException("sort is null");
        }

        try {
            setSort(parseSort(sort));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return getSort();
    }

    @SuppressWarnings("unchecked")
    private static Sequence<Dictionary.Pair<String, SortDirection>> parseSort(String json)
        throws SerializationException {
        ArrayList<Dictionary.Pair<String, SortDirection>> sort =
            new ArrayList<Dictionary.Pair<String, SortDirection>>();

        List<?> list = JSONSerializer.parseList(json);
        for (Object item : list) {
            Map<String, ?> map = (Map<String, ?>)item;

            Dictionary.Pair<String, SortDirection> pair =
                new Dictionary.Pair<String, SortDirection>((String)map.get(COLUMN_NAME_KEY),
                    SortDirection.valueOf(((String)map.get(SORT_DIRECTION_KEY)).toUpperCase(Locale.ENGLISH)));
            sort.add(pair);
        }

        return sort;
    }

    /**
     * Clears the sort.
     */
    public void clearSort() {
        if (!sortMap.isEmpty()) {
            sortMap.clear();
            sortList.clear();
            tableViewSortListeners.sortChanged(this);
        }
    }

    /**
     * Returns the disabled state of a given row.
     *
     * @param index
     * The index of the row whose disabled state is to be tested.
     *
     * @return
     * <tt>true</tt> if the row is disabled; <tt>false</tt>,
     * otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean isRowDisabled(int index) {
        boolean disabled = false;

        if (disabledRowFilter != null) {
            Object row = tableData.get(index);
            disabled = ((Filter<Object>)disabledRowFilter).include(row);
        }

        return disabled;
    }

    /**
     * Returns the disabled row filter.
     *
     * @return
     * The disabled row filter, or <tt>null</tt> if no disabled row filter is
     * set.
     */
    public Filter<?> getDisabledRowFilter() {
        return disabledRowFilter;
    }

    /**
     * Sets the disabled row filter.
     *
     * @param disabledRowFilter
     * The disabled row filter, or <tt>null</tt> for no disabled row filter.
     */
    public void setDisabledRowFilter(Filter<?> disabledRowFilter) {
        Filter<?> previousDisabledRowFilter = this.disabledRowFilter;

        if (previousDisabledRowFilter != disabledRowFilter) {
            this.disabledRowFilter = disabledRowFilter;
            tableViewListeners.disabledRowFilterChanged(this, previousDisabledRowFilter);
        }
    }

    public String getTableDataKey() {
        return tableDataKey;
    }

    public void setTableDataKey(String tableDataKey) {
        String previousTableDataKey = this.tableDataKey;

        if (previousTableDataKey != tableDataKey) {
            this.tableDataKey = tableDataKey;
            tableViewBindingListeners.tableDataKeyChanged(this, previousTableDataKey);
        }
    }

    public BindType getTableDataBindType() {
        return tableDataBindType;
    }

    public void setTableDataBindType(BindType tableDataBindType) {
        if (tableDataBindType == null) {
            throw new IllegalArgumentException("tableDataBindType is null");
        }

        BindType previousTableDataBindType = this.tableDataBindType;

        if (previousTableDataBindType != tableDataBindType) {
            this.tableDataBindType = tableDataBindType;
            tableViewBindingListeners.tableDataBindTypeChanged(this, previousTableDataBindType);
        }
    }

    public TableDataBindMapping getTableDataBindMapping() {
        return tableDataBindMapping;
    }

    public void setTableDataBindMapping(TableDataBindMapping tableDataBindMapping) {
        TableDataBindMapping previousTableDataBindMapping = this.tableDataBindMapping;

        if (previousTableDataBindMapping != tableDataBindMapping) {
            this.tableDataBindMapping = tableDataBindMapping;
            tableViewBindingListeners.tableDataBindMappingChanged(this, previousTableDataBindMapping);
        }
    }

    public String getSelectedRowKey() {
        return selectedRowKey;
    }

    public void setSelectedRowKey(String selectedRowKey) {
        String previousSelectedRowKey = this.selectedRowKey;

        if (previousSelectedRowKey != selectedRowKey) {
            this.selectedRowKey = selectedRowKey;
            tableViewBindingListeners.selectedRowKeyChanged(this, previousSelectedRowKey);
        }
    }

    public BindType getSelectedRowBindType() {
        return selectedRowBindType;
    }

    public void setSelectedRowBindType(BindType selectedRowBindType) {
        if (selectedRowBindType == null) {
            throw new IllegalArgumentException("selectedRowBindType is null");
        }

        BindType previousSelectedRowBindType = this.selectedRowBindType;
        if (previousSelectedRowBindType != selectedRowBindType) {
            this.selectedRowBindType = selectedRowBindType;
            tableViewBindingListeners.selectedRowBindTypeChanged(this, previousSelectedRowBindType);
        }
    }

    public SelectedRowBindMapping getSelectedRowBindMapping() {
        return selectedRowBindMapping;
    }

    public void setSelectedRowBindMapping(SelectedRowBindMapping selectedRowBindMapping) {
        SelectedRowBindMapping previousSelectedRowBindMapping = this.selectedRowBindMapping;

        if (previousSelectedRowBindMapping != selectedRowBindMapping) {
            this.selectedRowBindMapping = selectedRowBindMapping;
            tableViewBindingListeners.selectedRowBindMappingChanged(this, previousSelectedRowBindMapping);
        }
    }

    public String getSelectedRowsKey() {
        return selectedRowsKey;
    }

    public void setSelectedRowsKey(String selectedRowsKey) {
        String previousSelectedRowsKey = this.selectedRowsKey;

        if (previousSelectedRowsKey != selectedRowsKey) {
            this.selectedRowsKey = selectedRowsKey;
            tableViewBindingListeners.selectedRowsKeyChanged(this, previousSelectedRowsKey);
        }
    }

    public BindType getSelectedRowsBindType() {
        return selectedRowsBindType;
    }

    public void setSelectedRowsBindType(BindType selectedRowsBindType) {
        if (selectedRowsBindType == null) {
            throw new IllegalArgumentException("selectedRowsBindType is null");
        }

        BindType previousSelectedRowsBindType = this.selectedRowsBindType;
        if (previousSelectedRowsBindType != selectedRowsBindType) {
            this.selectedRowsBindType = selectedRowsBindType;
            tableViewBindingListeners.selectedRowsBindTypeChanged(this, previousSelectedRowsBindType);
        }
    }

    public SelectedRowBindMapping getSelectedRowsBindMapping() {
        return selectedRowsBindMapping;
    }

    public void setSelectedRowsBindMapping(SelectedRowBindMapping selectedRowsBindMapping) {
        SelectedRowBindMapping previousSelectedRowsBindMapping = this.selectedRowsBindMapping;

        if (previousSelectedRowsBindMapping != selectedRowsBindMapping) {
            this.selectedRowsBindMapping = selectedRowsBindMapping;
            tableViewBindingListeners.selectedRowsBindMappingChanged(this, previousSelectedRowsBindMapping);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(Object context) {
        // Bind to list data
        if (tableDataKey != null
            && tableDataBindType != BindType.STORE
            && JSON.containsKey(context, tableDataKey)) {
            Object value = JSON.get(context, tableDataKey);

            List<?> tableDataLocal;
            if (tableDataBindMapping == null) {
                tableDataLocal = (List<?>)value;
            } else {
                tableDataLocal = tableDataBindMapping.toTableData(value);
            }

            setTableData(tableDataLocal);
        }

        switch (selectMode) {
            case SINGLE: {
                // Bind using selected row key
                if (selectedRowKey != null
                    && selectedRowBindType != BindType.STORE
                    && JSON.containsKey(context, selectedRowKey)) {
                    Object row = JSON.get(context, selectedRowKey);

                    int index;
                    if (selectedRowBindMapping == null) {
                        index = ((List<Object>)tableData).indexOf(row);
                    } else {
                        index = selectedRowBindMapping.indexOf(tableData, row);
                    }

                    setSelectedIndex(index);
                }

                break;
            }

            case MULTI: {
                // Bind using selected rows key
                if (selectedRowsKey != null
                    && selectedRowsBindType != BindType.STORE
                    && JSON.containsKey(context, selectedRowsKey)) {
                    Sequence<Object> rows = (Sequence<Object>)JSON.get(context, selectedRowsKey);

                    clearSelection();

                    for (int i = 0, n = rows.getLength(); i < n; i++) {
                        Object row = rows.get(i);

                        int index;
                        if (selectedRowsBindMapping == null) {
                            index = ((List<Object>)tableData).indexOf(row);
                        } else {
                            index = selectedRowsBindMapping.indexOf(tableData, row);
                        }

                        if (index != -1) {
                            addSelectedIndex(index);
                        }
                    }
                }

                break;
            }
            case NONE:
                break;
        }
    }

    @Override
    public void store(Object context) {
        // Bind to table data
        if (tableDataKey != null
            && tableDataBindType != BindType.LOAD) {
            Object value;
            if (tableDataBindMapping == null) {
                value = tableData;
            } else {
                value = tableDataBindMapping.valueOf(tableData);
            }

            JSON.put(context, tableDataKey, value);
        }

        switch (selectMode) {
            case SINGLE: {
                // Bind using selected row key
                if (selectedRowKey != null
                    && selectedRowBindType != BindType.LOAD) {
                    Object row;

                    int selectedIndex = getSelectedIndex();
                    if (selectedIndex == -1) {
                        row = null;
                    } else {
                        if (selectedRowBindMapping == null) {
                            row = tableData.get(selectedIndex);
                        } else {
                            row = selectedRowBindMapping.get(tableData, selectedIndex);
                        }
                    }

                    JSON.put(context, selectedRowKey, row);
                }

                break;
            }

            case MULTI: {
                // Bind using selected rows key
                if (selectedRowsKey != null
                    && selectedRowsBindType != BindType.LOAD) {
                    ArrayList<Object> rows = new ArrayList<Object>();

                    Sequence<Span> selectedRanges = getSelectedRanges();
                    for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
                        Span range = selectedRanges.get(i);

                        for (int index = range.start; index <= range.end; index++) {
                            Object row;
                            if (selectedRowsBindMapping == null) {
                                row = tableData.get(index);
                            } else {
                                row = selectedRowsBindMapping.get(tableData, index);
                            }

                            rows.add(row);
                        }
                    }

                    JSON.put(context, selectedRowsKey, rows);
                }

                break;
            }
            case NONE:
                break;
        }
    }

    @Override
    public void clear() {
        if (tableDataKey != null) {
            setTableData(new ArrayList<Object>());
        }

        if (selectedRowKey != null
            || selectedRowsKey != null) {
            setSelectedRow(null);
        }
    }

    /**
     * Returns the index of the row at a given location.
     *
     * @param y
     * The y-coordinate of the row to identify.
     *
     * @return
     * The row index, or <tt>-1</tt> if there is no row at the given
     * y-coordinate.
     */
    public int getRowAt(int y) {
        TableView.Skin tableViewSkin = (TableView.Skin)getSkin();
        return tableViewSkin.getRowAt(y);
    }

    /**
     * Returns the index of the column at a given location.
     *
     * @param x
     * The x-coordinate of the column to identify.
     *
     * @return
     * The column index, or <tt>-1</tt> if there is no column at the given
     * x-coordinate.
     */
    public int getColumnAt(int x) {
        TableView.Skin tableViewSkin = (TableView.Skin)getSkin();
        return tableViewSkin.getColumnAt(x);
    }

    /**
     * Returns the bounding area of a given row.
     *
     * @param rowIndex
     * The row index.
     *
     * @return
     * The bounding area of the row.
     */
    public Bounds getRowBounds(int rowIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin)getSkin();
        return tableViewSkin.getRowBounds(rowIndex);
    }

    /**
     * Returns the bounding area of a given column.
     *
     * @param columnIndex
     * The column index.
     *
     * @return
     * The bounding area of the column.
     */
    public Bounds getColumnBounds(int columnIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin)getSkin();
        return tableViewSkin.getColumnBounds(columnIndex);
    }

    /**
     * Returns the bounding area of a given cell.
     *
     * @param rowIndex
     * The row index of the cell.
     *
     * @param columnIndex
     * The column index of the cell.
     *
     * @return
     * The bounding area of the cell.
     */
    public Bounds getCellBounds(int rowIndex, int columnIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin)getSkin();
        return tableViewSkin.getCellBounds(rowIndex, columnIndex);
    }

    public ListenerList<TableViewListener> getTableViewListeners() {
        return tableViewListeners;
    }

    public ListenerList<TableViewColumnListener> getTableViewColumnListeners() {
        return tableViewColumnListeners;
    }

    public ListenerList<TableViewRowListener> getTableViewRowListeners() {
        return tableViewRowListeners;
    }

    public ListenerList<TableViewSelectionListener> getTableViewSelectionListeners() {
        return tableViewSelectionListeners;
    }

    public ListenerList<TableViewSortListener> getTableViewSortListeners() {
        return tableViewSortListeners;
    }

    public ListenerList<TableViewBindingListener> getTableViewBindingListeners() {
        return tableViewBindingListeners;
    }
}
