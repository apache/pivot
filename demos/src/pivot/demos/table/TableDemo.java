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
package pivot.demos.table;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.ScrollPane;
import pivot.wtk.TableView;
import pivot.wtk.TableViewSelectionListener;
import pivot.wtk.Viewport;
import pivot.wtk.ViewportListener;
import pivot.wtk.Window;
import pivot.wtk.content.TableRow;
import pivot.wtkx.WTKXSerializer;

public class TableDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        Component content =
            (Component)wtkxSerializer.readObject(getClass().getResource("table_demo.wtkx"));

        ArrayList<TableRow> tableData = new ArrayList<TableRow>();
        for (int i = 0; i < 10; i++) {
            TableRow tableRow = new TableRow();
            tableRow.put("a", i * 1);
            tableRow.put("b", i * 2);
            tableRow.put("c", i * 3);
            tableRow.put("d", i * 4);
            tableRow.put("e", i * 5);
            tableRow.put("f", i * 6);
            tableData.add(tableRow);
        }

        final TableView primaryTableView =
            (TableView)wtkxSerializer.getObjectByName("fixedColumnTable.primaryTableView");
        primaryTableView.setTableData(tableData);
        primaryTableView.getStyles().put("inactiveSelectionColor",
            primaryTableView.getStyles().get("selectionColor"));
        primaryTableView.getStyles().put("inactiveSelectionBackgroundColor",
            primaryTableView.getStyles().get("selectionBackgroundColor"));
        primaryTableView.getStyles().put("showHighlight", false);

        final TableView fixedTableView =
            (TableView)wtkxSerializer.getObjectByName("fixedColumnTable.fixedTableView");
        fixedTableView.setTableData(tableData);
        fixedTableView.getStyles().put("inactiveSelectionColor",
            fixedTableView.getStyles().get("selectionColor"));
        fixedTableView.getStyles().put("inactiveSelectionBackgroundColor",
            fixedTableView.getStyles().get("selectionBackgroundColor"));
        fixedTableView.getStyles().put("showHighlight", false);

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

        final ScrollPane primaryScrollPane =
            (ScrollPane)wtkxSerializer.getObjectByName("fixedColumnTable.primaryScrollPane");

        final ScrollPane fixedScrollPane =
            (ScrollPane)wtkxSerializer.getObjectByName("fixedColumnTable.fixedScrollPane");

        primaryScrollPane.getViewportListeners().add(new ViewportListener() {
            public void scrollTopChanged(Viewport scrollPane, int previousScrollTop) {
                fixedScrollPane.setScrollTop(scrollPane.getScrollTop());
            }

            public void scrollLeftChanged(Viewport scrollPane, int previousScrollLeft) {
                fixedScrollPane.setScrollLeft(scrollPane.getScrollLeft());
            }

            public void viewChanged(Viewport scrollPane, Component previousView) {
                // No-op
            }
        });

        fixedScrollPane.getViewportListeners().add(new ViewportListener() {
            public void scrollTopChanged(Viewport scrollPane, int previousScrollTop) {
                primaryScrollPane.setScrollTop(scrollPane.getScrollTop());
            }

            public void scrollLeftChanged(Viewport scrollPane, int previousScrollLeft) {
                primaryScrollPane.setScrollLeft(scrollPane.getScrollLeft());
            }

            public void viewChanged(Viewport scrollPane, Component previousView) {
                // No-op
            }
        });

        window = new Window(content);
        window.setTitle("Table Demo");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
