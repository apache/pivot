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

public interface TableViewColumnListener {
    public void columnInserted(TableView tableView, int index);
    public void columnsRemoved(TableView tableView, int index, Sequence<TableView.Column> columns);

    public void columnNameChanged(TableView tableView, int index, String previousName);
    public void columnHeaderDataChanged(TableView tableView, int index, Object previousHeaderData);
    public void columnWidthChanged(TableView tableView, int index, int previousWidth, boolean previousRelative);
    public void columnSortDirectionChanged(TableView tableView, int index, SortDirection previousSortDirection);
    public void columnFilterChanged(TableView tableView, int index, Object previousFilter);
    public void columnCellRendererChanged(TableView tableView, int index, TableView.CellRenderer previousCellRenderer);
}
