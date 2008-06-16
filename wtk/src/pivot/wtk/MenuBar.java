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
import pivot.collections.Sequence;
import pivot.wtk.Menu.ItemGroup;
import pivot.wtk.skin.terra.MenuBarSkin;

/**
 * TODO Make Menu constructor protected?
 *
 * TODO Add ItemGroupRenderer interface and property (including change event)
 *
 * @author gbrown
 */
public class MenuBar extends Component {
    public final class ItemGroupSequence implements Sequence<ItemGroup>,
        Iterable<ItemGroup> {
        public int add(ItemGroup item) {
            // TODO Auto-generated method stub
            return 0;
        }

        public void insert(ItemGroup item, int index) {
            // TODO Auto-generated method stub
        }

        public ItemGroup update(int index, ItemGroup item) {
            throw new UnsupportedOperationException();
        }

        public int remove(ItemGroup item) {
            // TODO Auto-generated method stub
            return 0;
        }

        public Sequence<ItemGroup> remove(int index, int count) {
            // TODO Auto-generated method stub
            return null;
        }

        public ItemGroup get(int index) {
            // TODO Auto-generated method stub
            return null;
        }

        public int indexOf(ItemGroup item) {
            // TODO Auto-generated method stub
            return 0;
        }

        public int getLength() {
            // TODO Auto-generated method stub
            return 0;
        }

        public Iterator<ItemGroup> iterator() {
            // TODO
            return null;
        }

    }

    public interface ItemDataRenderer {
        public void render(Object item, MenuBar menuBar, boolean highlighted);
    }

    public MenuBar() {
        // TODO

        if (getClass() == MenuBar.class) {
            setSkinClass(MenuBarSkin.class);
        }
    }

    public ItemGroupSequence getMenuItemGroups() {
        // TODO
        return null;
    }

    public ItemDataRenderer getItemDataRenderer() {
        // TODO
        return null;
    }

    public void setItemDataRenderer(ItemDataRenderer itemDataRenderer) {
        if (itemDataRenderer == null) {
            throw new IllegalArgumentException("itemDataRenderer is null.");
        }

        // TODO Fire event
    }
}
