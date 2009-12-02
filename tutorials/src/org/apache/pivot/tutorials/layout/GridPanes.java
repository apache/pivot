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
package org.apache.pivot.tutorials.layout;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class GridPanes implements Application {
    private class ContextMenuHandler extends MenuHandler.Adapter {
        private int x = -1;
        private int y = -1;

        @Override
        public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
            this.x = x;
            this.y = y;

            // Add our menu sections
            menu.getSections().add(rowSection);
            menu.getSections().add(columnSection);

            return false;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    private Window window = null;
    private GridPane gridPane = null;

    private Menu.Section rowSection = null;
    private Menu.Section columnSection = null;

    private ContextMenuHandler contextMenuHandler = new ContextMenuHandler();

    public GridPanes() {
        Action.NamedActionDictionary namedActions = Action.getNamedActions();

        namedActions.put("insertRow", new Action() {
            @Override
            public void perform() {
                // Create and insert a new row
                GridPane.Row row = new GridPane.Row();

                // Populate the row with the expected content
                for (int i = 0, n = gridPane.getColumns().getLength(); i < n; i++) {
                    Panel panel = new Panel();
                    panel.getStyles().put("backgroundColor", "#dddcd5");
                    row.add(panel);
                }

                int rowIndex = gridPane.getRowAt(contextMenuHandler.getY());
                gridPane.getRows().insert(row, rowIndex);
            }
        });

        namedActions.put("removeRow", new Action() {
            @Override
            public void perform() {
                ArrayList<String> options = new ArrayList<String>("OK", "Cancel");
                String message = "Remove Row?";
                Label body = new Label("Are you sure you want to remove the row?");
                body.getStyles().put("wrapText", true);

                final Prompt prompt = new Prompt(MessageType.QUESTION, message, options, body);
                prompt.setSelectedOption(0);

                prompt.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (prompt.getResult() && prompt.getSelectedOption() == 0) {
                            int rowIndex = gridPane.getRowAt(contextMenuHandler.getY());
                            gridPane.getRows().remove(rowIndex, 1);
                        }
                    }
                });
            }
        });

        namedActions.put("insertColumn", new Action() {
            @Override
            public void perform() {
                // Create and insert a new column
                GridPane.Column column = new GridPane.Column();
                int columnIndex = gridPane.getColumnAt(contextMenuHandler.getX());
                gridPane.getColumns().insert(column, columnIndex);

                // Populate the column with the expected content
                GridPane.RowSequence rows = gridPane.getRows();
                for (int i = 0, n = rows.getLength(); i < n; i++) {
                    Panel panel = new Panel();
                    panel.getStyles().put("backgroundColor", "#dddcd5");
                    rows.get(i).insert(panel, columnIndex);
                }
            }
        });

        namedActions.put("removeColumn", new Action() {
            @Override
            public void perform() {
                ArrayList<String> options = new ArrayList<String>("OK", "Cancel");
                String message = "Remove Column?";
                Label body = new Label("Are you sure you want to remove the column?");
                body.getStyles().put("wrapText", true);

                final Prompt prompt = new Prompt(MessageType.QUESTION, message, options, body);
                prompt.setSelectedOption(0);

                prompt.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (prompt.getResult() && prompt.getSelectedOption() == 0) {
                            int columnIndex = gridPane.getColumnAt(contextMenuHandler.getX());

                            // Remove the component at that index from each row
                            for (GridPane.Row row : gridPane.getRows()) {
                                row.remove(columnIndex, 1);
                            }

                            gridPane.getColumns().remove(columnIndex, 1);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "grid_panes.wtkx");

        gridPane = (GridPane)wtkxSerializer.get("gridPane");
        rowSection = (Menu.Section)wtkxSerializer.get("rowSection");
        columnSection = (Menu.Section)wtkxSerializer.get("columnSection");

        gridPane.setMenuHandler(contextMenuHandler);

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
        // No-op
    }

    @Override
    public void resume() {
        // No-op
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(GridPanes.class, args);
    }
}
