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
package org.apache.pivot.wtk.test;

import static org.junit.Assert.*;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.junit.Test;

public class MultiSelectTest {
    private ListView listView1 = new ListView();
    private ListView listView2 = new ListView();

    @Test
    public void basicTest() {
        listView1.getListViewSelectionListeners().add(new ListViewSelectionListener() {
            @Override
            public void selectedRangeAdded(ListView listView, int rangeStart, int rangeEnd) {
                listView2.addSelectedRange(rangeStart, rangeEnd);
            }

            @Override
            public void selectedRangeRemoved(ListView listView, int rangeStart, int rangeEnd) {
                listView2.removeSelectedRange(rangeStart, rangeEnd);
            }

            @Override
            public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
                listView2.setSelectedRanges(listView1.getSelectedRanges());
            }
        });

        NumericSpinnerData listData = new NumericSpinnerData(0, 255, 1);

        listView1.setListData(listData);
        listView1.setSelectMode(ListView.SelectMode.MULTI);

        listView2.setListData(listData);
        listView2.setSelectMode(ListView.SelectMode.MULTI);

        assertEquals(listView1.addSelectedRange(0, 4).getLength(), 1);
        assertEquals(listView1.addSelectedRange(0, 4).getLength(), 0);
        assertEquals(listView1.addSelectedRange(8, 12).getLength(), 1);
        assertEquals(listView1.addSelectedRange(4, 7).getLength(), 1);
        assertEquals(listView1.removeSelectedRange(1, 1).getLength(), 1);
        assertEquals(listView1.removeSelectedRange(0, 0).getLength(), 1);
        assertEquals(listView1.removeSelectedRange(0, 0).getLength(), 0);
        assertEquals(listView1.removeSelectedRange(14, 14).getLength(), 0);
        assertEquals(listView1.addSelectedRange(16, 19).getLength(), 1);
        assertEquals(listView1.removeSelectedRange(10, 17).getLength(), 2);

        Sequence<Span> selectedRanges = listView1.getSelectedRanges();
        assertEquals(selectedRanges.getLength(), 2);
        assertEquals(selectedRanges.get(0), new Span(2, 9));
        assertEquals(selectedRanges.get(1), new Span(18, 19));

        assertEquals(listView1.removeSelectedRange(1, 20).getLength(), 2);
        assertEquals(listView1.removeSelectedRange(0, 5).getLength(), 0);

        compareSelectionState();
    }

    private void compareSelectionState() {
        Sequence<Span> selectedRanges1 = listView1.getSelectedRanges();
        Sequence<Span> selectedRanges2 = listView2.getSelectedRanges();

        assertEquals(selectedRanges1.getLength(), selectedRanges2.getLength());

        for (int i = 0, n = selectedRanges1.getLength(); i < n; i++) {
            assertEquals(selectedRanges1.get(i), selectedRanges2.get(i));
        }
    }
}
