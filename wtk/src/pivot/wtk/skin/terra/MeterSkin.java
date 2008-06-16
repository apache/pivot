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
import pivot.wtk.skin.ComponentSkin;

public class MeterSkin extends ComponentSkin
    implements MeterListener {
    private static final int DEFAULT_WIDTH = 100;
    private static final int DEFAULT_HEIGHT = 12;

    // Style properties
    protected float gridFrequency = DEFAULT_GRID_FREQUENCY;
    protected Color gridColor = DEFAULT_GRID_COLOR;
    protected Color color = DEFAULT_COLOR;

    // Default style values
    private static final float DEFAULT_GRID_FREQUENCY = 0.25f;
    private static final Color DEFAULT_GRID_COLOR = new Color(0xD9, 0xD9, 0xD9);
    private static final Color DEFAULT_COLOR = new Color(0x6C, 0x9C, 0xCD);

    // Style keys
    protected static final String GRID_FREQUENCY_KEY = "gridFrequency";
    protected static final String GRID_COLOR_KEY = "gridColor";
    protected static final String COLOR_KEY = "color";

    @Override
    public void install(Component component) {
        validateComponentType(component, Meter.class);

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

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(GRID_FREQUENCY_KEY)) {
            value = gridFrequency;
        } else if (key.equals(GRID_COLOR_KEY)) {
            value = gridColor;
        } else if (key.equals(COLOR_KEY)) {
            value = color;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    public Object put(String key, Object value) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(GRID_FREQUENCY_KEY)) {
            if (value instanceof String) {
                value = Float.parseFloat((String)value);
            }

            validatePropertyType(key, value, Number.class, false);

            previousValue = gridFrequency;
            gridFrequency = ((Number)value).floatValue();

            repaintComponent();
        } else if (key.equals(GRID_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = gridColor;
            gridColor = (Color)value;

            repaintComponent();
        } else if (key.equals(COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = color;
            color = (Color)value;

            repaintComponent();
        } else {
            previousValue = super.put(key, value);
        }

        return previousValue;
    }

    @Override
    public Object remove(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object previousValue = null;

        if (key.equals(GRID_FREQUENCY_KEY)) {
            previousValue = put(key, DEFAULT_GRID_FREQUENCY);
        } else if (key.equals(GRID_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_GRID_COLOR);
        } else if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else {
            previousValue = super.remove(key);
        }

        return previousValue;
    }

    @Override
    public boolean containsKey(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        return (key.equals(GRID_FREQUENCY_KEY)
            || key.equals(GRID_COLOR_KEY)
            || key.equals(COLOR_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
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
