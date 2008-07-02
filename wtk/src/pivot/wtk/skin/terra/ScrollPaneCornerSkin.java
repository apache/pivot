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
import pivot.wtk.ScrollPane;
import pivot.wtk.skin.ComponentSkin;

public class ScrollPaneCornerSkin extends ComponentSkin {
    // Style properties
    protected Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    protected Color color = DEFAULT_COLOR;

    // Default style values
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(0xF0, 0xEC, 0xE7);
    private static final Color DEFAULT_COLOR = new Color(0x81, 0x76, 0x67);

    // Style keys
    protected static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    protected static final String COLOR_KEY = "color";

    @Override
    public void install(Component component) {
        validateComponentType(component, ScrollPane.Corner.class);

        super.install(component);
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    public int getPreferredWidth(int height) {
        // ScrollPane corners have no implicit preferred size.
        return 0;
    }

    public int getPreferredHeight(int width) {
        // ScrollPane corners have no implicit preferred size.
        return 0;
    }

    public Dimensions getPreferredSize() {
        // ScrollPane corners have no implicit preferred size.
        return new Dimensions(0, 0);
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        ScrollPane.Corner corner = (ScrollPane.Corner)getComponent();

        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(backgroundColor);
        graphics.fill(new Rectangle2D.Double(0, 0, width, height));
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            value = backgroundColor;
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

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = backgroundColor;
            backgroundColor = (Color)value;

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

        if (key.equals(BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BACKGROUND_COLOR);
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

        return (key.equals(BACKGROUND_COLOR_KEY)
            || key.equals(COLOR_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }
}
