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

/**
 * NOTE The system "wait" cursor is not included in this enum since it applies
 * to the application as a whole, rather than a single component. To show/hide
 * the wait cursor, use {@link ApplicationContext#setBlocked(boolean)}.
 *
 * @author gbrown
 */
public enum Cursor {
    DEFAULT,
    HAND,
    TEXT,
    WAIT,
    CROSSHAIR,
    MOVE,
    RESIZE_NORTH,
    RESIZE_SOUTH,
    RESIZE_EAST,
    RESIZE_WEST,
    RESIZE_NORTH_EAST,
    RESIZE_NORTH_WEST,
    RESIZE_SOUTH_EAST,
    RESIZE_SOUTH_WEST;

    public static Cursor decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null.");
        }

        Cursor cursor = null;

        if (value.equalsIgnoreCase("default")) {
            cursor = Cursor.DEFAULT;
        } else if (value.equalsIgnoreCase("hand")) {
            cursor = Cursor.HAND;
        } else if (value.equalsIgnoreCase("text")) {
            cursor = Cursor.TEXT;
        } else if (value.equalsIgnoreCase("wait")) {
            cursor = Cursor.WAIT;
        } else if (value.equalsIgnoreCase("crosshair")) {
            cursor = Cursor.CROSSHAIR;
        } else if (value.equalsIgnoreCase("move")) {
            cursor = Cursor.MOVE;
        } else if (value.equalsIgnoreCase("resizeNorth")) {
            cursor = Cursor.RESIZE_NORTH;
        } else if (value.equalsIgnoreCase("resizeSouth")) {
            cursor = Cursor.RESIZE_SOUTH;
        } else if (value.equalsIgnoreCase("resizeEast")) {
            cursor = Cursor.RESIZE_EAST;
        } else if (value.equalsIgnoreCase("resizeWest")) {
            cursor = Cursor.RESIZE_WEST;
        } else if (value.equalsIgnoreCase("resizeNorthEast")) {
            cursor = Cursor.RESIZE_NORTH_EAST;
        } else if (value.equalsIgnoreCase("resizeNorthWest")) {
            cursor = Cursor.RESIZE_NORTH_WEST;
        } else if (value.equalsIgnoreCase("resizeSouthEast")) {
            cursor = Cursor.RESIZE_SOUTH_EAST;
        } else if (value.equalsIgnoreCase("resizeSouthWest")) {
            cursor = Cursor.RESIZE_SOUTH_WEST;
        } else {
            throw new IllegalArgumentException("\"" + value
                + "\" is not a valid cursor.");
        }

        return cursor;
    }
}
