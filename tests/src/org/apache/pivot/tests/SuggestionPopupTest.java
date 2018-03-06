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
package org.apache.pivot.tests;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupCloseListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;

public final class SuggestionPopupTest implements Application {
    private Window window = null;

    @BXML private TextInput textInput = null;
    @BXML private Label selectedIndexLabel = null;

    private SuggestionPopup suggestionPopup = new SuggestionPopup();

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(SuggestionPopupTest.class,
            "suggestion_popup_test.bxml");
        bxmlSerializer.bind(this);

        textInput.getTextInputContentListeners().add(new TextInputContentListener() {
            @Override
            public void textInserted(final TextInput textInputArgument, final int index, final int count) {
                ArrayList<String> suggestions = new ArrayList<>("One", "Two", "Three", "Four", "Five");
                suggestionPopup.setSuggestionData(suggestions);
                suggestionPopup.open(textInputArgument, new SuggestionPopupCloseListener() {
                    @Override
                    public void suggestionPopupClosed(final SuggestionPopup suggestionPopupArgument) {
                        if (suggestionPopupArgument.getResult()) {
                            selectedIndexLabel.setText("You selected suggestion number "
                                + suggestionPopupArgument.getSelectedIndex() + ".");
                        } else {
                            selectedIndexLabel.setText("You didn't select anything.");
                        }
                    }
                });
            }

            @Override
            public void textRemoved(final TextInput textInputArgument, final int index, final int count) {
                suggestionPopup.close();
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(SuggestionPopupTest.class, args);
    }
}
