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
package org.apache.pivot.tutorials.layout;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

public class Forms extends Window implements Bindable {
    private ListButton titleListButton = null;
    private BoxPane nameBoxPane = null;
    private TextInput lastNameTextInput = null;
    private TextInput firstNameTextInput = null;
    private PushButton submitButton = null;
    private Label errorLabel = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        titleListButton = (ListButton) namespace.get("titleListButton");
        nameBoxPane = (BoxPane) namespace.get("nameBoxPane");
        lastNameTextInput = (TextInput) namespace.get("lastNameTextInput");
        firstNameTextInput = (TextInput) namespace.get("firstNameTextInput");
        submitButton = (PushButton) namespace.get("submitButton");
        errorLabel = (Label) namespace.get("errorLabel");

        submitButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                String titleOfPerson = (String) titleListButton.getButtonData();
                System.out.println("The title of the person is: \"" + titleOfPerson + "\"");

                String lastName = lastNameTextInput.getText();
                String firstName = firstNameTextInput.getText();

                Form.Flag flag = null;
                if (lastName.length() == 0 || firstName.length() == 0) {
                    flag = new Form.Flag(MessageType.ERROR, "Name is required.");
                }

                Form.setFlag(nameBoxPane, flag);

                if (flag == null) {
                    errorLabel.setText("");
                    Prompt.prompt("Pretending to submit...", Forms.this);
                } else {
                    errorLabel.setText("Some required information is missing.");
                }
            }
        });
    }

}
