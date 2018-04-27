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

import org.apache.pivot.collections.List;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;

/**
 * Table view listener interface.
 */
public interface TableViewListener {
    /**
     * Table view listeners.
     */
    public static class Listeners extends ListenerList<TableViewListener> implements TableViewListener {
        @Override
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            forEach(listener -> listener.tableDataChanged(tableView, previousTableData));
        }

        @Override
        public void columnSourceChanged(TableView tableView, TableView previousColumnSource) {
            forEach(listener -> listener.columnSourceChanged(tableView, previousColumnSource));
        }

        @Override
        public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
            forEach(listener -> listener.rowEditorChanged(tableView, previousRowEditor));
        }

        @Override
        public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
            forEach(listener -> listener.selectModeChanged(tableView, previousSelectMode));
        }

        @Override
        public void disabledRowFilterChanged(TableView tableView,
            Filter<?> previousDisabledRowFilter) {
            forEach(listener -> listener.disabledRowFilterChanged(tableView, previousDisabledRowFilter));
        }
    }

    /**
     * Table view listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TableViewListener {
        @Override
        public void tableDataChanged(TableView tableView, List<?> previousTableData) {
            // empty block
        }

        @Override
        public void columnSourceChanged(TableView tableView, TableView previousColumnSource) {
            // empty block
        }

        @Override
        public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
            // empty block
        }

        @Override
        public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
            // empty block
        }

        @Override
        public void disabledRowFilterChanged(TableView tableView,
            Filter<?> previousDisabledRowFilter) {
            // empty block
        }
    }

    /**
     * Called when a table view's table data has changed.
     *
     * @param tableView The source of this event.
     * @param previousTableData The previous data list for this table view.
     */
    default void tableDataChanged(TableView tableView, List<?> previousTableData) {
    }

    /**
     * Called when a table view's column source has changed.
     *
     * @param tableView The source of this event.
     * @param previousColumnSource The previous column source for this table.
     */
    default void columnSourceChanged(TableView tableView, TableView previousColumnSource) {
    }

    /**
     * Called when a table view's row editor has changed.
     *
     * @param tableView The source of this event.
     * @param previousRowEditor The row editor that was previously used.
     */
    default void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor) {
    }

    /**
     * Called when a table view's select mode has changed.
     *
     * @param tableView The source of the event.
     * @param previousSelectMode What the select mode used to be.
     */
    default void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode) {
    }

    /**
     * Called when a table view's disabled row filter has changed.
     *
     * @param tableView The table view in question.
     * @param previousDisabledRowFilter What the previous filter for disabled rows was.
     */
    default void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter) {
    }
}
