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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.FocusTraversalPolicy;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowListener;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.media.Image;

/**
 * Window skin.
 */
public class WindowSkin extends ContainerSkin implements Window.Skin,
    WindowListener, WindowStateListener {
    /**
     * Focus traversal policy that always returns the window's content. This
     * ensures that focus does not traverse out of the window.
     */
    public static class WindowFocusTraversalPolicy implements FocusTraversalPolicy {
        @Override
        public Component getNextComponent(Container container, Component component, FocusTraversalDirection direction) {
            assert (container instanceof Window) : "container is not a Window";

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
    public int getPreferredWidth(int height) {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null) ? content.getPreferredWidth(height) : 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null) ? content.getPreferredHeight(width) : 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        return (content != null) ? content.getPreferredSize() : new Dimensions(0, 0);
    }

    @Override
    public void layout() {
        Window window = (Window)getComponent();
        Component content = window.getContent();

        if (content != null) {
            content.setSize(window.getSize());
        }
    }

    @Override
    public Bounds getClientArea() {
        return new Bounds(0, 0, getWidth(), getHeight());
    }

    @Override
    public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(container, button, x, y);

        Window window = (Window)container;
        window.moveToFront();

        return consumed;
    }

    // Window events
    @Override
    public void titleChanged(Window window, String previousTitle) {
        // No-op
    }

    @Override
    public void iconAdded(Window window, Image addedIcon) {
        // No-op
    }

    @Override
    public void iconInserted(Window window, Image addedIcon, int index) {
        // No-op
    }

    @Override
    public void iconsRemoved(Window window, int index, Sequence<Image> removed) {
        // No-op
    }

    @Override
    public void contentChanged(Window window, Component previousContent) {
        invalidateComponent();
    }

    @Override
    public void activeChanged(Window window, Window obverseWindow) {
        // No-op
    }

    @Override
    public void maximizedChanged(Window window) {
        // No-op
    }

    // Window state events
    @Override
    public void windowOpened(Window window) {
        // No-op
    }

    @Override
    public Vote previewWindowClose(Window window) {
        return Vote.APPROVE;
    }

    @Override
    public void windowCloseVetoed(Window window, Vote reason) {
        // No-op
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        // No-op
    }

    @Override
    public Vote previewWindowOpen(Window window) {
        return Vote.APPROVE;
    }

    @Override
    public void windowOpenVetoed(Window window, Vote reason) {
        // No-op
    }

}
