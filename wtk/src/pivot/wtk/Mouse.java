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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

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

    /**
     * Enumeration defining supported drop actions.
     *
     * @author gbrown
     */
    public enum DropAction {
        COPY,
        MOVE,
        LINK;

        public int getMask() {
            return 1 << ordinal();
        }

        public boolean isSelected(int dropActions) {
            return ((dropActions & getMask()) > 0);
        }

        public static DropAction decode(String value) {
            return valueOf(value.toUpperCase());
        }
    }

    private static int x = 0;
    private static int y = 0;
    private static int modifiersEx = 0;

    private static Object dragContent = null;
    private static Class<?> dragContentType = null;
    private static int supportedDropActions = 0;
    private static Visual dragRepresentation = null;
    private static Point dragOffset = null;
    private static MouseDragListener mouseDragListener = null;

    protected static void initialize(ApplicationContext applicationContext) {
        ApplicationContext.DisplayHost displayHost = applicationContext.getDisplayHost();

        displayHost.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent event) {
                modifiersEx = event.getModifiersEx();
            }

            public void mouseReleased(MouseEvent event) {
                modifiersEx = event.getModifiersEx();
            }
        });

        displayHost.addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseMoved(MouseEvent event) {
                x = event.getX();
                y = event.getY();
            }

            public void mouseDragged(MouseEvent event) {
                x = event.getX();
                y = event.getY();
            }
        });

        // TODO Add native drop handler (DropTargetAdapter)
    }

    /**
     * Returns the x-coordinate of the mouse, in the coordinate system of
     * the display used by the current thread.
     */
    public static int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of the mouse, in the coordinate system of
     * the display used by the current thread.
     */
    public static int getY() {
        return y;
    }

    /**
     * Returns a bitfield representing the mouse buttons that are currently
     * pressed.
     */
    public static int getButtons() {
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
        Cursor cursor = null;

        ApplicationContext applicationContext = ApplicationContext.getApplicationContext();
        ApplicationContext.DisplayHost displayHost = applicationContext.getDisplayHost();
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
    @SuppressWarnings("deprecation")
    public static void setCursor(Cursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null.");
        }

        ApplicationContext applicationContext = ApplicationContext.getApplicationContext();
        ApplicationContext.DisplayHost displayHost = applicationContext.getDisplayHost();
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

    /**
     * Initiates a drag operation.
     *
     * @param dragContent
     * @param supportedDropActions
     */
    public static void drag(Object dragContent, int supportedDropActions) {
        drag(dragContent, supportedDropActions, null, null, null);
    }

    /**
     * Initiates a drag operation.
     *
     * @param dragContent
     * @param supportedDropActions
     * @param mouseDragListener
     */
    public static void drag(Object dragContent, int supportedDropActions,
        MouseDragListener mouseDragListener) {
        drag(dragContent, supportedDropActions, null, null, mouseDragListener);
    }

    /**
     * Initiates a drag operation.
     *
     * @param dragContent
     * @param supportedDropActions
     * @param dragRepresentation
     * @param dragOffset
     */
    public static void drag(Object dragContent, int supportedDropActions,
        Visual dragRepresentation, Point dragOffset) {
        drag(dragContent, supportedDropActions, dragRepresentation, dragOffset, null);
    }

    /**
     * Initiates a drag operation.
     *
     * @param dragContent
     * @param supportedDropActions
     * @param dragRepresentation
     * @param dragOffset
     * @param mouseDragListener
     */
    public static void drag(Object dragContent, int supportedDropActions,
        Visual dragRepresentation, Point dragOffset, MouseDragListener mouseDragListener) {
        if (dragContent == null) {
            throw new IllegalArgumentException("dragContent is null.");
        }

        if (supportedDropActions == 0) {
            throw new IllegalArgumentException("supportedDropActions must be greater than 0.");
        }

        if (dragRepresentation != null
            && dragOffset == null) {
            throw new IllegalArgumentException("offset is required when a representation is specified.");
        }

        // TODO Should this override an existing drag?
        if (isDrag()) {
            throw new IllegalStateException("A drag is already in progress.");
        }

        Mouse.dragContent = dragContent;
        Mouse.dragContentType = dragContent.getClass();
        Mouse.supportedDropActions = supportedDropActions;
        Mouse.dragRepresentation = dragRepresentation;
        Mouse.dragOffset = dragOffset;
        Mouse.mouseDragListener = mouseDragListener;

        updateDragCursor();
    }

    /**
     * Returns the drag state.
     *
     * @return
     * <tt>true</tt> if a drag operation is in progress; <tt>false</tt>,
     * otherwise.
     */
    public static boolean isDrag() {
        return (dragContentType != null);
    }

    /**
     * Returns the type of the item currently being dragged.
     *
     * @return
     * The type of the item being dragged, or <tt>null</tt> if nothing is
     * currently being dragged.
     */
    public static Class<?> getDragContentType() {
        return dragContentType;
    }

    /**
     * Returns a bitfield containing the supported drop actions for the current
     * drag operation.
     *
     * @return
     * The supported drop actions, or <tt>0</tt> if nothing is currently being
     * dragged.
     */
    public static int getSupportedDropActions() {
        return supportedDropActions;
    }

    /**
     * Returns a visual representing the item being dragged.
     *
     * @return
     * The drag representation, or <tt>null</tt> if nothing is currently
     * being dragged or the item has no visual representation.
     */
    public static Visual getDragRepresentation() {
        return dragRepresentation;
    }

    /**
     * Returns the drag offset.
     *
     * @return
     * The offset of the mouse pointer within the drag visual, or <tt>null</tt>
     * if nothing is currently being dragged or the item has no visual
     * representation.
     */
    public static Point getDragOffset() {
        return dragOffset;
    }

    /**
     * Tests whether a drop action is valid for the current drag state. A
     * drop action is valid if it is supported by the drag source and currently
     * selected via keyboard modifier keys.
     *
     * @param dropAction
     *
     * @return
     * <tt>true</tt> if the drop option is valid; <tt>false</tt>, otherwise.
     */
    public static boolean isValidDropAction(DropAction dropAction) {
        return (Keyboard.getDropAction() == dropAction
            && dropAction.isSelected(supportedDropActions));
    }

    /**
     * Drops the item currently being dragged.
     *
     * @param dropAction
     * The drop action to apply, or <tt>null</tt> for no drop action.
     *
     * @return
     * The item that was dragged, or <tt>null</tt> if <tt>dropAction</tt> is
     * null.
     */
    public static Object drop(DropAction dropAction) {
        if (dragContentType == null) {
            throw new IllegalStateException("A drag is not in progress.");
        }

        Object dragContent = null;
        if (dropAction != null) {
            if (!isValidDropAction(dropAction)) {
                throw new IllegalStateException("dropAction is not valid.");
            }

            dragContent = Mouse.dragContent;
        }

        if (mouseDragListener != null) {
            mouseDragListener.mouseDrop(dropAction);
        }

        Mouse.dragContent = null;

        dragContentType = null;
        supportedDropActions = 0;
        dragRepresentation = null;
        dragOffset = null;
        mouseDragListener = null;

        return dragContent;
    }

    protected static void updateDragCursor() {
        ApplicationContext applicationContext = ApplicationContext.getApplicationContext();
        ApplicationContext.DisplayHost displayHost = applicationContext.getDisplayHost();

        java.awt.Cursor cursor = java.awt.Cursor.getDefaultCursor();

        if (isDrag()) {
            // Show an appropriate cursor for the union of the supported drop
            // actions and the user's selected drop action
            DropAction dropAction = Keyboard.getDropAction();

            if (dropAction != null) {
                if (dropAction.isSelected(supportedDropActions)) {
                    switch (dropAction) {
                        case COPY: {
                            cursor = java.awt.dnd.DragSource.DefaultCopyDrop;
                            break;
                        }

                        case MOVE: {
                            cursor = java.awt.dnd.DragSource.DefaultMoveDrop;
                            break;
                        }

                        case LINK: {
                            cursor = java.awt.dnd.DragSource.DefaultLinkDrop;
                            break;
                        }
                    }
                } else {
                    switch (dropAction) {
                        case COPY: {
                            cursor = java.awt.dnd.DragSource.DefaultCopyNoDrop;
                            break;
                        }

                        case MOVE: {
                            cursor = java.awt.dnd.DragSource.DefaultMoveNoDrop;
                            break;
                        }

                        case LINK: {
                            cursor = java.awt.dnd.DragSource.DefaultLinkNoDrop;
                            break;
                        }
                    }
                }
            }
        }

        displayHost.setCursor(cursor);
    }
}
