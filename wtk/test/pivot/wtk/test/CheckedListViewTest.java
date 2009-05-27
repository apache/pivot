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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.ListView;
import pivot.wtk.Window;

public class CheckedListViewTest implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        ListView listView = new ListView(JSONSerializer.parseList("['One', 'Two', 'Three', 'Four']"));
        listView.setSelectMode(ListView.SelectMode.MULTI);
        listView.setCheckmarksEnabled(true);
        listView.setItemChecked(0, true);
        listView.setItemChecked(2, true);

        window = new Window(listView);
        window.setTitle("Checked List View Test");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void resume() {
    }


    public void suspend() {
    }
}
