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
    private Color backgroundColor = null;
    private float opacity = 1.0f;
    private float scaleX = 1.0f;
    private float scaleY = 1.0f;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

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
            graphics.fillRect(0, 0, getWidth(), getHeight());
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
                imageWidth = (double)imageSize.width * scaleX;

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
                imageHeight = (double)imageSize.height * scaleY;

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
            imageGraphics.scale(scaleX, scaleY);
            image.setSize((int)imageWidth, (int)imageHeight);

            imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            image.paint(imageGraphics);
            imageGraphics.dispose();
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

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(Color.decode(backgroundColor));
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
        repaintComponent();
    }

    public final void setOpacity(Number opacity) {
        if (opacity == null) {
            throw new IllegalArgumentException("opacity is null.");
        }

        setOpacity(opacity.floatValue());
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
        invalidateComponent();
    }

    public final void setScaleX(Number scaleX) {
        if (scaleX == null) {
            throw new IllegalArgumentException("scaleX is null.");
        }

        setScaleX(scaleX.floatValue());
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
        invalidateComponent();
    }

    public final void setScaleY(Number scaleY) {
        if (scaleY == null) {
            throw new IllegalArgumentException("scaleY is null.");
        }

        setScaleY(scaleY.floatValue());
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        repaintComponent();
    }

    public final void setHorizontalAlignment(String horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        setHorizontalAlignment(HorizontalAlignment.decode(horizontalAlignment));
    }

    public VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    public void setVerticalAlignment(VerticalAlignment verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        this.verticalAlignment = verticalAlignment;
        repaintComponent();
    }

    public final void setVerticalAlignment(String verticalAlignment) {
        if (verticalAlignment == null) {
            throw new IllegalArgumentException("verticalAlignment is null.");
        }

        setVerticalAlignment(VerticalAlignment.decode(verticalAlignment));
    }

    // Image view events
    public void imageChanged(ImageView imageView, Image previousImage) {
        invalidateComponent();
    }
}
