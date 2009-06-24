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

import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ListViews implements Application {
    private Window window = null;

    @WTKX private Label selectionLabel;
    @WTKX private ListView listView;

    private ListViewSelectionListener listViewSelectionListener =
        new ListViewSelectionListener() {
        public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
            updateSelection(listView);
        }

        public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
            updateSelection(listView);
        }

        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
            updateSelection(listView);
        }

        private void updateSelection(ListView listView) {
            String selectionText = "";

            Sequence<Span> selectedRanges = listView.getSelectedRanges();
            for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
                Span selectedRange = selectedRanges.get(i);

                for (int j = selectedRange.getStart();
                    j <= selectedRange.getEnd();
                    j++) {
                    if (selectionText.length() > 0) {
                        selectionText += ", ";
                    }

                    String text = (String)listView.getListData().get(j);
                    selectionText += text;
                }
            }

            selectionLabel.setText(selectionText);
        }
    };

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "list_views.wtkx");
        wtkxSerializer.bind(this, ListViews.class);

        listView.getListViewSelectionListeners().add(listViewSelectionListener);

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ListViews.class, args);
    }
}
