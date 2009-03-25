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

import pivot.collections.List;

/**
 * Table view listener interface.
 *
 * @author gbrown
 */
public interface TableViewListener {
    /**
     * Called when a table view's table data has changed.
     *
     * @param tableView
     * @param previousTableData
     */
    public void tableDataChanged(TableView tableView, List<?> previousTableData);

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
}
