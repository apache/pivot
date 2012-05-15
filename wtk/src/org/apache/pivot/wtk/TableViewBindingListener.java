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
 * Table view binding listener.
 */
public interface TableViewBindingListener {
    /**
     * Table view binding listener adapter.
     */
    public static class Adapter implements TableViewBindingListener {
        @Override
        public void tableDataKeyChanged(TableView tableView, String previousTableDataKey) {
            // empty block
        }

        @Override
        public void tableDataBindTypeChanged(TableView tableView, BindType previousTableDataBindType) {
            // empty block
        }

        @Override
        public void tableDataBindMappingChanged(TableView tableView,
            TableView.TableDataBindMapping previousTableDataBindMapping) {
            // empty block
        }

        @Override
        public void selectedRowKeyChanged(TableView tableView, String previousSelectedRowKey) {
            // empty block
        }

        @Override
        public void selectedRowBindTypeChanged(TableView tableView, BindType previousSelectedRowBindType) {
            // empty block
        }

        @Override
        public void selectedRowBindMappingChanged(TableView tableView,
            TableView.SelectedRowBindMapping previousSelectedRowBindMapping) {
            // empty block
        }

        @Override
        public void selectedRowsKeyChanged(TableView tableView, String previousSelectedRowsKey) {
            // empty block
        }

        @Override
        public void selectedRowsBindTypeChanged(TableView tableView, BindType previousSelectedRowsBindType) {
            // empty block
        }

        @Override
        public void selectedRowsBindMappingChanged(TableView tableView,
            TableView.SelectedRowBindMapping previousSelectedRowsBindMapping) {
            // empty block
        }
    }

    /**
     * Called when a table view's table data key has changed.
     *
     * @param tableView
     * @param previousTableDataKey
     */
    public void tableDataKeyChanged(TableView tableView, String previousTableDataKey);

    /**
     * Called when a table view's table data bind type has changed.
     *
     * @param tableView
     * @param previousTableDataBindType
     */
    public void tableDataBindTypeChanged(TableView tableView, BindType previousTableDataBindType);

    /**
     * Called when a table view's table data bind mapping has changed.
     *
     * @param tableView
     * @param previousTableDataBindMapping
     */
    public void tableDataBindMappingChanged(TableView tableView, TableView.TableDataBindMapping previousTableDataBindMapping);

    /**
     * Called when a table view's selected row key has changed.
     *
     * @param tableView
     * @param previousSelectedRowKey
     */
    public void selectedRowKeyChanged(TableView tableView, String previousSelectedRowKey);

    /**
     * Called when a table view's selected row bind type has changed.
     *
     * @param tableView
     * @param previousSelectedRowBindType
     */
    public void selectedRowBindTypeChanged(TableView tableView, BindType previousSelectedRowBindType);

    /**
     * Called when a table view's selected row bind mapping has changed.
     *
     * @param tableView
     * @param previousSelectedRowBindMapping
     */
    public void selectedRowBindMappingChanged(TableView tableView,
        TableView.SelectedRowBindMapping previousSelectedRowBindMapping);

    /**
     * Called when a table view's selected rows key has changed.
     *
     * @param tableView
     * @param previousSelectedRowsKey
     */
    public void selectedRowsKeyChanged(TableView tableView, String previousSelectedRowsKey);

    /**
     * Called when a table view's selected rows bind type has changed.
     *
     * @param tableView
     * @param previousSelectedRowsBindType
     */
    public void selectedRowsBindTypeChanged(TableView tableView, BindType previousSelectedRowsBindType);

    /**
     * Called when a table view's selected rows bind mapping has changed.
     *
     * @param tableView
     * @param previousSelectedRowsBindMapping
     */
    public void selectedRowsBindMappingChanged(TableView tableView,
        TableView.SelectedRowBindMapping previousSelectedRowsBindMapping);
}
