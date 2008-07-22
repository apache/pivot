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
package pivot.tutorials.text;

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.TextInput;
import pivot.wtk.TextInputCharacterListener;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class Text implements Application {
    private Window window = new Window();
    private ArrayList<String> states = new ArrayList<String>();

    public Text() {
        // Populate the lookup values, ensuring that they are sorted
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

    public void startup() throws Exception {
        ComponentLoader componentLoader = new ComponentLoader();
        Component content = componentLoader.load("pivot/tutorials/text/text.wtkx");

        TextInput stateTextInput =
            (TextInput)componentLoader.getComponent("stateTextInput");

        stateTextInput.getTextInputCharacterListeners().add(new
            TextInputCharacterListener() {
            public void charactersInserted(TextInput textInput,
                int index, int count) {
                String text = textInput.getText();

                int i = Sequence.Search.binarySearch(states, text,
                    states.getComparator());

                if (i < 0) {
                    i = -(i + 1);
                    int n = states.getLength();

                    if (i < n) {
                        text = text.toLowerCase();
                        String state = states.get(i);

                        if (state.toLowerCase().startsWith(text)) {
                            String nextState = (i == n - 1) ?
                                null : states.get(i + 1);

                            if (nextState == null
                                || !nextState.toLowerCase().startsWith(text)) {
                                textInput.setText(state);
                                textInput.setSelection(state.length(), 0);
                            }
                        }
                    }
                }
            }

            public void charactersRemoved(TextInput textInput, int index, int count) {
            }

            public void charactersReset(TextInput textInput) {
            }
        });

        window.setContent(content);
        window.setMaximized(true);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
