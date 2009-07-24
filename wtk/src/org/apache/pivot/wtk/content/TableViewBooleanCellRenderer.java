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

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.VerticalAlignment;


/**
 * Default renderer for table view cells that contain boolean data. Renders
 * cell contents as a checkbox.
 *
 * @author gbrown
 */
public class TableViewBooleanCellRenderer extends BoxPane
    implements TableView.CellRenderer {
    /**
     * Internal style dictionary that provides add-on styles.
     *
     * @author tvolkert
     */
    private class StyleDictionary implements Dictionary<String, Object> {
        private Dictionary<String, Object> styles;

        public StyleDictionary() {
            styles = TableViewBooleanCellRenderer.super.getStyles();
        }

        public Object get(String key) {
            if (key == null) {
                throw new IllegalArgumentException();
            }

            Object value;

            if (key.equals(CHECKBOX_DISABLED_KEY)) {
                value = checkboxDisabled;
            } else {
                value = styles.get(key);
            }

            return value;
        }

        public Object put(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException();
            }

            Object previousValue;

            if (key.equals(CHECKBOX_DISABLED_KEY)) {
                previousValue = checkboxDisabled;
                checkboxDisabled = !checkboxDisabled;
            } else {
                previousValue = styles.put(key, value);
            }

            return previousValue;
        }

        public Object remove(String key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(String key) {
            if (key == null) {
                throw new IllegalArgumentException();
            }

            boolean containsKey;

            if (key.equals(CHECKBOX_DISABLED_KEY)) {
                containsKey = true;
            } else {
                containsKey = styles.containsKey(key);
            }

            return containsKey;
        }

        public boolean isEmpty() {
            return false;
        }
    }

    private Checkbox checkbox = new Checkbox();
    private boolean checkboxDisabled = false;

    private final StyleDictionary styleDictionary = new StyleDictionary();

    /**
     * The style property that controls whether the renderer's checkbox should
     * always appear disabled or whether it should correspond to the enabled
     * state of the table view and row. If this style is <tt>true</tt>, the
     * checkbox will always appear disabled, regardless of the enabled states
     * of the table view and row.
     */
    public static final String CHECKBOX_DISABLED_KEY = "checkboxDisabled";

    public TableViewBooleanCellRenderer() {
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

    @Override
    public Dictionary<String, Object> getStyles() {
        return styleDictionary;
    }

    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        if (value != null) {
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
            checkbox.setEnabled(!checkboxDisabled && tableView.isEnabled() && !rowDisabled);
        }
    }
}
