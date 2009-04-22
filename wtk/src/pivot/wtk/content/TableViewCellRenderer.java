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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import pivot.beans.BeanDictionary;
import pivot.collections.Dictionary;
import pivot.wtk.Component;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.TableView;
import pivot.wtk.VerticalAlignment;

/**
 * Default table cell renderer. Renders cell contents as a string.
 *
 * @author gbrown
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

        Object font = tableViewStyles.get("font");

        if (font instanceof Font) {
            styles.put("font", font);
        }

        Object color = null;

        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
                if (tableView.isFocused()) {
                    color = tableViewStyles.get("selectionColor");
                } else {
                    color = tableViewStyles.get("inactiveSelectionColor");
                }
            } else {
                color = tableViewStyles.get("color");
            }
        } else {
            color = tableViewStyles.get("disabledColor");
        }

        if (color instanceof Color) {
            styles.put("color", color);
        }
    }
}
