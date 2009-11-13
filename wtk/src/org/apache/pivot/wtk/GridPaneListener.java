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

/**
 * Grid pane listener interface.
 */
public interface GridPaneListener {
    /**
     * Grid pane listener adapter.
     */
    public static class Adapter implements GridPaneListener {
        @Override
        public void rowInserted(GridPane GridPane, int index) {
        }

        @Override
        public void rowsRemoved(GridPane GridPane, int index, Sequence<GridPane.Row> rows) {
        }

        @Override
        public void rowHighlightedChanged(GridPane.Row row) {
        }

        @Override
        public void columnInserted(GridPane GridPane, int index) {
        }

        @Override
        public void columnsRemoved(GridPane GridPane, int index,
            Sequence<GridPane.Column> columns) {
        }

        @Override
        public void columnHighlightedChanged(GridPane.Column column) {
        }

        @Override
        public void cellInserted(GridPane.Row row, int column) {
        }

        @Override
        public void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed) {
        }

        @Override
        public void cellUpdated(GridPane.Row row, int column, Component previousComponent) {
        }
    }

    /**
     * Called when a row has been inserted into a grid pane.
     *
     * @param GridPane
     * @param index
     */
    public void rowInserted(GridPane GridPane, int index);

    /**
     * Called when rows have been removed from a grid pane.
     *
     * @param GridPane
     * @param index
     * @param rows
     */
    public void rowsRemoved(GridPane GridPane, int index, Sequence<GridPane.Row> rows);

    /**
     * Called when a row's highlighted state has changed.
     *
     * @param row
     */
    public void rowHighlightedChanged(GridPane.Row row);

    /**
     * Called when a column has been inserted into a grid pane.
     *
     * @param GridPane
     * @param index
     */
    public void columnInserted(GridPane GridPane, int index);

    /**
     * Called when column's have been removed from a grid pane.
     *
     * @param GridPane
     * @param index
     * @param columns
     */
    public void columnsRemoved(GridPane GridPane, int index, Sequence<GridPane.Column> columns);

    /**
     * Called when a column's highlighted state has changed.
     *
     * @param column
     */
    public void columnHighlightedChanged(GridPane.Column column);

    /**
     * Called when a cell has been inserted into a grid pane.
     *
     * @param row
     * @param column
     */
    public void cellInserted(GridPane.Row row, int column);

    /**
     * Called when cell's have been removed from a grid pane.
     *
     * @param row
     * @param column
     * @param removed
     */
    public void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed);

    /**
     * Called when a cell has been updated in a grid pane.
     *
     * @param row
     * @param column
     * @param previousComponent
     */
    public void cellUpdated(GridPane.Row row, int column, Component previousComponent);
}
