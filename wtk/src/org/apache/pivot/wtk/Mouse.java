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

import java.awt.MouseInfo;

/**
 * Class representing the system mouse.
 */
public final class Mouse {
    /**
     * Enumeration representing mouse buttons.
     */
    public enum Button {
        LEFT,
        RIGHT,
        MIDDLE;

        public int getMask() {
            return 1 << ordinal();
        }
    }

    /**
     * Enumeration defining supported scroll types.
     */
    public enum ScrollType {
        UNIT,
        BLOCK
    }

    private static int buttons = 0;
    private static Component capturer = null;

    /**
     * Returns a bitfield representing the mouse buttons that are currently
     * pressed.
     */
    public static int getButtons() {
        return buttons;
    }

    protected static void setButtons(int buttons) {
        Mouse.buttons = buttons;
    }

    /**
     * Tests the pressed state of a button.
     *
     * @param button
     *
     * @return
     * <tt>true</tt> if the button is pressed; <tt>false</tt>, otherwise.
     */
    public static boolean isPressed(Button button) {
        return (buttons & button.getMask()) > 0;
    }

    /**
     * Returns the number of mouse buttons.
     */
    public static int getButtonCount() {
        return MouseInfo.getNumberOfButtons();
    }

    /**
     * "Captures" the mouse, causing all mouse input to be delegated to the
     * given component rather than propagating down the component hierarchy.
     *
     * @param capturerArgument
     * The component that wants to capture the mouse. The mouse pointer must
     * currently be over the component.
     */
    public static void capture(Component capturerArgument) {
        if (capturerArgument == null) {
            throw new IllegalArgumentException("capturer is null.");
        }

        if (!capturerArgument.isMouseOver()) {
            throw new IllegalArgumentException("Mouse pointer is not currently over capturer.");
        }

        if (Mouse.capturer != null) {
            throw new IllegalStateException("Mouse is already captured.");
        }

        Mouse.capturer = capturerArgument;
    }

    /**
     * Releases mouse capture, causing mouse input to resume propagation down
     * the component hierarchy.
     */
    public static void release() {
        if (capturer == null) {
            throw new IllegalStateException("Mouse is not currently captured.");
        }

        Display display = capturer.getDisplay();
        Point location = display.getMouseLocation();

        Component descendant = null;
        if (location != null) {
            descendant = display.getDescendantAt(location.x, location.y);
        }

        while (descendant != null
            && descendant != capturer) {
            descendant = descendant.getParent();
        }

        if (descendant == null) {
            // The mouse is no longer over the capturer
            capturer.mouseOut();
            Mouse.setCursor(display);

            // Allow the mouse to re-enter the display
            if (location != null) {
                display.mouseMove(location.x, location.y);
            }
        }

        capturer = null;
    }

    /**
     * Returns the mouse capturer.
     *
     * @return
     * The component that has captured the mouse, or <tt>null</tt> if the mouse
     * is not currently captured.
     */
    public static Component getCapturer() {
        return capturer;
    }

    /**
     * Returns the current cursor.
     *
     * @throws IllegalStateException
     * If the mouse is not currently captured.
     */
    public static Cursor getCursor() {
        if (capturer == null) {
            throw new IllegalStateException("Mouse is not currently captured.");
        }

        Cursor cursor = null;

        Display display = capturer.getDisplay();
        ApplicationContext.DisplayHost displayHost = display.getDisplayHost();

        int cursorID = displayHost.getCursor().getType();
        switch (cursorID) {
            case java.awt.Cursor.DEFAULT_CURSOR: {
                cursor = Cursor.DEFAULT;
                break;
            }

            case java.awt.Cursor.HAND_CURSOR: {
                cursor = Cursor.HAND;
                break;
            }

            case java.awt.Cursor.TEXT_CURSOR: {
                cursor = Cursor.TEXT;
                break;
            }

            case java.awt.Cursor.WAIT_CURSOR: {
                cursor = Cursor.WAIT;
                break;
            }

            case java.awt.Cursor.CROSSHAIR_CURSOR: {
                cursor = Cursor.CROSSHAIR;
                break;
            }

            case java.awt.Cursor.MOVE_CURSOR: {
                cursor = Cursor.MOVE;
                break;
            }

            case java.awt.Cursor.N_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_NORTH;
                break;
            }

            case java.awt.Cursor.S_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_SOUTH;
                break;
            }

            case java.awt.Cursor.E_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_EAST;
                break;
            }

            case java.awt.Cursor.W_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_WEST;
                break;
            }

            case java.awt.Cursor.NE_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_NORTH_EAST;
                break;
            }

            case java.awt.Cursor.SW_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_SOUTH_WEST;
                break;
            }

            case java.awt.Cursor.NW_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_NORTH_WEST;
                break;
            }

            case java.awt.Cursor.SE_RESIZE_CURSOR: {
                cursor = Cursor.RESIZE_SOUTH_EAST;
                break;
            }

            default: {
                throw new IllegalArgumentException();
            }
        }

        return cursor;
    }

    /**
     * Sets the cursor to an explicit value.
     *
     * @param cursor
     *
     * @throws IllegalStateException
     * If the mouse is not currently captured.
     */
    public static void setCursor(Cursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null.");
        }

        if (capturer == null) {
            throw new IllegalStateException("Mouse is not currently captured.");
        }

        Display display = capturer.getDisplay();
        ApplicationContext.DisplayHost displayHost = display.getDisplayHost();
        displayHost.setCursor(getCursor(cursor));
    }

    /**
     * Sets the cursor based on a given component.
     *
     * @param component
     */
    public static void setCursor(Component component) {
        if (component == null) {
            throw new IllegalArgumentException("component is null.");
        }

        if (!component.isVisible()) {
            throw new IllegalArgumentException("component is not visible.");
        }

        Cursor cursor = null;

        if (component.isEnabled()) {
            cursor = component.getCursor();
            while (cursor == null
                && component != null
                && !(component instanceof Display)) {
                component = component.getParent();
                if (component != null) {
                    cursor = component.getCursor();
                }
            }
        }

        if (component != null) {
            Display display = component.getDisplay();
            ApplicationContext.DisplayHost displayHost = display.getDisplayHost();
            displayHost.setCursor((cursor == null) ? java.awt.Cursor.getDefaultCursor() :
                getCursor(cursor));
        }
    }

    private static java.awt.Cursor getCursor(Cursor cursor) {
        int cursorID = -1;

        switch (cursor) {
            case DEFAULT: {
                cursorID = java.awt.Cursor.DEFAULT_CURSOR;
                break;
            }

            case HAND: {
                cursorID = java.awt.Cursor.HAND_CURSOR;
                break;
            }

            case TEXT: {
                cursorID = java.awt.Cursor.TEXT_CURSOR;
                break;
            }

            case WAIT: {
                cursorID = java.awt.Cursor.WAIT_CURSOR;
                break;
            }

            case CROSSHAIR: {
                cursorID = java.awt.Cursor.CROSSHAIR_CURSOR;
                break;
            }

            case MOVE: {
                cursorID = java.awt.Cursor.MOVE_CURSOR;
                break;
            }

            case RESIZE_NORTH: {
                cursorID = java.awt.Cursor.N_RESIZE_CURSOR;
                break;
            }

            case RESIZE_SOUTH: {
                cursorID = java.awt.Cursor.S_RESIZE_CURSOR;
                break;
            }

            case RESIZE_EAST: {
                cursorID = java.awt.Cursor.E_RESIZE_CURSOR;
                break;
            }

            case RESIZE_WEST: {
                cursorID = java.awt.Cursor.W_RESIZE_CURSOR;
                break;
            }

            case RESIZE_NORTH_EAST: {
                cursorID = java.awt.Cursor.NE_RESIZE_CURSOR;
                break;
            }

            case RESIZE_SOUTH_WEST: {
                cursorID = java.awt.Cursor.SW_RESIZE_CURSOR;
                break;
            }

            case RESIZE_NORTH_WEST: {
                cursorID = java.awt.Cursor.NW_RESIZE_CURSOR;
                break;
            }

            case RESIZE_SOUTH_EAST: {
                cursorID = java.awt.Cursor.SE_RESIZE_CURSOR;
                break;
            }

            default: {
                throw new IllegalArgumentException();
            }
        }

        return new java.awt.Cursor(cursorID);
    }
}
