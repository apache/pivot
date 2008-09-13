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
import pivot.wtk.ComponentListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Container;
import pivot.wtk.ContainerMouseListener;
import pivot.wtk.Cursor;
import pivot.wtk.Display;
import pivot.wtk.Mouse;
import pivot.wtk.Popup;
import pivot.wtk.Window;

/**
 * <p>Popup skin.</p>
 *
 * @author gbrown
 */
public class PopupSkin extends WindowSkin
    implements ComponentListener, ComponentMouseButtonListener, ContainerMouseListener {
    public PopupSkin() {
        super();
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Popup.class);

        super.install(component);
    }

    @Override
    public void uninstall() {
        throw new UnsupportedOperationException("Can't uninstall "
            + getClass().getName());
    }

    // Window events
    public void windowOpened(Window window) {
        // Add this as a component and container mouse listener on display
        Display display = window.getDisplay();
        display.getComponentMouseButtonListeners().add(this);
        display.getContainerMouseListeners().add(this);

        // Add this as a component listener on the affiliate's ancestry
        Popup popup = (Popup)window;
        Component ancestor = popup.getAffiliate();

        while (ancestor != null) {
            ancestor.getComponentListeners().add(this);
            ancestor = ancestor.getParent();
        }
    }

    public void windowClosed(Window window, Display display) {
        // Remove this as a component and container mouse listener on display
        display.getComponentMouseButtonListeners().remove(this);
        display.getContainerMouseListeners().remove(this);

        // Remove this as a component listener on the affiliate's ancestry
        Popup popup = (Popup)window;
        Component ancestor = popup.getAffiliate();

        while (ancestor != null) {
            ancestor.getComponentListeners().remove(this);
            ancestor = ancestor.getParent();
        }
    }

    // Component events
    public void parentChanged(Component component, Container previousParent) {
        // Ignore this event if it came from the affiliate's window.
        // The window's parent may change as a result of a z-order change or
        // the window being closed. As an owned window, this window will remain
        // on top of the affiliate's window if the parent change is a result of
        // a z-order change and will close if the parent change is a result
        // of the affiliate's window being closed.

        if (!(component instanceof Window)) {
            // Remove this as a component listener on the previous parent's
            // ancestry
            Component ancestor = previousParent;

            while (ancestor != null) {
                ancestor.getComponentListeners().remove(this);
                ancestor = ancestor.getParent();
            }

            // Close the popup
            Popup popup = (Popup)getComponent();
            popup.close();
        }
    }

    public void sizeChanged(Component component, int previousWidth, int previousHeight) {
        // Close the popup
        Popup popup = (Popup)getComponent();
        popup.close();
    }

    public void locationChanged(Component component, int previousX, int previousY) {
        // Close the popup
        Popup popup = (Popup)getComponent();
        popup.close();
    }

    public void visibleChanged(Component component) {
        // Close the popup
        Popup popup = (Popup)getComponent();
        popup.close();
    }

    public void styleUpdated(Component component, String styleKey, Object previousValue) {
        // No-op
    }

    public void cursorChanged(Component component, Cursor previousCursor) {
        // No-op
    }

    public void tooltipTextChanged(Component component, String previousTooltipText) {
        // No-op
    }

    // Component mouse events
    public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        // If the event did not occur within a window that is owned by this
        // popup, close the popup
        Display display = (Display)component;
        Popup popup = (Popup)getComponent();

        Window window = (Window)display.getComponentAt(x, y);
        if (window == null
            || !popup.isOwningAncestorOf(window)) {
            popup.close();
        }
    }

    public void mouseUp(Component component, Mouse.Button button, int x, int y) {
    }

    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
    }

    // Container mouse events
    public void mouseMove(Container container, int x, int y) {
    }

    public void mouseDown(Container container, Mouse.Button button, int x, int y) {
    }

    public void mouseUp(Container container, Mouse.Button button, int x, int y) {
    }

    public void mouseWheel(Container container, Mouse.ScrollType scrollType,
        int scrollAmount, int wheelRotation, int x, int y) {
        // If the event did not occur over this component, close the popup
        Display display = (Display)container;
        Popup popup = (Popup)getComponent();

        if (display.getComponentAt(x, y) != popup) {
            popup.close();
        }
    }
}
