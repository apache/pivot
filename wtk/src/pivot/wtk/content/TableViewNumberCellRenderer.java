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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import pivot.collections.Dictionary;
import pivot.wtk.TableView;

public class TableViewNumberCellRenderer extends TableViewCellRenderer {
    protected class PropertyDictionary extends TableViewCellRenderer.PropertyDictionary {
        @Override
        public Object get(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object value = null;

            if (key.equals(NUMBER_FORMAT_KEY)) {
                value = getNumberFormat();
            } else {
                value = super.get(key);
            }

            return value;
        }

        @Override
        public Object put(String key, Object value) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object previousValue = null;

            if (key.equals(NUMBER_FORMAT_KEY)) {
                if (value instanceof String) {
                    value = new DecimalFormat((String)value);
                }

                previousValue = numberFormat;
                setNumberFormat((NumberFormat)value);
            } else {
                previousValue = super.put(key, value);
            }

            return previousValue;
        }

        @Override
        public Object remove(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object previousValue = null;

            if (key.equals(NUMBER_FORMAT_KEY)) {
                previousValue = put(key, NUMBER_FORMAT_KEY);
            } else {
                previousValue = super.remove(key);
            }

            return previousValue;
        }

        @Override
        public boolean containsKey(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            return (key.equals(NUMBER_FORMAT_KEY)
                    || super.containsKey(key));
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private NumberFormat numberFormat = DEFAULT_NUMBER_FORMAT;

    public static final String NUMBER_FORMAT_KEY = "numberFormat";

    public static final NumberFormat DEFAULT_NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public TableViewNumberCellRenderer() {
        super();

        properties = new PropertyDictionary();
    }

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(NumberFormat numberFormat) {
        if (numberFormat == null) {
            throw new IllegalArgumentException("numberFormat is null.");
        }

        this.numberFormat = numberFormat;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        // Get the row and cell data
        String columnName = column.getName();
        Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
        Object cellData = rowData.get(columnName);

        String formattedNumber = null;
        if (cellData instanceof Number) {
            formattedNumber = numberFormat.format((Number)cellData);
        }

        setText(formattedNumber);

        renderStyles(tableView, rowSelected, rowDisabled);
    }
}
