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
package org.apache.pivot.wtk.skin;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Transparency;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ImageViewListener;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.ImageListener;

/**
 * Image view skin. <p> TODO Add a rotation (float) style.
 */
public class ImageViewSkin extends ComponentSkin implements ImageViewListener {
    private Color backgroundColor = null;
    private float opacity = 1.0f;
    private HorizontalAlignment horizontalAlignment = HorizontalAlignment.CENTER;
    private VerticalAlignment verticalAlignment = VerticalAlignment.CENTER;

    private boolean fill = false;
    private boolean preserveAspectRatio = true;

    private int imageX = 0;
    private int imageY = 0;
    private float scaleX = 1;
    private float scaleY = 1;

    private ImageListener imageListener = new ImageListener() {
        @Override
        public void sizeChanged(final Image image, final int previousWidth, final int previousHeight) {
            invalidateComponent();
        }

        @Override
        public void baselineChanged(final Image image, final int previousBaseline) {
            invalidateComponent();
        }

        @Override
        public void regionUpdated(final Image image, final int x, final int y, final int width, final int height) {
            // TODO A rounding error is causing an off-by-one error; we're
            // accounting for it here by adding 1 to width and height
            Bounds bounds = new Bounds(
                imageX + (int) Math.floor(x * scaleX),
                imageY + (int) Math.floor(y * scaleY),
                (int) Math.ceil(width * scaleX) + 1,
                (int) Math.ceil(height * scaleY) + 1);
            repaintComponent(bounds);
        }
    };

    @Override
    public void install(final Component component) {
        super.install(component);

        ImageView imageView = (ImageView) component;
        imageView.getImageViewListeners().add(this);

        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().add(imageListener);
        }
    }

    @Override
    public int getPreferredWidth(final int height) {
        ImageView imageView = (ImageView) getComponent();
        Image image = imageView.getImage();

        return (image == null) ? 0 : image.getWidth();
    }

    @Override
    public int getPreferredHeight(final int width) {
        ImageView imageView = (ImageView) getComponent();
        Image image = imageView.getImage();

        return (image == null) ? 0 : image.getHeight();
    }

    @Override
    public Dimensions getPreferredSize() {
        ImageView imageView = (ImageView) getComponent();
        Image image = imageView.getImage();

        return (image == null)
            ? Dimensions.ZERO : new Dimensions(image.getWidth(), image.getHeight());
    }

    @Override
    public int getBaseline(final int width, final int height) {
        ImageView imageView = (ImageView) getComponent();
        Image image = imageView.getImage();

        int baseline = -1;

        if (image != null) {
            baseline = image.getBaseline();

            if (baseline != -1) {
                Dimensions imageSize = image.getSize();

                if (fill) {
                    // Scale to fit
                    if (preserveAspectRatio) {
                        float aspectRatio = (float) width / (float) height;
                        float imageAspectRatio = (float) imageSize.width / (float) imageSize.height;

                        if (aspectRatio > imageAspectRatio) {
                            baseline *= (float) height / (float) imageSize.height;
                        } else {
                            float scaleYLocal = (float) width / (float) imageSize.width;
                            baseline *= scaleYLocal;
                            baseline += (int) (height - imageSize.height * scaleYLocal) / 2;
                        }
                    } else {
                        baseline *= (float) height / (float) imageSize.height;
                    }
                } else {
                    if (verticalAlignment == VerticalAlignment.CENTER) {
                        baseline += (height - imageSize.height) / 2;
                    } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
                        baseline += height - imageSize.height;
                    }
                }
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        ImageView imageView = (ImageView) getComponent();
        Image image = imageView.getImage();

        if (image != null) {
            int width = getWidth();
            int height = getHeight();

            Dimensions imageSize = image.getSize();

            if (fill) {
                // Scale to fit
                if (preserveAspectRatio) {
                    float aspectRatio = (float) width / (float) height;
                    float imageAspectRatio = (float) imageSize.width / (float) imageSize.height;

                    if (aspectRatio > imageAspectRatio) {
                        imageY = 0;
                        scaleY = (float) height / (float) imageSize.height;

                        imageX = (int) (width - imageSize.width * scaleY) / 2;
                        scaleX = scaleY;
                    } else {
                        imageX = 0;
                        scaleX = (float) width / (float) imageSize.width;

                        imageY = (int) (height - imageSize.height * scaleX) / 2;
                        scaleY = scaleX;
                    }
                } else {
                    imageX = 0;
                    scaleX = (float) width / (float) imageSize.width;

                    imageY = 0;
                    scaleY = (float) height / (float) imageSize.height;
                }
            } else {
                if (horizontalAlignment == HorizontalAlignment.CENTER) {
                    imageX = (width - imageSize.width) / 2;
                } else if (horizontalAlignment == HorizontalAlignment.RIGHT) {
                    imageX = width - imageSize.width;
                } else {
                    imageX = 0;
                }

                scaleX = 1.0f;

                if (verticalAlignment == VerticalAlignment.CENTER) {
                    imageY = (height - imageSize.height) / 2;
                } else if (verticalAlignment == VerticalAlignment.BOTTOM) {
                    imageY = height - imageSize.height;
                } else {
                    imageY = 0;
                }

                scaleY = 1.0f;
            }
        }
    }

    @Override
    public void paint(final Graphics2D graphics) {
        ImageView imageView = (ImageView) getComponent();
        Image image = imageView.getImage();

        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setPaint(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (image != null) {
            Graphics2D imageGraphics = (Graphics2D) graphics.create();
            imageGraphics.translate(imageX, imageY);
            imageGraphics.scale(scaleX, scaleY);

            // Apply an alpha composite if the opacity value is less than the current alpha
            float alpha = 1.0f;

            Composite composite = imageGraphics.getComposite();
            if (composite instanceof AlphaComposite) {
                AlphaComposite alphaComposite = (AlphaComposite) composite;
                alpha = alphaComposite.getAlpha();
            }

            if (opacity < alpha) {
                imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    opacity));
            }

            image.paint(imageGraphics);
            imageGraphics.dispose();
        }
    }

    /**
     * @return <tt>false</tt>; image views are not focusable.
     */
    @Override
    public final boolean isFocusable() {
        return false;
    }

    @Override
    public final boolean isOpaque() {
        return (backgroundColor != null && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    /**
     * @return The color that is painted behind the image.
     */
    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the color that is painted behind the image.
     *
     * @param backgroundColor The new color for behind the image.
     */
    public final void setBackgroundColor(final Color backgroundColor) {
        // A null background is acceptable
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    /**
     * Sets the color that is painted behind the image.
     *
     * @param backgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    /**
     * @return The opacity of the image, in [0,1].
     */
    public final float getOpacity() {
        return opacity;
    }

    /**
     * Sets the opacity of the image.
     *
     * @param opacity A number between 0 (transparent) and 1 (opaque), inclusive.
     */
    public final void setOpacity(final float opacity) {
        if (opacity < 0 || opacity > 1) {
            throw new IllegalArgumentException("Opacity out of range [0,1].");
        }

        this.opacity = opacity;
        repaintComponent();
    }

    /**
     * Sets the opacity of the image.
     *
     * @param opacity A number between 0 (transparent) and 1 (opaque), inclusive.
     */
    public final void setOpacity(final Number opacity) {
        Utils.checkNull(opacity, "opacity");

        setOpacity(opacity.floatValue());
    }

    /**
     * @return The horizontal alignment of the image.
     */
    public final HorizontalAlignment getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * Sets the horizontal alignment of the image. Ignored if the
     * <code>fill</code> style is true.
     *
     * @param horizontalAlignment The new alignment value.
     */
    public final void setHorizontalAlignment(final HorizontalAlignment horizontalAlignment) {
        Utils.checkNull(horizontalAlignment, "horizontalAlignment");

        this.horizontalAlignment = horizontalAlignment;
        layout();
        repaintComponent();
    }

    /**
     * @return The vertical alignment of the image.
     */
    public final VerticalAlignment getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * Sets the vertical alignment of the image. Ignored if the
     * <code>fill</code> style is true.
     *
     * @param verticalAlignment The new alignment value.
     */
    public final void setVerticalAlignment(final VerticalAlignment verticalAlignment) {
        Utils.checkNull(verticalAlignment, "verticalAlignment");

        this.verticalAlignment = verticalAlignment;
        layout();
        repaintComponent();
    }

    /**
     * @return A boolean indicating whether the image will be scaled to fit the
     * space in which it is placed.
     */
    public final boolean getFill() {
        return fill;
    }

    /**
     * Sets a boolean indicating whether the image will be scaled to fit the
     * space in which it is placed. Note that for scaling to occur, the
     * ImageView must specify a preferred size or be placed in a container that
     * constrains its size.
     *
     * @param fill The new fill flag value.
     */
    public final void setFill(final boolean fill) {
        this.fill = fill;
        layout();
        repaintComponent();
    }

    /**
     * @return A boolean indicating whether, when the image is scaled, its
     * aspect ratio is preserved.
     */
    public final boolean getPreserveAspectRatio() {
        return preserveAspectRatio;
    }

    /**
     * Sets a boolean indicating whether, when the image is scaled, its aspect
     * ratio is preserved. Ignored if the <code>fill</code> style is false.
     *
     * @param preserveAspectRatio The new flag value.
     */
    public final void setPreserveAspectRatio(final boolean preserveAspectRatio) {
        this.preserveAspectRatio = preserveAspectRatio;
        layout();
        repaintComponent();
    }

    // Image view events
    @Override
    public void imageChanged(final ImageView imageView, final Image previousImage) {
        if (previousImage != null) {
            previousImage.getImageListeners().remove(imageListener);
        }

        Image image = imageView.getImage();
        if (image != null) {
            image.getImageListeners().add(imageListener);
        }

        invalidateComponent();
    }

    @Override
    public void asynchronousChanged(final ImageView imageView) {
        // No-op
    }
}
