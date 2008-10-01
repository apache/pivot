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
package pivot.wtk;

import pivot.collections.Dictionary;
import pivot.util.CalendarDate;
import pivot.util.ListenerList;
import pivot.wtk.content.ButtonDataRenderer;

/**
 * A component that allows a user to select a calendar date. The date picker
 * is hidden until the user pushes the button.
 *
 * @author tvolkert
 */
public class DatePickerButton extends Button {
    /**
     * Date picker button skin interface. Date picker button skins are required
     * to implement this.
     *
     * @author tvolkert
     */
    public interface Skin {
        public DatePicker getDatePicker();
    }

    /**
     * Date picker button listener list.
     *
     * @author tvolkert
     */
    private static class DatePickerButtonListenerList
        extends ListenerList<DatePickerButtonListener>
        implements DatePickerButtonListener {

        public void yearChanged(DatePickerButton datePickerButton, int previousYear) {
            for (DatePickerButtonListener listener : this) {
                listener.yearChanged(datePickerButton, previousYear);
            }
        }

        public void monthChanged(DatePickerButton datePickerButton, int previousMonth) {
            for (DatePickerButtonListener listener : this) {
                listener.monthChanged(datePickerButton, previousMonth);
            }
        }

        public void selectedDateKeyChanged(DatePickerButton datePickerButton,
            String previousSelectedDateKey) {
            for (DatePickerButtonListener listener : this) {
                listener.selectedDateKeyChanged(datePickerButton, previousSelectedDateKey);
            }
        }
    }

    /**
     * Date picker button selection listener list.
     *
     * @author tvolkert
     */
    private static class DatePickerButtonSelectionListenerList
        extends ListenerList<DatePickerButtonSelectionListener>
        implements DatePickerButtonSelectionListener {

        public void selectedDateChanged(DatePickerButton datePickerButton,
            CalendarDate previousSelectedDate) {
            for (DatePickerButtonSelectionListener listener : this) {
                listener.selectedDateChanged(datePickerButton, previousSelectedDate);
            }
        }
    }

    private DatePickerButtonListenerList datePickerButtonListeners =
        new DatePickerButtonListenerList();
    private DatePickerButtonSelectionListenerList datePickerButtonSelectionListeners =
        new DatePickerButtonSelectionListenerList();

    /**
     * Creates a blank date picker button whose date picker is set to the
     * current month and year in the default timezone in the default locale.
     */
    public DatePickerButton() {
        this(null);
    }

    /**
     * Creates a date picker button with the given button data and whose date
     * picker is set to the current month and year in the default timezone in
     * the default locale.
     *
     * @param buttonData
     * The button's button data
     */
    public DatePickerButton(Object buttonData) {
        super(buttonData);

        setDataRenderer(new ButtonDataRenderer());

        installSkin(DatePickerButton.class);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof DatePickerButton.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + DatePickerButton.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported by DatePickerButton.
     */
    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Date picker buttons cannot be toggle buttons.");
    }

    /**
     * Returns the year associated with this date picker button.
     *
     * @return
     * The date picker year.
     */
    public int getYear() {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();

        return datePicker.getYear();
    }

    /**
     * Sets the year associated with this date picker button.
     * <p>
     * Fires {@link DatePickerButtonListener#yearChanged(DatePickerButton, int)}.
     *
     * @param year
     * The year
     */
    public void setYear(int year) {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();
        int previousYear = datePicker.getYear();

        if (previousYear != year) {
            datePicker.setYear(year);
            datePickerButtonListeners.yearChanged(this, previousYear);
        }
    }

    /**
     * Returns the month associated with this date picker button.
     *
     * @return
     * The date picker month.
     */
    public int getMonth() {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();

        return datePicker.getMonth();
    }

    /**
     * Sets the month associated with this date picker button.
     * <p>
     * Fires {@link DatePickerButtonListener#monthChanged(DatePickerButton, int)}.
     *
     * @param month
     * The month
     */
    public void setMonth(int month) {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();
        int previousMonth = datePicker.getMonth();

        if (previousMonth != month) {
            datePicker.setMonth(month);
            datePickerButtonListeners.monthChanged(this, previousMonth);
        }
    }

    /**
     * Returns the currently selected date.
     *
     * @return
     * The currently selected date, or <tt>null</tt> if nothing is selected
     */
    public CalendarDate getSelectedDate() {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();

        return datePicker.getSelectedDate();
    }

    /**
     * Sets the selected date.
     *
     * @param selectedDate
     * The date to select, or <tt>null</tt> to clear the selection
     */
    public void setSelectedDate(CalendarDate selectedDate) {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();
        CalendarDate previousSelectedDate = datePicker.getSelectedDate();

        if ((selectedDate == null ^ previousSelectedDate == null)
            || (selectedDate != null && !selectedDate.equals(previousSelectedDate))) {
            datePicker.setSelectedDate(selectedDate);
            datePickerButtonSelectionListeners.selectedDateChanged(this,
                previousSelectedDate);
        }
    }

    /**
     * Sets the selected date to the date represented by the specified date
     * string. The date string must be in the <tt>ISO 8601</tt> "calendar date"
     * format, which is <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param selectedDate
     * A string in the form of <tt>[YYYY]-[MM]-[DD]</tt> (e.g. 2008-07-23)
     */
    public final void setSelectedDate(String selectedDate) {
        if (selectedDate == null) {
            throw new IllegalArgumentException("selectedDate is null.");
        }

        setSelectedDate(new CalendarDate(selectedDate));
    }

    /**
     * Gets the data binding key that is set on this date picker button.
     */
    public String getSelectedDateKey() {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();

        return datePicker.getSelectedDateKey();
    }

    /**
     * Sets this date picker button's data binding key.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        DatePickerButton.Skin datePickerButtonSkin = (DatePickerButton.Skin)getSkin();
        DatePicker datePicker = datePickerButtonSkin.getDatePicker();

        String previousSelectedDateKey = datePicker.getSelectedDateKey();

        if ((selectedDateKey == null ^ previousSelectedDateKey == null)
            || (selectedDateKey != null && !selectedDateKey.equals(previousSelectedDateKey))) {
            datePicker.setSelectedDateKey(selectedDateKey);
            datePickerButtonListeners.selectedDateKeyChanged(this,
                previousSelectedDateKey);
        }
    }

    /**
     * Loads the selected date from the specified bind context using this date
     * picker button's bind key, if one is set.
     */
    @Override
    public void load(Dictionary<String, Object> context) {
        String selectedDateKey = getSelectedDateKey();

        if (selectedDateKey != null
            && context.containsKey(selectedDateKey)) {
            Object value = context.get(selectedDateKey);

            if (value instanceof CalendarDate) {
                setSelectedDate((CalendarDate)value);
            } else if (value instanceof String) {
                setSelectedDate((String)value);
            } else {
                throw new IllegalArgumentException("Invalid date type: " +
                    value.getClass().getName());
            }
        }
    }

    /**
     * Stores the selected date into the specified bind context using this date
     * picker button's bind key, if one is set.
     */
    @Override
    public void store(Dictionary<String, Object> context) {
        String selectedDateKey = getSelectedDateKey();

        if (selectedDateKey != null) {
            context.put(selectedDateKey, getSelectedDate());
        }
    }

    /**
     * Gets this date picker's <tt>DatePickerButtonListener</tt> collection.
     */
    public ListenerList<DatePickerButtonListener> getDatePickerButtonListeners() {
        return datePickerButtonListeners;
    }

    /**
     * Gets this date picker's <tt>DatePickerButtonSelectionListener</tt> collection.
     */
    public ListenerList<DatePickerButtonSelectionListener> getDatePickerButtonSelectionListeners() {
        return datePickerButtonSelectionListeners;
    }
}
