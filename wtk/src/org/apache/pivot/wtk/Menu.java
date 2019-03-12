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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.MenuItemDataRenderer;

/**
 * Component that presents a cascading menu.
 */
@DefaultProperty("sections")
public class Menu extends Container {
    /**
     * Component representing a menu item.
     */
    @DefaultProperty("menu")
    public static class Item extends Button {
        private Section section = null;

        private Menu menu = null;
        private boolean active = false;

        private ItemListener.Listeners itemListeners = new ItemListener.Listeners();

        private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new MenuItemDataRenderer();

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
            if (parent != null && !(parent instanceof Menu)) {
                throw new IllegalArgumentException("Parent must be an instance of "
                    + Menu.class.getName());
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

        public Section getSection() {
            return section;
        }

        public Menu getMenu() {
            return menu;
        }

        public void setMenu(Menu menu) {
            if (menu != null && menu.getItem() != null) {
                throw new IllegalArgumentException("Menu already belongs to an item.");
            }

            Menu previousMenu = this.menu;

            if (previousMenu != menu) {
                if (previousMenu != null) {
                    previousMenu.item = null;
                }

                if (menu != null) {
                    menu.item = this;
                }

                this.menu = menu;

                itemListeners.menuChanged(this, previousMenu);
            }
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            if (active && (getParent() == null || !isEnabled())) {
                throw new IllegalStateException("Cannot set menu active when it has no parent, or it is disabled.");
            }

            if (this.active != active) {
                this.active = active;

                // Update the active item
                Menu menuLocal = (Menu) getParent();
                Item activeItem = menuLocal.getActiveItem();

                if (active) {
                    // Set this as the new active item (do this before
                    // de-selecting any currently active item so the
                    // menu bar's change event isn't fired twice)
                    menuLocal.setActiveItem(this);

                    // Deactivate any previously active item
                    if (activeItem != null) {
                        activeItem.setActive(false);
                    }
                } else {
                    // If this item is currently active, clear the selection
                    if (activeItem == this) {
                        menuLocal.setActiveItem(null);
                    }
                }

                itemListeners.activeChanged(this);
            }
        }

        @Override
        @UnsupportedOperation
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException("Menu items can't be tri-state.");
        }

        @Override
        public void press() {
            if (isToggleButton()) {
                setSelected(getButtonGroup() == null ? !isSelected() : true);
            }

            super.press();

            if (menu == null) {
                Item item = this;

                while (item != null) {
                    Menu menuLocal = (Menu) item.getParent();

                    if (menuLocal == null) {
                        item = null;
                    } else {
                        menuLocal.menuItemSelectionListeners.itemSelected(this);
                        item = menuLocal.item;
                    }
                }
            }
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
         * Item listeners.
         */
        public static class Listeners extends ListenerList<ItemListener> implements ItemListener {
            @Override
            public void menuChanged(Item item, Menu previousMenu) {
                forEach(listener -> listener.menuChanged(item, previousMenu));
            }

            @Override
            public void activeChanged(Item item) {
                forEach(listener -> listener.activeChanged(item));
            }
        }

        /**
         * Called when an item's menu has changed.
         *
         * @param item The item that has been moved.
         * @param previousMenu The menu where the item used to live.
         */
        public void menuChanged(Item item, Menu previousMenu);

        /**
         * Called when an item's active state has changed.
         *
         * @param item The item that is changing.
         */
        public void activeChanged(Item item);
    }

    /**
     * Class representing a menu section. A section is a grouping of menu items
     * within a menu.
     */
    public static class Section implements Sequence<Item>, Iterable<Item> {
        private Menu menu = null;

        private String name = null;
        private ArrayList<Item> items = new ArrayList<>();

        private SectionListener.Listeners sectionListeners = new SectionListener.Listeners();

        public Menu getMenu() {
            return menu;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String previousName = this.name;

            if (name != previousName) {
                this.name = name;
                sectionListeners.nameChanged(this, previousName);
            }
        }

        @Override
        public int add(Item item) {
            int index = getLength();
            insert(item, index);

            return index;
        }

        @Override
        public void insert(Item item, int index) {
            if (item.getSection() != null) {
                throw new IllegalArgumentException("Menu.Item already has a section.");
            }

            items.insert(item, index);
            item.section = this;

            if (menu != null) {
                menu.add(item);
                sectionListeners.itemInserted(this, index);
            }
        }

        @Override
        @UnsupportedOperation
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

                item.section = null;

                if (menu != null) {
                    menu.remove(item);
                }
            }

            sectionListeners.itemsRemoved(this, index, removed);

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
            return new ImmutableIterator<>(items.iterator());
        }

        public ListenerList<SectionListener> getSectionListeners() {
            return sectionListeners;
        }
    }

    /**
     * Section listener interface.
     */
    public interface SectionListener {
        /**
         * Section listeners.
         */
        public static class Listeners extends ListenerList<SectionListener> implements SectionListener {
            @Override
            public void itemInserted(Menu.Section section, int index) {
                forEach(listener -> listener.itemInserted(section, index));
            }

            @Override
            public void itemsRemoved(Menu.Section section, int index, Sequence<Item> removed) {
                forEach(listener -> listener.itemsRemoved(section, index, removed));
            }

            @Override
            public void nameChanged(Menu.Section section, String previousName) {
                forEach(listener -> listener.nameChanged(section, previousName));
            }
        }

        /**
         * Called when a menu item has been inserted.
         *
         * @param section The section that is changing.
         * @param index The index where the item was inserted.
         */
        public void itemInserted(Section section, int index);

        /**
         * Called when menu items have been removed.
         *
         * @param section The section that has changed.
         * @param index The starting index of the removed items.
         * @param removed The sequence of the items that were removed.
         */
        public void itemsRemoved(Section section, int index, Sequence<Item> removed);

        /**
         * Called when a section's name has changed.
         *
         * @param section The section that changed.
         * @param previousName The previous name for this section.
         */
        public void nameChanged(Section section, String previousName);
    }

    /**
     * Section sequence implementation.
     */
    public final class SectionSequence implements Sequence<Section>, Iterable<Section> {
        private SectionSequence() {
        }

        @Override
        public int add(Section section) {
            int index = getLength();
            insert(section, index);

            return index;
        }

        @Override
        public void insert(Section section, int index) {
            if (section.menu != null) {
                throw new IllegalArgumentException("Menu.Section already has a menu.");
            }

            sections.insert(section, index);
            section.menu = Menu.this;

            for (int i = 0, n = section.getLength(); i < n; i++) {
                Menu.this.add(section.get(i));
            }

            menuListeners.sectionInserted(Menu.this, index);
        }

        @Override
        @UnsupportedOperation
        public Section update(int index, Section section) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int remove(Section section) {
            int index = sections.indexOf(section);
            if (index != -1) {
                remove(index, 1);
            }

            return index;
        }

        @Override
        public Sequence<Section> remove(int index, int count) {
            Sequence<Section> removed = sections.remove(index, count);

            for (int i = 0, n = removed.getLength(); i < n; i++) {
                Section section = removed.get(i);
                section.menu = null;

                for (Item itemLocal : section) {
                    Menu.this.remove(itemLocal);
                }
            }

            menuListeners.sectionsRemoved(Menu.this, index, removed);

            return removed;
        }

        public void clear() {
            remove(0, sections.getLength());
        }

        @Override
        public Section get(int index) {
            return sections.get(index);
        }

        @Override
        public int indexOf(Section itemArgument) {
            return sections.indexOf(itemArgument);
        }

        @Override
        public int getLength() {
            return sections.getLength();
        }

        @Override
        public Iterator<Section> iterator() {
            return new ImmutableIterator<>(sections.iterator());
        }
    }

    private Item item = null;

    private ArrayList<Section> sections = new ArrayList<>();
    private SectionSequence sectionSequence = new SectionSequence();

    private Item activeItem = null;

    private MenuListener.Listeners menuListeners = new MenuListener.Listeners();
    private MenuItemSelectionListener.Listeners menuItemSelectionListeners = new MenuItemSelectionListener.Listeners();

    public Menu() {
        installSkin(Menu.class);
    }

    /**
     * @return The parent item of this menu.
     */
    public Item getItem() {
        return item;
    }

    public SectionSequence getSections() {
        return sectionSequence;
    }

    public Item getActiveItem() {
        return activeItem;
    }

    private void setActiveItem(Item activeItem) {
        Item previousActiveItem = this.activeItem;

        if (previousActiveItem != activeItem) {
            this.activeItem = activeItem;
            menuListeners.activeItemChanged(this, previousActiveItem);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            for (Section section : sections) {
                if (section.indexOf((Menu.Item) component) >= 0) {
                    throw new UnsupportedOperationException();
                }
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<MenuListener> getMenuListeners() {
        return menuListeners;
    }

    public ListenerList<MenuItemSelectionListener> getMenuItemSelectionListeners() {
        return menuItemSelectionListeners;
    }
}
