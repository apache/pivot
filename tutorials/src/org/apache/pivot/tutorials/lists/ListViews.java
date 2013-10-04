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

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListItem;

public class ListViews extends Window implements Bindable {
    private Label selectionLabel = null;
    private ListView listView = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        selectionLabel = (Label) namespace.get("selectionLabel");
        listView = (ListView) namespace.get("listView");

        listView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
            @Override
            public void selectedRangeAdded(ListView listViewArgument, int rangeStart, int rangeEnd) {
                updateSelection(listViewArgument);
            }

            @Override
            public void selectedRangeRemoved(ListView listViewArgument, int rangeStart, int rangeEnd) {
                updateSelection(listViewArgument);
            }

            @Override
            public void selectedRangesChanged(ListView listViewArgument,
                Sequence<Span> previousSelectedRanges) {
                if (previousSelectedRanges != null
                    && previousSelectedRanges != listViewArgument.getSelectedRanges()) {
                    updateSelection(listViewArgument);
                }
            }

            @Override
            public void selectedItemChanged(ListView listViewArgument, Object previousSelectedItem) {
                // No-op
            }

            private void updateSelection(ListView listViewArgument) {
                // TODO: in future use StringBuffer instead ...
                String selectionText = "";

                Sequence<Span> selectedRanges = listViewArgument.getSelectedRanges();
                for (int i = 0, n = selectedRanges.getLength(); i < n; i++) {
                    Span selectedRange = selectedRanges.get(i);

                    for (int j = selectedRange.start; j <= selectedRange.end; j++) {
                        if (selectionText.length() > 0) {
                            selectionText += ", ";
                        }

                        Object item = listViewArgument.getListData().get(j);
                        String text;
                        if (item instanceof ListItem) { // item is a listItem
                                                        // (for example because
                                                        // it has an image)
                            text = ((ListItem) item).getText();
                        } else { // item is a standard item for listData
                            text = item.toString();
                        }
                        selectionText += text;
                    }
                }

                selectionLabel.setText(selectionText);
            }
        });
    }
}
