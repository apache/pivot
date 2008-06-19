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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.LinkButton;
import pivot.wtk.Mouse;
import pivot.wtk.skin.ButtonSkin;

public class LinkButtonSkin extends ButtonSkin {
    boolean highlighted = false;

    // Style properties
    protected Font font = DEFAULT_FONT;
    protected Color color = DEFAULT_COLOR;
    protected Color disabledColor = DEFAULT_DISABLED_COLOR;

    // Default style values
    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_COLOR = new Color(0x2c, 0x56, 0x80);
    private static final Color DEFAULT_DISABLED_COLOR = new Color(0x99, 0x99, 0x99);

    // Style keys
    protected static final String FONT_KEY = "font";
    protected static final String COLOR_KEY = "color";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";

    public void install(Component component) {
        validateComponentType(component, LinkButton.class);

        super.install(component);
    }

    public int getPreferredWidth(int height) {
        LinkButton linkButton = (LinkButton)getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getPreferredWidth(height);
    }

    public int getPreferredHeight(int width) {
        LinkButton linkButton = (LinkButton)getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getPreferredHeight(width);
    }

    public Dimensions getPreferredSize() {
        LinkButton linkButton = (LinkButton)getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getPreferredSize();
    }

    public void paint(Graphics2D graphics) {
        LinkButton linkButton = (LinkButton)getComponent();
        int width = getWidth();
        int height = getHeight();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, highlighted);
        dataRenderer.setSize(width, height);

        dataRenderer.paint(graphics);
    }

    /**
     * @return
     * <tt>false</tt>; link buttons are not focusable.
     */
    @Override
    public boolean isFocusable() {
        return false;
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("key is null.");
        }

        Object value = null;

        if (key.equals(FONT_KEY)) {
            value = font;
        } else if (key.equals(COLOR_KEY)) {
            value = color;
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            value = disabledColor;
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

        if (key.equals(FONT_KEY)) {
            if (value instanceof String) {
                value = Font.decode((String)value);
            }

            validatePropertyType(key, value, Font.class, false);

            previousValue = font;
            font = (Font)value;

            invalidateComponent();
        } else if (key.equals(COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = color;
            color = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledColor;
            disabledColor = (Color)value;

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

        if (key.equals(FONT_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
        } else if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_COLOR);
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

        return (key.equals(FONT_KEY)
            || key.equals(COLOR_KEY)
            || key.equals(DISABLED_COLOR_KEY)
            || super.containsKey(key));
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        highlighted = false;
        repaintComponent();
    }

    @Override
    public void mouseOver() {
        super.mouseOver();

        highlighted = true;

        repaintComponent();
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        highlighted = false;

        repaintComponent();
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        LinkButton linkButton = (LinkButton)getComponent();
        linkButton.press();
    }
}
