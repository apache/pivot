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
    private NumberFormat numberFormat = DEFAULT_NUMBER_FORMAT;

    public static final NumberFormat DEFAULT_NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public NumberFormat getNumberFormat() {
        return numberFormat;
    }

    public void setNumberFormat(NumberFormat numberFormat) {
        if (numberFormat == null) {
            throw new IllegalArgumentException("numberFormat is null.");
        }

        this.numberFormat = numberFormat;
    }

    public void setNumberFormat(String numberFormat) {
        setNumberFormat(new DecimalFormat(numberFormat));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        String formattedNumber = null;

        // Get the row and cell data
        String columnName = column.getName();
        if (columnName != null) {
            Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
            Object cellData = rowData.get(columnName);

            if (cellData instanceof Number) {
                formattedNumber = numberFormat.format((Number)cellData);
            }
        }

        setText(formattedNumber);

        renderStyles(tableView, rowSelected, rowDisabled);
    }
}
