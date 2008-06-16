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
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Window;

public class DisplaySkin extends ContainerSkin {
    // Style properties
    private boolean activeWindowFollowsMouse = DEFAULT_ACTIVE_WINDOW_FOLLOWS_MOUSE;

    // Default style values
    private static final Color DEFAULT_BACKGROUND_COLOR = Color.LIGHT_GRAY;
    private static final boolean DEFAULT_ACTIVE_WINDOW_FOLLOWS_MOUSE = false;

    // Style keys
    private static final String ACTIVE_WINDOW_FOLLOWS_MOUSE_KEY = "activeWindowFollowsMouse";

    public DisplaySkin() {
        super();

        backgroundColor = DEFAULT_BACKGROUND_COLOR;
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
        for (Component window : display.getComponents()) {
            if (window.isDisplayable()) {
                Boolean maximized = (Boolean)window.getAttributes().get(Display.MAXIMIZED_ATTRIBUTE);
                if (maximized != null
                    && maximized) {
                    window.setLocation(0, 0);
                    window.setSize(display.getSize());
                } else {
                    Dimensions preferredSize = window.getPreferredSize();
                    int preferredWidth = preferredSize.width;
                    int preferredHeight = preferredSize.height;

                    if (window.getWidth() != preferredWidth
                        || window.getHeight() != preferredHeight) {
                        window.setSize(preferredWidth, preferredHeight);
                    }
                }

                window.setVisible(true);
            } else {
                window.setVisible(false);
            }
        }
    }

    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(ACTIVE_WINDOW_FOLLOWS_MOUSE_KEY)) {
            value = activeWindowFollowsMouse;
        } else {
            value = super.get(key);
        }

        return value;
    }

    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(ACTIVE_WINDOW_FOLLOWS_MOUSE_KEY)) {
            validatePropertyType(key, value, Boolean.class, false);

            previousValue = activeWindowFollowsMouse;
            activeWindowFollowsMouse = (Boolean)value;

            repaintComponent();
        } else {
            super.put(key, value);
        }

        return previousValue;
    }

    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(ACTIVE_WINDOW_FOLLOWS_MOUSE_KEY)) {
            previousValue = put(key, DEFAULT_ACTIVE_WINDOW_FOLLOWS_MOUSE);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(ACTIVE_WINDOW_FOLLOWS_MOUSE_KEY)
            || super.containsKey(key));
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean mouseMove(int x, int y) {
        boolean consumed = super.mouseMove(x, y);

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

    public void attributeAdded(Component component, Container.Attribute attribute) {
        getComponent().invalidate();
    }

    public void attributeUpdated(Component component, Container.Attribute attribute,
        Object previousValue) {
        getComponent().invalidate();
    }

    public void attributeRemoved(Component component, Container.Attribute attribute,
        Object value) {
        getComponent().invalidate();
    }
}

