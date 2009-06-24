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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputCharacterListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class Text implements Application {
    private Window window = null;

    @WTKX private TextInput stateTextInput;

    private ArrayList<String> states;

    private TextInputCharacterListener textInputCharacterListener =
        new TextInputCharacterListener.Adapter() {
        public void charactersInserted(final TextInput textInput, int index, int count) {
            String text = textInput.getText();

            int i = ArrayList.binarySearch(states, text,
                states.getComparator());

            if (i < 0) {
                i = -(i + 1);
                int n = states.getLength();

                if (i < n) {
                    text = text.toLowerCase();
                    final String state = states.get(i);

                    if (state.toLowerCase().startsWith(text)) {
                        String nextState = (i == n - 1) ?
                            null : states.get(i + 1);

                        if (nextState == null
                            || !nextState.toLowerCase().startsWith(text)) {
                            textInput.setText(state);
                        }
                    }
                }
            }
        }
    };

    public Text() {
        // Populate the lookup values, ensuring that they are sorted
        states = new ArrayList<String>();
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

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "text.wtkx");
        wtkxSerializer.bind(this, Text.class);

        stateTextInput.getTextInputCharacterListeners().add(textInputCharacterListener);

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Text.class, args);
    }
}
