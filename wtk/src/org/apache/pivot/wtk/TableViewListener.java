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

/**
 * Table view listener interface.
 */
public interface TableViewListener {
    /**
     * Table view listener adapter.
     */
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
        public void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter) {
            // empty block
        }
    }

    /**
     * Called when a table view's table data has changed.
     *
     * @param tableView
     * @param previousTableData
     */
    public void tableDataChanged(TableView tableView, List<?> previousTableData);

    /**
     * Called when a table view's column source has changed.
     *
     * @param tableView
     * @param previousColumnSource
     */
    public void columnSourceChanged(TableView tableView, TableView previousColumnSource);

    /**
     * Called when a table view's row editor has changed.
     *
     * @param tableView
     * @param previousRowEditor
     */
    public void rowEditorChanged(TableView tableView, TableView.RowEditor previousRowEditor);

    /**
     * Called when a table view's select mode has changed.
     *
     * @param tableView
     * @param previousSelectMode
     */
    public void selectModeChanged(TableView tableView, TableView.SelectMode previousSelectMode);

    /**
     * Called when a table view's disabled row filter has changed.
     *
     * @param tableView
     * @param previousDisabledRowFilter
     */
    public void disabledRowFilterChanged(TableView tableView, Filter<?> previousDisabledRowFilter);
}
