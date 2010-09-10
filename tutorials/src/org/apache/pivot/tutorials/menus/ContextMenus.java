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

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class ContextMenus implements Application {
    private Window window = null;
    private MenuHandler menuHandler = new MenuHandler.Adapter() {
        @Override
        public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
            final Component descendant = window.getDescendantAt(x, y);

            Menu.Section menuSection = new Menu.Section();
            menu.getSections().add(menuSection);

            Menu.Item whatIsThisMenuItem = new Menu.Item("What is this?");
            whatIsThisMenuItem.setAction(new Action() {
                @Override
                public void perform(Component source) {
                    String description = (String)descendant.getUserData().get("description");
                    String message = "This is a " + description + ".";

                    Prompt.prompt(message, window);
                }
            });

            menuSection.add(whatIsThisMenuItem);

            return false;
        }
    };

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "context_menus.wtkx");
        window.setMenuHandler(menuHandler);

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
        DesktopApplicationContext.main(ContextMenus.class, args);
    }
}
