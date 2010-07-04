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
package org.apache.pivot.demos.lists;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;

public class MultiSelect extends Window implements Bindable {
    private ListView dataListView = null;
    private ListView selectionListView = null;

    @Override
    public void initialize(Dictionary<String, Object> namespace, URL location, Resources resources) {
        dataListView = (ListView)namespace.get("dataListView");
        selectionListView = (ListView)namespace.get("selectionListView");

        dataListView.getListViewSelectionListeners().add(new ListViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
                refreshSelectionListData();
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        refreshSelectionListData();
        dataListView.requestFocus();
    }

    private void refreshSelectionListData() {
        selectionListView.setListData(new ArrayList<Span>(dataListView.getSelectedRanges()));
    }
}
