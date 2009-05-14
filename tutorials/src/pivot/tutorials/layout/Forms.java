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
package pivot.tutorials.layout;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Form;
import pivot.wtk.Label;
import pivot.wtk.MessageType;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.TextInput;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class Forms extends Bindable implements Application {
    @Load(resourceName="forms.wtkx") private Window window;
    @Bind(fieldName="window") private FlowPane nameFlowPane;
    @Bind(fieldName="window") private TextInput lastNameTextInput;
    @Bind(fieldName="window") private TextInput firstNameTextInput;
    @Bind(fieldName="window") private PushButton submitButton;
    @Bind(fieldName="window") private Label errorLabel;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        bind();

        submitButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                String lastName = lastNameTextInput.getText();
                String firstName = firstNameTextInput.getText();

                Form.Flag flag = null;
                if (lastName.length() == 0
                    || firstName.length() == 0) {
                    flag = new Form.Flag(MessageType.ERROR, "Name is required.");
                }

                Form.setFlag(nameFlowPane, flag);

                if (flag == null) {
                    errorLabel.setText(null);
                    Prompt.prompt("Pretending to submit...", window);
                } else {
                    errorLabel.setText("Some required information is missing.");
                }
            }
        });

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Forms.class, args);
    }
}
