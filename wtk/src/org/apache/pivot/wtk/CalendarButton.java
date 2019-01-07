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
package org.apache.pivot.wtk;

import java.util.Locale;

import org.apache.pivot.annotations.UnsupportedOperation;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.content.CalendarButtonDataRenderer;

/**
 * A component that allows a user to select a calendar date. The calendar is
 * hidden until the user pushes the button.
 */
public class CalendarButton extends Button {
    /**
     * CalendarButton skin interface. CalendarButton skins must implement this
     * interface to facilitate additional communication between the component
     * and the skin.
     */
    public interface Skin {
        public Window getCalendarPopup();
    }

    private int year;
    private int month;

    private CalendarDate selectedDate = null;
    private Locale locale = Locale.getDefault();
    private Filter<CalendarDate> disabledDateFilter = null;

    private String selectedDateKey = null;
    private BindType selectedDateBindType = BindType.BOTH;
    private Calendar.SelectedDateBindMapping selectedDateBindMapping = null;

    private CalendarButtonListener.Listeners calendarButtonListeners = new CalendarButtonListener.Listeners();
    private CalendarButtonSelectionListener.Listeners calendarButtonSelectionListeners =
        new CalendarButtonSelectionListener.Listeners();
    private CalendarButtonBindingListener.Listeners calendarButtonBindingListeners =
        new CalendarButtonBindingListener.Listeners();

    public static final String LANGUAGE_KEY = "language";
    public static final String COUNTRY_KEY = "country";
    public static final String VARIANT_KEY = "variant";

    private static final Button.DataRenderer DEFAULT_DATA_RENDERER = new CalendarButtonDataRenderer();

    public CalendarButton() {
        this(new CalendarDate());
    }

    private CalendarButton(CalendarDate calendarDate) {
        this(calendarDate.year, calendarDate.month);
    }

    public CalendarButton(int year, int month) {
        this.year = year;
        this.month = month;

        setDataRenderer(DEFAULT_DATA_RENDERER);
        installSkin(CalendarButton.class);

        setSelectedDate(new CalendarDate());
    }

    @Override
    protected void setSkin(org.apache.pivot.wtk.Skin skin) {
        checkSkin(skin, CalendarButton.Skin.class);

        super.setSkin(skin);
    }

    /**
     * @return the popup window associated with this components skin
     */
    public Window getListPopup() {
        return ((CalendarButton.Skin) getSkin()).getCalendarPopup();
    }

    /**
     * @throws UnsupportedOperationException This method is not supported by
     * CalendarButton.
     */
    @Override
    @UnsupportedOperation
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Calendar buttons cannot be toggle buttons.");
    }

    /**
     * @return The year to which this calendar button is currently set.
     */
    public int getYear() {
        return year;
    }

    /**
     * Sets this calendar's year.
     *
     * @param year The new current year for this calendar button.
     */
    public void setYear(int year) {
        int previousYear = this.year;

        if (previousYear != year) {
            this.year = year;
            calendarButtonListeners.yearChanged(this, previousYear);
        }
    }

    /**
     * @return The month to which this calendar button is currently set.
     */
    public int getMonth() {
        return month;
    }

    /**
     * Sets this calendar's month.
     *
     * @param month The new month value to set this calendar button to.
     */
    public void setMonth(int month) {
        int previousMonth = this.month;

        if (previousMonth != month) {
            this.month = month;
            calendarButtonListeners.monthChanged(this, previousMonth);
        }
    }

    /**
     * Returns the currently selected date.
     *
     * @return The currently selected date, or <tt>null</tt> if nothing is
     * selected.
     */
    public CalendarDate getSelectedDate() {
        return selectedDate;
    }

    /**
     * Sets the selected date.
     *
     * @param selectedDate The date to select, or <tt>null</tt> to clear the
     * selection.
     */
    public void setSelectedDate(CalendarDate selectedDate) {
        CalendarDate previousSelectedDate = this.selectedDate;

        if (previousSelectedDate != selectedDate) {
            this.selectedDate = selectedDate;
            calendarButtonSelectionListeners.selectedDateChanged(this, previousSelectedDate);
        }
    }

    /**
     * Sets the selected date to the date represented by the specified date
     * string. The date string must be in the <tt>ISO 8601</tt> "calendar date"
     * format, which is <tt>[YYYY]-[MM]-[DD]</tt>.
     *
     * @param selectedDate A string in the form of <tt>[YYYY]-[MM]-[DD]</tt>
     * (e.g. 2008-07-23)
     * @throws IllegalArgumentException if the selected data value is {@code null}.
     */
    public final void setSelectedDate(String selectedDate) {
        setSelectedDate(CalendarDate.decode(selectedDate));
    }

    /**
     * @return The locale used to present calendar data.
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale The new locale used to format/present the data.
     * @throws IllegalArgumentException if the given locale is {@code null}.
     */
    public void setLocale(Locale locale) {
        Utils.checkNull(locale, "locale");

        Locale previousLocale = this.locale;
        if (previousLocale != locale) {
            this.locale = locale;
            calendarButtonListeners.localeChanged(this, previousLocale);
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale An dictionary containing values for language, country, and
     * variant. Country and variant are optional but the must adhere to the
     * following rules: <ul> <li>If variant is specified, language and country
     * are required;</li> <li>Otherwise, if country is specified, language is
     * required;</li> <li>Otherwise, language is required.</li> </ul>
     * @throws IllegalArgumentException if the given locale dictionary is {@code null}.
     */
    public void setLocale(Dictionary<String, ?> locale) {
        Utils.checkNull(locale, "locale");

        String language = (String) locale.get(LANGUAGE_KEY);
        String country = (String) locale.get(COUNTRY_KEY);
        String variant = (String) locale.get(VARIANT_KEY);

        if (variant != null) {
            setLocale(new Locale(language, country, variant));
        } else if (country != null) {
            setLocale(new Locale(language, country));
        } else {
            setLocale(new Locale(language));
        }
    }

    /**
     * Sets the locale used to present calendar data.
     *
     * @param locale A JSON map containing values for language, country, and
     * variant.
     * @throws IllegalArgumentException if the locale string is {@code null} or
     * if there is a problem parsing the string.
     * @see #setLocale(Dictionary)
     */
    public void setLocale(String locale) {
        Utils.checkNullOrEmpty(locale, "locale");

        try {
            setLocale(JSONSerializer.parseMap(locale));
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }
    }

    public Filter<CalendarDate> getDisabledDateFilter() {
        return disabledDateFilter;
    }

    public void setDisabledDateFilter(Filter<CalendarDate> disabledDateFilter) {
        Filter<CalendarDate> previousDisabledDateFilter = this.disabledDateFilter;

        if (previousDisabledDateFilter != disabledDateFilter) {
            this.disabledDateFilter = disabledDateFilter;
            calendarButtonListeners.disabledDateFilterChanged(this, previousDisabledDateFilter);
        }
    }

    /**
     * @return The data binding key that is set on this calendar button.
     */
    public String getSelectedDateKey() {
        return selectedDateKey;
    }

    /**
     * Sets this calendar button's data binding key.
     *
     * @param selectedDateKey The new binding key for the calendar's selected date.
     */
    public void setSelectedDateKey(String selectedDateKey) {
        String previousSelectedDateKey = this.selectedDateKey;

        if (previousSelectedDateKey != selectedDateKey) {
            this.selectedDateKey = selectedDateKey;
            calendarButtonBindingListeners.selectedDateKeyChanged(this, previousSelectedDateKey);
        }
    }

    public BindType getSelectedDateBindType() {
        return selectedDateBindType;
    }

    public void setSelectedDateBindType(BindType selectedDateBindType) {
        Utils.checkNull(selectedDateBindType, "selectedDateBindType");

        BindType previousSelectedDateBindType = this.selectedDateBindType;

        if (previousSelectedDateBindType != selectedDateBindType) {
            this.selectedDateBindType = selectedDateBindType;
            calendarButtonBindingListeners.selectedDateBindTypeChanged(this,
                previousSelectedDateBindType);
        }
    }

    public Calendar.SelectedDateBindMapping getSelectedDateBindMapping() {
        return selectedDateBindMapping;
    }

    public void setSelectedDateBindMapping(Calendar.SelectedDateBindMapping bindMapping) {
        Calendar.SelectedDateBindMapping previousSelectedDateBindMapping = this.selectedDateBindMapping;

        if (previousSelectedDateBindMapping != bindMapping) {
            this.selectedDateBindMapping = bindMapping;
            calendarButtonBindingListeners.selectedDateBindMappingChanged(this,
                previousSelectedDateBindMapping);
        }
    }

    @Override
    public void load(Object context) {
        if (selectedDateKey != null && JSON.containsKey(context, selectedDateKey)
            && selectedDateBindType != BindType.STORE) {
            Object value = JSON.get(context, selectedDateKey);

            CalendarDate selectedDateLocal = null;

            if (value instanceof CalendarDate) {
                selectedDateLocal = (CalendarDate) value;
            } else if (selectedDateBindMapping == null) {
                if (value != null) {
                    selectedDateLocal = CalendarDate.decode(value.toString());
                }
            } else {
                selectedDateLocal = selectedDateBindMapping.toDate(value);
            }

            setSelectedDate(selectedDateLocal);
        }
    }

    @Override
    public void store(Object context) {
        if (selectedDateKey != null && selectedDateBindType != BindType.LOAD) {
            JSON.put(context, selectedDateKey, (selectedDateBindMapping == null) ? selectedDate
                : selectedDateBindMapping.valueOf(selectedDate));
        }
    }

    @Override
    public void clear() {
        super.clear(); // for better consistency with superclass

        if (selectedDateKey != null) {
            setSelectedDate((CalendarDate) null);
        }

    }

    /**
     * Clears the selection.
     */
    public void clearSelection() {
        setSelectedDate((CalendarDate) null);
    }

    /**
     * @return The calendar button listener list.
     */
    public ListenerList<CalendarButtonListener> getCalendarButtonListeners() {
        return calendarButtonListeners;
    }

    /**
     * @return The calendar button selection listener list.
     */
    public ListenerList<CalendarButtonSelectionListener> getCalendarButtonSelectionListeners() {
        return calendarButtonSelectionListeners;
    }

    /**
     * @return The calendar button binding listener list.
     */
    public ListenerList<CalendarButtonBindingListener> getCalendarButtonBindingListeners() {
        return calendarButtonBindingListeners;
    }
}
