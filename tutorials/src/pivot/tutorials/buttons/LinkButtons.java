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

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.LinkButton;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class LinkButtons implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();

        Component content =
            (Component)wtkxSerializer.readObject("pivot/tutorials/buttons/link_buttons.wtkx");

        final CardPane cardPane = (CardPane)wtkxSerializer.getObjectByName("cardPane");

        LinkButton nextButton = (LinkButton)wtkxSerializer.getObjectByName("nextButton");
        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(1);
            }
        });

        LinkButton previousButton = (LinkButton)wtkxSerializer.getObjectByName("previousButton");
        previousButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(0);
            }
        });

        window = new Window();
        window.setContent(content);
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
