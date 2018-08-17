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
package org.apache.pivot.tests.issues;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * This test will check that the previewWindowOpen method is called before the
 * ListPopup of the MenuButton is opened. This is crucial because one need to
 * populate the Menu before the Window opens, so that correct sizing and layout
 * can be performed.
 */
public class Pivot765 implements Application {
    private boolean menuPopulated = false;

    @Override
    public void startup(final Display display, Map<String, String> properties) throws Exception {
        final MenuButton button = new MenuButton();
        button.setButtonData("Populate menu and open!");
        Window window = new Window(button);

        button.getListPopup().getWindowStateListeners().add(new WindowStateListener() {
            @Override
            public Vote previewWindowOpen(Window windowArgument) {
                Menu menu = new Menu();
                Menu.Section section = new Menu.Section();
                menu.getSections().add(section);
                section.add(new Menu.Item("A dynamically added menu item"));
                button.setMenu(menu);

                menuPopulated = true;
                return Vote.APPROVE;
            }

            @Override
            public void windowOpened(Window windowArgument) {
                if (!menuPopulated) {
                    Alert.alert(
                        "Window was opened before the menu was populated. "
                            + "Either previewWindowOpen threw an exception, "
                            + "or it wasn't called before the Window was opened.",
                        windowArgument);
                }
            }

            @Override
            public void windowClosed(Window windowArgument, Display displayArgument, Window owner) {
                // Remove menu for subsequent open attempt
                button.setMenu(null);
                menuPopulated = false;
            }
        });

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(new String[] {Pivot765.class.getName()});
    }

}
