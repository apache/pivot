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
package pivot.wtkx.test.databinding;

import java.io.InputStream;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Form;

import pivot.wtk.Frame;
import pivot.wtk.ListView;
import pivot.wtkx.ComponentLoader;

public class DataBindingTest implements Application {
    public static class IDMapping implements ListView.ValueMapping {
        @SuppressWarnings("unchecked")
        public int indexOf(List<?> list, Object value) {
            int index = 0;
            int count = list.getLength();
            while (index < count) {
                Dictionary<String, Object> dictionary = (Dictionary<String, Object>)list.get(index);
                if (dictionary.get("id").equals(value)) {
                    break;
                }

                index++;
            }

            if (index == count) {
                index = -1;
            }

            return index;
        }

        @SuppressWarnings("unchecked")
        public Object valueOf(List<?> list, int index) {
            Dictionary<String, Object> dictionary = (Dictionary<String, Object>)list.get(index);
            Object value = dictionary.get("id");
            return value;
        }
    }

    private Frame frame = null;

    @SuppressWarnings("unchecked")
    public void startup() throws Exception {
        // Load the components from the XML file
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();
        Component component = componentLoader.load("pivot/wtkx/test/databinding/application.wtkx");

        // Create and open the frame
        frame = new Frame();
        frame.setContent(component);
        frame.open();

        // Populate the form
        InputStream dataStream = getClass().getResourceAsStream("application.json");
        JSONSerializer jsonSerializer = new JSONSerializer();
        Map<String, Object> data = (Map<String, Object>)jsonSerializer.readObject(dataStream);

        Form form = (Form)componentLoader.getComponent("form");
        form.load(data);
    }

    public void shutdown() throws Exception {
        frame.close();
    }

    public void resume() throws Exception {
    }

    public void suspend() throws Exception {
    }
}
