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
package org.apache.pivot.wtk.content;

import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.media.Image;

/**
 * Default menu item data implementation.
 */
public class MenuItemData extends ButtonData {
    private Keyboard.KeyStroke keyboardShortcut = null;

    /**
     * Constructor.
     */
    public MenuItemData() {
        this(null, null, null);
    }

    /**
     * Constructor.
     *
     * @param icon
     * The icon to display in the menu item.
     */
    public MenuItemData(Image icon) {
        this(icon, null, null);
    }

    /**
     * Constructor.
     *
     * @param text
     * The text to display in the menu item.
     */
    public MenuItemData(String text) {
        this(null, text, null);
    }

    /**
     * Constructor.
     *
     * @param icon
     * The icon to display in the menu item.
     *
     * @param text
     * The text to display in the menu item.
     */
    public MenuItemData(Image icon, String text) {
        this(icon, text, null);
    }

    /**
     * Constructor.
     *
     * @param icon
     * The icon to display in the menu item.
     *
     * @param text
     * The text to display in the menu item.
     *
     * @param keyboardShortcut
     * The keyboard shortcut associated with this menu item.
     */
    public MenuItemData(Image icon, String text, Keyboard.KeyStroke keyboardShortcut) {
        super(icon, text);

        this.keyboardShortcut = keyboardShortcut;
    }

    public Keyboard.KeyStroke getKeyboardShortcut() {
        return keyboardShortcut;
    }

    public void setKeyboardShortcut(Keyboard.KeyStroke keyboardShortcut) {
        this.keyboardShortcut = keyboardShortcut;
    }

    public void setKeyboardShortcut(String keyboardShortcut) {
        setKeyboardShortcut(Keyboard.KeyStroke.decode(keyboardShortcut));
    }
}
