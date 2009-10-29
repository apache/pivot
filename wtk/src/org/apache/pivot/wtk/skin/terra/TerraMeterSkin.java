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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.MeterListener;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;


/**
 * Meter skin.
 */
public class TerraMeterSkin extends ComponentSkin
    implements MeterListener {
    private Color fillColor;
    private Color gridColor;
    private float gridFrequency;
    private Font font;
    private Color textColor;

    private static final FontRenderContext FONT_RENDER_CONTEXT = new FontRenderContext(null, true, false);
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 12;

    public TerraMeterSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        fillColor = theme.getColor(16);
        gridColor = theme.getColor(10);
        gridFrequency = 0.25f;
        font = theme.getFont();
        textColor = theme.getColor(1);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Meter meter = (Meter)component;
        meter.getMeterListeners().add(this);
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public int getPreferredWidth(int height) {
        Meter meter = (Meter)getComponent();
        String text = meter.getText();

        int preferredWidth;
        if (text != null
            && text.length() > 0) {
            Rectangle2D stringBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth()) + 2;
        } else {
            preferredWidth = 0;
        }

        // If Meter has no content, its preferred width is hard coded in the
        // class and is not affected by the height constraint.
        preferredWidth = Math.max(preferredWidth, DEFAULT_WIDTH);

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Meter meter = (Meter)getComponent();
        String text = meter.getText();
        
        int preferredHeight = 0;
        if (text!=null && text.length()>0) {
            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
            preferredHeight = (int)Math.ceil(lm.getHeight()) + 2;
        }
        
        // If Meter has no content, its preferred height is hard coded in the
        // class and is not affected by the width constraint.
        preferredHeight = Math.max(preferredHeight, DEFAULT_HEIGHT);
        
        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        Meter meter = (Meter)getComponent();
        String text = meter.getText();
        
        int preferredWidth = 0;
        int preferredHeight = 0;
        if (text!=null && text.length()>0) {
            Rectangle2D stringBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT);
            preferredWidth = (int)Math.ceil(stringBounds.getWidth()) + 2;
            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
            preferredHeight = (int)Math.ceil(lm.getHeight()) + 2;
        }
        
        // If Meter has no content, its preferred size is hard coded in the class.
        preferredWidth = Math.max(preferredWidth, DEFAULT_WIDTH);
        preferredHeight = Math.max(preferredHeight, DEFAULT_HEIGHT);
        
        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width) {
        Meter meter = (Meter)getComponent();
        String text = meter.getText();
        if (text!=null && text.length()>0) {
            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
            return (int)Math.ceil(lm.getAscent() - 2);
        } else {
            return -1;
        }
    }
    
    @Override
    public void layout() {
        // No-op
    }

    @Override
    public void paint(Graphics2D graphics) {
        Meter meter = (Meter)getComponent();

        int width = getWidth();
        int height = getHeight();
        int meterStop = (int)(meter.getPercentage() * width);

        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);
        if (FONT_RENDER_CONTEXT.isAntiAliased()) {
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                Platform.getTextAntialiasingHint());
        }
        if (FONT_RENDER_CONTEXT.usesFractionalMetrics()) {
            graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        }
        
        // Paint the interior fill
        graphics.setPaint(new GradientPaint(0, 0, TerraTheme.brighten(fillColor),
            0, height, TerraTheme.darken(fillColor)));
        graphics.fillRect(0, 0, meterStop, height);

        // Paint the grid
        graphics.setPaint(gridColor);
        GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
        int nLines = (int)Math.ceil(1 / gridFrequency) - 1;
        float gridSeparation = width * gridFrequency;
        for (int i = 0; i < nLines; i++) {
            int gridX = (int)((i + 1) * gridSeparation);
            GraphicsUtilities.drawLine(graphics, gridX, 0, height, Orientation.VERTICAL);
        }

        String text = meter.getText();
        if (text!=null && text.length()>0) {
            LineMetrics lm = font.getLineMetrics("", FONT_RENDER_CONTEXT);
            int ascent = Math.round(lm.getAscent());
            Rectangle2D stringBounds = font.getStringBounds(text, FONT_RENDER_CONTEXT);
            int textWidth = (int)Math.ceil(stringBounds.getWidth());
            int textX = (width - textWidth - 2) / 2 + 1;
            
            // Paint the text
            Shape previousClip = graphics.getClip();
            graphics.clipRect(0, 0, meterStop, height);
            graphics.setPaint(Color.LIGHT_GRAY);
            graphics.setFont(font);
            graphics.drawString(meter.getText(), textX, ascent+1);
            graphics.setClip(previousClip);
            graphics.clipRect(meterStop, 0, width, height);
            graphics.setPaint(textColor);
            graphics.setFont(font);
            graphics.drawString(meter.getText(), textX, ascent+1);
            graphics.setClip(previousClip);
        }
    }

    public Color getColor() {
        return fillColor;
    }

    public void setColor(Color color) {
        this.fillColor = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        this.gridColor = gridColor;
        repaintComponent();
    }

    public final void setGridColor(String gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        setGridColor(GraphicsUtilities.decodeColor(gridColor));
    }

    public float getGridFrequency() {
        return gridFrequency;
    }

    public void setGridFrequency(float gridFrequency) {
        if (gridFrequency <= 0 || gridFrequency > 1) {
            throw new IllegalArgumentException("gridFrequency must be > 0 and <= 1");
        }
        this.gridFrequency = gridFrequency;
        repaintComponent();
    }

    public final void setGridFrequency(Number gridFrequency) {
        if (gridFrequency == null) {
            throw new IllegalArgumentException("gridFrequency is null.");
        }

        setGridFrequency(gridFrequency.floatValue());
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }
    
    /**
     * Listener for meter percentage changes.
     *
     * @param meter
     *     The source of the event.
     *
     * @param previousPercentage
     *     The previous percentage value.
     */
    @Override
    public void percentageChanged(Meter meter, double previousPercentage) {
        repaintComponent();
    }

    /**
     * Listener for meter text changes.
     *
     * @param meter
     *     The source of the event.
     *
     * @param previousText
     *    The previous text value.
     */
    @Override
    public void textChanged(Meter meter, String previousText) {
        invalidateComponent();
    }
}
