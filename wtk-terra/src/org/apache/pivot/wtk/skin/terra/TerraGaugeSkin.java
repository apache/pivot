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
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;


/**
 * Skin class for the {@link Gauge} component, which draws a circular gauge with possible text
 * in the center.
 * <p> The gauge colors, as well as colors for the warning and/or critical colors can be set.
 *
 * @param <T> The numeric type for the gauge value.
 */
public class TerraGaugeSkin<T extends Number> extends ComponentSkin implements GaugeListener<T> {
    private static final float STROKE_WIDTH = 6.0f;

    private Color backgroundColor;
    /** This is the color of the circle part of the gauge, where the value "is not". */
    private Color gaugeColor;
    private Color textColor;
    private Color tickColor;
    /** This is the color for the "value" if it is below the warning or critical levels. */
    private Color color;
    private Color borderColor;
    private Color warningColor;
    private Color criticalColor;
    private boolean onlyMaxColor = false;
    private boolean showTickMarks = false;
    private Insets padding;
    private Font font;
    private float thickness = STROKE_WIDTH;
    private T tickFrequency;
    private boolean showBorder = false;
    private float borderThickness = 1.0f;

    private static final RenderingHints RENDERING_HINTS = new RenderingHints(null);

    static {
        RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RENDERING_HINTS.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    };

    public TerraGaugeSkin() {
        font = currentTheme().getFont().deriveFont(Font.BOLD, 24.0f);
        setBackgroundColor(defaultBackgroundColor());
        setGaugeColor(8);
        setColor(Color.green);
        setWarningColor(20);
        setCriticalColor(23);
        setTextColor(8);
        setTickColor(6);
        setBorderColor(6);
        setPadding(Insets.NONE);
    }

    @Override
    public void install(final Component component) {
        super.install(component);

        @SuppressWarnings("unchecked")
        Gauge<T> gauge = (Gauge<T>)component;
        gauge.getGaugeListeners().add(this);
    }

    /**
     * This is a "display-only" component, so as such will not accept focus.
     */
    @Override
    public final boolean isFocusable() {
        return false;
    }

    @Override
    public void layout() {
        // Nothing to do because we have no child components
    }

    @Override
    public int getPreferredHeight(final int width) {
        return 128;  // Note: same as TerraActivityIndicatorSkin
    }

    @Override
    public int getPreferredWidth(final int height) {
        return 128;  // Note: same as TerraActivityIndicatorSkin
    }

    /**
     * Do the transformation of the arcs to the normal cartesian coordinates, and for the specified origin,
     * then draw the arc in the given color.  Assumes the rendering hints and the stroke have already been set.
     * <p> Start and extent are in the 0-360 range.
     *
     * @param graphics Where to draw the arc.
     * @param rect The enclosing rectangle for the arc.
     * @param origin Which of the compass points to use for the start of the arc.
     * @param arcStart The starting angle from the origin for the arc.
     * @param arcExtent The angular extent of the arc (from the start).
     * @param color The color to use for this arc.
     */
    private void drawArc(final Graphics2D graphics, final Rectangle2D rect, final Origin origin,
        final float arcStart, final float arcExtent, final Color color) {
        float newStart = origin.getOriginAngle() - arcStart - arcExtent;
        Arc2D arc = new Arc2D.Float(rect, newStart, arcExtent, Arc2D.OPEN);
        graphics.setPaint(color);
        graphics.draw(arc);
    }

    @Override
    public void paint(final Graphics2D graphics) {
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
        // And only account for the border thickness (the outer part) if > 1.0
        boolean showingBorder = showBorder && borderColor != null;
        float borderAdjust = (showingBorder ? (borderThickness > 1.0f ? borderThickness : 0.0f) : 0.0f);
        float diameter = (float) (Math.min(size.width - padding.getWidth(), size.height - padding.getHeight()))
            - thickness - (borderAdjust * 2.0f);
        float x = ((float) size.width - diameter) / 2.0f;
        float y = ((float) size.height - diameter) / 2.0f;

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

        graphics.setRenderingHints(RENDERING_HINTS);
        graphics.setStroke(new BasicStroke(thickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));

        // Note: presume that critical > warning if both are set
        Color maxColor = null;
        if (warningLevel != null && warningColor != null) {
            float warningValue = warningLevel.floatValue() - minValue;
            if (activeValue >= warningValue) {
                float warningAngle = warningValue * toAngle;
                if (criticalLevel != null && criticalColor != null) {
                    float criticalValue = criticalLevel.floatValue() - minValue;
                    if (activeValue >= criticalValue) {
                        if (onlyMaxColor) {
                            maxColor = criticalColor;
                        } else {
                            // Three segments here:
                            // min->warning (normal color),
                            // warning->critical (warning color),
                            // critical->active (critical color)
                            float criticalAngle = criticalValue * toAngle;
                            drawArc(graphics, rect, origin, 0.0f, warningAngle, color);
                            drawArc(graphics, rect, origin, warningAngle, criticalAngle - warningAngle, warningColor);
                            drawArc(graphics, rect, origin, criticalAngle, activeAngle - criticalAngle, criticalColor);
                        }
                    } else {
                        if (onlyMaxColor) {
                            maxColor = warningColor;
                        } else {
                            // Two segments here: min->warning (normal), warning->active (warning)
                            drawArc(graphics, rect, origin, 0.0f, warningAngle, color);
                            drawArc(graphics, rect, origin, warningAngle, activeAngle - warningAngle, warningColor);
                        }
                    }
                } else {
                    if (onlyMaxColor) {
                        maxColor = warningColor;
                    } else {
                        // Two segments here: min->warning (normal), warning->active (warning color)
                        drawArc(graphics, rect, origin, 0.0f, warningAngle, color);
                        drawArc(graphics, rect, origin, warningAngle, activeAngle - warningAngle, warningColor);
                    }
                }
            } else {
                // Just one segment, the normal value
                drawArc(graphics, rect, origin, 0.0f, activeAngle, color);
            }
        } else if (criticalLevel != null && criticalColor != null) {
            float criticalValue = criticalLevel.floatValue() - minValue;
            if (activeValue > criticalValue) {
                if (onlyMaxColor) {
                    maxColor = criticalColor;
                } else {
                    // Two here: min->critical (normal color), critical->active (critical color)
                    float criticalAngle = criticalValue * toAngle;
                    drawArc(graphics, rect, origin, 0.0f, criticalAngle, color);
                    drawArc(graphics, rect, origin, criticalAngle, activeAngle - criticalAngle, criticalColor);
                }
            } else {
                // One, min->active (normal color)
                drawArc(graphics, rect, origin, 0.0f, activeAngle, color);
            }
        } else {
            // Else just one segment (min->active, normal color)
            drawArc(graphics, rect, origin, 0.0f, activeAngle, color);
        }

        // Now if we didn't draw the multiple segments because of "onlyMaxColor" do it now
        if (onlyMaxColor && maxColor != null) {
            drawArc(graphics, rect, origin, 0.0f, activeAngle, maxColor);
        }

        // Now draw the "inactive" part the rest of the way
        if (activeAngle < 360.0f) {
            drawArc(graphics, rect, origin, activeAngle, 360.0f - activeAngle, gaugeColor);
        }

        // Now draw the border strokes if requested around the whole circle
        if (showingBorder) {
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            graphics.setStroke(new BasicStroke(borderThickness, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            float border = borderThickness > 1.0f ? borderThickness : 0.0f;
            float halfStroke = (thickness + border) / 2.0f;
            float thicknessAdjust = thickness + border;
            Rectangle2D rectOuter = new Rectangle2D.Float(x - halfStroke, y - halfStroke,
                 diameter + thicknessAdjust, diameter + thicknessAdjust);
            Rectangle2D rectInner = new Rectangle2D.Float(x + halfStroke, y + halfStroke,
                diameter - thicknessAdjust, diameter - thicknessAdjust);
            drawArc(graphics, rectInner, origin, 360.0f, 360.0f, borderColor);
            drawArc(graphics, rectOuter, origin, 360.0f, 360.0f, borderColor);
        }

        // On top of the arcs, draw the tick marks (if requested)
        if (showTickMarks && tickFrequency != null && tickColor != null) {
            // frequency is how many gauge values between marks
            float frequency = tickFrequency.floatValue();
            int numMarks = (int) Math.floor(fullRange / frequency);
            graphics.setColor(tickColor == null ? backgroundColor : tickColor);
            // Note: VALUE_STROKE_PURE tends to make the arcs fine but the lines non-uniform,
            // while VALUE_STROKE_NORMALIZE works well for the lines but makes the arcs "wobble" around a bit
            graphics.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);
            graphics.setStroke(new BasicStroke(0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
            float radius = diameter / 2.0f;
            float innerRadius = radius - (thickness / 2.0f);
            float outerRadius = radius + (thickness / 2.0f);
            float xCenter = x + radius;
            float yCenter = y + radius;
            // Draw the "0" mark at the origin
            switch (origin) {
                case NORTH:
                    graphics.drawLine((int) xCenter, (int) (yCenter - innerRadius),
                                      (int) xCenter, (int) (yCenter - outerRadius));
                    break;
                case EAST:
                    graphics.drawLine((int) (xCenter + innerRadius), (int) yCenter,
                                      (int) (xCenter + outerRadius), (int) yCenter);
                    break;
                case SOUTH:
                    graphics.drawLine((int) xCenter, (int) (yCenter + innerRadius),
                                      (int) xCenter, (int) (yCenter + outerRadius));
                    break;
                case WEST:
                    graphics.drawLine((int) (xCenter - innerRadius), (int) yCenter,
                                      (int) (xCenter - outerRadius), (int) yCenter);
                    break;
                default:
                    break;
            }
            // Draw clockwise from the origin, subtracting the frequency each time
            double startAngleRadians = origin.getOriginAngle() * Math.PI / 180.0;
            double frequencyAngleRadians = frequency / fullRange * Math.PI * 2.0;
            double angleRadians = startAngleRadians - frequencyAngleRadians;
            for (int i = 0; i < numMarks; i++) {
                float cosAngle = (float) Math.cos(angleRadians);
                float sinAngle = (float) Math.sin(angleRadians);
                int xInner = (int) (xCenter + (innerRadius * cosAngle) + 0.5f);
                int yInner = (int) (yCenter + (innerRadius * sinAngle) + 0.5f);
                int xOuter = (int) (xCenter + (outerRadius * cosAngle) + 0.5f);
                int yOuter = (int) (yCenter + (outerRadius * sinAngle) + 0.5f);
                graphics.drawLine(xInner, yInner, xOuter, yOuter);
                angleRadians -= frequencyAngleRadians;
            }
        }

        // Draw the text in the middle (if any)
        if (!Utils.isNullOrEmpty(text)) {
            FontRenderContext fontRenderContext = GraphicsUtilities.prepareForText(graphics, font, textColor);
            LineMetrics lm = font.getLineMetrics(text, fontRenderContext);
            Rectangle2D textBounds = font.getStringBounds(text, fontRenderContext);

            // Since this is only a single line, ignore the text leading in the height
            double textHeight = lm.getAscent() + lm.getDescent();
            double textX = x + (diameter - textBounds.getWidth()) / 2.0;
            double textY = y + (diameter - textHeight) / 2.0 + lm.getAscent();

            graphics.drawString(text, (int) textX, (int) textY);
        }
    }

    public final Font getFont() {
        return font;
    }

    public final void setFont(final Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        repaintComponent();
    }

    public final void setFont(final String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(final Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public final Color getBackgroundColor() {
        return backgroundColor;
    }

    public final void setBackgroundColor(final Color backgroundColor) {
        // We allow a null background color here
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(final String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public final void setBackgroundColor(final int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public final Color getColor() {
        return color;
    }

    public final void setColor(final Color color) {
        Utils.checkNull(color, "color");
        this.color = color;
        repaintComponent();
    }

    public final void setColor(final String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(final int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public final Color getGaugeColor() {
        return gaugeColor;
    }

    public final void setGaugeColor(final Color gaugeColor) {
        Utils.checkNull(gaugeColor, "gaugeColor");
        this.gaugeColor = gaugeColor;
        repaintComponent();
    }

    public final void setGaugeColor(final String gaugeColor) {
        setGaugeColor(GraphicsUtilities.decodeColor(gaugeColor, "gaugeColor"));
    }

    public final void setGaugeColor(final int gaugeColor) {
        Theme theme = currentTheme();
        setGaugeColor(theme.getColor(gaugeColor));
    }

    public final Color getTextColor() {
        return textColor;
    }

    public final void setTextColor(final Color textColor) {
        Utils.checkNull(textColor, "textColor");
        this.textColor = textColor;
        repaintComponent();
    }

    public final void setTextColor(final String textColor) {
        setTextColor(GraphicsUtilities.decodeColor(textColor, "textColor"));
    }

    public final void setTextColor(final int textColor) {
        Theme theme = currentTheme();
        setTextColor(theme.getColor(textColor));
    }

    public final Color getTickColor() {
        return tickColor;
    }

    /**
     * Set the color for the radial "tick" marks along the arc of the gauge.
     * <p> Note: to disable tick drawing, use a style of "showTickMarks:false".
     * @param tickColor Any color value, or {@code null} to use the background color.
     */
    public final void setTickColor(final Color tickColor) {
        // Tick color can be null to use the background color.
        this.tickColor = tickColor;
        repaintComponent();
    }

    public final void setTickColor(final String tickColor) {
        setTickColor(GraphicsUtilities.decodeColor(tickColor, "tickColor"));
    }

    public final void setTickColor(final int tickColor) {
        Theme theme = currentTheme();
        setTickColor(theme.getColor(tickColor));
    }

    public final Color getBorderColor() {
        return borderColor;
    }

    /**
     * Set the color for the borders around the arcs.
     * <p> Note: to disable border drawing, use a style of "showBorder:false",
     * or set this color to null.
     * @param borderColor Any color value, or {@code null} to disable border drawing.
     */
    public final void setBorderColor(final Color borderColor) {
        // Border color can be null to not draw the border.
        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(final String borderColor) {
        setBorderColor(GraphicsUtilities.decodeColor(borderColor, "borderColor"));
    }

    public final void setBorderColor(final int borderColor) {
        Theme theme = currentTheme();
        setBorderColor(theme.getColor(borderColor));
    }

    public final Color getWarningColor() {
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
    public final void setWarningColor(final Color warningColor) {
        this.warningColor = warningColor;
        repaintComponent();
    }

    public final void setWarningColor(final String warningColor) {
        setWarningColor(GraphicsUtilities.decodeColor(warningColor, "warningColor"));
    }

    public final void setWarningColor(final int warningColor) {
        Theme theme = currentTheme();
        setWarningColor(theme.getColor(warningColor));
    }

    public final Color getCriticalColor() {
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
    public final void setCriticalColor(final Color criticalColor) {
        this.criticalColor = criticalColor;
    }

    public final void setCriticalColor(final String criticalColor) {
        setCriticalColor(GraphicsUtilities.decodeColor(criticalColor, "criticalColor"));
    }

    public final void setCriticalColor(final int criticalColor) {
        Theme theme = currentTheme();
        setCriticalColor(theme.getColor(criticalColor));
    }

    public final Insets getPadding() {
        return padding;
    }

    public final void setPadding(final Insets padding) {
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
    public final void setPadding(final Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A sequence containing the values [top left, bottom, right].
     */
    public final void setPadding(final Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A single value to use for the padding on all sides.
     */
    public final void setPadding(final int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A single value to use for the padding on all sides.
     */
    public final void setPadding(final Number padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the gauge area and
     * the actual drawing.
     *
     * @param padding A string containing an integer or a JSON map or list with
     * keys/values top, left, bottom, and/or right.
     */
    public final void setPadding(final String padding) {
        setPadding(Insets.decode(padding));
    }

    public final float getThickness() {
        return thickness;
    }

    /**
     * Set the thickness of the value display.
     * @param thickness The new value (default is {@link #STROKE_WIDTH}).
     * @throws IllegalArgumentException if the value is 0.0 or less.
     */
    public final void setThickness(final float thickness) {
        Utils.checkPositive(thickness, "thickness");

        this.thickness = thickness;
        repaintComponent();
    }

    public final void setThickness(final Number thickness) {
        Utils.checkNull(thickness, "thickness");
        setThickness(thickness.floatValue());
    }

    public final void setThickness(final String thickness) {
        Utils.checkNullOrEmpty(thickness, "thickness");
        setThickness(StringUtils.toNumber(thickness, Float.class));
    }

    public final boolean getShowTickMarks() {
        return showTickMarks;
    }

    public final void setShowTickMarks(final boolean showTickMarks) {
        this.showTickMarks = showTickMarks;
        repaintComponent();
    }

    public final T getTickFrequency() {
        return tickFrequency;
    }

    public final void setTickFrequency(final T frequency) {
        Utils.checkNull(frequency, "frequency");
        this.tickFrequency = frequency;
        repaintComponent();
    }

    public final boolean getShowBorder() {
        return showBorder;
    }

    public final void setShowBorder(final boolean showBorder) {
        this.showBorder = showBorder;
        repaintComponent();
    }

    public final float getBorderThickness() {
        return borderThickness;
    }

    /**
     * Set the thickness of the border around the gauge.
     * @param borderThickness The new value (default is 1.0f).
     * @throws IllegalArgumentException if the value is 0.0 or less.
     */
    public final void setBorderThickness(final float borderThickness) {
        Utils.checkPositive(borderThickness, "borderThickness");

        this.borderThickness = borderThickness;
        invalidateComponent();
    }

    public final void setBorderThickness(final Number borderThickness) {
        Utils.checkNull(borderThickness, "borderThickness");
        setBorderThickness(borderThickness.floatValue());
    }

    public final void setBorderThickness(final String borderThickness) {
        Utils.checkNullOrEmpty(borderThickness, "borderThickness");
        setBorderThickness(StringUtils.toNumber(borderThickness, Float.class));
    }

    public final boolean getOnlyMaxColor() {
        return onlyMaxColor;
    }

    /**
     * Set whether or not to only show the maximum color for the entire gauge.
     * @param onlyMaxColor <tt>true</tt> to show the entire gauge in the normal,
     * warning, or critical color (if set) which is appropriate for the value
     * and the warning or critical levels (again if set), <tt>false</tt> to
     * show multiple segments.
     */
    public final void setOnlyMaxColor(final boolean onlyMaxColor) {
        this.onlyMaxColor = onlyMaxColor;
        repaintComponent();
    }

    @Override
    public void originChanged(final Gauge<T> gauge, final Origin previousOrigin) {
        invalidateComponent();
    }

    @Override
    public void valueChanged(final Gauge<T> gauge, final T previousValue) {
        repaintComponent();
    }

    @Override
    public void textChanged(final Gauge<T> gauge, final String previousText) {
        repaintComponent();
    }

    @Override
    public void minValueChanged(final Gauge<T> gauge, final T previousMinValue) {
        repaintComponent();
    }

    @Override
    public void maxValueChanged(final Gauge<T> gauge, final T previousMaxValue) {
        repaintComponent();
    }

    @Override
    public void warningLevelChanged(final Gauge<T> gauge, final T previousWarningLevel) {
        repaintComponent();
    }

    @Override
    public void criticalLevelChanged(final Gauge<T> gauge, final T previousCriticalLevel) {
        repaintComponent();
    }
}
