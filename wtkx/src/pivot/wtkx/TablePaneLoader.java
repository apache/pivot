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
package pivot.wtkx;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pivot.collections.ArrayList;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.TablePane;

class TablePaneLoader extends ContainerLoader {
    public static final String TABLE_PANE_TAG = "TablePane";
    public static final String ROWS_TAG = "rows";
    public static final String COLUMNS_TAG = "columns";
    public static final String CELLS_TAG = "cells";

    public static final String ROW_ATTRIBUTE = "row";
    public static final String COLUMN_ATTRIBUTE = "column";
    public static final String ROW_SPAN_ATTRIBUTE = "rowSpan";
    public static final String COLUMN_SPAN_ATTRIBUTE = "columnSpan";

    public static final String ROW_TAG = "Row";
    public static final String ROW_HEIGHT_ATTRIBUTE = "height";
    public static final String ROW_SELECTED_ATTRIBUTE = "selected";

    public static final String COLUMN_TAG = "Column";
    public static final String COLUMN_WIDTH_ATTRIBUTE = "width";
    public static final String COLUMN_SELECTED_ATTRIBUTE = "selected";

    public static final String RELATIVE_SIZE_INDICATOR = "*";

    protected Container createContainer() {
        return new TablePane();
    }

    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        TablePane tablePane = (TablePane)super.load(element, rootLoader);

        NodeList childNodes = element.getChildNodes();
        int n = childNodes.getLength();

        if (n > 0) {
            ComponentLoader componentLoader = new ComponentLoader();

            for (int i = 0; i < n; i++) {
                Node childNode = childNodes.item(i);

                if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element childElement = (Element)childNode;
                    String childTagName = childElement.getTagName();

                    if (childTagName.equals(ROWS_TAG)) {
                        ArrayList<TablePane.Row> rows = getRows(childElement);

                        for (TablePane.Row row : rows) {
                            tablePane.getRows().add(row);
                        }
                    } else if (childTagName.equals(COLUMNS_TAG)) {
                        ArrayList<TablePane.Column> columns = getColumns(childElement);

                        for (TablePane.Column column : columns) {
                            tablePane.getColumns().add(column);
                        }
                    } else if (childTagName.equals(CELLS_TAG)) {
                        NodeList cellNodes = childElement.getChildNodes();

                        for (int j = 0, m = cellNodes.getLength(); j < m; j++) {
                            Node cellNode = cellNodes.item(j);

                            if (cellNode.getNodeType() == Node.ELEMENT_NODE) {
                                Element cellElement = (Element)cellNode;

                                if (!cellElement.hasAttribute(ROW_ATTRIBUTE)) {
                                    throw new LoadException("TablePane cells must " +
                                        "contain 'row' attribute.");
                                }

                                if (!cellElement.hasAttribute(COLUMN_ATTRIBUTE)) {
                                    throw new LoadException("TablePane cells must " +
                                        "contain 'column' attribute.");
                                }

                                int row = Integer.parseInt
                                    (cellElement.getAttribute(ROW_ATTRIBUTE));
                                int column = Integer.parseInt
                                    (cellElement.getAttribute(COLUMN_ATTRIBUTE));

                                Component component = componentLoader.load(cellElement,
                                    rootLoader);

                                tablePane.setCellComponent(row, column, component);

                                if (cellElement.hasAttribute(ROW_SPAN_ATTRIBUTE)) {
                                   int rowSpan = Integer.parseInt
                                      (cellElement.getAttribute(ROW_SPAN_ATTRIBUTE));
                                   TablePane.setRowSpan(component, rowSpan);
                                }

                                if (cellElement.hasAttribute(COLUMN_SPAN_ATTRIBUTE)) {
                                   int columnSpan = Integer.parseInt
                                      (cellElement.getAttribute(COLUMN_SPAN_ATTRIBUTE));
                                   TablePane.setColumnSpan(component, columnSpan);
                                }
                            }
                        }
                    }
                }
            }
        }

        return tablePane;
    }

    private ArrayList<TablePane.Row> getRows(Element element) {
        ArrayList<TablePane.Row> rows = new ArrayList<TablePane.Row>();

        NodeList childNodes = element.getChildNodes();
        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;

                if (childElement.getTagName().equals(ROW_TAG)) {
                    int height = -1;
                    boolean relative = false;
                    boolean selected = false;

                    if (childElement.hasAttribute(ROW_HEIGHT_ATTRIBUTE)) {
                        String heightAttribute = childElement.getAttribute
                            (ROW_HEIGHT_ATTRIBUTE);

                        int j = heightAttribute.lastIndexOf(RELATIVE_SIZE_INDICATOR);
                        if (j != -1) {
                            relative = true;
                            heightAttribute = heightAttribute.substring(0, j);
                        }

                        height = Integer.parseInt(heightAttribute);
                    }

                    if (childElement.hasAttribute(ROW_SELECTED_ATTRIBUTE)) {
                        String selectedAttribute = childElement.getAttribute
                            (ROW_SELECTED_ATTRIBUTE);

                        selected = Boolean.parseBoolean(selectedAttribute);
                    }

                    rows.add(new TablePane.Row(height, relative, selected));
                }
            }
        }

        return rows;
    }

    private ArrayList<TablePane.Column> getColumns(Element element) {
        ArrayList<TablePane.Column> columns = new ArrayList<TablePane.Column>();

        NodeList childNodes = element.getChildNodes();
        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;

                if (childElement.getTagName().equals(COLUMN_TAG)) {
                    int width = -1;
                    boolean relative = false;
                    boolean selected = false;

                    if (childElement.hasAttribute(COLUMN_WIDTH_ATTRIBUTE)) {
                        String widthAttribute = childElement.getAttribute
                            (COLUMN_WIDTH_ATTRIBUTE);

                        int j = widthAttribute.lastIndexOf(RELATIVE_SIZE_INDICATOR);
                        if (j != -1) {
                            relative = true;
                            widthAttribute = widthAttribute.substring(0, j);
                        }

                        width = Integer.parseInt(widthAttribute);
                    }

                    if (childElement.hasAttribute(COLUMN_SELECTED_ATTRIBUTE)) {
                        String selectedAttribute = childElement.getAttribute
                            (COLUMN_SELECTED_ATTRIBUTE);

                        selected = Boolean.parseBoolean(selectedAttribute);
                    }

                    columns.add(new TablePane.Column(width, relative, selected));
                }
            }
        }

        return columns;
    }
}
