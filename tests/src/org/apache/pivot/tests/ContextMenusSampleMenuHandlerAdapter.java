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

import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuHandler;

public class ContextMenusSampleMenuHandlerAdapter implements MenuHandler {
    private Component descendant = null;

    protected Component getDescendant() {
        return descendant;
    }

    protected void setDescendant(Component descendant) {
        this.descendant = descendant;
    }

    @Override
    public void configureMenuBar(Component component, MenuBar menuBar) {
        System.out.println("Configure menu bar: " + component);
    }

    @Override
    public void cleanupMenuBar(Component component, MenuBar menuBar) {
        System.out.println("Clean up menu bar: " + component);
    }

    @Override
    public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
        Menu.Section menuSection = new Menu.Section();
        menu.getSections().add(menuSection);

        menuSection.add(new Menu.Item("Do Nothing"));
        Menu.Item doNothingMenuItem = new Menu.Item("Do Nothing and disabled");
        doNothingMenuItem.setEnabled(false);
        menuSection.add(doNothingMenuItem);

        Menu.Item whatIsThisMenuItem = new Menu.Item("What is this?");
        whatIsThisMenuItem.setAction(new Action() {
            @Override
            public void perform(Component source) {
                String description = (descendant != null) ? (String) descendant.getUserData().get(
                    "description") : "empty";
                String message = "This is a " + description + " description.";

                System.out.println("perform: " + message);
            }
        });
        menuSection.add(whatIsThisMenuItem);

        Menu.Item nullActionMenuItem = new Menu.Item("Item with null action");
        nullActionMenuItem.setAction((Action) null);
        menuSection.add(nullActionMenuItem);

        Menu.Item disabledActionMenuItem = new Menu.Item("Item with disabled action");
        disabledActionMenuItem.setAction(new Action() {
            @Override
            public void perform(Component source) {
                System.out.println("in perform");
            }
        });
        disabledActionMenuItem.getAction().setEnabled(false);
        menuSection.add(disabledActionMenuItem);

        return false;
    }

}
