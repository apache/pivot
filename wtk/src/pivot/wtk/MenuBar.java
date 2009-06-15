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
package pivot.wtk;

import java.util.Iterator;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;

import pivot.wtk.content.MenuBarItemDataRenderer;

/**
 * Component representing a horizontal menu bar.
 *
 * @author gbrown
 */
public class MenuBar extends Container {
    /**
     * Component representing a menu bar item.
     *
     * @author gbrown
     */
    public static class Item extends Button {
        private class ItemListenerList extends ListenerList<ItemListener>
            implements ItemListener {
            public void menuChanged(Item item, Menu previousMenu) {
                for (ItemListener listener : this) {
                    listener.menuChanged(item, previousMenu);
                }
            }
        }

        private MenuBar menuBar = null;
        private Menu menu = null;

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
            if (!(parent instanceof MenuBar)) {
                throw new IllegalArgumentException("Parent must be an instance of "
                    + MenuBar.class.getName());
            }

            super.setParent(parent);
        }

        public MenuBar getMenuBar() {
            return menuBar;
        }

        private void setMenuBar(MenuBar menuBar) {
            this.menuBar = menuBar;
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

        @Override
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException("Menu bar items cannot be toggle buttons.");
        }

        public ListenerList<ItemListener> getItemListeners() {
            return itemListeners;
        }
    }

    /**
     * Item listener interface.
     *
     * @author gbrown
     */
    public interface ItemListener {
        public void menuChanged(Item item, Menu previousMenu);
    }

    /**
     * Item sequence implementation.
     *
     * @author gbrown
     */
    public final class ItemSequence implements Sequence<Item>, Iterable<Item> {
        public int add(Item item) {
            int index = getLength();
            insert(item, index);

            return index;
        }

        public void insert(Item item, int index) {
            if (item.getMenuBar() != null) {
                throw new IllegalArgumentException("item already has a menu bar.");
            }

            MenuBar.this.add(item);
            items.insert(item, index);
            item.setMenuBar(MenuBar.this);

            menuBarListeners.itemInserted(MenuBar.this, index);
        }

        public Item update(int index, Item item) {
            throw new UnsupportedOperationException();
        }

        public int remove(Item item) {
            int index = items.indexOf(item);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Item> remove(int index, int count) {
            Sequence<Item> removed = items.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Item item = removed.get(i);
                item.setMenuBar(null);
                MenuBar.this.remove(item);
            }

            menuBarListeners.itemsRemoved(MenuBar.this, index, removed);

            return removed;
        }

        public Item get(int index) {
            return items.get(index);
        }

        public int indexOf(Item item) {
            return items.indexOf(item);
        }

        public int getLength() {
            return items.getLength();
        }

        public Iterator<Item> iterator() {
            return new ImmutableIterator<Item>(items.iterator());
        }
    }

    private class MenuBarListenerList extends ListenerList<MenuBarListener>
        implements MenuBarListener {
        public void itemInserted(MenuBar menuBar, int index) {
            for (MenuBarListener listener : this) {
                listener.itemInserted(menuBar, index);
            }
        }

        public void itemsRemoved(MenuBar menuBar, int index, Sequence<MenuBar.Item> removed) {
            for (MenuBarListener listener : this) {
                listener.itemsRemoved(menuBar, index, removed);
            }
        }

        public void activeChanged(MenuBar menuBar) {
            for (MenuBarListener listener : this) {
                listener.activeChanged(menuBar);
            }
        }
    }

    private ArrayList<Item> items = new ArrayList<Item>();
    private ItemSequence itemSequence = new ItemSequence();

    private boolean active = false;

    private MenuBarListenerList menuBarListeners = new MenuBarListenerList();

    public MenuBar() {
        installSkin(MenuBar.class);
    }

    public ItemSequence getItems() {
        return itemSequence;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        if (this.active != active) {
            this.active = active;

            if (!active) {
                getWindow().setActiveDescendant(null);
            }

            menuBarListeners.activeChanged(this);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Item item = (Item)get(i);

            if (item.getMenuBar() != null) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<MenuBarListener> getMenuBarListeners() {
        return menuBarListeners;
    }
}
