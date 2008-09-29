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

import pivot.util.CalendarDate;
import pivot.wtk.Border;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.DatePicker;
import pivot.wtk.DatePickerButton;
import pivot.wtk.DatePickerButtonListener;
import pivot.wtk.DatePickerButtonSelectionListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;
import pivot.wtk.skin.ButtonSkin;

/**
 * Date picker button skin.
 *
 * @author tvolkert
 */
public class DatePickerButtonSkin extends ButtonSkin
    implements DatePickerButton.Skin, ButtonPressListener,
               DatePickerButtonListener, DatePickerButtonSelectionListener {

    private class DatePickerPopupKeyHandler implements ComponentKeyListener {
        public void keyTyped(Component component, char character) {
            // No-op
        }

        public boolean keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            switch (keyCode) {
                case Keyboard.KeyCode.ESCAPE: {
                    datePickerPopup.close();
                    getComponent().requestFocus();
                    break;
                }
            }

            return false;
        }

        public boolean keyReleased(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            return false;
        }
    }

    private DatePicker datePicker = null;
    private Border datePickerBorder = null;
    private Popup datePickerPopup = null;

    private boolean pressed = false;

    private static final int TRIGGER_WIDTH = 14;

    private Font font = new Font("Verdana", Font.PLAIN, 11);
    private Color color = new Color(0x00, 0x00, 0x00);
    private Color disabledColor = new Color(0x99, 0x99, 0x99);
    private Color backgroundColor = new Color(0xE6, 0xE3, 0xDA);
    private Color disabledBackgroundColor = new Color(0xF7, 0xF5, 0xEB);
    private Color borderColor = new Color(0x99, 0x99, 0x99);
    private Color disabledBorderColor = new Color(0xCC, 0xCC, 0xCC);
    private Color bevelColor = new Color(0xF7, 0xF5, 0xEB);
    private Color pressedBevelColor = new Color(0xCC, 0xCA, 0xC2);
    private Color disabledBevelColor = new Color(0xFF, 0xFF, 0xFF);
    private Insets padding = new Insets(2);

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

    public DatePickerButtonSkin() {
        // Create the date picker and border
        datePicker = new DatePicker();
        datePickerBorder = new Border(datePicker);
        datePickerBorder.getStyles().put("padding", new Insets(0));

        // Create the popup
        datePickerPopup = new Popup(datePickerBorder);
        datePickerPopup.getComponentKeyListeners().add(new DatePickerPopupKeyHandler());
    }

    @Override
    public void install(Component component) {
        super.install(component);

        DatePickerButton datePickerButton = (DatePickerButton)component;

        datePickerButton.getButtonPressListeners().add(this);
        datePickerButton.getDatePickerButtonSelectionListeners().add(this);

        datePicker.setYear(datePickerButton.getYear());
        datePicker.setMonth(datePickerButton.getMonth());

        datePickerBorder.getStyles().put("borderColor", borderColor);
    }

    @Override
    public void uninstall() {
        DatePickerButton datePickerButton = (DatePickerButton)getComponent();

        datePickerPopup.close();

        datePickerButton.getButtonPressListeners().remove(this);
        datePickerButton.getDatePickerButtonSelectionListeners().remove(this);

        super.uninstall();
    }

    @SuppressWarnings("unchecked")
    public int getPreferredWidth(int height) {
        DatePickerButton datePickerButton = (DatePickerButton)getComponent();

        Button.DataRenderer dataRenderer = datePickerButton.getDataRenderer();

        // Determine the preferred width of the current button data
        dataRenderer.render(datePickerButton.getButtonData(), datePickerButton, false);
        int preferredWidth = dataRenderer.getPreferredWidth(-1);

        preferredWidth += TRIGGER_WIDTH + padding.left + padding.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        DatePickerButton datePickerButton = (DatePickerButton)getComponent();
        Button.DataRenderer dataRenderer = datePickerButton.getDataRenderer();

        dataRenderer.render(datePickerButton.getButtonData(), datePickerButton, false);

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
        DatePickerButton datePickerButton = (DatePickerButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (datePickerButton.isEnabled()) {
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
        graphics.fillRect(0, 0, width, height);

        // Draw all lines with a 1px solid stroke
        graphics.setStroke(new BasicStroke());

        // Paint the bevel
        graphics.setPaint(bevelColor);
        graphics.drawLine(1, 1, width - 2, 1);

        // Paint the border
        graphics.setPaint(borderColor);

        int contentX = 0;
        int contentY = 0;
        int contentWidth = Math.max(width - TRIGGER_WIDTH - 1, 0);
        int contentHeight = Math.max(height - 1, 0);

        graphics.drawRect(contentX, contentY, contentWidth, contentHeight);

        int triggerX = Math.max(width - TRIGGER_WIDTH - 1, 0);
        int triggerY = 0;
        int triggerHeight = Math.max(height - 1, 0);

        graphics.drawRect(triggerX, triggerY, TRIGGER_WIDTH, triggerHeight);

        // Paint the content
        Button.DataRenderer dataRenderer = datePickerButton.getDataRenderer();
        dataRenderer.render(datePickerButton.getButtonData(), datePickerButton, false);
        dataRenderer.setSize(Math.max(contentWidth - (padding.left + padding.right + 2) + 1, 0),
            Math.max(contentHeight - (padding.top + padding.bottom + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (datePickerButton.isFocused()) {
            BasicStroke dashStroke = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

            graphics.setStroke(dashStroke);
            graphics.setColor(borderColor);

            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            graphics.drawRect(2, 2, Math.max(contentWidth - 4, 0),
                Math.max(contentHeight - 4, 0));

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

        int tx = triggerX + Math.round((TRIGGER_WIDTH
            - triggerIconShape.getBounds().width) / 2f);
        int ty = triggerY + Math.round((triggerHeight
            - triggerIconShape.getBounds().height) / 2f);
        triggerGraphics.translate(tx, ty);

        triggerGraphics.draw(triggerIconShape);
        triggerGraphics.fill(triggerIconShape);
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        if (pressed) {
            pressed = false;
            repaintComponent();
        }
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        consumed |= datePickerPopup.isOpen();

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public void mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        DatePickerButton datePickerButton = (DatePickerButton)getComponent();

        datePickerButton.requestFocus();
        datePickerButton.press();
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        DatePickerButton datePickerButton = (DatePickerButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            datePickerButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // DatePickerButton.Skin methods

    public DatePicker getDatePicker() {
        return datePicker;
    }

    // ComponentStateListener methods

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        datePickerPopup.close();

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
                && focusedComponent.getWindow() != datePickerPopup) {
                datePickerPopup.close();
            }
        }

        pressed = false;
        repaintComponent();
    }

    // ButtonPressListener methods

    public void buttonPressed(Button button) {
        if (datePickerPopup.isOpen()) {
            datePickerPopup.close();
        } else {
            DatePickerButton datePickerButton = (DatePickerButton)button;

            // Determine the popup's location and preferred size, relative
            // to the button
            Display display = datePickerButton.getWindow().getDisplay();
            Point displayCoordinates = datePickerButton.mapPointToAncestor(display, 0, 0);
            displayCoordinates.y += getHeight() - 1;

            // TODO Ensure that the popup remains within the bounds of the display
            datePickerPopup.setLocation(displayCoordinates);
            datePickerPopup.open(datePickerButton);
        }
    }

    // DatePickerButtonListener methods

    public void yearChanged(DatePickerButton datePickerButton, int previousYear) {
        // No-op
    }

    public void monthChanged(DatePickerButton datePickerButton, int previousMonth) {
        // No-op
    }

    public void selectedDateKeyChanged(DatePickerButton datePickerButton,
        String previousSelectedDateKey) {
        // No-op
    }

    // DatePickerButtonSelectionListener methods

    public void selectedDateChanged(DatePickerButton datePickerButton,
        CalendarDate previousSelectedDate) {
        CalendarDate selectedDate = datePickerButton.getSelectedDate();
        datePicker.setSelectedDate((CalendarDate)null);
        datePickerButton.setButtonData(selectedDate);

        datePickerPopup.close();
        getComponent().requestFocus();
    }
}
