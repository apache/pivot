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
package pivot.wtk;

import java.util.Comparator;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.ListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.ListenerList;

import pivot.wtk.content.TableViewCellRenderer;
import pivot.wtk.content.TableViewHeaderData;

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
            setSortDirection(sortDirection == null ? (SortDirection)null :
                SortDirection.decode(sortDirection));
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

            return (comparable.compareTo(value)) * (sortDirection == SortDirection.ASCENDING ? 1 : -1);
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

            Object headerData = column.getHeaderData();
            if (!(headerData instanceof TableViewHeaderData)) {
                headerData = new TableViewHeaderData((String)headerData);
                column.setHeaderData(headerData);
            }

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
     * List event handler.
     *
     * @author gbrown
     */
    private class ListHandler implements ListListener<Object> {
        public void itemInserted(List<Object> list, int index) {
            // Increment selected ranges
            selectedRanges.insertIndex(index);

            // Increment disabled indexes
            int i = ArrayList.binarySearch(disabledIndexes, index);
            if (i < 0) {
                i = -(i + 1);
            }

            int n = disabledIndexes.getLength();
            while (i < n) {
                disabledIndexes.update(i, disabledIndexes.get(i) + 1);
                i++;
            }

            // Notify listeners that items were inserted
            tableViewRowListeners.rowInserted(TableView.this, index);
        }

        public void itemsRemoved(List<Object> list, int index, Sequence<Object> items) {
            int count = items.getLength();

            // Decrement selected ranges
            selectedRanges.removeIndexes(index, count);

            // Decrement disabled indexes
            int i = ArrayList.binarySearch(disabledIndexes, index);
            if (i < 0) {
                i = -(i + 1);
            }

            int n = disabledIndexes.getLength();
            while (i < n) {
                disabledIndexes.update(i, disabledIndexes.get(i) - count);
                i++;
            }

            // Notify listeners that items were removed
            tableViewRowListeners.rowsRemoved(TableView.this, index, count);
        }

        public void itemUpdated(List<Object> list, int index, Object previousItem) {
            tableViewRowListeners.rowUpdated(TableView.this, index);
        }

        public void listCleared(List<Object> list) {
            // All items were removed; clear the selection and notify
            // listeners
            selectedRanges.clear();
            disabledIndexes.clear();

            tableViewRowListeners.rowsCleared(TableView.this);
        }

        public void comparatorChanged(List<Object> list,
            Comparator<Object> previousComparator) {
            if (list.getComparator() != null) {
                selectedRanges.clear();
                disabledIndexes.clear();

                tableViewRowListeners.rowsSorted(TableView.this);
            }
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
     * List view item state listener list.
     *
     * @author gbrown
     */
    private static class TableViewRowStateListenerList extends ListenerList<TableViewRowStateListener>
        implements TableViewRowStateListener {
        public void rowDisabledChanged(TableView tableView, int index) {
            for (TableViewRowStateListener listener : this) {
                listener.rowDisabledChanged(tableView, index);
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
    private ListHandler tableDataHandler = new ListHandler();

    private SpanSequence selectedRanges = new SpanSequence();
    private SelectMode selectMode = SelectMode.SINGLE;

    private ArrayList<Integer> disabledIndexes = new ArrayList<Integer>();

    private RowEditor rowEditor = null;

    private TableViewListenerList tableViewListeners = new TableViewListenerList();
    private TableViewColumnListenerList tableViewColumnListeners =
        new TableViewColumnListenerList();
    private TableViewRowListenerList tableViewRowListeners = new TableViewRowListenerList();
    private TableViewRowStateListenerList tableViewRowStateListeners =
        new TableViewRowStateListenerList();
    private TableViewSelectionListenerList tableViewSelectionListeners
        = new TableViewSelectionListenerList();

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
    protected void setSkin(pivot.wtk.Skin skin) {
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

                ((List<Object>)previousTableData).getListListeners().remove(tableDataHandler);
            }

            ((List<Object>)tableData).getListListeners().add(tableDataHandler);

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

        setTableData(JSONSerializer.parseList(tableData));
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
     * @param selectedRanges
     * The new selection
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
        tableViewSelectionListeners.selectedRangesChanged(this, previousSelectedRanges);
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

        tableViewSelectionListeners.selectedRangeAdded(this, range.getStart(),
            range.getEnd());
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
     * @param rangeStart
     * The start of the range to remove.
     *
     * @param rangeEnd
     * The end of the range to remove.
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

        tableViewSelectionListeners.selectedRangeRemoved(this, range.getStart(),
            range.getEnd());
    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        if (selectedRanges.getLength() > 0) {
            SpanSequence previousSelectedSpans = this.selectedRanges;
            selectedRanges = new SpanSequence();

            tableViewSelectionListeners.selectedRangesChanged(this, previousSelectedSpans);
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

    public Object getSelectedRow() {
        int index = getSelectedIndex();
        Object row = null;

        if (index >= 0) {
            row = tableData.get(index);
        }

        return row;
    }

    public Sequence<Object> getSelectedRows() {
        ArrayList<Object> rows = new ArrayList<Object>();

        for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
            Span span = selectedRanges.get(i);

            for (int index = span.getStart(), end = span.getEnd(); index <= end; index++) {
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
        return (ArrayList.binarySearch(disabledIndexes, index) >= 0);
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
        int i = ArrayList.binarySearch(disabledIndexes, index);

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

    public ListenerList<TableViewRowStateListener> getTableViewRowStateListeners() {
        return tableViewRowStateListeners;
    }

    public ListenerList<TableViewSelectionListener> getTableViewSelectionListeners() {
        return tableViewSelectionListeners;
    }
}
