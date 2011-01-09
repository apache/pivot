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
package org.apache.pivot.scene;

import java.awt.MouseInfo;
import java.awt.Toolkit;

import org.apache.pivot.scene.data.DropAction;
import org.apache.pivot.scene.data.Manifest;

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
     * Enumeration defining supported mouse cursor types.
     */
    public enum Cursor {
        DEFAULT(java.awt.Cursor.DEFAULT_CURSOR),
        HAND(java.awt.Cursor.HAND_CURSOR),
        TEXT(java.awt.Cursor.TEXT_CURSOR),
        WAIT(java.awt.Cursor.WAIT_CURSOR),
        CROSSHAIR(java.awt.Cursor.CROSSHAIR_CURSOR),
        MOVE(java.awt.Cursor.MOVE_CURSOR),
        RESIZE_NORTH(java.awt.Cursor.N_RESIZE_CURSOR),
        RESIZE_SOUTH(java.awt.Cursor.S_RESIZE_CURSOR),
        RESIZE_EAST(java.awt.Cursor.E_RESIZE_CURSOR),
        RESIZE_WEST(java.awt.Cursor.W_RESIZE_CURSOR),
        RESIZE_NORTH_EAST(java.awt.Cursor.NE_RESIZE_CURSOR),
        RESIZE_NORTH_WEST(java.awt.Cursor.NW_RESIZE_CURSOR),
        RESIZE_SOUTH_EAST(java.awt.Cursor.SE_RESIZE_CURSOR),
        RESIZE_SOUTH_WEST(java.awt.Cursor.SW_RESIZE_CURSOR);

        private int awtCursorType;

        private Cursor(int awtCursorType) {
            this.awtCursorType = awtCursorType;
        };

        protected int getAWTCursorType() {
            return awtCursorType;
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

    private static final int DEFAULT_MULTI_CLICK_INTERVAL = 400;

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
     * Returns the system multi-click interval.
     */
    public static int getMultiClickInterval() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer multiClickInterval = (Integer)toolkit.getDesktopProperty("awt.multiClickInterval");

        if (multiClickInterval == null) {
            multiClickInterval = DEFAULT_MULTI_CLICK_INTERVAL;
        }

        return multiClickInterval;
    }

    /**
     * Begins a drag operation.
     *
     * @param dragContent
     * @param supportedDropActions
     */
    public void drag(Manifest dragContent, int supportedDropActions) {
        // TODO This may be called by application code sourcing local data,
        // or by the platform with remote data; exceptions will be thrown
        // if the drag system is in an incorrect state

        // TODO Provide a protected setter for RemoteManifest and public
        // for LocalManifest; private setter for generic Manifest
    }

    /**
     * During a drag, returns the current drag content.
     *
     * @return
     * The current drag content, or <tt>null</tt> if nothing is being dragged.
     */
    public Manifest getDragContent() {
        // TODO
        return null;
    }

    /**
     * During a drag, returns a bitmask containing the supported drop actions.
     *
     * @return
     * The supported drop actions, or <tt>0</tt> if nothing is being dragged.
     */
    public int getSupportedDropActions() {
        // TODO
        return 0;
    }

    /**
     * During a drag, notifies the drag source of the drop action that would
     * occur if the drag content was dropped.
     *
     * @param dropAction
     */
    public void accept(DropAction dropAction) {
        // TODO
    }

    /**
     * During a drag, notifies the drag source that the content was dropped.
     *
     * @param dropAction
     */
    public void drop(DropAction dropAction) {
        // TODO
    }

    /**
     * Returns the system drag threshold.
     */
    public static int getDragThreshold() {
        return java.awt.dnd.DragSource.getDragThreshold();
    }
}
