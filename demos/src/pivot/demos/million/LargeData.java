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
package pivot.demos.million;

import java.net.URL;

import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.serialization.CSVSerializer;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.web.GetQuery;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.ListButton;
import pivot.wtk.PushButton;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TaskAdapter;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class LargeData implements Application {
    private String basePath = null;

	private Window window = null;

    private ListButton fileListButton = null;
    private PushButton loadDataButton = null;
    private Label statusLabel = null;
    private TableView tableView = null;
    private TableViewHeader tableViewHeader = null;

    private GetQuery getQuery = null;
    private CSVSerializer csvSerializer;

    private static final String BASE_PATH_KEY = "basePath";

    public LargeData() {
    	csvSerializer = new CSVSerializer("ISO-8859-1");
    	csvSerializer.getKeys().add("c0");
    	csvSerializer.getKeys().add("c1");
    	csvSerializer.getKeys().add("c2");
    	csvSerializer.getKeys().add("c3");
    }

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        basePath = properties.get(BASE_PATH_KEY);
        if (basePath == null) {
            throw new IllegalArgumentException("basePath is required.");
        }

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(getClass().getResource("large_data.wtkx"));

        fileListButton = (ListButton)wtkxSerializer.getObjectByName("fileListButton");

        loadDataButton = (PushButton)wtkxSerializer.getObjectByName("loadDataButton");
        loadDataButton.getButtonPressListeners().add(new ButtonPressListener() {
        	public void buttonPressed(Button button) {
        		button.setEnabled(false);
        		loadData();
        	}
        });

        statusLabel = (Label)wtkxSerializer.getObjectByName("statusLabel");

        tableView = (TableView)wtkxSerializer.getObjectByName("tableView");

        tableViewHeader = (TableViewHeader)wtkxSerializer.getObjectByName("tableViewHeader");
        tableViewHeader.getTableViewHeaderPressListeners().add(new TableView.SortHandler() {
        	@Override
        	public void headerPressed(TableViewHeader tableViewHeader, int index) {
        		long startTime = System.currentTimeMillis();
        		super.headerPressed(tableViewHeader, index);
        		long endTime = System.currentTimeMillis();

        		statusLabel.setText("Data sorted in " + (endTime - startTime) + " ms.");
        	}
        });

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    private void loadData() {
    	String fileName = (String)fileListButton.getSelectedValue();

    	URL origin = ApplicationContext.getOrigin();
    	System.out.println(origin);

    	getQuery = new GetQuery(origin.getHost(), origin.getPort(), basePath + fileName, false);
    	String location = getQuery.getLocation().toString();
    	statusLabel.setText("Loading data from " + location);
    	System.out.println(location);

    	getQuery.setSerializer(csvSerializer);

    	final long startTime = System.currentTimeMillis();

        getQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            public void taskExecuted(Task<Object> task) {
                if (task == getQuery) {
                    long endTime = System.currentTimeMillis();
                    statusLabel.setText("Data loaded in " + (endTime - startTime) + " ms.");

                    tableView.setTableData((List<?>)task.getResult());

                    getQuery = null;
                    loadDataButton.setEnabled(true);
                }
            }

            public void executeFailed(Task<Object> task) {
                if (task == getQuery) {
                	statusLabel.setText(task.getFault().getMessage());
                    getQuery = null;
                    loadDataButton.setEnabled(true);
                }
            }
        }));
    }
}
