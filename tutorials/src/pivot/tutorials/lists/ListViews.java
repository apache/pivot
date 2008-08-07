/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import pivot.wtk.Component;
import pivot.wtk.Label;
import pivot.wtk.ListView;
import pivot.wtk.ListViewSelectionListener;
import pivot.wtk.Span;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class ListViews implements Application {
    private Window window = null;

    @SuppressWarnings("unchecked")
    public void startup() throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        Component content =
            (Component)wtkxSerializer.readObject("pivot/tutorials/lists/list_views.wtkx");

        final Label selectionLabel =
            (Label)wtkxSerializer.getObjectByName("selectionLabel");

        ListView listView = (ListView)wtkxSerializer.getObjectByName("listView");
        listView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
            public void selectionChanged(ListView listView) {
                String selectionText = "";

                Sequence<Span> selectedRanges = listView.getSelectedRanges();
                for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
                    Span selectedRange = selectedRanges.get(i);

                    for (int j = selectedRange.getStart();
                        j <= selectedRange.getEnd();
                        j++) {
                        Object item = listView.getListData().get(j);
                        Dictionary<String, Object> dictionary =
                            (Dictionary<String, Object>)item;

                        if (selectionText.length() > 0) {
                            selectionText += ", ";
                        }

                        selectionText += dictionary.get("label");
                    }
                }

                selectionLabel.setText(selectionText);
            }
        });

        window = new Window();
        window.setContent(content);
        window.setMaximized(true);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
