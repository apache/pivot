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
package org.apache.pivot.examples.sheets;

import java.net.URL;
import java.util.Iterator;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.skin.terra.TerraSheetSkin.SheetPlacement;

public class SheetSlideDirectionWindow extends Window implements Bindable {

    @BXML
    Sheet sheet;
    @BXML
    TablePane tablePane;
    @BXML
    Form form;
    @BXML
    ListButton listButton;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {

        // Populate the ListButton with values from the enum
        listButton.setListData(new ArrayList<>(SheetPlacement.values()));
        listButton = null;

        // Populate the form with data from the Sheet's styles
        form.load(sheet.getStyles());

        // Common ButtonPressListener to be applied to all four of
        // the PushButtons representing a slide direction
        ButtonPressListener openSheetButtonPressListener = new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                StyleDictionary sheetStyles = sheet.getStyles();
                form.store(sheetStyles);
                button.store(sheetStyles);
                form.load(sheetStyles);
                sheet.load(sheetStyles);
                sheet.open(button.getWindow());
            }
        };

        // Apply the common ButtonPressListener to the PushButtons
        for (Iterator<Component> it = tablePane.iterator(); it.hasNext();) {
            Component component = it.next();
            if (component instanceof PushButton) {
                PushButton button = (PushButton) component;
                button.getButtonPressListeners().add(openSheetButtonPressListener);
                button.setTooltipText("Press me!");
            }
        }
        tablePane = null;

        // Mouse handler to enable users to quickly close the sheet
        final ContainerMouseListener displayMouseHandler = new ContainerMouseListener() {
            @Override
            public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
                Display display = (Display) container;
                Component component = display.getComponentAt(x, y);

                // Close the sheet by clicking away from it.
                // This allows resizing etc to work without requiring
                // a close button or similar on the sheet.
                boolean consumed = (component != sheet);
                if (consumed) {
                    sheet.close();
                }
                return consumed;
            }
        };

        // Add/remove the mouse handler based on the Sheet's state
        sheet.getWindowStateListeners().add(new WindowStateListener() {
            @Override
            public void windowOpened(Window window) {
                window.getDisplay().getContainerMouseListeners().add(displayMouseHandler);
            }

            @Override
            public void windowClosed(Window window, Display display, Window owner) {
                display.getContainerMouseListeners().remove(displayMouseHandler);
            }
        });
    }

}

