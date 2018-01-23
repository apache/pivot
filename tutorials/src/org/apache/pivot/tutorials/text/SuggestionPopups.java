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
package org.apache.pivot.tutorials.text;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;

public class SuggestionPopups extends Window implements Bindable {
    private TextInput stateTextInput = null;

    private ArrayList<String> states;
    private SuggestionPopup suggestionPopup = new SuggestionPopup();

    public SuggestionPopups() {
        // Populate the lookup values, ensuring that they are sorted
        states = new ArrayList<>();
        states.setComparator(String.CASE_INSENSITIVE_ORDER);

        states.add("Alabama");
        states.add("Alaska");
        states.add("Arizona");
        states.add("Arkansas");
        states.add("California");
        states.add("Colorado");
        states.add("Connecticut");
        states.add("Delaware");
        states.add("District of Columbia");
        states.add("Florida");
        states.add("Georgia");
        states.add("Hawaii");
        states.add("Idaho");
        states.add("Illinois");
        states.add("Indiana");
        states.add("Iowa");
        states.add("Kansas");
        states.add("Kentucky");
        states.add("Louisiana");
        states.add("Maine");
        states.add("Maryland");
        states.add("Massachusetts");
        states.add("Michigan");
        states.add("Minnesota");
        states.add("Mississippi");
        states.add("Missouri");
        states.add("Montana");
        states.add("Nebraska");
        states.add("Nevada");
        states.add("New Hampshire");
        states.add("New Jersey");
        states.add("New Mexico");
        states.add("New York");
        states.add("North Carolina");
        states.add("North Dakota");
        states.add("Ohio");
        states.add("Oklahoma");
        states.add("Oregon");
        states.add("Pennsylvania");
        states.add("Rhode Island");
        states.add("South Carolina");
        states.add("South Dakota");
        states.add("Tennessee");
        states.add("Texas");
        states.add("Utah");
        states.add("Vermont");
        states.add("Virginia");
        states.add("Washington");
        states.add("West Virginia");
        states.add("Wisconsin");
        states.add("Wyoming");
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        stateTextInput = (TextInput) namespace.get("stateTextInput");
        stateTextInput.getTextInputContentListeners().add(new TextInputContentListener() {
            @Override
            public void textInserted(TextInput textInput, int index, int count) {
                String text = textInput.getText();
                ArrayList<String> suggestions = new ArrayList<>();

                for (String state : states) {
                    if (state.toUpperCase().startsWith(text.toUpperCase())) {
                        suggestions.add(state);
                    }
                }

                if (suggestions.getLength() > 0) {
                    suggestionPopup.setSuggestionData(suggestions);
                    suggestionPopup.open(textInput);
                }
            }

            @Override
            public void textRemoved(TextInput textInput, int index, int count) {
                suggestionPopup.close();
            }
        });

        suggestionPopup.setListSize(4);
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);
        stateTextInput.requestFocus();
    }
}
