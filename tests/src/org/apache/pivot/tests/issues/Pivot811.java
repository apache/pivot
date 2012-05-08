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
package org.apache.pivot.tests.issues;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.ScrollPane.ScrollBarPolicy;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

public class Pivot811 extends Application.Adapter {
    private Display display = null;

    public void startup(final Display display, Map<String, String> properties) throws Exception {
        this.display = display;

        Frame listFrame = new Frame();
        listFrame.setTitle("List Frame");
        listFrame.setPreferredSize(400, 300);
        listFrame.setLocation(20, 20);
        listFrame.getStyles().put("padding", new Insets(0, 0, 0, 0));

        BoxPane boxPane = new BoxPane();
        boxPane.getStyles().put("fill", true);
        boxPane.setOrientation(Orientation.VERTICAL);
        listFrame.setContent(boxPane);

        Label infoLabel = new Label("Double click on a list item to open a detail frame");
        boxPane.add(infoLabel);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setHorizontalScrollBarPolicy(ScrollBarPolicy.FILL);
        scrollPane.setVerticalScrollBarPolicy(ScrollBarPolicy.FILL_TO_CAPACITY);
        // scrollPane.setRepaintAllViewport(true);  // workaround for pivot-738, needed only in in some cases
        boxPane.add(scrollPane);

        final ListView listView = new ListView();
        List<String> listData = new ArrayList<String>();
        for (int i = 0; i < 50; ++i) {
            listData.add("List Item " + i);
        }
        listView.setListData(listData);
        scrollPane.setView(listView);

        listView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListView listView, Object previousSelectedItem) {
                System.out.println("selectedItemChanged : " + listView.getSelectedItem());
            }
        });

        listView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                System.out.println("mouseClick : " + count);

                if (count == 2) {
                    System.out.println("double click, now open a detail frame");

                    Frame detailFrame = new Frame();
                    detailFrame.setTitle("Detail Frame");
                    detailFrame.setPreferredSize(400, 300);
                    int selectedIndex = listView.getSelectedIndex();
                    detailFrame.setLocation(80 + (selectedIndex * 10), 80 + (selectedIndex * 10));
                    detailFrame.getStyles().put("padding", new Insets(0, 0, 0, 0));

                    BoxPane boxPane = new BoxPane();
                    boxPane.getStyles().put("fill", true);
                    boxPane.setOrientation(Orientation.VERTICAL);
                    detailFrame.setContent(boxPane);

                    String selectedItem = listView.getSelectedItem().toString();
                    Label label = new Label("Selected Item is \"" + selectedItem + "\"");
                    boxPane.add(label);
                    boxPane.add(new Label(""));  // spacer

                    boxPane.add(new Label("Click inside the text input to focus it"));
                    TextInput textInput = new TextInput();
                    textInput.setText("Focusable component");
                    boxPane.add(textInput);  // workaround for pivot-811: add a focusable element inside the frame

                    detailFrame.open(display);

                    // workaround for pivot-811: force the focus on the first focusable element inside the frame
                    detailFrame.requestFocus();
                    // textInput.requestFocus();  // or use this ...
                }

                return true;
            }
        });

        listFrame.open(display);

        listView.setSelectedIndex(0);
        listView.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        for (int i = 0; i < display.getLength(); i++) {
            ((Window) display.get(i)).close();
        }

        return false;
    }

    public static void main(String[] args) {
        // turn on debug drawing, to better see painting problems
        System.setProperty("org.apache.pivot.wtk.debugfocus", "true");  // debug focus
        // System.setProperty("org.apache.pivot.wtk.debugpaint", "true");  // debug paint

        DesktopApplicationContext.main(new String[] { Pivot811.class.getName() });
    }

}
