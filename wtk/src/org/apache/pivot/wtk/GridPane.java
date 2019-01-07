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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Container that arranges components in a two-dimensional grid, where every
 * cell is the same size.
 */
@DefaultProperty("rows")
public class GridPane extends Container {
    /**
     * Represents a grid pane row.
     */
    public static class Row implements Sequence<Component>, Iterable<Component> {
        private ArrayList<Component> cells = new ArrayList<>();

        private GridPane gridPane = null;

        public Row() {
        }

        public Row(GridPane gridPane) {
            if (gridPane != null) {
                gridPane.getRows().add(this);
            }
        }

        /**
         * Returns the grid pane with which this row is associated.
         *
         * @return The row's grid pane, or <tt>null</tt> if the row does not
         * currently belong to a grid.
         */
        public GridPane getGridPane() {
            return gridPane;
        }

        @Override
        public int add(Component component) {
            int index = getLength();
            insert(component, index);

            return index;
        }

        @Override
        public void insert(Component component, int index) {
            Utils.checkNull(component, "component");

            if (component.getParent() != null) {
                throw new IllegalArgumentException("Component already has a parent.");
            }

            cells.insert(component, index);

            if (gridPane != null) {
                gridPane.add(component);
                gridPane.gridPaneListeners.cellInserted(this, index);
            }
        }

        @Override
        public Component update(int index, Component component) {
            Component previousComponent = cells.get(index);

            if (component != previousComponent) {
                Utils.checkNull(component, "Component");

                if (component.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                cells.update(index, component);

                if (gridPane != null) {
                    gridPane.add(component);
                    gridPane.gridPaneListeners.cellUpdated(this, index, previousComponent);
                    gridPane.remove(previousComponent);
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

            if (gridPane != null) {
                gridPane.gridPaneListeners.cellsRemoved(this, index, removed);

                for (int i = 0, n = removed.getLength(); i < n; i++) {
                    Component component = removed.get(i);
                    gridPane.remove(component);
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
            return new ImmutableIterator<>(cells.iterator());
        }
    }

    /**
     * Grid pane skin interface. Grid pane skins must implement this interface
     * to facilitate additional communication between the component and the
     * skin.
     */
    public interface Skin {
        public int getRowAt(int y);

        public Bounds getRowBounds(int row);

        public int getColumnAt(int x);

        public Bounds getColumnBounds(int column);
    }

    /**
     * Class that manages a grid pane's row list. Callers get access to the row
     * sequence via {@link GridPane#getRows()}.
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
            Utils.checkNull(row, "Row");

            if (row.getGridPane() != null) {
                throw new IllegalArgumentException("Row is already in use by another grid pane.");
            }

            rows.insert(row, index);
            row.gridPane = GridPane.this;

            for (int i = 0, n = row.getLength(); i < n; i++) {
                Component component = row.get(i);
                GridPane.this.add(component);
            }

            // Notify listeners
            gridPaneListeners.rowInserted(GridPane.this, index);
        }

        @Override
        @UnsupportedOperation
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
                    row.gridPane = null;

                    for (int j = 0, m = row.getLength(); j < m; j++) {
                        Component component = row.get(j);
                        GridPane.this.remove(component);
                    }
                }

                gridPaneListeners.rowsRemoved(GridPane.this, index, removed);
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
            return new ImmutableIterator<>(rows.iterator());
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

    private int columnCount;

    private ArrayList<Row> rows = new ArrayList<>();
    private RowSequence rowSequence = new RowSequence();

    private GridPaneListener.Listeners gridPaneListeners = new GridPaneListener.Listeners();

    /**
     * Creates a new grid pane.
     */
    public GridPane() {
        this(0);
    }

    /**
     * Creates a new grid pane with the specified column count.
     *
     * @param columnCount Number of columns for this grid.
     */
    public GridPane(int columnCount) {
        Utils.checkNonNegative(columnCount, "columnCount");

        setColumnCount(columnCount);

        installSkin(GridPane.class);
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, GridPane.Skin.class);

        super.setSkin(skin);
    }

    /**
     * @return The number of columns in the grid pane.
     */
    public int getColumnCount() {
        return columnCount;
    }

    /**
     * Sets the number of columns in the grid pane.
     *
     * @param columnCount The new number of columns in the grid.
     */
    public void setColumnCount(int columnCount) {
        int previousColumnCount = this.columnCount;

        if (previousColumnCount != columnCount) {
            this.columnCount = columnCount;
            gridPaneListeners.columnCountChanged(this, previousColumnCount);
        }
    }

    /**
     * @return The grid pane row sequence.
     */
    public RowSequence getRows() {
        return rowSequence;
    }

    /**
     * Returns the index of the row at a given location.
     *
     * @param y The y-coordinate of the row to identify.
     * @return The row index, or <tt>-1</tt> if there is no row at the given
     * y-coordinate.
     */
    public int getRowAt(int y) {
        GridPane.Skin gridPaneSkin = (GridPane.Skin) getSkin();
        return gridPaneSkin.getRowAt(y);
    }

    /**
     * Returns the bounds of a given row.
     *
     * @param row The row index.
     * @return The bounds for the given row.
     */
    public Bounds getRowBounds(int row) {
        GridPane.Skin gridPaneSkin = (GridPane.Skin) getSkin();
        return gridPaneSkin.getRowBounds(row);
    }

    /**
     * Returns the index of the column at a given location.
     *
     * @param x The x-coordinate of the column to identify.
     * @return The column index, or <tt>-1</tt> if there is no column at the
     * given x-coordinate.
     */
    public int getColumnAt(int x) {
        GridPane.Skin gridPaneSkin = (GridPane.Skin) getSkin();
        return gridPaneSkin.getColumnAt(x);
    }

    /**
     * Returns the bounds of a given column.
     *
     * @param column The column index.
     * @return The bounds of the given column.
     */
    public Bounds getColumnBounds(int column) {
        GridPane.Skin gridPaneSkin = (GridPane.Skin) getSkin();
        return gridPaneSkin.getColumnBounds(column);
    }

    /**
     * Gets the component at the specified cell in this grid pane.
     *
     * @param rowIndex The row index of the cell.
     * @param columnIndex The column index of the cell.
     * @return The component in the specified cell, or <tt>null</tt> if the cell
     * is empty.
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
     * @param index The index at which components were removed.
     * @param count The number of components removed.
     * @return The sequence of components that were removed.
     */
    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            for (Row row : rows) {
                if (row.indexOf(component) >= 0) {
                    throw new UnsupportedOperationException("Cannot directly remove a row from a GridPane.");
                }
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    /**
     * @return The grid pane listener list.
     */
    public ListenerList<GridPaneListener> getGridPaneListeners() {
        return gridPaneListeners;
    }
}
