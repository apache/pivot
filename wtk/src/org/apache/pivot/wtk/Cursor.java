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

/**
 * Enumeration defining the supported mouse cursor types.
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
            throw new IllegalArgumentException();
        }

        Cursor cursor = null;
        if (value.equals("default")) {
            cursor = Cursor.DEFAULT;
        } else if (value.equals("hand")) {
            cursor = Cursor.HAND;
        } else if (value.equals("text")) {
            cursor = Cursor.TEXT;
        } else if (value.equals("wait")) {
            cursor = Cursor.WAIT;
        } else if (value.equals("crosshair")) {
            cursor = Cursor.CROSSHAIR;
        } else if (value.equals("move")) {
            cursor = Cursor.MOVE;
        } else if (value.equals("resizeNorth")) {
            cursor = Cursor.RESIZE_NORTH;
        } else if (value.equals("resizeSouth")) {
            cursor = Cursor.RESIZE_SOUTH;
        } else if (value.equals("resizeEast")) {
            cursor = Cursor.RESIZE_EAST;
        } else if (value.equals("resizeWest")) {
            cursor = Cursor.RESIZE_WEST;
        } else if (value.equals("resizeNorthEast")) {
            cursor = Cursor.RESIZE_NORTH_EAST;
        } else if (value.equals("resizeNorthWest")) {
            cursor = Cursor.RESIZE_NORTH_WEST;
        } else if (value.equals("resizeSouthEast")) {
            cursor = Cursor.RESIZE_SOUTH_EAST;
        } else if (value.equals("resizeSouthWest")) {
            cursor = Cursor.RESIZE_SOUTH_WEST;
        } else {
            cursor = valueOf(value);
        }

        return cursor;
    }
}
