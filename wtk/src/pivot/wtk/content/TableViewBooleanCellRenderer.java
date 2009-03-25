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

import pivot.beans.BeanDictionary;
import pivot.collections.Dictionary;
import pivot.wtk.Checkbox;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.TableView;
import pivot.wtk.VerticalAlignment;

/**
 * Default renderer for table view cells that contain boolean data. Renders
 * cell contents as a checkbox.
 *
 * @author gbrown
 */
public class TableViewBooleanCellRenderer extends FlowPane
    implements TableView.CellRenderer {
    private Checkbox checkbox = new Checkbox();

    public TableViewBooleanCellRenderer() {
        super();

        add(checkbox);

        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);

        // Since this component doesn't have a parent, it won't be validated
        // via layout; ensure that it is valid here
        validate();
    }

    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        boolean checkboxSelected = false;

        // Get the row and cell data
        String columnName = column.getName();
        if (columnName != null) {
            Dictionary<String, Object> rowData;
            if (value instanceof Dictionary<?, ?>) {
                rowData = (Dictionary<String, Object>)value;
            } else {
                rowData = new BeanDictionary(value);
            }

            Object cellData = rowData.get(columnName);

            if (cellData instanceof String) {
                cellData = Boolean.parseBoolean((String)cellData);
            }

            if (cellData instanceof Boolean) {
                checkboxSelected = (Boolean)cellData;
            } else {
                System.err.println("Data for \"" + columnName + "\" is not an instance of "
                    + Boolean.class.getName());
            }
        }

        checkbox.setSelected(checkboxSelected);
        checkbox.setEnabled(tableView.isEnabled() && !rowDisabled);
    }
}
