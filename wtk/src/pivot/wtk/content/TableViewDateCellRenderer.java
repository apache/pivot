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

/**
 * <p>Default renderer for table view cells that contain date data. Renders
 * cell contents as a formatted date.</p>
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
        String formattedDate = null;

        // Get the row and cell data
        String columnName = column.getName();
        if (columnName != null) {
            Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
            Object cellData = rowData.get(columnName);

            if (cellData instanceof Date) {
                formattedDate = dateFormat.format((Date)cellData);
            }
        }

        setText(formattedDate);

        renderStyles(tableView, rowSelected, rowDisabled);
    }
}
