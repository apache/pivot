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
package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Menu;
import pivot.wtk.Theme;
import pivot.wtk.media.Image;
import pivot.wtk.skin.MenuItemSkin;

/**
 * Terra menu item skin.
 *
 * @author gbrown
 */
public class TerraMenuItemSkin extends MenuItemSkin {
    public final class CheckmarkImage extends Image {
        public static final int SIZE = 14;
        public static final int CHECKMARK_SIZE = 10;

        public int getWidth() {
            return SIZE;
        }

        public int getHeight() {
            return SIZE;
        }

        public void paint(Graphics2D graphics) {
            Menu.Item menuItem = (Menu.Item)getComponent();
            Menu menu = menuItem.getSection().getMenu();

            Color color = (Color)menu.getStyles().get("color");
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(2.5f));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw a checkmark
            int n = CHECKMARK_SIZE / 2;
            int m = CHECKMARK_SIZE / 4;
            int offsetX = (SIZE - (n + m)) / 2;
            int offsetY = (SIZE - n) / 2;

            graphics.drawLine(offsetX, (n - m) + offsetY,
                m + offsetX, n + offsetY);
            graphics.drawLine(m + offsetX, n + offsetY,
                (m + n) + offsetX, offsetY);
        }
    }

    private Image checkmarkImage = new CheckmarkImage();

    public static final int EXPANDER_SIZE = 11;
    public static final int EXPANDER_ICON_SIZE = 5;

    @Override
    public void install(Component component) {
        super.install(component);

        Menu.Item menuItem = (Menu.Item)component;
        menuItem.setCursor(Cursor.DEFAULT);
    }

    public int getPreferredWidth(int height) {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        return dataRenderer.getPreferredWidth(height) + EXPANDER_SIZE;
    }

    public int getPreferredHeight(int width) {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        return Math.max(dataRenderer.getPreferredHeight(width), EXPANDER_SIZE);
    }

    public Dimensions getPreferredSize() {
        Menu.Item menuItem = (Menu.Item)getComponent();

        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, false);

        Dimensions preferredSize = dataRenderer.getPreferredSize();

        preferredSize.width += EXPANDER_SIZE;
        preferredSize.height = Math.max(preferredSize.height, EXPANDER_SIZE);

        return preferredSize;
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        Menu.Item menuItem = (Menu.Item)getComponent();
        Menu menu = menuItem.getSection().getMenu();

        int width = getWidth();
        int height = getHeight();

        boolean highlight = (menuItem.isFocused()
            || menuPopup.isOpen());

        // Paint highlight state
        if (highlight) {
            Color highlightBackgroundColor = (Color)menu.getStyles().get("highlightBackgroundColor");

            TerraTheme theme = (TerraTheme)Theme.getTheme();
            if (theme.useGradients()) {
	            graphics.setPaint(new GradientPaint(width / 2, 0, TerraTheme.brighten(highlightBackgroundColor),
	                width / 2, height, highlightBackgroundColor));
            } else {
                graphics.setColor(highlightBackgroundColor);
            }

            graphics.fillRect(0, 0, width, height);
        }

        // Paint the content
        Button.DataRenderer dataRenderer = menuItem.getDataRenderer();
        dataRenderer.render(menuItem.getButtonData(), menuItem, highlight);
        dataRenderer.setSize(Math.max(width - EXPANDER_SIZE, 0), height);

        dataRenderer.paint(graphics);

        // Paint the expander
        if (menuItem.getMenu() != null) {
            Color color = (Color)(highlight ?
                menu.getStyles().get("highlightColor") : menu.getStyles().get("color"));
            graphics.setColor(color);
            graphics.setStroke(new BasicStroke(0));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.translate(dataRenderer.getWidth() + (EXPANDER_SIZE - EXPANDER_ICON_SIZE) / 2,
                (height - EXPANDER_ICON_SIZE) / 2);

            int[] xPoints = {0, EXPANDER_ICON_SIZE, 0};
            int[] yPoints = {0, EXPANDER_ICON_SIZE / 2, EXPANDER_ICON_SIZE};
            graphics.fillPolygon(xPoints, yPoints, 3);
            graphics.drawPolygon(xPoints, yPoints, 3);
        }
    }

    public Image getCheckmarkImage() {
        return checkmarkImage;
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
