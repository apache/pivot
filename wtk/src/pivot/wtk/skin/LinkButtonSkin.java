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
package pivot.wtk.skin;

import pivot.wtk.Component;
import pivot.wtk.LinkButton;
import pivot.wtk.Mouse;

/**
 * Abstract base class for link button skins.
 *
 * @author gbrown
 */
public abstract class LinkButtonSkin extends ButtonSkin {
    /**
     * @return
     * <tt>false</tt>; link buttons are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        if (button == Mouse.Button.LEFT) {
            LinkButton linkButton = (LinkButton)getComponent();
            linkButton.press();
        }

        return consumed;
    }
}
