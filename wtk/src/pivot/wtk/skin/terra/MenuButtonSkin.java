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

import pivot.wtk.Button;
import pivot.wtk.Dimensions;
import pivot.wtk.Mouse;
import pivot.wtk.skin.ButtonSkin;

/**
 * <p>Menu button skin.</p>
 *
 * <p>TODO Complete this class.</p>
 *
 * <p>TODO This class contains a MenuPopup instance?</p>
 *
 * @author gbrown
 */
public class MenuButtonSkin extends ButtonSkin {
    public void layout() {
        // TODO Auto-generated method stub

    }

    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    public void paint(Graphics2D graphics) {
        // TODO Auto-generated method stub

    }

    public void buttonPushed(Button button) {
        // TODO Toggle the popup's open state
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        // TODO Consume the event if the menu button is repeatable and the event
        // occurs over the trigger

        return super.mouseDown(button, x, y);
    }
}
