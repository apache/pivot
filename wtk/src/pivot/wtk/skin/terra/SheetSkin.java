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

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.collections.Dictionary;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Sheet;
import pivot.wtk.effects.DropShadowDecorator;
import pivot.wtk.skin.WindowSkin;

public class SheetSkin extends WindowSkin {
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Insets padding = new Insets(8);

    private DropShadowDecorator dropShadowDecorator = null;

    public SheetSkin() {
        setBackgroundColor(new Color(0xF7, 0xF5, 0xEB));
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Sheet.class);

        super.install(component);

        Sheet sheet = (Sheet)component;

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        sheet.getDecorators().add(dropShadowDecorator);
    }

    @Override
    public void uninstall() {
        Sheet sheet = (Sheet)getComponent();

        // Detach the drop shadow decorator
        sheet.getDecorators().remove(dropShadowDecorator);
        dropShadowDecorator = null;

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (height != -1) {
                height = Math.max(height - (padding.top + padding.bottom + 2), 0);
            }

            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right + 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (width != -1) {
                width = Math.max(width - (padding.left + padding.right + 2), 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom + 2);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null
            && content.isDisplayable()) {
            Dimensions preferredContentSize = content.getPreferredSize();
            preferredWidth = preferredContentSize.width;
            preferredHeight = preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right + 2);
        preferredHeight += (padding.top + padding.bottom + 2);

        Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);

        return preferredSize;
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Sheet sheet = (Sheet)getComponent();
        Component content = sheet.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);

                content.setLocation(padding.left + 1, padding.top + 1);

                int contentWidth = Math.max(width - (padding.left + padding.right + 2), 0);
                int contentHeight = Math.max(height - (padding.top + padding.bottom + 2), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(borderColor);
        graphics.drawRect(0, 0, width - 1, height - 1);
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(Color.decode(borderColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }
}
