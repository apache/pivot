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

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class ButtonGroupTest implements Application {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.getButtonGroupListeners().add(new ButtonGroupListener() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroupArgument, Button previousSelection) {
                System.out.println("selectionChanged(): previousSelection = " + previousSelection
                    + ", selection = " + buttonGroupArgument.getSelection());
            }

            @Override
            public void buttonAdded(ButtonGroup buttonGroupArgument, Button button) {
                System.out.println("buttonAdded(): " + button);
            }

            @Override
            public void buttonRemoved(ButtonGroup buttonGroupArgument, Button button) {
                System.out.println("buttonRemoved(): " + button);
            }
        });

        BoxPane boxPane = new BoxPane();

        PushButton button1 = new PushButton("One");
        button1.setToggleButton(true);
        button1.setButtonGroup(buttonGroup);
        boxPane.add(button1);

        PushButton button2 = new PushButton("Two");
        button2.setToggleButton(true);
        button2.setButtonGroup(buttonGroup);
        boxPane.add(button2);

        PushButton button3 = new PushButton("Three");
        button3.setToggleButton(true);
        button3.setButtonGroup(buttonGroup);
        boxPane.add(button3);

        PushButton button4 = new PushButton("Four");
        button4.setToggleButton(true);
        button4.setButtonGroup(buttonGroup);
        boxPane.add(button4);

        // button1.setSelected(true);
        // buttonGroup.setSelection(button1);
        // buttonGroup.setSelection(null);

        window = new Window(boxPane);
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

    public static void main(String[] args) {
        DesktopApplicationContext.main(ButtonGroupTest.class, args);
    }
}
