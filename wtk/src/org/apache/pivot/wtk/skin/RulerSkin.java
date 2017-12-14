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

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Borders;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Ruler;
import org.apache.pivot.wtk.RulerListener;
import org.apache.pivot.wtk.Theme;

public class RulerSkin extends ComponentSkin implements RulerListener {
    private Color color;
    private Color backgroundColor;
    private int markerSpacing;
    private Insets markerInsets;
    private boolean flip;
    private Borders borders;
    private int majorDivision;
    private int minorDivision;

    public RulerSkin() {
        // For now the default colors are not from the Theme.
        setColor(Color.BLACK);
        setBackgroundColor(CSSColor.LightYellow.getColor());

        markerSpacing = 5;
        markerInsets = new Insets(0);
        flip = false;
        borders = Borders.ALL;
        majorDivision = 4;
        minorDivision = 2;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Ruler ruler = (Ruler) component;
        ruler.getRulerListeners().add(this);
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public int getPreferredHeight(int width) {
        Ruler ruler = (Ruler) getComponent();
        Orientation orientation = ruler.getOrientation();

        return (orientation == Orientation.HORIZONTAL) ? 20 : 0;
    }

    @Override
    public int getPreferredWidth(int height) {
        Ruler ruler = (Ruler) getComponent();
        Orientation orientation = ruler.getOrientation();

        return (orientation == Orientation.VERTICAL) ? 20 : 0;
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        int top = markerInsets.top;
        int left = markerInsets.left;
        int bottom = height - markerInsets.getHeight();
        int right = width - markerInsets.getWidth();

        Ruler ruler = (Ruler) getComponent();

        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(color);
        switch (borders) {
            case NONE:
                break;
            case ALL:
                graphics.drawRect(0, 0, width - 1, height - 1);
                break;
            case TOP:
                graphics.drawLine(0, 0, width - 1, 0);
                break;
            case BOTTOM:
                graphics.drawLine(0, height - 1, width - 1, height - 1);
                break;
            case LEFT:
                graphics.drawLine(0, 0, 0, height - 1);
                break;
            case RIGHT:
                graphics.drawLine(width - 1, 0, width - 1, height - 1);
                break;
            case LEFT_RIGHT:
                graphics.drawLine(0, 0, 0, height - 1);
                graphics.drawLine(width - 1, 0, width - 1, height - 1);
                break;
            case TOP_BOTTOM:
                graphics.drawLine(0, 0, width - 1, 0);
                graphics.drawLine(0, height - 1, width - 1, height - 1);
                break;
        }

        height -= markerInsets.getHeight();
        width -= markerInsets.getWidth();

        Orientation orientation = ruler.getOrientation();
        switch (orientation) {
            case HORIZONTAL: {
                int mid = (height + 2) * 3 / 8;
                int start = flip ? bottom - 1 : top;
                int end2 = flip ? (bottom - 1 - height / 2) : height / 2;
                int end3 = flip ? (bottom - 1 - mid) : mid;
                int end4 = flip ? (bottom - 1 - height / 4) : height / 4;

                for (int i = 0, n = width / markerSpacing + 1; i < n; i++) {
                    int x = i * markerSpacing + left;

                    
                    if (majorDivision != 0 && i % majorDivision == 0) {
                        graphics.drawLine(x, start, x, end2);
                    } else if (minorDivision != 0 && i % minorDivision == 0) {
                        graphics.drawLine(x, start, x, end3);
                    } else {
                        graphics.drawLine(x, start, x, end4);
                    }
                }

                break;
            }

            case VERTICAL: {
                int mid = (width + 2) * 3 / 8;
                int start = flip ? right - 1 : left;
                int end2 = flip ? (right - 1 - width / 2) : width / 2;
                int end3 = flip ? (right - 1 - mid) : mid;
                int end4 = flip ? (right - 1 - width / 4) : width / 4;

                for (int i = 0, n = height / markerSpacing + 1; i < n; i++) {
                    int y = i * markerSpacing + top;

                    if (majorDivision != 0 && i % majorDivision == 0) {
                        graphics.drawLine(start, y, end2, y);
                    } else if (minorDivision != 0 && i % minorDivision == 0) {
                        graphics.drawLine(start, y, end3, y);
                    } else {
                        graphics.drawLine(start, y, end4, y);
                    }
                }

                break;
            }

            default: {
                break;
            }
        }
    }

    @Override
    public void orientationChanged(Ruler ruler) {
        invalidateComponent();
    }

    /**
     * @return The interval at which the "major" (that is, the long)
     * markers are drawn.
     */
    public int getMajorDivision() {
        return majorDivision;
    }

    /**
     * Set the major division interval.
     *
     * @param major The number of markers interval at which to draw
     * a "major" (long) marker.  Can be zero to not draw any major
     * markers.
     */
    public void setMajorDivision(int major) {
        Utils.checkNonNegative(major, "majorDivision");

        // TODO: check for sanity of major vs. minor here??
        this.majorDivision = major;
        repaintComponent();
    }

    public void setMajorDivision(Number major) {
        Utils.checkNull(major, "majorDivision");

        setMajorDivision(major.intValue());
    }

    /**
     * @return The interval at which the "minor" (that is, the slightly
     * longer than normal) markers are drawn.
     */
    public int getMinorDivision() {
        return minorDivision;
    }

    /**
     * Set the minor division interval.
     *
     * @param minor The number of markers interval at which to draw
     * a "minor" (slightly longer than normal) marker.  Can be zero
     * to not draw any minor markers.
     */
    public void setMinorDivision(int minor) {
        Utils.checkNonNegative(minor, "minorDivision");

        // TODO: check for sanity of major vs. minor here??
        this.minorDivision = minor;
        repaintComponent();
    }

    public void setMinorDivision(Number minor) {
        Utils.checkNull(minor, "minorDivision");

        setMinorDivision(minor.intValue());
    }

    /**
     * @return The number of pixels interval at which to draw markers.
     */
    public int getMarkerSpacing() {
        return markerSpacing;
    }

    /**
     * Set the number of pixels interval at which to draw the markers.
     *
     * @param spacing The number of pixels between markers (must be &gt;= 1).
     */
    public void setMarkerSpacing(int spacing) {
        Utils.checkPositive(spacing, "markerSpacing");

        this.markerSpacing = spacing;
        invalidateComponent();
    }

    public void setMarkerSpacing(Number spacing) {
        Utils.checkNull(spacing, "markerSpacing");

        setMarkerSpacing(spacing.intValue());
    }

    /**
     * @return Whether the ruler is "flipped", that is the markers
     * start from the inside rather than the outside.
     */
    public boolean getFlip() {
        return flip;
    }

    public void setFlip(boolean flip) {
        this.flip = flip;
    }

    /**
     * @return The border configuration for this ruler.
     */
    public Borders getBorders() {
        return borders;
    }

    public void setBorders(Borders borders) {
        Utils.checkNull(borders, "borders");

        this.borders = borders;
        repaintComponent();
    }

    /**
     * @return The insets for the markers (on each edge).
     */
    public Insets getMarkerInsets() {
        return markerInsets;
    }

    public void setMarkerInsets(Insets insets) {
        Utils.checkNull(insets, "markerInsets");

        this.markerInsets = insets;
        repaintComponent();
    }

    /**
     * Returns the foreground color for the markers of the ruler.
     *
     * @return The foreground (marker) color.
     */
    public Color getColor() {
        return color;
    }

    /**
     * Sets the foreground color for the markers of the ruler.
     *
     * @param color The foreground (that is, the marker) color.
     */
    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    /**
     * Sets the foreground color for the markers of the ruler.
     *
     * @param color Any of the {@linkplain GraphicsUtilities#decodeColor color
     * values recognized by Pivot}.
     */
    public void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    /**
     * Returns the background color of the ruler.
     *
     * @return The current background color.
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color of the ruler.
     *
     * @param backgroundColor New background color value (can be {@code null}
     * for a transparent background).
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    /**
     * Sets the background color of the ruler.
     *
     * @param backgroundColor Any of the
     * {@linkplain GraphicsUtilities#decodeColor color values recognized by
     * Pivot}.
     */
    public void setBackgroundColor(String backgroundColor) {
        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor, "backgroundColor"));
    }

    public void setBackgroundColor(int backgroundColor) {
        Theme theme = currentTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }
}
