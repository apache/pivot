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
package pivot.wtk;

import java.util.Comparator;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.content.TableViewCellRenderer;

/**
 * Displays a sequence of items partitioned into columns, optionally allowing a
 * user to select one or more rows.
 *
 * @author gbrown
 */
@ComponentInfo(icon="TableView.png")
public class TableView extends Component {
    /**
     * Contains information about a table column.
     *
     * @author gbrown
     */
    public static class Column {
        private TableView tableView = null;

        private String name = null;
        private Object headerData = null;
        private int width = 0;
        private boolean relative = false;
        private SortDirection sortDirection = null;
        Object filter = null;
        private CellRenderer cellRenderer = new TableViewCellRenderer();

        /**
         * Default column width.
         */
        public static final int DEFAULT_WIDTH = 100;

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
            if (name == null) {
                throw new IllegalArgumentException("name is required.");
            }

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
        protected void setTableView(TableView tableView) {
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
                    tableView.tableViewColumnListeners.columnNameChanged(tableView,
                        tableView.getColumns().indexOf(this), previousName);
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
                    tableView.tableViewColumnListeners.columnHeaderDataChanged(tableView,
                        tableView.getColumns().indexOf(this), previousHeaderData);
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
                    tableView.tableViewColumnListeners.columnWidthChanged(tableView,
                        tableView.getColumns().indexOf(this), previousWidth,
                        previousRelative);
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
         * @param selected
         * <tt>true</tt> to select the column; <tt>false</tt> to de-select it.
         */
        public void setSortDirection(SortDirection sortDirection) {
            SortDirection previousSortDirection = this.sortDirection;

            if (previousSortDirection != sortDirection) {
                this.sortDirection = sortDirection;

                if (tableView != null) {
                    tableView.tableViewColumnListeners.columnSortDirectionChanged(tableView,
                        tableView.getColumns().indexOf(this), previousSortDirection);
                }
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
                    tableView.tableViewColumnListeners.columnFilterChanged(tableView,
                        tableView.getColumns().indexOf(this), previousFilter);
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
                    tableView.tableViewColumnListeners.columnCellRendererChanged(tableView,
                        tableView.getColumns().indexOf(this), previousCellRenderer);
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
        MULTI;

        public static SelectMode decode(String value) {
            return valueOf(value.toUpperCase());
        }
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
     * Table view skin interface. Table view skins must implement
     * this interface to facilitate additional communication between the
     * component and the skin.
     *
     * @author gbrown
     */
    public interface Skin extends pivot.wtk.Skin {
        public int getRowAt(int y);
        public int getColumnAt(int x);
        public Rectangle getRowBounds(int rowIndex);
        public Rectangle getCellBounds(int rowIndex, int columnIndex);
    }

    /**
     * Internal class for managing the table's column list.
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
     * List event handler.
     *
     * @author gbrown
     */
    private class ListHandler implements ListListener<Object> {
        public void itemInserted(List<Object> list, int index) {
            int m = selectedRanges.insertIndex(index);

            // Notify listeners that items were inserted
            tableViewRowListeners.rowInserted(TableView.this, index);

            // If any spans were modified, notify listeners of selection change
            if (m > 0) {
                tableViewSelectionListeners.selectionChanged(TableView.this);
            }
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            if (items == null) {
                // All items were removed; clear the selection and notify
                // listeners
                selectedRanges.clear();
                tableViewRowListeners.rowsRemoved(TableView.this, index, -1);
                tableViewSelectionListeners.selectionChanged(TableView.this);
            } else {
                int count = items.getLength();

                int s = selectedRanges.getLength();
                int m = selectedRanges.removeIndexes(index, count);

                // Notify listeners that items were removed
                tableViewRowListeners.rowsRemoved(TableView.this, index, count);

                // If any selection values were removed or any spans were modified,
                // notify listeners of selection change
                if (s != selectedRanges.getLength()
                    || m > 0) {
                    tableViewSelectionListeners.selectionChanged(TableView.this);
                }
            }
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            tableViewRowListeners.rowUpdated(TableView.this, index);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                int s = selectedRanges.getLength();
                selectedRanges.clear();

                tableViewRowListeners.rowsSorted(TableView.this);

                if (s > 0) {
                    tableViewSelectionListeners.selectionChanged(TableView.this);
                }
            }
        }
    }

    /**
     * Table view listener list.
     *
     * @author gbrown
     */
    private class TableViewListenerList extends ListenerList<TableViewListener>
        implements TableViewListener {
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            for (TableViewListener listener : this) {
                listener.tableDataChanged(tableView, previousTableData);
            }
        }

        public void selectModeChanged(TableView tableView, SelectMode previousSelectMode) {
            for (TableViewListener listener : this) {
                listener.selectModeChanged(tableView, previousSelectMode);
            }
        }
    }

    /**
     * Table view column listener list.
     *
     * @author gbrown
     */
    private class TableViewColumnListenerList extends ListenerList<TableViewColumnListener>
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

        public void columnNameChanged(TableView tableView, int index, String previousName) {
            for (TableViewColumnListener listener : this) {
                listener.columnNameChanged(tableView, index, previousName);
            }
        }

        public void columnHeaderDataChanged(TableView tableView, int index, Object previousHeaderData) {
            for (TableViewColumnListener listener : this) {
                listener.columnHeaderDataChanged(tableView, index, previousHeaderData);
            }
        }

        public void columnWidthChanged(TableView tableView, int index, int previousWidth, boolean previousRelative) {
            for (TableViewColumnListener listener : this) {
                listener.columnWidthChanged(tableView, index, previousWidth, previousRelative);
            }
        }

        public void columnSortDirectionChanged(TableView tableView, int index, SortDirection previousSortDirection) {
            for (TableViewColumnListener listener : this) {
                listener.columnSortDirectionChanged(tableView, index, previousSortDirection);
            }
        }

        public void columnFilterChanged(TableView tableView, int index, Object previousFilter) {
            for (TableViewColumnListener listener : this) {
                listener.columnFilterChanged(tableView, index, previousFilter);
            }
        }

        public void columnCellRendererChanged(TableView tableView, int index, TableView.CellRenderer previousCellRenderer) {
            for (TableViewColumnListener listener : this) {
                listener.columnCellRendererChanged(tableView, index, previousCellRenderer);
            }
        }
    }

    /**
     * Table view item listener list.
     *
     * @author gbrown
     */
    private class TableViewRowListenerList extends ListenerList<TableViewRowListener>
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

        public void rowsSorted(TableView tableView) {
            for (TableViewRowListener listener : this) {
                listener.rowsSorted(tableView);
            }
        }
    }


    /**
     * List view item state listener list.
     *
     * @author gbrown
     */
    private class TableViewRowStateListenerList extends ListenerList<TableViewRowStateListener>
        implements TableViewRowStateListener {
        public void rowDisabledChanged(TableView tableView, int index) {
            for (TableViewRowStateListener listener : this) {
                listener.rowDisabledChanged(tableView, index);
            }
        }
    }

    /**
     * Table view selection listener list.
     *
     * @author gbrown
     */
    private class TableViewSelectionListenerList extends ListenerList<TableViewSelectionListener>
        implements TableViewSelectionListener {
        public void selectionChanged(TableView tableView) {
            for (TableViewSelectionListener listener : this) {
                listener.selectionChanged(tableView);
            }
        }
    }

    /**
     * Table view selection detail listener list.
     *
     * @author gbrown
     */
    private class TableViewSelectionDetailListenerList extends ListenerList<TableViewSelectionDetailListener>
        implements TableViewSelectionDetailListener {
        public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
            for (TableViewSelectionDetailListener listener : this) {
                listener.selectedRangeAdded(tableView, rangeStart, rangeEnd);
            }
        }

        public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
            for (TableViewSelectionDetailListener listener : this) {
                listener.selectedRangeRemoved(tableView, rangeStart, rangeEnd);
            }
        }

        public void selectionReset(TableView tableView, Sequence<Span> previousSelection) {
            for (TableViewSelectionDetailListener listener : this) {
                listener.selectionReset(tableView, previousSelection);
            }
        }
    }

    private ArrayList<Column> columns = new ArrayList<Column>();
    private ColumnSequence columnSequence = new ColumnSequence();

    private List<?> tableData = null;
    private ListHandler tableDataHandler = new ListHandler();

    private SpanSequence selectedRanges = new SpanSequence();
    private SelectMode selectMode = SelectMode.SINGLE;

    private ArrayList<Integer> disabledIndexes = new ArrayList<Integer>();

    private TableViewListenerList tableViewListeners = new TableViewListenerList();
    private TableViewColumnListenerList tableViewColumnListeners =
        new TableViewColumnListenerList();
    private TableViewRowListenerList tableViewRowListeners = new TableViewRowListenerList();
    private TableViewRowStateListenerList tableViewRowStateListeners =
        new TableViewRowStateListenerList();
    private TableViewSelectionListenerList tableViewSelectionListeners
        = new TableViewSelectionListenerList();
    private TableViewSelectionDetailListenerList tableViewSelectionDetailListeners
        = new TableViewSelectionDetailListenerList();

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
    public void setSkinClass(Class<? extends pivot.wtk.Skin> skinClass) {
        if (!TableView.Skin.class.isAssignableFrom(skinClass)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TableView.Skin.class.getName());
        }

        super.setSkinClass(skinClass);
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

                ((List<Object>)previousTableData).getListListeners().remove(tableDataHandler);
            }

            ((List<Object>)tableData).getListListeners().add(tableDataHandler);

            // Update the list data and fire change event
            this.tableData = tableData;
            tableViewListeners.tableDataChanged(this, previousTableData);
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

        return (selectedRanges.getLength() == 0) ? -1 : selectedRanges.get(0).getStart();
    }

    /**
     * Sets the selection to a single index.
     *
     * @param index
     * The index to select, or <tt>-1</tt> to clear the selection.
     */
    public void setSelectedIndex(int index) {
        ArrayList<Span> selectedRanges = new ArrayList<Span>();

        if (index >= 0) {
            selectedRanges.add(new Span(index, index));
        }

        setSelectedRanges(selectedRanges);
    }

    /**
     * Returns the table's current selection.
     */
    public Sequence<Span> getSelectedRanges() {
        // Return a copy of the selection list (including copies of the
        // list contents)
        ArrayList<Span> selectedRanges = new ArrayList<Span>();

        for (int i = 0, n = this.selectedRanges.getLength(); i < n; i++) {
            selectedRanges.add(new Span(this.selectedRanges.get(i)));
        }

        return selectedRanges;
    }

    /**
     * Sets the selection to the given span sequence. Any overlapping or
     * connecting spans will be consolidated, and the resulting selection will
     * be sorted in ascending order.
     *
     * @param selection
     */
    public void setSelectedRanges(Sequence<Span> selectedRanges) {
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
        SpanSequence ranges = new SpanSequence();

        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span range = selectedRanges.get(i);

            if (range == null) {
                throw new IllegalArgumentException("range is null.");
            }

            if (range.getStart() < 0 || range.getEnd() >= tableData.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            ranges.add(range);
        }

        SpanSequence previousSelectedRanges = this.selectedRanges;
        this.selectedRanges = ranges;

        // Notify listeners
        tableViewSelectionDetailListeners.selectionReset(this, previousSelectedRanges);
        tableViewSelectionListeners.selectionChanged(this);
    }

    /**
     * Returns the first selected index.
     *
     * @return
     * The first selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getFirstSelectedIndex() {
        return (selectedRanges.getLength() > 0) ?
            selectedRanges.get(0).getStart() : -1;
    }

    /**
     * Returns the last selected index.
     *
     * @return
     * The last selected index, or <tt>-1</tt> if nothing is selected.
     */
    public int getLastSelectedIndex() {
        return (selectedRanges.getLength() > 0) ?
            selectedRanges.get(selectedRanges.getLength() - 1).getEnd() : -1;
    }

    /**
     * Adds a single index to the selection.
     *
     * @param index
     * The index to add.
     */
    public void addSelectedIndex(int index) {
        addSelectedRange(index, index);
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param rangeStart
     * The first index in the range.
     *
     * @param rangeEnd
     * The last index in the range.
     */
    public void addSelectedRange(int rangeStart, int rangeEnd) {
        addSelectedRange(new Span(rangeStart, rangeEnd));
    }

    /**
     * Adds a range of indexes to the selection.
     *
     * @param range
     * The range to add.
     */
    public void addSelectedRange(Span range) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Table view is not in multi-select mode.");
        }

        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        if (range.getStart() < 0 || range.getEnd() >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        selectedRanges.add(range);

        tableViewSelectionDetailListeners.selectedRangeAdded(this,
            range.getStart(), range.getEnd());
        tableViewSelectionListeners.selectionChanged(this);
    }

    /**
     * Removes a single index from the selection.
     *
     * @param index
     * The index to remove.
     */
    public void removeSelectedIndex(int index) {
        removeSelectedRange(index, index);
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param range
     * The range to remove.
     */
    public void removeSelectedRange(int rangeStart, int rangeEnd) {
        removeSelectedRange(new Span(rangeStart, rangeEnd));
    }

    /**
     * Removes a range of indexes from the selection.
     *
     * @param range
     * The range to remove.
     */
    public void removeSelectedRange(Span range) {
        if (selectMode != SelectMode.MULTI) {
            throw new IllegalStateException("Table view is not in multi-select mode.");
        }

        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        if (range.getStart() < 0 || range.getEnd() >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        selectedRanges.remove(range);

        tableViewSelectionDetailListeners.selectedRangeRemoved(this,
            range.getStart(), range.getEnd());
        tableViewSelectionListeners.selectionChanged(this);
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (selectedRanges.getLength() > 0) {
            SpanSequence previousSelectedSpans = this.selectedRanges;
            selectedRanges = new SpanSequence();

            tableViewSelectionDetailListeners.selectionReset(this, previousSelectedSpans);
            tableViewSelectionListeners.selectionChanged(this);
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
    public boolean isIndexSelected(int index) {
        return isRangeSelected(index, index);
    }

    /**
     * Returns the selection state of a given range.
     *
     * @param rangeStart
     * The first index in the range.
     *
     * @param rangeEnd
     * The last index in the range.
     *
     * @return <tt>true</tt> if the entire range is selected; <tt>false</tt>,
     * otherwise.
     */
    public boolean isRangeSelected(int rangeStart, int rangeEnd) {
        return isRangeSelected(new Span(rangeStart, rangeEnd));
    }

    /**
     * Returns the selection state of a given range.
     *
     * @param range
     * The range whose selection state is to be tested.
     *
     * @return <tt>true</tt> if the entire range is selected; <tt>false</tt>,
     * otherwise.
     */
    public boolean isRangeSelected(Span range) {
        boolean selected = false;

        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        if (range.getStart() < 0 || range.getEnd() >= tableData.getLength()) {
            throw new IndexOutOfBoundsException();
        }

        // Locate the span in the selection
        int i = selectedRanges.indexOf(range);

        // If the selected span contains the given span, it is considered
        // selected
        if (i >= 0) {
            Span selectedSpan = selectedRanges.get(i);
            selected = selectedSpan.contains(range);
        }

        return selected;
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

        setSelectMode(SelectMode.decode(selectMode));
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
    public boolean isRowDisabled(int index) {
        return (Sequence.Search.binarySearch(disabledIndexes, index) >= 0);
    }

    /**
     * Sets the disabled state of a row.
     *
     * @param index
     * The index of the row whose disabled state is to be set.
     *
     * @param disabled
     * <tt>true</tt> to disable the row; <tt>false</tt>, otherwise.
     */
    public void setRowDisabled(int index, boolean disabled) {
        int i = Sequence.Search.binarySearch(disabledIndexes, index);

        if ((i < 0 && disabled)
            || (i >= 0 && !disabled)) {
            if (disabled) {
                disabledIndexes.insert(index, -(i + 1));
            } else {
                disabledIndexes.remove(i, 1);
            }

            tableViewRowStateListeners.rowDisabledChanged(this, index);
        }
    }

    public Sequence<Integer> getDisabledIndexes() {
        ArrayList<Integer> disabledIndexes = new ArrayList<Integer>();

        for (int i = 0, n = this.disabledIndexes.getLength(); i < n; i++) {
            disabledIndexes.add(this.disabledIndexes.get(i));
        }

        return disabledIndexes;
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
    public Rectangle getRowBounds(int rowIndex) {
        TableView.Skin tableViewSkin = (TableView.Skin)getSkin();
        return tableViewSkin.getRowBounds(rowIndex);
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
    public Rectangle getCellBounds(int rowIndex, int columnIndex) {
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

    public ListenerList<TableViewRowStateListener> getTableViewRowStateListeners() {
        return tableViewRowStateListeners;
    }

    public ListenerList<TableViewSelectionListener> getTableViewSelectionListeners() {
        return tableViewSelectionListeners;
    }

    public ListenerList<TableViewSelectionDetailListener> getTableViewSelectionDetailListeners() {
        return tableViewSelectionDetailListeners;
    }
}
