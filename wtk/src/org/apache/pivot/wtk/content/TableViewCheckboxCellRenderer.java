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

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.VerticalAlignment;

/**
 * Default renderer for table view cells that contain boolean data. Renders
 * cell contents as a checkbox.
 */
public class TableViewCheckboxCellRenderer extends BoxPane
    implements TableView.CellRenderer {
    protected Checkbox checkbox = new Checkbox();
    private boolean checkboxDisabled = false;

    public TableViewCheckboxCellRenderer() {
        add(checkbox);

        getStyles().put("padding", 3);
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

    @Override
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        if (row != null) {
            // Get the row and cell data
            if (columnName != null) {
                if (checkbox.isTriState()) {
                    checkbox.setStateKey(columnName);
                } else {
                    checkbox.setSelectedKey(columnName);
                }
                checkbox.load(row);
            } else {
                checkbox.setState(Button.State.UNSELECTED);
            }
            checkbox.setEnabled(!checkboxDisabled && tableView.isEnabled() && !disabled);
        }
    }

    @Override
    public String toString(Object row, String columnName) {
        return null;
    }

    public boolean isCheckboxDisabled() {
        return checkboxDisabled;
    }

    public void setCheckboxDisabled(boolean checkboxDisabled) {
        this.checkboxDisabled = checkboxDisabled;
    }
}
