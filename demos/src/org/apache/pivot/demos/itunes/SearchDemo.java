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
package org.apache.pivot.demos.itunes;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Comparator;
import java.util.Locale;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.Image;

public class SearchDemo implements Application {
    private Window window = null;

    @BXML private TextInput termTextInput;
    @BXML private PushButton searchButton;
    @BXML private Label statusLabel;
    @BXML private TableView resultsTableView;
    @BXML private BoxPane activityIndicatorBoxPane;
    @BXML private ActivityIndicator activityIndicator;
    @BXML private ImageView artworkImageView;
    @BXML private PushButton previewButton;

    private GetQuery getQuery = null;

    private final Image searchImage;
    private final Image cancelImage;

    public static final String APPLICATION_KEY = "application";
    public static final String QUERY_HOSTNAME = "ax.phobos.apple.com.edgesuite.net";
    public static final String BASE_QUERY_PATH = "/WebObjects/MZStoreServices.woa/wa/itmsSearch";
    public static final String MEDIA = "all";
    public static final int LIMIT = 100;

    public SearchDemo() throws Exception {
        searchImage = Image.load(getClass().getResource("magnifier.png"));
        cancelImage = Image.load(getClass().getResource("bullet_cross.png"));
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        bxmlSerializer.getNamespace().put(APPLICATION_KEY, this);

        window = (Window) bxmlSerializer.readObject(SearchDemo.class, "search_demo.bxml");
        bxmlSerializer.bind(this, SearchDemo.class);

        searchButton.setButtonData(searchImage);
        window.open(display);

        termTextInput.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * Executes a search.
     *
     * @param term The search term.
     * @throws IllegalArgumentException If <tt>term</tt> is <tt>null</tt> or
     * empty.
     * @throws IllegalStateException If a query is already executing.
     */
    public void executeQuery(String term) {
        Utils.checkNullOrEmpty(term, "search term");

        if (getQuery != null) {
            throw new IllegalStateException("Query is already running!");
        }

        String country = Locale.getDefault().getCountry().toLowerCase();

        getQuery = new GetQuery(QUERY_HOSTNAME, BASE_QUERY_PATH);
        getQuery.getParameters().put("term", term);
        getQuery.getParameters().put("country", country);
        getQuery.getParameters().put("media", MEDIA);
        getQuery.getParameters().put("limit", Integer.toString(LIMIT));
        getQuery.getParameters().put("output", "json");

        System.out.println(getQuery.getLocation());

        statusLabel.setText("Searching...");
        updateActivityState();

        getQuery.execute(new TaskAdapter<>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                if (task == getQuery) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> result = (Map<String, Object>) task.getResult();
                    @SuppressWarnings("unchecked")
                    List<Object> results = (List<Object>) result.get("results");

                    // Preserve any existing sort
                    @SuppressWarnings("unchecked")
                    List<Object> tableData = (List<Object>) resultsTableView.getTableData();
                    Comparator<Object> comparator = tableData.getComparator();
                    results.setComparator(comparator);

                    // Update the table data
                    resultsTableView.setTableData(results);
                    statusLabel.setText("Found " + results.getLength() + " matching items.");

                    getQuery = null;
                    updateActivityState();

                    if (results.getLength() > 0) {
                        resultsTableView.setSelectedIndex(0);
                        resultsTableView.requestFocus();
                    } else {
                        termTextInput.requestFocus();
                    }
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                if (task == getQuery) {
                    statusLabel.setText(task.getFault().getMessage());

                    getQuery = null;
                    updateActivityState();

                    termTextInput.requestFocus();
                }
            }
        }));
    }

    /**
     * Aborts an executing query.
     *
     * @throws IllegalStateException If a query is not currently executing.
     */
    public void abortQuery() {
        if (getQuery == null) {
            throw new IllegalStateException("Query is not running!");
        }

        getQuery.abort();

        getQuery = null;
        updateActivityState();
    }

    /**
     * Tests whether a query is currently executing.
     *
     * @return <tt>true</tt> if a query is currently executing; <tt>false</tt>,
     * otherwise.
     */
    public boolean isQueryExecuting() {
        return (getQuery != null);
    }

    private void updateActivityState() {
        boolean active = (getQuery != null);

        activityIndicatorBoxPane.setVisible(active);
        activityIndicator.setActive(active);

        termTextInput.setEnabled(!active);
        searchButton.setButtonData(active ? cancelImage : searchImage);
    }

    /**
     * Updates the artwork to reflect the current selection.
     */
    public void updateArtwork() {
        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) resultsTableView.getSelectedRow();

        URL artworkURL = null;
        if (result != null) {
            try {
                artworkURL = new URL((String) result.get("artworkUrl100"));
            } catch (MalformedURLException exception) {
                // ignore exception
            }
        }

        if (artworkURL == null) {
            artworkImageView.setImage((Image) null);
        } else {
            Image.load(artworkURL, new TaskAdapter<>(new TaskListener<Image>() {
                @Override
                public void taskExecuted(Task<Image> task) {
                    artworkImageView.setImage(task.getResult());
                }

                @Override
                public void executeFailed(Task<Image> task) {
                    artworkImageView.setImage((Image) null);
                }
            }));
        }

        previewButton.setEnabled(result != null);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(SearchDemo.class, args);
    }
}
