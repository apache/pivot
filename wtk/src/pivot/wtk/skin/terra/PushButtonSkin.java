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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;

import pivot.collections.Map;
import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.PushButton;
import pivot.wtk.Rectangle;
import pivot.wtk.skin.ButtonSkin;

/**
 * TODO Add a "flat" or "toolbar" boolean style that, when set, only paints
 * the button when rolled over or pressed. Otherwise, only the content is
 * painted.
 *
 * @author gbrown
 */
public class PushButtonSkin extends ButtonSkin
    implements ButtonStateListener {
    private boolean pressed = false;

    // Style properties
    protected Font font = DEFAULT_FONT;
    protected Color color = DEFAULT_COLOR;
    protected Color disabledColor = DEFAULT_DISABLED_COLOR;
    protected Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
    protected Color disabledBackgroundColor = DEFAULT_DISABLED_BACKGROUND_COLOR;
    protected Color borderColor = DEFAULT_BORDER_COLOR;
    protected Color disabledBorderColor = DEFAULT_DISABLED_BORDER_COLOR;
    protected Color bevelColor = DEFAULT_BEVEL_COLOR;
    protected Color pressedBevelColor = DEFAULT_PRESSED_BEVEL_COLOR;
    protected Color disabledBevelColor = DEFAULT_DISABLED_BEVEL_COLOR;
    protected Insets padding = DEFAULT_PADDING;

    // Default style values
    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_DISABLED_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(0xE6, 0xE3, 0xDA);
    private static final Color DEFAULT_DISABLED_BACKGROUND_COLOR = new Color(0xF7, 0xF5, 0xEB);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_DISABLED_BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);
    private static final Color DEFAULT_BEVEL_COLOR = new Color(0xF7, 0xF5, 0xEB);
    private static final Color DEFAULT_PRESSED_BEVEL_COLOR = new Color(0xCC, 0xCA, 0xC2);
    private static final Color DEFAULT_DISABLED_BEVEL_COLOR = Color.WHITE;
    protected static final Insets DEFAULT_PADDING = new Insets(3);

    // Style keys
    protected static final String FONT_KEY = "font";
    protected static final String COLOR_KEY = "color";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";
    protected static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    protected static final String DISABLED_BACKGROUND_COLOR_KEY = "disabledBackgroundColor";
    protected static final String BORDER_COLOR_KEY = "borderColor";
    protected static final String DISABLED_BORDER_COLOR_KEY = "disabledBorderColor";
    protected static final String BEVEL_COLOR_KEY = "bevelColor";
    protected static final String PRESSED_BEVEL_COLOR_KEY = "pressedBevelColor";
    protected static final String DISABLED_BEVEL_COLOR_KEY = "disabledBevelColor";
    protected static final String PADDING_KEY = "padding";

    public PushButtonSkin() {
    }

    public void install(Component component) {
        validateComponentType(component, PushButton.class);

        super.install(component);

        PushButton pushButton = (PushButton)component;
        pushButton.getButtonStateListeners().add(this);
    }

    public void uninstall() {
        PushButton pushButton = (PushButton)getComponent();
        pushButton.getButtonStateListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        // Include padding in constraint
        if (height != -1) {
            height = Math.max(height - (padding.top + padding.bottom + 2), 0);
        }

        int preferredWidth = dataRenderer.getPreferredWidth(height)
            + padding.left + padding.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        // Include padding in constraint
        if (width != -1) {
            width = Math.max(width - (padding.left + padding.right + 2), 0);
        }

        int preferredHeight = dataRenderer.getPreferredHeight(width)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        PushButton pushButton = (PushButton)getComponent();
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();

        dataRenderer.render(pushButton.getButtonData(), pushButton, false);

        Dimensions preferredContentSize = dataRenderer.getPreferredSize();

        int preferredWidth = preferredContentSize.width
            + padding.left + padding.right + 2;

        int preferredHeight = preferredContentSize.height
            + padding.top + padding.bottom + 2;

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D graphics) {
        PushButton pushButton = (PushButton)getComponent();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (pushButton.isEnabled()) {
            backgroundColor = this.backgroundColor;
            bevelColor = (pressed
                || pushButton.isSelected()) ? pressedBevelColor : this.bevelColor;
            borderColor = this.borderColor;
        }
        else {
            backgroundColor = disabledBackgroundColor;
            bevelColor = disabledBevelColor;
            borderColor = disabledBorderColor;
        }

        // Paint the background
        graphics.setPaint(backgroundColor);
        Rectangle bounds = new Rectangle(0, 0, getWidth(), getHeight());
        graphics.fill(bounds);

        // Draw all lines with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Paint the border
        Rectangle borderRectangle = new Rectangle(0, 0,
            bounds.width - 1, bounds.height - 1);
        graphics.setPaint(borderColor);
        graphics.draw(borderRectangle);

        // Paint the bevel
        Line2D bevelLine = new Line2D.Double(1, 1, bounds.width - 2, 1);
        graphics.setPaint(bevelColor);
        graphics.draw(bevelLine);

        // Paint the content
        Button.DataRenderer dataRenderer = pushButton.getDataRenderer();
        dataRenderer.render(pushButton.getButtonData(), pushButton, false);
        dataRenderer.setSize(Math.max(bounds.width - (padding.left + padding.right + 2), 0),
            Math.max(getHeight() - (padding.top + padding.bottom + 2), 0));

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (pushButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Rectangle(2, 2, Math.max(bounds.width - 5, 0),
                Math.max(bounds.height - 5, 0)));
        }
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
        } else if (key.equals(BACKGROUND_COLOR_KEY)) {
            value = backgroundColor;
        } else if (key.equals(DISABLED_BACKGROUND_COLOR_KEY)) {
            value = disabledBackgroundColor;
        } else if (key.equals(BORDER_COLOR_KEY)) {
            value = borderColor;
        } else if (key.equals(DISABLED_BORDER_COLOR_KEY)) {
            value = disabledBorderColor;
        } else if (key.equals(BEVEL_COLOR_KEY)) {
            value = bevelColor;
        } else if (key.equals(PRESSED_BEVEL_COLOR_KEY)) {
            value = pressedBevelColor;
        } else if (key.equals(DISABLED_BEVEL_COLOR_KEY)) {
            value = disabledBevelColor;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else {
            value = super.get(key);
        }

        return value;
    }

    @Override
    @SuppressWarnings("unchecked")
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
        } else if (key.equals(BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = backgroundColor;
            backgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BACKGROUND_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledBackgroundColor;
            disabledBackgroundColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BORDER_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = borderColor;
            borderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BORDER_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledBorderColor;
            disabledBorderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BEVEL_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = bevelColor;
            bevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(PRESSED_BEVEL_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = pressedBevelColor;
            pressedBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BEVEL_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledBevelColor;
            disabledBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(PADDING_KEY)) {
            if (value instanceof Number) {
                value = new Insets(((Number)value).intValue());
            } else {
                if (value instanceof Map<?, ?>) {
                    value = new Insets((Map<String, Object>)value);
                }
            }

            validatePropertyType(key, value, Insets.class, false);

            previousValue = padding;
            padding = (Insets)value;

            invalidateComponent();
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

        if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else if (key.equals(FONT_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_COLOR);
        } else if (key.equals(BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BACKGROUND_COLOR);
        } else if (key.equals(DISABLED_BACKGROUND_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BACKGROUND_COLOR);
        } else if (key.equals(BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BORDER_COLOR);
        } else if (key.equals(DISABLED_BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BORDER_COLOR);
        } else if (key.equals(BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BEVEL_COLOR);
        } else if (key.equals(PRESSED_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_PRESSED_BEVEL_COLOR);
        } else if (key.equals(DISABLED_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BEVEL_COLOR);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
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
            || key.equals(BACKGROUND_COLOR_KEY)
            || key.equals(DISABLED_BACKGROUND_COLOR_KEY)
            || key.equals(BORDER_COLOR_KEY)
            || key.equals(DISABLED_BORDER_COLOR_KEY)
            || key.equals(BEVEL_COLOR_KEY)
            || key.equals(PRESSED_BEVEL_COLOR_KEY)
            || key.equals(DISABLED_BEVEL_COLOR_KEY)
            || key.equals(PADDING_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        pressed = false;
        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        pressed = false;
        repaintComponent();
    }

    @Override
    public void mouseOut() {
        super.mouseOut();

        if (pressed) {
            pressed = false;
            repaintComponent();
        }
    }

    @Override
    public boolean mouseDown(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(button, x, y);

        pressed = true;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseUp(Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        PushButton pushButton = (PushButton)getComponent();

        if (pushButton.isFocusable()) {
            Component.setFocusedComponent(pushButton);
        }

        pushButton.press();
    }

    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
        } else {
            consumed = super.keyPressed(keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        PushButton pushButton = (PushButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            pushButton.press();
        } else {
            consumed = super.keyReleased(keyCode, keyLocation);
        }

        return consumed;
    }

    public void stateChanged(Button toggleButton, Button.State previousState) {
        repaintComponent();
    }
}
