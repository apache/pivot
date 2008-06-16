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

import pivot.wtk.Button;
import pivot.wtk.Component;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Checkbox;
import pivot.wtk.Rectangle;
import pivot.wtk.skin.ButtonSkin;

/**
 * TODO Button alignment style (vertical only).
 *
 * @author gbrown
 */
public class CheckboxSkin extends ButtonSkin
    implements ButtonStateListener {
    private static final int CHECKBOX_SIZE = 14;
    private static final int CHECKMARK_SIZE = 10;

    // Style properties
    protected Font font = DEFAULT_FONT;
    protected Color color = DEFAULT_COLOR;
    protected Color buttonColor = DEFAULT_BUTTON_COLOR;
    protected Color buttonBorderColor = DEFAULT_BUTTON_BORDER_COLOR;
    protected Color buttonBevelColor = DEFAULT_BUTTON_BEVEL_COLOR;
    protected Color buttonSelectionColor = DEFAULT_BUTTON_SELECTION_COLOR;
    protected Color disabledColor = DEFAULT_DISABLED_COLOR;
    protected Color disabledButtonColor = DEFAULT_DISABLED_BUTTON_COLOR;
    protected Color disabledButtonBorderColor = DEFAULT_DISABLED_BUTTON_BORDER_COLOR;
    protected Color disabledButtonSelectionColor = DEFAULT_DISABLED_BUTTON_SELECTION_COLOR;
    protected int spacing = DEFAULT_SPACING;

    // Default style values
    private static final Font DEFAULT_FONT = new Font("Verdana", Font.PLAIN, 11);
    private static final Color DEFAULT_COLOR = Color.BLACK;
    private static final Color DEFAULT_BUTTON_COLOR = Color.WHITE;
    private static final Color DEFAULT_BUTTON_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_BUTTON_BEVEL_COLOR = new Color(0xF7, 0xF5, 0xEB);
    private static final Color DEFAULT_BUTTON_SELECTION_COLOR = new Color(0x2c, 0x56, 0x80);
    private static final Color DEFAULT_DISABLED_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_DISABLED_BUTTON_COLOR = new Color(0xcc, 0xca, 0xc2);
    private static final Color DEFAULT_DISABLED_BUTTON_BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);
    private static final Color DEFAULT_DISABLED_BUTTON_SELECTION_COLOR = new Color(0x99, 0x99, 0x99);
    private static final int DEFAULT_SPACING = 3;

    // Style keys
    protected static final String FONT_KEY = "font";
    protected static final String COLOR_KEY = "color";
    protected static final String BUTTON_COLOR_KEY = "buttonColor";
    protected static final String BUTTON_BORDER_COLOR_KEY = "buttonBorderColor";
    protected static final String BUTTON_BEVEL_COLOR_KEY = "buttonBevelColor";
    protected static final String BUTTON_SELECTION_COLOR_KEY = "buttonSelectionColor";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";
    protected static final String DISABLED_BUTTON_COLOR_KEY = "disabledButtonColor";
    protected static final String DISABLED_BUTTON_BORDER_COLOR_KEY = "disabledButtonBorderColor";
    protected static final String DISABLED_BUTTON_SELECTION_COLOR_KEY = "disabledButtonSelectionColor";
    protected static final String SPACING_KEY = "spacing";

    public CheckboxSkin() {
    }

    public void install(Component component) {
        validateComponentType(component, Checkbox.class);

        super.install(component);

        Checkbox checkbox = (Checkbox)component;
        checkbox.getButtonStateListeners().add(this);
    }

    public void uninstall() {
        Checkbox checkbox = (Checkbox)getComponent();
        checkbox.getButtonStateListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        Checkbox checkbox = (Checkbox)getComponent();
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();

        int preferredWidth = CHECKBOX_SIZE;

        Object buttonData = checkbox.getButtonData();
        if (buttonData != null) {
            dataRenderer.render(buttonData, checkbox, false);
            preferredWidth += dataRenderer.getPreferredWidth(height)
                + spacing * 2;
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        Checkbox checkbox = (Checkbox)getComponent();
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();

        int preferredHeight = CHECKBOX_SIZE;

        Object buttonData = checkbox.getButtonData();
        if (buttonData != null) {
            if (width != -1) {
                width = Math.max(width - (CHECKBOX_SIZE + spacing), 0);
            }

            dataRenderer.render(checkbox.getButtonData(), checkbox, false);

            preferredHeight = Math.max(preferredHeight,
                dataRenderer.getPreferredHeight(width));
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Checkbox checkbox = (Checkbox)getComponent();
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();

        dataRenderer.render(checkbox.getButtonData(), checkbox, false);

        int preferredWidth = CHECKBOX_SIZE;
        int preferredHeight = CHECKBOX_SIZE;

        Object buttonData = checkbox.getButtonData();
        if (buttonData != null) {
            dataRenderer.render(buttonData, checkbox, false);
            preferredWidth += dataRenderer.getPreferredWidth(-1)
                + spacing * 2;

            preferredHeight = Math.max(preferredHeight,
                dataRenderer.getPreferredHeight(-1));
        }

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D graphics) {
        Checkbox checkbox = (Checkbox)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Paint the button
        paintButton((Graphics2D)graphics.create(), checkbox, height);

        // Paint the content
        Button.DataRenderer dataRenderer = checkbox.getDataRenderer();
        dataRenderer.render(checkbox.getButtonData(), checkbox, false);
        dataRenderer.setSize(Math.max(width - (CHECKBOX_SIZE + spacing * 2), 0), height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(CHECKBOX_SIZE + spacing, 0);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (checkbox.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(buttonBorderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Rectangle(CHECKBOX_SIZE + 1, 0,
                dataRenderer.getWidth() + spacing * 2 - 2,
                dataRenderer.getHeight() - 1));
        }
    }

    private void paintButton(Graphics2D graphics, Checkbox checkbox, int height) {
        Color buttonBorderColor = null;
        Color buttonSelectionColor = null;
        if (checkbox.isEnabled()) {
            buttonBorderColor = this.buttonBorderColor;
            buttonSelectionColor = this.buttonSelectionColor;
        }
        else {
            buttonBorderColor = disabledButtonBorderColor;
            buttonSelectionColor = disabledButtonSelectionColor;
        }

        // Center the button vertically
        graphics.translate(0, (height - CHECKBOX_SIZE) / 2);

        // Paint the border
        Rectangle buttonRectangle = new Rectangle(0, 0,
            CHECKBOX_SIZE - 1, CHECKBOX_SIZE - 1);
        graphics.setPaint(buttonColor);
        graphics.fill(buttonRectangle);
        graphics.setPaint(buttonBorderColor);
        graphics.draw(buttonRectangle);

        // Paint the bevel
        Line2D bevelLine = new Line2D.Double(1, 1, CHECKBOX_SIZE - 2, 1);
        graphics.setPaint(buttonBevelColor);
        graphics.draw(bevelLine);

        // Paint the checkmark
        Button.State state = checkbox.getState();

        if (state == Button.State.SELECTED) {
            graphics.setColor(buttonSelectionColor);
            graphics.setStroke(new BasicStroke(2.5f));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw a checkmark
            int n = CHECKMARK_SIZE / 2;
            int m = CHECKMARK_SIZE / 4;
            int offsetX = (CHECKBOX_SIZE - (n + m)) / 2;
            int offsetY = (CHECKBOX_SIZE - n) / 2;

            graphics.drawLine(offsetX, (n - m) + offsetY,
                m + offsetX, n + offsetY);
            graphics.drawLine(m + offsetX, n + offsetY,
                (m + n) + offsetX, offsetY);

            /*
            // Draw an "X"
            int checkSize = (CHECKBOX_SIZE - (CHECKMARK_SIZE - 1)) / 2;
            graphics.draw(new Line2D.Double(checkSize, checkSize,
                checkSize + CHECKMARK_SIZE - 1, checkSize + CHECKMARK_SIZE - 1));
            graphics.draw(new Line2D.Double(checkSize, checkSize + CHECKMARK_SIZE - 1,
                checkSize + CHECKMARK_SIZE - 1, checkSize));
            */
        } else {
            if (state == Button.State.MIXED) {
                graphics.setColor(buttonSelectionColor);
                graphics.setStroke(new BasicStroke(3f));

                graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

                int x0 = 4;
                int x1 = CHECKBOX_SIZE - 5;
                int y = CHECKBOX_SIZE / 2;

                graphics.drawLine(x0, y, x1, y);
            }
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
        } else if (key.equals(BUTTON_COLOR_KEY)) {
            value = buttonColor;
        } else if (key.equals(BUTTON_BORDER_COLOR_KEY)) {
            value = buttonBorderColor;
        } else if (key.equals(BUTTON_BEVEL_COLOR_KEY)) {
            value = buttonBevelColor;
        } else if (key.equals(BUTTON_SELECTION_COLOR_KEY)) {
            value = buttonSelectionColor;
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            value = disabledColor;
        } else if (key.equals(DISABLED_BUTTON_COLOR_KEY)) {
            value = disabledButtonColor;
        } else if (key.equals(DISABLED_BUTTON_BORDER_COLOR_KEY)) {
            value = disabledButtonBorderColor;
        } else if (key.equals(DISABLED_BUTTON_SELECTION_COLOR_KEY)) {
            value = disabledButtonSelectionColor;
        } else if (key.equals(SPACING_KEY)) {
            value = spacing;
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
        } else if (key.equals(BUTTON_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = buttonColor;
            buttonColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BUTTON_BORDER_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = buttonBorderColor;
            buttonBorderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BUTTON_BEVEL_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = buttonBevelColor;
            buttonBevelColor = (Color)value;

            repaintComponent();
        } else if (key.equals(BUTTON_SELECTION_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = buttonSelectionColor;
            buttonSelectionColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledColor;
            disabledColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BUTTON_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledButtonColor;
            disabledButtonColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BUTTON_BORDER_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledButtonBorderColor;
            disabledButtonBorderColor = (Color)value;

            repaintComponent();
        } else if (key.equals(DISABLED_BUTTON_SELECTION_COLOR_KEY)) {
            if (value instanceof String) {
                value = Color.decode((String)value);
            }

            validatePropertyType(key, value, Color.class, false);

            previousValue = disabledButtonSelectionColor;
            disabledButtonSelectionColor = (Color)value;

            repaintComponent();
        } else if (key.equals(SPACING_KEY)) {
            if (value instanceof String) {
                value = Integer.parseInt((String)value);
            } else if (value instanceof Number) {
                value = ((Number)value).intValue();
            }

            validatePropertyType(key, value, Integer.class, false);

            previousValue = spacing;
            spacing = (Integer)value;

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

        if (key.equals(FONT_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
        } else if (key.equals(COLOR_KEY)) {
            previousValue = put(key, DEFAULT_COLOR);
        } else if (key.equals(BUTTON_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_COLOR);
        } else if (key.equals(BUTTON_BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_BORDER_COLOR);
        } else if (key.equals(BUTTON_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_BEVEL_COLOR);
        } else if (key.equals(BUTTON_SELECTION_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BUTTON_SELECTION_COLOR);
        } else if (key.equals(DISABLED_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_COLOR);
        } else if (key.equals(DISABLED_BUTTON_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BUTTON_COLOR);
        } else if (key.equals(DISABLED_BUTTON_BORDER_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BUTTON_BORDER_COLOR);
        } else if (key.equals(DISABLED_BUTTON_SELECTION_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BUTTON_SELECTION_COLOR);
        } else if (key.equals(SPACING_KEY)) {
            previousValue = put(key, DEFAULT_SPACING);
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
            || key.equals(BUTTON_COLOR_KEY)
            || key.equals(BUTTON_BORDER_COLOR_KEY)
            || key.equals(BUTTON_BEVEL_COLOR_KEY)
            || key.equals(BUTTON_SELECTION_COLOR_KEY)
            || key.equals(DISABLED_COLOR_KEY)
            || key.equals(DISABLED_BUTTON_COLOR_KEY)
            || key.equals(DISABLED_BUTTON_BORDER_COLOR_KEY)
            || key.equals(DISABLED_BUTTON_SELECTION_COLOR_KEY)
            || key.equals(SPACING_KEY)
            || super.containsKey(key));
    }

    public boolean isEmpty() {
        return false;
    }

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        repaintComponent();
    }

    @Override
    public void mouseClick(Mouse.Button button, int x, int y, int count) {
        Checkbox checkbox = (Checkbox)getComponent();

        Component.setFocusedComponent(checkbox);
        checkbox.press();
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        Checkbox checkbox = (Checkbox)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            checkbox.press();
        } else {
            consumed = super.keyReleased(keyCode, keyLocation);
        }

        return consumed;
    }

    public void stateChanged(Button button, Button.State previousState) {
        repaintComponent();
    }
}
