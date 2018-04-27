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
 * Table view column listener interface.
 */
public interface TableViewColumnListener {
    /**
     * Table view column listeners.
     */
    public static class Listeners extends ListenerList<TableViewColumnListener>
        implements TableViewColumnListener {
        @Override
        public void columnInserted(TableView tableView, int index) {
            forEach(listener -> listener.columnInserted(tableView, index));
        }

        @Override
        public void columnsRemoved(TableView tableView, int index,
            Sequence<TableView.Column> columns) {
            forEach(listener -> listener.columnsRemoved(tableView, index, columns));
        }

        @Override
        public void columnNameChanged(TableView.Column column, String previousName) {
            forEach(listener -> listener.columnNameChanged(column, previousName));
        }

        @Override
        public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
            forEach(listener -> listener.columnHeaderDataChanged(column, previousHeaderData));
        }

        @Override
        public void columnHeaderDataRendererChanged(TableView.Column column,
            TableView.HeaderDataRenderer previousHeaderDataRenderer) {
            forEach(listener -> listener.columnHeaderDataRendererChanged(column, previousHeaderDataRenderer));
        }

        @Override
        public void columnWidthChanged(TableView.Column column, int previousWidth, boolean previousRelative) {
            forEach(listener -> listener.columnWidthChanged(column, previousWidth, previousRelative));
        }

        @Override
        public void columnWidthLimitsChanged(TableView.Column column, int previousMinimumWidth,
            int previousMaximumWidth) {
            forEach(listener -> listener.columnWidthLimitsChanged(column, previousMinimumWidth,
                    previousMaximumWidth));
        }

        @Override
        public void columnFilterChanged(TableView.Column column, Object previousFilter) {
            forEach(listener -> listener.columnFilterChanged(column, previousFilter));
        }

        @Override
        public void columnCellRendererChanged(TableView.Column column,
            TableView.CellRenderer previousCellRenderer) {
            forEach(listener -> listener.columnCellRendererChanged(column, previousCellRenderer));
        }
    }

    /**
     * Table view column listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TableViewColumnListener {
        @Override
        public void columnInserted(TableView tableView, int index) {
            // empty block
        }

        @Override
        public void columnsRemoved(TableView tableView, int index,
            Sequence<TableView.Column> columns) {
            // empty block
        }

        @Override
        public void columnNameChanged(TableView.Column column, String previousName) {
            // empty block
        }

        @Override
        public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
            // empty block
        }

        @Override
        public void columnHeaderDataRendererChanged(TableView.Column column,
            TableView.HeaderDataRenderer previousColumnHeaderDataRenderer) {
            // empty block
        }

        @Override
        public void columnWidthChanged(TableView.Column column, int previousWidth,
            boolean previousRelative) {
            // empty block
        }

        @Override
        public void columnWidthLimitsChanged(TableView.Column column, int previousMinimumWidth,
            int previousMaximumWidth) {
            // empty block
        }

        @Override
        public void columnFilterChanged(TableView.Column column, Object previousFilter) {
            // empty block
        }

        @Override
        public void columnCellRendererChanged(TableView.Column column,
            TableView.CellRenderer previousCellRenderer) {
            // empty block
        }
    }

    /**
     * Called when a column is inserted into a table view's column sequence.
     *
     * @param tableView The table view that has changed.
     * @param index Where the new column has been inserted.
     */
    default void columnInserted(TableView tableView, int index) {
    }

    /**
     * Called when columns are removed from a table view's column sequence.
     *
     * @param tableView The table view that has changed.
     * @param index The starting location of the removed columns.
     * @param columns The actual sequence of columns that were removed.
     */
    default void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns) {
    }

    /**
     * Called when a column's name has changed.
     *
     * @param column The column that changed names.
     * @param previousName What the previous name was.
     */
    default void columnNameChanged(TableView.Column column, String previousName) {
    }

    /**
     * Called when a column's header data has changed.
     *
     * @param column The column that changed.
     * @param previousHeaderData What the header data used to be.
     */
    default void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData) {
    }

    /**
     * Called when a column's header data renderer has changed.
     *
     * @param column The column whose header data renderer has changed.
     * @param previousHeaderDataRenderer The previous renderer for header data.
     */
    default void columnHeaderDataRendererChanged(TableView.Column column,
        TableView.HeaderDataRenderer previousHeaderDataRenderer) {
    }

    /**
     * Called when a column's width has changed.
     *
     * @param column The column that changed.
     * @param previousWidth The previous numeric value of the column width.
     * @param previousRelative Whether the previous width was relative or not.
     */
    default void columnWidthChanged(TableView.Column column, int previousWidth,
        boolean previousRelative) {
    }

    /**
     * Called when a column's width limits have changed.
     *
     * @param column The source of this event.
     * @param previousMinimumWidth The previous minimum column width.
     * @param previousMaximumWidth The previous maximum column width.
     */
    default void columnWidthLimitsChanged(TableView.Column column, int previousMinimumWidth,
        int previousMaximumWidth) {
    }

    /**
     * Called when a column's filter has changed.
     *
     * @param column The source of this event.
     * @param previousFilter The previous filter value for this column.
     */
    default void columnFilterChanged(TableView.Column column, Object previousFilter) {
    }

    /**
     * Called when a column's cell renderer has changed.
     *
     * @param column The source of this event.
     * @param previousCellRenderer The previous cell renderer for this column.
     */
    default void columnCellRendererChanged(TableView.Column column,
        TableView.CellRenderer previousCellRenderer) {
    }
}
