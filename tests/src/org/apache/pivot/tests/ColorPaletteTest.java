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
package org.apache.pivot.tests;

import java.awt.Color;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.StackPane;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;

public class ColorPaletteTest implements Application {
    private Window window = null;

    @SuppressWarnings("unused")
    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        Theme theme = Theme.getTheme();

        TablePane tablePane = new TablePane();
        new TablePane.Column(tablePane, 1, true);
        new TablePane.Column(tablePane, 1, true);
        new TablePane.Column(tablePane, 1, true);

        int numberOfPaletteColors = theme.getNumberOfPaletteColors();
        // ArrayList<String> colors = new ArrayList<>(numberOfPaletteColors);
        for (int i = 0; i < numberOfPaletteColors; i++) {
            TablePane.Row row = new TablePane.Row(tablePane, 1, true);

            row.add(createCell(i * 3));
            row.add(createCell(i * 3 + 1));
            row.add(createCell(i * 3 + 2));
        }

        tablePane.getStyles().put(Style.horizontalSpacing, 4);
        tablePane.getStyles().put(Style.verticalSpacing, 4);

        Border border = new Border(tablePane);
        border.getStyles().put(Style.padding, 6);

        this.window = new Window(border);
        this.window.setTitle("Color Palette");
        this.window.setMaximized(true);
        this.window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (this.window != null) {
            this.window.close();
        }

        return false;
    }

    private static Component createCell(int index) {
        StackPane stackPane = new StackPane();

        Border border = new Border();
        border.getStyles().put(Style.backgroundColor, index);

        stackPane.add(border);

        Label label = new Label();
        label.setText(Integer.toString(index));
        label.getStyles().put(Style.backgroundColor, Color.WHITE);
        label.getStyles().put(Style.padding, 2);

        Color themeColor = border.getStyles().getColor(Style.backgroundColor);
        Label colorLabel = new Label();
        colorLabel.setText(String.format("R:%1$d,G:%2$d,B:%3$d",
             themeColor.getRed(), themeColor.getGreen(), themeColor.getBlue()));
        colorLabel.getStyles().put(Style.backgroundColor, Color.WHITE);
        colorLabel.getStyles().put(Style.padding, 2);

        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        boxPane.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        boxPane.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);

        boxPane.add(new Border(label));
        boxPane.add(new Border(colorLabel));
        stackPane.add(boxPane);

        return stackPane;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ColorPaletteTest.class, args);
    }

}
