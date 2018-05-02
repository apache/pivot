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
package org.apache.pivot.demos.colors;

import java.awt.Color;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FlowPane;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;

public final class Colors implements Application {
    private Window mainWindow;
    @Override
    public void startup(Display display, Map<String, String> properties) {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put(Style.padding, 6);
        for (CSSColor color : CSSColor.values()) {
            BoxPane container = new BoxPane(Orientation.VERTICAL);
            container.getStyles().put(Style.padding, 4);
            container.getStyles().put(Style.fill, true);
            BoxPane colorFill = new BoxPane(Orientation.VERTICAL);
            Color fillColor = color.getColor();
            colorFill.getStyles().put(Style.backgroundColor, fillColor);
            colorFill.setMinimumWidth(50);
            colorFill.setPreferredHeight(50);
            colorFill.setTooltipText(String.format("%1$s=R:%2$3d,G:%3$3d,B:%4$3d",
                    color.toString(), fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue()));
            Label nameLabel = new Label(color.toString());
            nameLabel.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
            container.add(colorFill);
            container.add(nameLabel);
            flowPane.add(new Border(container));
        }
        ScrollPane scrollPane = new ScrollPane(ScrollPane.ScrollBarPolicy.FILL,
                ScrollPane.ScrollBarPolicy.AUTO);
        scrollPane.setView(flowPane);
        mainWindow = new Window(scrollPane);
        mainWindow.setMaximized(true);
        mainWindow.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (mainWindow != null) {
            mainWindow.close();
            mainWindow = null;
        }
        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Colors.class, args);
    }
}
