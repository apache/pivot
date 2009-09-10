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
package org.apache.pivot.tutorials.menus;

import java.io.IOException;

import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class MenuBars implements Application {
    private Window window = null;
    private TabPane tabPane = null;

    private MenuHandler menuHandler = new MenuHandler.Adapter() {
        @Override
        public void configureMenuBar(Component component, MenuBar menuBar) {
            boolean enabled = (component instanceof TextInput);

            Action.getNamedActions().get("cut").setEnabled(enabled);
            Action.getNamedActions().get("copy").setEnabled(enabled);
            Action.getNamedActions().get("paste").setEnabled(enabled);
        }
    };

    public MenuBars() {
        Action.getNamedActions().put("fileNew", new Action() {
            @Override
            public void perform() {
                WTKXSerializer wtkxSerializer = new WTKXSerializer();
                Component tab;
                try {
                    tab = new Border((Component)wtkxSerializer.readObject(this, "document.wtkx"));

                    TextInput textInput1 = (TextInput)wtkxSerializer.get("textInput1");
                    textInput1.setMenuHandler(menuHandler);

                    TextInput textInput2 = (TextInput)wtkxSerializer.get("textInput2");
                    textInput2.setMenuHandler(menuHandler);

                    PushButton pushButton = (PushButton)wtkxSerializer.get("pushButton");
                    pushButton.setMenuHandler(menuHandler);
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                tabPane.getTabs().add(tab);
                TabPane.setLabel(tab, "Document " + tabPane.getTabs().getLength());
            }
        });

        Action.getNamedActions().put("fileOpen", new Action() {
            @Override
            public void perform() {
                FileBrowserSheet fileBrowserSheet = new FileBrowserSheet(FileBrowserSheet.Mode.OPEN);
                fileBrowserSheet.open(window);
            }
        });

        Action.getNamedActions().put("cut", new Action() {
            @Override
            public void perform() {
                TextInput textInput = (TextInput)window.getFocusDescendant();
                textInput.cut();
            }
        });

        Action.getNamedActions().put("copy", new Action() {
            @Override
            public void perform() {
                TextInput textInput = (TextInput)window.getFocusDescendant();
                textInput.copy();
            }
        });

        Action.getNamedActions().put("paste", new Action() {
            @Override
            public void perform() {
                TextInput textInput = (TextInput)window.getFocusDescendant();
                textInput.paste();
            }
        });
    }

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "menu_bars.wtkx");

        tabPane = (TabPane)wtkxSerializer.get("tabPane");

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(MenuBars.class, args);
    }
}
