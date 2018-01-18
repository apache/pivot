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
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Window;

public class ListViewTest implements Application {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) {
        ListView listView = new ListView();

        List<String> listData = new ArrayList<>();
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
        listView.setDisabledItemFilter(new Filter<String>() {
            @Override
            public boolean include(String item) {
                return !Character.isDigit(item.charAt(0));
            }
        });

        listView.setCheckmarksEnabled(true);
        listView.setItemChecked(4, true);
        listView.setItemChecked(6, true);

        window = new Window(listView);
        window.open(display);

        listData.insert("-1", 0);
        listData.insert("-2", 0);

        listData.remove(0, 3);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ListViewTest.class, args);
    }
}
