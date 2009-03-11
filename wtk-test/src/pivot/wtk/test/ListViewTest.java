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
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.ListView;
import pivot.wtk.Window;

public class ListViewTest implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties) {
        ListView listView = new ListView();

        List<Object> listData = new ArrayList<Object>();
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

        listView.setListData(listData);

        listView.setItemDisabled(3, true);
        listView.setItemDisabled(5, true);

        listView.setCheckmarksEnabled(true);
        listView.setItemChecked(4, true);
        listView.setItemChecked(6, true);

        window = new Window(listView);
        window.open(display);

        listData.insert("-1", 0);
        listData.insert("-2", 0);

        listData.remove(0, 3);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
