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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * Table view selection listener interface.
 */
public interface TableViewSelectionListener {
    /**
     * Table view selection listeners.
     */
    public static class Listeners extends ListenerList<TableViewSelectionListener>
        implements TableViewSelectionListener {
        @Override
        public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
            forEach(listener -> listener.selectedRangeAdded(tableView, rangeStart, rangeEnd));
        }

        @Override
        public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
            forEach(listener -> listener.selectedRangeRemoved(tableView, rangeStart, rangeEnd));
        }

        @Override
        public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelection) {
            forEach(listener -> listener.selectedRangesChanged(tableView, previousSelection));
        }

        @Override
        public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
            forEach(listener -> listener.selectedRowChanged(tableView, previousSelectedRow));
        }
    }

    /**
     * Table view selection listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements TableViewSelectionListener {
        @Override
        public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
            // empty block
        }

        @Override
        public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
            // empty block
        }

        @Override
        public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
            // empty block
        }

        @Override
        public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
            // empty block
        }
    }

    /**
     * Called when a range has been added to a table view's selection.
     *
     * @param tableView The source of the event.
     * @param rangeStart The start index of the range that was added, inclusive.
     * @param rangeEnd The end index of the range that was added, inclusive.
     */
    default void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
    }

    /**
     * Called when a range has been removed from a table view's selection.
     *
     * @param tableView The source of the event.
     * @param rangeStart The start index of the range that was removed,
     * inclusive.
     * @param rangeEnd The end index of the range that was removed, inclusive.
     */
    default void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
    }

    /**
     * Called when a table view's selection state has been reset.
     *
     * @param tableView The source of the event.
     * @param previousSelectedRanges If the selection changed directly, contains
     * the ranges that were previously selected. If the selection changed
     * indirectly as a result of a model change, contains the current selection.
     * Otherwise, contains <tt>null</tt>.
     */
    default void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
    }

    /**
     * Called when a table view's selected item has changed.
     *
     * @param tableView The source of the event.
     * @param previousSelectedRow The row that was previously selected.
     */
    default void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
    }
}
