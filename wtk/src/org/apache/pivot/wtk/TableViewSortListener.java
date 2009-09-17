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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;

/**
 * Table view sort listener interface.
 */
public interface TableViewSortListener {
    /**
     * Called when a sort has been added to a table view.
     *
     * @param tableView
     * @param columnName
     */
    public void sortAdded(TableView tableView, String columnName);

    /**
     * Called when a sort has been updated in a table view.
     *
     * @param tableView
     * @param columnName
     * @param previousSortDirection
     */
    public void sortUpdated(TableView tableView, String columnName,
        SortDirection previousSortDirection);

    /**
     * Called when a sort has been removed from a table view.
     *
     * @param tableView
     * @param columnName
     * @param sortDirection
     */
    public void sortRemoved(TableView tableView, String columnName,
        SortDirection sortDirection);

    /**
     * Called when a table view's sort has changed.
     *
     * @param tableView
     * @param previousSort
     */
    public void sortChanged(TableView tableView,
        Sequence<Dictionary.Pair<String, SortDirection>> previousSort);
}
