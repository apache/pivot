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

import pivot.util.ListenerList;
import pivot.wtk.content.TableViewHeaderDataRenderer;

/**
 * <p>Component representing a table view header.</p>
 *
 * @author gbrown
 */
public class TableViewHeader extends Component {
    /**
     * Table view header data renderer interface.
     *
     * @author gbrown
     */
    public interface DataRenderer extends Renderer {
        public void render(Object data, TableViewHeader tableViewHeader, boolean highlighted);
    }

    /**
     * <p>Table view header skin interface. Table view header skins must
     * implement this.</p>
     *
     * @author gbrown
     */
    public interface Skin extends pivot.wtk.Skin {
        public int getHeaderAt(int x);
        public Bounds getHeaderBounds(int index);
    }

    private static class TableViewHeaderListenerList extends ListenerList<TableViewHeaderListener>
        implements TableViewHeaderListener {
        public void tableViewChanged(TableViewHeader tableViewHeader,
            TableView previousTableView) {
            for (TableViewHeaderListener listener : this) {
                listener.tableViewChanged(tableViewHeader, previousTableView);
            }
        }

        public void dataRendererChanged(TableViewHeader tableViewHeader,
            TableViewHeader.DataRenderer previousDataRenderer) {
            for (TableViewHeaderListener listener : this) {
                listener.dataRendererChanged(tableViewHeader, previousDataRenderer);
            }
        }
    }

    private static class TableViewHeaderPressListenerList extends ListenerList<TableViewHeaderPressListener>
        implements TableViewHeaderPressListener {
        public void headerPressed(TableViewHeader tableViewHeader, int index) {
            for (TableViewHeaderPressListener listener : this) {
                listener.headerPressed(tableViewHeader, index);
            }
        }
    }

    private TableView tableView = null;
    private DataRenderer dataRenderer = null;

    private TableViewHeaderListenerList tableViewHeaderListeners = new TableViewHeaderListenerList();
    private TableViewHeaderPressListenerList tableViewHeaderPressListeners = new TableViewHeaderPressListenerList();

    public TableViewHeader() {
        this(null);
    }

    public TableViewHeader(TableView tableView) {
        setDataRenderer(new TableViewHeaderDataRenderer());
        installSkin(TableViewHeader.class);

        setTableView(tableView);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof TableViewHeader.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + TableViewHeader.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    public TableView getTableView() {
        return tableView;
    }

    public void setTableView(TableView tableView) {
        TableView previousTableView = this.tableView;

        if (previousTableView != tableView) {
            this.tableView = tableView;
            tableViewHeaderListeners.tableViewChanged(this, previousTableView);
        }
    }

    public DataRenderer getDataRenderer() {
        return dataRenderer;
    }

    public void setDataRenderer(DataRenderer dataRenderer) {
        if (dataRenderer == null) {
            throw new IllegalArgumentException("dataRenderer is null.");
        }

        DataRenderer previousDataRenderer = this.dataRenderer;
        if (previousDataRenderer != dataRenderer) {
            this.dataRenderer = dataRenderer;
            tableViewHeaderListeners.dataRendererChanged(this, previousDataRenderer);
        }
    }

    public void pressHeader(int index) {
        tableViewHeaderPressListeners.headerPressed(this, index);
    }

    /**
     * Returns the index of the header at a given location.
     *
     * @param x
     * The x-coordinate of the header to identify.
     *
     * @return
     * The column index, or <tt>-1</tt> if there is no column at the given
     * x-coordinate.
     */
    public int getHeaderAt(int x) {
        TableViewHeader.Skin tableViewHeaderSkin = (TableViewHeader.Skin)getSkin();
        return tableViewHeaderSkin.getHeaderAt(x);
    }

    /**
     * Returns the bounding area of a given header.
     *
     * @param index
     * The index of the header.
     *
     * @return
     * The bounding area of the header.
     */
    public Bounds getHeaderBounds(int index) {
        TableViewHeader.Skin tableViewHeaderSkin = (TableViewHeader.Skin)getSkin();
        return tableViewHeaderSkin.getHeaderBounds(index);
    }

    public ListenerList<TableViewHeaderListener> getTableViewHeaderListeners() {
        return tableViewHeaderListeners;
    }

    public ListenerList<TableViewHeaderPressListener> getTableViewHeaderPressListeners() {
        return tableViewHeaderPressListeners;
    }
}
