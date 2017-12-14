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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Ruler;
import org.apache.pivot.wtk.RulerListener;
import org.apache.pivot.wtk.Theme;

public class RulerSkin extends ComponentSkin implements RulerListener {
    private Color color;
    private Color backgroundColor;
    private int markerSpacing;

    public RulerSkin() {
        // For now the default colors are not from the Theme.
        setColor(Color.BLACK);
        setBackgroundColor(CSSColor.LightYellow.getColor());

        markerSpacing = 5;
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

        Ruler ruler = (Ruler) getComponent();

        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(color);
        graphics.drawRect(0, 0, width - 1, height - 1);

        Orientation orientation = ruler.getOrientation();
        switch (orientation) {
            case HORIZONTAL: {
                for (int i = 0, n = width / markerSpacing + 1; i < n; i++) {
                    int x = i * markerSpacing;

                    if (i % 4 == 0) {
                        graphics.drawLine(x, 0, x, height / 2);
                    } else if (i % 2 == 0) {
                        graphics.drawLine(x, 0, x, height / 3);
                    } else {
                        graphics.drawLine(x, 0, x, height / 4);
                    }
                }

                break;
            }

            case VERTICAL: {
                for (int i = 0, n = height / markerSpacing + 1; i < n; i++) {
                    int y = i * markerSpacing;

                    if (i % 4 == 0) {
                        graphics.drawLine(0, y, width / 2, y);
                    } else if (i % 2 == 0) {
                        graphics.drawLine(0, y, width / 3, y);
                    } else {
                        graphics.drawLine(0, y, width / 4, y);
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

    public int getMarkerSpacing() {
        return markerSpacing;
    }

    public void setMarkerSpacing(int spacing) {
        Utils.checkNonNegative(spacing, "markerSpacing");

        this.markerSpacing = spacing;
        invalidateComponent();
    }

    public void setMarkerSpacing(Number spacing) {
        Utils.checkNull(spacing, "markerSpacing");

        setMarkerSpacing(spacing.intValue());
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
