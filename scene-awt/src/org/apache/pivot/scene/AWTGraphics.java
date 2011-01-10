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
package org.apache.pivot.scene;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import org.apache.pivot.scene.Bounds;
import org.apache.pivot.scene.Color;
import org.apache.pivot.scene.Font;
import org.apache.pivot.scene.Graphics;
import org.apache.pivot.scene.Paint;
import org.apache.pivot.scene.PathGeometry;
import org.apache.pivot.scene.Platform;
import org.apache.pivot.scene.SolidColorPaint;
import org.apache.pivot.scene.Stroke;
import org.apache.pivot.scene.Transform;
import org.apache.pivot.scene.media.Raster;

/**
 * AWT graphics implementation.
 */
public class AWTGraphics extends Graphics {
    private Graphics2D graphics2D;

    private float alpha;
    private CompositeOperation compositeOperation;
    private boolean antiAliased;
    private Stroke stroke;
    private Paint paint;
    private Font font;

    private Line2D.Float line2D = new Line2D.Float();
    private Rectangle2D.Float rectangle2D = new Rectangle2D.Float();
    private RoundRectangle2D.Float roundRectangle2D = new RoundRectangle2D.Float();
    private Arc2D.Float arc2D = new Arc2D.Float();
    private Ellipse2D.Float ellipse2D = new Ellipse2D.Float();

    public AWTGraphics(Graphics2D graphics2D) {
        if (graphics2D == null) {
            throw new IllegalArgumentException();
        }

        this.graphics2D = graphics2D;

        setAlpha(1.0f);
        setCompositeOperation(CompositeOperation.SOURCE_OVER);
        setAntiAliased(false);
        setStroke(new Stroke());
        setPaint(new SolidColorPaint(Color.BLACK));
        setFont(Platform.getPlatform().getDefaultFont());
    }

    // Clipping
    @Override
    public void clip(int x, int y, int width, int height) {
        graphics2D.clipRect(x, y, width, height);
    }

    @Override
    public Bounds getClipBounds() {
        Rectangle clipBounds = graphics2D.getClipBounds();
        return new Bounds(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
    }

    // Compositing
    @Override
    public float getAlpha() {
        return alpha;
    }

    @Override
    public void setAlpha(float alpha) {
        if (alpha < 0
            || alpha > 1) {
            throw new IllegalArgumentException("Alpha must be between 0 and 1.");
        }

        this.alpha = alpha;

        AlphaComposite alphaComposite = (AlphaComposite)graphics2D.getComposite();
        graphics2D.setComposite(alphaComposite.derive(alpha));
    }

    @Override
    public CompositeOperation getCompositeOperation() {
        return compositeOperation;
    }

    @Override
    public void setCompositeOperation(CompositeOperation compositeOperation) {
        int rule = -1;
        switch (compositeOperation) {
            case SOURCE_ATOP: {
                rule = AlphaComposite.SRC_ATOP;
                break;
            }

            case SOURCE_IN: {
                rule = AlphaComposite.SRC_IN;
                break;
            }

            case SOURCE_OUT: {
                rule = AlphaComposite.SRC_OUT;
                break;
            }

            case SOURCE_OVER: {
                rule = AlphaComposite.SRC_OVER;
                break;
            }

            case DESTINATION_ATOP: {
                rule = AlphaComposite.DST_ATOP;
                break;
            }

            case DESTINATION_IN: {
                rule = AlphaComposite.DST_IN;
                break;
            }

            case DESTINATION_OUT: {
                rule = AlphaComposite.DST_OUT;
                break;
            }

            case DESTINATION_OVER: {
                rule = AlphaComposite.DST_OVER;
                break;
            }

            case CLEAR: {
                rule = AlphaComposite.CLEAR;
                break;
            }

            case XOR: {
                rule = AlphaComposite.XOR;
                break;
            }
        }

        graphics2D.setComposite(AlphaComposite.getInstance(rule, alpha));
    }

    // Anti-aliasing
    @Override
    public boolean isAntiAliased() {
        return antiAliased;
    }

    @Override
    public void setAntiAliased(boolean antiAliased) {
        this.antiAliased = antiAliased;

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAliased ?
            RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    // Primitive drawing/filling
    @Override
    public Stroke getStroke() {
        return stroke;
    }

    @Override
    public void setStroke(Stroke stroke) {
        if (stroke == null) {
            throw new IllegalArgumentException("stroke is null.");
        }

        graphics2D.setStroke((java.awt.Stroke)stroke.getNativeStroke());
    }

    @Override
    public Paint getPaint() {
        return paint;
    }

    @Override
    public void setPaint(Paint paint) {
        if (paint == null) {
            throw new IllegalArgumentException("paint is null.");
        }

        this.paint = paint;

        graphics2D.setPaint((java.awt.Paint)paint.getNativePaint());
    }

    @Override
    public void drawLine(float x1, float y1, float x2, float y2) {
        // TODO Optimize to use drawLine() when appropriate

        line2D.x1 = x1;
        line2D.y1 = y1;
        line2D.x2 = x2;
        line2D.y2 = y2;

        graphics2D.draw(line2D);
    }

    @Override
    public void drawRectangle(float x, float y, float width, float height, float cornerRadius) {
        // TODO Optimize to use drawRect() or drawRoundRect() when appropriate

        if (cornerRadius == 0) {
            rectangle2D.x = x;
            rectangle2D.y = y;
            rectangle2D.width = width;
            rectangle2D.height = height;

            graphics2D.draw(rectangle2D);
        } else {
            roundRectangle2D.x = x;
            roundRectangle2D.y = y;
            roundRectangle2D.width = width;
            roundRectangle2D.height = height;
            roundRectangle2D.archeight = cornerRadius;
            roundRectangle2D.arcwidth = cornerRadius;

            graphics2D.draw(roundRectangle2D);
        }
    }

    @Override
    public void drawArc(float x, float y, float width, float height, float start, float extent) {
        // TODO Optimize to use drawArc() when appropriate

        arc2D.x = x;
        arc2D.y = y;
        arc2D.width = width;
        arc2D.height = height;
        arc2D.start = start;
        arc2D.extent = extent;

        graphics2D.draw(arc2D);
    }

    @Override
    public void drawEllipse(float x, float y, float width, float height) {
        // TODO Optimize to use drawOval() when appropriate

        ellipse2D.x = x;
        ellipse2D.y = y;
        ellipse2D.width = width;
        ellipse2D.height = height;

        graphics2D.draw(ellipse2D);
    }

    @Override
    public void drawPath(PathGeometry pathGeometry) {
        graphics2D.draw((Path2D.Float)pathGeometry.getNativePathGeometry());
    }

    @Override
    public void fillRectangle(float x, float y, float width, float height, float cornerRadius) {
        // TODO Optimize to use fillRect() or fillRoundRect() when appropriate

        if (cornerRadius == 0) {
            rectangle2D.x = x;
            rectangle2D.y = y;
            rectangle2D.width = width;
            rectangle2D.height = height;

            graphics2D.fill(rectangle2D);
        } else {
            roundRectangle2D.x = x;
            roundRectangle2D.y = y;
            roundRectangle2D.width = width;
            roundRectangle2D.height = height;
            roundRectangle2D.archeight = cornerRadius;
            roundRectangle2D.arcwidth = cornerRadius;

            graphics2D.fill(roundRectangle2D);
        }
    }

    @Override
    public void fillArc(float x, float y, float width, float height, float start, float extent) {
        // TODO Optimize to use fillArc() when appropriate

        arc2D.x = x;
        arc2D.y = y;
        arc2D.width = width;
        arc2D.height = height;
        arc2D.start = start;
        arc2D.extent = extent;

        graphics2D.fill(arc2D);
    }

    @Override
    public void fillEllipse(float x, float y, float width, float height) {
        // TODO Optimize to use drawOval() when appropriate

        ellipse2D.x = x;
        ellipse2D.y = y;
        ellipse2D.width = width;
        ellipse2D.height = height;

        graphics2D.draw(ellipse2D);
    }

    @Override
    public void fillPath(PathGeometry pathGeometry) {
        graphics2D.fill((Path2D.Float)pathGeometry.getNativePathGeometry());
    }

    @Override
    public void drawRaster(Raster raster, int x, int y, int width, int height) {
        graphics2D.drawImage((BufferedImage)raster.getNativeRaster(), x, y, null);
    }

    // Blitting
    @Override
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {
        graphics2D.copyArea(x, y, width, height, dx, dy);
    }

    // Text
    @Override
    public Font getFont() {
        return font;
    }

    @Override
    public void setFont(Font font) {
        graphics2D.setFont((java.awt.Font)font.getNativeFont());
    }

    @Override
    public void drawText(CharSequence text, int start, int length, float x, float y) {
        java.awt.Font nativeFont = (java.awt.Font)font.getNativeFont();
        FontRenderContext fontRenderContext = AWTPlatform.getFontRenderContext();
        GlyphVector glyphVector = nativeFont.createGlyphVector(fontRenderContext,
            new CharSequenceCharacterIterator(text, start, start + length));

        graphics2D.drawGlyphVector(glyphVector, x, y);
    }

    // Transformations
    @Override
    public void transform(float m11, float m12, float m21, float m22, float dx, float dy) {
        // TODO
    }

    @Override
    public Transform getCurrentTransform() {
        // TODO
        return null;
    }

    // Creation/disposal
    @Override
    public Graphics create() {
        return new AWTGraphics((Graphics2D)graphics2D.create());
    }

    @Override
    public void dispose() {
        graphics2D.dispose();
    }

    @Override
    public Object getNativeGraphics() {
        return graphics2D;
    }
}
