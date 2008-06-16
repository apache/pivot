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
import java.awt.geom.Ellipse2D;

import pivot.wtk.Component;
import pivot.wtk.Button;
import pivot.wtk.ButtonStateListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.RadioButton;
import pivot.wtk.Rectangle;
import pivot.wtk.skin.ButtonSkin;

/**
 * TODO Button alignment style (vertical only).
 *
 * @author gbrown
 */
public class RadioButtonSkin extends ButtonSkin
    implements ButtonStateListener {
    private static final int BUTTON_DIAMETER = 14;
    private static final int BUTTON_SELECTION_DIAMETER = 6;

    // Style properties
    protected Font font = DEFAULT_FONT;
    protected Color color = DEFAULT_COLOR;
    protected Color buttonColor = DEFAULT_BUTTON_COLOR;
    protected Color buttonBorderColor = DEFAULT_BUTTON_BORDER_COLOR;
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
    protected static final String BUTTON_SELECTION_COLOR_KEY = "buttonSelectionColor";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";
    protected static final String DISABLED_BUTTON_COLOR_KEY = "disabledButtonColor";
    protected static final String DISABLED_BUTTON_BORDER_COLOR_KEY = "disabledButtonBorderColor";
    protected static final String DISABLED_BUTTON_SELECTION_COLOR_KEY = "disabledButtonSelectionColor";
    protected static final String SPACING_KEY = "spacing";

    public RadioButtonSkin() {
    }

    public void install(Component component) {
        validateComponentType(component, RadioButton.class);

        super.install(component);

        RadioButton radioButton = (RadioButton)component;
        radioButton.getButtonStateListeners().add(this);
    }

    public void uninstall() {
        RadioButton radioButton = (RadioButton)getComponent();
        radioButton.getButtonStateListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        int preferredWidth = BUTTON_DIAMETER
            + dataRenderer.getPreferredWidth(height)
            + spacing * 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        if (width != -1) {
            width = Math.max(width - (BUTTON_DIAMETER + spacing), 0);
        }

        int preferredHeight = Math.max(BUTTON_DIAMETER,
            dataRenderer.getPreferredHeight(width));

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        RadioButton radioButton = (RadioButton)getComponent();
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();

        dataRenderer.render(radioButton.getButtonData(), radioButton, false);

        int preferredWidth = BUTTON_DIAMETER
            + dataRenderer.getPreferredWidth(-1)
            + spacing * 2;

        int preferredHeight = Math.max(BUTTON_DIAMETER,
            dataRenderer.getPreferredHeight(-1));

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void paint(Graphics2D graphics) {
        RadioButton radioButton = (RadioButton)getComponent();
        int width = getWidth();
        int height = getHeight();

        // Paint the button
        paintButton((Graphics2D)graphics.create(), radioButton, height);

        // Paint the content
        Button.DataRenderer dataRenderer = radioButton.getDataRenderer();
        dataRenderer.render(radioButton.getButtonData(), radioButton, false);
        dataRenderer.setSize(Math.max(width - (BUTTON_DIAMETER + spacing * 2), 0), height);

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(BUTTON_DIAMETER + spacing, 0);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (radioButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(buttonBorderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Rectangle(BUTTON_DIAMETER + 1, 0,
                dataRenderer.getWidth() + spacing * 2 - 2,
                dataRenderer.getHeight() - 1));
        }
    }

    private void paintButton(Graphics2D graphics, RadioButton radioButton, int height) {
        // Paint the button
        Color buttonBorderColor = null;
        Color buttonSelectionColor = null;
        if (radioButton.isEnabled()) {
            buttonBorderColor = this.buttonBorderColor;
            buttonSelectionColor = this.buttonSelectionColor;
        }
        else {
            buttonBorderColor = disabledButtonBorderColor;
            buttonSelectionColor = disabledButtonSelectionColor;
        }

        // Center the button vertically
        graphics.translate(0, (height - BUTTON_DIAMETER) / 2);

        // Paint the border
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Ellipse2D buttonCircle = new Ellipse2D.Double(0, 0,
            BUTTON_DIAMETER - 1, BUTTON_DIAMETER - 1);
        graphics.setColor(buttonBorderColor);
        graphics.fill(buttonCircle);

        Ellipse2D innerButtonCircle = new Ellipse2D.Double(1, 1,
            BUTTON_DIAMETER - 3, BUTTON_DIAMETER - 3);
        graphics.setColor(buttonBorderColor);
        graphics.fill(buttonCircle);

        graphics.setColor(buttonColor);
        graphics.fill(innerButtonCircle);

        if (radioButton.isSelected()) {
            Ellipse2D buttonSelectionCircle = new Ellipse2D.Double((BUTTON_DIAMETER
                - (BUTTON_SELECTION_DIAMETER - 1)) / 2,
                (BUTTON_DIAMETER - (BUTTON_SELECTION_DIAMETER - 1)) / 2,
                BUTTON_SELECTION_DIAMETER - 1, BUTTON_SELECTION_DIAMETER - 1);
            graphics.setColor(buttonSelectionColor);
            graphics.fill(buttonSelectionCircle);
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
        RadioButton radioButton = (RadioButton)getComponent();

        Component.setFocusedComponent(radioButton);
        radioButton.press();
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        RadioButton radioButton = (RadioButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            radioButton.press();
        } else {
            consumed = super.keyReleased(keyCode, keyLocation);
        }

        return consumed;
    }

    public void stateChanged(Button button, Button.State previousState) {
        repaintComponent();
    }
}
