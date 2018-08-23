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
package org.apache.pivot.demos.suggest;

import java.awt.Desktop;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.Constants;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupCloseListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;

public class SuggestionDemo extends Window implements Bindable {
    private TextInput textInput = null;
    private ActivityIndicator activityIndicator = null;

    private SuggestionPopup suggestionPopup = new SuggestionPopup();
    private GetQuery suggestionQuery = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        textInput = (TextInput) namespace.get("textInput");
        activityIndicator = (ActivityIndicator) namespace.get("activityIndicator");

        textInput.getTextInputContentListeners().add(new TextInputContentListener() {
            @Override
            public void textInserted(TextInput textInputArgument, int index, int count) {
                getSuggestions();
            }

            @Override
            public void textRemoved(TextInput textInputArgument, int index, int count) {
                if (suggestionQuery != null) {
                    suggestionQuery.abort();
                }

                suggestionPopup.close();
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);
        textInput.requestFocus();
    }

    private void getSuggestions() {
        if (suggestionQuery != null && suggestionQuery.isPending()) {
            suggestionQuery.abort();
        }

        // Get the query text
        String text;
        try {
            text = URLEncoder.encode(textInput.getText(), Constants.URL_ENCODING);
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception);
        }

        // Create query
        suggestionQuery = new GetQuery("search.yahooapis.com",
            "/WebSearchService/V1/relatedSuggestion");
        suggestionQuery.getParameters().put("appid", getClass().getName());
        suggestionQuery.getParameters().put("query", text);
        suggestionQuery.getParameters().put("output", "json");

        suggestionQuery.execute(new TaskAdapter<>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                if (task == suggestionQuery) {
                    List<?> suggestions = null;

                    Object result = JSON.get(task.getResult(), "ResultSet.Result");
                    if (result instanceof List<?>) {
                        suggestions = (List<?>) result;
                    }

                    if (suggestions == null || suggestions.getLength() == 0) {
                        suggestionPopup.close();
                    } else {
                        suggestionPopup.setSuggestionData(suggestions);
                        suggestionPopup.open(textInput, new SuggestionPopupCloseListener() {
                            @Override
                            public void suggestionPopupClosed(
                                SuggestionPopup suggestionPopupArgument) {
                                if (suggestionPopupArgument.getResult()) {
                                    String textLocal;
                                    try {
                                        textLocal = URLEncoder.encode(textInput.getText(), Constants.URL_ENCODING);
                                    } catch (UnsupportedEncodingException exception) {
                                        throw new RuntimeException(exception);
                                    }

                                    String location = "http://search.yahoo.com/search?p="
                                        + textLocal;

                                    try {
                                        Desktop.getDesktop().browse(new URI(location));
                                    } catch (IOException exception) {
                                        System.err.println(exception);
                                    } catch (URISyntaxException exception) {
                                        System.err.println(exception);
                                    }
                                }
                            }
                        });
                    }

                    activityIndicator.setActive(false);
                    suggestionQuery = null;
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                if (task == suggestionQuery) {
                    System.err.println(task.getFault());
                    activityIndicator.setActive(false);
                    suggestionQuery = null;
                }
            }
        }));

        activityIndicator.setActive(true);
    }
}
