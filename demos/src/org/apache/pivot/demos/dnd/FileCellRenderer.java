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
package org.apache.pivot.demos.dnd;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.TableView;
import pivot.wtk.VerticalAlignment;

public class FileCellRenderer extends Label implements TableView.CellRenderer {
    public static final String NAME_KEY = "name";
    public static final String SIZE_KEY = "size";
    public static final String LAST_MODIFIED_KEY = "lastModified";

    public FileCellRenderer() {
        getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
    }

    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        if (value != null) {
            File file = (File)value;
            String columnName = column.getName();

            String text;
            if (columnName.equals(NAME_KEY)) {
                text = file.getName();
                getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
            } else if (columnName.equals(SIZE_KEY)) {
                long length = file.length();

                // TODO kB, MB, etc.
                text = Long.toString(length);
                getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
            } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                long lastModified = file.lastModified();
                Date lastModifiedDate = new Date(lastModified);

                // TODO Use an appropriate format
                DateFormat dateFormat = DateFormat.getDateInstance();
                text = dateFormat.format(lastModifiedDate);
                getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
            } else {
                text = null;
            }

            setText(text);
        }

        Font font = (Font)tableView.getStyles().get("font");
        getStyles().put("font", font);

        Color color;
        if (tableView.isEnabled() && !rowDisabled) {
            if (rowSelected) {
                if (tableView.isFocused()) {
                    color = (Color)tableView.getStyles().get("selectionColor");
                } else {
                    color = (Color)tableView.getStyles().get("inactiveSelectionColor");
                }
            } else {
                color = (Color)tableView.getStyles().get("color");
            }
        } else {
            color = (Color)tableView.getStyles().get("disabledColor");
        }

        getStyles().put("color", color);
    }
}
