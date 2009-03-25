/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.demos.dom;

import pivot.collections.Dictionary;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.BrowserApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.PushButton;
import pivot.wtk.Window;

public class DOMTest implements Application {
    private Window window = null;
    private PushButton helloButton = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        FlowPane flowPane = new FlowPane();
        flowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);

        helloButton = new PushButton("Say Hello");
        flowPane.add(helloButton);

        helloButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                BrowserApplicationContext.eval("sayHello(\"Hello from Java!\")", DOMTest.this);
            }
        });

        window = new Window(flowPane);
        window.setMaximized(true);
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

    public void sayHello(String helloText) {
        Alert.alert(helloText, window);
    }
}
