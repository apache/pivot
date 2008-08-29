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
package pivot.demos.tables;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableViewSelectionListener;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class FixedColumnTable implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        Component content =
            (Component)wtkxSerializer.readObject(getClass().getResource("fixed_column_table.wtkx"));

        // Get references to the table views and table view headers
        final TableView primaryTableView =
            (TableView)wtkxSerializer.getObjectByName("primaryTableView");
        final TableViewHeader primaryTableViewHeader =
            (TableViewHeader)wtkxSerializer.getObjectByName("primaryTableViewHeader");

        final TableView fixedTableView =
            (TableView)wtkxSerializer.getObjectByName("fixedTableView");
        final TableViewHeader fixedTableViewHeader =
            (TableViewHeader)wtkxSerializer.getObjectByName("fixedTableViewHeader");

        // Keep selection state in sync
        primaryTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            public void selectionChanged(TableView tableView) {
                int selectedIndex = tableView.getSelectedIndex();
                if (fixedTableView.getSelectedIndex() != selectedIndex) {
                    fixedTableView.setSelectedIndex(selectedIndex);
                }
            }
        });

        fixedTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            public void selectionChanged(TableView tableView) {
                int selectedIndex = tableView.getSelectedIndex();
                if (primaryTableView.getSelectedIndex() != selectedIndex) {
                    primaryTableView.setSelectedIndex(selectedIndex);
                }
            }
        });

        // Keep header state in sync
        primaryTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler() {
            public void headerPressed(TableViewHeader tableViewHeader, int index) {
                super.headerPressed(tableViewHeader, index);

                TableView.ColumnSequence columns = fixedTableView.getColumns();
                for (int i = 0, n = columns.getLength(); i < n; i++) {
                    TableView.Column column = columns.get(i);
                    column.setSortDirection(null);
                }
            }
        });

        fixedTableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler() {
            public void headerPressed(TableViewHeader tableViewHeader, int index) {
                super.headerPressed(tableViewHeader, index);

                TableView.ColumnSequence columns = primaryTableView.getColumns();
                for (int i = 0, n = columns.getLength(); i < n; i++) {
                    TableView.Column column = columns.get(i);
                    column.setSortDirection(null);
                }
            }
        });

        // Open the window
        window = new Window(content);
        window.setTitle("Fixed Column Table Demo");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
