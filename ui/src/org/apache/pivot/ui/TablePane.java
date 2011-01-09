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
package org.apache.pivot.ui;

import org.apache.pivot.bxml.DefaultProperty;
import org.apache.pivot.scene.Group;
import org.apache.pivot.util.ObservableList;

/**
 * Container that arranges components in a two-dimensional grid, optionally
 * spanning multiple rows and columns.
 */
// TODO Not abstract; just for prototyping
public abstract class TablePane extends Component {
    public static class Column {
        private int preferredWidth = -1;
        private boolean relative = false;

        public int getPreferredWidth() {
            return preferredWidth;
        }

        public void setPreferredWidth(int width) {
            this.preferredWidth = width;

            // TODO Fire event
        }

        public boolean isRelative() {
            return relative;
        }

        public void setRelative(boolean relative) {
            this.relative = relative;

            // TODO Fire event
        }

        // TODO
    }

    // TODO Not abstract
    @DefaultProperty("cells")
    public abstract static class Row extends Group {
        private boolean relative = false;

        public boolean isRelative() {
            return relative;
        }

        public void setRelative(boolean relative) {
            this.relative = relative;

            // TODO Fire event
        }

        public ObservableList<Cell> getCells() {
            // TODO
            return null;
        }

        // TODO
    }

    // TODO Not abstract
    @DefaultProperty("content")
    public static class Cell extends Group {
        private Component content;
        private int columnSpan = 1;
        private int rowSpan = 1;

        public Component getContent() {
            return content;
        }

        public void setContent(Component content) {
            this.content = content;

            // TODO Fire event
        }

        public int getColumnSpan() {
            return columnSpan;
        }

        public void setColumnSpan(int columnSpan) {
            this.columnSpan = columnSpan;

            // TODO Fire event
        }

        public int getRowSpan() {
            return rowSpan;
        }

        public void setRowSpan(int rowSpan) {
            this.rowSpan = rowSpan;

            // TODO Fire event
        }

        // TODO?
    }

    public ObservableList<Column> getColumns() {
        // TODO
        return null;
    }

    public ObservableList<Column> getRows() {
        // TODO
        return null;
    }

    // TODO
}
