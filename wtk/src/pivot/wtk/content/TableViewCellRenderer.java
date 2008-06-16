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
package pivot.wtk.content;

import java.awt.Color;
import java.awt.Font;

import pivot.collections.Dictionary;
import pivot.wtk.Component;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Renderer;
import pivot.wtk.TableView;
import pivot.wtk.VerticalAlignment;

public class TableViewCellRenderer extends Label
    implements TableView.CellRenderer {
    protected class PropertyDictionary extends Renderer.PropertyDictionary {
        public Object get(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            return null;
        }

        public Object put(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            System.out.println("\"" + key + "\" is not a valid property for "
                + getClass().getName() + ".");

            return null;
        }

        public Object remove(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            return null;
        }

        public boolean containsKey(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            return false;
        }

        public boolean isEmpty() {
            return true;
        }
    }

    protected PropertyDictionary properties = new PropertyDictionary();

    public TableViewCellRenderer() {
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
    }

    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        // Get the row and cell data
        String columnName = column.getName();
        Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
        Object cellData = rowData.get(columnName);

        setText(cellData == null ? null : cellData.toString());

        renderStyles(tableView, rowSelected, rowDisabled);
    }

    protected void renderStyles(TableView tableView, boolean rowSelected, boolean rowDisabled) {
        Component.StyleDictionary tableViewStyles = tableView.getStyles();
        Component.StyleDictionary styles = getStyles();

        Object font = tableViewStyles.get("font");

        if (font instanceof Font) {
            styles.put("font", font);
        } else {
            styles.remove("font");
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
        } else {
            styles.remove("color");
        }
    }

    public PropertyDictionary getProperties() {
        return properties;
    }
}
