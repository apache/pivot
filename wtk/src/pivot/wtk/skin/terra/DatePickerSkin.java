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
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;

import pivot.collections.Sequence;
import pivot.util.CalendarDate;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.Container;
import pivot.wtk.Cursor;
import pivot.wtk.DatePicker;
import pivot.wtk.DatePickerListener;
import pivot.wtk.DatePickerSelectionListener;
import pivot.wtk.Dimensions;
import pivot.wtk.Mouse;
import pivot.wtk.PushButton;
import pivot.wtk.media.Image;
import pivot.wtk.skin.ButtonSkin;
import pivot.wtk.skin.ContainerSkin;

/**
 * Date picker skin.
 *
 * @author tvolkert
 */
public class DatePickerSkin extends ContainerSkin
    implements DatePickerListener, DatePickerSelectionListener {

    @Override
    public void install(Component component) {
        validateComponentType(component, DatePicker.class);

        super.install(component);

        DatePicker datePicker = (DatePicker)component;
        datePicker.getDatePickerListeners().add(this);
        datePicker.getDatePickerSelectionListeners().add(this);
    }

    @Override
    public void uninstall() {
        DatePicker datePicker = (DatePicker)getComponent();
        datePicker.getDatePickerListeners().remove(this);
        datePicker.getDatePickerSelectionListeners().remove(this);

        super.uninstall();
    }

    @Override
    public int getPreferredWidth(int height) {
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        return 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO Optimize
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    public void layout() {
        // TODO
    }

    // DatePickerListener methods

    public void monthChanged(DatePicker datePicker, int previousMonth) {
        // TODO
    }

    public void yearChanged(DatePicker datePicker, int previousYear) {
        // TODO
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
