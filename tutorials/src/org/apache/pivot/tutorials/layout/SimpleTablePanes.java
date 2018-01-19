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
package org.apache.pivot.tutorials.layout;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;

public class SimpleTablePanes extends Window implements Bindable {
    private TablePane tablePane = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        tablePane = (TablePane) namespace.get("tablePane");

        tablePane.getComponentMouseButtonListeners().add(
            new ComponentMouseButtonListener() {
                @Override
                public boolean mouseClick(Component component, Mouse.Button button, int x, int y,
                    int count) {
                    int rowIndex = tablePane.getRowAt(y);
                    int columnIndex = tablePane.getColumnAt(x);

                    if (rowIndex >= 0 && columnIndex >= 0) {
                        TablePane.Row row = tablePane.getRows().get(rowIndex);
                        TablePane.Column column = tablePane.getColumns().get(columnIndex);

                        int rowHeight = row.getHeight();
                        int columnWidth = column.getWidth();

                        String message = "Registered Click At " + rowIndex + "," + columnIndex;

                        Label heightLabel = new Label(String.format("The row's height is %d (%s)",
                            rowHeight, rowHeight == -1 ? "default" : (row.isRelative() ? "relative"
                                : "absolute")));
                        Label widthLabel = new Label(String.format("The column's width is %d (%s)",
                            columnWidth, columnWidth == -1 ? "default"
                                : (column.isRelative() ? "relative" : "absolute")));

                        BoxPane body = new BoxPane(Orientation.VERTICAL);
                        body.add(heightLabel);
                        body.add(widthLabel);

                        Prompt.prompt(MessageType.INFO, message, body, SimpleTablePanes.this);
                    }

                    return false;
                }
            });
    }
}
