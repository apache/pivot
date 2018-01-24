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
 * Table view header press listener.
 */
public interface TableViewHeaderPressListener {
    /**
     * Table view header press listeners.
     */
    public static class Listeners extends ListenerList<TableViewHeaderPressListener>
        implements TableViewHeaderPressListener {
        @Override
        public void headerPressed(TableViewHeader tableViewHeader, int index) {
            forEach(listener -> listener.headerPressed(tableViewHeader, index));
        }
    }

    /**
     * Called when a table view header has been pressed.
     *
     * @param tableViewHeader The source of this event.
     * @param index The location in this header that was pressed.
     */
    public void headerPressed(TableViewHeader tableViewHeader, int index);
}
