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
import pivot.collections.Sequence;
import pivot.wtk.Border;
import pivot.wtk.BorderListener;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.CornerRadii;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;

/**
 * TODO Draw title
 *
 * TODO Add titleAlignment style (horizontal only)
 *
 * @author gbrown
 */
public class BorderSkin extends ContainerSkin
    implements BorderListener {
    private Color borderColor = Color.BLACK;
    private int borderThickness = 1;
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
                height = Math.max(height - (borderThickness * 2) -
                    padding.top - padding.bottom, 0);
            }

            preferredWidth = content.getPreferredWidth(height);
        }

        preferredWidth += (padding.left + padding.right) + (borderThickness * 2);

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
                width = Math.max(width - (borderThickness * 2)
                    - padding.left - padding.right, 0);
            }

            preferredHeight = content.getPreferredHeight(width);
        }

        preferredHeight += (padding.top + padding.bottom) + (borderThickness * 2);

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

        preferredWidth += (padding.left + padding.right) + (borderThickness * 2);
        preferredHeight += (padding.top + padding.bottom) + (borderThickness * 2);

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

                content.setLocation(padding.left + borderThickness,
                    padding.top + borderThickness);

                int contentWidth = Math.max(width - (padding.left + padding.right
                    + (borderThickness * 2)), 0);
                int contentHeight = Math.max(height - (padding.top + padding.bottom
                    + (borderThickness * 2)), 0);

                content.setSize(contentWidth, contentHeight);
            } else {
                content.setVisible(false);
            }
        }
    }

    @Override
    public void paintBackground(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        // TODO Java2D doesn't support variable corner radii; we'll need to
        // "fake" this by drawing multiple rounded rectangles and clipping
        int cornerRadius = cornerRadii.topLeft;

        // Clip the background to a rectangle that is effectively the middle
        // of the border thickness, so we don't anti-alias the background
        // with the outer edge of the border
        RoundRectangle2D clipRectangle = new RoundRectangle2D.Double(borderThickness / 2,
            borderThickness / 2,
            width - borderThickness, height - borderThickness,
            cornerRadius - borderThickness / 2, cornerRadius - borderThickness / 2);

        // Paint the background
        Graphics2D baseGraphics = (Graphics2D)graphics.create();
        baseGraphics.clip(clipRectangle);
        super.paintBackground(baseGraphics);
        baseGraphics.dispose();

        // Create a shape representing the border
        RoundRectangle2D outerRectangle = new RoundRectangle2D.Double(0, 0,
            width, height,
            cornerRadius, cornerRadius);

        RoundRectangle2D innerRectangle = new RoundRectangle2D.Double(borderThickness,
            borderThickness,
            width - borderThickness * 2, height - borderThickness * 2,
            Math.max(cornerRadius - borderThickness, 0),
            Math.max(cornerRadius - borderThickness, 0));

        Area borderArea = new Area(outerRectangle);
        borderArea.subtract(new Area(innerRectangle));

        graphics.setPaint(borderColor);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        // TODO Optimize by using Graphics#fillRoundRect()?
        graphics.fill(borderArea);
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

    public int getBorderThickness() {
        return borderThickness;
    }

    public void setBorderThickness(int borderThickness) {
        this.borderThickness = borderThickness;
        repaintComponent();
    }

    public void setBorderThickness(Number borderThickness) {
        if (borderThickness == null) {
            throw new IllegalArgumentException("borderThickness is null.");
        }

        setBorderThickness(borderThickness.intValue());
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

    // Container events
    @Override
    public void componentInserted(Container container, int index) {
        super.componentInserted(container, index);

        invalidateComponent();
    }

    @Override
    public void componentsRemoved(Container container, int index, Sequence<Component> components) {
        super.componentsRemoved(container, index, components);

        invalidateComponent();
    }

    // Border events
    public void titleChanged(Border border, String previousTitle) {
        invalidateComponent();
    }
}
