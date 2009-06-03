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
package pivot.wtk.skin;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Transparency;

import pivot.wtk.Bounds;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.GraphicsUtilities;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.ImageViewListener;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;
import pivot.wtk.media.ImageListener;

/**
 * Image view skin.
 * <p>
 * TODO Add rotation style.
 * <p>
 * TODO If horizontal or vertical alignment is "justify", but not both, scale
 * aspect-correct.
 *
 * @author gbrown
 */
public class ImageViewSkin extends ComponentSkin implements ImageViewListener {
    private Color backgroundColor = null;
    private float opacity = 1.0f;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    private int imageX = 0;
    private int imageY = 0;
    private float scaleX = 1;
    private float scaleY = 1;

    private ImageListener imageListener = new ImageListener() {
        public void sizeChanged(Image image, int previousWidth, int previousHeight) {
            invalidateComponent();
        }

        public void regionUpdated(Image image, int x, int y, int width, int height) {
            // TODO A rounding error is causing an off-by-one error; we're
            // accounting for it here by adding 1 to width and height
            Bounds bounds = new Bounds(imageX + (int)Math.floor(((float)x * scaleX)),
                imageY + (int)Math.floor((float)y * scaleY),
                (int)Math.ceil((float)width * scaleX) + 1,
                (int)Math.ceil((float)height * scaleY) + 1);
            repaintComponent(bounds);
        }
    };

    public void install(Component component) {
        super.install(component);

        ImageView imageView = (ImageView)component;
        imageView.getImageViewListeners().add(this);

        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().add(imageListener);
        }
    }

    public void uninstall() {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().remove(imageListener);
        }

        imageView.getImageViewListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        return (image == null) ? 0 : image.getWidth();
    }

    public int getPreferredHeight(int width) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        return (image == null) ? 0 : image.getHeight();
    }

    public Dimensions getPreferredSize() {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        return (image == null) ? new Dimensions(0, 0) : new Dimensions(image.getWidth(),
            image.getHeight());
    }

    public void layout() {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        if (image != null) {
            int width = getWidth();
            int height = getHeight();

            Dimensions imageSize = image.getSize();

            if (horizontalAlignment == HorizontalAlignment.JUSTIFY) {
                imageX = 0;
                scaleX = (float)width / (float)imageSize.width;
            } else {
                scaleX = 1.0f;

                if (horizontalAlignment == HorizontalAlignment.CENTER) {
                    imageX = (width - imageSize.width) / 2;
                } else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
                    imageX = width - imageSize.width;
                } else {
                    imageX = 0;
                }
            }

            if (verticalAlignment == VerticalAlignment.JUSTIFY) {
                imageY = 0;
                scaleY = (float)height / (float)imageSize.height;
            } else {
                scaleY = 1.0f;

                if (verticalAlignment == VerticalAlignment.CENTER) {
                    imageY = (height - imageSize.height) / 2;
                } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
                    imageY = height - imageSize.height;
                } else {
                    imageY = 0;
                }
            }
        }
    }

    public void paint(Graphics2D graphics) {
        ImageView imageView = (ImageView)getComponent();
        Image image = imageView.getImage();

        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (image != null) {
            Graphics2D imageGraphics = (Graphics2D)graphics.create();
            imageGraphics.translate(imageX, imageY);
            imageGraphics.scale(scaleX, scaleY);

            // Apply an alpha composite if the opacity value is less than
            // the current alpha
            float alpha = 1.0f;

            Composite composite = imageGraphics.getComposite();
            if (composite instanceof AlphaComposite) {
                AlphaComposite alphaComposite = (AlphaComposite)composite;
                alpha = alphaComposite.getAlpha();
            }

            if (opacity < alpha) {
                imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
            }

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

    /**
     * An image view's background color dictates whether or not it's opaque. We
     * can't rely on the <tt>opacity</tt> style because even if the skin's
     * opacity is <tt>1</tt>, the actual image itself may contain transparent
     * or translucent pixels.
     *
     * @return
     * <tt>true</tt> if <tt>opacity</tt> is <tt>1</tt>; <tt>false</tt> otherwise.
     */
    @Override
    public boolean isOpaque() {
        boolean opaque = false;

        if (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE) {
            opaque = true;
        }

        return opaque;
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

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Opacity out of range [0,1].");
        }

        this.opacity = opacity;
        repaintComponent();
    }

    public final void setOpacity(Number opacity) {
        if (opacity == null) {
            throw new IllegalArgumentException("opacity is null.");
        }

        setOpacity(opacity.floatValue());
    }

    public HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    public void setHorizontalAlignment(HorizontalAlignment horizontalAlignment) {
        if (horizontalAlignment == null) {
            throw new IllegalArgumentException("horizontalAlignment is null.");
        }

        this.horizontalAlignment = horizontalAlignment;
        layout();
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
        layout();
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
        if (previousImage != null) {
            previousImage.getImageListeners().remove(imageListener);
        }

        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().add(imageListener);
        }

        invalidateComponent();
    }
}
