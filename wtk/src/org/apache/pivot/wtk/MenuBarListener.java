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
 * Menu bar listener interface.
 */
public interface MenuBarListener {
    /**
     * Menu bar listener adapter.
     */
    public static class Adapter implements MenuBarListener {
        @Override
        public void itemInserted(MenuBar menuBar, int index) {
            // empty block
        }

        @Override
        public void itemsRemoved(MenuBar menuBar, int index, Sequence<MenuBar.Item> removed) {
            // empty block
        }

        @Override
        public void activeItemChanged(MenuBar menuBar, MenuBar.Item previousActiveItem) {
            // empty block
        }
    }

    /**
     * Called when a menu bar item has been inserted.
     *
     * @param menuBar
     * @param index
     */
    public void itemInserted(MenuBar menuBar, int index);

    /**
     * Called when menu bar items have been removed.
     *
     * @param menuBar
     * @param index
     * @param removed
     */
    public void itemsRemoved(MenuBar menuBar, int index, Sequence<MenuBar.Item> removed);

    /**
     * Called when a menu bar's active item has changed.
     *
     * @param menuBar
     * @param previousActiveItem
     */
    public void activeItemChanged(MenuBar menuBar, MenuBar.Item previousActiveItem);
}
