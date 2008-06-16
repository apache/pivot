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
package pivot.wtk.test;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.wtk.Application;
import pivot.wtk.ListView;
import pivot.wtk.Span;

public class ListViewSelectionTest implements Application {
    private ListView listView = new ListView();

    @SuppressWarnings("unchecked")
    public ListViewSelectionTest() {
        List<Object> listData = (List<Object>)listView.getListData();

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

    public void startup() throws Exception {
        ArrayList<Span> selectedRanges = new ArrayList<Span>();
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
    }

    public void shutdown() throws Exception {
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }

    protected void dumpSelection() {
        System.out.println("Selected " + listView.getSelectedRanges());
    }

    protected void verifySelection(int index) {
        System.out.println("Index " + index + " "
            + (listView.isIndexSelected(index) ? "is" : "is not") + " selected");
    }
}
