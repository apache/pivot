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
package pivot.tutorials.lists;

import pivot.collections.Dictionary;
import pivot.collections.Sequence;
import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.ListViewSelectionListener;
import pivot.wtk.Span;
import pivot.wtk.Window;
import pivot.wtkx.WTKX;
import pivot.wtkx.WTKXSerializer;

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

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "list_views.wtkx");
        wtkxSerializer.bind(this);

        listView.getListViewSelectionListeners().add(listViewSelectionListener);

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ListViews.class, args);
    }
}
