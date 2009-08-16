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

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.VerticalAlignment;


/**
 * Default table cell renderer. Renders cell contents as a string.
 */
public class TableViewCellRenderer extends Label
    implements TableView.CellRenderer {
    public TableViewCellRenderer() {
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
    }

    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        renderStyles(tableView, rowSelected, rowDisabled);

        if (value != null) {
            Object cellData = null;

            // Get the row and cell data
            String columnName = column.getName();
            if (columnName != null) {
                Dictionary<String, Object> rowData;
                if (value instanceof Dictionary<?, ?>) {
                    rowData = (Dictionary<String, Object>)value;
                } else {
                    rowData = new BeanDictionary(value);
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
}
