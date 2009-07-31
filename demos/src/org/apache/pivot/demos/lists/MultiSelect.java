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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class MultiSelect implements Application {
    private Window window = null;
    private ListView dataListView = null;
    private ListView selectionListView = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "multi_select.wtkx");

        dataListView = (ListView)wtkxSerializer.get("dataListView");
        selectionListView = (ListView)wtkxSerializer.get("selectionListView");

        dataListView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
            public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
                refreshSelectionListData();
            }

            public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
                refreshSelectionListData();
            }

            public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
                refreshSelectionListData();
            }

            private void refreshSelectionListData() {
                selectionListView.setListData(new ArrayList<Span>(dataListView.getSelectedRanges()));
            }
        });

        window.open(display);
        dataListView.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(MultiSelect.class, args);
    }
}
