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

import java.awt.Color;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ColorItem;
import org.apache.pivot.wtkx.WTKXSerializer;

public class TablePanes implements Application {
    private class ContextMenuHandler extends MenuHandler.Adapter {
        private int x = -1;
        private int y = -1;

        @Override
        public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
            this.x = x;
            this.y = y;

            int rowIndex = tablePane.getRowAt(y);
            int columnIndex = tablePane.getColumnAt(x);

            Action.NamedActionDictionary namedActions = Action.getNamedActions();

            namedActions.get("configureCell").setEnabled(rowIndex > 0 && columnIndex > 0);
            namedActions.get("configureRow").setEnabled(rowIndex > 0);
            namedActions.get("insertRow").setEnabled(rowIndex > 0);
            namedActions.get("removeRow").setEnabled(rowIndex > 0);
            namedActions.get("configureColumn").setEnabled(columnIndex > 0);
            namedActions.get("insertColumn").setEnabled(columnIndex > 0);
            namedActions.get("removeColumn").setEnabled(columnIndex > 0);

            menu.getSections().add(cellSection);
            menu.getSections().add(rowSection);
            menu.getSections().add(columnSection);

            return false;
        }

        public TablePane.Row getRow() {
            TablePane.Row row = null;

            int rowIndex = tablePane.getRowAt(y);

            if (rowIndex >= 0) {
                row = tablePane.getRows().get(rowIndex);
            }

            return row;
        }

        public TablePane.Column getColumn() {
            TablePane.Column column = null;

            int columnIndex = tablePane.getColumnAt(x);

            if (columnIndex >= 0) {
                column = tablePane.getColumns().get(columnIndex);
            }

            return column;
        }

        public Component getCellComponent() {
            Component component = null;

            int rowIndex = tablePane.getRowAt(y);
            int columnIndex = tablePane.getColumnAt(x);

            if (rowIndex > 0
                && columnIndex > 0) {
                component = tablePane.getCellComponent(rowIndex, columnIndex);
            }

            return component;
        }
    }

    private Window window = null;
    private TablePane tablePane = null;

    private Menu.Section cellSection = null;
    private Menu.Section rowSection = null;
    private Menu.Section columnSection = null;

    private ContextMenuHandler contextMenuHandler = new ContextMenuHandler();

    public TablePanes() {
        Action.NamedActionDictionary namedActions = Action.getNamedActions();

        namedActions.put("configureCell", new Action() {
            @Override
            public void perform() {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                Sheet sheet;

                try {
                    sheet = (Sheet)wtkxSerializer.readObject(this, "table_panes_configure_cell.wtkx");
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                final Component cellComponent = contextMenuHandler.getCellComponent();

                final Spinner rowSpanSpinner = (Spinner)wtkxSerializer.get("rowSpan");
                final Spinner columnSpanSpinner = (Spinner)wtkxSerializer.get("columnSpan");
                final ListButton backgroundColorListButton = (ListButton)wtkxSerializer.get("backgroundColor");

                // Pre-populate the form
                Color backgroundColor = (Color)cellComponent.getStyles().get("backgroundColor");
                rowSpanSpinner.setSelectedItem(TablePane.getRowSpan(cellComponent));
                columnSpanSpinner.setSelectedItem(TablePane.getColumnSpan(cellComponent));
                backgroundColorListButton.setSelectedItem(new ColorItem(backgroundColor));

                sheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (sheet.getResult()) {
                            // Update the component based on the form input
                            TablePane.setRowSpan(cellComponent,
                                (Integer)rowSpanSpinner.getSelectedItem());
                            TablePane.setColumnSpan(cellComponent,
                                (Integer)columnSpanSpinner.getSelectedItem());

                            ColorItem colorItem = (ColorItem)backgroundColorListButton.getSelectedItem();
                            cellComponent.getStyles().put("backgroundColor", colorItem.getColor());
                        }
                    }
                });
            }
        });

        namedActions.put("configureRow", new Action() {
            @Override
            public void perform() {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                Sheet sheet;

                try {
                    sheet = (Sheet)wtkxSerializer.readObject(this, "table_panes_configure_row.wtkx");
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                    }
                });
            }
        });

        namedActions.put("insertRow", new Action() {
            @Override
            public void perform() {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                Sheet sheet;

                try {
                    sheet = (Sheet)wtkxSerializer.readObject(this, "table_panes_configure_row.wtkx");
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                    }
                });
            }
        });

        namedActions.put("removeRow", new Action() {
            @Override
            public void perform() {
            }
        });

        namedActions.put("configureColumn", new Action() {
            @Override
            public void perform() {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                Sheet sheet;

                try {
                    sheet = (Sheet)wtkxSerializer.readObject(this, "table_panes_configure_column.wtkx");
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                    }
                });
            }
        });

        namedActions.put("insertColumn", new Action() {
            @Override
            public void perform() {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                Sheet sheet;

                try {
                    sheet = (Sheet)wtkxSerializer.readObject(this, "table_panes_configure_column.wtkx");
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(window, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                    }
                });
            }
        });

        namedActions.put("removeColumn", new Action() {
            @Override
            public void perform() {
            }
        });
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "table_panes.wtkx");

        tablePane = (TablePane)wtkxSerializer.get("tablePane");
        cellSection = (Menu.Section)wtkxSerializer.get("cellSection");
        rowSection = (Menu.Section)wtkxSerializer.get("rowSection");
        columnSection = (Menu.Section)wtkxSerializer.get("columnSection");

        tablePane.setMenuHandler(contextMenuHandler);

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
        DesktopApplicationContext.main(TablePanes.class, args);
    }
}
