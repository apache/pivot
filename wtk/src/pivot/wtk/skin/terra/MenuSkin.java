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

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Menu;
import pivot.wtk.MenuListener;
import pivot.wtk.skin.ButtonSkin;

/**
 * TODO Define left and right gutter styles that the renderer can query.
 *
 * @author gbrown
 */
public class MenuSkin extends ButtonSkin implements MenuListener {
    public void install(Component component) {
        validateComponentType(component, Menu.class);

        super.install(component);

        // TODO Add this as a menu listener
        // TODO Add this as a menu item listener
    }

    public void uninstall() {
        // TODO Remove this as a menu listener
        // TODO Remove this as a menu item listener

        super.uninstall();
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

    public void sectionInserted(Menu menu, int index) {
        invalidateComponent();
    }

    public void sectionsRemoved(Menu menu, int index, int count) {
        invalidateComponent();
    }

    public void itemInserted(Menu.Section section, int index) {
        invalidateComponent();
    }

    public void itemsRemoved(Menu.Section section, int index, int count) {
        invalidateComponent();
    }

    public void itemMenuChanged(Menu.Item menuItem, Menu previousMenu) {
        // No-op
    }
}
