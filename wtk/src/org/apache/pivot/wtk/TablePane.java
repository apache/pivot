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

import java.util.Iterator;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

/**
 * Container that arranges components in a two-dimensional grid, optionally
 * spanning multiple rows and columns, much like an HTML <tt>&lt;table&gt;</tt>
 * element.
 * <p>
 * Note that unlike an HTML <tt>&lt;table&gt;</tt>, components that span
 * multiple rows or columns will not "push" other components out of their way.
 * Instead, the spanning components will simply overlay the cells into which
 * they span. This means that application developers may have to use
 * {@link Filler filler cells} in the cells that are spanned.
 */
@DefaultProperty("rows")
public class TablePane extends Container {
    /**
     * Represents a table pane row.
     */
    public static class Row implements Sequence<Component>, Iterable<Component> {
        private int height;
        private boolean relative;
        private boolean highlighted;

        private ArrayList<Component> cells = new ArrayList<Component>();

        private TablePane tablePane = null;

        public Row() {
            this(-1, false, false);
        }

        public Row(int height) {
            this(height, false, false);
        }

        public Row(int height, boolean relative) {
            this(height, relative, false);
        }

        public Row(int height, boolean relative, boolean highlighted) {
            this.height = height;
            this.relative = relative;
            this.highlighted = highlighted;
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
            boolean relativeLocal = false;

            if (height.endsWith(RELATIVE_SIZE_INDICATOR)) {
                relativeLocal = true;
                height = height.substring(0, height.length() - 1);
            }

            setHeight(Integer.parseInt(height), relativeLocal);
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
         * Returns the highlighted flag.
         *
         * @return
         * <tt>true</tt> if the row is highlighted, <tt>false</tt> if it is not
         */
        public boolean isHighlighted() {
            return highlighted;
        }

        /**
         * Sets the highlighted flag.
         *
         * @param highlighted
         * <tt>true</tt> to set the row as highlighted, <tt>false</tt> to set
         * it as not highlighted
         */
        public void setHighlighted(boolean highlighted) {
            if (highlighted != this.highlighted) {
                this.highlighted = highlighted;

                if (tablePane != null) {
                    tablePane.tablePaneListeners.rowHighlightedChanged(this);
                }
            }
        }

        @Override
        public int add(Component component) {
            int index = getLength();
            insert(component, index);

            return index;
        }

        @Override
        public void insert(Component component, int index) {
            if (component == null) {
                throw new IllegalArgumentException("Component is null.");
            }

            if (component.getParent() != null) {
                throw new IllegalArgumentException("Component already has a parent.");
            }

            cells.insert(component, index);

            if (tablePane != null) {
                tablePane.add(component);
                tablePane.tablePaneListeners.cellInserted(this, index);
            }
        }

        @Override
        public Component update(int index, Component component) {
            Component previousComponent = cells.get(index);

            if (component != previousComponent) {
                if (component == null) {
                    throw new IllegalArgumentException("Component is null.");
                }

                if (component.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                cells.update(index, component);

                if (tablePane != null) {
                    tablePane.add(component);
                    tablePane.tablePaneListeners.cellUpdated(this, index, previousComponent);
                    tablePane.remove(previousComponent);
                }
            }

            return previousComponent;
        }

        @Override
        public int remove(Component component) {
            int index = indexOf(component);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Component> remove(int index, int count) {
            Sequence<Component> removed = cells.remove(index, count);

            if (tablePane != null) {
                tablePane.tablePaneListeners.cellsRemoved(this, index, removed);

                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Component component = removed.get(i);
                    tablePane.remove(component);
                }
            }

            return removed;
        }

        @Override
        public Component get(int index) {
            return cells.get(index);
        }

        @Override
        public int indexOf(Component component) {
            return cells.indexOf(component);
        }

        @Override
        public int getLength() {
            return cells.getLength();
        }

        @Override
        public Iterator<Component> iterator() {
            return new ImmutableIterator<Component>(cells.iterator());
        }
    }

    /**
     * Represents a table pane column.
     */
    public static class Column {
        private TablePane tablePane = null;

        private int width;
        private boolean relative;
        private boolean highlighted;

        public Column() {
            this(-1, false, false);
        }

        public Column(int width) {
            this(width, false, false);
        }

        public Column(int width, boolean relative) {
            this(width, relative, false);
        }

        public Column(int width, boolean relative, boolean highlighted) {
            this.width = width;
            this.relative = relative;
            this.highlighted = highlighted;
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

            if (width.endsWith(RELATIVE_SIZE_INDICATOR)) {
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
         * Returns the highlighted flag.
         *
         * @return
         * <tt>true</tt> if the column is highlighted, <tt>false</tt> if it is not
         */
        public boolean isHighlighted() {
            return highlighted;
        }

        /**
         * Sets the highlighted flag.
         *
         * @param highlighted
         * <tt>true</tt> to set the column as highlighted, <tt>false</tt> to set
         * it as not highlighted
         */
        public void setHighlighted(boolean highlighted) {
            if (highlighted != this.highlighted) {
                this.highlighted = highlighted;

                if (tablePane != null) {
                    tablePane.tablePaneListeners.columnHighlightedChanged(this);
                }
            }
        }
    }

    /**
     * Table pane skin interface. Table pane skins must implement
     * this interface to facilitate additional communication between the
     * component and the skin.
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
     */
    public final class RowSequence implements Sequence<Row>, Iterable<Row> {
        private RowSequence() {
        }

        @Override
        public int add(Row row) {
            int index = getLength();
            insert(row, index);

            return index;
        }

        @Override
        public void insert(Row row, int index) {
            if (row == null) {
                throw new IllegalArgumentException("row is null.");
            }

            if (row.tablePane != null) {
                throw new IllegalArgumentException
                    ("row is already in use by another table pane.");
            }

            rows.insert(row, index);
            row.tablePane = TablePane.this;

            for (int i = 0, n = row.getLength(); i < n; i++) {
                Component component = row.get(i);
                TablePane.this.add(component);
            }

            // Notify listeners
            tablePaneListeners.rowInserted(TablePane.this, index);
        }

        @Override
        public Row update(int index, Row row) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Row row) {
            int index = indexOf(row);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Row> remove(int index, int count) {
            Sequence<Row> removed = rows.remove(index, count);

            if (count > 0) {
                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Row row = removed.get(i);
                    row.tablePane = null;

                    for (int j = 0, m = row.getLength(); j < m; j++) {
                        Component component = row.get(j);
                        TablePane.this.remove(component);
                    }
                }

                tablePaneListeners.rowsRemoved(TablePane.this, index, removed);
            }

            return removed;
        }

        @Override
        public Row get(int index) {
            return rows.get(index);
        }

        @Override
        public int indexOf(Row row) {
            return rows.indexOf(row);
        }

        @Override
        public int getLength() {
            return rows.getLength();
        }

        @Override
        public Iterator<Row> iterator() {
            return new ImmutableIterator<Row>(rows.iterator());
        }
    }

    /**
     * Class that manages a table pane's column list. Callers get access to the
     * column sequence via {@link TablePane#getColumns()}.
     */
    public final class ColumnSequence implements Sequence<Column>, Iterable<Column> {
        private ColumnSequence() {
        }

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

            if (column.tablePane != null) {
                throw new IllegalArgumentException
                    ("column is already in use by another table pane.");
            }

            columns.insert(column, index);
            column.tablePane = TablePane.this;

            // Notify listeners
            tablePaneListeners.columnInserted(TablePane.this, index);
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
                    Column column = removed.get(i);
                    column.tablePane = null;
                }

                tablePaneListeners.columnsRemoved(TablePane.this, index, removed);
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
     * Component that can be used as filler for empty cells.
     */
    public static final class Filler extends Component {
        public Filler() {
            installSkin(Filler.class);
        }
    }

    private enum Attribute {
        ROW_SPAN,
        COLUMN_SPAN;
    }

    private static class TablePaneListenerList extends WTKListenerList<TablePaneListener>
        implements TablePaneListener {
        @Override
        public void rowInserted(TablePane tablePane, int index) {
            for (TablePaneListener listener : this) {
                listener.rowInserted(tablePane, index);
            }
        }

        @Override
        public void rowsRemoved(TablePane tablePane, int index,
            Sequence<TablePane.Row> rows) {
            for (TablePaneListener listener : this) {
                listener.rowsRemoved(tablePane, index, rows);
            }
        }

        @Override
        public void rowHeightChanged(TablePane.Row row, int previousHeight,
            boolean previousRelative) {
            for (TablePaneListener listener : this) {
                listener.rowHeightChanged(row, previousHeight, previousRelative);
            }
        }

        @Override
        public void rowHighlightedChanged(TablePane.Row row) {
            for (TablePaneListener listener : this) {
                listener.rowHighlightedChanged(row);
            }
        }

        @Override
        public void columnInserted(TablePane tablePane, int index) {
            for (TablePaneListener listener : this) {
                listener.columnInserted(tablePane, index);
            }
        }

        @Override
        public void columnsRemoved(TablePane tablePane, int index,
            Sequence<TablePane.Column> columns) {
            for (TablePaneListener listener : this) {
                listener.columnsRemoved(tablePane, index, columns);
            }
        }

        @Override
        public void columnWidthChanged(TablePane.Column column, int previousWidth,
            boolean previousRelative) {
            for (TablePaneListener listener : this) {
                listener.columnWidthChanged(column, previousWidth, previousRelative);
            }
        }

        @Override
        public void columnHighlightedChanged(TablePane.Column column) {
            for (TablePaneListener listener : this) {
                listener.columnHighlightedChanged(column);
            }
        }

        @Override
        public void cellInserted(TablePane.Row row, int column) {
            for (TablePaneListener listener : this) {
                listener.cellInserted(row, column);
            }
        }

        @Override
        public void cellsRemoved(TablePane.Row row, int column,
            Sequence<Component> removed) {
            for (TablePaneListener listener : this) {
                listener.cellsRemoved(row, column, removed);
            }
        }

        @Override
        public void cellUpdated(TablePane.Row row, int column,
            Component previousComponent) {
            for (TablePaneListener listener : this) {
                listener.cellUpdated(row, column, previousComponent);
            }
        }
    }

    private static class TablePaneAttributeListenerList extends WTKListenerList<TablePaneAttributeListener>
        implements TablePaneAttributeListener {
        @Override
        public void rowSpanChanged(TablePane tablePane, Component component,
            int previousRowSpan) {
            for (TablePaneAttributeListener listener : this) {
                listener.rowSpanChanged(tablePane, component, previousRowSpan);
            }
        }

        @Override
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
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
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
     * Returns the bounds of a given row.
     *
     * @param row
     * The row index.
     */
    public Bounds getRowBounds(int row) {
        TablePane.Skin tablePaneSkin = (TablePane.Skin)getSkin();
        return tablePaneSkin.getRowBounds(row);
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
     * Returns the bounds of a given column.
     *
     * @param column
     * The column index.
     */
    public Bounds getColumnBounds(int column) {
        TablePane.Skin tablePaneSkin = (TablePane.Skin)getSkin();
        return tablePaneSkin.getColumnBounds(column);
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

            for (Row row : rows) {
                if (row.indexOf(component) >= 0) {
                    throw new UnsupportedOperationException();
                }
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    /**
     * Returns the table pane listener list.
     */
    public ListenerList<TablePaneListener> getTablePaneListeners() {
        return tablePaneListeners;
    }

    /**
     * Returns the table pane attribute listener list.
     */
    public ListenerList<TablePaneAttributeListener> getTablePaneAttributeListeners() {
        return tablePaneAttributeListeners;
    }

    public static int getRowSpan(Component component) {
        Integer value = (Integer)component.getAttribute(Attribute.ROW_SPAN);
        return (value == null) ? 1 : value;
    }

    public static void setRowSpan(Component component, int rowSpan) {
        Integer previousValue = (Integer)component.setAttribute(Attribute.ROW_SPAN, rowSpan);
        int previousRowSpan = (previousValue == null) ? 1 : previousValue;

        if (previousRowSpan != rowSpan) {
            Container parent = component.getParent();

            if (parent instanceof TablePane) {
                TablePane tablePane = (TablePane)parent;
                tablePane.tablePaneAttributeListeners.rowSpanChanged(tablePane,
                    component, previousRowSpan);
            }
        }
    }

    public static int getColumnSpan(Component component) {
        Integer value = (Integer)component.getAttribute(Attribute.COLUMN_SPAN);
        return (value == null) ? 1 : value;
    }

    public static void setColumnSpan(Component component, int columnSpan) {
        Integer previousValue = (Integer)component.setAttribute(Attribute.COLUMN_SPAN, columnSpan);
        int previousColumnSpan = (previousValue == null) ? 1 : previousValue;

        if (previousColumnSpan != columnSpan) {
            Container parent = component.getParent();

            if (parent instanceof TablePane) {
                TablePane tablePane = (TablePane)parent;
                tablePane.tablePaneAttributeListeners.columnSpanChanged(tablePane,
                    component, previousColumnSpan);
            }
        }
    }
}
