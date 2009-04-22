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
package pivot.demos.transition;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class TransitionDemo implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        Component content =
            (Component)wtkxSerializer.readObject(getClass().getResource("transition.wtkx"));

        ButtonPressListener trigger = new ButtonPressListener() {
            public void buttonPressed(Button button) {
                button.setEnabled(false);

                CollapseTransition transition = new CollapseTransition(button, 300, 30);
                transition.start();
            }
        };

        Button button1 = (Button)wtkxSerializer.getObjectByName("button1");
        button1.getButtonPressListeners().add(trigger);

        Button button2 = (Button)wtkxSerializer.getObjectByName("button2");
        button2.getButtonPressListeners().add(trigger);

        Button button3 = (Button)wtkxSerializer.getObjectByName("button3");
        button3.getButtonPressListeners().add(trigger);

        Button button4 = (Button)wtkxSerializer.getObjectByName("button4");
        button4.getButtonPressListeners().add(trigger);

        // Open the window
        window = new Window(content);
        window.setTitle("Transition Demo");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
