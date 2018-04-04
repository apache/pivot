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
package org.apache.pivot.tutorials.bxmlexplorer;

import java.net.URL;

import org.apache.pivot.beans.DefaultProperty;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Window.IconImageSequence;
import org.apache.pivot.wtk.WindowListener;
import org.apache.pivot.wtk.media.Image;

/**
 * Because we can't render a real Window object inside our container, create a
 * fake window that looks mostly like a real window.
 */
@DefaultProperty("content")
public class FakeWindow extends Container {

    private FakeWindowListener.Listeners windowListeners = new FakeWindowListener.Listeners();

    private Component content = null;

    public final Window window;

    public FakeWindow(Window realWindow) {
        Component contentLocal = realWindow.getContent();
        realWindow.setContent(null);
        this.window = realWindow;
        window.getWindowListeners().add(new WindowListener() {

            @Override
            public void titleChanged(Window windowArgument, String previousTitle) {
                windowListeners.titleChanged(FakeWindow.this, previousTitle);
            }

            @Override
            public void iconAdded(Window windowArgument, Image addedIcon) {
                windowListeners.iconAdded(FakeWindow.this, addedIcon);
            }

            @Override
            public void iconInserted(Window windowArgument, Image addedIcon, int index) {
                windowListeners.iconInserted(FakeWindow.this, addedIcon, index);
            }

            @Override
            public void iconsRemoved(Window windowArgument, int index, Sequence<Image> removed) {
                windowListeners.iconsRemoved(FakeWindow.this, index, removed);
            }

            @Override
            public void contentChanged(Window windowArgument, Component previousContent) {
                windowListeners.contentChanged(FakeWindow.this, previousContent);
            }

            @Override
            public void activeChanged(Window windowArgument, Window obverseWindow) {
                // empty block
            }

            @Override
            public void maximizedChanged(Window windowArgument) {
                // empty block
            }
        });
        setContent(contentLocal);
        setSkin(new FakeWindowSkin());
    }

    public IconImageSequence getIcons() {
        return window.getIcons();
    }

    public void setIcon(URL iconURL) {
        window.setIcon(iconURL);
    }

    public void setIcon(String iconName) {
        window.setIcon(iconName);
    }

    public String getTitle() {
        return window.getTitle();
    }

    public void setTitle(String title) {
        window.setTitle(title);
    }

    public ListenerList<FakeWindowListener> getWindowListeners() {
        return windowListeners;
    }

    public Component getContent() {
        return content;
    }

    public void setContent(Component content) {
        Component previousContent = this.content;

        if (content != previousContent) {
            this.content = null;

            // Remove any previous content component
            if (previousContent != null) {
                remove(previousContent);
            }

            // Add the component
            if (content != null) {
                insert(content, 0);
            }

            this.content = content;

            windowListeners.contentChanged(this, previousContent);
        }
    }
}
