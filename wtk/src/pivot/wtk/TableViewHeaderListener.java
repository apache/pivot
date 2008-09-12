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

/**
 * <p>Table view header listener interface.</p>
 *
 * @author gbrown
 */
public interface TableViewHeaderListener {
    /**
     * Called when a table view header's table view has changed.
     *
     * @param tableViewHeader
     * @param previousTableView
     */
    public void tableViewChanged(TableViewHeader tableViewHeader,
        TableView previousTableView);

    /**
     * Called when a table view header's data renderer has changed.
     *
     * @param tableViewHeader
     * @param previousDataRenderer
     */
    public void dataRendererChanged(TableViewHeader tableViewHeader,
        TableViewHeader.DataRenderer previousDataRenderer);
}
