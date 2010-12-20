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

import org.apache.pivot.json.JSON;
import org.apache.pivot.text.FileSizeFormat;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;

/**
 * Default renderer for table view cells that contain file size data. Renders
 * cell contents as a formatted file size.
 */
public class TableViewFileSizeCellRenderer extends TableViewCellRenderer {
    public TableViewFileSizeCellRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

        // Apply more padding on the right so the right-aligned cells don't
        // appear to run into left-aligned cells in the next column
        getStyles().put("padding", new Insets(2, 2, 2, 6));
    }

    @Override
    public String toString(Object row, String columnName) {
        Object cellData = JSON.get(row, columnName);

        String string;
        if (cellData instanceof Number) {
            string = FileSizeFormat.getInstance().format(cellData);
        } else {
            string = (cellData == null) ? null : cellData.toString();
        }

        return string;
    }
}
