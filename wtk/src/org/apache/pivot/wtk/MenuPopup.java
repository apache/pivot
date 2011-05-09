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
import org.apache.pivot.util.Vote;

/**
 * Popup class that displays a cascading menu.
 */
@DefaultProperty("menu")
public class MenuPopup extends Window {
    private static class MenuPopupListenerList extends WTKListenerList<MenuPopupListener>
        implements MenuPopupListener {
        @Override
        public void menuChanged(MenuPopup menuPopup, Menu previousMenu) {
            for (MenuPopupListener listener : this) {
                listener.menuChanged(menuPopup, previousMenu);
            }
        }
    }

    private static class MenuPopupStateListenerList extends WTKListenerList<MenuPopupStateListener>
        implements MenuPopupStateListener {
        @Override
        public Vote previewMenuPopupClose(MenuPopup menuPopup, boolean immediate) {
            Vote vote = Vote.APPROVE;

            for (MenuPopupStateListener listener : this) {
                vote = vote.tally(listener.previewMenuPopupClose(menuPopup, immediate));
            }

            return vote;
        }

        @Override
        public void menuPopupCloseVetoed(MenuPopup menuPopup, Vote reason) {
            for (MenuPopupStateListener listener : this) {
                listener.menuPopupCloseVetoed(menuPopup, reason);
            }
        }

        @Override
        public void menuPopupClosed(MenuPopup menuPopup) {
            for (MenuPopupStateListener listener : this) {
                listener.menuPopupClosed(menuPopup);
            }
        }
    }

    private Menu menu;
    private boolean contextMenu = false;

    private boolean closing = false;

    private MenuPopupListenerList menuPopupListeners = new MenuPopupListenerList();
    private MenuPopupStateListenerList menuPopupStateListeners = new MenuPopupStateListenerList();

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
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        open(display, null, location.x, location.y);
    }

    public final void open(Window owner, int x, int y) {
        if (owner == null) {
            throw new IllegalArgumentException();
        }

        open(owner.getDisplay(), owner, x, y);
    }

    public final void open(Window owner, Point location) {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

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
            } else if (vote == Vote.DENY){
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
