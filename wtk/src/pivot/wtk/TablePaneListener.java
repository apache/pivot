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

public interface TablePaneListener {
    // Row methods
    public void rowInserted(TablePane tablePane, int index);
    public void rowsRemoved(TablePane tablePane, int index,
        Sequence<TablePane.Row> rows);
    public void rowHeightChanged(TablePane.Row row, int previousHeight,
        boolean previousRelative);
    public void rowSelectedChanged(TablePane.Row row);

    // Column methods
    public void columnInserted(TablePane tablePane, int index);
    public void columnsRemoved(TablePane tablePane, int index,
        Sequence<TablePane.Column> columns);
    public void columnWidthChanged(TablePane.Column column, int previousWidth,
        boolean previousRelative);
    public void columnSelectedChanged(TablePane.Column column);

    // Cell methods
    public void cellInserted(TablePane.Row row, int column);
    public void cellsRemoved(TablePane.Row row, int column,
        Sequence<Component> removed);
    public void cellUpdated(TablePane.Row row, int column,
        Component previousComponent);
}
