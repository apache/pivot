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

import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.LinkButton;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class LinkButtons implements Application {
    private Window window = null;

    public void startup() throws Exception {
        ComponentLoader componentLoader = new ComponentLoader();
        Component content =
            componentLoader.load("pivot/tutorials/buttons/link_buttons.wtkx");

        final CardPane cardPane = (CardPane)componentLoader.getComponent("cardPane");

        LinkButton nextButton = (LinkButton)componentLoader.getComponent("nextButton");
        nextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(1);
            }
        });

        LinkButton previousButton = (LinkButton)componentLoader.getComponent("previousButton");
        previousButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                cardPane.setSelectedIndex(0);
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
