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
import java.net.URLEncoder;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSON;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupCloseListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputCharacterListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class SuggestionDemo implements Application {
    private Window window = null;
    private TextInput textInput = null;
    private ActivityIndicator activityIndicator = null;

    private SuggestionPopup suggestionPopup = new SuggestionPopup();
    private GetQuery suggestionQuery = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "suggestion_demo.wtkx");
        textInput = (TextInput)wtkxSerializer.get("textInput");
        activityIndicator = (ActivityIndicator)wtkxSerializer.get("activityIndicator");

        textInput.getTextInputCharacterListeners().add(new TextInputCharacterListener() {
            @Override
            public void charactersInserted(TextInput textInput, int index, int count) {
                getSuggestions();
            }

            @Override
            public void charactersRemoved(TextInput textInput, int index, int count) {
                if (suggestionQuery != null) {
                    suggestionQuery.abort();
                }

                suggestionPopup.close();
            }
        });

        window.open(display);
        textInput.requestFocus();
    }

    private void getSuggestions() {
        if (suggestionQuery != null
            && suggestionQuery.isPending()) {
            suggestionQuery.abort();
        }

        // Get the query text
        String text;
        try {
            text = URLEncoder.encode(textInput.getText(), "UTF-8");
        } catch (UnsupportedEncodingException exception) {
            throw new RuntimeException(exception);
        }

        // Create query
        suggestionQuery = new GetQuery("search.yahooapis.com", "/WebSearchService/V1/relatedSuggestion");
        suggestionQuery.getParameters().put("appid", getClass().getName());
        suggestionQuery.getParameters().put("query", text);
        suggestionQuery.getParameters().put("output", "json");

        suggestionQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                if (task == suggestionQuery) {
                    List<?> suggestions = null;

                    Object result = JSON.get(task.getResult(), "ResultSet.Result");
                    if (result instanceof List<?>) {
                        suggestions = (List<?>)result;
                    }

                    if (suggestions == null
                        || suggestions.getLength() == 0) {
                        suggestionPopup.close();
                    } else {
                        suggestionPopup.setSuggestions(suggestions);
                        suggestionPopup.open(textInput, new SuggestionPopupCloseListener() {
                            @Override
                            public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
                                if (suggestionPopup.getResult()) {
                                    String text;
                                    try {
                                        text = URLEncoder.encode(textInput.getText(), "UTF-8");
                                    } catch (UnsupportedEncodingException exception) {
                                        throw new RuntimeException(exception);
                                    }

                                    String location = "http://search.yahoo.com/search?p=" + text;

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

    public static void main(String[] args) {
        DesktopApplicationContext.main(SuggestionDemo.class, args);
    }
}
