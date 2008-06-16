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

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.util.ListenerList;
import pivot.wtk.content.MenuItemDataRenderer;
import pivot.wtk.skin.terra.MenuSkin;

/**
 * Class representing a menu.
 *
 * @author gbrown
 */
public class Menu extends Component {
    public static class Item {
        private Section section = null;

        private Object data = null;
        private boolean checked = false;
        private boolean enabled = true;
        private Keyboard.KeyStroke actionTrigger = null;

        /**
         * Item constructor.
         *
         * @param data
         * The data associated with the item.
         */
        public Item(Object data) {
            setData(data);
        }

        /**
         * Returns the section to which this item belongs.
         *
         * @return
         * The item's section, or <tt>null</tt> if the item is not currently
         * assigned to a section.
         */
        public Section getSection() {
            return section;
        }

        /**
         * Sets the item's section. Called by a section when an item is added
         * to or removed from it.
         *
         * @param section
         */
        private void setSection(Section section) {
            assert (section == null) : "section is not null.";

            this.section = section;
        }

        /**
         * Returns the data associated with the item.
         *
         * @return
         * The item's data.
         */
        public Object getData() {
            return data;
        }

        /**
         * Sets the item's data.
         *
         * @param data
         * The item data.
         */
        public void setData(Object data) {
            if (data == null) {
                throw new IllegalArgumentException("data is null.");
            }

            Object previousData = this.data;
            if (previousData != data) {
                this.data = data;
                update();
            }
        }

        /**
         * Returns the checked state of the item.
         *
         * @return
         * <tt>true</tt> if the item is checked; <tt>false</tt>, otherwise.
         */
        public boolean isChecked() {
            return checked;
        }

        /**
         * Sets the checked state of the item.
         *
         * @param checked
         * <tt>true</tt> if the item is checked; <tt>false</tt>, otherwise.
         */
        public void setChecked(boolean checked) {
            if (this.checked != checked) {
                this.checked = checked;
                update();
            }
        }

        /**
         * Returns the enabled state of the item.
         *
         * @return
         * <tt>true</tt> if the item is enabled; <tt>false</tt>, otherwise.
         */
        public boolean isEnabled() {
            return enabled;
        }

        /**
         * Sets the enabled state of the item.
         *
         * @param enabled
         * <tt>true</tt> if the item is enabled; <tt>false</tt>, otherwise.
         */
        public void setEnabled(boolean enabled) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                update();
            }
        }

        /**
         * Returns the action key for this menu item.
         *
         * @return
         * The menu item's action key, or <tt>null</tt> if no action key
         * is defined.
         */
        public Keyboard.KeyStroke getActionTrigger() {
            return actionTrigger;
        }

        /**
         * Sets this menu item's action key. If specified, the key is used to
         * look up the action in the global action map when the menu item is
         * selected and execute the mapped action.
         *
         * @param actionKey
         * The menu item's action key, or <tt>null</tt> if no action key is
         * defined.
         */
        public void setActionTrigger(Keyboard.KeyStroke actionTrigger) {
            Keyboard.KeyStroke previousActionTrigger = this.actionTrigger;

            if (previousActionTrigger != actionTrigger) {
                this.actionTrigger = actionTrigger;
                update();
            }
        }

        protected void update() {
            if (section != null) {
                List<Item> items = section.getItems();
                items.update(items.indexOf(this), this);
            }
        }
    }

    /**
     * An item group is a collection of menu items grouped into sections.
     *
     * @author gbrown
     */
    public static class ItemGroup extends Item {
        private ArrayList<Section> sections = new ArrayList<Section>();

        public ItemGroup(Object data) {
            super(data);
        }

        public List<Section> getSections() {
            // TODO Return a custom subclass of ArrayList<Section> that will
            // call setItemGroup() on any added/removed sections
            return sections;
        }
    }

    /**
     * A section is a grouping of menu items within an item group.
     *
     * @author gbrown
     */
    public static class Section {
        private ItemGroup itemGroup = null;

        private ArrayList<Item> items = new ArrayList<Item>();
        private ItemDataRenderer itemDataRenderer = new MenuItemDataRenderer();

        public ItemGroup getItemGroup() {
            return itemGroup;
        }

        private void setItemGroup(ItemGroup itemGroup) {
            assert (itemGroup == null) : "itemGroup is not null.";

            this.itemGroup = itemGroup;
        }

        public List<Item> getItems() {
            // TODO Return a custom subclass of ArrayList<Section> that will
            // call setSection() on any added/removed items
            return items;
        }

        /**
         * Returns the renderer to be used for the items in this section.
         *
         * @return
         * The renderer for this section.
         */
        public ItemDataRenderer getItemDataRenderer() {
            return itemDataRenderer;
        }

        /**
         * Sets the renderer to be used for the items in this section.
         *
         * @param itemDataRenderer
         * The renderer for this section.
         */
        public void setItemDataRenderer(ItemDataRenderer itemDataRenderer) {
            if (itemDataRenderer == null) {
                throw new IllegalArgumentException("itemDataRenderer is null.");
            }

            ItemDataRenderer previousItemRenderer = this.itemDataRenderer;

            if (previousItemRenderer != itemDataRenderer) {
                this.itemDataRenderer = itemDataRenderer;
                update();
            }
        }

        protected void update() {
            if (itemGroup != null) {
                List<Section> sections = itemGroup.getSections();
                sections.update(sections.indexOf(this), this);
            }
        }
    }

    /**
     * Menu item renderer interface.
     *
     * @author gbrown
     */
    public interface ItemDataRenderer extends Renderer {
        /**
         * Prepares the renderer for layout or paint.
         *
         * @param item
         * The item to render.
         *
         * @param menu
         * The host component.
         *
         * @param checked
         * If <tt>true</tt>, the renderer should present a checked state for
         * the item.
         *
         * @param highlighted
         * If <tt>true</tt>, the renderer should present a highlighted state
         * for the item.
         *
         * @param disabled
         * If <tt>true</tt>, the renderer should present a disabled state for
         * the item.
         */
        public void render(Object item, Menu menu, boolean checked,
            boolean highlighted, boolean disabled);
    }

    private ItemGroup menuData = null;

    public Menu() {
        this(null);
    }

    public Menu(ItemGroup menuData) {
        setMenuData(menuData);

        if (getClass() == Menu.class) {
            setSkinClass(MenuSkin.class);
        }
    }

    /**
     * Returns the menu data.
     */
    public ItemGroup getMenuData() {
        return this.menuData;
    }

    /**
     * Sets the menu data.
     *
     * @param menuData
     * The data to be presented by the menu.
     */
    public void setMenuData(ItemGroup menuData) {
        this.menuData = menuData;

        // TODO Fire change event
    }

    /**
     * Selects a menu item, firing a menu selection event.
     */
    public void selectItem(Item item) {
        // TODO Fire event
    }

    public ListenerList<MenuListener> getMenuListeners() {
        // TODO
        return null;
    }

    public ListenerList<MenuSelectionListener> getMenuSelectionListeners() {
        // TODO
        return null;
    }
}
