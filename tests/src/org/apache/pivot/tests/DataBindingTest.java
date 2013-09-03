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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ListButtonDataRenderer;
import org.apache.pivot.wtk.content.ListViewItemRenderer;
import org.apache.pivot.wtk.content.SpinnerItemRenderer;

public class DataBindingTest extends Application.Adapter {
    public static class TestListButtonDataRenderer extends ListButtonDataRenderer {
        @Override
        public void render(final Object data, Button button, boolean highlighted) {
            Object dataLoaded = data;
            if (dataLoaded != null) {
                dataLoaded = JSON.get(data, "text");
            }

            super.render(dataLoaded, button, highlighted);
        }

        @Override
        public String toString(Object data) {
            return JSON.get(data, "text");
        }
    }

    public static class TestListViewItemRenderer extends ListViewItemRenderer {
        @Override
        public void render(final Object item, int index, final ListView listView, boolean selected,
            boolean checked, boolean highlighted, boolean disabled) {
            Object itemLoaded = item;
            if (itemLoaded != null) {
                itemLoaded = JSON.get(item, "text");
            }

            super.render(itemLoaded, index, listView, selected, checked, highlighted, disabled);
        }

        @Override
        public String toString(Object item) {
            return JSON.get(item, "text");
        }
    }

    public static class TestSpinnerItemRenderer extends SpinnerItemRenderer {
        @Override
        public void render(final Object item, final Spinner spinner) {
            Object itemLoaded = item;
            if (itemLoaded != null) {
                itemLoaded = JSON.get(item, "text");
            }

            super.render(itemLoaded, spinner);
        }

        @Override
        public String toString(Object item) {
            return JSON.get(item, "text");
        }
    }

    public static class TestBindMapping implements ListView.ItemBindMapping, Spinner.ItemBindMapping {
        @Override
        public int indexOf(List<?> list, Object value) {
            int i = 0;
            int n = list.getLength();
            while (i < n) {
                @SuppressWarnings("unchecked")
                Map<String, ?> map = (Map<String, ?>)list.get(i);
                if (map.get("id").equals(value)) {
                    break;
                }

                i++;
            }

            if (i == n) {
                i = -1;
            }

            return i;
        }

        @Override
        public Object get(List<?> list, int index) {
            @SuppressWarnings("unchecked")
            Map<String, ?> map = (Map<String, ?>)list.get(index);
            return map.get("id");
        }
    }

    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(DataBindingTest.class, "data_binding_test.bxml");
        window.open(display);

        HashMap<String, Object> context = new HashMap<>();
        context.put("id1", "1");
        context.put("id2", "2");
        context.put("id3", "3");

        window.getContent().load(context);

        context = new HashMap<>();
        window.getContent().store(context);

        System.out.println(JSONSerializer.toString(context));
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DataBindingTest.class, args);
    }
}
