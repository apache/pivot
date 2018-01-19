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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Span;

public class ListViewSelectionTest implements Application {
    private ListView listView = new ListView();

    public ListViewSelectionTest() {
        @SuppressWarnings("unchecked")
        List<Object> listData = (List<Object>) listView.getListData();

        listData.add("0");
        listData.add("1");
        listData.add("2");
        listData.add("3");
        listData.add("4");
        listData.add("5");
        listData.add("6");
        listData.add("7");
        listData.add("8");
        listData.add("9");
        listData.add("A");
        listData.add("B");
        listData.add("C");
        listData.add("D");
        listData.add("E");
        listData.add("F");

        listView.setSelectMode(ListView.SelectMode.MULTI);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) {
        ArrayList<Span> selectedRanges = new ArrayList<>();
        selectedRanges.add(new Span(0, 0));

        listView.setSelectedRanges(selectedRanges);
        dumpSelection();

        listView.addSelectedRange(new Span(4, 4));
        dumpSelection();

        listView.addSelectedRange(new Span(2, 2));
        dumpSelection();

        listView.addSelectedRange(new Span(0, 4));
        dumpSelection();

        selectedRanges.clear();
        selectedRanges.add(new Span(1, 1));
        selectedRanges.add(new Span(3, 3));

        listView.setSelectedRanges(selectedRanges);
        dumpSelection();

        listView.addSelectedRange(new Span(0, 4));
        dumpSelection();

        listView.removeSelectedRange(new Span(2, 2));
        dumpSelection();

        listView.removeSelectedRange(new Span(4, 4));
        dumpSelection();

        listView.removeSelectedRange(new Span(0, 0));
        dumpSelection();

        listView.removeSelectedRange(new Span(1, 3));
        dumpSelection();

        selectedRanges.clear();
        selectedRanges.add(new Span(4, 6));
        listView.setSelectedRanges(selectedRanges);
        dumpSelection();

        listView.addSelectedRange(new Span(2, 5));
        dumpSelection();

        listView.addSelectedRange(new Span(4, 8));
        dumpSelection();

        verifySelection(0);
        verifySelection(4);
        verifySelection(6);
        verifySelection(8);

        listView.removeSelectedRange(new Span(8, 12));
        dumpSelection();
        verifySelection(8);

        listView.removeSelectedRange(new Span(0, 4));
        dumpSelection();
        verifySelection(4);

        listView.getListViewSelectionListeners().add(new ListViewSelectionListener() {
            @Override
            public void selectedRangesChanged(ListView listViewArgument,
                Sequence<Span> previousSelectedRanges) {
                System.out.println("Selection changed");
            }
        });

        listView.setSelectedIndex(2);
        listView.getListData().remove(2, 1);
    }

    @Override
    public boolean shutdown(boolean optional) {
        return false;
    }

    protected void dumpSelection() {
        System.out.println("Selected " + listView.getSelectedRanges());
    }

    protected void verifySelection(int index) {
        System.out.println("Index " + index + " "
            + (listView.isItemSelected(index) ? "is" : "is not") + " selected");
    }

    public static void main(String[] args) {
        // Note that when run as Application, no List elements will be displayed
        // on the screen, but only some messages will be displayed to the text
        // console
        DesktopApplicationContext.main(ListViewSelectionTest.class, args);
    }

}
