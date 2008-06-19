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
package pivot.wtkx.test;

import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.Button;
import pivot.wtk.Display;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class CursorTest implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content = componentLoader.load("pivot/wtkx/test/cursor.wtkx");

        Button button = (Button)componentLoader.getComponent("button");
        button.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                Alert.alert(Alert.Type.INFO, "This is a modal alert dialog.", window);
            }
        });

        window = new Window();
        window.setTitle("Cursor Test");
        window.setContent(content);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE,
            Boolean.TRUE);

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
