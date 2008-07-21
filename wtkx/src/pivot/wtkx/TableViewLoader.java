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

import java.io.StringReader;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Component;
import pivot.wtk.TableView;

class TableViewLoader extends Loader {
    public static final String TABLE_VIEW_TAG = "TableView";

    public static final String SELECT_MODE_ATTRIBUTE = "selectMode";
    public static final String SELECTED_INDEX_ATTRIBUTE = "selectedIndex";
    public static final String SELECTED_INDEXES_ATTRIBUTE = "selectedIndexes";
    public static final String DISABLED_ROWS_ATTRIBUTE = "disabledRows";

    public static final String COLUMNS_TAG = "columns";
    public static final String COLUMN_TAG = "Column";
    public static final String COLUMN_NAME_ATTRIBUTE = "name";
    public static final String COLUMN_WIDTH_ATTRIBUTE = "width";
    public static final String COLUMN_HEADER_DATA_ATTRIBUTE = "headerData";
    public static final String COLUMN_CELL_RENDERER_ATTRIBUTE = "cellRenderer";
    public static final String COLUMN_CELL_RENDERER_STYLES_ATTRIBUTE = "cellRendererStyles";
    public static final String COLUMN_CELL_RENDERER_PROPERTIES_ATTRIBUTE = "cellRendererProperties";

    public static final String TABLE_DATA_TAG = "tableData";

    public static final String RELATIVE_WIDTH_INDICATOR = "*";

    @Override
    @SuppressWarnings("unchecked")
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        TableView tableView = new TableView();

        NodeList childNodes = element.getChildNodes();

        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;
                String childTagName = childElement.getTagName();

                if (childTagName.equals(COLUMNS_TAG)) {
                    ArrayList<TableView.Column> columns = getColumns(childElement, rootLoader);

                    for (TableView.Column column : columns) {
                        tableView.getColumns().add(column);
                    }
                } else {
                    if (childTagName.equals(TABLE_DATA_TAG)) {
                        InlineDataList elementList = InlineDataList.load(childElement, rootLoader);
                        tableView.setTableData(elementList);
                    }
                }
            }
        }

        if (element.hasAttribute(SELECT_MODE_ATTRIBUTE)) {
            String selectModeAttribute = element.getAttribute(SELECT_MODE_ATTRIBUTE);
            TableView.SelectMode selectMode = TableView.SelectMode.decode(selectModeAttribute);
            tableView.setSelectMode(selectMode);
        }

        if (element.hasAttribute(SELECTED_INDEX_ATTRIBUTE)) {
            int selectedIndex = Integer.parseInt(element.getAttribute(SELECTED_INDEX_ATTRIBUTE));
            tableView.setSelectedIndex(selectedIndex);
        }

        if (element.hasAttribute(SELECTED_INDEXES_ATTRIBUTE)) {
            String selectedIndexesAttribute = element.getAttribute(SELECTED_INDEXES_ATTRIBUTE);

            StringReader selectedIndexesReader = null;
            try {
                selectedIndexesReader = new StringReader(selectedIndexesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                List<Object> selectedIndexes =
                    (List<Object>)jsonSerializer.readObject(selectedIndexesReader);

                for (Object index : selectedIndexes) {
                    tableView.addSelectedIndex((Integer)index);
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to set selected indexes.", exception);
            } finally {
                if (selectedIndexesReader != null) {
                    selectedIndexesReader.close();
                }
            }
        }

        if (element.hasAttribute(DISABLED_ROWS_ATTRIBUTE)) {
            String disabledRowsAttribute = element.getAttribute(DISABLED_ROWS_ATTRIBUTE);

            StringReader disabledRowsReader = null;
            try {
                disabledRowsReader = new StringReader(disabledRowsAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                List<Object> disabledItems =
                    (List<Object>)jsonSerializer.readObject(disabledRowsReader);

                for (Object index : disabledItems) {
                    tableView.setRowDisabled((Integer)index, true);
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to set disabled rows.", exception);
            } finally {
                if (disabledRowsReader != null) {
                    disabledRowsReader.close();
                }
            }
        }

        return tableView;
    }

    @SuppressWarnings("unchecked")
    private ArrayList<TableView.Column> getColumns(Element element, ComponentLoader rootLoader)
        throws LoadException {
        ArrayList<TableView.Column> columns = new ArrayList<TableView.Column>();

        NodeList childNodes = element.getChildNodes();
        for (int i = 0, n = childNodes.getLength(); i < n; i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element)childNode;

                if (childElement.getTagName().equals(COLUMN_TAG)) {
                    String name = null;
                    Object headerData = null;
                    int width = -1;
                    boolean relative = false;

                    if (childElement.hasAttribute(COLUMN_NAME_ATTRIBUTE)) {
                        name = childElement.getAttribute(COLUMN_NAME_ATTRIBUTE);
                    }

                    if (childElement.hasAttribute(COLUMN_HEADER_DATA_ATTRIBUTE)) {
                        String headerDataAttribute = childElement.getAttribute(COLUMN_HEADER_DATA_ATTRIBUTE);

                        StringReader headerDataReader = null;
                        try {
                            headerDataReader = new StringReader(headerDataAttribute);
                            JSONSerializer jsonSerializer = new JSONSerializer();
                            headerData = rootLoader.resolve(jsonSerializer.readObject(headerDataReader));
                        } catch(Exception exception) {
                            throw new LoadException("Unable to set header data.", exception);
                        } finally {
                            if (headerDataReader != null) {
                                headerDataReader.close();
                            }
                        }
                    }

                    if (childElement.hasAttribute(COLUMN_WIDTH_ATTRIBUTE)) {
                        String widthAttribute = childElement.getAttribute(COLUMN_WIDTH_ATTRIBUTE);

                        int j = widthAttribute.lastIndexOf(RELATIVE_WIDTH_INDICATOR);
                        if (j != -1) {
                            relative = true;
                            widthAttribute = widthAttribute.substring(0, j);
                        }

                        width = Integer.parseInt(widthAttribute);
                    }

                    TableView.Column column = new TableView.Column(name, headerData, width, relative);

                    if (childElement.hasAttribute(COLUMN_CELL_RENDERER_ATTRIBUTE)) {
                        String cellRendererAttribute = childElement.getAttribute(COLUMN_CELL_RENDERER_ATTRIBUTE);

                        try {
                            Class<?> cellRendererClass = Class.forName(cellRendererAttribute);
                            column.setCellRenderer((TableView.CellRenderer)cellRendererClass.newInstance());
                        } catch(Exception exception) {
                            throw new LoadException("Unable to install cell renderer.", exception);
                        }
                    }

                    TableView.CellRenderer cellRenderer = column.getCellRenderer();

                    if (childElement.hasAttribute(COLUMN_CELL_RENDERER_STYLES_ATTRIBUTE)) {
                        String cellRendererStylesAttribute = childElement.getAttribute(COLUMN_CELL_RENDERER_STYLES_ATTRIBUTE);

                        StringReader cellRendererStylesReader = null;
                        try {
                            cellRendererStylesReader = new StringReader(cellRendererStylesAttribute);
                            JSONSerializer jsonSerializer = new JSONSerializer();
                            Map<String, Object> styles =
                                (Map<String, Object>)jsonSerializer.readObject(cellRendererStylesReader);

                            for (String key : styles) {
                                cellRenderer.getStyles().put(key, styles.get(key));
                            }
                        } catch(Exception exception) {
                            throw new LoadException("Unable to apply cell renderer styles.", exception);
                        } finally {
                            if (cellRendererStylesReader != null) {
                                cellRendererStylesReader.close();
                            }
                        }
                    }

                    if (childElement.hasAttribute(COLUMN_CELL_RENDERER_PROPERTIES_ATTRIBUTE)) {
                        String cellRendererPropertiesAttribute =
                            childElement.getAttribute(COLUMN_CELL_RENDERER_PROPERTIES_ATTRIBUTE);

                        StringReader cellRendererPropertiesReader = null;
                        try {
                            cellRendererPropertiesReader = new StringReader(cellRendererPropertiesAttribute);
                            JSONSerializer jsonSerializer = new JSONSerializer();
                            Map<String, Object> properties =
                                (Map<String, Object>)jsonSerializer.readObject(cellRendererPropertiesReader);

                            for (String key : properties) {
                                cellRenderer.put(key, properties.get(key));
                            }
                        } catch(Exception exception) {
                            throw new LoadException("Unable to apply cell renderer properties.", exception);
                        } finally {
                            if (cellRendererPropertiesReader != null) {
                                cellRendererPropertiesReader.close();
                            }
                        }
                    }

                    columns.add(column);
                }
            }
        }

        return columns;
    }
}
