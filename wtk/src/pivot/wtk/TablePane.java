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

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ListenerList;

/**
 *
 * @author tvolkert
 */
public class TablePane extends Container {
    public static final class Row implements Sequence<Component> {
        private int height = 0;
        private boolean relative = false;
        private boolean selected = false;

        private ArrayList<Component> cells = new ArrayList<Component>();

        private TablePane tablePane = null;

        public Row() {
            this(0, false, false);
        }

        public Row(int height) {
            this(height, false, false);
        }

        public Row(int height, boolean relative) {
            this(height, relative, false);
        }

        public Row(int height, boolean relative, boolean selected) {
            this.height = height;
            this.relative = relative;
            this.selected = selected;
        }

        /**
         * Returns the table pane with which this row is associated.
         *
         * @return
         * The row's table pane, or <tt>null</tt> if the row does not
         * currently belong to a table.
         */
        public TablePane getTablePane() {
            return tablePane;
        }

        /**
         * Sets the table pane with which this row is associated.
         *
         * @param tablePane
         * The row's table pane, or <tt>null</tt> if the row does not
         * currently belong to a table.
         */
        private void setTablePane(TablePane tablePane) {
            this.tablePane = tablePane;
        }

        /**
         * Returns the row height.
         *
         * @return
         * The height of the row.
         */
        public int getHeight() {
            return height;
        }

        /**
         * Returns the relative flag.
         *
         * @return
         * <tt>true</tt> if the row height is relative, <tt>false</tt> if it
         * is fixed.
         */
        public boolean isRelative() {
            return relative;
        }

        /**
         * Set the row height.
         *
         * @param height
         * The absolute height of the row.
         */
        public void setHeight(int height) {
            setHeight(height, false);
        }

        /**
         * Set the row height.
         *
         * @param height
         * The encoded height of the row. If the string ends with the '*'
         * character, it is treated as a relative value. Otherwise, it is
         * considered an absolute value.
         */
        public void setHeight(String height) {
            boolean relative = false;

            if (height.endsWith(RELATIVE_SIZE_INDICATOR)) {
                relative = true;
                height = height.substring(0, height.length() - 1);
            }

            setHeight(Integer.parseInt(height), relative);
        }

        /**
         * Sets the row height.
         *
         * @param height
         * The height of the row.
         *
         * @param relative
         * <tt>true</tt> if the row height is relative, <tt>false</tt> if it
         * is fixed.
         */
        public void setHeight(int height, boolean relative) {
            int previousHeight = this.height;
            boolean previousRelative = this.relative;

            if (previousHeight != height
                || previousRelative != relative) {
                this.height = height;
                this.relative = relative;

                if (tablePane != null) {
                    tablePane.tablePaneListeners.rowHeightChanged(this,
                        previousHeight, previousRelative);
                }
            }
        }

        /**
         * Returns the selected flag.
         *
         * @return
         * <tt>true</tt> if the row is selected, <tt>false</tt> if it is not
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Sets the selected flag.
         *
         * @param selected
         * <tt>true</tt> to set the row as selected, <tt>false</tt> to set
         * it as not selected
         */
        public void setSelected(boolean selected) {
            if (selected != this.selected) {
                this.selected = selected;

                if (tablePane != null) {
                    tablePane.tablePaneListeners.rowSelectedChanged(this);
                }
            }
        }

        public int add(Component component) {
            int i = getLength();
            insert(component, i);

            return i;
        }

        public void insert(Component component, int index) {
            if (index < 0
                || index > cells.getLength()) {
                throw new IndexOutOfBoundsException();
            }

            if (component != null
                && tablePane != null) {
                // Add the component to the table pane
                tablePane.add(component);

                // Attach the attributes
                component.setAttributes(new TablePaneAttributes());
            }

            cells.insert(component, index);

            if (tablePane != null) {
                // Notify table pane listeners
                tablePane.tablePaneListeners.cellInserted(this, index);
            }
        }

        public Component update(int index, Component component) {
            Component previousComponent = cells.get(index);

            if (component != null
                && tablePane != null) {
                // Add the component to the table pane
                tablePane.add(component);

                // Attach the attributes
                component.setAttributes(new TablePaneAttributes());
            }

            cells.update(index, component);

            if (previousComponent != null
                && tablePane != null) {
                // Detach the attributes
                component.setAttributes(null);
            }

            if (tablePane != null
                && component != previousComponent) {
                // Notify table pane listeners
                tablePane.tablePaneListeners.cellUpdated(this, index,
                    previousComponent);
            }

            if (previousComponent != null
                && tablePane != null) {
                // Remove the component from the table pane
                tablePane.remove(component);
            }

            return previousComponent;
        }

        public int remove(Component component) {
            int index = indexOf(component);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Component> remove(int index, int count) {
            Sequence<Component> removed = cells.remove(index, count);

            if (tablePane != null) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Component component = removed.get(i);
                    if (component != null) {
                        component.setAttributes(null);
                    }
                }

                // Notify table pane listeners
                tablePane.tablePaneListeners.cellsRemoved(this, index, removed);

                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Component component = removed.get(i);
                    if (component != null) {
                        tablePane.remove(component);
                    }
                }
            }

            return removed;
        }

        public Component get(int index) {
            return cells.get(index);
        }

        public int indexOf(Component component) {
            return cells.indexOf(component);
        }

        public int getLength() {
            return cells.getLength();
        }
    }

    public static class Column {
        private TablePane tablePane = null;

        private int width = 0;
        private boolean relative = false;
        private boolean selected = false;

        public Column() {
            this(0, false, false);
        }

        public Column(int width) {
            this(width, false, false);
        }

        public Column(int width, boolean relative) {
            this(width, relative, false);
        }

        public Column(int width, boolean relative, boolean selected) {
            this.width = width;
            this.relative = relative;
            this.selected = selected;
        }

        /**
         * Returns the table pane with which this column is associated.
         *
         * @return
         * The column's table pane, or <tt>null</tt> if the column does not
         * currently belong to a table.
         */
        public TablePane getTablePane() {
            return tablePane;
        }

        /**
         * Sets the table pane with which this column is associated.
         *
         * @param tablePane
         * The column's table pane, or <tt>null</tt> if the column does not
         * currently belong to a table.
         */
        private void setTablePane(TablePane tablePane) {
            this.tablePane = tablePane;
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

            if (width.endsWith(RELATIVE_SIZE_INDICATOR)) {
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

                if (tablePane != null) {
                    tablePane.tablePaneListeners.columnWidthChanged(this,
                        previousWidth, previousRelative);
                }
            }
        }

        /**
         * Returns the selected flag.
         *
         * @return
         * <tt>true</tt> if the column is selected, <tt>false</tt> if it is not
         */
        public boolean isSelected() {
            return selected;
        }

        /**
         * Sets the selected flag.
         *
         * @param selected
         * <tt>true</tt> to set the column as selected, <tt>false</tt> to set
         * it as not selected
         */
        public void setSelected(boolean selected) {
            if (selected != this.selected) {
                this.selected = selected;

                if (tablePane != null) {
                    tablePane.tablePaneListeners.columnSelectedChanged(this);
                }
            }
        }
    }

    protected static class TablePaneAttributes extends Attributes {
        private int rowSpan = 1;
        private int columnSpan = 1;

        public int getRowSpan() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            int previousRowSpan = this.rowSpan;
            this.rowSpan = rowSpan;

            Component component = getComponent();
            TablePane tablePane = (TablePane)component.getParent();
            if (tablePane != null) {
                tablePane.tablePaneAttributeListeners.rowSpanChanged(tablePane,
                    component, previousRowSpan);
            }
        }

        public int getColumnSpan() {
            return columnSpan;
        }

        public void setColumnSpan(int columnSpan) {
            int previousColumnSpan = this.columnSpan;
            this.columnSpan = columnSpan;

            Component component = getComponent();
            TablePane tablePane = (TablePane)component.getParent();
            if (tablePane != null) {
                tablePane.tablePaneAttributeListeners.columnSpanChanged(tablePane,
                    component, previousColumnSpan);
            }
        }
    }

    /**
     * Table pane skin interface. Table pane skins must implement
     * this interface to facilitate additional communication between the
     * component and the skin.
     *
     * @author tvolkert
     */
    public interface Skin {
        public int getRowAt(int y);
        public Bounds getRowBounds(int row);
        public int getColumnAt(int x);
        public Bounds getColumnBounds(int column);
    }

    /**
     * Class that manages a table pane's row list. Callers get access to the
     * row sequence via {@link TablePane#getRows()}.
     *
     * @author tvolkert
     */
    public final class RowSequence implements Sequence<Row> {
        private RowSequence() {
        }

        public int add(Row row) {
            int i = getLength();
            insert(row, i);

            return i;
        }

        public void insert(Row row, int index) {
            if (row == null) {
                throw new IllegalArgumentException("row is null.");
            }

            if (row.getTablePane() != null) {
                throw new IllegalArgumentException
                    ("row is already in use by another table pane.");
            }

            rows.insert(row, index);
            row.setTablePane(TablePane.this);

            for (int i = 0, n = row.getLength(); i < n; i++) {
                Component component = row.get(i);

                // Add each component in the row to the table pane
                TablePane.this.add(component);

                // Attach attributes to each row component
                component.setAttributes(new TablePaneAttributes());
            }

            // Notify listeners
            tablePaneListeners.rowInserted(TablePane.this, index);
        }

        public Row update(int index, Row row) {
            throw new UnsupportedOperationException();
        }

        public int remove(Row row) {
            int index = indexOf(row);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Row> remove(int index, int count) {
            Sequence<Row> removed = rows.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Row row = removed.get(i);

                    row.setTablePane(null);

                    for (int j = 0, m = row.getLength(); j < m; j++) {
                        Component component = row.get(j);

                        if (component != null) {
                            // Detach attributes from each row component
                            component.setAttributes(null);

                            // Remove each component in the row from the table pane
                            TablePane.this.remove(component);
                        }
                    }
                }

                tablePaneListeners.rowsRemoved(TablePane.this, index, removed);
            }

            return removed;
        }

        public Row get(int index) {
            return rows.get(index);
        }

        public int indexOf(Row row) {
            return rows.indexOf(row);
        }

        public int getLength() {
            return rows.getLength();
        }
    }

    /**
     * Class that manages a table pane's column list. Callers get access to the
     * column sequence via {@link TablePane#getColumns()}.
     *
     * @author tvolkert
     */
    public final class ColumnSequence implements Sequence<Column> {
        private ColumnSequence() {
        }

        public int add(Column column) {
            int i = getLength();
            insert(column, i);

            return i;
        }

        public void insert(Column column, int index) {
            if (column == null) {
                throw new IllegalArgumentException("column is null.");
            }

            if (column.getTablePane() != null) {
                throw new IllegalArgumentException
                    ("column is already in use by another table pane.");
            }

            columns.insert(column, index);
            column.setTablePane(TablePane.this);

            // Notify listeners
            tablePaneListeners.columnInserted(TablePane.this, index);
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
                    removed.get(i).setTablePane(null);
                }

                tablePaneListeners.columnsRemoved(TablePane.this, index, removed);
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
     * Internal listener list.
     */
    private static class TablePaneListenerList extends ListenerList<TablePaneListener>
        implements TablePaneListener {
        public void rowInserted(TablePane tablePane, int index) {
            for (TablePaneListener listener : this) {
                listener.rowInserted(tablePane, index);
            }
        }

        public void rowsRemoved(TablePane tablePane, int index,
            Sequence<TablePane.Row> rows) {
            for (TablePaneListener listener : this) {
                listener.rowsRemoved(tablePane, index, rows);
            }
        }

        public void rowHeightChanged(TablePane.Row row, int previousHeight,
            boolean previousRelative) {
            for (TablePaneListener listener : this) {
                listener.rowHeightChanged(row, previousHeight, previousRelative);
            }
        }

        public void rowSelectedChanged(TablePane.Row row) {
            for (TablePaneListener listener : this) {
                listener.rowSelectedChanged(row);
            }
        }

        public void columnInserted(TablePane tablePane, int index) {
            for (TablePaneListener listener : this) {
                listener.columnInserted(tablePane, index);
            }
        }

        public void columnsRemoved(TablePane tablePane, int index,
            Sequence<TablePane.Column> columns) {
            for (TablePaneListener listener : this) {
                listener.columnsRemoved(tablePane, index, columns);
            }
        }

        public void columnWidthChanged(TablePane.Column column, int previousWidth,
            boolean previousRelative) {
            for (TablePaneListener listener : this) {
                listener.columnWidthChanged(column, previousWidth, previousRelative);
            }
        }

        public void columnSelectedChanged(TablePane.Column column) {
            for (TablePaneListener listener : this) {
                listener.columnSelectedChanged(column);
            }
        }

        public void cellInserted(TablePane.Row row, int column) {
            for (TablePaneListener listener : this) {
                listener.cellInserted(row, column);
            }
        }

        public void cellsRemoved(TablePane.Row row, int column,
            Sequence<Component> removed) {
            for (TablePaneListener listener : this) {
                listener.cellsRemoved(row, column, removed);
            }
        }

        public void cellUpdated(TablePane.Row row, int column,
            Component previousComponent) {
            for (TablePaneListener listener : this) {
                listener.cellUpdated(row, column, previousComponent);
            }
        }
    }

    private static class TablePaneAttributeListenerList extends ListenerList<TablePaneAttributeListener>
        implements TablePaneAttributeListener {
        public void rowSpanChanged(TablePane tablePane, Component component,
            int previousRowSpan) {
            for (TablePaneAttributeListener listener : this) {
                listener.rowSpanChanged(tablePane, component, previousRowSpan);
            }
        }

        public void columnSpanChanged(TablePane tablePane, Component component,
            int previousColumnSpan) {
            for (TablePaneAttributeListener listener : this) {
                listener.columnSpanChanged(tablePane, component, previousColumnSpan);
            }
        }
    }

    private ArrayList<Row> rows = null;
    private RowSequence rowSequence = new RowSequence();

    private ArrayList<Column> columns = null;
    private ColumnSequence columnSequence = new ColumnSequence();

    private TablePaneListenerList tablePaneListeners = new TablePaneListenerList();
    private TablePaneAttributeListenerList tablePaneAttributeListeners = new TablePaneAttributeListenerList();

    public static final String RELATIVE_SIZE_INDICATOR = "*";

    /**
     * Creates a new <tt>TablePane</tt> with empty row and column sequences.
     */
    public TablePane() {
        this(new ArrayList<Column>());
    }

    /**
     * Creates a new <tt>TablePane</tt> with the specified columns.
     *
     * @param columns
     * The column sequence to use. A copy of this sequence will be made
     */
    public TablePane(Sequence<Column> columns) {
        if (columns == null) {
            throw new IllegalArgumentException("columns is null");
        }

        this.rows = new ArrayList<Row>();
        this.columns = new ArrayList<Column>(columns);

        installSkin(TablePane.class);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof TablePane.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TablePane.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * Returns the table pane row sequence.
     *
     * @return
     * The table pane row sequence
     */
    public RowSequence getRows() {
        return rowSequence;
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
        TablePane.Skin tablePaneSkin = (TablePane.Skin)getSkin();
        return tablePaneSkin.getRowAt(y);
    }

    /**
     * Returns the table pane column sequence.
     *
     * @return
     * The table pane column sequence
     */
    public ColumnSequence getColumns() {
        return columnSequence;
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
        TablePane.Skin tablePaneSkin = (TablePane.Skin)getSkin();
        return tablePaneSkin.getColumnAt(x);
    }

    /**
     * Gets the component at the specified cell in this table pane.
     *
     * @param rowIndex
     * The row index of the cell
     *
     * @param columnIndex
     * The column index of the cell
     *
     * @return
     * The component in the specified cell, or <tt>null</tt> if the cell is
     * empty
     */
    public Component getCellComponent(int rowIndex, int columnIndex) {
        Row row = rows.get(rowIndex);

        Component component = null;

        if (row.getLength() > columnIndex) {
            component = row.get(columnIndex);
        }

        return component;
    }

    /**
     * Sets the component at the specified cell in this table pane.
     *
     * @param row
     * The row index of the cell
     *
     * @param column
     * The column index of the cell
     *
     * @param component
     * The component to place in the specified cell, or <tt>null</tt> to empty
     * the cell
     */
    public void setCellComponent(int row, int column, Component component) {
        rows.get(row).update(column, component);
    }

    /**
     * Overrides the base method to check whether or not a cell component is
     * being removed, and fires the appropriate event in that case.
     *
     * @param index
     * The index at which components were removed
     *
     * @param count
     * The number of components removed
     *
     * @return
     * The sequence of components that were removed
     */
    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component.getAttributes() != null) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    /**
     * Gets this component's table pane listener list.
     *
     * @return
     * The table pane listeners on this component
     */
    public ListenerList<TablePaneListener> getTablePaneListeners() {
        return tablePaneListeners;
    }

    /**
     * Gets this component's table pane attribute listener list.
     *
     * @return
     * The table pane attribute listeners on this component
     */
    public ListenerList<TablePaneAttributeListener> getTablePaneAttributeListeners() {
        return tablePaneAttributeListeners;
    }

    public static int getRowSpan(Component component) {
        TablePaneAttributes tablePaneAttributes = (TablePaneAttributes)component.getAttributes();
        return (tablePaneAttributes == null) ? -1 : tablePaneAttributes.getRowSpan();
    }

    public static void setRowSpan(Component component, int rowSpan) {
        TablePaneAttributes tablePaneAttributes = (TablePaneAttributes)component.getAttributes();
        if (tablePaneAttributes == null) {
            throw new UnsupportedOperationException();
        }

        tablePaneAttributes.setRowSpan(rowSpan);
    }

    public static final void setRowSpan(Component component, String rowSpan) {
        if (rowSpan == null) {
            throw new IllegalArgumentException("rowSpan is null.");
        }

        setRowSpan(component, Integer.parseInt(rowSpan));
    }

    public static int getColumnSpan(Component component) {
        TablePaneAttributes tablePaneAttributes = (TablePaneAttributes)component.getAttributes();
        return (tablePaneAttributes == null) ? -1 : tablePaneAttributes.getColumnSpan();
    }

    public static void setColumnSpan(Component component, int columnSpan) {
        TablePaneAttributes tablePaneAttributes = (TablePaneAttributes)component.getAttributes();
        if (tablePaneAttributes == null) {
            throw new UnsupportedOperationException();
        }

        tablePaneAttributes.setColumnSpan(columnSpan);
    }

    public static final void setColumnSpan(Component component, String columnSpan) {
        if (columnSpan == null) {
            throw new IllegalArgumentException("columnSpan is null.");
        }

        setColumnSpan(component, Integer.parseInt(columnSpan));
    }
}
