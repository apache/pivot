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

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewItemStateListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;

public class CheckedListViewTest implements Application {

    private String toShortString(Span span) {
        if (span.getLength() == 1) {
            return Integer.toString(span.start);
        } else {
            return String.format("%1$d-%2$d", span.normalStart(), span.normalEnd());
        }
    }

    Window mainWindow;
    @BXML Checkbox allowMixedStateCheckbox;
    @BXML Checkbox showMixedAsSelectedCheckbox;
    @BXML ListView listView;
    @BXML Label selectedItemsLabel;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer serializer = new BXMLSerializer();
        mainWindow = (Window) serializer.readObject(CheckedListViewTest.class, "checked_list_view_test.bxml");
        serializer.bind(this);

        allowMixedStateCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                listView.setAllowTriStateCheckmarks(button.isSelected());
                // Not sure why, but changing this setting clears all the checks but doesn't
                // trigger the item state listener (it's documented, but ...)
                selectedItemsLabel.setText("");
            }
        });

        showMixedAsSelectedCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                listView.setCheckmarksMixedAsChecked(button.isSelected());
            }
        });

        listView.getComponentKeyListeners().add(new ComponentKeyListener() {
            @Override
            public boolean keyPressed(Component component, int keyCode,
                Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE) {
                    @SuppressWarnings("unchecked")
                    List<Object> listData = (List<Object>) listView.getListData();

                    Sequence<Span> selectedRanges = listView.getSelectedRanges();
                    for (int i = selectedRanges.getLength() - 1; i >= 0; i--) {
                        Span selectedRange = selectedRanges.get(i);
                        listData.remove(selectedRange.start, selectedRange.end
                            - selectedRange.start + 1);
                    }
                }

                return false;
            }
        });

        listView.getListViewItemStateListeners().add(new ListViewItemStateListener() {

            private void displayCheckedItems(ListView listView) {
                List<?> listData = listView.getListData();
                StringBuffer buf = new StringBuffer();
                for (Integer i : listView.getCheckedIndexes()) {
                    if (buf.length() > 0) {
                        buf.append(",");
                    }
                    Object item = listData.get(i);
                    buf.append(item.toString());
                }
                selectedItemsLabel.setText(buf.toString());
            }

            @Override
            public void itemCheckedChanged(ListView listView, int index) {
                displayCheckedItems(listView);
            }

            @Override
            public void itemCheckedStateChanged(ListView listView, int index) {
                displayCheckedItems(listView);
            }
        });

        listView.setItemChecked(0, true);
        listView.setItemChecked(2, true);

        mainWindow.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (mainWindow != null) {
            mainWindow.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CheckedListViewTest.class, args);
    }
}
