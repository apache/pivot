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

import java.util.Iterator;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.MenuBarItemDataRenderer;

/**
 * Component representing a menu bar.
 */
@DefaultProperty("items")
public class MenuBar extends Container {
    /**
     * Component representing a menu bar item.
     */
    @DefaultProperty("menu")
    public static class Item extends Button {
        private static class ItemListenerList extends WTKListenerList<ItemListener>
            implements ItemListener {
            @Override
            public void menuChanged(Item item, Menu previousMenu) {
                for (ItemListener listener : this) {
                    listener.menuChanged(item, previousMenu);
                }
            }

            @Override
            public void activeChanged(Item item) {
                for (ItemListener listener : this) {
                    listener.activeChanged(item);
                }
            }
        }

        private Menu menu = null;
        private boolean active = false;

        private ItemListenerList itemListeners = new ItemListenerList();

        private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new MenuBarItemDataRenderer();

        public Item() {
            this(null);
        }

        public Item(Object buttonData) {
            super(buttonData);

            setDataRenderer(DEFAULT_DATA_RENDERER);
            installSkin(Item.class);
        }

        @Override
        protected void setParent(Container parent) {
            if (parent != null
                && !(parent instanceof MenuBar)) {
                throw new IllegalArgumentException("Parent must be an instance of "
                    + MenuBar.class.getName());
            }

            setActive(false);

            super.setParent(parent);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);

            if (!enabled) {
                setActive(false);
            }
        }

        public Menu getMenu() {
            return menu;
        }

        public void setMenu(Menu menu) {
            if (menu != null
                && menu.getItem() != null) {
                throw new IllegalArgumentException("menu already belongs to an item.");
            }

            Menu previousMenu = this.menu;

            if (previousMenu != menu) {
                this.menu = menu;
                itemListeners.menuChanged(this, previousMenu);
            }
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            if (active
                && (getParent() == null
                || !isEnabled())) {
                throw new IllegalStateException();
            }

            if (this.active != active) {
                this.active = active;

                // Update the active item
                MenuBar menuBar = (MenuBar)getParent();
                Item activeItem = menuBar.getActiveItem();

                if (active) {
                    // Set this as the new active item (do this before
                    // de-selecting any currently active item so the
                    // menu bar's change event isn't fired twice)
                    menuBar.setActiveItem(this);

                    // Deactivate any previously active item
                    if (activeItem != null) {
                        activeItem.setActive(false);
                    }
                }
                else {
                    // If this item is currently active, clear the
                    // selection
                    if (activeItem == this) {
                        menuBar.setActiveItem(null);
                    }
                }

                itemListeners.activeChanged(this);
            }
        }

        @Override
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException("Menu bar items cannot be toggle buttons.");
        }

        @Override
        public boolean isEnabled() {
            return (super.isEnabled()
                && menu != null);
        }

        public ListenerList<ItemListener> getItemListeners() {
            return itemListeners;
        }
    }

    /**
     * Item listener interface.
     */
    public interface ItemListener {
        /**
         * Called when an item's menu has changed.
         *
         * @param item
         * @param previousMenu
         */
        public void menuChanged(Item item, Menu previousMenu);

        /**
         * Called when an item's active state has changed.
         *
         * @param item
         */
        public void activeChanged(Item item);
    }

    /**
     * Item sequence implementation.
     */
    public final class ItemSequence implements Sequence<Item>, Iterable<Item> {
        @Override
        public int add(Item item) {
            int index = getLength();
            insert(item, index);

            return index;
        }

        @Override
        public void insert(Item item, int index) {
            MenuBar.this.add(item);
            items.insert(item, index);

            menuBarListeners.itemInserted(MenuBar.this, index);
        }

        @Override
        public Item update(int index, Item item) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Item item) {
            int index = items.indexOf(item);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Item> remove(int index, int count) {
            Sequence<Item> removed = items.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Item item = removed.get(i);
                item.setActive(false);

                MenuBar.this.remove(item);
            }

            menuBarListeners.itemsRemoved(MenuBar.this, index, removed);

            return removed;
        }

        @Override
        public Item get(int index) {
            return items.get(index);
        }

        @Override
        public int indexOf(Item item) {
            return items.indexOf(item);
        }

        @Override
        public int getLength() {
            return items.getLength();
        }

        @Override
        public Iterator<Item> iterator() {
            return new ImmutableIterator<Item>(items.iterator());
        }
    }

    private static class MenuBarListenerList extends WTKListenerList<MenuBarListener>
        implements MenuBarListener {
        @Override
        public void itemInserted(MenuBar menuBar, int index) {
            for (MenuBarListener listener : this) {
                listener.itemInserted(menuBar, index);
            }
        }

        @Override
        public void itemsRemoved(MenuBar menuBar, int index, Sequence<MenuBar.Item> removed) {
            for (MenuBarListener listener : this) {
                listener.itemsRemoved(menuBar, index, removed);
            }
        }

        @Override
        public void activeItemChanged(MenuBar menuBar, MenuBar.Item previousActiveItem) {
            for (MenuBarListener listener : this) {
                listener.activeItemChanged(menuBar, previousActiveItem);
            }
        }
    }

    private ArrayList<Item> items = new ArrayList<Item>();
    private ItemSequence itemSequence = new ItemSequence();

    private Item activeItem = null;

    private MenuBarListenerList menuBarListeners = new MenuBarListenerList();

    public MenuBar() {
        installSkin(MenuBar.class);
    }

    public ItemSequence getItems() {
        return itemSequence;
    }

    public Item getActiveItem() {
        return activeItem;
    }

    private void setActiveItem(Item activeItem) {
        Item previousActiveItem = this.activeItem;

        if (previousActiveItem != activeItem) {
            this.activeItem = activeItem;
            menuBarListeners.activeItemChanged(this, previousActiveItem);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            if (items.indexOf((Item)component) >= 0) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public void activateNextItem() {
        int n = items.getLength();

        int index;
        if (activeItem == null) {
            index = 0;
        } else {
            index = items.indexOf(activeItem) + 1;
        }

        while (index < n) {
            Item item = items.get(index++);

            if (item.isEnabled()) {
                item.setActive(true);
                break;
            }
        }
    }

    public void activatePreviousItem() {
        int n = items.getLength();

        int index;
        if (activeItem == null) {
            index = n - 1;
        } else {
            index = items.indexOf(activeItem) - 1;
        }

        while (index >= 0) {
            Item item = items.get(index--);

            if (item.isEnabled()) {
                item.setActive(true);
                break;
            }
        }
    }

    public ListenerList<MenuBarListener> getMenuBarListeners() {
        return menuBarListeners;
    }
}
