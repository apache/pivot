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

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.TableView;

/**
 * Default renderer for table view cells that contain numeric data. Renders
 * cell contents as a formatted number.
 */
public class TableViewNumberCellRenderer extends TableViewCellRenderer {
    private NumberFormat numberFormat = DEFAULT_NUMBER_FORMAT;

    public static final NumberFormat DEFAULT_NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public TableViewNumberCellRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        // Apply more padding on the right so the right-aligned cells don't
        // appear to run into left-aligned cells in the next column
        getStyles().put("padding", new Insets(2, 2, 2, 6));
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

    public void setNumberFormat(String numberFormat) {
        setNumberFormat(new DecimalFormat(numberFormat));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void render(Object row, int rowIndex, int columnIndex,
        TableView tableView, String columnName,
        boolean selected, boolean highlighted, boolean disabled) {
        renderStyles(tableView, selected, disabled);

        String formattedNumber = null;

        if (row != null
            && columnName != null) {
            // Get the row and cell data
            Dictionary<String, Object> rowData;
            if (row instanceof Dictionary<?, ?>) {
                rowData = (Dictionary<String, Object>)row;
            } else {
                rowData = new BeanAdapter(row);
            }

            Object cellData = rowData.get(columnName);

            if (cellData != null) {
                if (cellData instanceof Number) {
                    formattedNumber = numberFormat.format(cellData);
                } else {
                    System.err.println("Data for \"" + columnName + "\" is a "
                        + cellData.getClass().getName() + ", not a " + Number.class.getName());
                }
            }
        }

        setText(formattedNumber);
    }
}
