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

/**
 * Table view column listener interface.
 */
public interface TableViewColumnListener {
    /**
     * Table view column listener adapter.
     */
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
     * @param tableView
     * @param index
     */
    public void columnInserted(TableView tableView, int index);

    /**
     * Called when columns are removed from a table view's column sequence.
     *
     * @param tableView
     * @param index
     * @param columns
     */
    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns);

    /**
     * Called when a column's name has changed.
     *
     * @param column
     * @param previousName
     */
    public void columnNameChanged(TableView.Column column, String previousName);

    /**
     * Called when a column's header data has changed.
     *
     * @param column
     * @param previousHeaderData
     */
    public void columnHeaderDataChanged(TableView.Column column, Object previousHeaderData);

    /**
     * Called when a column's header data renderer has changed.
     *
     * @param column
     * @param previousHeaderDataRenderer
     */
    public void columnHeaderDataRendererChanged(TableView.Column column,
        TableView.HeaderDataRenderer previousHeaderDataRenderer);

    /**
     * Called when a column's width has changed.
     *
     * @param column
     * @param previousWidth
     * @param previousRelative
     */
    public void columnWidthChanged(TableView.Column column, int previousWidth,
        boolean previousRelative);

    /**
     * Called when a column's width limits have changed.
     *
     * @param column
     * @param previousMinimumWidth
     * @param previousMaximumWidth
     */
    public void columnWidthLimitsChanged(TableView.Column column, int previousMinimumWidth,
        int previousMaximumWidth);

    /**
     * Called when a column's filter has changed.
     *
     * @param column
     * @param previousFilter
     */
    public void columnFilterChanged(TableView.Column column, Object previousFilter);

    /**
     * Called when a column's cell renderer has changed.
     *
     * @param column
     * @param previousCellRenderer
     */
    public void columnCellRendererChanged(TableView.Column column,
        TableView.CellRenderer previousCellRenderer);
}
