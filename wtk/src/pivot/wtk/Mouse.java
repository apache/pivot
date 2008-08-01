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

public final class Mouse {
    public enum Button {
        LEFT,
        RIGHT,
        MIDDLE;

        public int getMask() {
            return 2 << ordinal();
        }
    }

    public enum ScrollType {
        UNIT,
        BLOCK
    }

    private static int x = 0;
    private static int y = 0;

    private static int buttons = 0x00;

    /**
     * Returns the x-coordinate of the mouse, in Display coordinates.
     */
    public static int getX() {
        return x;
    }

    /**
     * Returns the x-coordinate of the mouse, in Display coordinates.
     */
    public static int getY() {
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
        return buttons;
    }

    protected static void setButtons(int buttons) {
        Mouse.buttons = buttons;
    }

    /**
     * Tests the pressed state of a modifier.
     *
     * @param modifier
     *
     * @return
     * <tt>true</tt> if the modifier is pressed; <tt>false</tt>, otherwise.
     */
    public static boolean isPressed(Button button) {
        return (buttons & button.getMask()) > 0;
    }
}
