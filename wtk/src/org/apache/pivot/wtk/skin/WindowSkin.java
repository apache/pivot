/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin;

import java.awt.Color;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowListener;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.media.Image;

/**
 * Window skin.
 */
public class WindowSkin extends ContainerSkin
    implements WindowListener, WindowStateListener {
    /**
     * Focus traversal policy that always returns the window's content. This
     * ensures that focus does not traverse out of the window.
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

        return (content != null) ? content.getPreferredWidth(height) : 0;
    }

    public int getPreferredHeight(int width) {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null) ? content.getPreferredHeight(width) : 0;
    }

    public Dimensions getPreferredSize() {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null) ? content.getPreferredSize() : new Dimensions(0, 0);
    }

    public void layout() {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        if (content != null) {
            content.setSize(window.getSize());
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

    public void activeChanged(Window window, Window obverseWindow) {
        // No-op
    }

    public void maximizedChanged(Window window) {
        // No-op
    }

    public void windowMoved(Window window, int from, int to) {
        // No-op
    }

    // Window state events
    public Vote previewWindowOpen(Window window, Display display) {
        return Vote.APPROVE;
    }

    public void windowOpenVetoed(Window window, Vote reason) {
        // No-op
    }

    public void windowOpened(Window window) {
    }

    public Vote previewWindowClose(Window window) {
        return Vote.APPROVE;
    }

    public void windowCloseVetoed(Window window, Vote reason) {
        // No-op
    }

    public void windowClosed(Window window, Display display) {
    }
}
