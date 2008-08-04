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

import pivot.collections.Dictionary;
import pivot.collections.List;
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

    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = Color.BLACK;
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color backgroundColor = new Color(0xE6, 0xE3, 0xDA);
    private Color disabledBackgroundColor = new Color(0xF7, 0xF5, 0xEB);
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Color disabledBorderColor = new Color(0xCC, 0xCC, 0xCC);
    private Color bevelColor = new Color(0xF7, 0xF5, 0xEB);
    private Color pressedBevelColor = new Color(0xCC, 0xCA, 0xC2);
    private Color disabledBevelColor = Color.WHITE;
    private Insets padding = new Insets(3);

    private boolean pressed = false;

    private static final int TRIGGER_WIDTH = 14;

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
        listViewBorder.getStyles().put("borderColor", borderColor);
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
        contentGraphics.dispose();

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

        triggerGraphics.dispose();
    }

    public ListView getListView() {
        return listView;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(Color.decode(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(Color.decode(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(Color.decode(backgroundColor));
    }

    public Color getDisabledBackgroundColor() {
        return disabledBackgroundColor;
    }

    public void setDisabledBackgroundColor(Color disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        this.disabledBackgroundColor = disabledBackgroundColor;
        repaintComponent();
    }

    public final void setDisabledBackgroundColor(String disabledBackgroundColor) {
        if (disabledBackgroundColor == null) {
            throw new IllegalArgumentException("disabledBackgroundColor is null.");
        }

        setDisabledBackgroundColor(Color.decode(disabledBackgroundColor));
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(Color.decode(borderColor));
    }

    public Color getDisabledBorderColor() {
        return disabledBorderColor;
    }

    public void setDisabledBorderColor(Color disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        this.disabledBorderColor = disabledBorderColor;
        repaintComponent();
    }

    public final void setDisabledBorderColor(String disabledBorderColor) {
        if (disabledBorderColor == null) {
            throw new IllegalArgumentException("disabledBorderColor is null.");
        }

        setDisabledBorderColor(Color.decode(disabledBorderColor));
    }

    public Color getBevelColor() {
        return bevelColor;
    }

    public void setBevelColor(Color bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        this.bevelColor = bevelColor;
        repaintComponent();
    }

    public final void setBevelColor(String bevelColor) {
        if (bevelColor == null) {
            throw new IllegalArgumentException("bevelColor is null.");
        }

        setBevelColor(Color.decode(bevelColor));
    }

    public Color getPressedBevelColor() {
        return pressedBevelColor;
    }

    public void setPressedBevelColor(Color pressedBevelColor) {
        if (pressedBevelColor == null) {
            throw new IllegalArgumentException("pressedBevelColor is null.");
        }

        this.pressedBevelColor = pressedBevelColor;
        repaintComponent();
    }

    public final void setPressedBevelColor(String pressedBevelColor) {
        if (pressedBevelColor == null) {
            throw new IllegalArgumentException("pressedBevelColor is null.");
        }

        setPressedBevelColor(Color.decode(pressedBevelColor));
    }

    public Color getDisabledBevelColor() {
        return disabledBevelColor;
    }

    public void setDisabledBevelColor(Color disabledBevelColor) {
        if (disabledBevelColor == null) {
            throw new IllegalArgumentException("disabledBevelColor is null.");
        }

        this.disabledBevelColor = disabledBevelColor;
        repaintComponent();
    }

    public final void setDisabledBevelColor(String disabledBevelColor) {
        if (disabledBevelColor == null) {
            throw new IllegalArgumentException("disabledBevelColor is null.");
        }

        setDisabledBackgroundColor(Color.decode(disabledBevelColor));
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public Object getListFont() {
        return listView.getStyles().get("font");
    }

    public void setListFont(Object listFont) {
        listView.getStyles().put("font", listFont);
    }

    public Object getListColor() {
        return listView.getStyles().get("color");
    }

    public void setListColor(Object listColor) {
        listView.getStyles().put("color", listColor);
    }

    public Object getListDisabledColor() {
        return listView.getStyles().get("disabledColor");
    }

    public void setListDisabledColor(Object listDisabledColor) {
        listView.getStyles().put("disabledColor", listDisabledColor);
    }

    public Object getListBackgroundColor() {
        return listView.getStyles().get("backgroundColor");
    }

    public void setListBackgroundColor(Object listBackgroundColor) {
        listView.getStyles().put("backgroundColor", listBackgroundColor);
    }

    public Object getListSelectionColor() {
        return listView.getStyles().get("selectionColor");
    }

    public void setListSelectionColor(Object listSelectionColor) {
        listView.getStyles().put("selectionColor", listSelectionColor);
    }

    public Object getListSelectionBackgroundColor() {
        return listView.getStyles().get("selectionBackgroundColor");
    }

    public void setListSelectionBackgroundColor(Object listSelectionBackgroundColor) {
        listView.getStyles().put("selectionBackgroundColor", listSelectionBackgroundColor);
    }

    public Object getListInactiveSelectionColor() {
        return listView.getStyles().get("inactiveSelectionColor");
    }

    public void setListInactiveSelectionColor(Object listInactiveSelectionColor) {
        listView.getStyles().put("inactiveSelectionColor", listInactiveSelectionColor);
    }

    public Object getListInactiveSelectionBackgroundColor() {
        return listView.getStyles().get("inactiveSelectionBackgroundColor");
    }

    public void setListInactiveSelectionBackgroundColor(Object listInactiveSelectionBackgroundColor) {
        listView.getStyles().put("inactiveSelectionBackgroundColor", listInactiveSelectionBackgroundColor);
    }

    public Object getListHighlightColor() {
        return listView.getStyles().get("highlightColor");
    }

    public void setListHighlightColor(Object listHighlightColor) {
        listView.getStyles().put("highlightColor", listHighlightColor);
    }

    public Object getListHighlightBackgroundColor() {
        return listView.getStyles().get("highlightBackgroundColor");
    }

    public void setListHighlightBackgroundColor(Object listHighlightBackgroundColor) {
        listView.getStyles().put("highlightBackgroundColor", listHighlightBackgroundColor);
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
