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
package org.apache.pivot.tutorials.webqueries;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;

/**
 * Web query tutorial application.
 */
public class WebQueries implements Application {
    private Window window = null;

    private ListView listView = null;
    private Label loadingLabel = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(WebQueries.class, "web_queries.bxml");

        listView = (ListView) bxmlSerializer.getNamespace().get("listView");
        loadingLabel = (Label) bxmlSerializer.getNamespace().get("loadingLabel");

        // Execute the query:
        // http://pipes.yahoo.com/pipes/pipe.run?_id=43115761f2da5af5341ae2e56a93d646&_render=json
        GetQuery getQuery = new GetQuery("pipes.yahoo.com", "/pipes/pipe.run");
        getQuery.getParameters().put("_id", "43115761f2da5af5341ae2e56a93d646");
        getQuery.getParameters().put("_render", "json");

        getQuery.execute(new TaskAdapter<>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                List<?> items = (List<?>) JSON.get(task.getResult(), "value.items");
                if (items.getLength() > 0) {
                    listView.setListData(items);
                    loadingLabel.setVisible(false);
                } else {
                    loadingLabel.setText("No results.");
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                loadingLabel.setText(task.getFault().getMessage());
            }
        }));

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(WebQueries.class, args);
    }

}
