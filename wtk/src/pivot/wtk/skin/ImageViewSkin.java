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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.ImageViewListener;
import pivot.wtk.Rectangle;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

/**
 * TODO Add flipHorizontal and flipVertical styles
 *
 * TODO Add rotation style
 *
 * @author gbrown
 */
public class ImageViewSkin extends ComponentSkin implements ImageViewListener {
    protected Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    protected float opacity = DEFAULT_OPACITY;
    protected float scaleX = DEFAULT_SCALE_X;
    protected float scaleY = DEFAULT_SCALE_Y;
    protected HorizontalAlignment horizontalAlignment = DEFAULT_HORIZONTAL_ALIGNMENT;
    protected VerticalAlignment verticalAlignment = DEFAULT_VERTICAL_ALIGNMENT;

    private static final Color DEFAULT_BACKGROUND_COLOR = null;
    private static final float DEFAULT_OPACITY = 1.0f;
    private static final float DEFAULT_SCALE_X = 1.0f;
    private static final float DEFAULT_SCALE_Y = 1.0f;
    private static final HorizontalAlignment DEFAULT_HORIZONTAL_ALIGNMENT = HorizontalAlignment.CENTER;
    private static final VerticalAlignment DEFAULT_VERTICAL_ALIGNMENT = VerticalAlignment.CENTER;

    protected static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    protected static final String OPACITY_KEY = "opacity";
    protected static final String SCALE_X_KEY = "scaleX";
    protected static final String SCALE_Y_KEY = "scaleY";
    protected static final String HORIZONTAL_ALIGNMENT_KEY = "horizontalAlignment";
    protected static final String VERTICAL_ALIGNMENT_KEY = "verticalAlignment";

    public ImageViewSkin() {
    }

    public void install(Component component) {
        validateComponentType(component, ImageView.class);

        super.install(component);

        ImageView imageView = (ImageView)component;
        imageView.getImageViewListeners().add(this);
    }

    public void uninstall() {
        ImageView imageView = (ImageView)getComponent();
        imageView.getImageViewListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        if (image != null) {
            preferredWidth = (int)Math.round((double)image.getPreferredWidth(-1) * scaleX);
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        if (image != null) {
            preferredHeight = (int)Math.round((double)image.getPreferredHeight(-1) * scaleY);
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        if (image != null) {
            preferredWidth = (int)Math.round((double)image.getPreferredWidth(-1) * scaleX);

            preferredHeight = (int)Math.round((double)image.getPreferredHeight(-1) * scaleY);
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        // No-op for component skins
    }

    public void paint(Graphics2D graphics) {
        ImageView imageView = (ImageView)getComponent();

        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
            graphics.fill(bounds);
        }

        Image image = imageView.getImage();

        if (image != null) {
            // Paint the image centered in the available space
            double width = getWidth();
            double height = getHeight();

            double imageX = 0;
            double imageY = 0;
            double imageWidth = 0;
            double imageHeight = 0;
            Dimensions imageSize = image.getPreferredSize();

            if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                imageWidth = width;
            } else {
                imageWidth = imageSize.getWidth() * scaleX;

                switch (horizontalAlignment) {
                    case LEFT: {
                        imageX = 0;
                        break;
                    }

                    case CENTER: {
                        imageX = (width - imageWidth) / 2d;
                        break;
                    }

                    case RIGHT: {
                        imageX = width - imageWidth;
                        break;
                    }
                }
            }

            if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                imageHeight = height;
            } else {
                imageHeight = imageSize.getHeight() * scaleY;

                switch (verticalAlignment) {
                    case TOP: {
                        imageY = 0;
                        break;
                    }

                    case CENTER: {
                        imageY = (height - imageHeight) / 2d;
                        break;
                    }

                    case BOTTOM: {
                        imageY = height - imageHeight;
                        break;
                    }
                }
            }

            Graphics2D imageGraphics = (Graphics2D)graphics.create();
            imageGraphics.translate(imageX, imageY);
            image.setSize((int)imageWidth, (int)imageHeight);

            imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            image.paint(imageGraphics);
        }
    }

    /**
     * @return
     * <tt>false</tt>; image views are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            value = backgroundColor;
        } else if (key.equals(OPACITY_KEY)) {
            value = opacity;
        } else if (key.equals(SCALE_X_KEY)) {
            value = scaleX;
        } else if (key.equals(SCALE_Y_KEY)) {
            value = scaleY;
        } else if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            value = horizontalAlignment;
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            value = verticalAlignment;
        } else {
            value = super.get(key);
        }

        return value;
    }

    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, true);

            previousValue = backgroundColor;
            backgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(OPACITY_KEY)) {
            validatePropertyType(key, value, Number.class, false);

            previousValue = opacity;
            opacity = ((Number)value).floatValue();

            repaintComponent();
        } else if (key.equals(SCALE_X_KEY)) {
            validatePropertyType(key, value, Number.class, false);

            previousValue = scaleX;
            scaleX = ((Number)value).floatValue();

            invalidateComponent();
        } else if (key.equals(SCALE_Y_KEY)) {
            validatePropertyType(key, value, Number.class, false);

            previousValue = scaleY;
            scaleY = ((Number)value).floatValue();

            invalidateComponent();
        } else if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = HorizontalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, HorizontalAlignment.class, false);

            previousValue = horizontalAlignment;
            horizontalAlignment = (HorizontalAlignment)value;

            repaintComponent();
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            if (value instanceof String) {
                value = VerticalAlignment.decode((String)value);
            }

            validatePropertyType(key, value, VerticalAlignment.class, false);

            previousValue = verticalAlignment;
            verticalAlignment = (VerticalAlignment)value;

            repaintComponent();
        } else {
            super.put(key, value);
        }

        return previousValue;
    }

    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BACKGROUND_COLOR);
        } else if (key.equals(OPACITY_KEY)) {
            previousValue = put(key, DEFAULT_OPACITY);
        } else if (key.equals(SCALE_X_KEY)) {
            previousValue = put(key, DEFAULT_SCALE_X);
        } else if (key.equals(SCALE_Y_KEY)) {
            previousValue = put(key, DEFAULT_SCALE_Y);
        } else if (key.equals(HORIZONTAL_ALIGNMENT_KEY)) {
            previousValue = put(key, DEFAULT_HORIZONTAL_ALIGNMENT);
        } else if (key.equals(VERTICAL_ALIGNMENT_KEY)) {
            previousValue = put(key, DEFAULT_VERTICAL_ALIGNMENT);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(BACKGROUND_COLOR_KEY)
            || key.equals(OPACITY_KEY)
            || key.equals(SCALE_X_KEY)
            || key.equals(SCALE_Y_KEY)
            || key.equals(HORIZONTAL_ALIGNMENT_KEY)
            || key.equals(VERTICAL_ALIGNMENT_KEY)
            || super.containsKey(key));
    }

    public boolean isEmpty() {
        return false;
    }

    public void imageChanged(ImageView imageView, Image previousImage) {
        invalidateComponent();
    }

    public void scaleChanged(ImageView imageView, double previousScaleX, double previousScaleY) {
        invalidateComponent();
    }
}
