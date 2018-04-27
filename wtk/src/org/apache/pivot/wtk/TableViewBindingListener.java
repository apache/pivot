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
 * Table view binding listener.
 */
public interface TableViewBindingListener {
    /**
     * Table view binding listeners.
     */
    public static class Listeners extends ListenerList<TableViewBindingListener>
        implements TableViewBindingListener {
        @Override
        public void tableDataKeyChanged(TableView tableView, String previousTableDataKey) {
            forEach(listener -> listener.tableDataKeyChanged(tableView, previousTableDataKey));
        }

        @Override
        public void tableDataBindTypeChanged(TableView tableView, BindType previousTableDataBindType) {
            forEach(listener -> listener.tableDataBindTypeChanged(tableView, previousTableDataBindType));
        }

        @Override
        public void tableDataBindMappingChanged(TableView tableView,
            TableView.TableDataBindMapping previousTableDataBindMapping) {
            forEach(listener -> listener.tableDataBindMappingChanged(tableView, previousTableDataBindMapping));
        }

        @Override
        public void selectedRowKeyChanged(TableView tableView, String previousSelectedRowKey) {
            forEach(listener -> listener.selectedRowKeyChanged(tableView, previousSelectedRowKey));
        }

        @Override
        public void selectedRowBindTypeChanged(TableView tableView,
            BindType previousSelectedRowBindType) {
            forEach(listener -> listener.selectedRowBindTypeChanged(tableView, previousSelectedRowBindType));
        }

        @Override
        public void selectedRowBindMappingChanged(TableView tableView,
            TableView.SelectedRowBindMapping previousSelectedRowBindMapping) {
            forEach(listener -> listener.selectedRowBindMappingChanged(tableView, previousSelectedRowBindMapping));
        }

        @Override
        public void selectedRowsKeyChanged(TableView tableView, String previousSelectedRowsKey) {
            forEach(listener -> listener.selectedRowsKeyChanged(tableView, previousSelectedRowsKey));
        }

        @Override
        public void selectedRowsBindTypeChanged(TableView tableView,
            BindType previousSelectedRowsBindType) {
            forEach(listener -> listener.selectedRowsBindTypeChanged(tableView, previousSelectedRowsBindType));
        }

        @Override
        public void selectedRowsBindMappingChanged(TableView tableView,
            TableView.SelectedRowBindMapping previousSelectedRowsBindMapping) {
            forEach(listener -> listener.selectedRowsBindMappingChanged(tableView, previousSelectedRowsBindMapping));
        }
    }

    /**
     * Table view binding listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
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
        public void selectedRowBindTypeChanged(TableView tableView,
            BindType previousSelectedRowBindType) {
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
        public void selectedRowsBindTypeChanged(TableView tableView,
            BindType previousSelectedRowsBindType) {
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
     * @param tableView The source of this event.
     * @param previousTableDataKey The previous key for the table data.
     */
    default void tableDataKeyChanged(TableView tableView, String previousTableDataKey) {
    }

    /**
     * Called when a table view's table data bind type has changed.
     *
     * @param tableView The source of this event.
     * @param previousTableDataBindType The previous bind type for the table data.
     */
    default void tableDataBindTypeChanged(TableView tableView, BindType previousTableDataBindType) {
    }

    /**
     * Called when a table view's table data bind mapping has changed.
     *
     * @param tableView The source of this event.
     * @param previousTableDataBindMapping The previous bind mapping for the table data.
     */
    default void tableDataBindMappingChanged(TableView tableView,
        TableView.TableDataBindMapping previousTableDataBindMapping) {
    }

    /**
     * Called when a table view's selected row key has changed.
     *
     * @param tableView The source of this event.
     * @param previousSelectedRowKey The previous key for the selected row.
     */
    default void selectedRowKeyChanged(TableView tableView, String previousSelectedRowKey) {
    }

    /**
     * Called when a table view's selected row bind type has changed.
     *
     * @param tableView The source of this event.
     * @param previousSelectedRowBindType The previous bind type for the selected row.
     */
    default void selectedRowBindTypeChanged(TableView tableView, BindType previousSelectedRowBindType) {
    }

    /**
     * Called when a table view's selected row bind mapping has changed.
     *
     * @param tableView The source of the event.
     * @param previousSelectedRowBindMapping The previous bind mapping for the selected row.
     */
    default void selectedRowBindMappingChanged(TableView tableView,
        TableView.SelectedRowBindMapping previousSelectedRowBindMapping) {
    }

    /**
     * Called when a table view's selected rows key has changed.
     *
     * @param tableView The source of this event.
     * @param previousSelectedRowsKey The previous key for obtaining the selected rows value.
     */
    default void selectedRowsKeyChanged(TableView tableView, String previousSelectedRowsKey) {
    }

    /**
     * Called when a table view's selected rows bind type has changed.
     *
     * @param tableView The source of this event.
     * @param previousSelectedRowsBindType The previous bind type for the selected rows.
     */
    default void selectedRowsBindTypeChanged(TableView tableView,
        BindType previousSelectedRowsBindType) {
    }

    /**
     * Called when a table view's selected rows bind mapping has changed.
     *
     * @param tableView The source of this event.
     * @param previousSelectedRowsBindMapping The previous value of the bind mapping for the selected rows.
     */
    default void selectedRowsBindMappingChanged(TableView tableView,
        TableView.SelectedRowBindMapping previousSelectedRowsBindMapping) {
    }
}
