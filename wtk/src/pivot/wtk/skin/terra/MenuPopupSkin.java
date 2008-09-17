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
import pivot.wtk.MenuPopup;
import pivot.wtk.MenuPopupListener;
import pivot.wtk.skin.PopupSkin;

/**
 * TODO Implement skin methods.
 *
 * TODO Create a panorama and add the component's Menu to it.
 *
 * @author gbrown
 */
public class MenuPopupSkin extends PopupSkin
    implements MenuPopupListener {
    @Override
    public void install(Component component) {
        validateComponentType(component, MenuPopup.class);

        super.install(component);

        // TODO
    }

    @Override
    public void uninstall() {
        // TODO

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

    public void menuChanged(MenuPopup menuPopup, Menu previousMenu) {
        // TODO?
    }
}
