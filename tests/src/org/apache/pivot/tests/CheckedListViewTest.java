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

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;


public class CheckedListViewTest extends Application.Adapter {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        final ListView listView = new ListView(JSONSerializer.parseList("['One', 'Two', 'Three', 'Four']"));
        listView.setSelectMode(ListView.SelectMode.MULTI);
        listView.setCheckmarksEnabled(true);
        listView.setItemChecked(0, true);
        listView.setItemChecked(2, true);

        listView.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE) {
                    List<Object> listData = (List<Object>)listView.getListData();

                    Sequence<Span> selectedRanges = listView.getSelectedRanges();
                    for (int i = selectedRanges.getLength() - 1; i >= 0; i--) {
                        Span selectedRange = selectedRanges.get(i);
                        listData.remove(selectedRange.start, selectedRange.end - selectedRange.start + 1);
                    }
                }

                return false;
            }
        });

        window = new Window(listView);
        window.setTitle("Checked List View Test");
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(CheckedListViewTest.class, args);
    }
}
