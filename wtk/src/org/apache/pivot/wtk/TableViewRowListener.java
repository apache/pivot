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

/**
 * Table view row listener interface.
 */
public interface TableViewRowListener {
    /**
     * Table row listener adapter.
     */
    public static class Adapter implements TableViewRowListener {
        @Override
        public void rowInserted(TableView tableView, int index) {
            // empty block
        }

        @Override
        public void rowsRemoved(TableView tableView, int index, int count) {
            // empty block
        }

        @Override
        public void rowUpdated(TableView tableView, int index) {
            // empty block
        }

        @Override
        public void rowsCleared(TableView tableView) {
            // empty block
        }

        @Override
        public void rowsSorted(TableView tableView) {
            // empty block
        }
    }

    /**
     * Called when a row has been inserted into the table view.
     *
     * @param tableView
     * The source of the event.
     *
     * @param index
     * The index of the row that was inserted.
     */
    public void rowInserted(TableView tableView, int index);

    /**
     * Called when rows have been removed from the table view.
     *
     * @param tableView
     * The source of the event.
     *
     * @param index
     * The first index affected by the event.
     *
     * @param count
     * The number of rows that were removed, or <tt>-1</tt> if all rows
     * were removed.
     */
    public void rowsRemoved(TableView tableView, int index, int count);

    /**
     * Called when an row in the table view has been updated.
     *
     * @param tableView
     * The source of the event.
     *
     * @param index
     * The first index affected by the event.
     */
    public void rowUpdated(TableView tableView, int index);

    /**
     * Called when the rows in a table view have been cleared.
     *
     * @param tableView
     */
    public void rowsCleared(TableView tableView);

    /**
     * Called when the rows in a table have been sorted.
     *
     * @param tableView
     * The source of the event.
     */
    public void rowsSorted(TableView tableView);
}
