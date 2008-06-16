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
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;

import pivot.collections.List;
import pivot.collections.Map;
import pivot.wtk.Border;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.ListButton;
import pivot.wtk.ListButtonListener;
import pivot.wtk.ListButtonSelectionListener;
import pivot.wtk.ListView;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;
import pivot.wtk.Rectangle;
import pivot.wtk.skin.ButtonSkin;

public class ListButtonSkin extends ButtonSkin
    implements ListButton.Skin, ButtonPressListener,
        ListButtonListener, ListButtonSelectionListener {
    private class ListViewPopupKeyHandler implements ComponentKeyListener {
        public void keyTyped(Component component, char character) {
            // No-op
        }

        public void keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            switch (keyCode) {
                case Keyboard.KeyCode.ESCAPE: {
                    listViewPopup.close();
                    Component.setFocusedComponent(getComponent());
                    break;
                }

                case Keyboard.KeyCode.ENTER: {
                    ListButton listButton = (ListButton)getComponent();

                    int index = listView.getSelectedIndex();
                    Object data = listView.getListData().get(index);

                    listView.clearSelection();
                    listButton.setSelectedIndex(index);
                    listButton.setButtonData(data);

                    listViewPopup.close();
                    Component.setFocusedComponent(getComponent());
                    break;
                }
            }
        }

        public void keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            // No-op
        }
    }

    private class ListViewPopupMouseListener implements ComponentMouseButtonListener {
        public void mouseDown(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseUp(Component component, Mouse.Button button, int x, int y) {
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            ListButton listButton = (ListButton)getComponent();

            int index = listView.getSelectedIndex();
            Object data = listView.getListData().get(index);

            listView.clearSelection();
            listButton.setSelectedIndex(index);
            listButton.setButtonData(data);

            listViewPopup.close();
            Component.setFocusedComponent(getComponent());
        }
    }

    private ListView listView = null;
    private Border listViewBorder = null;
    private Popup listViewPopup = null;

    private boolean pressed = false;

    private static final int TRIGGER_WIDTH = 14;

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
    private static final Color DEFAULT_COLOR = new Color(0x00, 0x00, 0x00);
    private static final Color DEFAULT_DISABLED_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_BACKGROUND_COLOR = new Color(0xE6, 0xE3, 0xDA);
    private static final Color DEFAULT_DISABLED_BACKGROUND_COLOR = new Color(0xF7, 0xF5, 0xEB);
    private static final Color DEFAULT_BORDER_COLOR = new Color(0x99, 0x99, 0x99);
    private static final Color DEFAULT_DISABLED_BORDER_COLOR = new Color(0xCC, 0xCC, 0xCC);
    private static final Color DEFAULT_BEVEL_COLOR = new Color(0xF7, 0xF5, 0xEB);
    private static final Color DEFAULT_PRESSED_BEVEL_COLOR = new Color(0xCC, 0xCA, 0xC2);
    private static final Color DEFAULT_DISABLED_BEVEL_COLOR = new Color(0xFF, 0xFF, 0xFF);
    private static final Insets DEFAULT_PADDING = new Insets(2);

    // Style keys
    protected static final String FONT_KEY = "font";
    protected static final String COLOR_KEY = "color";
    protected static final String DISABLED_COLOR_KEY = "disabledColor";
    protected static final String BACKGROUND_COLOR_KEY = "backgroundColor";
    protected static final String DISABLED_BACKGROUND_COLOR_KEY = "disabledBackgroundColor";
    protected static final String BORDER_COLOR_KEY = "borderColor";
    protected static final String BEVEL_COLOR_KEY = "bevelColor";
    protected static final String PRESSED_BEVEL_COLOR_KEY = "pressedBevelColor";
    protected static final String DISABLED_BEVEL_COLOR_KEY = "disabledBevelColor";
    protected static final String PADDING_KEY = "padding";

    protected static final String LIST_FONT_KEY = "listFont";
    protected static final String LIST_COLOR_KEY = "listColor";
    protected static final String LIST_DISABLED_COLOR_KEY = "listDisabledColor";
    protected static final String LIST_BACKGROUND_COLOR_KEY = "listBackgroundColor";
    protected static final String LIST_SELECTION_COLOR_KEY = "listSelectionColor";
    protected static final String LIST_SELECTION_BACKGROUND_COLOR_KEY = "listSelectionBackgroundColor";
    protected static final String LIST_INACTIVE_SELECTION_COLOR_KEY = "listInactiveSelectionColor";
    protected static final String LIST_INACTIVE_SELECTION_BACKGROUND_COLOR_KEY = "listInactiveSelectionBackgroundColor";
    protected static final String LIST_HIGHLIGHT_COLOR_KEY = "listHighlightColor";
    protected static final String LIST_HIGHLIGHT_BACKGROUND_COLOR_KEY = "listHighlightBackgroundColor";

    public ListButtonSkin() {
        // Create the list view and border
        // TODO Add the list view to a Panorama
        listView = new ListView();
        listViewBorder = new Border(listView);
        listViewBorder.getStyles().put("padding", new Insets(0));

        // Create the popup
        listViewPopup = new Popup(listViewBorder);
        listViewPopup.getComponentKeyListeners().add(new ListViewPopupKeyHandler());
        listViewPopup.getComponentMouseButtonListeners().add(new ListViewPopupMouseListener());
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, ListButton.class);

        super.install(component);

        ListButton listButton = (ListButton)component;

        listButton.getButtonPressListeners().add(this);
        listButton.getListButtonSelectionListeners().add(this);

        listView.setListData(listButton.getListData());
        listViewBorder.getStyles().put("borderColor", listButton.getStyles().get("borderColor"));
    }

    @Override
    public void uninstall() {
        ListButton listButton = (ListButton)getComponent();

        listViewPopup.close();

        listButton.getButtonPressListeners().remove(this);
        listButton.getListButtonSelectionListeners().remove(this);

        super.uninstall();
    }

    @SuppressWarnings("unchecked")
    public int getPreferredWidth(int height) {
        ListButton listButton = (ListButton)getComponent();
        List<Object> listData = (List<Object>)listButton.getListData();

        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        // Include padding in constraint
        if (height != -1) {
            height = Math.max(height - (padding.top + padding.bottom + 2), 0);
        }

        // Determine the preferred width of the current button data
        dataRenderer.render(listButton.getButtonData(),
            listButton, false);
        int preferredWidth = dataRenderer.getPreferredWidth(-1);

        // The preferred width of the button is the max. width of the rendered
        // content plus padding and the trigger width
        for (Object item : listData) {
            dataRenderer.render(item, listButton, false);
            preferredWidth = Math.max(preferredWidth, dataRenderer.getPreferredWidth(-1));
        }

        preferredWidth += TRIGGER_WIDTH + padding.left + padding.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        ListButton listButton = (ListButton)getComponent();
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();

        dataRenderer.render(listButton.getButtonData(), listButton, false);

        int preferredHeight = dataRenderer.getPreferredHeight(-1)
            + padding.top + padding.bottom + 2;

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        // TODO Optimize by performing calcuations locally
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        ListButton listButton = (ListButton)getComponent();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (listButton.isEnabled()) {
            backgroundColor = this.backgroundColor;
            bevelColor = (pressed) ? pressedBevelColor : this.bevelColor;
            borderColor = this.borderColor;
        } else {
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

        // Paint the bevel
        Line2D bevelLine = new Line2D.Double(1, 1, bounds.width - 2, 1);
        graphics.setPaint(bevelColor);
        graphics.draw(bevelLine);

        // Paint the border
        graphics.setPaint(borderColor);

        Rectangle contentBounds = new Rectangle(0, 0,
            Math.max(bounds.width - TRIGGER_WIDTH - 1, 0), Math.max(bounds.height - 1, 0));
        graphics.draw(contentBounds);

        Rectangle triggerBounds = new Rectangle(Math.max(bounds.width - TRIGGER_WIDTH - 1, 0), 0,
            TRIGGER_WIDTH, Math.max(bounds.height - 1, 0));
        graphics.draw(triggerBounds);

        // Paint the content
        Button.DataRenderer dataRenderer = listButton.getDataRenderer();
        dataRenderer.render(listButton.getButtonData(), listButton, false);
        dataRenderer.setSize(Math.max(contentBounds.width - (padding.left + padding.right + 2) + 1, 0),
            Math.max(contentBounds.height - (padding.top + padding.bottom + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (listButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.draw(new Rectangle(2, 2, Math.max(contentBounds.width - 4, 0),
                Math.max(contentBounds.height - 4, 0)));

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_OFF);
        }

        // Paint the trigger
        GeneralPath triggerIconShape = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        triggerIconShape.moveTo(0, 0);
        triggerIconShape.lineTo(3, 3);
        triggerIconShape.lineTo(6, 0);
        triggerIconShape.closePath();

        Graphics2D triggerGraphics = (Graphics2D)graphics.create();
        triggerGraphics.setStroke(new BasicStroke(0));
        triggerGraphics.setPaint(color);

        int tx = triggerBounds.x + (int)Math.round((triggerBounds.width
            - triggerIconShape.getBounds().width) / 2f);
        int ty = triggerBounds.y + (int)Math.round((triggerBounds.height
            - triggerIconShape.getBounds().height) / 2f);
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);
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
        } else if (key.equals(BEVEL_COLOR_KEY)) {
            value = bevelColor;
        } else if (key.equals(PRESSED_BEVEL_COLOR_KEY)) {
            value = pressedBevelColor;
        } else if (key.equals(DISABLED_BEVEL_COLOR_KEY)) {
            value = disabledBevelColor;
        } else if (key.equals(PADDING_KEY)) {
            value = padding;
        } else if (key.equals(LIST_FONT_KEY)) {
            value = listView.getStyles().get(ListViewSkin.FONT_KEY);
        } else if (key.equals(LIST_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.COLOR_KEY);
        } else if (key.equals(LIST_DISABLED_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.DISABLED_COLOR_KEY);
        } else if (key.equals(LIST_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.BACKGROUND_COLOR_KEY);
        } else if (key.equals(LIST_SELECTION_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.SELECTION_COLOR_KEY);
        } else if (key.equals(LIST_SELECTION_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.SELECTION_BACKGROUND_COLOR_KEY);
        } else if (key.equals(LIST_INACTIVE_SELECTION_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.INACTIVE_SELECTION_COLOR_KEY);
        } else if (key.equals(LIST_INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.INACTIVE_SELECTION_BACKGROUND_COLOR_KEY);
        } else if (key.equals(LIST_HIGHLIGHT_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.HIGHLIGHT_COLOR_KEY);
        } else if (key.equals(LIST_HIGHLIGHT_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().get(ListViewSkin.HIGHLIGHT_BACKGROUND_COLOR_KEY);
        } else {
            value = super.get(key);
        }

        return value;
    }

    public ListView getListView() {
        return listView;
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

            listViewBorder.getStyles().put("borderColor", borderColor);

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
        } else if (key.equals(LIST_FONT_KEY)) {
            value = listView.getStyles().put(ListViewSkin.FONT_KEY, value);
        } else if (key.equals(LIST_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.COLOR_KEY, value);
        } else if (key.equals(LIST_DISABLED_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.DISABLED_COLOR_KEY, value);
        } else if (key.equals(LIST_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.BACKGROUND_COLOR_KEY, value);
        } else if (key.equals(LIST_SELECTION_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.SELECTION_COLOR_KEY, value);
        } else if (key.equals(LIST_SELECTION_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.SELECTION_BACKGROUND_COLOR_KEY, value);
        } else if (key.equals(LIST_INACTIVE_SELECTION_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.INACTIVE_SELECTION_COLOR_KEY, value);
        } else if (key.equals(LIST_INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.INACTIVE_SELECTION_BACKGROUND_COLOR_KEY, value);
        } else if (key.equals(LIST_HIGHLIGHT_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.HIGHLIGHT_COLOR_KEY, value);
        } else if (key.equals(LIST_HIGHLIGHT_BACKGROUND_COLOR_KEY)) {
            value = listView.getStyles().put(ListViewSkin.HIGHLIGHT_BACKGROUND_COLOR_KEY, value);
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
        } else if (key.equals(BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_BEVEL_COLOR);
        } else if (key.equals(PRESSED_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_PRESSED_BEVEL_COLOR);
        } else if (key.equals(DISABLED_BEVEL_COLOR_KEY)) {
            previousValue = put(key, DEFAULT_DISABLED_BEVEL_COLOR);
        } else if (key.equals(PADDING_KEY)) {
            previousValue = put(key, DEFAULT_FONT);
        } else if (key.equals(LIST_FONT_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.FONT_KEY);
        } else if (key.equals(LIST_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.COLOR_KEY);
        } else if (key.equals(LIST_DISABLED_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.DISABLED_COLOR_KEY);
        } else if (key.equals(LIST_BACKGROUND_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.BACKGROUND_COLOR_KEY);
        } else if (key.equals(LIST_SELECTION_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.SELECTION_COLOR_KEY);
        } else if (key.equals(LIST_SELECTION_BACKGROUND_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.SELECTION_BACKGROUND_COLOR_KEY);
        } else if (key.equals(LIST_INACTIVE_SELECTION_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.INACTIVE_SELECTION_COLOR_KEY);
        } else if (key.equals(LIST_INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.INACTIVE_SELECTION_BACKGROUND_COLOR_KEY);
        } else if (key.equals(LIST_HIGHLIGHT_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.HIGHLIGHT_COLOR_KEY);
        } else if (key.equals(LIST_HIGHLIGHT_BACKGROUND_COLOR_KEY)) {
            previousValue = listView.getStyles().remove(ListViewSkin.HIGHLIGHT_BACKGROUND_COLOR_KEY);
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
            || key.equals(BEVEL_COLOR_KEY)
            || key.equals(PRESSED_BEVEL_COLOR_KEY)
            || key.equals(DISABLED_BEVEL_COLOR_KEY)
            || key.equals(PADDING_KEY)
            || key.equals(LIST_FONT_KEY)
            || key.equals(LIST_COLOR_KEY)
            || key.equals(LIST_DISABLED_COLOR_KEY)
            || key.equals(LIST_BACKGROUND_COLOR_KEY)
            || key.equals(LIST_SELECTION_COLOR_KEY)
            || key.equals(LIST_SELECTION_BACKGROUND_COLOR_KEY)
            || key.equals(LIST_INACTIVE_SELECTION_COLOR_KEY)
            || key.equals(LIST_INACTIVE_SELECTION_BACKGROUND_COLOR_KEY)
            || key.equals(LIST_HIGHLIGHT_COLOR_KEY)
            || key.equals(LIST_HIGHLIGHT_BACKGROUND_COLOR_KEY)
            || super.containsKey(key));
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        listViewPopup.close();

        pressed = false;
        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()) {
            Component focusedComponent = Component.getFocusedComponent();
            if (focusedComponent != null
                && focusedComponent.getWindow() != listViewPopup) {
                listViewPopup.close();
            }
        }

        pressed = false;
        repaintComponent();
    }

    // Component mouse events
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

        consumed |= listViewPopup.isOpen();

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
        ListButton listButton = (ListButton)getComponent();

        Component.setFocusedComponent(listButton);

        listButton.press();

        if (listView.isShowing()) {
            Component.setFocusedComponent(listView);
        }
    }

    // Component key events
    @Override
    public boolean keyPressed(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
            consumed = true;
        } else {
            consumed = super.keyPressed(keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        ListButton listButton = (ListButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            listButton.press();
        } else {
            consumed = super.keyReleased(keyCode, keyLocation);
        }

        return consumed;
    }

    // Button events
    public void buttonPressed(Button button) {
        if (listViewPopup.isOpen()) {
            listViewPopup.close();
        } else {
            ListButton listButton = (ListButton)button;

            if (listButton.getListData().getLength() > 0) {
                // Determine the popup's location and preferred size, relative
                // to the button
                Point location = listButton.mapPointToAncestor(Display.getInstance(), 0, 0);
                location.y += getHeight() - 1;

                // TODO Ensure that the popup remains within the bounds of the display
                listViewPopup.setLocation(location);
                listViewPopup.open(listButton);

                if (listView.getFirstSelectedIndex() == -1
                    && listView.getListData().getLength() > 0) {
                    listView.setSelectedIndex(0);
                }

                Component.setFocusedComponent(listView);
            }
        }
    }

    // List button events
    public void listDataChanged(ListButton listButton, List<?> previousListData) {
        // TODO Clear the button data?

        listView.setListData(listButton.getListData());
    }

    public void itemRendererChanged(ListButton listButton, ListView.ItemRenderer previousItemRenderer) {
        listView.setItemRenderer(listButton.getItemRenderer());
    }

    public void selectedValueKeyChanged(ListButton listButton, String previousSelectedValueKey) {
        // No-op
    }

    public void valueMappingChanged(ListButton listButton, ListView.ValueMapping previousValueMapping) {
        // No-op
    }

    // List button selection events
    public void selectedIndexChanged(ListButton listButton, int previousSelectedIndex) {
        // Set the selected item as the button data
        int selectedIndex = listButton.getSelectedIndex();

        Object buttonData = (selectedIndex == -1) ? null : listButton.getListData().get(selectedIndex);
        listButton.setButtonData(buttonData);
    }
}
