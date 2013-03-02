/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSON;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonGroupListener;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Cursor;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.content.ButtonDataRenderer;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.content.SpinnerItemRenderer;
import org.apache.pivot.wtk.skin.ButtonSkin;
import org.apache.pivot.wtk.skin.CalendarSkin;

/**
 * Terra calendar skin.
 */
public class TerraCalendarSkin extends CalendarSkin {
    public class DateButton extends Button {
        public DateButton() {
            super(null);

            super.setToggleButton(true);
            setDataRenderer(dateButtonDataRenderer);

            setSkin(new DateButtonSkin());
        }

        @Override
        public void press() {
            setSelected(true);

            super.press();
        }

        @Override
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setTriState(boolean triState) {
            throw new UnsupportedOperationException();
        }
    }

    public class DateButtonSkin extends ButtonSkin {
        @Override
        public void install(Component component) {
            super.install(component);

            component.setCursor(Cursor.DEFAULT);
        }

        @Override
        public int getPreferredWidth(int height) {
            DateButton dateButton = (DateButton)getComponent();

            int preferredWidth = 0;

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            preferredWidth = dataRenderer.getPreferredWidth(height) + padding * 2;

            return preferredWidth;
        }

        @Override
        public int getPreferredHeight(int width) {
            int preferredHeight = 0;

            DateButton dateButton = (DateButton)getComponent();

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            preferredHeight = dataRenderer.getPreferredHeight(width) + padding * 2;

            return preferredHeight;
        }

        @Override
        public Dimensions getPreferredSize() {
            DateButton dateButton = (DateButton)getComponent();

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            Dimensions preferredSize = dataRenderer.getPreferredSize();

            return new Dimensions(preferredSize.width + padding * 2,
                preferredSize.height + padding * 2);
        }

        @Override
        public void paint(Graphics2D graphics) {
            DateButton dateButton = (DateButton)getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            if (dateButton.isSelected()) {
                graphics.setPaint(new GradientPaint(width / 2f, 0, selectionBevelColor,
                    width / 2f, height, selectionBackgroundColor));

                graphics.fillRect(0, 0, width, height);
            } else {
                if (highlighted) {
                    graphics.setColor(highlightBackgroundColor);
                    graphics.fillRect(0, 0, width, height);
                }
            }

            // Paint a border if this button represents today
            CalendarDate date = (CalendarDate)dateButton.getButtonData();
            if (date.equals(today)) {
                graphics.setColor(dividerColor);
                GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
            }

            // Paint the content
            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(date, dateButton, highlighted);
            dataRenderer.setSize(Math.max(width - padding * 2, 0),
                Math.max(height - padding * 2, 0));

            graphics.translate(padding, padding);
            dataRenderer.paint(graphics);
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

        @Override
        public void focusedChanged(Component component, Component obverseComponent) {
            highlighted = component.isFocused();

            super.focusedChanged(component, obverseComponent);
        }

        @Override
        public void mouseOver(Component component) {
            super.mouseOver(component);

            Calendar calendar = (Calendar)TerraCalendarSkin.this.getComponent();

            if (calendar.containsFocus()) {
                component.requestFocus();
            }
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = super.mouseClick(component, button, x, y, count);

            DateButton dateButton = (DateButton)getComponent();
            dateButton.requestFocus();
            dateButton.press();

            return consumed;
        }

        /**
         * {@link KeyCode#ENTER ENTER} 'presses' the button.<br>
         * {@link KeyCode#UP UP}, {@link KeyCode#DOWN DOWN},
         * {@link KeyCode#LEFT LEFT} & {@link KeyCode#RIGHT RIGHT} Navigate
         * around the date grid.
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            DateButton dateButton = (DateButton)getComponent();

            if (keyCode == Keyboard.KeyCode.ENTER) {
                dateButton.press();
            } else if (keyCode == Keyboard.KeyCode.UP
                || keyCode == Keyboard.KeyCode.DOWN
                || keyCode == Keyboard.KeyCode.LEFT
                || keyCode == Keyboard.KeyCode.RIGHT) {
                CalendarDate date = (CalendarDate)dateButton.getButtonData();

                Calendar calendar = (Calendar)TerraCalendarSkin.this.getComponent();
                int cellIndex = getCellIndex(date.year, date.month, date.day, calendar.getLocale());
                int rowIndex = cellIndex / 7;
                int columnIndex = cellIndex % 7;

                Component nextButton;
                switch (keyCode) {
                    case Keyboard.KeyCode.UP: {
                        do {
                            rowIndex--;
                            if (rowIndex < 0) {
                                rowIndex = 5;
                            }

                            TablePane.Row row = calendarTablePane.getRows().get(rowIndex + 2);
                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;
                    }

                    case Keyboard.KeyCode.DOWN: {
                        do {
                            rowIndex++;
                            if (rowIndex > 5) {
                                rowIndex = 0;
                            }

                            TablePane.Row row = calendarTablePane.getRows().get(rowIndex + 2);
                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;
                    }

                    case Keyboard.KeyCode.LEFT: {
                        TablePane.Row row = calendarTablePane.getRows().get(rowIndex + 2);

                        do {
                            columnIndex--;
                            if (columnIndex < 0) {
                                columnIndex = 6;
                            }

                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;
                    }

                    case Keyboard.KeyCode.RIGHT: {
                        TablePane.Row row = calendarTablePane.getRows().get(rowIndex + 2);

                        do {
                            columnIndex++;
                            if (columnIndex > 6) {
                                columnIndex = 0;
                            }

                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;
                    }

                    default: {
                        break;
                    }
                }

                consumed = true;
            } else {
                consumed = super.keyPressed(component, keyCode, keyLocation);
            }

            return consumed;
        }

        /**
         * {@link KeyCode#SPACE SPACE} 'presses' the button.
         */
        @Override
        public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            DateButton dateButton = (DateButton)getComponent();

            if (keyCode == Keyboard.KeyCode.SPACE) {
                dateButton.press();
                consumed = true;
            } else {
                consumed = super.keyReleased(component, keyCode, keyLocation);
            }

            return consumed;
        }
    }

    public class MonthSpinnerItemRenderer extends SpinnerItemRenderer {
        @Override
        public void render(Object item, Spinner spinner) {
            Calendar calendar = (Calendar)getComponent();

            // Since we're only rendering the month, the year and day do not matter here
            CalendarDate date = new CalendarDate(2000, (Integer)item, 0);

            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", calendar.getLocale());
            Object itemFromFormat = monthFormat.format(date.toCalendar().getTime());

            super.render(itemFromFormat, spinner);
        }
    }

    private static class DateButtonDataRenderer extends ButtonDataRenderer {
        @Override
        public void render(Object data, Button button, boolean highlighted) {
            CalendarDate date = (CalendarDate)data;
            super.render(date.day + 1, button, highlighted);

            if (button.isSelected()) {
                label.getStyles().put("color", button.getStyles().get("selectionColor"));
            }
        }
    }

    private TablePane calendarTablePane;
    private Spinner monthSpinner;
    private Spinner yearSpinner;

    private DateButton[][] dateButtons = new DateButton[6][7];
    private ButtonGroup dateButtonGroup;

    private Button.DataRenderer dateButtonDataRenderer = new DateButtonDataRenderer();

    private int weekdayCharacterIndex;

    private CalendarDate today = null;

    private Font font;
    private Color color;
    private Color disabledColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
    private Color dividerColor;
    private int padding = 4;

    private Color selectionBevelColor;

    public TerraCalendarSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(14);
        highlightColor = theme.getColor(1);
        highlightBackgroundColor = theme.getColor(10);
        dividerColor = theme.getColor(9);

        selectionBevelColor = TerraTheme.brighten(selectionBackgroundColor);

        // Create the table pane
        calendarTablePane = new TablePane();
        for (int i = 0; i < 7; i++) {
            calendarTablePane.getColumns().add(new TablePane.Column(1, true));
        }

        // Month spinner
        monthSpinner = new Spinner();
        monthSpinner.setSpinnerData(new NumericSpinnerData(0, 11));
        monthSpinner.setItemRenderer(new MonthSpinnerItemRenderer());
        monthSpinner.setCircular(true);
        monthSpinner.getStyles().put("sizeToContent", true);

        monthSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                Calendar calendar = (Calendar)getComponent();
                calendar.setMonth((Integer)spinner.getSelectedItem());
            }
        });

        // Year spinner
        yearSpinner = new Spinner();
        yearSpinner.setSpinnerData(new NumericSpinnerData(0, Short.MAX_VALUE));

        yearSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                Calendar calendar = (Calendar)getComponent();
                calendar.setYear((Integer)spinner.getSelectedItem());
            }
        });

        // Attach a listener to consume mouse clicks
        ComponentMouseButtonListener spinnerMouseButtonListener = new ComponentMouseButtonListener.Adapter() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                return true;
            }
        };

        monthSpinner.getComponentMouseButtonListeners().add(spinnerMouseButtonListener);
        yearSpinner.getComponentMouseButtonListeners().add(spinnerMouseButtonListener);

        // Add the month/year table pane
        TablePane monthYearTablePane = new TablePane();
        monthYearTablePane.getStyles().put("padding", 3);
        monthYearTablePane.getStyles().put("horizontalSpacing", 4);

        monthYearTablePane.getColumns().add(new TablePane.Column(1, true));
        monthYearTablePane.getColumns().add(new TablePane.Column(-1));

        TablePane.Row monthYearRow = new TablePane.Row(-1);
        monthYearTablePane.getRows().add(monthYearRow);
        monthYearRow.add(monthSpinner);
        monthYearRow.add(yearSpinner);

        TablePane.Row calendarRow = new TablePane.Row();
        calendarRow.add(monthYearTablePane);
        calendarTablePane.getRows().add(calendarRow);

        TablePane.setColumnSpan(monthYearTablePane, 7);

        // Add the day labels
        calendarRow = new TablePane.Row();

        Font labelFont = theme.getFont();
        labelFont = labelFont.deriveFont(Font.BOLD);

        for (int i = 0; i < 7; i++) {
            Label label = new Label();
            label.getStyles().put("font", labelFont);
            label.getStyles().put("padding", new Insets(2, 2, 4, 2));
            label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
            calendarRow.add(label);
        }

        calendarTablePane.getRows().add(calendarRow);

        // Add the buttons
        dateButtonGroup = new ButtonGroup();
        dateButtonGroup.getButtonGroupListeners().add(new ButtonGroupListener.Adapter() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                Calendar calendar = (Calendar)getComponent();

                Button selection = buttonGroup.getSelection();
                if (selection == null) {
                    CalendarDate selectedDate = calendar.getSelectedDate();

                    // If no date was selected, or the selection changed as a
                    // result of the user toggling the date button (as opposed
                    // to changing the month or year), clear the selection
                    if (selectedDate == null
                        || (selectedDate.year == yearSpinner.getSelectedIndex()
                            && selectedDate.month == monthSpinner.getSelectedIndex())) {
                        calendar.setSelectedDate((CalendarDate)null);
                    }
                } else {
                    calendar.setSelectedDate((CalendarDate)selection.getButtonData());
                }
            }
        });

        for (int j = 0; j < 6; j++) {
            calendarRow = new TablePane.Row(1, true);

            for (int i = 0; i < 7; i++) {
                DateButton dateButton = new DateButton();
                dateButtons[j][i] = dateButton;
                dateButton.setButtonGroup(dateButtonGroup);

                calendarRow.add(dateButton);
            }

            calendarTablePane.getRows().add(calendarRow);
        }

        Resources resources;
        try {
            resources = new Resources(TerraCalendarSkin.class.getName());
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        weekdayCharacterIndex = JSON.getInt(resources, "weekdayCharacterIndex");
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Calendar calendar = (Calendar)component;
        calendar.add(calendarTablePane);

        yearSpinner.setSelectedIndex(calendar.getYear());
        monthSpinner.setSelectedIndex(calendar.getMonth());
        updateLabels();
        updateCalendar();
    }

    @Override
    public int getPreferredWidth(int height) {
        return calendarTablePane.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return calendarTablePane.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return calendarTablePane.getPreferredSize();
    }

    @Override
    public int getBaseline(int width, int height) {
        return calendarTablePane.getBaseline(width, height);
    }

    @Override
    public void layout() {
        calendarTablePane.setSize(getWidth(), getHeight());
        calendarTablePane.setLocation(0, 0);
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        int width = getWidth();
        Bounds monthYearRowBounds = calendarTablePane.getRowBounds(0);
        graphics.setColor(highlightBackgroundColor);
        graphics.fillRect(monthYearRowBounds.x, monthYearRowBounds.y,
            monthYearRowBounds.width, monthYearRowBounds.height);

        Bounds labelRowBounds = calendarTablePane.getRowBounds(1);

        graphics.setColor(dividerColor);
        int dividerY = labelRowBounds.y + labelRowBounds.height - 2;
        GraphicsUtilities.drawLine(graphics, 2, dividerY, Math.max(0, width - 4), Orientation.HORIZONTAL);
    }

    private void updateLabels() {
        TablePane.Row row = calendarTablePane.getRows().get(1);

        Calendar calendar = (Calendar)getComponent();
        Locale locale = calendar.getLocale();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(locale);
        SimpleDateFormat monthFormat = new SimpleDateFormat("E", locale);
        int firstDayOfWeek = gregorianCalendar.getFirstDayOfWeek();

        for (int i = 0; i < 7; i++) {
            Label label = (Label)row.get(i);
            gregorianCalendar.set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek + i);
            String text = monthFormat.format(gregorianCalendar.getTime());
            text = Character.toString(text.charAt(weekdayCharacterIndex));
            label.setText(text);
        }
    }

    private void updateCalendar() {
        Calendar calendar = (Calendar)getComponent();
        int month = calendar.getMonth();
        int year = calendar.getYear();

        Filter<CalendarDate> disabledDateFilter = calendar.getDisabledDateFilter();

        monthSpinner.setSelectedIndex(month);
        yearSpinner.setSelectedIndex(year);

        // Determine the first and last days of the month
        Locale locale = calendar.getLocale();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(locale);
        gregorianCalendar.set(year, month, 1);
        int firstIndex = (7 + gregorianCalendar.get(java.util.Calendar.DAY_OF_WEEK)
            - gregorianCalendar.getFirstDayOfWeek()) % 7;
        int lastIndex = firstIndex + gregorianCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        // Determine the last day of last month
        gregorianCalendar.add(java.util.Calendar.MONTH, -1);
        int daysLastMonth = gregorianCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                month = calendar.getMonth();
                year = calendar.getYear();

                int k = j * 7 + i;

                DateButton dateButton = dateButtons[j][i];

                int day;
                boolean enabled = false;
                if (k < firstIndex) {
                    month--;
                    if (month < 0) {
                        month = 11;
                        year--;
                    }

                    day = daysLastMonth - (firstIndex - k);
                } else if (k >= lastIndex) {
                    month++;
                    if (month > 11) {
                        month = 0;
                        year++;
                    }

                    day = k - lastIndex;
                } else {
                    day = k - firstIndex;
                    enabled = true;
                }

                CalendarDate buttonData = new CalendarDate(year, month, day);
                dateButton.setButtonData(buttonData);
                dateButton.setEnabled(enabled
                    && (disabledDateFilter == null
                        || !disabledDateFilter.include(buttonData)));
            }
        }

        // Show/hide last row
        CalendarDate lastWeekStartDate = (CalendarDate)dateButtons[5][0].getButtonData();
        boolean visible = (lastWeekStartDate.month == calendar.getMonth());
        for (Component component : calendarTablePane.getRows().get(7)) {
            component.setVisible(visible);
        }

        today = new CalendarDate();
        updateSelection(calendar.getSelectedDate());
    }

    private void updateSelection(CalendarDate selectedDate) {
        Calendar calendar = (Calendar)getComponent();
        Button selection = dateButtonGroup.getSelection();

        if (selectedDate == null) {
            if (selection != null) {
                selection.setSelected(false);
            }
        } else {
            int year = selectedDate.year;
            int month = selectedDate.month;

            if (year == calendar.getYear()
                && month == calendar.getMonth()) {
                int day = selectedDate.day;

                // Update the button group
                int cellIndex = getCellIndex(year, month, day, calendar.getLocale());
                int rowIndex = cellIndex / 7;
                int columnIndex = cellIndex % 7;

                TablePane.Row row = calendarTablePane.getRows().get(rowIndex + 2);
                DateButton dateButton = (DateButton)row.get(columnIndex);
                dateButton.setSelected(true);
            } else {
                if (selection != null) {
                    selection.setSelected(false);
                }
            }
        }
    }

    private static int getCellIndex(int year, int month, int day, Locale locale) {
        GregorianCalendar gregorianCalendar = new GregorianCalendar(locale);
        gregorianCalendar.set(year, month, 1);
        int firstDay = ((gregorianCalendar.get(java.util.Calendar.DAY_OF_WEEK)
            - gregorianCalendar.getFirstDayOfWeek()) + 7) % 7;
        int cellIndex = firstDay + day;

        return cellIndex;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;

        monthSpinner.getStyles().put("font", font);
        yearSpinner.getStyles().put("font", font);

        TablePane.Row row = calendarTablePane.getRows().get(1);
        for (int i = 0; i < 7; i++) {
            Label label = (Label)row.get(i);
            label.getStyles().put("font", font);
        }

        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
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

        setColor(GraphicsUtilities.decodeColor(color));
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

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
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

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
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

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
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

        setHighlightColor(GraphicsUtilities.decodeColor(highlightColor));
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

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
    }

    public Color getDividerColor() {
        return dividerColor;
    }

    public void setDividerColor(Color dividerColor) {
        if (dividerColor == null) {
            throw new IllegalArgumentException("dividerColor is null.");
        }

        this.dividerColor = dividerColor;
        repaintComponent();
    }

    public final void setDividerColor(String dividerColor) {
        if (dividerColor == null) {
            throw new IllegalArgumentException("dividerColor is null.");
        }

        setDividerColor(GraphicsUtilities.decodeColor(dividerColor));
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        if (padding < 0) {
            throw new IllegalArgumentException("padding is negative.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
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
    public void localeChanged(Calendar calendar, Locale previousLocale) {
        invalidateComponent();

        updateLabels();
        updateCalendar();
    }

    @Override
    public void disabledDateFilterChanged(Calendar calendar, Filter<CalendarDate> previousDisabledDateFilter) {
        updateCalendar();
    }

    // Calendar selection events
    @Override
    public void selectedDateChanged(Calendar calendar, CalendarDate previousSelectedDate) {
        updateSelection(calendar.getSelectedDate());
    }
}
