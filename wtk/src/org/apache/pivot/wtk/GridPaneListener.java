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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Grid pane listener interface.
 */
public interface GridPaneListener {
    /**
     * Grid pane listeners.
     */
    public static class Listeners extends ListenerList<GridPaneListener> implements
        GridPaneListener {
        @Override
        public void columnCountChanged(GridPane gridPane, int previousColumnCount) {
            forEach(listener -> listener.columnCountChanged(gridPane, previousColumnCount));
        }

        @Override
        public void rowInserted(GridPane gridPane, int index) {
            forEach(listener -> listener.rowInserted(gridPane, index));
        }

        @Override
        public void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows) {
            forEach(listener -> listener.rowsRemoved(gridPane, index, rows));
        }

        @Override
        public void cellInserted(GridPane.Row row, int column) {
            forEach(listener -> listener.cellInserted(row, column));
        }

        @Override
        public void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed) {
            forEach(listener -> listener.cellsRemoved(row, column, removed));
        }

        @Override
        public void cellUpdated(GridPane.Row row, int column, Component previousComponent) {
            forEach(listener -> listener.cellUpdated(row, column, previousComponent));
        }
    }

    /**
     * Grid pane listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements GridPaneListener {
        @Override
        public void columnCountChanged(GridPane gridPane, int previousColumnCount) {
            // empty block
        }

        @Override
        public void rowInserted(GridPane gridPane, int index) {
            // empty block
        }

        @Override
        public void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows) {
            // empty block
        }

        @Override
        public void cellInserted(GridPane.Row row, int column) {
            // empty block
        }

        @Override
        public void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed) {
            // empty block
        }

        @Override
        public void cellUpdated(GridPane.Row row, int column, Component previousComponent) {
            // empty block
        }
    }

    /**
     * Called when a grid pane's column count has changed.
     *
     * @param gridPane            The grid pane that has changed.
     * @param previousColumnCount The previous column count for the grid.
     */
    default void columnCountChanged(GridPane gridPane, int previousColumnCount) {
    }

    /**
     * Called when a row has been inserted into a grid pane.
     *
     * @param gridPane The grid pane that has changed.
     * @param index    The index of the row that was just inserted.
     */
    default void rowInserted(GridPane gridPane, int index) {
    }

    /**
     * Called when rows have been removed from a grid pane.
     *
     * @param gridPane The grid pane that has changed.
     * @param index    The starting index of the row(s) that were removed.
     * @param rows     The complete sequence of removed rows.
     */
    default void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows) {
    }

    /**
     * Called when a cell has been inserted into a grid pane.
     *
     * @param row    The parent row of the cell that was inserted.
     * @param column The column index of the inserted cell.
     */
    default void cellInserted(GridPane.Row row, int column) {
    }

    /**
     * Called when cells have been removed from a grid pane.
     *
     * @param row     The parent row of the removed cell(s).
     * @param column  The starting column index of the removed cells.
     * @param removed The complete sequence of removed cells.
     */
    default void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed) {
    }

    /**
     * Called when a cell has been updated in a grid pane.
     *
     * @param row               The parent row object of the updated cell.
     * @param column            The column index of the updated cell.
     * @param previousComponent The previous contents of this cell.
     */
    default void cellUpdated(GridPane.Row row, int column, Component previousComponent) {
    }
}
