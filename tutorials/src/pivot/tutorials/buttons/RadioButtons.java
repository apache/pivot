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
package pivot.tutorials.buttons;

import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class RadioButtons implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader componentLoader = new ComponentLoader();
        Component content =
            componentLoader.load("pivot/tutorials/buttons/radio_buttons.wtkx");

        // Get a reference to the button group
        RadioButton oneButton =
            (RadioButton)componentLoader.getComponent("oneButton");
        final Button.Group numbersGroup = oneButton.getGroup();

        // Add a button press listener
        PushButton selectButton =
            (PushButton)componentLoader.getComponent("selectButton");

        selectButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                String message = "You selected \""
                    + numbersGroup.getSelection().getButtonData()
                    + "\".";
                Alert.alert(Alert.Type.INFO, message, window);
            }
        });

        window = new Window();
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
