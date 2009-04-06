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

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.util.Locale;

import pivot.collections.Dictionary;
import pivot.wtk.TableView;

/**
 * Renders table cells as currency values.
 *
 * @author tvolkert
 */
public class TableViewCurrencyCellRenderer extends TableViewCellRenderer {
    private NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();

    private static final FieldPosition DONT_CARE_FIELD_POSITION = new FieldPosition(0);

    @Override
    @SuppressWarnings("unchecked")
    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        // Get the row and cell data
        String columnName = column.getName();
        Dictionary<String, Object> rowData = (Dictionary<String, Object>)value;
        Object cellData = rowData.get(columnName);

        StringBuffer formattedValue = currencyFormat.format(cellData, new StringBuffer(),
            DONT_CARE_FIELD_POSITION);
        setText(formattedValue.toString());

        renderStyles(tableView, rowSelected, rowDisabled);
    }
}
