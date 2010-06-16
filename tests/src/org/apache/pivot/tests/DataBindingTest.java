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

import org.apache.pivot.beans.BeanSerializer;
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

public class DataBindingTest implements Application {
    public static class TestListButtonDataRenderer extends ListButtonDataRenderer {
        @Override
        public void render(Object data, Button button, boolean highlighted) {
            if (data != null) {
                data = JSON.getString(data, "text");
            }

            super.render(data, button, highlighted);
        }

        @Override
        public String toString(Object data) {
            return JSON.getString(data, "text");
        }
    }

    public static class TestListViewItemRenderer extends ListViewItemRenderer {
        @Override
        public void render(Object item, int index, ListView listView, boolean selected,
            boolean checked, boolean highlighted, boolean disabled) {
            if (item != null) {
                item = JSON.getString(item, "text");
            }

            super.render(item, index, listView, selected, checked, highlighted, disabled);
        }

        @Override
        public String toString(Object item) {
            return JSON.getString(item, "text");
        }
    }

    public static class TestSpinnerItemRenderer extends SpinnerItemRenderer {
        @Override
        public void render(Object item, Spinner spinner) {
            if (item != null) {
                item = JSON.getString(item, "text");
            }

            super.render(item, spinner);
        }

        @Override
        public String toString(Object item) {
            return JSON.getString(item, "text");
        }
    }

    public static class TestBindMapping implements ListView.ItemBindMapping, Spinner.ItemBindMapping {
        @SuppressWarnings("unchecked")
        public int indexOf(List<?> list, Object value) {
            int i = 0;
            int n = list.getLength();
            while (i < n) {
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

        @SuppressWarnings("unchecked")
        public Object get(List<?> list, int index) {
            Map<String, ?> map = (Map<String, ?>)list.get(index);
            return map.get("id");
        }
    }

    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BeanSerializer beanSerializer = new BeanSerializer();
        window = (Window)beanSerializer.readObject(this, "data_binding_test.bxml");
        window.open(display);

        HashMap<String, Object> context = new HashMap<String, Object>();
        context.put("id1", "1");
        context.put("id2", "2");
        context.put("id3", "3");

        window.getContent().load(context);

        context = new HashMap<String, Object>();
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

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(DataBindingTest.class, args);
    }
}
