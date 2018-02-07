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
package org.apache.pivot.demos.dom;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.BrowserApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.Window;

public class DOMInteractionDemo implements Application {

    private class CallFromBrowserCallback implements Runnable {
        String text;

        public CallFromBrowserCallback(String text) {
            this.text = text;
        }

        @Override
        public void run() {
            if (window.isBlocked()) {
                System.out.println("I'm already saying \"" + text + "\" !");
            } else {
                Prompt.prompt(text, window);
            }
        }
    }

    private Window window = null;
    private PushButton helloButton = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BoxPane boxPane = new BoxPane();
        boxPane.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);

        helloButton = new PushButton("Say Hello");
        boxPane.add(helloButton);

        helloButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                BrowserApplicationContext.eval("sayHello(\"Hello from Pivot!\")",
                    DOMInteractionDemo.this);
            }
        });

        Border border = new Border(boxPane);
        border.getStyles().put(Style.color, 7);
        border.getStyles().put(Style.padding, 5);
        window = new Window(border);
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * Set text of the message, then display it in a Popup. <p> Called by
     * JavaScript from the Browser.
     *
     * @param helloText the text of the message
     */
    public void sayHello(String helloText) {
        ApplicationContext.queueCallback(new CallFromBrowserCallback(helloText));
    }

}
