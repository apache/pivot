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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import pivot.collections.Dictionary;
import pivot.wtk.TableView;

public class TableViewDateCellRenderer extends TableViewCellRenderer {
    protected class PropertyDictionary extends TableViewCellRenderer.PropertyDictionary {
        @Override
        public Object get(String key) {
            if (key == null) {
                throw new IllegalArgumentException("key is null.");
            }

            Object value = null;

            if (key.equals(DATE_FORMAT_KEY)) {
                value = getDateFormat();
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

            if (key.equals(DATE_FORMAT_KEY)) {
                if (value instanceof String) {
                    value = new SimpleDateFormat((String)value);
                }

                previousValue = dateFormat;
                setDateFormat((DateFormat)value);
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

            if (key.equals(DATE_FORMAT_KEY)) {
                previousValue = put(key, DEFAULT_DATE_FORMAT);
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

            return (key.equals(DATE_FORMAT_KEY)
                    || super.containsKey(key));
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private DateFormat dateFormat = DEFAULT_DATE_FORMAT;

    public static final String DATE_FORMAT_KEY = "dateFormat";

    public static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance();

    public TableViewDateCellRenderer() {
        super();

        properties = new PropertyDictionary();
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat is null.");
        }

        this.dateFormat = dateFormat;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        // Get the row and cell data
        String columnName = column.getName();
        Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
        Object cellData = rowData.get(columnName);

        String formattedDate = null;
        if (cellData instanceof Date) {
            formattedDate = dateFormat.format((Date)cellData);
        }

        setText(formattedDate);

        renderStyles(tableView, rowSelected, rowDisabled);
    }
}
