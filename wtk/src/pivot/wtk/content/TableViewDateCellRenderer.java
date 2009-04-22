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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import pivot.beans.BeanDictionary;
import pivot.collections.Dictionary;
import pivot.wtk.TableView;

/**
 * Default renderer for table view cells that contain date data. Renders
 * cell contents as a formatted date.
 *
 * @author gbrown
 */
public class TableViewDateCellRenderer extends TableViewCellRenderer {
    private DateFormat dateFormat = DEFAULT_DATE_FORMAT;

    public static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance();

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        if (dateFormat == null) {
            throw new IllegalArgumentException("dateFormat is null.");
        }

        this.dateFormat = dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        setDateFormat(new SimpleDateFormat(dateFormat));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        renderStyles(tableView, rowSelected, rowDisabled);

        if (value != null) {
            String formattedDate = null;

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

                if (cellData instanceof Date) {
                    formattedDate = dateFormat.format((Date)cellData);
                } else {
                    System.err.println("Data for \"" + columnName + "\" is not an instance of "
                        + Date.class.getName());
                }
            }

            setText(formattedDate);
        }
    }
}
