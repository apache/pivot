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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Gauge;
import org.apache.pivot.wtk.GaugeListener;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Origin;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;


/**
 * Skin class for the {@link Gauge} component, which draws a circular gauge with possible text
 * in the center.
 * <p> The gauge colors, as well as colors for the warning and/or critical colors can be set.
 */
public class TerraGaugeSkin<T extends Number> extends ComponentSkin implements GaugeListener<T> {
    private static final float STROKE_WIDTH = 6.0f;

    private Color backgroundColor;
    /** This is the color of the circle part of the gauge, where the value "is not". */
    private Color gaugeColor;
    private Color textColor;
    /** This is the color for the "value" if it is below the warning or critical levels. */
    private Color color;
    private Color warningColor;
    private Color criticalColor;
    private boolean showTickMarks = false;
    private Insets padding;
    private Font font;
    private float thickness = STROKE_WIDTH;
    private float textAscent;

    private static final RenderingHints renderingHints = new RenderingHints(null);

    static {
        renderingHints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        renderingHints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        renderingHints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    };

    public TerraGaugeSkin() {
        // TODO: set the rest of the default stuff (colors, etc.)
        font = currentTheme().getFont().deriveFont(Font.BOLD, 24.0f);
        setBackgroundColor(defaultBackgroundColor());
        setGaugeColor(8);
        setColor(Color.green);
        setWarningColor(20);
        setCriticalColor(23);
        setTextColor(8);
        setPadding(Insets.NONE);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        @SuppressWarnings("unchecked")
        Gauge<T> gauge = (Gauge<T>)component;
        gauge.getGaugeListeners().add(this);
    }

    /**
     * This is a "display-only" component, so as such will not accept focus.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public void layout() {
        // Nothing to do because we have no child components
    }

    @Override
    public int getPreferredHeight(int width) {
        return 128;  // Note: same as TerraActivityIndicatorSkin
    }

    @Override
    public int getPreferredWidth(int height) {
        return 128;  // Note: same as TerraActivityIndicatorSkin
    }

    /**
     * Do the transformation of the arcs to the normal cartesian coordinates, and for the specified origin,
     * then draw the arc in the given color.  Assumes the rendering hints and the stroke have already been set.
     * <p> Start and extent are in the 0-360 range
     */
    private void drawArc(Graphics2D graphics, Rectangle2D rect, Origin origin, float arcStart, float arcExtent, Color color) {
        float newStart = origin.getOriginAngle() - arcStart - arcExtent;
        Arc2D arc = new Arc2D.Float(rect, newStart, arcExtent, Arc2D.OPEN);
        graphics.setPaint(color);
        graphics.draw(arc);
    }

    @Override
    public void paint(Graphics2D graphics) {
        @SuppressWarnings("unchecked")
        Gauge<T> gauge = (Gauge<T>)getComponent();
        // NOTE: sanity check:  warning level > min && < max, warning < critical if both set
        // also critical > main && < max, critical > warning if both set

        String text = gauge.getText();
        T value = gauge.getValue();
        T minLevel = gauge.getMinValue();
        T maxLevel = gauge.getMaxValue();
        T warningLevel = gauge.getWarningLevel();
        T criticalLevel = gauge.getCriticalLevel();

        Dimensions size = gauge.getSize();
        Origin origin = gauge.getOrigin();

        // The pen thickness is centered on the path, so we need to calculate the path diameter for the
        // center of the stroke width (basically 1/2 the thickness on each side, or the thickness itself)
        float diameter = (float)(Math.min(size.width - padding.getWidth(), size.height - padding.getHeight()))
            - thickness;
        float x = ((float)size.width - diameter) / 2.0f;
        float y = ((float)size.height - diameter) / 2.0f;

        Rectangle2D rect = new Rectangle2D.Float(x, y, diameter, diameter);

        float minValue = minLevel == null ? 0.0f : minLevel.floatValue();
        float maxValue = maxLevel == null ? 100.0f : maxLevel.floatValue();
        float fullRange = maxValue - minValue;
        float toAngle = 360.0f / fullRange;
        float activeValue = (value == null ? 0.0f : value.floatValue()) - minValue;
        float activeAngle = activeValue * toAngle;

        if (backgroundColor != null) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, size.width, size.height);
        }

        graphics.setRenderingHints(renderingHints);
        graphics.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        // Note: presume that critical > warning if both are set
        if (warningLevel != null && warningColor != null) {
            float warningValue = warningLevel.floatValue() - minValue;
            if (activeValue >= warningValue) {
                float warningAngle = warningValue * toAngle;
                if (criticalLevel != null && criticalColor != null) {
                    float criticalValue = criticalLevel.floatValue() - minValue;
                    if (activeValue >= criticalValue) {
                        // Three segments here: min->warning (normal color), warning->critical (warning color), critical->active (critical color)
                        float criticalAngle = criticalValue * toAngle;
                        drawArc(graphics, rect, origin, 0.0f, warningAngle, color);
                        drawArc(graphics, rect, origin, warningAngle, criticalAngle - warningAngle, warningColor);
                        drawArc(graphics, rect, origin, criticalAngle, activeAngle - criticalAngle, criticalColor);
                    } else {
                        // Two segments here: min->warning (normal), warning->active (warning)
                        drawArc(graphics, rect, origin, 0.0f, warningAngle, color);
                        drawArc(graphics, rect, origin, warningAngle, activeAngle - warningAngle, warningColor);
                    }
                } else {
                    // Two segments here: min->warning (normal), warning->active (warning color)
                    drawArc(graphics, rect, origin, 0.0f, warningAngle, color);
                    drawArc(graphics, rect, origin, warningAngle, activeAngle - warningAngle, warningColor);
                }
            } else {
                // Just one segment, the normal value
                drawArc(graphics, rect, origin, 0.0f, activeAngle, color);
            }
        } else if (criticalLevel != null && criticalColor != null) {
            float criticalValue = criticalLevel.floatValue() - minValue;
            if (activeValue > criticalValue) {
                // Two here: min->critical (normal color), critical->active (critical color)
                float criticalAngle = criticalValue * toAngle;
                drawArc(graphics, rect, origin, 0.0f, criticalAngle, color);
                drawArc(graphics, rect, origin, criticalAngle, activeAngle - criticalAngle, criticalColor);
            } else {
                // One, min->active (normal color)
                drawArc(graphics, rect, origin, 0.0f, activeAngle, color);
            }
        } else {
            // Else just one segment (min->active, normal color)
            drawArc(graphics, rect, origin, 0.0f, activeAngle, color);
        }

        // Now draw the "inactive" part the rest of the way
        if (activeAngle < 360.0f) {
            drawArc(graphics, rect, origin, activeAngle, 360.0f - activeAngle, gaugeColor);
        }

        // Draw the text in the middle (if any)
        if (!Utils.isNullOrEmpty(text)) {
            FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics, font, textColor);

            Rectangle2D textBounds = font.getStringBounds(text, fontRenderContext);
            double textX = x + (diameter - textBounds.getWidth()) / 2.0;
            double textY = y + (diameter - textBounds.getHeight()) / 2.0 + textAscent;

            graphics.drawString(text, (int) textX, (int) textY);
        }
    }

    // TODO: possible other styles to implement:
    // show radial marks

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(Color backgroundColor) {
        // We allow a null background color here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public Color getColor() {
        return color;
    }

    public final void setColor(Color color) {
        Utils.checkNull(color, "color");
        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public Color getGaugeColor() {
        return gaugeColor;
    }

    public final void setGaugeColor(Color gaugeColor) {
        Utils.checkNull(gaugeColor, "gaugeColor");
        this.gaugeColor = gaugeColor;
        repaintComponent();
    }

    public final void setGaugeColor(String gaugeColor) {
        setGaugeColor(GraphicsUtilities.decodeColor(gaugeColor, "gaugeColor"));
    }

    public final void setGaugeColor(int gaugeColor) {
        Theme theme = currentTheme();
        setGaugeColor(theme.getColor(gaugeColor));
    }

    public Color getTextColor() {
        return textColor;
    }

    public final void setTextColor(Color textColor) {
        Utils.checkNull(textColor, "textColor");
        this.textColor = textColor;
        repaintComponent();
    }

    public final void setTextColor(String textColor) {
        setTextColor(GraphicsUtilities.decodeColor(textColor, "textColor"));
    }

    public final void setTextColor(int textColor) {
        Theme theme = currentTheme();
        setTextColor(theme.getColor(textColor));
    }

    public Color getWarningColor() {
        return this.warningColor;
    }

    /**
     * Set the warning color to use for the portion of the value display when the
     * value exceeds the gauge's warning level.
     * <p> Note: one, two, or three colors may be displayed depending on the warning
     * and critical levels (and whether they are set), and the warning and critical
     * colors (and whether they are set) (and of course what the value is).
     * @param warningColor A color for the warning levels, or {@code null} to disable
     * the warning level checks.
     */
    public final void setWarningColor(Color warningColor) {
        // Note: null is okay here to effectively disable using the warning color logic
        this.warningColor = warningColor;
        repaintComponent();
    }

    public final void setWarningColor(String warningColor) {
        setWarningColor(GraphicsUtilities.decodeColor(warningColor, "warningColor"));
    }

    public final void setWarningColor(int warningColor) {
        Theme theme = currentTheme();
        setWarningColor(theme.getColor(warningColor));
    }

    public Color getCriticalColor() {
        return this.criticalColor;
    }

    /**
     * Set the critical color to use for the portion of the value display when the
     * value exceeds the gauge's critical level.
     * <p> Note: one, two, or three colors may be displayed depending on the warning
     * and critical levels (and whether they are set), and the warning and critical
     * colors (and whether they are set) (and of course what the value is).
     * @param criticalColor A color for the critical levels, or {@code null} to disable
     * the critical level checks.
     */
    public final void setCriticalColor(Color criticalColor) {
        // Note: null is okay here to disable using the critical color logic
        this.criticalColor = criticalColor;
    }

    public final void setCriticalColor(String criticalColor) {
        setCriticalColor(GraphicsUtilities.decodeColor(criticalColor, "criticalColor"));
    }

    public final void setCriticalColor(int criticalColor) {
        Theme theme = currentTheme();
        setCriticalColor(theme.getColor(criticalColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A dictionary containing the keys {top ,left, bottom, and/or right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A sequence containing the values [top left, bottom, right].
     */
    public final void setPadding(Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A single value to use for the padding on all sides.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A single value to use for the padding on all sides.
     */
    public void setPadding(Number padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A string containing an integer or a JSON map or list with
     * keys/values top, left, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        setPadding(Insets.decode(padding));
    }

    public float getThickness() {
        return thickness;
    }

    /**
     * Set the thickness of the value display.
     * @param thickness The new value (default is {@link #STROKE_WIDTH}).
     * @throws IllegalArgumentException if the value is 0.0 or less.
     */
    public final void setThickness(float thickness) {
        Utils.checkPositive(thickness, "thickness");

        this.thickness = thickness;
        repaintComponent();
    }

    public final void setThickness(Number thickness) {
        Utils.checkNull(thickness, "thickness");
        setThickness(thickness.floatValue());
    }

    public final void setThickness(String thickness) {
        Utils.checkNullOrEmpty(thickness, "thickness");
        setThickness(StringUtils.toNumber(thickness, Float.class));
    }

    @Override
    public void originChanged(Gauge<T> gauge, Origin previousOrigin) {
        invalidateComponent();
    }

    @Override
    public void valueChanged(Gauge<T> gauge, T previousValue) {
        repaintComponent();
    }

    @Override
    public void textChanged(Gauge<T> gauge, String previousText) {
        String text = gauge.getText();
        if (!Utils.isNullOrEmpty(text)) {
            FontRenderContext fontRenderContext = Platform.getFontRenderContext();
            LineMetrics lm = font.getLineMetrics(text, fontRenderContext);
            textAscent = lm.getAscent();
        }
        repaintComponent();
    }

    @Override
    public void minMaxValueChanged(Gauge<T> gauge, T previousMinValue, T previousMaxValue) {
        repaintComponent();
    }

    @Override
    public void warningCriticalLevelChanged(Gauge<T> gauge, T previousWarningLevel, T previousCriticalLevel) {
        repaintComponent();
    }
}
