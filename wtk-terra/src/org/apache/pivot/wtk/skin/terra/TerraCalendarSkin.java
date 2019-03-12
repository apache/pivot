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

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSON;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Utils;
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
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.Style;
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
        @UnsupportedOperation
        public void setToggleButton(boolean toggleButton) {
            throw new UnsupportedOperationException();
        }

        @Override
        @UnsupportedOperation
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
            DateButton dateButton = (DateButton) getComponent();

            int preferredWidth = 0;

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            preferredWidth = dataRenderer.getPreferredWidth(height) + padding * 2;

            return preferredWidth;
        }

        @Override
        public int getPreferredHeight(int width) {
            int preferredHeight = 0;

            DateButton dateButton = (DateButton) getComponent();

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            preferredHeight = dataRenderer.getPreferredHeight(width) + padding * 2;

            return preferredHeight;
        }

        @Override
        public Dimensions getPreferredSize() {
            DateButton dateButton = (DateButton) getComponent();

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, false);

            Dimensions preferredSize = dataRenderer.getPreferredSize();

            return new Dimensions(preferredSize.width + padding * 2,
                                  preferredSize.height + padding * 2);
        }

        @Override
        public void paint(Graphics2D graphics) {
            DateButton dateButton = (DateButton) getComponent();

            int width = getWidth();
            int height = getHeight();

            // Paint the background
            if (dateButton.isSelected()) {
                if (!themeIsFlat()) {
                    graphics.setPaint(new GradientPaint(width / 2f, 0, selectionBevelColor, width / 2f,
                        height, selectionBackgroundColor));
                } else {
                    graphics.setPaint(selectionBackgroundColor);
                }

                graphics.fillRect(0, 0, width, height);
            } else {
                if (highlighted) {
                    graphics.setColor(highlightBackgroundColor);
                    graphics.fillRect(0, 0, width, height);
                }
            }

            // Paint a border if this button represents today
            CalendarDate date = (CalendarDate) dateButton.getButtonData();
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

            Calendar calendar = (Calendar) TerraCalendarSkin.this.getComponent();

            if (calendar.containsFocus()) {
                component.requestFocus();
            }
        }

        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            boolean consumed = super.mouseClick(component, button, x, y, count);

            DateButton dateButton = (DateButton) getComponent();
            dateButton.requestFocus();
            dateButton.press();

            return consumed;
        }

        /**
         * {@link KeyCode#ENTER ENTER} 'presses' the button.<br>
         * {@link KeyCode#UP UP}, {@link KeyCode#DOWN DOWN},
         * {@link KeyCode#LEFT LEFT} &amp; {@link KeyCode#RIGHT RIGHT} Navigate
         * around the date grid.
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation) {
            boolean consumed = false;

            DateButton dateButton = (DateButton) getComponent();

            if (keyCode == KeyCode.ENTER) {
                dateButton.press();
            } else if (keyCode == KeyCode.UP || keyCode == KeyCode.DOWN
                || keyCode == KeyCode.LEFT || keyCode == KeyCode.RIGHT) {
                CalendarDate date = (CalendarDate) dateButton.getButtonData();

                Calendar calendar = (Calendar) TerraCalendarSkin.this.getComponent();
                int cellIndex = getCellIndex(date.year, date.month, date.day, calendar.getLocale());
                int rowIndex = cellIndex / 7;
                int columnIndex = cellIndex % 7;

                Component nextButton;
                TablePane.Row row;

                switch (keyCode) {
                    case KeyCode.UP:
                        do {
                            rowIndex--;
                            if (rowIndex < 0) {
                                rowIndex = 5;
                            }

                            row = calendarTablePane.getRows().get(rowIndex + 2);
                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;

                    case KeyCode.DOWN:
                        do {
                            rowIndex++;
                            if (rowIndex > 5) {
                                rowIndex = 0;
                            }

                            row = calendarTablePane.getRows().get(rowIndex + 2);
                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;

                    case KeyCode.LEFT:
                        row = calendarTablePane.getRows().get(rowIndex + 2);

                        do {
                            columnIndex--;
                            if (columnIndex < 0) {
                                columnIndex = 6;
                            }

                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;

                    case KeyCode.RIGHT:
                        row = calendarTablePane.getRows().get(rowIndex + 2);

                        do {
                            columnIndex++;
                            if (columnIndex > 6) {
                                columnIndex = 0;
                            }

                            nextButton = row.get(columnIndex);
                        } while (!nextButton.isEnabled());

                        nextButton.requestFocus();
                        break;

                    default:
                        break;
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
        public boolean keyReleased(Component component, int keyCode,
            KeyLocation keyLocation) {
            boolean consumed = false;

            DateButton dateButton = (DateButton) getComponent();

            if (keyCode == KeyCode.SPACE) {
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
            Calendar calendar = (Calendar) getComponent();

            // Since we're only rendering the month, the year and day do not matter here
            CalendarDate date = new CalendarDate(2000, ((Integer) item).intValue(), 0);

            SimpleDateFormat monthFormat = new SimpleDateFormat("MMMM", calendar.getLocale());
            Object itemFromFormat = monthFormat.format(date.toCalendar().getTime());

            super.render(itemFromFormat, spinner);
        }
    }

    private static class DateButtonDataRenderer extends ButtonDataRenderer {
        @Override
        public void render(Object data, Button button, boolean highlighted) {
            CalendarDate date = (CalendarDate) data;
            super.render(Integer.valueOf(date.day + 1), button, highlighted);

            if (button.isSelected()) {
                label.getStyles().put(Style.color, button.getStyles().get(Style.selectionColor));
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

    @SuppressWarnings("unused")
    public TerraCalendarSkin() {
        Theme theme = currentTheme();
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
            new TablePane.Column(calendarTablePane, 1, true);
        }

        // Month spinner
        monthSpinner = new Spinner();
        monthSpinner.setSpinnerData(new NumericSpinnerData(0, 11));
        monthSpinner.setItemRenderer(new MonthSpinnerItemRenderer());
        monthSpinner.setCircular(true);
        monthSpinner.getStyles().put(Style.sizeToContent, true);

        monthSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                Calendar calendar = (Calendar) getComponent();
                calendar.setMonth(((Integer) spinner.getSelectedItem()).intValue());
            }
        });

        // Year spinner
        yearSpinner = new Spinner();
        yearSpinner.setSpinnerData(
            new NumericSpinnerData(CalendarDate.MIN_CALENDAR_YEAR, CalendarDate.MAX_CALENDAR_YEAR));

        yearSpinner.getSpinnerSelectionListeners().add(new SpinnerSelectionListener() {
            @Override
            public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                Calendar calendar = (Calendar) getComponent();
                calendar.setYear(((Integer) spinner.getSelectedItem()).intValue());
            }
        });

        // Attach a listener to consume mouse clicks
        ComponentMouseButtonListener spinnerMouseButtonListener = new ComponentMouseButtonListener() {
            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y,
                int count) {
                return true;
            }
        };

        monthSpinner.getComponentMouseButtonListeners().add(spinnerMouseButtonListener);
        yearSpinner.getComponentMouseButtonListeners().add(spinnerMouseButtonListener);

        // Add the month/year table pane
        TablePane monthYearTablePane = new TablePane();
        monthYearTablePane.getStyles().put(Style.padding, 3);
        monthYearTablePane.getStyles().put(Style.horizontalSpacing, 4);

        new TablePane.Column(monthYearTablePane, 1, true);
        new TablePane.Column(monthYearTablePane, -1);

        TablePane.Row monthYearRow = new TablePane.Row(monthYearTablePane, -1);
        monthYearRow.add(monthSpinner);
        monthYearRow.add(yearSpinner);

        TablePane.Row calendarRow = new TablePane.Row(calendarTablePane);
        calendarRow.add(monthYearTablePane);

        TablePane.setColumnSpan(monthYearTablePane, 7);

        // Add the day labels
        calendarRow = new TablePane.Row(calendarTablePane);

        Font labelFont = theme.getFont();
        labelFont = labelFont.deriveFont(Font.BOLD);

        for (int i = 0; i < 7; i++) {
            Label label = new Label();
            label.getStyles().put(Style.font, labelFont);
            label.getStyles().put(Style.padding, new Insets(2, 2, 4, 2));
            label.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
            calendarRow.add(label);
        }

        // Add the buttons
        dateButtonGroup = new ButtonGroup();
        dateButtonGroup.getButtonGroupListeners().add(new ButtonGroupListener() {
            @Override
            public void selectionChanged(ButtonGroup buttonGroup, Button previousSelection) {
                Calendar calendar = (Calendar) getComponent();

                Button selection = buttonGroup.getSelection();
                if (selection == null) {
                    CalendarDate selectedDate = calendar.getSelectedDate();

                    // If no date was selected, or the selection changed as a
                    // result of the user toggling the date button (as opposed
                    // to changing the month or year), clear the selection
                    if (selectedDate == null
                        || (selectedDate.year == ((Integer) yearSpinner.getSelectedItem()).intValue()
                        && selectedDate.month == monthSpinner.getSelectedIndex())) {
                        calendar.setSelectedDate((CalendarDate) null);
                    }
                } else {
                    calendar.setSelectedDate((CalendarDate) selection.getButtonData());
                }
            }
        });

        for (int j = 0; j < 6; j++) {
            calendarRow = new TablePane.Row(calendarTablePane, 1, true);

            for (int i = 0; i < 7; i++) {
                DateButton dateButton = new DateButton();
                dateButtons[j][i] = dateButton;
                dateButton.setButtonGroup(dateButtonGroup);

                calendarRow.add(dateButton);
            }
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

        Calendar calendar = (Calendar) component;
        calendar.add(calendarTablePane);

        yearSpinner.setSelectedItem(Integer.valueOf(calendar.getYear()));
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
        GraphicsUtilities.drawLine(graphics, 2, dividerY, Math.max(0, width - 4),
            Orientation.HORIZONTAL);
    }

    private void updateLabels() {
        TablePane.Row row = calendarTablePane.getRows().get(1);

        Calendar calendar = (Calendar) getComponent();
        Locale locale = calendar.getLocale();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(locale);
        SimpleDateFormat monthFormat = new SimpleDateFormat("E", locale);
        int firstDayOfWeek = gregorianCalendar.getFirstDayOfWeek();

        for (int i = 0; i < 7; i++) {
            Label label = (Label) row.get(i);
            gregorianCalendar.set(java.util.Calendar.DAY_OF_WEEK, firstDayOfWeek + i);
            String text = monthFormat.format(gregorianCalendar.getTime());
            text = Character.toString(text.charAt(weekdayCharacterIndex));
            label.setText(text);
        }
    }

    private void updateCalendar() {
        Calendar calendar = (Calendar) getComponent();
        int month = calendar.getMonth();
        int year = calendar.getYear();

        Filter<CalendarDate> disabledDateFilter = calendar.getDisabledDateFilter();

        monthSpinner.setSelectedIndex(month);
        yearSpinner.setSelectedItem(Integer.valueOf(year));

        // Determine the first and last days of the month
        Locale locale = calendar.getLocale();
        GregorianCalendar gregorianCalendar = new GregorianCalendar(locale);
        gregorianCalendar.set(year, month, 1);
        int firstIndex = (7 + gregorianCalendar.get(java.util.Calendar.DAY_OF_WEEK)
            - gregorianCalendar.getFirstDayOfWeek()) % 7;
        int lastIndex = firstIndex
            + gregorianCalendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH);

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
                    && (disabledDateFilter == null || !disabledDateFilter.include(buttonData)));
            }
        }

        // Show/hide last row
        CalendarDate lastWeekStartDate = (CalendarDate) dateButtons[5][0].getButtonData();
        boolean visible = (lastWeekStartDate.month == calendar.getMonth());
        for (Component component : calendarTablePane.getRows().get(7)) {
            component.setVisible(visible);
        }

        today = new CalendarDate();
        updateSelection(calendar.getSelectedDate());
    }

    private void updateSelection(CalendarDate selectedDate) {
        Calendar calendar = (Calendar) getComponent();
        Button selection = dateButtonGroup.getSelection();

        if (selectedDate == null) {
            if (selection != null) {
                selection.setSelected(false);
            }
        } else {
            int year = selectedDate.year;
            int month = selectedDate.month;

            if (year == calendar.getYear() && month == calendar.getMonth()) {
                int day = selectedDate.day;

                // Update the button group
                int cellIndex = getCellIndex(year, month, day, calendar.getLocale());
                int rowIndex = cellIndex / 7;
                int columnIndex = cellIndex % 7;

                TablePane.Row row = calendarTablePane.getRows().get(rowIndex + 2);
                DateButton dateButton = (DateButton) row.get(columnIndex);
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
        Utils.checkNull(font, "font");

        this.font = font;

        monthSpinner.getStyles().put(Style.font, font);
        yearSpinner.getStyles().put(Style.font, font);

        TablePane.Row row = calendarTablePane.getRows().get(1);
        for (int i = 0; i < 7; i++) {
            Label label = (Label) row.get(i);
            label.getStyles().put(Style.font, font);
        }

        invalidateComponent();
    }

    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        Utils.checkNull(selectionColor, "selectionColor");

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor, "selectionColor"));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        Utils.checkNull(selectionBackgroundColor, "selectionBackgroundColor");

        this.selectionBackgroundColor = selectionBackgroundColor;
        selectionBevelColor = TerraTheme.brighten(selectionBackgroundColor);
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        setSelectionBackgroundColor(
            GraphicsUtilities.decodeColor(selectionBackgroundColor, "selectionBackgroundColor"));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        Utils.checkNull(highlightColor, "highlightColor");

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public final void setHighlightColor(String highlightColor) {
        setHighlightColor(GraphicsUtilities.decodeColor(highlightColor, "highlightColor"));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        Utils.checkNull(highlightBackgroundColor, "highlightBackgroundColor");

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public final void setHighlightBackgroundColor(String highlightBackgroundColor) {
        setHighlightBackgroundColor(
            GraphicsUtilities.decodeColor(highlightBackgroundColor, "highlightBackgroundColor"));
    }

    public Color getDividerColor() {
        return dividerColor;
    }

    public void setDividerColor(Color dividerColor) {
        Utils.checkNull(dividerColor, "dividerColor");

        this.dividerColor = dividerColor;
        repaintComponent();
    }

    public final void setDividerColor(String dividerColor) {
        setDividerColor(GraphicsUtilities.decodeColor(dividerColor, "dividerColor"));
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        Utils.checkNonNegative(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Number padding) {
        Utils.checkNull(padding, "padding");

        setPadding(padding.intValue());
    }

    // Calendar events
    @Override
    public void yearChanged(Calendar calendar, int previousYear) {
        yearSpinner.setSelectedItem(Integer.valueOf(calendar.getYear()));
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
    public void disabledDateFilterChanged(Calendar calendar,
        Filter<CalendarDate> previousDisabledDateFilter) {
        updateCalendar();
    }

    // Calendar selection events
    @Override
    public void selectedDateChanged(Calendar calendar, CalendarDate previousSelectedDate) {
        updateSelection(calendar.getSelectedDate());
    }
}
