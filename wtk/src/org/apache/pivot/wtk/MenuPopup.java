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
package org.apache.pivot.wtk;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.util.Vote;

/**
 * Popup class that displays a cascading menu.
 */
@DefaultProperty("menu")
public class MenuPopup extends Window {
    private Menu menu;
    private boolean contextMenu = false;

    private boolean closing = false;

    private MenuPopupListener.Listeners menuPopupListeners = new MenuPopupListener.Listeners();
    private MenuPopupStateListener.Listeners menuPopupStateListeners = new MenuPopupStateListener.Listeners();

    public MenuPopup() {
        this(null);
    }

    public MenuPopup(Menu menu) {
        setMenu(menu);
        installSkin(MenuPopup.class);
    }

    public Menu getMenu() {
        return menu;
    }

    public void setMenu(Menu menu) {
        Menu previousMenu = this.menu;

        if (previousMenu != menu) {
            this.menu = menu;
            menuPopupListeners.menuChanged(this, previousMenu);
        }
    }

    public boolean isContextMenu() {
        return contextMenu;
    }

    public final void open(Display display, int x, int y) {
        open(display, null, x, y);
    }

    public final void open(Display display, Point location) {
        Utils.checkNull(location, "location");

        open(display, null, location.x, location.y);
    }

    public final void open(Window owner, int x, int y) {
        Utils.checkNull(owner, "owner");

        open(owner.getDisplay(), owner, x, y);
    }

    public final void open(Window owner, Point location) {
        Utils.checkNull(location, "location");

        open(owner, location.x, location.y);
    }

    public void open(Display display, Window owner, int x, int y) {
        contextMenu = true;
        setLocation(x, y);

        super.open(display, owner);
    }

    @Override
    public boolean isClosing() {
        return closing;
    }

    @Override
    public final void close() {
        close(false);
    }

    public void close(boolean immediate) {
        if (!isClosed()) {
            closing = true;

            Vote vote = menuPopupStateListeners.previewMenuPopupClose(this, immediate);

            if (vote == Vote.APPROVE) {
                super.close();

                closing = super.isClosing();

                if (isClosed()) {
                    menuPopupStateListeners.menuPopupClosed(this);
                }
            } else if (vote == Vote.DENY) {
                closing = false;
                menuPopupStateListeners.menuPopupCloseVetoed(this, vote);
            }
        }

        if (isClosed()) {
            contextMenu = false;
        }
    }

    public ListenerList<MenuPopupListener> getMenuPopupListeners() {
        return menuPopupListeners;
    }

    public ListenerList<MenuPopupStateListener> getMenuPopupStateListeners() {
        return menuPopupStateListeners;
    }
}
