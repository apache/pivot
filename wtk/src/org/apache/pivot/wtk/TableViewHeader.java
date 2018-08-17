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
import org.apache.pivot.util.Utils;

/**
 * Component representing a table view header.
 */
public class TableViewHeader extends Component {
    /**
     * Enumeration representing a sort mode.
     */
    public enum SortMode {
        NONE, SINGLE_COLUMN, MULTI_COLUMN
    }

    /**
     * Table view header skin interface. Table view header skins must implement
     * this.
     */
    public interface Skin {
        public int getHeaderAt(int x);

        public Bounds getHeaderBounds(int index);
    }

    private TableView tableView;
    private SortMode sortMode = SortMode.NONE;

    private TableViewHeaderListener.Listeners tableViewHeaderListeners = new TableViewHeaderListener.Listeners();
    private TableViewHeaderPressListener.Listeners tableViewHeaderPressListeners =
        new TableViewHeaderPressListener.Listeners();

    public TableViewHeader() {
        this(null);
    }

    public TableViewHeader(final TableView tableView) {
        installSkin(TableViewHeader.class);
        setTableView(tableView);
    }

    @Override
    protected void setSkin(final org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, TableViewHeader.Skin.class);

        super.setSkin(skin);
    }

    public TableView getTableView() {
        return tableView;
    }

    public void setTableView(final TableView tableView) {
        TableView previousTableView = this.tableView;

        if (previousTableView != tableView) {
            this.tableView = tableView;
            tableViewHeaderListeners.tableViewChanged(this, previousTableView);
        }
    }

    public SortMode getSortMode() {
        return sortMode;
    }

    public void setSortMode(final SortMode sortMode) {
        Utils.checkNull(sortMode, "sortMode");

        SortMode previousSortMode = this.sortMode;
        if (previousSortMode != sortMode) {
            this.sortMode = sortMode;
            tableViewHeaderListeners.sortModeChanged(this, previousSortMode);
        }
    }

    public void pressHeader(final int index) {
        tableViewHeaderPressListeners.headerPressed(this, index);
    }

    /**
     * Returns the index of the header at a given location.
     *
     * @param x The x-coordinate of the header to identify.
     * @return The column index, or <tt>-1</tt> if there is no column at the
     * given x-coordinate.
     */
    public int getHeaderAt(final int x) {
        TableViewHeader.Skin tableViewHeaderSkin = (TableViewHeader.Skin) getSkin();
        return tableViewHeaderSkin.getHeaderAt(x);
    }

    /**
     * Returns the bounding area of a given header.
     *
     * @param index The index of the header.
     * @return The bounding area of the header.
     */
    public Bounds getHeaderBounds(final int index) {
        TableViewHeader.Skin tableViewHeaderSkin = (TableViewHeader.Skin) getSkin();
        return tableViewHeaderSkin.getHeaderBounds(index);
    }

    public ListenerList<TableViewHeaderListener> getTableViewHeaderListeners() {
        return tableViewHeaderListeners;
    }

    public ListenerList<TableViewHeaderPressListener> getTableViewHeaderPressListeners() {
        return tableViewHeaderPressListeners;
    }
}
