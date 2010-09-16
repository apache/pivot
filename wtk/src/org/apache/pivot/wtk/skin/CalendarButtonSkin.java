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
package org.apache.pivot.wtk.skin;

import java.util.Locale;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.CalendarButtonListener;
import org.apache.pivot.wtk.CalendarButtonSelectionListener;
import org.apache.pivot.wtk.CalendarListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;

/**
 * Abstract base class for calendar button skins.
 * <p>
 * TODO Rather than blindly closing when a mouse down is received, we could
 * instead cache the selection state in the popup's container mouse down event
 * and compare it to the current state in component mouse down. If different,
 * we close the popup. This would also tie this base class less tightly to its
 * concrete subclasses.
 */
public abstract class CalendarButtonSkin extends ButtonSkin
    implements CalendarButton.Skin, CalendarButtonListener, CalendarButtonSelectionListener {
    protected Calendar calendar;
    protected Window calendarPopup;

    protected boolean pressed = false;

    private ComponentMouseButtonListener calendarPopupMouseButtonListener = new ComponentMouseButtonListener.Adapter() {
        @Override
        public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
            CalendarButton calendarButton = (CalendarButton)getComponent();

            calendarPopup.close();

            CalendarDate date = calendar.getSelectedDate();
            calendarButton.setSelectedDate(date);

            return true;
        }
    };

    private ComponentKeyListener calendarPopupKeyListener = new ComponentKeyListener.Adapter() {
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            CalendarButton calendarButton = (CalendarButton)getComponent();

            switch (keyCode) {
                case Keyboard.KeyCode.ESCAPE: {
                    calendarPopup.close();
                    break;
                }

                case Keyboard.KeyCode.TAB:
                case Keyboard.KeyCode.ENTER: {
                    calendarPopup.close();

                    if (keyCode == Keyboard.KeyCode.TAB) {
                        FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                            FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;
                        calendarButton.transferFocus(direction);
                    }

                    CalendarDate date = calendar.getSelectedDate();
                    calendarButton.setSelectedDate(date);

                    break;
                }
            }

            return false;
        }
    };

    private WindowStateListener calendarPopupWindowStateListener = new WindowStateListener.Adapter() {
        @Override
        public void windowOpened(Window window) {
            Display display = window.getDisplay();
            display.getContainerMouseListeners().add(displayMouseListener);
        }

        @Override
        public void windowClosed(Window window, Display display, Window owner) {
            display.getContainerMouseListeners().remove(displayMouseListener);
            getComponent().getWindow().moveToFront();
        }
    };

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!calendarPopup.isAncestor(descendant)
                && descendant != CalendarButtonSkin.this.getComponent()) {
                calendarPopup.close();
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            boolean consumed = false;

            Display display = (Display)container;
            Window window = (Window)display.getComponentAt(x, y);

            if (window != calendarPopup) {
                consumed = true;
            }

            return consumed;
        }
    };

    public CalendarButtonSkin() {
        calendar = new Calendar();
        calendar.getCalendarListeners().add(new CalendarListener.Adapter() {
            @Override
            public void yearChanged(Calendar calendar, int previousYear) {
                CalendarButton calendarButton = (CalendarButton)getComponent();
                calendarButton.setYear(calendar.getYear());
            }

            @Override
            public void monthChanged(Calendar calendar, int previousMonth) {
                CalendarButton calendarButton = (CalendarButton)getComponent();
                calendarButton.setMonth(calendar.getMonth());
            }
        });

        calendarPopup = new Window();
        calendarPopup.getComponentMouseButtonListeners().add(calendarPopupMouseButtonListener);
        calendarPopup.getComponentKeyListeners().add(calendarPopupKeyListener);
        calendarPopup.getWindowStateListeners().add(calendarPopupWindowStateListener);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        CalendarButton calendarButton = (CalendarButton)component;
        calendarButton.getCalendarButtonListeners().add(this);
        calendarButton.getCalendarButtonSelectionListeners().add(this);

        calendar.setLocale(calendarButton.getLocale());
    }

    // CalendarButton.Skin methods

    public Window getCalendarPopup() {
        return calendarPopup;
    }

    // Component state events
    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        calendarPopup.close();
        pressed = false;
    }

    @Override
    public void focusedChanged(Component component, Component obverseComponent) {
        super.focusedChanged(component, obverseComponent);

        // Close the popup if focus was transferred to a component whose
        // window is not the popup
        if (!component.isFocused()
            && !calendarPopup.containsFocus()) {
            calendarPopup.close();
        }

        pressed = false;
    }

    // Component mouse events
    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        pressed = false;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        pressed = true;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseUp(component, button, x, y);

        pressed = false;
        repaintComponent();

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        CalendarButton calendarButton = (CalendarButton)getComponent();

        calendarButton.requestFocus();
        calendarButton.press();

        return consumed;
    }

    /**
     * {@link Keyboard.KeyCode#SPACE} repaints the component to reflect the
     * pressed state.
     *
     * @see #keyReleased(Component, int,
     * org.apache.pivot.wtk.Keyboard.KeyLocation)
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = true;
            repaintComponent();
            consumed = true;
        } else {
            consumed = super.keyPressed(component, keyCode, keyLocation);
        }

        return consumed;
    }

    /**
     * {@link Keyboard.KeyCode#SPACE} 'presses' the button.
     */
    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        CalendarButton calendarButton = (CalendarButton)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            pressed = false;
            repaintComponent();

            calendarButton.press();
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    // Calendar button events
    @Override
    public void yearChanged(CalendarButton calendarButton, int previousYear) {
        calendar.setYear(calendarButton.getYear());
    }

    @Override
    public void monthChanged(CalendarButton calendarButton, int previousMonth) {
        calendar.setMonth(calendarButton.getMonth());
    }

    @Override
    public void localeChanged(CalendarButton calendarButton, Locale previousLocale) {
        calendar.setLocale(calendarButton.getLocale());
    }

    @Override
    public void disabledDateFilterChanged(CalendarButton calendarButton,
        Filter<CalendarDate> previousDisabledDateFilter) {
        calendar.setDisabledDateFilter(calendarButton.getDisabledDateFilter());
    }

    // Calendar button selection events
    @Override
    public void selectedDateChanged(CalendarButton calendarButton,
        CalendarDate previousSelectedDate) {
        // Set the selected date as the button data
        CalendarDate date = calendarButton.getSelectedDate();
        calendarButton.setButtonData(date);

        calendar.setSelectedDate(date);

        if (date != null) {
            calendar.setYear(date.year);
            calendar.setMonth(date.month);
        }
    }
}
