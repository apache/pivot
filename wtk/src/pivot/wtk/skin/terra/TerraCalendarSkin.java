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
import java.awt.Graphics2D;

import pivot.util.CalendarDate;
import pivot.wtk.Button;
import pivot.wtk.Calendar;
import pivot.wtk.CalendarListener;
import pivot.wtk.CalendarSelectionListener;
import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.Spinner;
import pivot.wtk.SpinnerSelectionListener;
import pivot.wtk.TablePane;
import pivot.wtk.Theme;
import pivot.wtk.content.ButtonDataRenderer;
import pivot.wtk.content.NumericSpinnerData;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.CalendarSkin;

/**
 * Terra calendar skin.
 *
 * @author gbrown
 */
public class TerraCalendarSkin extends CalendarSkin
    implements CalendarListener, CalendarSelectionListener {
    public class DateButton extends Button {
        public DateButton() {
            this(null);
        }

        public DateButton(Object buttonData) {
            super(buttonData);

            setToggleButton(true);
            setDataRenderer(DEFAULT_DATA_RENDERER);

            setSkin(new DateButtonSkin());
        }

        public void press() {
            if (isToggleButton()) {
                State state = getState();

                if (state == State.SELECTED) {
                    setState(State.UNSELECTED);
                }
                else if (state == State.UNSELECTED) {
                    setState(isTriState() ? State.MIXED : State.SELECTED);
                }
                else {
                    setState(State.SELECTED);
                }
            }

            super.press();
        }
    }

    public class DateButtonSkin extends ButtonSkin {
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
            DateButton dateButton = (DateButton)getComponent();

            int width = getWidth();
            int height = getHeight();

            // TODO Paint highlight/selection state

            Button.DataRenderer dataRenderer = dateButton.getDataRenderer();
            dataRenderer.render(dateButton.getButtonData(), dateButton, highlighted);
            dataRenderer.setSize(width - padding * 2, height - padding * 2);

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
    }

    private TablePane tablePane;
    private Spinner monthSpinner;
    private Spinner yearSpinner;

    private DateButton[][] dateButtons = new DateButton[6][7];
    private Button.Group dateButtonGroup;

    private Font font;
    private Color color;
    private Color disabledColor;
    private int padding = 4;

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new ButtonDataRenderer();

    public TerraCalendarSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        font = theme.getFont();
        color = theme.getColor(13);
        disabledColor = theme.getColor(7);

        // Create the table pane
        tablePane = new TablePane();
        for (int i = 0; i < 7; i++) {
            tablePane.getColumns().add(new TablePane.Column(1, true));
        }

        // TODO Set custom data models and renderers on spinners
        // NOTE Month renderer should use locale-specific strings
        monthSpinner = new Spinner();
        monthSpinner.setSpinnerData(new NumericSpinnerData(0, 11));

        yearSpinner = new Spinner();
        yearSpinner.setSpinnerData(new NumericSpinnerData(0, Short.MAX_VALUE));

        SpinnerSelectionListener spinnerSelectionListener = new SpinnerSelectionListener() {
            public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
                updateCalendar();
            }
        };

        monthSpinner.getSpinnerSelectionListeners().add(spinnerSelectionListener);
        yearSpinner.getSpinnerSelectionListeners().add(spinnerSelectionListener);

        TablePane.Row row;

        // Add the month/year flow pane
        FlowPane monthYearFlowPane = new FlowPane();
        monthYearFlowPane.getStyles().put("padding", 2);

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

    private void updateCalendar() {
        // TODO Update month/year spinners

        for (int j = 0; j < 6; j++) {
            for (int i = 0; i < 7; i++) {
                DateButton dateButton = dateButtons[j][i];

                // TODO
            }
        }
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

    // Calendar selection events
    @Override
    public void selectedDateChanged(Calendar calendar, CalendarDate previousSelectedDate) {
        CalendarDate date = calendar.getSelectedDate();
        if (date.getYear() == calendar.getYear()
            && date.getMonth() == calendar.getMonth()) {
            repaintComponent();
        }
    }
}
