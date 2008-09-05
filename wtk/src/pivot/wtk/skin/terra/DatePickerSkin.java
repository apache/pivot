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
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import pivot.collections.ArrayList;
import pivot.util.CalendarDate;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.CardPane;
import pivot.wtk.Component;
import pivot.wtk.DatePicker;
import pivot.wtk.DatePickerListener;
import pivot.wtk.DatePickerSelectionListener;
import pivot.wtk.Dimensions;
import pivot.wtk.FlowPane;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Insets;
import pivot.wtk.Label;
import pivot.wtk.PushButton;
import pivot.wtk.TablePane;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Date picker skin.
 *
 * @author tvolkert
 */
public class DatePickerSkin extends ContainerSkin
    implements DatePickerListener, DatePickerSelectionListener {

    public static class CalendarView extends Component {
        private DatePicker datePicker;

        public CalendarView(DatePicker datePicker) {
            this.datePicker = datePicker;

            installSkin(CalendarView.class);
        }

        public DatePicker getDatePicker() {
            return datePicker;
        }
    }

    public static class CalendarViewSkin extends ComponentSkin {
        private ArrayList<String> daysOfWeek = new ArrayList<String>();

        public CalendarViewSkin() {
            GregorianCalendar calendar = new GregorianCalendar(0, 0, 1);
            calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEE");

            for (int i = 0; i < 7; i++) {
                Date date = calendar.getTime();
                daysOfWeek.add(dateFormat.format(date));
                calendar.add(Calendar.DAY_OF_WEEK, 1);
            }
        }

        @Override
        public void install(Component component) {
            validateComponentType(component, CalendarView.class);

            super.install(component);
        }

        public int getPreferredWidth(int height) {
            return 100;
        }

        public int getPreferredHeight(int width) {
            return 100;
        }

        public Dimensions getPreferredSize() {
            // TODO Optimize
            return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
        }

        public void layout() {
            // No-op
        }

        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            graphics.setPaint(Color.WHITE);
            graphics.fill(new Rectangle2D.Double(0, 0, width, height));

            // TODO
        }
    }

    private TablePane tablePane = new TablePane();
    private PushButton yearPrevButton = new PushButton("<");
    private PushButton yearNextButton = new PushButton(">");
    private PushButton monthPrevButton = new PushButton("<");
    private PushButton monthNextButton = new PushButton(">");
    private CardPane monthCardPane = new CardPane();
    private Label yearLabel = new Label();
    private CalendarView calendar;

    public DatePickerSkin() {
        tablePane.getRows().add(new TablePane.Row(-1));
        tablePane.getRows().add(new TablePane.Row(-1));
        tablePane.getColumns().add(new TablePane.Column(-1));
        tablePane.getColumns().add(new TablePane.Column(1, true));
        tablePane.getColumns().add(new TablePane.Column(-1));
        tablePane.getStyles().put("backgroundColor", new Color(0xE6, 0xE3, 0xDA));
        tablePane.getStyles().put("verticalSpacing", 1);
        tablePane.getStyles().put("showHorizontalGridLines", true);
        tablePane.getStyles().put("gridColor", new Color(0x99, 0x99, 0x99));

        GregorianCalendar calendar = new GregorianCalendar(0, 0, 15);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM");

        for (int i = 0; i < 12; i++) {
            calendar.set(Calendar.MONTH, i);
            Date date = calendar.getTime();
            Label label = new Label(dateFormat.format(date));
            label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
            monthCardPane.add(label);
        }

        FlowPane monthFlowPane = new FlowPane();
        monthFlowPane.getStyles().put("padding", 2);
        monthFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        tablePane.setCellComponent(0, 0, monthFlowPane);
        monthFlowPane.add(monthPrevButton);
        monthFlowPane.add(monthCardPane);
        monthFlowPane.add(monthNextButton);

        FlowPane yearFlowPane = new FlowPane();
        yearFlowPane.getStyles().put("padding", 2);
        yearFlowPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        tablePane.setCellComponent(0, 2, yearFlowPane);
        yearFlowPane.add(yearPrevButton);
        yearFlowPane.add(yearLabel);
        yearFlowPane.add(yearNextButton);

        monthPrevButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                DatePicker datePicker = (DatePicker)getComponent();
                int month = datePicker.getMonth();

                if (month == 0) {
                    datePicker.setMonth(11);
                    datePicker.setYear(datePicker.getYear() - 1);
                } else {
                    datePicker.setMonth(month - 1);
                }
            }
        });

        monthNextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                DatePicker datePicker = (DatePicker)getComponent();
                int month = datePicker.getMonth();

                if (month == 11) {
                    datePicker.setMonth(0);
                    datePicker.setYear(datePicker.getYear() + 1);
                } else {
                    datePicker.setMonth(month + 1);
                }
            }
        });

        yearPrevButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                DatePicker datePicker = (DatePicker)getComponent();
                datePicker.setYear(datePicker.getYear() - 1);
            }
        });

        yearNextButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                DatePicker datePicker = (DatePicker)getComponent();
                datePicker.setYear(datePicker.getYear() + 1);
            }
        });
    }

    @Override
    public void install(Component component) {
        validateComponentType(component, DatePicker.class);

        super.install(component);

        DatePicker datePicker = (DatePicker)component;
        datePicker.getDatePickerListeners().add(this);
        datePicker.getDatePickerSelectionListeners().add(this);

        datePicker.add(tablePane);

        calendar = new CalendarView(datePicker);
        tablePane.setCellComponent(1, 0, calendar);
        TablePane.setColumnSpan(calendar, 3);

        updateYear();
        updateMonth();
    }

    @Override
    public void uninstall() {
        DatePicker datePicker = (DatePicker)getComponent();
        datePicker.getDatePickerListeners().remove(this);
        datePicker.getDatePickerSelectionListeners().remove(this);

        datePicker.remove(tablePane);
        datePicker.remove(calendar);

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

    private void updateYear() {
        DatePicker datePicker = (DatePicker)getComponent();
        yearLabel.setText(String.valueOf(datePicker.getYear()));
    }

    private void updateMonth() {
        DatePicker datePicker = (DatePicker)getComponent();
        monthCardPane.setSelectedIndex(datePicker.getMonth());
    }

    // DatePickerListener methods

    public void yearChanged(DatePicker datePicker, int previousYear) {
        updateYear();
    }

    public void monthChanged(DatePicker datePicker, int previousMonth) {
        updateMonth();
    }

    public void selectedDateKeyChanged(DatePicker datePicker,
        String previousSelectedDateKey) {
        // TODO
    }

    // DatePickerSelectionListener methods

    public void selectedDateChanged(DatePicker datePicker,
        CalendarDate previousSelectedDate) {
        // TODO
    }
}
