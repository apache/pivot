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

import java.util.Comparator;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.TableViewCellRenderer;

/**
 * Component that displays a sequence of items partitioned into columns,
 * optionally allowing a user to select one or more rows.
 *
 * @author gbrown
 */
public class TableView extends Component {
    /**
     * Contains information about a table column.
     *
     * @author gbrown
     */
    public static final class Column {
        private TableView tableView = null;

        private String name = null;
        private Object headerData = null;
        private int width = 0;
        private boolean relative = false;
        private SortDirection sortDirection = null;
        private Object filter = null;
        private CellRenderer cellRenderer = DEFAULT_CELL_RENDERER;

        private static final CellRenderer DEFAULT_CELL_RENDERER = new TableViewCellRenderer();

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
            if (width < 0) {
                throw new IllegalArgumentException("width is negative.");
            }

            this.name = name;
            this.headerData = headerData;
            this.width = width;
            this.relative = relative;
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
         * Sets the table view with which this column is associated.
         *
         * @param tableView
         * The column's table view, or <tt>null</tt> if the column does not
         * currently belong to a table.
         */
        private void setTableView(TableView tableView) {
            this.tableView = tableView;
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
            if (name == null) {
                throw new IllegalArgumentException("name is null.");
            }

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
            boolean relative = false;

            if (width.endsWith("*")) {
                relative = true;
                width = width.substring(0, width.length() - 1);
            }

            setWidth(Integer.parseInt(width), relative);
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
         * Returns the column's sort direction.
         *
         * @return
         * The column's sort direction, or <tt>null</tt> if the column is not
         * sorted.
         */
        public SortDirection getSortDirection() {
            return sortDirection;
        }

        /**
         * Sets the column's sort direction.
         *
         * @param sortDirection
         * The column's sort direction, or <tt>null</tt> to specify no
         * sort direction
         */
        public void setSortDirection(SortDirection sortDirection) {
            SortDirection previousSortDirection = this.sortDirection;

            if (previousSortDirection != sortDirection) {
                this.sortDirection = sortDirection;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnSortDirectionChanged(this,
                        previousSortDirection);
                }
            }
        }

        /**
         * Sets the column's sort direction.
         *
         * @param sortDirection
         * The column's sort direction, or <tt>null</tt> to specify no
         * sort direction
         */
        public final void setSortDirection(String sortDirection) {
            if (sortDirection == null) {
                setSortDirection((SortDirection)null);
            } else {
                setSortDirection(SortDirection.valueOf(sortDirection.toUpperCase()));
            }
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
     * Table cell renderer interface.
     *
     * @author gbrown
     */
    public interface CellRenderer extends Renderer {
        public void render(Object value, TableView tableView, TableView.Column column,
            boolean rowSelected, boolean rowHighlighted, boolean rowDisabled);
    }

    /**
     * Table row editor interface.
     *
     * gbrown
     */
    public interface RowEditor extends Editor {
        /**
         * Notifies the editor that editing should begin. If the editor is
         * currently installed on the table view, the skin may choose to call
         * this method when the user executes the appropriate gesture (as
         * defined by the skin).
         *
         * @param tableView
         * The table view
         *
         * @param rowIndex
         * The row index of the cell to edit
         *
         * @param columnIndex
         * The column index of the cell to edit
         *
         * @see
         * #setRowEditor(RowEditor)
         */
        public void edit(TableView tableView, int rowIndex, int columnIndex);
    }

    /**
     * Table view skin interface. Table view skins must implement this.
     *
     * @author gbrown
     */
    public interface Skin {
        public int getRowAt(int y);
        public int getColumnAt(int x);
        public Bounds getRowBounds(int rowIndex);
        public Bounds getColumnBounds(int columnIndex);
        public Bounds getCellBounds(int rowIndex, int columnIndex);
    }

    /**
     * Compares two rows. The dictionary values must implement
     * {@link Comparable}.
     * <p>
     * TODO Allow a caller to sort on multiple columns.
     */
    public static final class RowComparator implements Comparator<Object> {
        private String columnName = null;
        private SortDirection sortDirection = null;

        public RowComparator(String columnName, SortDirection sortDirection) {
            this.columnName = columnName;
            this.sortDirection = sortDirection;
        }

        @SuppressWarnings("unchecked")
        public int compare(Object o1, Object o2) {
            Dictionary<String, ?> row1;
            if (o1 instanceof Dictionary<?, ?>) {
                row1 = (Dictionary<String, ?>)o1;
            } else {
                row1 = new BeanDictionary(o1);
            }

            Dictionary<String, ?> row2;
            if (o2 instanceof Dictionary<?, ?>) {
                row2 = (Dictionary<String, ?>)o2;
            } else {
                row2 = new BeanDictionary(o2);
            }

            Comparable<Object> comparable = (Comparable<Object>)row1.get(columnName);
            Object value = row2.get(columnName);

            int result;
            if (comparable == null
                && value == null) {
                result = 0;
            } else if (comparable == null) {
                result = 1;
            } else if (value == null) {
                result = -1;
            } else {
                result = (comparable.compareTo(value)) * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
            }

            return result;
        }
    }

    /**
     * Default sort handler class. Sorts rows using {@link RowComparator}.
     */
    public static class SortHandler implements TableViewHeaderPressListener {
        @SuppressWarnings("unchecked")
        public void headerPressed(TableViewHeader tableViewHeader, int index) {
            TableView tableView = tableViewHeader.getTableView();
            TableView.ColumnSequence columns = tableView.getColumns();
            TableView.Column column = columns.get(index);

            SortDirection sortDirection = column.getSortDirection();

            if (sortDirection == null
                || sortDirection == SortDirection.DESCENDING) {
                sortDirection = SortDirection.ASCENDING;
            } else {
                sortDirection = SortDirection.DESCENDING;
            }

            List<Object> tableData = (List<Object>)tableView.getTableData();
            tableData.setComparator(new TableView.RowComparator(column.getName(), sortDirection));

            for (int i = 0, n = columns.getLength(); i < n; i++) {
                column = columns.get(i);
                column.setSortDirection(i == index ? sortDirection : null);
            }
        }
    }

    /**
     * Column sequence implementation.
     *
     * @author gbrown
     */
    public final class ColumnSequence implements Sequence<Column> {
        public int add(Column column) {
            int i = getLength();
            insert(column, i);

            return i;
        }

        public void insert(Column column, int index) {
            if (column == null) {
                throw new IllegalArgumentException("column is null.");
            }

            if (column.getTableView() != null) {
                throw new IllegalArgumentException("column is already in use by another table view.");
            }

            columns.insert(column, index);
            column.setTableView(TableView.this);

            tableViewColumnListeners.columnInserted(TableView.this, index);
        }

        public Column update(int index, Column column) {
            throw new UnsupportedOperationException();
        }

        public int remove(Column column) {
            int index = indexOf(column);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Column> remove(int index, int count) {
            Sequence<Column> removed = columns.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    removed.get(i).setTableView(null);
                }

                tableViewColumnListeners.columnsRemoved(TableView.this, index, removed);
            }

            return removed;
        }

        public Column get(int index) {
            return columns.get(index);
        }

        public int indexOf(Column column) {
            return columns.indexOf(column);
        }

        public int getLength() {
            return columns.getLength();
        }
    }

    /**
     * Table view listener list.
     *
     * @author gbrown
     */
    private static class TableViewListenerList extends ListenerList<TableViewListener>
        implements TableViewListener {
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            for (TableViewListener listener : this) {
                listener.tableDataChanged(tableView, previousTableData);
            }
        }

        public void rowEditorChanged(TableView tableView,
            TableView.RowEditor previousRowEditor) {
            for (TableViewListener listener : this) {
                listener.rowEditorChanged(tableView, previousRowEditor);
            }
        }

        public void selectModeChanged(TableView tableView, SelectMode previousSelectMode) {
            for (TableViewListener listener : this) {
                listener.selectModeChanged(tableView, previousSelectMode);
            }
        }

        public void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter) {
            for (TableViewListener listener : this) {
                listener.disabledRowFilterChanged(tableView, previousDisabledRowFilter);
            }
        }
    }

    /**
     * Table view column listener list.
     *
     * @author gbrown
     */
    private static class TableViewColumnListenerList extends ListenerList<TableViewColumnListener>
        implements TableViewColumnListener {
        public void columnInserted(TableView tableView, int index) {
            for (TableViewColumnListener listener : this) {
                listener.columnInserted(tableView, index);
            }
        }

        public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
            for (TableViewColumnListener listener : this) {
                listener.columnsRemoved(tableView, index, columns);
            }
        }

        public void columnNameChanged(Column column, String previousName) {
            for (TableViewColumnListener listener : this) {
                listener.columnNameChanged(column, previousName);
            }
        }

        public void columnHeaderDataChanged(Column column, Object previousHeaderData) {
            for (TableViewColumnListener listener : this) {
                listener.columnHeaderDataChanged(column, previousHeaderData);
            }
        }

        public void columnWidthChanged(Column column, int previousWidth, boolean previousRelative) {
            for (TableViewColumnListener listener : this) {
                listener.columnWidthChanged(column, previousWidth, previousRelative);
            }
        }

        public void columnSortDirectionChanged(Column column, SortDirection previousSortDirection) {
            for (TableViewColumnListener listener : this) {
                listener.columnSortDirectionChanged(column, previousSortDirection);
            }
        }

        public void columnFilterChanged(Column column, Object previousFilter) {
            for (TableViewColumnListener listener : this) {
                listener.columnFilterChanged(column, previousFilter);
            }
        }

        public void columnCellRendererChanged(Column column, TableView.CellRenderer previousCellRenderer) {
            for (TableViewColumnListener listener : this) {
                listener.columnCellRendererChanged(column, previousCellRenderer);
            }
        }
    }

    /**
     * Table view item listener list.
     *
     * @author gbrown
     */
    private static class TableViewRowListenerList extends ListenerList<TableViewRowListener>
        implements TableViewRowListener {
        public void rowInserted(TableView tableView, int index) {
            for (TableViewRowListener listener : this) {
                listener.rowInserted(tableView, index);
            }
        }

        public void rowsRemoved(TableView tableView, int index, int count) {
            for (TableViewRowListener listener : this) {
                listener.rowsRemoved(tableView, index, count);
            }
        }

        public void rowUpdated(TableView tableView, int index) {
            for (TableViewRowListener listener : this) {
                listener.rowUpdated(tableView, index);
            }
        }

        public void rowsCleared(TableView tableView) {
            for (TableViewRowListener listener : this) {
                listener.rowsCleared(tableView);
            }
        }

        public void rowsSorted(TableView tableView) {
            for (TableViewRowListener listener : this) {
                listener.rowsSorted(tableView);
            }
        }
    }

    /**
     * Table view selection detail listener list.
     *
     * @author gbrown
     */
    private static class TableViewSelectionListenerList extends ListenerList<TableViewSelectionListener>
        implements TableViewSelectionListener {
        public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRangeAdded(tableView, rangeStart, rangeEnd);
            }
        }

        public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRangeRemoved(tableView, rangeStart, rangeEnd);
            }
        }

        public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelection) {
            for (TableViewSelectionListener listener : this) {
                listener.selectedRangesChanged(tableView, previousSelection);
            }
        }
    }

    private ArrayList<Column> columns = new ArrayList<Column>();
    private ColumnSequence columnSequence = new ColumnSequence();

    private List<?> tableData = null;
    private ListListener<Object> tableDataListener = new ListListener<Object>() {
        public void itemInserted(List<Object> list, int index) {
            // Increment selected ranges
            tableSelection.insertIndex(index);

            // Notify listeners that items were inserted
            tableViewRowListeners.rowInserted(TableView.this, index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int count = items.getLength();

            // Decrement selected ranges
            tableSelection.removeIndexes(index, count);

            // Notify listeners that items were removed
            tableViewRowListeners.rowsRemoved(TableView.this, index, count);
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            tableViewRowListeners.rowUpdated(TableView.this, index);
        }

        public void listCleared(List<Object> list) {
            // All items were removed; clear the selection and notify
            // listeners
            tableSelection.clear();

            tableViewRowListeners.rowsCleared(TableView.this);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                tableSelection.clear();

                tableViewRowListeners.rowsSorted(TableView.this);
            }
        }
    };

    private ListSelection tableSelection = new ListSelection();
    private SelectMode selectMode = SelectMode.SINGLE;

    private Filter<?> disabledRowFilter = null;

    private RowEditor rowEditor = null;

    private TableViewListenerList tableViewListeners = new TableViewListenerList();
    private TableViewColumnListenerList tableViewColumnListeners = new TableViewColumnListenerList();
    private TableViewRowListenerList tableViewRowListeners = new TableViewRowListenerList();
    private TableViewSelectionListenerList tableViewSelectionListeners = new TableViewSelectionListenerList();

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
        return columnSequence;
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
     * Sets the table data. Clears any existing selection state.
     *
     * @param tableData
     * The data to be presented by the table.
     */
    @SuppressWarnings("unchecked")
    public void setTableData(List<?> tableData) {
        if (tableData == null) {
            throw new IllegalArgumentException("tableData is null.");
        }

        List<?> previousTableData = this.tableData;

        if (previousTableData != tableData) {
            if (previousTableData != null) {
                // Clear any existing selection
                clearSelection();

                ((List<Object>)previousTableData).getListListeners().remove(tableDataListener);
            }

            ((List<Object>)tableData).getListListeners().add(tableDataListener);

            // Update the list data and fire change event
            this.tableData = tableData;
            tableViewListeners.tableDataChanged(this, previousTableData);
        }
    }

    /**
     * Sets the table data. Clears any existing selection state.
     *
     * @param tableData
     * A JSON string (must begin with <tt>[</tt> and end with <tt>]</tt>)
     * denoting the data to be presented by this table.
     */
    public void setTableData(String tableData) {
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
     * When in single-select mode, returns the currently selected index.
     *
     * @return
     * The currently selected index.
     */
    public int getSelectedIndex() {
        if (selectMode != SelectMode.SINGLE) {
            throw new IllegalStateException("Table view is not in single-select mode.");
        }

        return (tableSelection.getLength() == 0) ? -1 : tableSelection.get(0).start;
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
     * Returns the table's current selection.
     */
    public Sequence<Span> getSelectedRanges() {
        return new ListSelectionSequence(tableSelection);
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

        if (selectMode == SelectMode.NONE) {
            throw new IllegalArgumentException("Selection is not enabled.");
        }

        if (selectMode == SelectMode.SINGLE) {
            int n = selectedRanges.getLength();

            if (n > 1) {
                throw new IllegalArgumentException("Selection length is greater than 1.");
            } else {
                if (n > 0) {
                    Span selectedRange = selectedRanges.get(0);

                    if (selectedRange.getLength() > 1) {
                        throw new IllegalArgumentException("Selected range length is greater than 1.");
                    }
                }
            }
        }

        // Update the selection
        ListSelectionSequence previousSelectedRanges = new ListSelectionSequence(tableSelection);

        ListSelection tableSelection = new ListSelection();
        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span range = selectedRanges.get(i);

            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            if (range.start < 0 || range.end >= tableData.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            tableSelection.addRange(range.start, range.end);
        }

        this.tableSelection = tableSelection;

        // Notify listeners
        tableViewSelectionListeners.selectedRangesChanged(this, previousSelectedRanges);

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
    private Sequence<Span> parseSelectedRanges(String json)
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
        return (tableSelection.getLength() > 0) ? tableSelection.get(0).start : -1;
    }

    /**
     * Returns the last selected index.
     *
     * @return
     * The last selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getLastSelectedIndex() {
        return (tableSelection.getLength() > 0) ?
            tableSelection.get(tableSelection.getLength() - 1).end : -1;
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

        if (start < 0 || end >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        Sequence<Span> addedRanges = tableSelection.addRange(start, end);

        for (int i = 0, n = addedRanges.getLength(); i < n; i++) {
            Span addedRange = addedRanges.get(i);
            tableViewSelectionListeners.selectedRangeAdded(this, addedRange.start, addedRange.end);
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

        if (start < 0 || end >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        Sequence<Span> removedRanges = tableSelection.removeRange(start, end);

        for (int i = 0, n = removedRanges.getLength(); i < n; i++) {
            Span removedRange = removedRanges.get(i);
            tableViewSelectionListeners.selectedRangeRemoved(this, removedRange.start, removedRange.end);
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
        if (tableSelection.getLength() > 0) {
            Sequence<Span> previousSelectedRanges = new ListSelectionSequence(tableSelection);
            tableSelection = new ListSelection();

            tableViewSelectionListeners.selectedRangesChanged(this,
                previousSelectedRanges);
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
        if (index < 0 || index >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        return (tableSelection.containsIndex(index));
    }

    public Object getSelectedRow() {
        int index = getSelectedIndex();
        Object row = null;

        if (index >= 0) {
            row = tableData.get(index);
        }

        return row;
    }

    public Sequence<?> getSelectedRows() {
        ArrayList<Object> rows = new ArrayList<Object>();

        for (int i = 0, n = tableSelection.getLength(); i < n; i++) {
            Span range = tableSelection.get(i);

            for (int index = range.start; index <= range.end; index++) {
                Object row = tableData.get(index);
                rows.add(row);
            }
        }

        return rows;
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

    public void setSelectMode(String selectMode) {
        if (selectMode == null) {
            throw new IllegalArgumentException("selectMode is null.");
        }

        setSelectMode(SelectMode.valueOf(selectMode.toUpperCase()));
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
}
