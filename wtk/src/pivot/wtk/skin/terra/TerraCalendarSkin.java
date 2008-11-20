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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Locale;

import pivot.util.CalendarDate;
import pivot.wtk.Bounds;
import pivot.wtk.Button;
import pivot.wtk.Calendar;
import pivot.wtk.CalendarListener;
import pivot.wtk.CalendarSelectionListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.Mouse;
import pivot.wtk.PushButton;
import pivot.wtk.Spinner;
import pivot.wtk.SpinnerSelectionListener;
import pivot.wtk.TablePane;
import pivot.wtk.Theme;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.content.NumericSpinnerData;
import pivot.wtk.content.SpinnerItemRenderer;
import pivot.wtk.skin.CalendarSkin;
import pivot.wtk.skin.PushButtonSkin;

/**
 * Terra calendar skin.
 *
 * TODO In calendar focus change event, repaint currently selected date button.
 *
 * @author gbrown
 */
public class TerraCalendarSkin extends CalendarSkin
    implements CalendarListener, CalendarSelectionListener {
    public class DateButton extends PushButton {
        public DateButton() {
            this(null);
        }

        public DateButton(Object buttonData) {
            super(buttonData);

            setToggleButton(true);
            setDataRenderer(dateButtonDataRenderer);

            setSkin(new DateButtonSkin());
        }
    }

    public class DateButtonSkin extends PushButtonSkin {
        @Override
        public void install(Component component) {
            super.install(component);

            component.setCursor(Cursor.DEFAULT);
        }

        public int getPreferredWidth(int height) {
            DateButton dateButton = (DateButton)getComponent();

            int preferredWidth = 0;

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            preferredWidth = dataRenderer.getPreferredWidth(height) + padding * 2;

            return preferredWidth;
        }

        public int getPreferredHeight(int width) {
            int preferredHeight = 0;

            DateButton dateButton = (DateButton)getComponent();

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            preferredHeight = dataRenderer.getPreferredHeight(width) + padding * 2;

            return preferredHeight;
        }

        public Dimensions getPreferredSize() {
            DateButton dateButton = (DateButton)getComponent();

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            Dimensions preferredSize = dataRenderer.getPreferredSize();

            preferredSize.width += padding * 2;
            preferredSize.height += padding * 2;

            return preferredSize;
        }

        public void paint(Graphics2D graphics) {
            Calendar calendar = (Calendar)TerraCalendarSkin.this.getComponent();
            DateButton dateButton = (DateButton)getComponent();

            int width = getWidth();
            int height = getHeight();

            if (dateButton.isSelected()) {
                if (calendar.containsFocus()) {
                    graphics.setPaint(new GradientPaint(width / 2, 0, selectionBevelColor,
                        width / 2, height, selectionBackgroundColor));
                } else {
                    graphics.setColor(inactiveSelectionBackgroundColor);
                }

                graphics.fillRect(0, 0, width, height);
            } else if (pressed) {
                graphics.setColor(inactiveSelectionBackgroundColor);
                graphics.fillRect(0, 0, width, height);
            } else {
                if (highlighted) {
                    graphics.setColor(highlightBackgroundColor);
                    graphics.fillRect(0, 0, width, height);
                }
            }

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, highlighted);
            dataRenderer.setSize(width - padding * 2, height - padding * 2);

            graphics.translate(padding, padding);
            dataRenderer.paint(graphics);

            // TODO Paint a focus state (only when this button actually has
            // the focus)
        }

        public Font getFont() {
            return font;
        }

        public Color getColor() {
            return color;
        }

        public Color getDisabledColor() {
            return disabledColor;
        }

        public Color getSelectionColor() {
            return selectionColor;
        }
    }

    public class MonthSpinnerItemRenderer extends SpinnerItemRenderer {
        @Override
        public void render(Object item, Spinner spinner) {
            Calendar calendar = (Calendar)getComponent();

            CalendarDate date = new CalendarDate();
            date.setMonth((Integer)item);

            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM",
                calendar.getLocale());
            item = monthFormat.format(date.toCalendar().getTime());

            super.render(item, spinner);
        }
    }

    private class DateButtonDataRenderer extends ButtonDataRenderer {
        public void render(Object data, Button button, boolean highlighted) {
            super.render(data, button, highlighted);

            Calendar calendar = (Calendar)TerraCalendarSkin.this.getComponent();
            if (button.isSelected()
                && calendar.containsFocus()) {
                label.getStyles().put("color", button.getStyles().get("selectionColor"));
            }
        }
    }

    private TablePane tablePane;
    private Spinner monthSpinner;
    private Spinner yearSpinner;

    private DateButton[][] dateButtons = new DateButton[6][7];
    private Button.Group dateButtonGroup;

    private Button.DataRenderer dateButtonDataRenderer = new DateButtonDataRenderer();

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
    private int padding = 4;

    // Derived colors
    private Color selectionBevelColor;

    public TerraCalendarSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(19);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(9);
        highlightColor = theme.getColor(1);
        highlightBackgroundColor = theme.getColor(10);

        selectionBevelColor = TerraTheme.brighten(selectionBackgroundColor);

        // Create the table pane
        tablePane = new TablePane();
        for (int i = 0; i < 7; i++) {
            tablePane.getColumns().add(new TablePane.Column(1, true));
        }

        // Month spinner
        monthSpinner = new Spinner();
        monthSpinner.setSpinnerData(new NumericSpinnerData(0, 11));
        monthSpinner.setItemRenderer(new MonthSpinnerItemRenderer());
        monthSpinner.getStyles().put("sizeToContent", true);

        monthSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener() {
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
                Calendar calendar = (Calendar)getComponent();
                calendar.setMonth((Integer)spinner.getSelectedValue());
            }
        });

        // Year spinner
        yearSpinner = new Spinner();
        yearSpinner.setSpinnerData(new NumericSpinnerData(0, Short.MAX_VALUE));

        yearSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener() {
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
                Calendar calendar = (Calendar)getComponent();
                calendar.setYear((Integer)spinner.getSelectedValue());
            }
        });

        // Attach a listener to consume mouse clicks
        ComponentMouseButtonListener spinnerMouseButtonListener = new ComponentMouseButtonListener() {
            public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
                return false;
            }

            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                return true;
            }
        };

        monthSpinner.getComponentMouseButtonListeners().add(spinnerMouseButtonListener);
        yearSpinner.getComponentMouseButtonListeners().add(spinnerMouseButtonListener);

        TablePane.Row row;

        // Add the month/year flow pane
        FlowPane monthYearFlowPane = new FlowPane();
        monthYearFlowPane.getStyles().put("padding", 3);
        monthYearFlowPane.getStyles().put("horizontalAlignment", HorizontalAlignment.JUSTIFY);

        monthYearFlowPane.add(monthSpinner);
        monthYearFlowPane.add(yearSpinner);

        row = new TablePane.Row();
        row.add(monthYearFlowPane);
        tablePane.getRows().add(row);

        TablePane.setColumnSpan(monthYearFlowPane, 7);

        // Add the day labels
        row = new TablePane.Row();
        for (int i = 0; i < 7; i++) {
            // TODO Get a locale-specific abbreviation
            Label label = new Label(Integer.toString(i));
            label.getStyles().put("fontBold", true);
            label.getStyles().put("padding", new Insets(2, 2, 4, 2));
            label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
            row.add(label);
        }

        tablePane.getRows().add(row);

        // Add the buttons
        dateButtonGroup = new Button.Group();

        for (int j = 0; j < 6; j++) {
            row = new TablePane.Row(1, true);

            for (int i = 0; i < 7; i++) {
                // TODO Remove the index values
                DateButton dateButton = new DateButton(Integer.toString(i * j + i));
                dateButtons[j][i] = dateButton;
                dateButton.setGroup(dateButtonGroup);

                row.add(dateButton);
            }

            tablePane.getRows().add(row);
        }
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Calendar calendar = (Calendar)component;
        calendar.add(tablePane);

        yearSpinner.setSelectedIndex(calendar.getYear());
        monthSpinner.setSelectedIndex(calendar.getMonth());
        updateCalendar();
    }

    @Override
    public void uninstall() {
        Calendar calendar = (Calendar)getComponent();
        calendar.remove(tablePane);

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        return tablePane.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return tablePane.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return tablePane.getPreferredSize();
    }

    public void layout() {
        tablePane.setSize(getWidth(), getHeight());
        tablePane.setLocation(0, 0);
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        Bounds monthYearRowBounds = tablePane.getRowBounds(0);
        graphics.setColor(highlightBackgroundColor);
        graphics.fillRect(monthYearRowBounds.x, monthYearRowBounds.y,
            monthYearRowBounds.width, monthYearRowBounds.height);

        Bounds labelRowBounds = tablePane.getRowBounds(1);

        graphics.setColor(inactiveSelectionBackgroundColor);
        int dividerY = labelRowBounds.y + labelRowBounds.height - 2;
        graphics.drawLine(2, dividerY, Math.max(0, width - 3), dividerY);
    }

    private void updateCalendar() {
        Calendar calendar = (Calendar)getComponent();
        monthSpinner.setSelectedIndex(calendar.getMonth());
        yearSpinner.setSelectedIndex(calendar.getYear());

        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                DateButton dateButton = dateButtons[j][i];

                // TODO
            }
        }
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

        setColor(decodeColor(color));
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

        setDisabledColor(decodeColor(disabledColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(decodeColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        selectionBevelColor = TerraTheme.brighten(selectionBackgroundColor);
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(decodeColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(decodeColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(decodeColor(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public final void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(decodeColor(highlightColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(decodeColor(highlightBackgroundColor));
    }

    // Calendar events
    @Override
    public void yearChanged(Calendar calendar, int previousYear) {
        yearSpinner.setSelectedIndex(calendar.getYear());
        updateCalendar();
    }

    @Override
    public void monthChanged(Calendar calendar, int previousMonth) {
        monthSpinner.setSelectedIndex(calendar.getMonth());
        updateCalendar();
    }

    @Override
    public void selectedDateKeyChanged(Calendar calendar,
        String previousSelectedDateKey) {
        // No-op
    }

    @Override
    public void localeChanged(Calendar calendar, Locale previousLocale) {
        super.localeChanged(calendar, previousLocale);

        // TODO Repopulate day labels
    }

    // Calendar selection events
    @Override
    public void selectedDateChanged(Calendar calendar, CalendarDate previousSelectedDate) {
        CalendarDate date = calendar.getSelectedDate();
        if (date == null
            || (date.getYear() == calendar.getYear()
                && date.getMonth() == calendar.getMonth())) {
            repaintComponent();
        }
    }
}
