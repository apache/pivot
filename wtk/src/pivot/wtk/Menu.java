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
import pivot.wtk.content.MenuItemDataRenderer;

/**
 * <p>Component that presents a cascading menu.</p>
 *
 * @author gbrown
 */
public class Menu extends Container {
    /**
     * <p>Component representing a menu item.</p>
     *
     * @author gbrown
     */
    public static class Item extends Button {
        private static class ItemListenerList extends ListenerList<ItemListener>
            implements ItemListener {
            public void menuChanged(Item item, Menu previousMenu) {
                for (ItemListener listener : this) {
                    listener.menuChanged(item, previousMenu);
                }
            }
        }

        private Section section = null;
        private Menu menu = null;

        private ItemListenerList itemListeners = new ItemListenerList();

        public Item() {
            this(null);
        }

        public Item(Object buttonData) {
            super(buttonData);

            setDataRenderer(new MenuItemDataRenderer());
            installSkin(Item.class);
        }

        @Override
        protected void setParent(Container parent) {
            if (!(parent instanceof Menu)) {
                throw new IllegalArgumentException("Parent must be an instance of "
                    + Menu.class.getName());
            }

            super.setParent(parent);
        }

        public Section getSection() {
            return section;
        }

        private void setSection(Section section) {
            this.section = section;
        }

        public Menu getMenu() {
            return menu;
        }

        public void setMenu(Menu menu) {
            if (menu.getItem() != null) {
                throw new IllegalArgumentException("menu already belongs to an item.");
            }

            Menu previousMenu = this.menu;

            if (previousMenu != menu) {
                if (previousMenu != null) {
                    previousMenu.setItem(null);
                }

                if (menu != null) {
                    menu.setItem(this);
                }

                this.menu = menu;

                itemListeners.menuChanged(this, previousMenu);
            }
        }

        @Override
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException("Menu items can't be tri-state.");
        }

        @Override
        public void press() {
            super.press();

            if (isToggleButton()) {
                setSelected(getGroup() == null ? !isSelected() : true);
            }

            Item item = this;

            while (item != null
                && item.section != null
                && item.section.menu != null) {
                item.section.menu.menuItemListeners.itemPressed(this);
                item = item.section.menu.item;
            }
        }

        public ListenerList<ItemListener> getItemListeners() {
            return itemListeners;
        }
    }

    /**
     * <p>Item listener interface.</p>
     *
     * @author gbrown
     */
    public interface ItemListener {
        public void menuChanged(Item item, Menu previousMenu);
    }

    /**
     * <p>Class representing a menu section. A section is a grouping of menu
     * items within a menu.</p>
     *
     * @author gbrown
     */
    public static class Section implements Sequence<Item>, Iterable<Item> {
        private Menu menu = null;
        private ArrayList<Item> items = new ArrayList<Item>();

        public Menu getMenu() {
            return menu;
        }

        private void setMenu(Menu menu) {
            this.menu = menu;
        }

        public int add(Item item) {
            int index = getLength();
            insert(item, index);

            return index;
        }

        public void insert(Item item, int index) {
            if (item.getSection() != null) {
                throw new IllegalArgumentException("item already has a section.");
            }

            items.insert(item, index);
            item.setSection(this);

            if (menu != null) {
                menu.add(item);
                menu.menuListeners.itemInserted(this, index);
            }
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

                item.setSection(null);

                if (menu != null) {
                    menu.remove(item);
                }
            }

            if (menu != null) {
                menu.menuListeners.itemsRemoved(this, index, count);
            }

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

    /**
     * <p>Section sequence implementation.</p>
     *
     * @author gbrown
     */
    public final class SectionSequence implements Sequence<Section>, Iterable<Section> {
        private SectionSequence() {
        }

        public int add(Section section) {
            int index = getLength();
            insert(section, index);

            return index;
        }

        public void insert(Section section, int index) {
            if (section.getMenu() != null) {
                throw new IllegalArgumentException("section already has a menu.");
            }

            sections.insert(section, index);
            section.setMenu(Menu.this);

            for (int i = 0, n = section.getLength(); i < n; i++) {
                Menu.this.add(section.get(i));
            }

            menuListeners.sectionInserted(Menu.this, index);
        }

        public Section update(int index, Section section) {
            throw new UnsupportedOperationException();
        }

        public int remove(Section section) {
            int index = sections.indexOf(section);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        public Sequence<Section> remove(int index, int count) {
            Sequence<Section> removed = sections.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Section section = removed.get(i);

                section.setMenu(null);

                for (Item item : section) {
                    Menu.this.remove(item);
                }
            }

            menuListeners.sectionsRemoved(Menu.this, index, count);

            return removed;
        }

        public Section get(int index) {
            return sections.get(index);
        }

        public int indexOf(Section item) {
            return sections.indexOf(item);
        }

        public int getLength() {
            return sections.getLength();
        }

        public Iterator<Section> iterator() {
            return new ImmutableIterator<Section>(sections.iterator());
        }
    }

    private static class MenuListenerList extends ListenerList<MenuListener>
        implements MenuListener {
        public void sectionInserted(Menu menu, int index) {
            for (MenuListener listener : this) {
                listener.sectionInserted(menu, index);
            }
        }

        public void sectionsRemoved(Menu menu, int index, int count) {
            for (MenuListener listener : this) {
                listener.sectionsRemoved(menu, index, count);
            }
        }

        public void itemInserted(Menu.Section section, int index) {
            for (MenuListener listener : this) {
                listener.itemInserted(section, index);
            }
        }

        public void itemsRemoved(Menu.Section section, int index, int count) {
            for (MenuListener listener : this) {
                listener.itemsRemoved(section, index, count);
            }
        }
    }

    private static class MenuItemListenerList extends ListenerList<MenuItemPressListener>
        implements MenuItemPressListener {
        public void itemPressed(Menu.Item menuItem) {
            for (MenuItemPressListener listener : this) {
                listener.itemPressed(menuItem);
            }
        }
    }

    private Item item = null;
    private ArrayList<Section> sections = new ArrayList<Section>();
    private SectionSequence sectionSequence = new SectionSequence();

    private MenuListenerList menuListeners = new MenuListenerList();
    private MenuItemListenerList menuItemListeners = new MenuItemListenerList();

    public Menu() {
        installSkin(Menu.class);
    }

    public Item getItem() {
        return item;
    }

    private void setItem(Item item) {
        this.item = item;
    }

    public SectionSequence getSections() {
        return sectionSequence;
    }

    public ListenerList<MenuListener> getMenuListeners() {
        return menuListeners;
    }

    public ListenerList<MenuItemPressListener> getMenuItemPressListeners() {
        return menuItemListeners;
    }
}
