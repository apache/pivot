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
package pivot.wtk.skin;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.RoundRectangle2D;

import pivot.collections.Dictionary;
import pivot.wtk.Border;
import pivot.wtk.BorderListener;
import pivot.wtk.Component;
import pivot.wtk.CornerRadii;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;

/**
 * <p>Border skin.</p>
 *
 * <p>TODO Draw title.</p>
 *
 * @author gbrown
 */
public class BorderSkin extends ContainerSkin
    implements BorderListener {
    private Color color = Color.BLACK;
    private int thickness = 1;
    private Insets padding = new Insets(2);
    private CornerRadii cornerRadii = new CornerRadii(0);

    public BorderSkin() {
        setBackgroundColor(Color.WHITE);
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, Border.class);

        super.install(component);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        Border border = (Border)getComponent();
        Component content = border.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (height != -1) {
                height = Math.max(height - (thickness * 2) -
                    padding.top - padding.bottom, 0);
            }

            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right) + (thickness * 2);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        Border border = (Border)getComponent();
        Component content = border.getContent();

        if (content != null
            && content.isDisplayable()) {
            if (width != -1) {
                width = Math.max(width - (thickness * 2)
                    - padding.left - padding.right, 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + (thickness * 2);

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        Border border = (Border)getComponent();
        Component content = border.getContent();

        if (content != null
            && content.isDisplayable()) {
            Dimensions preferredContentSize = content.getPreferredSize();
            preferredWidth = preferredContentSize.width;
            preferredHeight = preferredContentSize.height;
        }

        preferredWidth += (padding.left + padding.right) + (thickness * 2);
        preferredHeight += (padding.top + padding.bottom) + (thickness * 2);

        Dimensions preferredSize = new Dimensions(preferredWidth, preferredHeight);

        return preferredSize;
    }

    public void layout() {
        int width = getWidth();
        int height = getHeight();

        Border border = (Border)getComponent();
        Component content = border.getContent();

        if (content != null) {
            if (content.isDisplayable()) {
                content.setVisible(true);

                content.setLocation(padding.left + thickness,
                    padding.top + thickness);

                int contentWidth = Math.max(width - (padding.left + padding.right
                    + (thickness * 2)), 0);
                int contentHeight = Math.max(height - (padding.top + padding.bottom
                    + (thickness * 2)), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        // TODO Java2D doesn't support variable corner radii; we'll need to
        // "fake" this by drawing multiple rounded rectangles and clipping
        int cornerRadius = cornerRadii.topLeft;

        // Clip the background to a rectangle that is effectively the middle
        // of the border thickness, so we don't anti-alias the background
        // with the outer edge of the border
        RoundRectangle2D clipRectangle = new RoundRectangle2D.Double(thickness / 2,
            thickness / 2,
            width - thickness, height - thickness,
            cornerRadius - thickness / 2, cornerRadius - thickness / 2);

        // Paint the background
        Graphics2D baseGraphics = (Graphics2D)graphics.create();
        baseGraphics.clip(clipRectangle);
        super.paint(baseGraphics);
        baseGraphics.dispose();

        // Create a shape representing the border
        RoundRectangle2D outerRectangle = new RoundRectangle2D.Double(0, 0,
            width, height,
            cornerRadius, cornerRadius);

        RoundRectangle2D innerRectangle = new RoundRectangle2D.Double(thickness,
            thickness,
            width - thickness * 2, height - thickness * 2,
            Math.max(cornerRadius - thickness, 0),
            Math.max(cornerRadius - thickness, 0));

        Area borderArea = new Area(outerRectangle);
        borderArea.subtract(new Area(innerRectangle));

        graphics.setPaint(color);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        // TODO Optimize by using Graphics#fillRoundRect()?
        graphics.fill(borderArea);
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

        setColor(Color.decode(color));
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
        repaintComponent();
    }

    public void setThickness(Number thickness) {
        if (thickness == null) {
            throw new IllegalArgumentException("thickness is null.");
        }

        setThickness(thickness.intValue());
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

    public void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public CornerRadii getCornerRadii() {
        return cornerRadii;
    }

    public void setCornerRadii(CornerRadii cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        this.cornerRadii = cornerRadii;
        repaintComponent();
    }

    public final void setCornerRadii(Dictionary<String, ?> cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        setCornerRadii(new CornerRadii(cornerRadii));
    }

    public final void setCornerRadii(int cornerRadii) {
        setCornerRadii(new CornerRadii(cornerRadii));
    }

    public void setCornerRadii(Number cornerRadii) {
        if (cornerRadii == null) {
            throw new IllegalArgumentException("cornerRadii is null.");
        }

        setCornerRadii(cornerRadii.intValue());
    }

    // Border events
    public void titleChanged(Border border, String previousTitle) {
        invalidateComponent();
    }

    public void contentChanged(Border border, Component previousContent) {
        invalidateComponent();
    }
}
