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
import java.awt.Graphics2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.Menu.Item;
import org.apache.pivot.wtk.Menu.Section;
import org.apache.pivot.wtk.MenuListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.ContainerSkin;

/**
 * Menu skin.
 */
public class TerraMenuSkin extends ContainerSkin implements MenuListener, Menu.SectionListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color activeColor;
    private Color activeBackgroundColor;
    private Color marginColor;
    private int margin;
    private Color separatorColor;
    private int sectionSpacing;
    private boolean showKeyboardShortcuts;

    public TerraMenuSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        Color backgroundColor = theme.getColor(4);
        backgroundColor = new Color(backgroundColor.getRed(), backgroundColor.getGreen(),
            backgroundColor.getBlue(), 228);
        setBackgroundColor(backgroundColor);

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        activeColor = theme.getColor(4);
        activeBackgroundColor = theme.getColor(14);
        marginColor = theme.getColor(11);
        marginColor = new Color(marginColor.getRed(), marginColor.getGreen(),
            marginColor.getBlue(), 228);
        margin = 20;
        separatorColor = theme.getColor(7);
        sectionSpacing = 7;
        showKeyboardShortcuts = true;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Menu menu = (Menu)component;
        menu.getMenuListeners().add(this);

        for (Menu.Section section : menu.getSections()) {
            section.getSectionListeners().add(this);
        }

        menu.setFocusTraversalPolicy(new IndexFocusTraversalPolicy(true));
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Menu menu = (Menu)getComponent();
        Menu.SectionSequence sections = menu.getSections();

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Menu.Section section = sections.get(i);

            for (Menu.Item item : section) {
                if (item.isVisible()) {
                    preferredWidth = Math.max(item.getPreferredWidth(-1),
                        preferredWidth);
                }
            }
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Menu menu = (Menu)getComponent();
        Menu.SectionSequence sections = menu.getSections();

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Menu.Section section = sections.get(i);

            for (Menu.Item item : section) {
                if (item.isVisible()) {
                    preferredHeight += item.getPreferredHeight(width);
                }
            }

            if (i > 0) {
                preferredHeight += sectionSpacing;
            }
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Menu menu = (Menu)getComponent();
        Menu.SectionSequence sections = menu.getSections();

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Menu.Section section = sections.get(i);

            for (Menu.Item item : section) {
                if (item.isVisible()) {
                    preferredWidth = Math.max(item.getPreferredWidth(),
                        preferredWidth);
                    preferredHeight += item.getPreferredHeight();
                }
            }

            if (i > 0) {
                preferredHeight += sectionSpacing;
            }
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public void layout() {
        Menu menu = (Menu)getComponent();
        Menu.SectionSequence sections = menu.getSections();

        int width = getWidth();
        int itemY = 0;

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Menu.Section section = sections.get(i);

            for (Menu.Item item : section) {
                if (item.isVisible()) {
                    item.setSize(width, item.getPreferredHeight(width));
                    item.setLocation(0, itemY);

                    itemY += item.getHeight();
                }
            }

            itemY += sectionSpacing;
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        Menu menu = (Menu)getComponent();

        int width = getWidth();
        int height = getHeight();

        // Paint the margin
        if (marginColor != null) {
            graphics.setColor(marginColor);
            graphics.fillRect(0, 0, margin, height);
        }

        Menu.SectionSequence sections = menu.getSections();

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Menu.Section section = sections.get(i);

            if (section.getLength() > 0) {
                Menu.Item item = section.get(section.getLength() - 1);
                int separatorY = item.getY() + item.getHeight() + sectionSpacing / 2;

                // Paint the line
                graphics.setColor(separatorColor);
                graphics.drawLine(1, separatorY, width - 2, separatorY);
            }
        }
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

    public Color getMarginColor() {
        return marginColor;
    }

    public void setMarginColor(Color marginColor) {
        this.marginColor = marginColor;
        repaintComponent();
    }

    public int getMargin() {
        return margin;
    }

    public void setMargin(int margin) {
        if (margin < 0) {
            throw new IllegalArgumentException("margin is negative.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    public Color getSeparatorColor() {
        return separatorColor;
    }

    public void setSeparatorColor(Color separatorColor) {
        if (separatorColor == null) {
            throw new IllegalArgumentException("separatorColor is null.");
        }

        this.separatorColor = separatorColor;
        repaintComponent();
    }

    public int getSectionSpacing() {
        return sectionSpacing;
    }

    public void setSectionSpacing(int sectionSpacing) {
        if (sectionSpacing < 0) {
            throw new IllegalArgumentException("sectionSpacing is negative.");
        }

        this.sectionSpacing = sectionSpacing;
        invalidateComponent();
    }

    public boolean getShowKeyboardShortcuts() {
        return showKeyboardShortcuts;
    }

    public void setShowKeyboardShortcuts(boolean showKeyboardShortcuts) {
        this.showKeyboardShortcuts = showKeyboardShortcuts;
        invalidateComponent();
    }

    /**
     * {@link KeyCode#UP UP} Select the previous enabled menu item.<br>
     * {@link KeyCode#DOWN DOWN} Select the next enabled menu item.<br>
     * {@link KeyCode#LEFT LEFT} Close the current sub-menu.<br>
     * {@link KeyCode#RIGHT RIGHT} Open the sub-menu of the current menu
     * item.<br>
     * {@link KeyCode#ENTER ENTER} 'presses' the active menu item if it
     * does not have a sub-menu.
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        Menu menu = (Menu)component;

        if (keyCode == Keyboard.KeyCode.UP) {
            Menu.SectionSequence sections = menu.getSections();
            int sectionCount = sections.getLength();

            Menu.Item activeItem = menu.getActiveItem();
            int sectionIndex;
            int itemIndex;
            if (activeItem == null) {
                sectionIndex = sectionCount - 1;
                itemIndex = -1;
            } else {
                Menu.Section section = activeItem.getSection();
                sectionIndex = sections.indexOf(section);
                itemIndex = section.indexOf(activeItem) - 1;

                if (itemIndex == -1) {
                    sectionIndex--;
                }
            }

            while (sectionIndex >= 0) {
                Section section = sections.get(sectionIndex);
                if (itemIndex == -1) {
                    int sectionLength = section.getLength();
                    itemIndex = sectionLength - 1;
                }

                while (itemIndex >= 0) {
                    Item item = section.get(itemIndex);

                    if (item.isEnabled()) {
                        item.setActive(true);
                        break;
                    }

                    itemIndex--;
                }

                if (itemIndex >= 0) {
                    break;
                }

                sectionIndex--;
            }

            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.DOWN) {
            Menu.SectionSequence sections = menu.getSections();
            int sectionCount = sections.getLength();

            Menu.Item activeItem = menu.getActiveItem();
            int sectionIndex;
            int itemIndex;
            if (activeItem == null) {
                sectionIndex = 0;
                itemIndex = 0;
            } else {
                Menu.Section section = activeItem.getSection();
                sectionIndex = sections.indexOf(section);
                itemIndex = section.indexOf(activeItem) + 1;
            }

            while (sectionIndex < sectionCount) {
                Section section = sections.get(sectionIndex);
                int sectionLength = section.getLength();

                while (itemIndex < sectionLength) {
                    Item item = section.get(itemIndex);

                    if (item.isEnabled()) {
                        item.setActive(true);
                        break;
                    }

                    itemIndex++;
                }

                if (itemIndex < sectionLength) {
                    break;
                }

                sectionIndex++;
                itemIndex = 0;
            }

            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.LEFT) {
            // Close the window if this is not a top-level menu
            if (menu.getItem() != null) {
                Window window = menu.getWindow();
                window.close();
                consumed = true;
            }
        } else if (keyCode == Keyboard.KeyCode.RIGHT) {
            Menu.Item activeItem = menu.getActiveItem();

            // Press if the item has a sub-menu
            if (activeItem != null
                && activeItem.getMenu() != null) {
                activeItem.press();
                consumed = true;
            }
        } else if (keyCode == Keyboard.KeyCode.ENTER) {
            Menu.Item activeItem = menu.getActiveItem();

            // Press if the item does not have a sub-menu
            if (activeItem != null
                && activeItem.getMenu() == null) {
                activeItem.press();
                consumed = true;
            }
        } else if (keyCode == Keyboard.KeyCode.TAB) {
            consumed = false;
        }

        return consumed;
    }

    /**
     * {@link KeyCode#SPACE SPACE} 'presses' the active menu item if it does
     * not have a sub-menu.
     */
    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyReleased(component, keyCode, keyLocation);

        Menu menu = (Menu)component;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            Menu.Item activeItem = menu.getActiveItem();

            // Press if the item does not have a sub-menu
            if (activeItem != null
                && activeItem.getMenu() == null) {
                activeItem.press();
                consumed = true;
            }
        }

        return consumed;
    }

    /**
     * Select the next enabled menu item where the first character of the
     * rendered text matches the typed key (case insensitive).
     */
    @Override
    public boolean keyTyped(Component component, char character) {
        boolean consumed = super.keyTyped(component, character);

        Menu menu = (Menu)component;
        Menu.SectionSequence sections = menu.getSections();
        int sectionCount = sections.getLength();

        Menu.Item activeItem = menu.getActiveItem();

        int sectionIndex;
        int itemIndex;
        if (activeItem == null) {
            sectionIndex = 0;
            itemIndex = 0;
        } else {
            Menu.Section section = activeItem.getSection();
            sectionIndex = sections.indexOf(section);
            itemIndex = section.indexOf(activeItem) + 1;
        }

        char characterUpper = Character.toUpperCase(character);

        while (sectionIndex < sectionCount) {
            Section section = sections.get(sectionIndex);
            int sectionLength = section.getLength();

            while (itemIndex < sectionLength) {
                Item item = section.get(itemIndex);
                if (item.isEnabled()) {
                    Button.DataRenderer itemDataRenderer = item.getDataRenderer();
                    String string = itemDataRenderer.toString(item.getButtonData());

                    if (string != null
                        && string.length() > 0) {
                        char first = Character.toUpperCase(string.charAt(0));

                        if (first == characterUpper) {
                            item.setActive(true);
                            consumed = true;
                            break;
                        }
                    }
                }

                itemIndex++;
            }

            if (itemIndex < sectionLength) {
                break;
            }

            sectionIndex++;
            itemIndex = 0;
        }

        return consumed;
    }

    @Override
    public void sectionInserted(Menu menu, int index) {
        Menu.Section section = menu.getSections().get(index);
        section.getSectionListeners().add(this);

        invalidateComponent();
    }

    @Override
    public void sectionsRemoved(Menu menu, int index, Sequence<Menu.Section> removed) {
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Menu.Section section = removed.get(i);
            section.getSectionListeners().remove(this);
        }

        invalidateComponent();
    }

    @Override
    public void itemInserted(Menu.Section section, int index) {
        invalidateComponent();
    }

    @Override
    public void itemsRemoved(Menu.Section section, int index, Sequence<Menu.Item> removed) {
        invalidateComponent();
    }

    @Override
    public void nameChanged(Menu.Section section, String previousName) {
        // No-op
    }

    @Override
    public void activeItemChanged(Menu menu, Menu.Item previousActiveItem) {
        // No-op
    }
}
