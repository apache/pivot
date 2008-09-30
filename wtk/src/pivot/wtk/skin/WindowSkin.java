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
import pivot.wtk.Direction;
import pivot.wtk.Display;
import pivot.wtk.FocusTraversalPolicy;
import pivot.wtk.Window;
import pivot.wtk.WindowListener;
import pivot.wtk.WindowStateListener;
import pivot.wtk.media.Image;

/**
 * Window skin.
 *
 * @author gbrown
 */
public class WindowSkin extends ContainerSkin
    implements WindowListener, WindowStateListener {
    /**
     * Focus traversal policy that always returns the window's content. This
     * ensures that focus does not traverse out of the window.
     *
     * @author gbrown
     */
    public static class WindowFocusTraversalPolicy implements FocusTraversalPolicy {
        public Component getNextComponent(Container container, Component component, Direction direction) {
            assert (container instanceof Window) : "container is not a Window";

            if (container == null) {
                throw new IllegalArgumentException("container is null.");
            }

            if (direction == null) {
                throw new IllegalArgumentException("direction is null.");
            }

            Window window = (Window)container;

            return window.getContent();
        }
    }

    public WindowSkin() {
        setBackgroundColor(Color.WHITE);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Window window = (Window)component;
        window.getWindowListeners().add(this);
        window.getWindowStateListeners().add(this);

        window.setFocusTraversalPolicy(new WindowFocusTraversalPolicy());
    }

    @Override
    public void uninstall() {
        Window window = (Window)getComponent();
        window.getWindowListeners().remove(this);
        window.getWindowStateListeners().remove(this);

        window.setFocusTraversalPolicy(null);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null
            && content.isDisplayable()) ?
            content.getPreferredWidth(height) : 0;
    }

    public int getPreferredHeight(int width) {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null
            && content.isDisplayable()) ?
            content.getPreferredHeight(width) : 0;
    }

    public Dimensions getPreferredSize() {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null
            && content.isDisplayable()) ?
            content.getPreferredSize() : new Dimensions(0, 0);
    }

    public void layout() {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);
                content.setSize(window.getSize());
            } else {
                content.setVisible(false);
            }
        }
    }

    // Window events
    public void titleChanged(Window window, String previousTitle) {
        // No-op
    }

    public void iconChanged(Window window, Image previousIcon) {
        // No-op
    }

    public void contentChanged(Window window, Component previousContent) {
        invalidateComponent();
    }

    public void ownerChanged(Window window, Window previousOwner) {
        // No-op
    }

    public void activeChanged(Window window) {
        // No-op
    }

    public void maximizedChanged(Window window) {
        // No-op
    }

    // Window state events
    public boolean previewWindowOpen(Window window, Display display) {
        return true;
    }

    public void windowOpenVetoed(Window window) {
        // No-op
    }

    public void windowOpened(Window window) {
    }

    public boolean previewWindowClose(Window window) {
        return true;
    }

    public void windowCloseVetoed(Window window) {
        // No-op
    }

    public void windowClosed(Window window, Display display) {
    }
}
