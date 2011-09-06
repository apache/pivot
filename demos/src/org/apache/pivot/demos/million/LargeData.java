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
package org.apache.pivot.demos.million;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.io.IOTask;
import org.apache.pivot.serialization.CSVSerializerListener;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TableViewRowComparator;

public class LargeData implements Application {
    private class LoadDataTask extends IOTask<Void> {
        private URL fileURL;

        public LoadDataTask(URL fileURL) {
            this.fileURL = fileURL;
        }

        @Override
        public Void execute() throws TaskExecutionException {
            try {
                InputStream inputStream = null;

                try {
                    inputStream = new MonitoredInputStream(fileURL.openStream());

                    CSVSerializer csvSerializer = new CSVSerializer();
                    csvSerializer.setKeys("c0", "c1", "c2", "c3");
                    csvSerializer.getCSVSerializerListeners().add(new CSVSerializerListener.Adapter() {
                        private ArrayList<Object> page = new ArrayList<Object>(pageSize);

                        @Override
                        public void endList(CSVSerializer csvSerializer) {
                            if (page.getLength() > 0) {
                                ApplicationContext.queueCallback(new AddRowsCallback(page));
                            }
                        }

                        @Override
                        public void readItem(CSVSerializer csvSerializer, Object item) {
                            page.add(item);

                            if (page.getLength() == pageSize) {
                                ApplicationContext.queueCallback(new AddRowsCallback(page));
                                page = new ArrayList<Object>(pageSize);
                            }
                        }
                    });

                    csvSerializer.readObject(inputStream);
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch(IOException exception) {
                throw new TaskExecutionException(exception);
            } catch(SerializationException exception) {
                throw new TaskExecutionException(exception);
            }

            return null;
        }
    }

    private class AddRowsCallback implements Runnable {
        private ArrayList<Object> page;

        public AddRowsCallback(ArrayList<Object> page) {
            this.page = page;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            List<Object> tableData = (List<Object>)tableView.getTableData();
            for (Object item : page) {
                tableData.add(item);
            }
        }
    }

    private String basePath = null;

    private Window window = null;
    private ListButton fileListButton = null;
    private PushButton loadDataButton = null;
    private PushButton cancelButton = null;
    private Label statusLabel = null;
    private TableView tableView = null;

    private int pageSize = 0;
    private LoadDataTask loadDataTask = null;

    private static final String BASE_PATH_KEY = "basePath";

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        basePath = properties.get(BASE_PATH_KEY);
        if (basePath == null) {
            throw new IllegalArgumentException(BASE_PATH_KEY + " is required.");
        }

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(LargeData.class, "large_data.bxml");
        fileListButton = (ListButton)bxmlSerializer.getNamespace().get("fileListButton");
        loadDataButton = (PushButton)bxmlSerializer.getNamespace().get("loadDataButton");
        cancelButton = (PushButton)bxmlSerializer.getNamespace().get("cancelButton");
        statusLabel = (Label)bxmlSerializer.getNamespace().get("statusLabel");
        tableView = (TableView)bxmlSerializer.getNamespace().get("tableView");

        loadDataButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                loadDataButton.setEnabled(false);
                cancelButton.setEnabled(true);

                loadData();
            }
        });

        cancelButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                if (loadDataTask != null) {
                    loadDataTask.abort();
                }

                loadDataButton.setEnabled(true);
                cancelButton.setEnabled(false);
            }
        });

        tableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView) {
                List<Object> tableData = (List<Object>)tableView.getTableData();

                long startTime = System.currentTimeMillis();
                tableData.setComparator(new TableViewRowComparator(tableView));
                long endTime = System.currentTimeMillis();

                statusLabel.setText("Data sorted in " + (endTime - startTime) + " ms.");
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
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

    private void loadData() {
        int index = fileListButton.getSelectedIndex();
        int capacity = (int)Math.pow(10, index + 1);
        tableView.setTableData(new ArrayList<Object>(capacity));

        pageSize = Math.max(capacity / 1000, 100);

        String fileName = (String)fileListButton.getSelectedItem();

        URL origin = ApplicationContext.getOrigin();

        URL fileURL = null;
        try {
            fileURL = new URL(origin, basePath + "/" + fileName);
        } catch(MalformedURLException exception) {
            System.err.println(exception.getMessage());
        }

        if (fileURL != null) {
            statusLabel.setText("Loading " + fileURL);

            final long t0 = System.currentTimeMillis();

            loadDataTask = new LoadDataTask(fileURL);
            loadDataTask.execute(new TaskAdapter<Void>(new TaskListener<Void>() {
                @Override
                public void taskExecuted(Task<Void> task) {
                    long t1 = System.currentTimeMillis();

                    statusLabel.setText("Read " + tableView.getTableData().getLength() + " rows in "
                        + (t1 - t0) + "ms");
                    loadDataButton.setEnabled(true);
                    cancelButton.setEnabled(false);

                    loadDataTask = null;
                }

                @Override
                public void executeFailed(Task<Void> task) {
                    String taskFault = task.getFault().toString();
                    System.err.println(taskFault);
                    statusLabel.setText(taskFault);
                    loadDataButton.setEnabled(true);
                    cancelButton.setEnabled(false);

                    loadDataTask = null;
                }
            }));
        }
    }
}
