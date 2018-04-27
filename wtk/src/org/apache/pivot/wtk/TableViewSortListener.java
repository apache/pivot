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

import org.apache.pivot.util.ListenerList;

/**
 * Table view sort listener interface.
 */
public interface TableViewSortListener {
    /**
     * Table view sort listeners.
     */
    public static class Listeners extends ListenerList<TableViewSortListener>
        implements TableViewSortListener {
        @Override
        public void sortAdded(TableView tableView, String columnName) {
            forEach(listener -> listener.sortAdded(tableView, columnName));
        }

        @Override
        public void sortUpdated(TableView tableView, String columnName,
            SortDirection previousSortDirection) {
            forEach(listener -> listener.sortUpdated(tableView, columnName, previousSortDirection));
        }

        @Override
        public void sortRemoved(TableView tableView, String columnName, SortDirection sortDirection) {
            forEach(listener -> listener.sortRemoved(tableView, columnName, sortDirection));
        }

        @Override
        public void sortChanged(TableView tableView) {
            forEach(listener -> listener.sortChanged(tableView));
        }
    }

    /**
     * Table view sort listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TableViewSortListener {
        @Override
        public void sortAdded(TableView tableView, String columnName) {
            // empty block
        }

        @Override
        public void sortUpdated(TableView tableView, String columnName,
            SortDirection previousSortDirection) {
            // empty block
        }

        @Override
        public void sortRemoved(TableView tableView, String columnName, SortDirection sortDirection) {
            // empty block
        }

        @Override
        public void sortChanged(TableView tableView) {
            // empty block
        }
    }

    /**
     * Called when a sort has been added to a table view.
     *
     * @param tableView The source of this event.
     * @param columnName The new column name added to the sort criteria.
     */
    default void sortAdded(TableView tableView, String columnName) {
    }

    /**
     * Called when a sort has been updated in a table view.
     *
     * @param tableView The source of this event.
     * @param columnName The column that was updated.
     * @param previousSortDirection The previous value of the sort direction for this column.
     */
    default void sortUpdated(TableView tableView, String columnName,
        SortDirection previousSortDirection) {
    }

    /**
     * Called when a sort has been removed from a table view.
     *
     * @param tableView The source of this event.
     * @param columnName The column name that was removed from the sort criteria.
     * @param sortDirection What the sort direction was for this column.
     */
    default void sortRemoved(TableView tableView, String columnName, SortDirection sortDirection) {
    }

    /**
     * Called when a table view's sort has changed.
     *
     * @param tableView The source of this event.
     */
    default void sortChanged(TableView tableView) {
    }
}
