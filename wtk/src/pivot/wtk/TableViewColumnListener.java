/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.collections.Sequence;

/**
 * <p>Table view column listener interface.</p>
 *
 * @author gbrown
 */
public interface TableViewColumnListener {
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
     * Called when a column's width has changed.
     *
     * @param column
     * @param previousWidth
     * @param previousRelative
     */
    public void columnWidthChanged(TableView.Column column, int previousWidth, boolean previousRelative);

    /**
     * Called when a column's sort direction has changed.
     *
     * @param column
     * @param previousSortDirection
     */
    public void columnSortDirectionChanged(TableView.Column column, SortDirection previousSortDirection);

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
    public void columnCellRendererChanged(TableView.Column column, TableView.CellRenderer previousCellRenderer);
}
