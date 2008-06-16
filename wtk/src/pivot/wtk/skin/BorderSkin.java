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

import pivot.collections.Map;
import pivot.wtk.Border;
import pivot.wtk.Component;
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
public class BorderSkin extends TitlePaneSkin {
    private Color borderColor = DEFAULT_BORDER_COLOR;
    private int borderThickness = DEFAULT_BORDER_THICKNESS;
    private Insets padding = DEFAULT_PADDING;
    private CornerRadii cornerRadii = DEFAULT_CORNER_RADII;

    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(0xff, 0xff, 0xff);

    private static final Color DEFAULT_BORDER_COLOR = new Color(0x00, 0x00, 0x00);
    private static final int DEFAULT_BORDER_THICKNESS = 1;
    private static final Insets DEFAULT_PADDING = new Insets(2);
    private static final CornerRadii DEFAULT_CORNER_RADII = new CornerRadii(0);

    protected static final String BORDER_COLOR_KEY = "borderColor";
    protected static final String BORDER_THICKNESS_KEY = "borderThickness";
    protected static final String PADDING_KEY = "padding";
    protected static final String CORNER_RADII_KEY = "cornerRadii";

    public BorderSkin() {
        backgroundColor = DEFAULT_BACKGROUND_COLOR;
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
    public void paint(Graphics2D graphics) {
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
        super.paint(baseGraphics);

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

        graphics.fill(borderArea);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(BORDER_COLOR_KEY)) {
            value = borderColor;
        }
        else if (key.equals(BORDER_THICKNESS_KEY)) {
            value = borderThickness;
        }
        else if (key.equals(PADDING_KEY)) {
            value = padding;
        }
        else if (key.equals(CORNER_RADII_KEY)) {
            value = cornerRadii;
        }
        else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(BORDER_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = borderColor;
            borderColor = (Color)value;

            repaintComponent();
        }
        else if (key.equals(BORDER_THICKNESS_KEY)) {
            if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, false);

            previousValue = borderThickness;
            borderThickness = (Integer)value;

            invalidateComponent();
        }
        else if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
        }
        else if (key.equals(CORNER_RADII_KEY)) {
            if (value instanceof Number) {
                value = new CornerRadii(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new CornerRadii((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, CornerRadii.class, false);

            previousValue = cornerRadii;
            cornerRadii = (CornerRadii)value;

            repaintComponent();
        }
        else {
            super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BORDER_COLOR);
        }
        else if (key.equals(BORDER_THICKNESS_KEY)) {
            previousValue = put(key, DEFAULT_BORDER_THICKNESS);
        }
        else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_PADDING);
        }
        else if (key.equals(CORNER_RADII_KEY)) {
            previousValue = put(key, DEFAULT_CORNER_RADII);
        }
        else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(BORDER_COLOR_KEY)
            || key.equals(BORDER_THICKNESS_KEY)
            || key.equals(PADDING_KEY)
            || key.equals(CORNER_RADII_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
