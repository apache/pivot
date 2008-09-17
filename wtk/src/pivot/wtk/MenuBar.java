/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.ArrayList;
import pivot.collections.Sequence;
import pivot.util.ImmutableIterator;
import pivot.util.ListenerList;

/**
 * <p>Component representing a horizontal menu bar.</p>
 *
 * @author gbrown
 */
public class MenuBar extends Container {
    /**
     * <p>Component representing a menu bar item.</p>
     *
     * @author gbrown
     */
    public static class Item extends Button {
        private MenuBar menuBar = null;
        private Menu menu = null;

        public Item() {
            this(null);
        }

        public Item(Object buttonData) {
            super(buttonData);
        }

        @Override
        protected void setParent(Container parent) {
            if (!(parent instanceof Menu)) {
                throw new IllegalArgumentException("Parent must be an instance of "
                    + Menu.class.getName());
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
            Menu previousMenu = this.menu;

            if (previousMenu != menu) {
                this.menu = menu;

                // TODO Fire event
            }
        }
    }

    /**
     * <p>Item sequence implementation.</p>
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
            // TODO
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
            // TODO
            return null;
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

    private ArrayList<Item> items = new ArrayList<Item>();
    private ItemSequence itemSequence = new ItemSequence();

    public ItemSequence getItems() {
        return itemSequence;
    }

    public ListenerList<MenuBarListener> getMenuBarListeners() {
        // TODO
        return null;
    }
}
