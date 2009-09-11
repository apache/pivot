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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ImmutableIterator;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.content.MenuItemDataRenderer;

/**
 * Component that presents a cascading menu.
 */
public class Menu extends Container {
    /**
     * Component representing a menu item.
     */
    public static class Item extends Button {
        private static class ItemListenerList extends ListenerList<ItemListener>
            implements ItemListener {
            @Override
            public void nameChanged(Item item, String previousName) {
                for (ItemListener listener : this) {
                    listener.nameChanged(item, previousName);
                }
            }

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

        private Section section = null;

        private String name = null;
        private Menu menu = null;
        private boolean active = false;

        private ItemListenerList itemListeners = new ItemListenerList();

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
            if (parent != null
                && !(parent instanceof Menu)) {
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

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String previousName = this.name;

            if (name != previousName) {
                if (section != null
                    && section.menu != null) {
                    if (section.menu.itemMap.containsKey(name)) {
                        throw new IllegalArgumentException("A menu item named \"" + name
                            + "\" already exists.");
                    }

                    if (previousName != null) {
                        section.menu.itemMap.remove(previousName);
                    }

                    section.menu.itemMap.put(name, this);
                }

                this.name = name;

                itemListeners.nameChanged(this, previousName);
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
            if (active
                && (getParent() == null
                    || !isEnabled())) {
                throw new IllegalStateException();
            }

            if (this.active != active) {
                this.active = active;

                // Update the active item
                Menu menu = (Menu)getParent();
                Item activeItem = menu.getActiveItem();

                if (active) {
                    // Set this as the new active item (do this before
                    // de-selecting any currently active item so the
                    // menu bar's change event isn't fired twice)
                    menu.setActiveItem(this);

                    // Deactivate any previously active item
                    if (activeItem != null) {
                        activeItem.setActive(false);
                    }
                }
                else {
                    // If this item is currently active, clear the
                    // selection
                    if (activeItem == this) {
                        menu.setActiveItem(null);
                    }
                }

                itemListeners.activeChanged(this);
            }
        }

        @Override
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException("Menu items can't be tri-state.");
        }

        @Override
        public void press() {
            if (isToggleButton()) {
                setSelected(getGroup() == null ? !isSelected() : true);
            }

            super.press();

            if (menu == null) {
                Item item = this;

                while (item != null) {
                    Menu menu = (Menu)item.getParent();

                    if (menu == null) {
                        item = null;
                    } else {
                        menu.menuItemSelectionListeners.itemSelected(this);
                        item = menu.item;
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
         * Called when an item's name has changed.
         *
         * @param item
         * @param previousName
         */
        public void nameChanged(Item item, String previousName);

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
     * Class representing a menu section. A section is a grouping of menu
     * items within a menu.
     */
    public static class Section implements Sequence<Item>, Iterable<Item> {
        private static class SectionListenerList extends ListenerList<SectionListener>
            implements SectionListener {
            @Override
            public void itemInserted(Menu.Section section, int index) {
                for (SectionListener listener : this) {
                    listener.itemInserted(section, index);
                }
            }

            @Override
            public void itemsRemoved(Menu.Section section, int index, Sequence<Item> removed) {
                for (SectionListener listener : this) {
                    listener.itemsRemoved(section, index, removed);
                }
            }

            @Override
            public void nameChanged(Menu.Section section, String previousName) {
                for (SectionListener listener : this) {
                    listener.nameChanged(section, previousName);
                }
            }
        }

        private Menu menu = null;

        private String name = null;
        private ArrayList<Item> items = new ArrayList<Item>();

        private SectionListenerList sectionListeners = new SectionListenerList();

        public Menu getMenu() {
            return menu;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            String previousName = this.name;

            if (name != previousName) {
                if (menu != null) {
                    if (menu.sectionMap.containsKey(name)) {
                        throw new IllegalArgumentException("A menu section named \"" + name
                            + "\" already exists.");
                    }

                    if (previousName != null) {
                        menu.sectionMap.remove(previousName);
                    }

                    menu.sectionMap.put(name, this);
                }

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
                throw new IllegalArgumentException("item already has a section.");
            }

            items.insert(item, index);
            item.section = this;

            if (menu != null) {
                menu.add(item);
                sectionListeners.itemInserted(this, index);
            }
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
            return new ImmutableIterator<Item>(items.iterator());
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
         * Called when a menu item has been inserted.
         *
         * @param section
         * @param index
         */
        public void itemInserted(Section section, int index);

        /**
         * Called when menu items have been removed.
         *
         * @param section
         * @param index
         * @param removed
         */
        public void itemsRemoved(Section section, int index, Sequence<Item> removed);

        /**
         * Called when a section's name has changed.
         *
         * @param section
         * @param previousName
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
                throw new IllegalArgumentException("section already has a menu.");
            }

            sections.insert(section, index);

            if (section.name != null) {
                if (sectionMap.containsKey(section.name)) {
                    throw new IllegalArgumentException("A menu section named \"" + section.name
                        + "\" already exists.");
                }

                sectionMap.put(section.name, section);
            }

            for (Item item : section) {
                if (item.name != null) {
                    if (itemMap.containsKey(item.name)) {
                        throw new IllegalArgumentException("A menu item named \"" + item.name
                            + "\" already exists.");
                    }

                    itemMap.put(item.name, item);
                }
            }

            section.menu = Menu.this;

            for (int i = 0, n = section.getLength(); i < n; i++) {
                Menu.this.add(section.get(i));
            }

            menuListeners.sectionInserted(Menu.this, index);
        }

        @Override
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

                if (section.name != null) {
                    sectionMap.remove(section.name);
                }

                for (Item item : section) {
                    if (item.name != null) {
                        itemMap.remove(item.name);
                    }
                }

                section.menu = null;

                for (Item item : section) {
                    Menu.this.remove(item);
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
        public int indexOf(Section item) {
            return sections.indexOf(item);
        }

        @Override
        public int getLength() {
            return sections.getLength();
        }

        @Override
        public Iterator<Section> iterator() {
            return new ImmutableIterator<Section>(sections.iterator());
        }
    }

    private static class MenuListenerList extends ListenerList<MenuListener>
        implements MenuListener {
        @Override
        public void sectionInserted(Menu menu, int index) {
            for (MenuListener listener : this) {
                listener.sectionInserted(menu, index);
            }
        }

        @Override
        public void sectionsRemoved(Menu menu, int index, Sequence<Section> removed) {
            for (MenuListener listener : this) {
                listener.sectionsRemoved(menu, index, removed);
            }
        }

        @Override
        public void activeItemChanged(Menu menu, Menu.Item previousActiveItem) {
            for (MenuListener listener : this) {
                listener.activeItemChanged(menu, previousActiveItem);
            }
        }
    }

    private static class MenuItemSelectionListenerList extends ListenerList<MenuItemSelectionListener>
        implements MenuItemSelectionListener {
        @Override
        public void itemSelected(Menu.Item menuItem) {
            for (MenuItemSelectionListener listener : this) {
                listener.itemSelected(menuItem);
            }
        }
    }

    private Item item = null;

    private ArrayList<Section> sections = new ArrayList<Section>();
    private SectionSequence sectionSequence = new SectionSequence();

    private HashMap<String, Section> sectionMap = new HashMap<String, Section>();
    private HashMap<String, Item> itemMap = new HashMap<String, Item>();

    private Item activeItem = null;

    private MenuListenerList menuListeners = new MenuListenerList();
    private MenuItemSelectionListenerList menuItemSelectionListeners = new MenuItemSelectionListenerList();

    public Menu() {
        installSkin(Menu.class);
    }

    /**
     * Retrieves the parent item of this menu.
     */
    public Item getItem() {
        return item;
    }

    public SectionSequence getSections() {
        return sectionSequence;
    }

    /**
     * Retrieves a named section.
     *
     * @param name
     *
     * @return
     * The section with the given name, or <tt>null</tt> if the name does not
     * exist.
     */
    public Section getSection(String name) {
        return sectionMap.get(name);
    }

    /**
     * Retrieves a named item.
     *
     * @param name
     *
     * @return
     * The item with the given name, or <tt>null</tt> if the name does not
     * exist.
     */
    public Item getItem(String name) {
        return itemMap.get(name);
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

    public void activateNextItem() {
        int sectionCount = sections.getLength();

        int sectionIndex;
        int itemIndex;

        if (activeItem == null) {
            sectionIndex = 0;
            itemIndex = 0;
        } else {
            sectionIndex = sections.indexOf(activeItem.section);
            itemIndex = activeItem.section.indexOf(activeItem) + 1;
        }

        while (sectionIndex < sectionCount) {
            Section section = sections.get(sectionIndex);
            int sectionLength = section.getLength();

            while (itemIndex < sectionLength) {
                Item item = section.get(itemIndex);

                if (item.isEnabled()) {
                    item.setActive(true);
                    break;
                } else {
                    itemIndex++;
                }
            }

            if (itemIndex < sectionLength) {
                break;
            } else {
                sectionIndex++;
                itemIndex = 0;
            }
        }
    }

    public void activatePreviousItem() {
        int sectionCount = sections.getLength();

        int sectionIndex;
        int itemIndex;

        if (activeItem == null) {
            sectionIndex = sectionCount - 1;
            itemIndex = -1;
        } else {
            sectionIndex = sections.indexOf(activeItem.section);
            itemIndex = activeItem.section.indexOf(activeItem) - 1;

            if (itemIndex == -1) {
                sectionIndex--;
            }
        }

        while (sectionIndex >= 0) {
            Section section = sections.get(sectionIndex);
            if (itemIndex == -1) {
                int sectionLength = section.getLength();
                itemIndex = sectionLength - 1;
            }

            while (itemIndex >= 0) {
                Item item = section.get(itemIndex);

                if (item.isEnabled()) {
                    item.setActive(true);
                    break;
                } else {
                    itemIndex--;
                }
            }

            if (itemIndex >= 0) {
                break;
            } else {
                sectionIndex--;
            }
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);

            for (Section section : sections) {
                if (section.indexOf((Menu.Item)component) >= 0) {
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
