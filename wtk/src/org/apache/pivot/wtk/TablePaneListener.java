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
 * Table pane listener interface.
 */
public interface TablePaneListener {
    /**
     * Table pane listeners.
     */
    public static class Listeners extends ListenerList<TablePaneListener> implements TablePaneListener {
        @Override
        public void rowInserted(TablePane tablePane, int index) {
            forEach(listener -> listener.rowInserted(tablePane, index));
        }

        @Override
        public void rowsRemoved(TablePane tablePane, int index, Sequence<TablePane.Row> rows) {
            forEach(listener -> listener.rowsRemoved(tablePane, index, rows));
        }

        @Override
        public void rowHeightChanged(TablePane.Row row, int previousHeight, boolean previousRelative) {
            forEach(listener -> listener.rowHeightChanged(row, previousHeight, previousRelative));
        }

        @Override
        public void rowHighlightedChanged(TablePane.Row row) {
            forEach(listener -> listener.rowHighlightedChanged(row));
        }

        @Override
        public void columnInserted(TablePane tablePane, int index) {
            forEach(listener -> listener.columnInserted(tablePane, index));
        }

        @Override
        public void columnsRemoved(TablePane tablePane, int index,
            Sequence<TablePane.Column> columns) {
            forEach(listener -> listener.columnsRemoved(tablePane, index, columns));
        }

        @Override
        public void columnWidthChanged(TablePane.Column column, int previousWidth,
            boolean previousRelative) {
            forEach(listener -> listener.columnWidthChanged(column, previousWidth, previousRelative));
        }

        @Override
        public void columnHighlightedChanged(TablePane.Column column) {
            forEach(listener -> listener.columnHighlightedChanged(column));
        }

        @Override
        public void cellInserted(TablePane.Row row, int column) {
            forEach(listener -> listener.cellInserted(row, column));
        }

        @Override
        public void cellsRemoved(TablePane.Row row, int column, Sequence<Component> removed) {
            forEach(listener -> listener.cellsRemoved(row, column, removed));
        }

        @Override
        public void cellUpdated(TablePane.Row row, int column, Component previousComponent) {
            forEach(listener -> listener.cellUpdated(row, column, previousComponent));
        }
    }

    /**
     * Table pane listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TablePaneListener {
        @Override
        public void rowInserted(TablePane tablePane, int index) {
            // empty block
        }

        @Override
        public void rowsRemoved(TablePane tablePane, int index, Sequence<TablePane.Row> rows) {
            // empty block
        }

        @Override
        public void rowHeightChanged(TablePane.Row row, int previousHeight, boolean previousRelative) {
            // empty block
        }

        @Override
        public void rowHighlightedChanged(TablePane.Row row) {
            // empty block
        }

        @Override
        public void columnInserted(TablePane tablePane, int index) {
            // empty block
        }

        @Override
        public void columnsRemoved(TablePane tablePane, int index,
            Sequence<TablePane.Column> columns) {
            // empty block
        }

        @Override
        public void columnWidthChanged(TablePane.Column column, int previousWidth,
            boolean previousRelative) {
            // empty block
        }

        @Override
        public void columnHighlightedChanged(TablePane.Column column) {
            // empty block
        }

        @Override
        public void cellInserted(TablePane.Row row, int column) {
            // empty block
        }

        @Override
        public void cellsRemoved(TablePane.Row row, int column, Sequence<Component> removed) {
            // empty block
        }

        @Override
        public void cellUpdated(TablePane.Row row, int column, Component previousComponent) {
            // empty block
        }
    }

    /**
     * Called when a row has been inserted into a table pane.
     *
     * @param tablePane The source of this event.
     * @param index The index of the inserted row.
     */
    default void rowInserted(TablePane tablePane, int index) {
    }

    /**
     * Called when rows have been removed from a table pane.
     *
     * @param tablePane The source of this event.
     * @param index The starting index of the rows that were removed.
     * @param rows The actual sequence of rows that were removed.
     */
    default void rowsRemoved(TablePane tablePane, int index, Sequence<TablePane.Row> rows) {
    }

    /**
     * Called when a row's height has changed.
     *
     * @param row The particular row whose height has changed.
     * @param previousHeight The previous numeric height value.
     * @param previousRelative Whether the previous row height was relative or not.
     */
    default void rowHeightChanged(TablePane.Row row, int previousHeight, boolean previousRelative) {
    }

    /**
     * Called when a row's highlighted state has changed.
     *
     * @param row The source of this event.
     */
    default void rowHighlightedChanged(TablePane.Row row) {
    }

    /**
     * Called when a column has been inserted into a table pane.
     *
     * @param tablePane The table pane that changed.
     * @param index The location where the column was inserted.
     */
    default void columnInserted(TablePane tablePane, int index) {
    }

    /**
     * Called when columns have been removed from a table pane.
     *
     * @param tablePane The table pane that changed.
     * @param index The starting index of the removed columns.
     * @param columns The actual sequence of the columns that were removed.
     */
    default void columnsRemoved(TablePane tablePane, int index, Sequence<TablePane.Column> columns) {
    }

    /**
     * Called when a column's width has changed.
     *
     * @param column The column that changed.
     * @param previousWidth The previous numeric value of the width.
     * @param previousRelative Whether the previous width was relative or not.
     */
    default void columnWidthChanged(TablePane.Column column, int previousWidth,
        boolean previousRelative) {
    }

    /**
     * Called when a column's highlighted state has changed.
     *
     * @param column The column that changed.
     */
    default void columnHighlightedChanged(TablePane.Column column) {
    }

    /**
     * Called when a cell has been inserted into a table pane.
     *
     * @param row The table pane row that has changed.
     * @param column The index of the new column.
     */
    default void cellInserted(TablePane.Row row, int column) {
    }

    /**
     * Called when cells have been removed from a table pane.
     *
     * @param row The table pane row that has changed.
     * @param column The starting index of the column(s) that were removed.
     * @param removed The actual sequence of removed columns.
     */
    default void cellsRemoved(TablePane.Row row, int column, Sequence<Component> removed) {
    }

    /**
     * Called when a cell has been updated in a table pane.
     *
     * @param row The table pane row that was changed.
     * @param column Index of the column that was updated.
     * @param previousComponent The previous cell contents.
     */
    default void cellUpdated(TablePane.Row row, int column, Component previousComponent) {
    }
}
