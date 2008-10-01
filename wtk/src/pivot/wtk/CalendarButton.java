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
public class CalendarButton extends Button {
    /**
     * Date picker button skin interface. Date picker button skins are required
     * to implement this.
     *
     * @author tvolkert
     */
    public interface Skin {
        public Calendar getCalendar();
    }

    /**
     * Date picker button listener list.
     *
     * @author tvolkert
     */
    private static class CalendarButtonListenerList
        extends ListenerList<CalendarButtonListener>
        implements CalendarButtonListener {

        public void yearChanged(CalendarButton calendarButton, int previousYear) {
            for (CalendarButtonListener listener : this) {
                listener.yearChanged(calendarButton, previousYear);
            }
        }

        public void monthChanged(CalendarButton calendarButton, int previousMonth) {
            for (CalendarButtonListener listener : this) {
                listener.monthChanged(calendarButton, previousMonth);
            }
        }

        public void selectedDateKeyChanged(CalendarButton calendarButton,
            String previousSelectedDateKey) {
            for (CalendarButtonListener listener : this) {
                listener.selectedDateKeyChanged(calendarButton, previousSelectedDateKey);
            }
        }
    }

    /**
     * Date picker button selection listener list.
     *
     * @author tvolkert
     */
    private static class CalendarButtonSelectionListenerList
        extends ListenerList<CalendarButtonSelectionListener>
        implements CalendarButtonSelectionListener {

        public void selectedDateChanged(CalendarButton calendarButton,
            CalendarDate previousSelectedDate) {
            for (CalendarButtonSelectionListener listener : this) {
                listener.selectedDateChanged(calendarButton, previousSelectedDate);
            }
        }
    }

    private CalendarButtonListenerList calendarButtonListeners =
        new CalendarButtonListenerList();
    private CalendarButtonSelectionListenerList calendarButtonSelectionListeners =
        new CalendarButtonSelectionListenerList();

    /**
     * Creates a blank date picker button whose date picker is set to the
     * current month and year in the default timezone in the default locale.
     */
    public CalendarButton() {
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
    public CalendarButton(Object buttonData) {
        super(buttonData);

        setDataRenderer(new ButtonDataRenderer());

        installSkin(CalendarButton.class);
    }

    @Override
    protected void setSkin(pivot.wtk.Skin skin) {
        if (!(skin instanceof CalendarButton.Skin)) {
            throw new IllegalArgumentException("Skin class must implement "
                + CalendarButton.Skin.class.getName());
        }

        super.setSkin(skin);
    }

    /**
     * @throws UnsupportedOperationException
     * This method is not supported by CalendarButton.
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
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();

        return calendar.getYear();
    }

    /**
     * Sets the year associated with this date picker button.
     * <p>
     * Fires {@link CalendarButtonListener#yearChanged(CalendarButton, int)}.
     *
     * @param year
     * The year
     */
    public void setYear(int year) {
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();
        int previousYear = calendar.getYear();

        if (previousYear != year) {
            calendar.setYear(year);
            calendarButtonListeners.yearChanged(this, previousYear);
        }
    }

    /**
     * Returns the month associated with this date picker button.
     *
     * @return
     * The date picker month.
     */
    public int getMonth() {
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();

        return calendar.getMonth();
    }

    /**
     * Sets the month associated with this date picker button.
     * <p>
     * Fires {@link CalendarButtonListener#monthChanged(CalendarButton, int)}.
     *
     * @param month
     * The month
     */
    public void setMonth(int month) {
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();
        int previousMonth = calendar.getMonth();

        if (previousMonth != month) {
            calendar.setMonth(month);
            calendarButtonListeners.monthChanged(this, previousMonth);
        }
    }

    /**
     * Returns the currently selected date.
     *
     * @return
     * The currently selected date, or <tt>null</tt> if nothing is selected
     */
    public CalendarDate getSelectedDate() {
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();

        return calendar.getSelectedDate();
    }

    /**
     * Sets the selected date.
     *
     * @param selectedDate
     * The date to select, or <tt>null</tt> to clear the selection
     */
    public void setSelectedDate(CalendarDate selectedDate) {
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();
        CalendarDate previousSelectedDate = calendar.getSelectedDate();

        if ((selectedDate == null ^ previousSelectedDate == null)
            || (selectedDate != null && !selectedDate.equals(previousSelectedDate))) {
            calendar.setSelectedDate(selectedDate);
            calendarButtonSelectionListeners.selectedDateChanged(this,
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
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();

        return calendar.getSelectedDateKey();
    }

    /**
     * Sets this date picker button's data binding key.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        CalendarButton.Skin calendarButtonSkin = (CalendarButton.Skin)getSkin();
        Calendar calendar = calendarButtonSkin.getCalendar();

        String previousSelectedDateKey = calendar.getSelectedDateKey();

        if ((selectedDateKey == null ^ previousSelectedDateKey == null)
            || (selectedDateKey != null && !selectedDateKey.equals(previousSelectedDateKey))) {
            calendar.setSelectedDateKey(selectedDateKey);
            calendarButtonListeners.selectedDateKeyChanged(this,
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
     * Gets this date picker's <tt>CalendarButtonListener</tt> collection.
     */
    public ListenerList<CalendarButtonListener> getCalendarButtonListeners() {
        return calendarButtonListeners;
    }

    /**
     * Gets this date picker's <tt>CalendarButtonSelectionListener</tt> collection.
     */
    public ListenerList<CalendarButtonSelectionListener> getCalendarButtonSelectionListeners() {
        return calendarButtonSelectionListeners;
    }
}
