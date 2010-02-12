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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.pivot.beans.BeanDictionary;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.wtk.TableView;


/**
 * Default renderer for table view cells that contain date data. Renders
 * cell contents as a formatted date.
 */
public class TableViewDateCellRenderer extends TableViewCellRenderer {
    private DateFormat dateFormat = DEFAULT_DATE_FORMAT;

    protected static final DateFormat DEFAULT_DATE_FORMAT = DateFormat.getDateInstance();

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
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        renderStyles(tableView, selected, disabled);

        String formattedDate = null;

        if (row != null) {
            // Get the row and cell data
            if (columnName != null) {
                Dictionary<String, Object> rowData;
                if (row instanceof Dictionary<?, ?>) {
                    rowData = (Dictionary<String, Object>)row;
                } else {
                    rowData = new BeanDictionary(row);
                }

                Object cellData = rowData.get(columnName);

                if (cellData != null) {
                    if (cellData instanceof Date) {
                        formattedDate = dateFormat.format((Date)cellData);
                    } else if (cellData instanceof Long) {
                        formattedDate = dateFormat.format(new Date((Long)cellData));
                    } else if (cellData instanceof Calendar) {
                        formattedDate = dateFormat.format(((Calendar)cellData).getTime());
                    } else if (cellData instanceof CalendarDate) {
                        formattedDate = dateFormat.format(((CalendarDate)cellData).toCalendar().getTime());
                    } else {
                        System.err.println(getClass().getName() + " cannot render " + cellData);
                    }
                }
            }
        }

        setText(formattedDate);
    }
}
