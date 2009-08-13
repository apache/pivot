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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.text.DateFormat;
import java.util.Date;

import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.TableView;

public class TableViewFileRenderer extends FileRenderer implements TableView.CellRenderer {
    public static final String ICON_KEY = "icon";
    public static final String NAME_KEY = "name";
    public static final String SIZE_KEY = "size";
    public static final String LAST_MODIFIED_KEY = "lastModified";

    public TableViewFileRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        getStyles().put("padding", new Insets(2));
    }

    public void render(Object value, TableView tableView, TableView.Column column,
        boolean rowSelected, boolean rowHighlighted, boolean rowDisabled) {
        String columnName = column.getName();

        if (columnName.equals(ICON_KEY)) {
            imageView.setVisible(true);
            label.setVisible(false);

            if (value != null) {
                File file = (File)value;
                imageView.setImage(FileRenderer.getIcon(file));
            }
        } else {
            imageView.setVisible(false);
            label.setVisible(true);

            if (value != null) {
                File file = (File)value;

                String text;
                if (columnName.equals(NAME_KEY)) {
                    text = file.getName();
                    getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
                } else if (columnName.equals(SIZE_KEY)) {
                    text = formatSize(file);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                    long lastModified = file.lastModified();
                    Date lastModifiedDate = new Date(lastModified);

                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
                    text = dateFormat.format(lastModifiedDate);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                } else {
                    text = null;
                }

                label.setText(text);
            }

            Font font = (Font)tableView.getStyles().get("font");
            label.getStyles().put("font", font);

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

            label.getStyles().put("color", color);
        }
    }
}
