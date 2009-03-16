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
import pivot.wtk.Calendar;
import pivot.wtk.CalendarButton;
import pivot.wtk.CalendarButtonListener;
import pivot.wtk.CalendarButtonSelectionListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Insets;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Popup;
import pivot.wtk.Theme;
import pivot.wtk.skin.ButtonSkin;

/**
 * calendar button skin.
 *
 * @author tvolkert
 */
public class TerraCalendarButtonSkin extends ButtonSkin
    implements CalendarButton.Skin, ButtonPressListener,
               CalendarButtonListener, CalendarButtonSelectionListener {

    private class CalendarPopupKeyHandler implements ComponentKeyListener {
        public void keyTyped(Component component, char character) {
            // No-op
        }

        public boolean keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            switch (keyCode) {
                case Keyboard.KeyCode.ESCAPE: {
                    calendarPopup.close();
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

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color disabledBackgroundColor;
    private Color borderColor;
    private Color disabledBorderColor;
    private Insets padding;

    // Derived colors
    private Color bevelColor;
    private Color pressedBevelColor;
    private Color disabledBevelColor;

    private Calendar calendar = null;
    private Border calendarBorder = null;
    private Popup calendarPopup = null;

    private boolean pressed = false;

    private static final int TRIGGER_WIDTH = 14;

    public TerraCalendarButtonSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(10);
        disabledBackgroundColor = theme.getColor(10);
        borderColor = theme.getColor(7);
        disabledBorderColor = theme.getColor(7);
        padding = new Insets(3);

        // Set the derived colors
        bevelColor = TerraTheme.brighten(backgroundColor);
        pressedBevelColor = TerraTheme.darken(backgroundColor);
        disabledBevelColor = disabledBackgroundColor;

        // Create the calendar and border
        calendar = new Calendar();
        calendarBorder = new Border(calendar);
        calendarBorder.getStyles().put("padding", new Insets(0));

        // Create the popup
        calendarPopup = new Popup(calendarBorder);
        calendarPopup.getComponentKeyListeners().add(new CalendarPopupKeyHandler());
    }

    @Override
    public void install(Component component) {
        super.install(component);

        CalendarButton calendarButton = (CalendarButton)component;
        calendarButton.getCalendarButtonSelectionListeners().add(this);

        calendar.setYear(calendarButton.getYear());
        calendar.setMonth(calendarButton.getMonth());

        calendarBorder.getStyles().put("borderColor", borderColor);
    }

    @Override
    public void uninstall() {
        CalendarButton calendarButton = (CalendarButton)getComponent();

        calendarPopup.close();
        calendarButton.getCalendarButtonSelectionListeners().remove(this);

        super.uninstall();
    }

    @SuppressWarnings("unchecked")
    public int getPreferredWidth(int height) {
        CalendarButton calendarButton = (CalendarButton)getComponent();

        Button.DataRenderer dataRenderer = calendarButton.getDataRenderer();

        // Determine the preferred width of the current button data
        dataRenderer.render(calendarButton.getButtonData(), calendarButton, false);
        int preferredWidth = dataRenderer.getPreferredWidth(-1);

        preferredWidth += TRIGGER_WIDTH + padding.left + padding.right + 2;

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        CalendarButton calendarButton = (CalendarButton)getComponent();
        Button.DataRenderer dataRenderer = calendarButton.getDataRenderer();

        dataRenderer.render(calendarButton.getButtonData(), calendarButton, false);

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
        CalendarButton calendarButton = (CalendarButton)getComponent();

        int width = getWidth();
        int height = getHeight();

        Color backgroundColor = null;
        Color bevelColor = null;
        Color borderColor = null;

        if (calendarButton.isEnabled()) {
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
        Button.DataRenderer dataRenderer = calendarButton.getDataRenderer();
        dataRenderer.render(calendarButton.getButtonData(), calendarButton, false);
        dataRenderer.setSize(Math.max(contentWidth - (padding.left + padding.right + 2) + 1, 0),
            Math.max(contentHeight - (padding.top + padding.bottom + 2) + 1, 0));

        Graphics2D contentGraphics = (Graphics2D)graphics.create();
        contentGraphics.translate(padding.left + 1, padding.top + 1);
        contentGraphics.clipRect(0, 0, dataRenderer.getWidth(), dataRenderer.getHeight());
        dataRenderer.paint(contentGraphics);

        // Paint the focus state
        if (calendarButton.isFocused()) {
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

        consumed |= calendarPopup.isOpen();

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
        CalendarButton calendarButton = (CalendarButton)getComponent();

        calendarButton.requestFocus();
        calendarButton.press();
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

        CalendarButton calendarButton = (CalendarButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            calendarButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // CalendarButton.Skin methods

    public Calendar getCalendar() {
        return calendar;
    }

    // ComponentStateListener methods

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        calendarPopup.close();

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
                && focusedComponent.getWindow() != calendarPopup) {
                calendarPopup.close();
            }
        }

        pressed = false;
        repaintComponent();
    }

    // ButtonPressListener methods

    public void buttonPressed(Button button) {
        if (calendarPopup.isOpen()) {
            calendarPopup.close();
        } else {
            CalendarButton calendarButton = (CalendarButton)button;

            // Determine the popup's location and preferred size, relative
            // to the button
            Display display = calendarButton.getDisplay();
            Point displayCoordinates = calendarButton.mapPointToAncestor(display, 0, 0);
            displayCoordinates.y += getHeight() - 1;

            // TODO Ensure that the popup remains within the bounds of the display
            calendarPopup.setLocation(displayCoordinates);
            calendarPopup.open(calendarButton);
        }
    }

    // CalendarButtonListener methods

    public void yearChanged(CalendarButton calendarButton, int previousYear) {
        // No-op
    }

    public void monthChanged(CalendarButton calendarButton, int previousMonth) {
        // No-op
    }

    public void selectedDateKeyChanged(CalendarButton calendarButton,
        String previousSelectedDateKey) {
        // No-op
    }

    // CalendarButtonSelectionListener methods

    public void selectedDateChanged(CalendarButton calendarButton,
        CalendarDate previousSelectedDate) {
        CalendarDate selectedDate = calendarButton.getSelectedDate();
        calendar.setSelectedDate((CalendarDate)null);
        calendarButton.setButtonData(selectedDate);

        calendarPopup.close();
        getComponent().requestFocus();
    }
}
