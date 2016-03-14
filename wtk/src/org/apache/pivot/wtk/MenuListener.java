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

import org.apache.pivot.collections.Sequence;

/**
 * Menu listener interface.
 */
public interface MenuListener {
    /**
     * Menu listener adapter.
     */
    public static class Adapter implements MenuListener {
        @Override
        public void sectionInserted(Menu menu, int index) {
            // empty block
        }

        @Override
        public void sectionsRemoved(Menu menu, int index, Sequence<Menu.Section> removed) {
            // empty block
        }

        @Override
        public void activeItemChanged(Menu menu, Menu.Item previousActiveItem) {
            // empty block
        }
    }

    /**
     * Called when a menu section has been inserted.
     *
     * @param menu The source of the event.
     * @param index Where the menu section was inserted.
     */
    public void sectionInserted(Menu menu, int index);

    /**
     * Called when menu sections have been removed.
     *
     * @param menu The menu that changed.
     * @param index The starting index of the removal.
     * @param removed The actual menu sections that were removed from the menu.
     */
    public void sectionsRemoved(Menu menu, int index, Sequence<Menu.Section> removed);

    /**
     * Called when a menu's active item has changed.
     *
     * @param menu The menu that changed.
     * @param previousActiveItem What the previously active menu item was.
     */
    public void activeItemChanged(Menu menu, Menu.Item previousActiveItem);
}
