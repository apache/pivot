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

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.ImageView;
import pivot.wtk.ImageViewListener;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.media.Image;

/**
 * Image view skin.
 * <p>
 * TODO Add rotation style.
 * <p>
 * TODO If horizontal or vertical alignment is specified, but not both, scale
 * aspect-correct in that dimension.
 *
 * @author gbrown
 */
public class ImageViewSkin extends ComponentSkin implements ImageViewListener {
    private Color backgroundColor = null;
    private float opacity = 1.0f;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    public void install(Component component) {
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
        // No-op for component skins
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
            Dimensions imageSize = image.getSize();

            int imageX, imageY;
            float scaleX, scaleY;

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
     * An image view's opacity style dictates whether or not it's opaque.
     *
     * @return
     * <tt>true</tt> if <tt>opacity</tt> is <tt>1</tt>; <tt>false</tt> otherwise.
     */
    @Override
    public boolean isOpaque() {
        boolean opaque = (backgroundColor != null);

        if (!opaque) {
            ImageView imageView = (ImageView)getComponent();
            Image image = imageView.getImage();

            if (image != null) {
                if (image.getWidth() >= getWidth()
                    && image.getHeight() >= getHeight()
                    && opacity == 1) {
                    opaque = true;
                }
            }
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

        setBackgroundColor(decodeColor(backgroundColor));
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
