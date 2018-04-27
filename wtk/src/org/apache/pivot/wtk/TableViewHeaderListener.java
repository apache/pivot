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
import org.apache.pivot.wtk.TableViewHeader.SortMode;

/**
 * Table view header listener interface.
 */
public interface TableViewHeaderListener {
    /**
     * Table view header listeners.
     */
    public static class Listeners extends ListenerList<TableViewHeaderListener>
        implements TableViewHeaderListener {
        @Override
        public void tableViewChanged(TableViewHeader tableViewHeader, TableView previousTableView) {
            forEach(listener -> listener.tableViewChanged(tableViewHeader, previousTableView));
        }

        @Override
        public void sortModeChanged(TableViewHeader tableViewHeader, SortMode previousSortMode) {
            forEach(listener -> listener.sortModeChanged(tableViewHeader, previousSortMode));
        }
    }

    /**
     * Table view header listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TableViewHeaderListener {
        @Override
        public void tableViewChanged(TableViewHeader tableViewHeader, TableView previousTableView) {
            // empty block
        }

        @Override
        public void sortModeChanged(TableViewHeader tableViewHeader, SortMode previousSortMode) {
            // empty block
        }
    }

    /**
     * Called when a table view header's table view has changed.
     *
     * @param tableViewHeader The source of this event.
     * @param previousTableView The table view that used to be associated with this header.
     */
    default void tableViewChanged(TableViewHeader tableViewHeader, TableView previousTableView) {
    }

    /**
     * Called when a table view header's sort mode has changed.
     *
     * @param tableViewHeader The source of this event.
     * @param previousSortMode The previous sort mode for this header.
     */
    default void sortModeChanged(TableViewHeader tableViewHeader, SortMode previousSortMode) {
    }
}
