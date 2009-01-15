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

import java.awt.Color;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Window;

/**
 * Display skin.
 *
 * @author gbrown
 */
public class DisplaySkin extends ContainerSkin {
    // Style properties
    private boolean activeWindowFollowsMouse = false;

    public DisplaySkin() {
        super();
        setBackgroundColor(Color.LIGHT_GRAY);
    }

    @Override
    public void install(Component component) {
        if (!(component instanceof Display)) {
            throw new IllegalArgumentException("DisplaySkin can only be installed on instances of Display.");
        }

        super.install(component);
    }

    public void layout() {
        Display display = (Display)getComponent();

        // Set all components to their preferred sizes
        for (Component component : display) {
            Window window = (Window)component;

            if (window.isDisplayable()) {
                if (window.isMaximized()) {
                    window.setLocation(0, 0);
                    window.setSize(display.getSize());
                } else {
                    Dimensions preferredSize = window.getPreferredSize();

                    if (window.getWidth() != preferredSize.width
                        || window.getHeight() != preferredSize.height) {
                        window.setSize(preferredSize.width, preferredSize.height);
                    }
                }

                window.setVisible(true);
            } else {
                window.setVisible(false);
            }
        }
    }

    public boolean getActiveWindowFollowsMouse() {
        return activeWindowFollowsMouse;
    }

    public void setActiveWindowFollowsMouse(boolean activeWindowFollowsMouse) {
        this.activeWindowFollowsMouse = activeWindowFollowsMouse;
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        if (activeWindowFollowsMouse) {
            Display display = (Display)getComponent();

            Window window = (Window)display.getComponentAt(x, y);
            if (window != null
                && window.isEnabled()
                && !window.isAuxilliary()) {
                Window.setActiveWindow(window);
            }
        }

        return consumed;
    }
}

