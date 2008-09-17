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
        private Section section = null;
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
            Menu previousMenu = this.menu;

            if (previousMenu != menu) {
                this.menu = menu;

                // TODO Fire event
            }
        }
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

    /**
     * <p>Section sequence implementation.</p>
     *
     * @author gbrown
     */
    public final class SectionSequence implements Sequence<Section> {
        private SectionSequence() {
        }

        public int add(Section section) {
            int index = getLength();
            insert(section, index);

            return index;
        }

        public void insert(Section section, int index) {
            // TODO
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
            // TODO
            return null;
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
    }

    private ArrayList<Section> sections = new ArrayList<Section>();
    private SectionSequence sectionSequence = new SectionSequence();

    public SectionSequence getSections() {
        return sectionSequence;
    }

    public ListenerList<MenuListener> getMenuListeners() {
        // TODO
        return null;
    }

    public ListenerList<MenuItemListener> getMenuItemListeners() {
        // TODO
        return null;
    }
}
