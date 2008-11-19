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
package pivot.wtk.content;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

import pivot.util.CalendarDate;
import pivot.wtk.Button;
import pivot.wtk.CalendarButton;
import pivot.wtk.HorizontalAlignment;

/**
 * Default calendar button data renderer.
 * <p>
 * TODO Add showIcon property to this class so the size of the button doesn't
 * change when changing selection between items with and without icons.
 *
 * @author gbrown
 */
public class CalendarButtonDataRenderer extends ButtonDataRenderer {
    public CalendarButtonDataRenderer() {
        getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void render(Object data, Button button, boolean highlight) {
        CalendarButton calendarButton = (CalendarButton)button;
        Locale locale = calendarButton.getLocale();

        if (data instanceof String) {
            data = new CalendarDate((String)data);
        }

        if (data instanceof CalendarDate) {
            CalendarDate date = (CalendarDate)data;

            DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
            data = dateFormat.format(new Date(date.getYear() - 1900, date.getMonth(), date.getDay() + 1));
        }

        super.render(data, button, highlight);
    }
}
