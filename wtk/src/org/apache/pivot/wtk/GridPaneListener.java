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
        public void columnCountChanged(GridPane gridPane, int previousColumnCount) {
            // empty block
        }

        @Override
        public void rowInserted(GridPane gridPane, int index) {
            // empty block
        }

        @Override
        public void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows) {
            // empty block
        }

        @Override
        public void cellInserted(GridPane.Row row, int column) {
            // empty block
        }

        @Override
        public void cellsRemoved(GridPane.Row row, int column, Sequence<Component> removed) {
            // empty block
        }

        @Override
        public void cellUpdated(GridPane.Row row, int column, Component previousComponent) {
            // empty block
        }
    }

    /**
     * Called when a grid pane's column count has changed.
     *
     * @param gridPane
     * @param previousColumnCount
     */
    public void columnCountChanged(GridPane gridPane, int previousColumnCount);

    /**
     * Called when a row has been inserted into a grid pane.
     *
     * @param gridPane
     * @param index
     */
    public void rowInserted(GridPane gridPane, int index);

    /**
     * Called when rows have been removed from a grid pane.
     *
     * @param gridPane
     * @param index
     * @param rows
     */
    public void rowsRemoved(GridPane gridPane, int index, Sequence<GridPane.Row> rows);

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
