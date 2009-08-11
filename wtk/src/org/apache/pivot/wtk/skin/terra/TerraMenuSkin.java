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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ContainerSkin;


/**
 * Menu skin.
 *
 * @author gbrown
 */
public class TerraMenuSkin extends ContainerSkin implements MenuListener, Menu.SectionListener {
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
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
        highlightColor = theme.getColor(4);
        highlightBackgroundColor = theme.getColor(19);
        marginColor = theme.getColor(11);
        marginColor = new Color(marginColor.getRed(), marginColor.getGreen(),
            marginColor.getBlue(), 228);
        margin = 20;
        separatorColor = theme.getColor(7);
        sectionSpacing = 7;
        showKeyboardShortcuts = true;
    }

    public void install(Component component) {
        super.install(component);

        Menu menu = (Menu)component;
        menu.getMenuListeners().add(this);

        for (Menu.Section section : menu.getSections()) {
            section.getSectionListeners().add(this);
        }
    }

    public void uninstall() {
        Menu menu = (Menu)getComponent();
        menu.getMenuListeners().remove(this);

        for (Menu.Section section : menu.getSections()) {
            section.getSectionListeners().remove(this);
        }

        super.uninstall();
    }

    @Override
    public boolean isFocusable() {
        return true;
    }

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

    public void layout() {
        Menu menu = (Menu)getComponent();
        Menu.SectionSequence sections = menu.getSections();

        int width = getWidth();
        int itemY = 0;

        for (int i = 0, n = sections.getLength(); i < n; i++) {
            Menu.Section section = sections.get(i);

            for (Menu.Item item : section) {
                if (item.isVisible()) {
                    item.setVisible(true);
                    item.setSize(width, item.getPreferredHeight(width));
                    item.setLocation(0, itemY);

                    itemY += item.getHeight();
                } else {
                    item.setVisible(false);
                }
            }

            itemY += sectionSpacing;
        }
    }

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

        setFont(Font.decode(font));
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

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public final void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(GraphicsUtilities.decodeColor(highlightColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
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

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        Menu menu = (Menu)component;

        if (keyCode == Keyboard.KeyCode.UP) {
            menu.transferFocus(null, Direction.BACKWARD);
            consumed = true;
        } else if (keyCode == Keyboard.KeyCode.DOWN) {
            menu.transferFocus(null, Direction.FORWARD);
            consumed = true;
        }

        return consumed;
    }

    public void sectionInserted(Menu menu, int index) {
        Menu.Section section = menu.getSections().get(index);
        section.getSectionListeners().add(this);

        invalidateComponent();
    }

    public void sectionsRemoved(Menu menu, int index, Sequence<Menu.Section> removed) {
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Menu.Section section = removed.get(i);
            section.getSectionListeners().remove(this);
        }

        invalidateComponent();
    }

    public void itemInserted(Menu.Section section, int index) {
        invalidateComponent();
    }

    public void itemsRemoved(Menu.Section section, int index, Sequence<Menu.Item> removed) {
        invalidateComponent();
    }

    public void nameChanged(Menu.Section section, String previousName) {
        // No-op
    }
}
