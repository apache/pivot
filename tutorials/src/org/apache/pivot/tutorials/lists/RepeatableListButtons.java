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
package org.apache.pivot.tutorials.lists;

import java.awt.Color;
import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Button.State;
import org.apache.pivot.wtk.ButtonStateListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ColorItem;

public class RepeatableListButtons extends Window implements Bindable {
    private ListButton colorListButton = null;
    private BoxPane checkboxBoxPane = null;

    private int selectedCount = 0;

    private Action applyColorAction = new Action() {
        @Override
        public void perform(Component source) {
            ColorItem colorItem = (ColorItem) colorListButton.getButtonData();
            Color color = colorItem.getColor();

            for (Component component : checkboxBoxPane) {
                Checkbox checkbox = (Checkbox) component;
                if (checkbox.isSelected()) {
                    checkbox.getStyles().put(Style.color, color);
                    checkbox.setSelected(false);
                }
            }
        }
    };

    public RepeatableListButtons() {
        Action.getNamedActions().put("applyColor", applyColorAction);
        applyColorAction.setEnabled(false);
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        colorListButton = (ListButton) namespace.get("colorListButton");
        checkboxBoxPane = (BoxPane) namespace.get("checkboxBoxPane");

        ButtonStateListener buttonStateListener = new ButtonStateListener() {
            @Override
            public void stateChanged(Button button, State previousState) {
                if (button.isSelected()) {
                    selectedCount++;
                } else {
                    selectedCount--;
                }

                applyColorAction.setEnabled(selectedCount > 0);
            }
        };

        ArrayList<String> numbers = new ArrayList<>("One", "Two", "Three", "Four", "Five", "Six",
            "Seven", "Eight", "Nine", "Ten");

        for (String number : numbers) {
            Checkbox checkbox = new Checkbox(number);
            checkbox.getButtonStateListeners().add(buttonStateListener);
            checkboxBoxPane.add(checkbox);
        }
    }
}
