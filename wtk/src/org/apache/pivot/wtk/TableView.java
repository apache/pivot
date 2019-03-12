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

import org.apache.pivot.annotations.UnsupportedOperation;
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
import org.apache.pivot.util.Utils;
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
            this(null, null, null, DEFAULT_WIDTH, false);
        }

        /**
         * Creates a new column with no header data and a fixed default width.
         *
         * @param name The column name.
         */
        public Column(final String name) {
            this(null, name, null, DEFAULT_WIDTH, false);
        }

        /**
         * Creates a new column with a fixed default width.
         *
         * @param name The column name.
         * @param headerData The column header data.
         */
        public Column(final String name, final Object headerData) {
            this(null, name, headerData, DEFAULT_WIDTH, false);
        }

        /**
         * Creates a new column with a fixed width.
         *
         * @param name The column name.
         * @param headerData The column header data.
         * @param width The width of the column.
         */
        public Column(final String name, final Object headerData, final int width) {
            this(null, name, headerData, width, false);
        }

        /**
         * Creates a new column.
         *
         * @param name The column name.
         * @param headerData The column header data.
         * @param width The width of the column.
         * @param relative If <tt>true</tt>, specifies a relative column width;
         * otherwise, specifies a fixed column width.
         */
        public Column(final String name, final Object headerData, final int width, final boolean relative) {
            this(null, name, headerData, width, relative);
        }

        public Column(final TableView tableView) {
            this(tableView, null, null, DEFAULT_WIDTH, false);
        }

        public Column(final TableView tableView, final String name) {
            this(tableView, name, null, DEFAULT_WIDTH, false);
        }

        public Column(final TableView tableView, final String name, final Object headerData) {
            this(tableView, name, headerData, DEFAULT_WIDTH, false);
        }

        public Column(final TableView tableView, final String name, final Object headerData, final int width) {
            this(tableView, name, headerData, width, false);
        }

        public Column(final TableView tableView, final String name, final Object headerData, final int width,
            final boolean relative) {
            setName(name);
            setHeaderData(headerData);
            setWidth(width, relative);
            if (tableView != null) {
                tableView.getColumns().add(this);
            }
        }

        /**
         * Returns the table view with which this column is associated.
         *
         * @return The column's table view, or <tt>null</tt> if the column does
         * not currently belong to a table.
         */
        public TableView getTableView() {
            return tableView;
        }

        /**
         * Returns the column name.
         *
         * @return The column name.
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the column name.
         *
         * @param name The column name.
         */
        public void setName(final String name) {
            String previousName = this.name;

            if (previousName != name) {
                this.name = name;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnNameChanged(this, previousName);
                }
            }
        }

        /**
         * Returns the column header data.
         *
         * @return The column header data, or <tt>null</tt> if the column has no
         * header data.
         */
        public Object getHeaderData() {
            return headerData;
        }

        /**
         * Sets the column header data.
         *
         * @param headerData The column header data, or <tt>null</tt> for no
         * header data.
         */
        public void setHeaderData(final Object headerData) {
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
         * @return The column header data renderer.
         */
        public HeaderDataRenderer getHeaderDataRenderer() {
            return headerDataRenderer;
        }

        /**
         * Sets the column header data renderer.
         *
         * @param headerDataRenderer The new renderer for the header data.
         */
        public void setHeaderDataRenderer(final HeaderDataRenderer headerDataRenderer) {
            Utils.checkNull(headerDataRenderer, "Header data renderer");

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
         * @return The width of the column.
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the relative flag.
         *
         * @return <tt>true</tt> if the column width is relative, <tt>false</tt>
         * if it is fixed.
         */
        public boolean isRelative() {
            return relative;
        }

        /**
         * Set the column width.
         *
         * @param width The absolute width of the column.
         */
        public void setWidth(final int width) {
            setWidth(width, false);
        }

        /**
         * Set the column width.
         *
         * @param width The encoded width of the row. If the string ends with
         * the '*' character, it is treated as a relative value. Otherwise, it
         * is considered an absolute value.
         */
        public void setWidth(final String width) {
            boolean relativeLocal = false;

            if (width.endsWith("*")) {
                relativeLocal = true;
                setWidth(Integer.parseInt(width.substring(0, width.length() - 1)), relativeLocal);
            } else {
                setWidth(Integer.parseInt(width), relativeLocal);
            }

        }

        /**
         * Sets the column width.
         *
         * @param width The width of the column.
         * @param relative <tt>true</tt> if the column width is relative,
         * <tt>false</tt> if it is fixed.
         */
        public void setWidth(final int width, final boolean relative) {
            if (width < (relative ? 0 : -1)) {
                throw new IllegalArgumentException("illegal width " + width);
            }

            int previousWidth = this.width;
            boolean previousRelative = this.relative;

            if (previousWidth != width || previousRelative != relative) {
                this.width = width;
                this.relative = relative;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnWidthChanged(this, previousWidth,
                        previousRelative);
                }
            }
        }

        /**
         * @return The minimum and maximum widths to which the column can size.
         */
        public Limits getWidthLimits() {
            return new Limits(minimumWidth, maximumWidth);
        }

        /**
         * Sets the minimum and maximum widths the column can size to.
         *
         * @param minimumWidth Column width cannot be smaller than this size.
         * @param maximumWidth Column width cannot be greater than this size.
         */
        public void setWidthLimits(final int minimumWidth, final int maximumWidth) {
            if (minimumWidth < 0) {
                throw new IllegalArgumentException("Minimum width is negative, " + minimumWidth);
            }

            if (maximumWidth < minimumWidth) {
                throw new IllegalArgumentException("Maximum width is smaller than minimum width, "
                    + maximumWidth + "<" + minimumWidth);
            }

            int previousMinimumWidth = this.minimumWidth;
            int previousMaximumWidth = this.maximumWidth;

            if (minimumWidth != previousMinimumWidth || maximumWidth != previousMaximumWidth) {
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
         * @param widthLimits The new width limits.
         */
        public void setWidthLimits(final Limits widthLimits) {
            setWidthLimits(widthLimits.minimum, widthLimits.maximum);
        }

        /**
         * Gets the minimum width the column is allowed to be.
         *
         * @return Minimum column width.
         */
        public int getMinimumWidth() {
            return minimumWidth;
        }

        /**
         * Sets the minimum width the column is allowed to be.
         *
         * @param minimumWidth Minimum column width.
         */
        public void setMinimumWidth(final int minimumWidth) {
            setWidthLimits(minimumWidth, maximumWidth);
        }

        /**
         * Get the maximum width the column is allowed to be.
         *
         * @return Maximum column width.
         */
        public int getMaximumWidth() {
            return maximumWidth;
        }

        /**
         * Set the maximum width the column is allowed to be.
         *
         * @param maximumWidth Maximum column width.
         */
        public void setMaximumWidth(final int maximumWidth) {
            setWidthLimits(minimumWidth, maximumWidth);
        }

        /**
         * Returns the column's filter.
         *
         * @return The column's filter, or <tt>null</tt> if the column does not
         * have a filter.
         */
        public Object getFilter() {
            return filter;
        }

        /**
         * Sets the column's filter.
         *
         * @param filter The column's filter, or <tt>null</tt> for no filter.
         */
        public void setFilter(final Object filter) {
            Object previousFilter = this.filter;

            if (previousFilter != filter) {
                this.filter = filter;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnFilterChanged(this, previousFilter);
                }
            }
        }

        /**
         * Returns the column's cell renderer.
         *
         * @return The cell renderer that is used to draw the contents of this
         * column.
         */
        public CellRenderer getCellRenderer() {
            return cellRenderer;
        }

        /**
         * Sets the column's cell renderer.
         *
         * @param cellRenderer The cell renderer that is used to draw the
         * contents of this column.
         */
        public void setCellRenderer(final CellRenderer cellRenderer) {
            Utils.checkNull(cellRenderer, "Cell renderer");

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
     * {@link Renderer} interface to customize the appearance of a cell in a
     * TableView.
     */
    public interface CellRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param row The row to render, or <tt>null</tt> if called to calculate
         * preferred height for skins that assume a fixed renderer height.
         * @param rowIndex The index of the row being rendered, or <tt>-1</tt> if
         * <tt>value</tt> is <tt>null</tt>.
         * @param columnIndex The index of the column being rendered.
         * @param tableView The host component.
         * @param columnName The name of the column being rendered.
         * @param selected If <tt>true</tt>, the row is selected.
         * @param highlighted If <tt>true</tt>, the row is highlighted.
         * @param disabled If <tt>true</tt>, the row is disabled.
         */
        public void render(Object row, int rowIndex, int columnIndex, TableView tableView,
            String columnName, boolean selected, boolean highlighted, boolean disabled);

        /**
         * Converts table view cell data to a string representation.
         *
         * @param row The row object.
         * @param columnName The name of the column.
         * @return The cell data's string representation, or <tt>null</tt> if the
         * data does not have a string representation. <p> Note that this method
         * may be called often during keyboard navigation, so implementations
         * should avoid unnecessary string allocations.
         */
        public String toString(Object row, String columnName);
    }

    /**
     * {@link Renderer} interface to customize the appearance of the header of a
     * TableView.
     */
    public interface HeaderDataRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param data The data to render, or <tt>null</tt> if called to
         * calculate preferred height for skins that assume a fixed renderer
         * height.
         * @param columnIndex The index of the column being rendered.
         * @param tableViewHeader The host component.
         * @param columnName The name of the column being rendered.
         * @param highlighted If <tt>true</tt>, the item is highlighted.
         */
        public void render(Object data, int columnIndex, TableViewHeader tableViewHeader,
            String columnName, boolean highlighted);

        /**
         * Converts table view header data to a string representation.
         *
         * @param item The header data item.
         * @return The data's string representation, or <tt>null</tt> if the data
         * does not have a string representation. <p> Note that this method may
         * be called often during keyboard navigation, so implementations should
         * avoid unnecessary string allocations.
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
         * @param tableView The table view being edited.
         * @param rowIndex Index of the row to edit.
         * @param columnIndex Index of the column to edit.
         */
        public void beginEdit(TableView tableView, int rowIndex, int columnIndex);

        /**
         * Terminates an edit operation.
         *
         * @param result <tt>true</tt> to perform the edit; <tt>false</tt> to
         * cancel it.
         */
        public void endEdit(boolean result);

        /**
         * @return Whether an edit is currently in progress.
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
         * @param value The value retrieved from the user object.
         * @return The object converted to list data for the table.
         */
        public List<?> toTableData(Object value);

        /**
         * Converts table data to a context value.
         *
         * @param tableData The current table list data.
         * @return The list converted to a form suitable for storage
         * in the user object.
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
         * @param tableData The source table data.
         * @param value The value to locate.
         * @return The index of first occurrence of the value if it exists in the
         * list; <tt>-1</tt>, otherwise.
         */
        public int indexOf(List<?> tableData, Object value);

        /**
         * Retrieves the value at the given index.
         *
         * @param tableData The source table data.
         * @param index The index of the value to retrieve.
         * @return The object value at that index.
         */
        public Object get(List<?> tableData, int index);
    }

    /**
     * Column sequence implementation.
     */
    public final class ColumnSequence implements Sequence<Column>, Iterable<Column> {
        @Override
        public int add(final Column column) {
            int index = getLength();
            insert(column, index);

            return index;
        }

        @Override
        public void insert(final Column column, final int index) {
            Utils.checkNull(column, "Column");

            if (column.tableView != null) {
                throw new IllegalArgumentException(
                    "Column is already in use by another table view.");
            }

            columns.insert(column, index);
            column.tableView = TableView.this;

            tableViewColumnListeners.columnInserted(TableView.this, index);
        }

        @Override
        @UnsupportedOperation
        public Column update(final int index, final Column column) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(final Column column) {
            int index = indexOf(column);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Column> remove(final int index, final int count) {
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
        public Column get(final int index) {
            return columns.get(index);
        }

        @Override
        public int indexOf(final Column column) {
            return columns.indexOf(column);
        }

        @Override
        public int getLength() {
            return columns.getLength();
        }

        @Override
        public Iterator<Column> iterator() {
            return new ImmutableIterator<>(columns.iterator());
        }
    }

    /**
     * Sort dictionary implementation.
     */
    public final class SortDictionary implements Dictionary<String, SortDirection>,
        Iterable<String> {
        @Override
        public SortDirection get(final String columnName) {
            return sortMap.get(columnName);
        }

        @Override
        public SortDirection put(final String columnName, final SortDirection sortDirection) {
            SortDirection previousSortDirection;

            if (sortDirection == null) {
                previousSortDirection = remove(columnName);
            } else {
                boolean update = containsKey(columnName);
                previousSortDirection = sortMap.put(columnName, sortDirection);

                if (update) {
                    tableViewSortListeners.sortUpdated(TableView.this, columnName,
                        previousSortDirection);
                } else {
                    sortList.add(columnName);
                    tableViewSortListeners.sortAdded(TableView.this, columnName);
                }
            }

            return previousSortDirection;
        }

        @Override
        public SortDirection remove(final String columnName) {
            SortDirection sortDirection = null;

            if (containsKey(columnName)) {
                sortDirection = sortMap.remove(columnName);
                sortList.remove(columnName);
                tableViewSortListeners.sortRemoved(TableView.this, columnName, sortDirection);
            }

            return sortDirection;
        }

        @Override
        public boolean containsKey(final String columnName) {
            return sortMap.containsKey(columnName);
        }

        public boolean isEmpty() {
            return sortMap.isEmpty();
        }

        public Dictionary.Pair<String, SortDirection> get(final int index) {
            String columnName = sortList.get(index);
            SortDirection sortDirection = sortMap.get(columnName);

            return new Dictionary.Pair<>(columnName, sortDirection);
        }

        public int getLength() {
            return sortList.getLength();
        }

        @Override
        public Iterator<String> iterator() {
            return sortList.iterator();
        }
    }

    private ArrayList<Column> columns = new ArrayList<>();
    private ColumnSequence columnSequence = new ColumnSequence();

    private List<?> tableData = null;
    private TableView columnSource = null;

    private RowEditor rowEditor = null;

    private RangeSelection rangeSelection = new RangeSelection();
    private SelectMode selectMode = SelectMode.SINGLE;

    private HashMap<String, SortDirection> sortMap = new HashMap<>();
    private ArrayList<String> sortList = new ArrayList<>();
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
        public void itemInserted(final List<Object> list, final int index) {
            // Increment selected ranges
            int updated = rangeSelection.insertIndex(index);

            // Notify listeners that items were inserted
            tableViewRowListeners.rowInserted(TableView.this, index);

            if (updated > 0) {
                tableViewSelectionListeners.selectedRangesChanged(TableView.this,
                    getSelectedRanges());
            }
        }

        @Override
        public void itemsRemoved(final List<Object> list, final int index, final Sequence<Object> items) {
            int count = items.getLength();

            int previousSelectedIndex;
            if (selectMode == SelectMode.SINGLE && rangeSelection.getLength() > 0) {
                previousSelectedIndex = rangeSelection.get(0).start;
            } else {
                previousSelectedIndex = -1;
            }

            // Decrement selected ranges
            int updated = rangeSelection.removeIndexes(index, count);

            // Notify listeners that items were removed
            tableViewRowListeners.rowsRemoved(TableView.this, index, count);

            if (updated > 0) {
                tableViewSelectionListeners.selectedRangesChanged(TableView.this,
                    getSelectedRanges());

                if (selectMode == SelectMode.SINGLE && getSelectedIndex() != previousSelectedIndex) {
                    tableViewSelectionListeners.selectedRowChanged(TableView.this, null);
                }
            }
        }

        @Override
        public void itemUpdated(final List<Object> list, final int index, final Object previousItem) {
            tableViewRowListeners.rowUpdated(TableView.this, index);
        }

        @Override
        public void listCleared(final List<Object> list) {
            int cleared = rangeSelection.getLength();
            rangeSelection.clear();

            tableViewRowListeners.rowsCleared(TableView.this);

            if (cleared > 0) {
                tableViewSelectionListeners.selectedRangesChanged(TableView.this,
                    getSelectedRanges());

                if (selectMode == SelectMode.SINGLE) {
                    tableViewSelectionListeners.selectedRowChanged(TableView.this, null);
                }
            }
        }

        @Override
        public void comparatorChanged(final List<Object> list, final Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int cleared = rangeSelection.getLength();
                rangeSelection.clear();
                tableViewRowListeners.rowsSorted(TableView.this);

                if (cleared > 0) {
                    tableViewSelectionListeners.selectedRangesChanged(TableView.this,
                        getSelectedRanges());

                    if (selectMode == SelectMode.SINGLE) {
                        tableViewSelectionListeners.selectedRowChanged(TableView.this, null);
                    }
                }
            }
        }
    };

    private TableViewListener.Listeners tableViewListeners = new TableViewListener.Listeners();
    private TableViewColumnListener.Listeners tableViewColumnListeners = new TableViewColumnListener.Listeners();
    private TableViewRowListener.Listeners tableViewRowListeners = new TableViewRowListener.Listeners();
    private TableViewSelectionListener.Listeners tableViewSelectionListeners =
        new TableViewSelectionListener.Listeners();
    private TableViewSortListener.Listeners tableViewSortListeners = new TableViewSortListener.Listeners();
    private TableViewBindingListener.Listeners tableViewBindingListeners = new TableViewBindingListener.Listeners();

    public static final String COLUMN_NAME_KEY = "columnName";
    public static final String SORT_DIRECTION_KEY = "sortDirection";

    /**
     * Creates a new table view populated with an empty array list.
     */
    public TableView() {
        this(new ArrayList<>());
    }

    /**
     * Creates a new table view populated with the given table data.
     *
     * @param tableData The initial data for this table view.
     */
    public TableView(final List<?> tableData) {
        setTableData(tableData);
        installSkin(TableView.class);
    }

    @Override
    protected void setSkin(final org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, TableView.Skin.class);

        super.setSkin(skin);
    }

    /**
     * Returns the table column sequence.
     *
     * @return The table column sequence.
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
     * @return The data currently presented by the table view.
     */
    public List<?> getTableData() {
        return this.tableData;
    }

    /**
     * Sets the table data.
     *
     * @param tableData The data to be presented by the table view.
     */
    @SuppressWarnings("unchecked")
    public void setTableData(final List<?> tableData) {
        Utils.checkNull(tableData, "Table data");

        List<?> previousTableData = this.tableData;

        if (previousTableData != tableData) {
            int cleared;
            if (previousTableData != null) {
                // Clear any existing selection
                cleared = rangeSelection.getLength();
                rangeSelection.clear();

                ((List<Object>) previousTableData).getListListeners().remove(tableDataListener);
            } else {
                cleared = 0;
            }

            ((List<Object>) tableData).getListListeners().add(tableDataListener);

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
     * @param tableData A JSON string (must begin with <tt>[</tt> and end with
     * <tt>]</tt>, denoting a list) which will be the data to be presented by the table view.
     */
    public final void setTableData(final String tableData) {
        Utils.checkNull(tableData, "Table data");

        try {
            setTableData(JSONSerializer.parseList(tableData));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    /**
     * Sets the table data.
     *
     * @param tableData A URL referring to a JSON file containing the data to be
     * presented by the table view.
     */
    public void setTableData(final URL tableData) {
        Utils.checkNull(tableData, "URL for table data");

        JSONSerializer jsonSerializer = new JSONSerializer();

        try {
            setTableData((List<?>) jsonSerializer.readObject(tableData.openStream()));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        } catch (IOException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public TableView getColumnSource() {
        return columnSource;
    }

    public void setColumnSource(final TableView columnSource) {
        TableView previousColumnSource = this.columnSource;

        if (previousColumnSource != columnSource) {
            this.columnSource = columnSource;
            tableViewListeners.columnSourceChanged(this, previousColumnSource);
        }
    }

    /**
     * Returns the editor used to edit rows in this table.
     *
     * @return The row editor, or <tt>null</tt> if no editor is installed.
     */
    public RowEditor getRowEditor() {
        return rowEditor;
    }

    /**
     * Sets the editor used to edit rows in this table.
     *
     * @param rowEditor The row editor for the list.
     */
    public void setRowEditor(final RowEditor rowEditor) {
        RowEditor previousRowEditor = this.rowEditor;

        if (previousRowEditor != rowEditor) {
            this.rowEditor = rowEditor;
            tableViewListeners.rowEditorChanged(this, previousRowEditor);
        }
    }

    /**
     * Returns the currently selected index, even when in multi-select mode.
     *
     * @return The currently selected index.
     */
    public int getSelectedIndex() {
        return getFirstSelectedIndex();
    }

    /**
     * Sets the selection to a single index.
     *
     * @param index The index to select, or <tt>-1</tt> to clear the selection.
     */
    public void setSelectedIndex(final int index) {
        if (index == -1) {
            clearSelection();
        } else {
            int tableDataLength = tableData.getLength();
            if (tableDataLength > 0 && index < tableDataLength) {
                setSelectedRange(index, index);
            }
        }
    }

    /**
     * Sets the selection to a single range.
     *
     * @param start The start of the selection range.
     * @param end The end of the range.
     */
    public void setSelectedRange(final int start, final int end) {
        ArrayList<Span> selectedRanges = new ArrayList<>();
        selectedRanges.add(new Span(start, end));

        setSelectedRanges(selectedRanges);
    }

    /**
     * Returns the currently selected ranges.
     *
     * @return An immutable list containing the currently selected ranges. Note
     * that the returned list is a wrapper around the actual selection, not a
     * copy. Any changes made to the selection state will be reflected in the
     * list, but events will not be fired.
     */
    public ImmutableList<Span> getSelectedRanges() {
        return rangeSelection.getSelectedRanges();
    }

    /**
     * Sets the selection to the given range sequence. Any overlapping or
     * connecting ranges will be consolidated, and the resulting selection will
     * be sorted in ascending order.
     *
     * @param selectedRanges The new sequence of selected ranges.
     * @return The ranges that were actually set.
     */
    public Sequence<Span> setSelectedRanges(final Sequence<Span> selectedRanges) {
        Utils.checkNull(selectedRanges, "Selected ranges");

        // When we're in mode NONE, the only thing we can do is to clear the
        // selection
        if (selectMode == SelectMode.NONE && selectedRanges.getLength() > 0) {
            throw new IllegalArgumentException("Selection is not enabled.");
        }

        // Update the selection
        Sequence<Span> previousSelectedRanges = this.rangeSelection.getSelectedRanges();
        Object previousSelectedRow = (selectMode == SelectMode.SINGLE) ? getSelectedRow() : null;

        RangeSelection listSelection = new RangeSelection();
        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span range = selectedRanges.get(i);

            Utils.checkNull(range, "Selected range");

            if (range.start < 0) {
                throw new IndexOutOfBoundsException("range.start < 0, " + range.start);
            }
            if (range.end >= tableData.getLength()) {
                throw new IndexOutOfBoundsException("range.end >= tableData length, " + range.end
                    + " >= " + tableData.getLength());
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
     * @param selectedRanges A JSON-formatted string containing the ranges to
     * select.
     * @return The ranges that were actually set.
     * @see #setSelectedRanges(Sequence)
     */
    public final Sequence<Span> setSelectedRanges(final String selectedRanges) {
        Utils.checkNull(selectedRanges, "Selected ranges");

        try {
            setSelectedRanges(parseSelectedRanges(selectedRanges));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return getSelectedRanges();
    }

    @SuppressWarnings("unchecked")
    private static Sequence<Span> parseSelectedRanges(final String json) throws SerializationException {
        ArrayList<Span> selectedRanges = new ArrayList<>();

        List<?> list = JSONSerializer.parseList(json);
        for (Object item : list) {
            Map<String, ?> map = (Map<String, ?>) item;
            selectedRanges.add(new Span(map));
        }

        return selectedRanges;
    }

    /**
     * Returns the first selected index.
     *
     * @return The first selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getFirstSelectedIndex() {
        return (rangeSelection.getLength() > 0) ? rangeSelection.get(0).start : -1;
    }

    /**
     * Returns the last selected index.
     *
     * @return The last selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getLastSelectedIndex() {
        return (rangeSelection.getLength() > 0) ? rangeSelection.get(rangeSelection.getLength() - 1).end
            : -1;
    }

    /**
     * Adds a single index to the selection.
     *
     * @param index The index to add.
     * @return <tt>true</tt> if the index was added to the selection;
     * <tt>false</tt>, otherwise.
     */
    public boolean addSelectedIndex(final int index) {
        Sequence<Span> addedRanges = addSelectedRange(index, index);
        return (addedRanges.getLength() > 0);
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param start The first index in the range.
     * @param end The last index in the range.
     * @return The ranges that were added to the selection.
     */
    public Sequence<Span> addSelectedRange(final int start, final int end) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Table view is not in multi-select mode.");
        }

        if (start < 0) {
            throw new IndexOutOfBoundsException("start < 0, " + start);
        }
        if (end >= tableData.getLength()) {
            throw new IndexOutOfBoundsException("end >= tableData.getLength(), " + end + " >= "
                + tableData.getLength());
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
     * @param range The range to add.
     * @return The ranges that were added to the selection.
     */
    public Sequence<Span> addSelectedRange(final Span range) {
        Utils.checkNull(range, "Range");

        return addSelectedRange(range.start, range.end);
    }

    /**
     * Removes a single index from the selection.
     *
     * @param index The index to remove.
     * @return <tt>true</tt> if the index was removed from the selection;
     * <tt>false</tt>, otherwise.
     */
    public boolean removeSelectedIndex(final int index) {
        Sequence<Span> removedRanges = removeSelectedRange(index, index);
        return (removedRanges.getLength() > 0);
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param start The start of the range to remove.
     * @param end The end of the range to remove.
     * @return The ranges that were removed from the selection.
     */
    public Sequence<Span> removeSelectedRange(final int start, final int end) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Table view is not in multi-select mode.");
        }

        if (start < 0) {
            throw new IndexOutOfBoundsException("start < 0, " + start);
        }
        if (end >= tableData.getLength()) {
            throw new IndexOutOfBoundsException("end >= tableData.getLength(), " + end + " >= "
                + tableData.getLength());
        }

        Sequence<Span> removedRanges = rangeSelection.removeRange(start, end);

        int n = removedRanges.getLength();
        for (int i = 0; i < n; i++) {
            Span removedRange = removedRanges.get(i);
            tableViewSelectionListeners.selectedRangeRemoved(this, removedRange.start,
                removedRange.end);
        }

        if (n > 0) {
            tableViewSelectionListeners.selectedRangesChanged(this, null);
        }

        return removedRanges;
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param range The range to remove.
     * @return The ranges that were removed from the selection.
     */
    public Sequence<Span> removeSelectedRange(final Span range) {
        Utils.checkNull(range, "Range");

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
     * @param index The index whose selection state is to be tested.
     * @return <tt>true</tt> if the index is selected; <tt>false</tt>, otherwise.
     */
    public boolean isRowSelected(final int index) {
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
    public void setSelectedRow(final Object row) {
        setSelectedIndex((row == null) ? -1 : ((List<Object>) tableData).indexOf(row));
    }

    public Sequence<?> getSelectedRows() {
        ArrayList<Object> rows = new ArrayList<>();

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
    public void setSelectedRows(final Sequence<Object> rows) {
        Utils.checkNull(rows, "Selected rows");

        ArrayList<Span> selectedRanges = new ArrayList<>();

        for (int i = 0, n = rows.getLength(); i < n; i++) {
            Object row = rows.get(i);
            Utils.checkNull(row, "Selected row");

            int index = ((List<Object>) tableData).indexOf(row);
            if (index == -1) {
                throw new IllegalArgumentException("\"" + row + "\" is not a valid selection.");
            }

            selectedRanges.add(new Span(index));
        }

        setSelectedRanges(selectedRanges);
    }

    /**
     * @return The current selection mode.
     */
    public SelectMode getSelectMode() {
        return selectMode;
    }

    /**
     * Sets the selection mode. Clears the selection if the mode has changed.
     *
     * @param selectMode The new selection mode.
     */
    public void setSelectMode(final SelectMode selectMode) {
        Utils.checkNull(selectMode, "Select mode");

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
     * @return The table view's sort dictionary.
     */
    public SortDictionary getSort() {
        return sortDictionary;
    }

    /**
     * Sets the table view's sort.
     *
     * @param columnName The column name to sort on.
     * @param sortDirection Whether ascending or descending sort on that column.
     * @return The new sort criteria.
     */
    @SuppressWarnings("unchecked")
    public Dictionary<String, SortDirection> setSort(final String columnName, final SortDirection sortDirection) {
        Dictionary.Pair<String, SortDirection> sort = new Dictionary.Pair<>(columnName, sortDirection);

        setSort(new ArrayList<>(sort));

        return getSort();
    }

    /**
     * Sets the table view's sort.
     *
     * @param sort A sequence of key/value pairs representing the sort. Keys
     * represent column names and values represent sort direction.
     * @return The new sort criteria.
     * @throws IllegalArgumentException if the sort parameter is {@code null}.
     */
    public Dictionary<String, SortDirection> setSort(
        final Sequence<Dictionary.Pair<String, SortDirection>> sort) {
        Utils.checkNull(sort, "Sort");

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
     * @param sort A JSON list containing JSON objects representing the sort.
     * @return The new sort criteria.
     * @see #setSort(Sequence)
     * @throws IllegalArgumentException if the sort parameter is {@code null}
     * or can't be parsed from the JSON input.
     */
    public final Dictionary<String, SortDirection> setSort(final String sort) {
        Utils.checkNull(sort, "Sort");

        try {
            setSort(parseSort(sort));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        return getSort();
    }

    @SuppressWarnings("unchecked")
    private static Sequence<Dictionary.Pair<String, SortDirection>> parseSort(final String json)
        throws SerializationException {
        ArrayList<Dictionary.Pair<String, SortDirection>> sort = new ArrayList<>();

        List<?> list = JSONSerializer.parseList(json);
        for (Object item : list) {
            Map<String, ?> map = (Map<String, ?>) item;

            Dictionary.Pair<String, SortDirection> pair = new Dictionary.Pair<>(
                (String) map.get(COLUMN_NAME_KEY),
                SortDirection.valueOf(((String) map.get(SORT_DIRECTION_KEY)).toUpperCase(Locale.ENGLISH)));
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
     * @param index The index of the row whose disabled state is to be tested.
     * @return <tt>true</tt> if the row is disabled; <tt>false</tt>, otherwise.
     */
    @SuppressWarnings("unchecked")
    public boolean isRowDisabled(final int index) {
        boolean disabled = false;

        if (disabledRowFilter != null) {
            Object row = tableData.get(index);
            disabled = ((Filter<Object>) disabledRowFilter).include(row);
        }

        return disabled;
    }

    /**
     * Returns the disabled row filter.
     *
     * @return The disabled row filter, or <tt>null</tt> if no disabled row
     * filter is set.
     */
    public Filter<?> getDisabledRowFilter() {
        return disabledRowFilter;
    }

    /**
     * Sets the disabled row filter.
     *
     * @param disabledRowFilter The disabled row filter, or <tt>null</tt> for no
     * disabled row filter.
     */
    public void setDisabledRowFilter(final Filter<?> disabledRowFilter) {
        Filter<?> previousDisabledRowFilter = this.disabledRowFilter;

        if (previousDisabledRowFilter != disabledRowFilter) {
            this.disabledRowFilter = disabledRowFilter;
            tableViewListeners.disabledRowFilterChanged(this, previousDisabledRowFilter);
        }
    }

    public String getTableDataKey() {
        return tableDataKey;
    }

    public void setTableDataKey(final String tableDataKey) {
        String previousTableDataKey = this.tableDataKey;

        if (previousTableDataKey != tableDataKey) {
            this.tableDataKey = tableDataKey;
            tableViewBindingListeners.tableDataKeyChanged(this, previousTableDataKey);
        }
    }

    public BindType getTableDataBindType() {
        return tableDataBindType;
    }

    public void setTableDataBindType(final BindType tableDataBindType) {
        Utils.checkNull(tableDataBindType, "Table data bind type");

        BindType previousTableDataBindType = this.tableDataBindType;

        if (previousTableDataBindType != tableDataBindType) {
            this.tableDataBindType = tableDataBindType;
            tableViewBindingListeners.tableDataBindTypeChanged(this, previousTableDataBindType);
        }
    }

    public TableDataBindMapping getTableDataBindMapping() {
        return tableDataBindMapping;
    }

    public void setTableDataBindMapping(final TableDataBindMapping tableDataBindMapping) {
        TableDataBindMapping previousTableDataBindMapping = this.tableDataBindMapping;

        if (previousTableDataBindMapping != tableDataBindMapping) {
            this.tableDataBindMapping = tableDataBindMapping;
            tableViewBindingListeners.tableDataBindMappingChanged(this,
                previousTableDataBindMapping);
        }
    }

    public String getSelectedRowKey() {
        return selectedRowKey;
    }

    public void setSelectedRowKey(final String selectedRowKey) {
        String previousSelectedRowKey = this.selectedRowKey;

        if (previousSelectedRowKey != selectedRowKey) {
            this.selectedRowKey = selectedRowKey;
            tableViewBindingListeners.selectedRowKeyChanged(this, previousSelectedRowKey);
        }
    }

    public BindType getSelectedRowBindType() {
        return selectedRowBindType;
    }

    public void setSelectedRowBindType(final BindType selectedRowBindType) {
        Utils.checkNull(selectedRowBindType, "Selected row bind type");

        BindType previousSelectedRowBindType = this.selectedRowBindType;
        if (previousSelectedRowBindType != selectedRowBindType) {
            this.selectedRowBindType = selectedRowBindType;
            tableViewBindingListeners.selectedRowBindTypeChanged(this, previousSelectedRowBindType);
        }
    }

    public SelectedRowBindMapping getSelectedRowBindMapping() {
        return selectedRowBindMapping;
    }

    public void setSelectedRowBindMapping(final SelectedRowBindMapping selectedRowBindMapping) {
        SelectedRowBindMapping previousSelectedRowBindMapping = this.selectedRowBindMapping;

        if (previousSelectedRowBindMapping != selectedRowBindMapping) {
            this.selectedRowBindMapping = selectedRowBindMapping;
            tableViewBindingListeners.selectedRowBindMappingChanged(this,
                previousSelectedRowBindMapping);
        }
    }

    public String getSelectedRowsKey() {
        return selectedRowsKey;
    }

    public void setSelectedRowsKey(final String selectedRowsKey) {
        String previousSelectedRowsKey = this.selectedRowsKey;

        if (previousSelectedRowsKey != selectedRowsKey) {
            this.selectedRowsKey = selectedRowsKey;
            tableViewBindingListeners.selectedRowsKeyChanged(this, previousSelectedRowsKey);
        }
    }

    public BindType getSelectedRowsBindType() {
        return selectedRowsBindType;
    }

    public void setSelectedRowsBindType(final BindType selectedRowsBindType) {
        Utils.checkNull(selectedRowsBindType, "Selected rows bind type");

        BindType previousSelectedRowsBindType = this.selectedRowsBindType;
        if (previousSelectedRowsBindType != selectedRowsBindType) {
            this.selectedRowsBindType = selectedRowsBindType;
            tableViewBindingListeners.selectedRowsBindTypeChanged(this,
                previousSelectedRowsBindType);
        }
    }

    public SelectedRowBindMapping getSelectedRowsBindMapping() {
        return selectedRowsBindMapping;
    }

    public void setSelectedRowsBindMapping(final SelectedRowBindMapping selectedRowsBindMapping) {
        SelectedRowBindMapping previousSelectedRowsBindMapping = this.selectedRowsBindMapping;

        if (previousSelectedRowsBindMapping != selectedRowsBindMapping) {
            this.selectedRowsBindMapping = selectedRowsBindMapping;
            tableViewBindingListeners.selectedRowsBindMappingChanged(this,
                previousSelectedRowsBindMapping);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void load(final Object context) {
        // Bind to list data
        if (tableDataKey != null && tableDataBindType != BindType.STORE
            && JSON.containsKey(context, tableDataKey)) {
            Object value = JSON.get(context, tableDataKey);

            List<?> tableDataLocal;
            if (tableDataBindMapping == null) {
                tableDataLocal = (List<?>) value;
            } else {
                tableDataLocal = tableDataBindMapping.toTableData(value);
            }

            setTableData(tableDataLocal);
        }

        switch (selectMode) {
            case SINGLE:
                // Bind using selected row key
                if (selectedRowKey != null && selectedRowBindType != BindType.STORE
                    && JSON.containsKey(context, selectedRowKey)) {
                    Object row = JSON.get(context, selectedRowKey);

                    int index;
                    if (selectedRowBindMapping == null) {
                        index = ((List<Object>) tableData).indexOf(row);
                    } else {
                        index = selectedRowBindMapping.indexOf(tableData, row);
                    }

                    setSelectedIndex(index);
                }

                break;

            case MULTI:
                // Bind using selected rows key
                if (selectedRowsKey != null && selectedRowsBindType != BindType.STORE
                    && JSON.containsKey(context, selectedRowsKey)) {
                    Sequence<Object> rows = (Sequence<Object>) JSON.get(context, selectedRowsKey);

                    clearSelection();

                    for (int i = 0, n = rows.getLength(); i < n; i++) {
                        Object row = rows.get(i);

                        int index;
                        if (selectedRowsBindMapping == null) {
                            index = ((List<Object>) tableData).indexOf(row);
                        } else {
                            index = selectedRowsBindMapping.indexOf(tableData, row);
                        }

                        if (index != -1) {
                            addSelectedIndex(index);
                        }
                    }
                }

                break;

            case NONE:
                break;

            default:
                break;
        }
    }

    @Override
    public void store(final Object context) {
        // Bind to table data
        if (tableDataKey != null && tableDataBindType != BindType.LOAD) {
            Object value;
            if (tableDataBindMapping == null) {
                value = tableData;
            } else {
                value = tableDataBindMapping.valueOf(tableData);
            }

            JSON.put(context, tableDataKey, value);
        }

        switch (selectMode) {
            case SINGLE:
                // Bind using selected row key
                if (selectedRowKey != null && selectedRowBindType != BindType.LOAD) {
                    Object row;

                    int selectedIndexLocal = getSelectedIndex();
                    if (selectedRowBindMapping == null) {
                        if (selectedIndexLocal == -1) {
                            row = null;
                        } else {
                            row = tableData.get(selectedIndexLocal);
                        }
                    } else {
                        row = selectedRowBindMapping.get(tableData, selectedIndexLocal);
                    }

                    JSON.put(context, selectedRowKey, row);
                }

                break;

            case MULTI:
                // Bind using selected rows key
                if (selectedRowsKey != null && selectedRowsBindType != BindType.LOAD) {
                    ArrayList<Object> rows = new ArrayList<>();

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

            case NONE:
                break;

            default:
                break;
        }
    }

    @Override
    public void clear() {
        if (tableDataKey != null) {
            setTableData(new ArrayList<>());
        }

        if (selectedRowKey != null || selectedRowsKey != null) {
            setSelectedRow(null);
        }
    }

    /**
     * Returns the index of the row at a given location.
     *
     * @param y The y-coordinate of the row to identify.
     * @return The row index, or <tt>-1</tt> if there is no row at the given
     * y-coordinate.
     */
    public int getRowAt(final int y) {
        TableView.Skin tableViewSkin = (TableView.Skin) getSkin();
        return tableViewSkin.getRowAt(y);
    }

    /**
     * Returns the index of the column at a given location.
     *
     * @param x The x-coordinate of the column to identify.
     * @return The column index, or <tt>-1</tt> if there is no column at the
     * given x-coordinate.
     */
    public int getColumnAt(final int x) {
        TableView.Skin tableViewSkin = (TableView.Skin) getSkin();
        return tableViewSkin.getColumnAt(x);
    }

    /**
     * Returns the bounding area of a given row.
     *
     * @param rowIndex The row index.
     * @return The bounding area of the row.
     */
    public Bounds getRowBounds(final int rowIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin) getSkin();
        return tableViewSkin.getRowBounds(rowIndex);
    }

    /**
     * Returns the bounding area of a given column.
     *
     * @param columnIndex The column index.
     * @return The bounding area of the column.
     */
    public Bounds getColumnBounds(final int columnIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin) getSkin();
        return tableViewSkin.getColumnBounds(columnIndex);
    }

    /**
     * Returns the bounding area of a given cell.
     *
     * @param rowIndex The row index of the cell.
     * @param columnIndex The column index of the cell.
     * @return The bounding area of the cell.
     */
    public Bounds getCellBounds(final int rowIndex, final int columnIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin) getSkin();
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
