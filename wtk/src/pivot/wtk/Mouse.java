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

import java.awt.MouseInfo;
import java.awt.event.MouseEvent;

/**
 * Class representing the system mouse.
 *
 * @author gbrown
 */
public final class Mouse {
    /**
     * Enumeration representing mouse buttons.
     *
     * @author gbrown
     */
    public enum Button {
        LEFT,
        RIGHT,
        MIDDLE;

        public int getMask() {
            return 1 << ordinal();
        }

        public boolean isSelected(int buttons) {
            return ((buttons & getMask()) > 0);
        }

        public static Button decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    /**
     * Enumeration defining supported scroll types.
     *
     * @author gbrown
     */
    public enum ScrollType {
        UNIT,
        BLOCK
    }

    private static int x = 0;
    private static int y = 0;
    private static int modifiersEx = -1;

    private static ApplicationContext.DisplayHost displayHost = null;

    /**
     * Returns the x-coordinate of the mouse, in the coordinate system of
     * the display used by the current thread.
     */
    public static int getX() {
        if (x == -1) {
            throw new IllegalStateException();
        }

        return x;
    }

    /**
     * Returns the y-coordinate of the mouse, in the coordinate system of
     * the display used by the current thread.
     */
    public static int getY() {
        if (y == -1) {
            throw new IllegalStateException();
        }

        return y;
    }

    protected static void setLocation(int x, int y) {
        Mouse.x = x;
        Mouse.y = y;
    }

    /**
     * Returns a bitfield representing the mouse buttons that are currently
     * pressed.
     */
    public static int getButtons() {
        if (modifiersEx == -1) {
            throw new IllegalStateException();
        }

        int buttons = 0x00;

        if ((modifiersEx & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
            buttons |= Mouse.Button.LEFT.getMask();
        }

        if ((modifiersEx & MouseEvent.BUTTON2_DOWN_MASK) > 0) {
            buttons |= Mouse.Button.MIDDLE.getMask();
        }

        if ((modifiersEx & MouseEvent.BUTTON3_DOWN_MASK) > 0) {
            buttons |= Mouse.Button.RIGHT.getMask();
        }

        return buttons;
    }

    protected static void setModifiersEx(int modifiersEx) {
        // TODO Determine WTK bitfield here instead of getButtons()?
        Mouse.modifiersEx = modifiersEx;
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
        return button.isSelected(getButtons());
    }

    /**
     * Returns the number of mouse buttons.
     */
    public static int getButtonCount() {
        return MouseInfo.getNumberOfButtons();
    }

    /**
     * Returns the system cursor.
     */
    public static Cursor getCursor() {
        if (displayHost == null) {
            throw new IllegalStateException();
        }

        Cursor cursor = null;

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
     * Sets the system cursor.
     *
     * @param cursor
     */
    public static void setCursor(Cursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null.");
        }

        if (displayHost == null) {
            throw new IllegalStateException();
        }

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

        displayHost.setCursor(new java.awt.Cursor(cursorID));
    }

    protected static void setDisplayHost(ApplicationContext.DisplayHost displayHost) {
        Mouse.displayHost = displayHost;
    }
}
