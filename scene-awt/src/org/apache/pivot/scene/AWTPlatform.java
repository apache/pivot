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

import java.awt.BasicStroke;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.pivot.scene.Font;
import org.apache.pivot.scene.LinearGradientPaint;
import org.apache.pivot.scene.MultiStopGradientPaint;
import org.apache.pivot.scene.PathGeometry;
import org.apache.pivot.scene.Platform;
import org.apache.pivot.scene.RadialGradientPaint;
import org.apache.pivot.scene.SolidColorPaint;
import org.apache.pivot.scene.Stroke;
import org.apache.pivot.scene.media.Raster;

/**
 * AWT platform implementation.
 */
public class AWTPlatform extends Platform {
    private static FontRenderContext fontRenderContext;

    static {
        // Initialize the font render context
        initializeFontRenderContext();

        // Listen for changes to the font desktop hints property
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                initializeFontRenderContext();
            }
        });
    }

    @Override
    public Font.Metrics getFontMetrics(Font font) {
        java.awt.Font nativeFont = getNativeFont(font);
        LineMetrics lm = nativeFont.getLineMetrics("", fontRenderContext);

        return new Font.Metrics(lm.getAscent(), lm.getDescent(), lm.getLeading());
    }

    @Override
    public float measureText(Font font, CharSequence text, int start, int length) {
        java.awt.Font nativeFont = getNativeFont(font);
        Rectangle2D stringBounds = nativeFont.getStringBounds(new CharSequenceCharacterIterator(text),
            0, text.length(), fontRenderContext);

        return (int)Math.ceil(stringBounds.getWidth());
    }

    @Override
    public Raster readRaster(InputStream inputStream) throws IOException {
        return new AWTRaster(ImageIO.read(inputStream));
    }

    @Override
    public void writeRaster(Raster raster, String mimeType,  OutputStream outputStream)
        throws IOException {
        ImageIO.write((BufferedImage)raster.getNativeRaster(), mimeType, outputStream);
    }

    @Override
    protected java.awt.Font getNativeFont(Font font) {
        int style = 0;

        if (font.bold) {
            style |= java.awt.Font.BOLD;
        }

        if (font.italic) {
            style |= java.awt.Font.ITALIC;
        }

        return new java.awt.Font(font.name, font.size, style);
    }

    @Override
    protected java.awt.Color getNativePaint(SolidColorPaint solidColorPaint) {
        return new java.awt.Color(solidColorPaint.color.red,
            solidColorPaint.color.green,
            solidColorPaint.color.blue,
            solidColorPaint.color.alpha);
    }

    @Override
    protected java.awt.LinearGradientPaint getNativePaint(LinearGradientPaint linearGradientPaint) {
        int n = linearGradientPaint.stops.size();
        float[] fractions = new float[n];
        java.awt.Color[] colors = new java.awt.Color[n];

        for (int i = 0; i < n; i++) {
            MultiStopGradientPaint.Stop stop = linearGradientPaint.stops.get(i);
            fractions[i] = stop.offset;
            colors[i] = new java.awt.Color(stop.color.red,
                stop.color.green,
                stop.color.blue,
                stop.color.alpha);
        }

        return new java.awt.LinearGradientPaint(linearGradientPaint.start.x,
            linearGradientPaint.start.y,
            linearGradientPaint.end.x,
            linearGradientPaint.end.y,
            fractions, colors);
    }

    @Override
    protected java.awt.RadialGradientPaint getNativePaint(RadialGradientPaint radialGradientPaint) {
        int n = radialGradientPaint.stops.size();
        float[] fractions = new float[n];
        java.awt.Color[] colors = new java.awt.Color[n];

        for (int i = 0; i < n; i++) {
            MultiStopGradientPaint.Stop stop = radialGradientPaint.stops.get(i);
            fractions[i] = stop.offset;
            colors[i] = new java.awt.Color(stop.color.red,
                stop.color.green,
                stop.color.blue,
                stop.color.alpha);
        }

        return new java.awt.RadialGradientPaint(radialGradientPaint.center.x,
            radialGradientPaint.center.y,
            radialGradientPaint.radius, fractions, colors);
    }

    @Override
    protected BasicStroke getNativeStroke(Stroke stroke) {
        int cap = -1;
        switch (stroke.lineCap) {
            case BUTT: {
                cap = BasicStroke.CAP_BUTT;
                break;
            }

            case ROUND: {
                cap = BasicStroke.CAP_ROUND;
                break;
            }

            case SQUARE: {
                cap = BasicStroke.CAP_SQUARE;
                break;
            }
        }

        int join = -1;
        switch (stroke.lineJoin) {
            case ROUND: {
                join = BasicStroke.JOIN_ROUND;
                break;
            }

            case BEVEL: {
                join = BasicStroke.JOIN_BEVEL;
                break;
            }

            case MITER: {
                join = BasicStroke.JOIN_MITER;
                break;
            }
        }

        float[] dash = null;
        float dashPhase = 0f;
        switch (stroke.lineStyle) {
            case DASHED: {
                // TODO
                break;
            }

            case DOTTED: {
                // TODO
                break;
            }
        }

        return new BasicStroke(stroke.lineWidth, cap, join, stroke.miterLimit, dash, dashPhase);
    }

    @Override
    protected Path2D.Float getNativePathGeometry(PathGeometry pathGeometry) {
        // TODO
        return null;
    }

    public static FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    private static void initializeFontRenderContext() {
        Object aaHint = null;
        Object fmHint = null;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.util.Map<?, ?> fontDesktopHints =
            (java.util.Map<?, ?>)toolkit.getDesktopProperty("awt.font.desktophints");
        if (fontDesktopHints != null) {
            aaHint = fontDesktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            fmHint = fontDesktopHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
        }

        if (aaHint == null) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        }

        if (fmHint == null) {
            fmHint = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
        }

        fontRenderContext = new FontRenderContext(null, aaHint, fmHint);
    }
}
