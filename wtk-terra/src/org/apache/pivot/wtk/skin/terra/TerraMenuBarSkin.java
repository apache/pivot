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
import java.awt.Font;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuBarListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Menu bar skin.
 */
public class TerraMenuBarSkin extends ContainerSkin implements MenuBarListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color activeColor;
    private Color activeBackgroundColor;
    private int spacing;

    public TerraMenuBarSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        font = theme.getFont().deriveFont(Font.BOLD);
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        activeColor = theme.getColor(4);
        activeBackgroundColor = theme.getColor(14);
        spacing = 2;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        MenuBar menuBar = (MenuBar)component;
        menuBar.getMenuBarListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        MenuBar menuBar = (MenuBar)getComponent();
        MenuBar.ItemSequence items = menuBar.getItems();

        int j = 0;
        for (int i = 0, n = items.getLength(); i < n; i++) {
            if (j > 0) {
                preferredWidth += spacing;
            }

            MenuBar.Item item = items.get(i);
            if (item.isVisible()) {
                preferredWidth += item.getPreferredWidth(height);
                j++;
            }
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        MenuBar menuBar = (MenuBar)getComponent();
        MenuBar.ItemSequence items = menuBar.getItems();

        for (int i = 0, n = items.getLength(); i < n; i++) {
            MenuBar.Item item = items.get(i);
            if (item.isVisible()) {
                preferredHeight = Math.max(item.getPreferredHeight(width), preferredHeight);
            }
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        MenuBar menuBar = (MenuBar)getComponent();
        MenuBar.ItemSequence items = menuBar.getItems();

        int j = 0;
        for (int i = 0, n = items.getLength(); i < n; i++) {
            if (j > 0) {
                preferredWidth += spacing;
            }

            MenuBar.Item item = items.get(i);
            if (item.isVisible()) {
                preferredWidth += item.getPreferredWidth(-1);
                preferredHeight = Math.max(item.getPreferredHeight(-1), preferredHeight);
            }
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        MenuBar menuBar = (MenuBar)getComponent();

        int height = getHeight();
        int itemX = 0;

        for (MenuBar.Item item : menuBar.getItems()) {
            if (item.isVisible()) {
                item.setSize(item.getPreferredWidth(height), height);
                item.setLocation(itemX, 0);

                itemX += item.getWidth() + spacing;
            }
        }
    }

    public final void setBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(color));
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public final void setDisabledColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(color));
    }

    public Color getActiveColor() {
        return activeColor;
    }

    public void setActiveColor(Color activeColor) {
        if (activeColor == null) {
            throw new IllegalArgumentException("activeColor is null.");
        }

        this.activeColor = activeColor;
        repaintComponent();
    }

    public final void setActiveColor(String activeColor) {
        if (activeColor == null) {
            throw new IllegalArgumentException("activeColor is null.");
        }

        setActiveColor(GraphicsUtilities.decodeColor(activeColor));
    }

    public final void setActiveColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setActiveColor(theme.getColor(color));
    }

    public Color getActiveBackgroundColor() {
        return activeBackgroundColor;
    }

    public void setActiveBackgroundColor(Color activeBackgroundColor) {
        if (activeBackgroundColor == null) {
            throw new IllegalArgumentException("activeBackgroundColor is null.");
        }

        this.activeBackgroundColor = activeBackgroundColor;
        repaintComponent();
    }

    public final void setActiveBackgroundColor(String activeBackgroundColor) {
        if (activeBackgroundColor == null) {
            throw new IllegalArgumentException("activeBackgroundColor is null.");
        }

        setActiveBackgroundColor(GraphicsUtilities.decodeColor(activeBackgroundColor));
    }

    public final void setActiveBackgroundColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setActiveBackgroundColor(theme.getColor(color));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("Spacing is negative.");
        }

        this.spacing = spacing;
        invalidateComponent();
    }

    @Override
    public void itemInserted(MenuBar menuBar, int index) {
        invalidateComponent();
    }

    @Override
    public void itemsRemoved(MenuBar menuBar, int index, Sequence<MenuBar.Item> removed) {
        invalidateComponent();
    }

    @Override
    public void activeItemChanged(MenuBar menuBar, MenuBar.Item previousActiveItem) {
        // No-op
    }
}
