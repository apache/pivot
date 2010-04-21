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
package org.apache.pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TextArea;


/**
 * Renders cell contents as a string using TextArea. Only really useful when the TableView is using the variableRowHeight style.
 */
public class TableViewTextAreaCellRenderer extends TextArea
    implements TableView.CellRenderer {
    public TableViewTextAreaCellRenderer() {
        getStyles().put("margin", new Insets(2));
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        renderStyles(tableView, selected, disabled);

        if (row != null) {
            Object cellData = null;

            // Get the row and cell data
            if (columnName != null) {
                Dictionary<String, Object> rowData;
                if (row instanceof Dictionary<?, ?>) {
                    rowData = (Dictionary<String, Object>)row;
                } else {
                    rowData = new BeanAdapter(row);
                }

                cellData = rowData.get(columnName);
            }

            setText(cellData == null ? null : cellData.toString());
        }
    }

    protected void renderStyles(TableView tableView, boolean rowSelected, boolean rowDisabled) {
        Component.StyleDictionary tableViewStyles = tableView.getStyles();
        Component.StyleDictionary styles = getStyles();

        Font font = (Font)tableViewStyles.get("font");
        styles.put("font", font);

        Color color;
        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
                if (tableView.isFocused()) {
                    color = (Color)tableViewStyles.get("selectionColor");
                } else {
                    color = (Color)tableViewStyles.get("inactiveSelectionColor");
                }
            } else {
                color = (Color)tableViewStyles.get("color");
            }
        } else {
            color = (Color)tableViewStyles.get("disabledColor");
        }

        styles.put("color", color);
    }

    @SuppressWarnings("unchecked")
    public String toString(Object row, String columnName) {
        Object cellData = null;

        // Get the row and cell data
        if (columnName != null) {
            Dictionary<String, Object> rowData;
            if (row instanceof Dictionary<?, ?>) {
                rowData = (Dictionary<String, Object>)row;
            } else {
                rowData = new BeanAdapter(row);
            }

            cellData = rowData.get(columnName);
        }

        return (cellData == null) ? null : cellData.toString();
    }
}
