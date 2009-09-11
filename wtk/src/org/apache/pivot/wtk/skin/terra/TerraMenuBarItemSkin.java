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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Transparency;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.skin.MenuBarItemSkin;


/**
 * Terra menu bar item skin.
 */
public class TerraMenuBarItemSkin extends MenuBarItemSkin {
    @Override
    public void install(Component component) {
        super.install(component);

        MenuBar.Item menuBarItem = (MenuBar.Item)component;
        menuBarItem.setCursor(Cursor.DEFAULT);
    }

    @Override
    public int getPreferredWidth(int height) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, false);

        return dataRenderer.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, false);

        return dataRenderer.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, false);

        return dataRenderer.getPreferredSize();
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        int width = getWidth();
        int height = getHeight();

        boolean highlight = menuBarItem.isActive();

        // Paint highlight state
        if (highlight) {
            MenuBar menuBar = (MenuBar)menuBarItem.getParent();
            Color activeBackgroundColor = (Color)menuBar.getStyles().get("activeBackgroundColor");
            graphics.setColor(activeBackgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        // Paint the content
        Button.DataRenderer dataRenderer = menuBarItem.getDataRenderer();
        dataRenderer.render(menuBarItem.getButtonData(), menuBarItem, highlight);
        dataRenderer.setSize(width, height);

        dataRenderer.paint(graphics);
    }

    @Override
    public boolean isOpaque() {
        boolean opaque = false;

        MenuBar.Item menuBarItem = (MenuBar.Item)getComponent();

        if (menuBarItem.isActive()) {
            MenuBar menuBar = (MenuBar)menuBarItem.getParent();
            Color activeBackgroundColor = (Color)menuBar.getStyles().get("activeBackgroundColor");
            opaque = (activeBackgroundColor.getTransparency() == Transparency.OPAQUE);
        }

        return opaque;
    }

    public Color getPopupBorderColor() {
        return (Color)menuPopup.getStyles().get("borderColor");
    }

    public void setPopupBorderColor(Color popupBorderColor) {
        menuPopup.getStyles().put("borderColor", popupBorderColor);
    }

    public void setPopupBorderColor(String popupBorderColor) {
        if (popupBorderColor == null) {
            throw new IllegalArgumentException("popupBorderColor is null.");
        }

        menuPopup.getStyles().put("borderColor", popupBorderColor);
    }
}
