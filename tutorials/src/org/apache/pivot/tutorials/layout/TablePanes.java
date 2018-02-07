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

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Panel;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;

public class TablePanes extends Window implements Bindable {
    private class ContextMenuHandler implements MenuHandler {
        private int x = -1;
        private int y = -1;

        @Override
        public boolean configureContextMenu(Component component, Menu menu, int xArgument,
            int yArgument) {
            this.x = xArgument;
            this.y = yArgument;

            // Set the enabled state of actions based on where the user clicked
            Action.NamedActionDictionary namedActions = Action.getNamedActions();

            int rowIndex = tablePane.getRowAt(yArgument);
            int columnIndex = tablePane.getColumnAt(xArgument);

            namedActions.get("configureCell").setEnabled(rowIndex > 0 && columnIndex > 0);
            namedActions.get("configureRow").setEnabled(rowIndex > 0);
            namedActions.get("insertRow").setEnabled(rowIndex > 0);
            namedActions.get("removeRow").setEnabled(rowIndex > 0);
            namedActions.get("configureColumn").setEnabled(columnIndex > 0);
            namedActions.get("insertColumn").setEnabled(columnIndex > 0);
            namedActions.get("removeColumn").setEnabled(columnIndex > 0);

            // Add our menu sections
            menu.getSections().add(cellSection);
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

    private TablePane tablePane = null;

    private Menu.Section cellSection = null;
    private Menu.Section rowSection = null;
    private Menu.Section columnSection = null;

    private ContextMenuHandler contextMenuHandler = new ContextMenuHandler();

    public TablePanes() {
        Action.NamedActionDictionary namedActions = Action.getNamedActions();

        namedActions.put("configureCell", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                Sheet sheet;

                // Make the cell component available to script blocks
                int rowIndex = tablePane.getRowAt(contextMenuHandler.getY());
                int columnIndex = tablePane.getColumnAt(contextMenuHandler.getX());
                Component component = tablePane.getCellComponent(rowIndex, columnIndex);
                bxmlSerializer.getNamespace().put("component", component);

                try {
                    sheet = (Sheet) bxmlSerializer.readObject(TablePanes.class,
                        "table_panes_configure_cell.bxml");
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(TablePanes.this);
            }
        });

        namedActions.put("configureRow", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                Sheet sheet;

                // Make the selected row available to script blocks
                int rowIndex = tablePane.getRowAt(contextMenuHandler.getY());
                TablePane.Row row = tablePane.getRows().get(rowIndex);
                bxmlSerializer.getNamespace().put("row", row);

                try {
                    sheet = (Sheet) bxmlSerializer.readObject(TablePanes.class,
                        "table_panes_configure_row.bxml");
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(TablePanes.this);
            }
        });

        namedActions.put("insertRow", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                Sheet sheet;

                // Create and insert a new row
                TablePane.Row row = new TablePane.Row();
                int rowIndex = tablePane.getRowAt(contextMenuHandler.getY());
                tablePane.getRows().insert(row, rowIndex);

                // Populate the row with the expected content
                row.add(new Label("-1"));
                for (int i = 1, n = tablePane.getColumns().getLength(); i < n; i++) {
                    Panel panel = new Panel();
                    panel.getStyles().put(Style.backgroundColor, "#dddcd5");
                    row.add(panel);
                }

                // Make the new row available to script blocks
                bxmlSerializer.getNamespace().put("row", row);

                try {
                    sheet = (Sheet) bxmlSerializer.readObject(TablePanes.class,
                        "table_panes_configure_row.bxml");
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(TablePanes.this);
            }
        });

        namedActions.put("removeRow", new Action() {
            @Override
            public void perform(Component source) {
                ArrayList<String> options = new ArrayList<>("OK", "Cancel");
                String message = "Remove Row?";
                Label body = new Label("Are you sure you want to remove the row?");
                body.getStyles().put(Style.wrapText, true);

                final Prompt prompt = new Prompt(MessageType.QUESTION, message, options, body);
                prompt.setSelectedOptionIndex(0);

                prompt.open(TablePanes.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (prompt.getResult() && prompt.getSelectedOptionIndex() == 0) {
                            int rowIndex = tablePane.getRowAt(contextMenuHandler.getY());
                            tablePane.getRows().remove(rowIndex, 1);
                        }
                    }
                });
            }
        });

        namedActions.put("configureColumn", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                Sheet sheet;

                // Make the selected column available to script blocks
                int columnIndex = tablePane.getColumnAt(contextMenuHandler.getX());
                TablePane.Column column = tablePane.getColumns().get(columnIndex);
                bxmlSerializer.getNamespace().put("column", column);

                try {
                    sheet = (Sheet) bxmlSerializer.readObject(TablePanes.class,
                        "table_panes_configure_column.bxml");
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(TablePanes.this);
            }
        });

        namedActions.put("insertColumn", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                Sheet sheet;

                // Create and insert a new column
                TablePane.Column column = new TablePane.Column();
                int columnIndex = tablePane.getColumnAt(contextMenuHandler.getX());
                tablePane.getColumns().insert(column, columnIndex);

                // Populate the column with the expected content
                TablePane.RowSequence rows = tablePane.getRows();
                rows.get(0).insert(new Label("-1"), columnIndex);
                for (int i = 1, n = rows.getLength(); i < n; i++) {
                    Panel panel = new Panel();
                    panel.getStyles().put(Style.backgroundColor, "#dddcd5");
                    rows.get(i).insert(panel, columnIndex);
                }

                // Make the new column available to script blocks
                bxmlSerializer.getNamespace().put("column", column);

                try {
                    sheet = (Sheet) bxmlSerializer.readObject(TablePanes.class,
                        "table_panes_configure_column.bxml");
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                }

                sheet.open(TablePanes.this);
            }
        });

        namedActions.put("removeColumn", new Action() {
            @Override
            public void perform(Component source) {
                ArrayList<String> options = new ArrayList<>("OK", "Cancel");
                String message = "Remove Column?";
                Label body = new Label("Are you sure you want to remove the column?");
                body.getStyles().put(Style.wrapText, true);

                final Prompt prompt = new Prompt(MessageType.QUESTION, message, options, body);
                prompt.setSelectedOptionIndex(0);

                prompt.open(TablePanes.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (prompt.getResult() && prompt.getSelectedOptionIndex() == 0) {
                            int columnIndex = tablePane.getColumnAt(contextMenuHandler.getX());

                            // Remove the component at that index from each row
                            for (TablePane.Row row : tablePane.getRows()) {
                                row.remove(columnIndex, 1);
                            }

                            tablePane.getColumns().remove(columnIndex, 1);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        tablePane = (TablePane) namespace.get("tablePane");
        cellSection = (Menu.Section) namespace.get("cellSection");
        rowSection = (Menu.Section) namespace.get("rowSection");
        columnSection = (Menu.Section) namespace.get("columnSection");

        tablePane.setMenuHandler(contextMenuHandler);
    }
}
