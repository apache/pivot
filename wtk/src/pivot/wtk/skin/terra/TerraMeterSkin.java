/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.skin.terra;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Meter;
import pivot.wtk.MeterListener;
import pivot.wtk.Theme;
import pivot.wtk.skin.ComponentSkin;

/**
 * Meter skin.
 *
 * @author tvolkert
 */
public class TerraMeterSkin extends ComponentSkin
    implements MeterListener {
    private Color color;
    private Color gridColor;
    private float gridFrequency;

    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 12;

    public TerraMeterSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        color = theme.getColor(5);
        gridColor = theme.getColor(3);
        gridFrequency = 0.25f;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Meter meter = (Meter)component;
        meter.getMeterListeners().add(this);
    }

    @Override
    public void uninstall() {
        Meter meter = (Meter)getComponent();
        meter.getMeterListeners().remove(this);

        super.uninstall();
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    public int getPreferredWidth(int height) {
        // Meter has no content, so its preferred width is hard coded in the
        // class and is not affected by the height constraint.
        return DEFAULT_WIDTH;
    }

    public int getPreferredHeight(int width) {
        // Meter has no content, so its preferred height is hard coded in the
        // class and is not affected by the width constraint.
        return DEFAULT_HEIGHT;
    }

    public Dimensions getPreferredSize() {
        // Meter has no content, so its preferred size is hard coded in the class.
        return new Dimensions(DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        Meter meter = (Meter)getComponent();

        // TODO Paint text

        double width = (double)getWidth();
        double height = (double)getHeight();
        double meterStop = meter.getPercentage() * width;

        graphics.setStroke(new BasicStroke());
        graphics.setPaint(color);
        graphics.fill(new Rectangle2D.Double(0, 0, meterStop - 1.0, height - 1.0));

        graphics.setPaint(gridColor);
        graphics.draw(new Rectangle2D.Double(0, 0, width - 1.0, height - 1.0));

        int nLines = (int)Math.ceil(1.0 / gridFrequency) - 1;
        double gridSeparation = width * gridFrequency;
        for (int i = 0; i < nLines; i++) {
           double gridX = (double)(i + 1) * gridSeparation;
            graphics.draw(new Line2D.Double(gridX, 0.0, gridX, height - 1.0));
        }
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(decodeColor(color));
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

        setGridColor(decodeColor(gridColor));
    }

    public float getGridFrequency() {
        return gridFrequency;
    }

    public void setGridFrequency(float gridFrequency) {
        this.gridFrequency = gridFrequency;
        repaintComponent();
    }

    public final void setGridFrequency(Number gridFrequency) {
        if (gridFrequency == null) {
            throw new IllegalArgumentException("gridFrequency is null.");
        }

        setGridFrequency(gridFrequency.floatValue());
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
    public void textChanged(Meter meter, String previousText) {
        invalidateComponent();
    }
}
