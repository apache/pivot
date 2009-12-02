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

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class SimpleGridPanes implements Application {
    private Window window = null;
    private GridPane gridPane = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "simple_grid_panes.wtkx");
        gridPane = (GridPane)wtkxSerializer.get("gridPane");

        gridPane.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                int rowIndex = gridPane.getRowAt(y);
                int columnIndex = gridPane.getColumnAt(x);

                if (rowIndex >= 0
                    && columnIndex >= 0) {
                    Dimensions d = gridPane.getCellComponent(rowIndex, columnIndex).getSize();
                    int rowHeight = d.height;
                    int columnWidth = d.width;

                    String message = "Registered Click At " + rowIndex + "," + columnIndex;

                    Label heightLabel = new Label(String.format("The row's height is %d",
                        rowHeight));
                    Label widthLabel = new Label(String.format("The column's width is %d",
                        columnWidth));

                    BoxPane body = new BoxPane(Orientation.VERTICAL);
                    body.add(heightLabel);
                    body.add(widthLabel);

                    Prompt.prompt(MessageType.INFO, message, body, window);
                }

                return false;
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
        // No-op
    }

    @Override
    public void resume() {
        // No-op
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(SimpleGridPanes.class, args);
    }
}
