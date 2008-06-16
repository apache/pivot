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
package pivot.wtk.skin.terra;

import java.awt.Graphics2D;
import java.util.Comparator;

import pivot.collections.List;
import pivot.collections.ListListener;
import pivot.collections.Sequence;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Menu;
import pivot.wtk.MenuListener;
import pivot.wtk.skin.ComponentSkin;

/**
 * TODO Create a shared AbstractMenuSkin that this and MenuSkin can share?
 *
 * TODO Define a "gutter" style that a renderer can query so it knows where
 * to paint a checkbox or icon.
 *
 * TODO The renderer is painted into the left side of the menu; the skin paints
 * the accelerator key and the group expander.
 *
 * TODO Define an inner SectionVisual class that will paint individual sections;
 * this class will also listen for list events on the section and call
 * invalidateComponent() as needed.
 *
 * @author gbrown
 */
public class MenuSkin extends ComponentSkin implements MenuListener, ListListener<Menu.Section> {
    public void install(Component component) {
        validateComponentType(component, Menu.class);

        super.install(component);

        // TODO Add this as a menu listener
        // TODO Add this as a menu item listener

        // TODO Add this as a list listener on the item group
    }

    public void uninstall() {
        // TODO Remove this as a menu listener
        // TODO Remove this as a menu item listener

        // TODO Remove this as a list listener on the item group
    }

    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void layout() {
        // TODO Auto-generated method stub

    }

    public void paint(Graphics2D graphics) {
        // TODO Auto-generated method stub

    }

    public void menuDataChanged(Menu menu, Menu.ItemGroup previousMenuData) {
        // TODO
    }

    public void itemInserted(List<Menu.Section> list, int index) {
        // TODO
    }

    public void itemsRemoved(List<Menu.Section> list, int index, Sequence<Menu.Section> items) {
        // TODO
    }

    public void itemUpdated(List<Menu.Section> list, int index, Menu.Section previousItem) {
        // TODO
    }

    public void comparatorChanged(List<Menu.Section> list, Comparator<Menu.Section> previousComparator) {
        // TODO
    }
}
