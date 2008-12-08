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

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.io.FileList;
import pivot.util.ImmutableIterator;
import pivot.wtk.Component.DecoratorSequence;
import pivot.wtk.effects.Decorator;
import pivot.wtk.media.Picture;

/**
 * Base class for application contexts.
 * <p>
 * TODO Fire events when entries are added to/removed from the cache?
 * <p>
 * TODO Provide a means of mapping common "actions" to keystrokes (e.g. "copy"
 * to Control-C or Command-C)
 *
 * @author gbrown
 */
public abstract class ApplicationContext {
    /**
     * Resource cache dictionary implementation.
     *
     * @author gbrown
     */
    public static final class ResourceCacheDictionary
        implements Dictionary<URL, Object>, Iterable<URL> {
        public Object get(URL key) {
            return resourceCache.get(key);
        }

        public Object put(URL key, Object value) {
            return resourceCache.put(key, value);
        }

        public Object remove(URL key) {
            return resourceCache.remove(key);
        }

        public boolean containsKey(URL key) {
            return resourceCache.containsKey(key);
        }

        public boolean isEmpty() {
            return resourceCache.isEmpty();
        }

        public Iterator<URL> iterator() {
            return new ImmutableIterator<URL>(resourceCache.iterator());
        }
    }

    /**
     * Native AWT display host.
     *
     * @author gbrown
     */
    public final class DisplayHost extends java.awt.Container {
        public static final long serialVersionUID = 0;

        private Component focusedComponent = null;

        private Point dragLocation = null;
        private DragSource dragSource = null;

        private Component dropDescendant = null;
        private DropAction dropAction = null;

        private DropTargetListener dropTargetListener = new DropTargetListener() {
            private Class<?> dragContentType = null;

            public void dragEnter(DropTargetDragEvent event) {
                // TODO
                if (dragSource != null) {
                    throw new IllegalStateException("Drag source already exists.");
                }

                dragSource = new NativeDragSource(event);
                dragContentType = getPreferredContentType(event.getTransferable());

                java.awt.Point location = event.getLocation();
                updateDropState(dragContentType, getDropAction(event.getDropAction()),
                    location.x, location.y, false);

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }
            }

            public void dragExit(DropTargetEvent event) {
                // Clear drag state
                dragSource = null;
                dragContentType = null;

                // Clear drop state
                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropTarget.hideDropState(dropDescendant);
                }

                dropDescendant = null;
                dropAction = null;
            }

            public void dragOver(DropTargetDragEvent event) {
                java.awt.Point location = event.getLocation();
                updateDropState(dragContentType, getDropAction(event.getDropAction()),
                    location.x, location.y, false);

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }
            }

            public void dropActionChanged(DropTargetDragEvent event) {
                java.awt.Point location = event.getLocation();
                updateDropState(dragContentType, getDropAction(event.getDropAction()),
                    location.x, location.y, false);

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }
            }

            public void drop(DropTargetDropEvent event) {
                if (dropAction == null) {
                    event.rejectDrop();
                } else {
                    event.acceptDrop(getNativeDropAction(dropAction));

                    DropTarget dropTarget = dropDescendant.getDropTarget();

                    Object dragContent = getPreferredContent(event.getTransferable());
                    java.awt.Point location = event.getLocation();

                    dropTarget.drop(dropDescendant, dragContent, dropAction, location.x, location.y);

                    event.dropComplete(true);
                }

                // Restore the cursor to the default
                setCursor(java.awt.Cursor.getDefaultCursor());

                // Clear drag state
                dragSource = null;
                dragContentType = null;

                // Clear drop state
                dropDescendant = null;
                dropAction = null;
            }
        };

        protected DisplayHost() {
            enableEvents(AWTEvent.COMPONENT_EVENT_MASK
                | AWTEvent.FOCUS_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);

            try {
                System.setProperty("sun.awt.noerasebackground", "true");
                System.setProperty("sun.awt.erasebackgroundonresize", "true");
            } catch (SecurityException exception) {
            }

            // Add native drop support
            new java.awt.dnd.DropTarget(this, dropTargetListener);

            setFocusTraversalKeysEnabled(false);
        }

        @Override
        public void repaint(int x, int y, int width, int height) {
            // Ensure that the repaint call is properly bounded (some
            // implementations of AWT do not properly clip the repaint call
            // when x or y is negative: the negative value is converted to 0,
            // but the width/height is not adjusted)
            if (x < 0) {
                width = Math.max(width + x, 0);
                x = 0;
            }

            if (y < 0) {
                height = Math.max(height + y, 0);
                y = 0;
            }

            if (width > 0
                && height > 0) {
                super.repaint(x, y, width, height);
            }
        }

        @Override
        public void paint(Graphics graphics) {
            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            ((Graphics2D)graphics).clip(new java.awt.Rectangle(0, 0, getWidth(), getHeight()));

            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            if (clipBounds != null
                && !clipBounds.isEmpty()) {
                try {
                    if (!paintVolatileBuffered((Graphics2D)graphics)) {
                        if (!paintBuffered((Graphics2D)graphics)) {
                            paintDisplay((Graphics2D)graphics);
                        }
                    }
                } catch (RuntimeException exception) {
                    System.out.println("Exception thrown during paint(): " + exception);
                    throw exception;
                }
            }
        }

        /**
         * Attempts to paint the display using an offscreen buffer.
         *
         * @param graphics
         * The source graphics context.
         *
         * @return
         * <tt>true</tt> if the display was painted using the offscreen
         * buffer; <tt>false</tt>, otherwise.
         */
        private boolean paintBuffered(Graphics2D graphics) {
            boolean painted = false;

            // Paint the display into an offscreen buffer
            GraphicsConfiguration gc = graphics.getDeviceConfiguration();
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            java.awt.image.BufferedImage bufferedImage =
                gc.createCompatibleImage(clipBounds.width, clipBounds.height,
                    Transparency.OPAQUE);

            if (bufferedImage != null) {
                Graphics2D bufferedImageGraphics = (Graphics2D)bufferedImage.getGraphics();
                bufferedImageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
                bufferedImageGraphics.translate(-clipBounds.x, -clipBounds.y);

                try {
                    paintDisplay(bufferedImageGraphics);
                    graphics.drawImage(bufferedImage, clipBounds.x, clipBounds.y, this);
                } finally {
                    bufferedImageGraphics.dispose();
                }

                painted = true;
            }

            return painted;
        }

        /**
         * Attempts to paint the display using a volatile offscreen buffer.
         *
         * @param graphics
         * The source graphics context.
         *
         * @return
         * <tt>true</tt> if the display was painted using the offscreen
         * buffer; <tt>false</tt>, otherwise.
         */
        private boolean paintVolatileBuffered(Graphics2D graphics) {
            boolean painted = false;

            // Paint the display into a volatile offscreen buffer
            GraphicsConfiguration gc = graphics.getDeviceConfiguration();
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            java.awt.image.VolatileImage volatileImage =
                gc.createCompatibleVolatileImage(clipBounds.width, clipBounds.height,
                    Transparency.OPAQUE);

            // If we have a valid volatile image, attempt to paint the
            // display to it
            if (volatileImage != null) {
                int valid = volatileImage.validate(getGraphicsConfiguration());

                if (valid == java.awt.image.VolatileImage.IMAGE_OK
                    || valid == java.awt.image.VolatileImage.IMAGE_RESTORED) {
                    Graphics2D volatileImageGraphics = (Graphics2D)volatileImage.getGraphics();
                    volatileImageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
                    volatileImageGraphics.translate(-clipBounds.x, -clipBounds.y);

                    try {
                        paintDisplay(volatileImageGraphics);
                        graphics.drawImage(volatileImage, clipBounds.x, clipBounds.y, this);
                    } finally {
                        volatileImageGraphics.dispose();
                    }

                    painted = !volatileImage.contentsLost();
                }
            }

            return painted;
        }

        /**
         * Paints the display including any decorators.
         *
         * @param graphics
         */
        private void paintDisplay(Graphics2D graphics) {
            Graphics2D decoratedGraphics = graphics;

            DecoratorSequence decorators = display.getDecorators();
            int n = decorators.getLength();
            for (int i = n - 1; i >= 0; i--) {
                Decorator decorator = decorators.get(i);
                decoratedGraphics = decorator.prepare(display, decoratedGraphics);
            }

            display.paint(graphics);

            for (int i = 0; i < n; i++) {
                Decorator decorator = decorators.get(i);
                decorator.update();
            }

            // Paint the drag visual
            if (dragSource != null) {
                Visual dragRepresentation = dragSource.getRepresentation();

                if (dragRepresentation != null) {
                    Point dragOffset = dragSource.getOffset();
                    int tx = dragLocation.x - dragOffset.x;
                    int ty = dragLocation.y - dragOffset.y;

                    graphics.translate(tx, ty);
                    dragRepresentation.paint(graphics);
                }
            }
        }

        private void repaintDragRepresentation() {
            Visual dragRepresentation = dragSource.getRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = dragSource.getOffset();

                repaint(dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());
            }
        }

        private void updateDropState(Class<?> dragContentType, DropAction userDropAction,
            int x, int y, boolean updateDropCursor) {
            // Update the drop descendant
            Component previousDropDescendant = dropDescendant;
            dropDescendant = display.getDescendantAt(x, y);
            dropAction = null;

            if (dropDescendant != null) {
                // Determine the supported drop actions
                int supportedDropActions = dragSource.getSupportedDropActions();

                // Map the location to the descendant's coordinate system
                Point dropLocation = dropDescendant.mapPointFromAncestor(display, x, y);

                DropTarget dropTarget = null;

                while (dropDescendant != null
                    && dropTarget == null) {
                    dropTarget = dropDescendant.getDropTarget();

                    if (dropTarget != null) {
                        DropAction dropAction = dropTarget.getDropAction(dropDescendant,
                            dragContentType, supportedDropActions, userDropAction,
                            dropLocation.x, dropLocation.y);

                        if (dropAction == null
                            || !dropAction.isSelected(supportedDropActions)) {
                            // The drop target rejected the drop or returned an invalid
                            // drop action
                            dropTarget = null;
                        } else {
                            this.dropAction = dropAction;
                        }
                    }

                    if (dropTarget == null) {
                        // A drop target has not been found; keep walking
                        // up the tree
                        dropLocation.x += dropDescendant.getX();
                        dropLocation.y += dropDescendant.getY();

                        dropDescendant = dropDescendant.getParent();
                    }
                }
            }

            if (previousDropDescendant == dropDescendant) {
                // The descendant hasn't changed
                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropTarget.updateDropState(dropDescendant, dropAction, x, y);
                }
            } else {
                // The drop descendant changed as a result of this move
                if (previousDropDescendant != null) {
                    DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                    previousDropTarget.hideDropState(previousDropDescendant);
                }

                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropTarget.showDropState(dropDescendant, dragContentType, dropAction);
                }

                if (updateDropCursor) {
                    setCursor(getDropCursor(dropAction));
                }
            }
        }

        private void startNativeDrag(final DragSource dragSource, final MouseEvent mouseEvent) {
            java.awt.dnd.DragSource awtDragSource = java.awt.dnd.DragSource.getDefaultDragSource();

            final int supportedDropActions = dragSource.getSupportedDropActions();

            DragGestureRecognizer dragGestureRecognizer =
                new DragGestureRecognizer(java.awt.dnd.DragSource.getDefaultDragSource(), displayHost) {
                private static final long serialVersionUID = 0;

                {   appendEvent(mouseEvent);
                }

                public int getSourceActions() {
                    int awtSourceActions = 0;

                    if (DropAction.COPY.isSelected(supportedDropActions)) {
                        awtSourceActions |= DnDConstants.ACTION_COPY;
                    }

                    if (DropAction.MOVE.isSelected(supportedDropActions)) {
                        awtSourceActions |= DnDConstants.ACTION_MOVE;
                    }

                    if (DropAction.LINK.isSelected(supportedDropActions)) {
                        awtSourceActions |= DnDConstants.ACTION_LINK;
                    }

                    return awtSourceActions;
                }

                protected void registerListeners() {
                    // No-op
                }

                protected void unregisterListeners() {
                    // No-op
                }
            };

            java.util.List<InputEvent> inputEvents = new java.util.ArrayList<InputEvent>();
            inputEvents.add(mouseEvent);

            // TODO If current user drop action is supported by drag source, use it
            // as initial action - otherwise, select MOVE, COPY, LINK in that order
            java.awt.Point location = new java.awt.Point(mouseEvent.getX(), mouseEvent.getY());
            DragGestureEvent trigger = new DragGestureEvent(dragGestureRecognizer,
                DnDConstants.ACTION_MOVE, location, inputEvents);

            Transferable transferable = new Clipboard.TransferableContent(dragSource.getContent());
            awtDragSource.startDrag(trigger, java.awt.Cursor.getDefaultCursor(),
                null, null, transferable, new DragSourceListener() {
                public void dragEnter(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                public void dragExit(DragSourceEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(java.awt.Cursor.getDefaultCursor());
                }

                public void dragOver(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                public void dropActionChanged(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                public void dragDropEnd(DragSourceDropEvent event) {
                    dragSource.endDrag(getDropAction(event.getDropAction()));
                }
            });
        }

        @Override
        protected void processComponentEvent(ComponentEvent event) {
            super.processComponentEvent(event);

            switch (event.getID()) {
                case ComponentEvent.COMPONENT_RESIZED: {
                    display.setSize(getWidth(), getHeight());
                    break;
                }

                case ComponentEvent.COMPONENT_MOVED: {
                    // No-op
                    break;
                }

                case ComponentEvent.COMPONENT_SHOWN: {
                    // No-op
                    break;
                }

                case ComponentEvent.COMPONENT_HIDDEN: {
                    // No-op
                    break;
                }
            }
        }

        @Override
        protected void processFocusEvent(FocusEvent event) {
            super.processFocusEvent(event);

            switch(event.getID()) {
                case FocusEvent.FOCUS_GAINED: {
                    if (focusedComponent != null
                        && focusedComponent.isShowing()
                        && !focusedComponent.isBlocked()) {
                        focusedComponent.requestFocus(true);
                    }

                    break;
                }

                case FocusEvent.FOCUS_LOST: {
                    focusedComponent = Component.getFocusedComponent();
                    Component.clearFocus(true);

                    break;
                }
            }
        }

        @Override
        protected void processMouseEvent(MouseEvent event) {
            super.processMouseEvent(event);

            // Get the event coordinates
            int x = event.getX();
            int y = event.getY();

            // Set the mouse state
            Mouse.setLocation(x, y);
            Mouse.setModifiersEx(event.getModifiersEx());

            // Get the button associated with this event
            Mouse.Button button = null;
            switch (event.getButton()) {
                case MouseEvent.BUTTON1: {
                    button = Mouse.Button.LEFT;
                    break;
                }

                case MouseEvent.BUTTON2: {
                    button = Mouse.Button.MIDDLE;
                    break;
                }

                case MouseEvent.BUTTON3: {
                    button = Mouse.Button.RIGHT;
                    break;
                }
            }

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_PRESSED: {
                    requestFocus();
                    dragLocation = new Point(x, y);
                    display.mouseDown(button, x, y);
                    break;
                }

                case MouseEvent.MOUSE_RELEASED: {
                    if (dragSource == null) {
                        display.mouseUp(button, x, y);
                    } else {
                        if (dropDescendant == null) {
                            dragSource.endDrag(null);
                        } else {
                            DropTarget dropTarget = dropDescendant.getDropTarget();
                            Object dragContent = dragSource.getContent();

                            dropTarget.drop(dropDescendant, dragContent, dropAction, x, y);
                            dragSource.endDrag(dropAction);
                        }

                        repaintDragRepresentation();

                        setCursor(java.awt.Cursor.getDefaultCursor());

                        // Clear the drag state
                        dragSource = null;

                        // Clear the drop state
                        dropDescendant = null;
                        dropAction = null;
                    }

                    // Clear the drag location
                    dragLocation = null;

                    break;
                }

                case MouseEvent.MOUSE_ENTERED: {
                    display.mouseOver();
                    break;
                }

                case MouseEvent.MOUSE_EXITED: {
                    display.mouseOut();
                    break;
                }
            }
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent event) {
            super.processMouseMotionEvent(event);

            // Get the event coordinates
            int x = event.getX();
            int y = event.getY();

            // Set the mouse state
            Mouse.setLocation(x, y);

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED: {
                    if (dragSource == null) {
                        // A drag is not active, so propagate the event to the display
                        display.mouseMove(x, y);

                        int dragThreshold = getDragThreshold();

                        if (dragLocation != null
                            && (Math.abs(x - dragLocation.x) > dragThreshold
                                || Math.abs(y - dragLocation.y) > dragThreshold)) {
                            // The user has dragged the mouse past the drag threshold; try
                            // to find a drag source
                            Component descendant = display.getDescendantAt(dragLocation.x,
                                dragLocation.y);

                            if (descendant != null) {
                                Point descendantDragLocation = descendant.mapPointFromAncestor(display,
                                    dragLocation.x, dragLocation.y);

                                while (descendant != null
                                    && dragSource == null) {
                                    dragSource = descendant.getDragSource();

                                    if (dragSource != null
                                        && !dragSource.beginDrag(descendant, descendantDragLocation.x,
                                            descendantDragLocation.y)) {
                                        // The drag source rejected the drag
                                        dragSource = null;
                                    }

                                    if (dragSource == null) {
                                        // A drag source has not been found; keep walking
                                        // up the tree
                                        descendantDragLocation.x += descendant.getX();
                                        descendantDragLocation.y += descendant.getY();

                                        descendant = descendant.getParent();
                                    }
                                }

                                if (dragSource == null) {
                                    // There was nothing to drag, so clear the drag location
                                    dragLocation = null;
                                } else {
                                    if (dragSource.isNative()) {
                                        startNativeDrag(dragSource, event);

                                        // Clear the drag source, since we may need to create a new
                                        // one wrapping the native drag; also clear the drag location
                                        dragSource = null;
                                        dragLocation = null;
                                    } else {
                                        // A drag has started
                                        if (dragSource.getRepresentation() != null
                                            && dragSource.getOffset() == null) {
                                            throw new IllegalStateException("Drag offset is required when a "
                                                + " respresentation is specified.");
                                        }

                                        if (display.isMouseOver()) {
                                            display.mouseOut();
                                        }

                                        // Update the drop state
                                        Object dragContent = dragSource.getContent();
                                        updateDropState(dragContent.getClass(), getUserDropAction(event),
                                            x, y, true);

                                        // Repaint the drag visual
                                        dragLocation.x = x;
                                        dragLocation.y = y;

                                        repaintDragRepresentation();
                                    }
                                }
                            }
                        }
                    } else {
                        if (dragLocation != null) {
                            // Update the drop state
                            Object dragContent = dragSource.getContent();
                            updateDropState(dragContent.getClass(), getUserDropAction(event),
                                x, y, true);

                            // Repaint the drag visual
                            repaintDragRepresentation();

                            dragLocation.x = x;
                            dragLocation.y = y;

                            repaintDragRepresentation();
                        }
                    }

                    break;
                }
            }
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent event) {
            super.processMouseWheelEvent(event);

            // Get the event coordinates
            int x = event.getX();
            int y = event.getY();

            // Get the scroll type
            Mouse.ScrollType scrollType = null;
            switch (event.getScrollType()) {
                case MouseWheelEvent.WHEEL_BLOCK_SCROLL: {
                    scrollType = Mouse.ScrollType.BLOCK;
                    break;
                }

                case MouseWheelEvent.WHEEL_UNIT_SCROLL: {
                    scrollType = Mouse.ScrollType.UNIT;
                    break;
                }
            }

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_WHEEL: {
                    if (dragSource == null) {
                        display.mouseWheel(scrollType, event.getScrollAmount(),
                            event.getWheelRotation(), x, y);
                    }
                    break;
                }
            }
        }

        @Override
        protected void processKeyEvent(KeyEvent event) {
            super.processKeyEvent(event);

            // Set the keyboard state
            Keyboard.setModifiersEx(event.getModifiersEx());

            // Get the key location
            Keyboard.KeyLocation keyLocation = null;
            switch (event.getKeyLocation()) {
                case KeyEvent.KEY_LOCATION_STANDARD: {
                    keyLocation = Keyboard.KeyLocation.STANDARD;
                    break;
                }

                case KeyEvent.KEY_LOCATION_LEFT: {
                    keyLocation = Keyboard.KeyLocation.LEFT;
                    break;
                }

                case KeyEvent.KEY_LOCATION_RIGHT: {
                    keyLocation = Keyboard.KeyLocation.RIGHT;
                    break;
                }

                case KeyEvent.KEY_LOCATION_NUMPAD: {
                    keyLocation = Keyboard.KeyLocation.KEYPAD;
                    break;
                }
            }

            if (dragSource == null) {
                // Process the event
                Component focusedComponent = Component.getFocusedComponent();

                switch (event.getID()) {
                    case KeyEvent.KEY_PRESSED: {
                        int keyCode = event.getKeyCode();
                        boolean consumed = false;

                        if (focusedComponent != null) {
                            consumed = focusedComponent.keyPressed(keyCode, keyLocation);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }

                    case KeyEvent.KEY_RELEASED: {
                        int keyCode = event.getKeyCode();
                        boolean consumed = false;

                        if (focusedComponent != null) {
                            consumed = focusedComponent.keyReleased(keyCode, keyLocation);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }

                    case KeyEvent.KEY_TYPED: {
                        boolean consumed = false;

                        if (focusedComponent != null) {
                            char keyChar = event.getKeyChar();
                            consumed = focusedComponent.keyTyped(keyChar);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }
                }
            } else {
                // Update the drop state
                Object dragContent = dragSource.getContent();
                updateDropState(dragContent.getClass(), getUserDropAction(event),
                    Mouse.getX(), Mouse.getY(), true);
            }
        }
    }

    private static class IntervalTask extends TimerTask {
        private Runnable runnable = null;

        public IntervalTask(Runnable runnable) {
            this.runnable = runnable;
        }

        public void run() {
            queueCallback(runnable);
        }
    }

    private static class TimeoutTask extends TimerTask {
        private Runnable runnable = null;
        int timeoutID = -1;

        public TimeoutTask(Runnable runnable, int timeoutID) {
            this.runnable = runnable;
            this.timeoutID = timeoutID;
        }

        public void run() {
            queueCallback(runnable, true);
            clearTimeout(timeoutID);
        }
    }

    private static class NativeDragSource implements DragSource {
        private int supportedDropActions;

        public NativeDragSource(DropTargetDragEvent event) {
            int awtSourceActions = event.getSourceActions();

            if ((awtSourceActions & DnDConstants.ACTION_COPY) > 0) {
                supportedDropActions |= DropAction.COPY.getMask();
            }

            if ((awtSourceActions & DnDConstants.ACTION_MOVE) > 0) {
                supportedDropActions |= DropAction.MOVE.getMask();
            }

            if ((awtSourceActions & DnDConstants.ACTION_LINK) > 0) {
                supportedDropActions |= DropAction.LINK.getMask();
            }
        }

        public boolean beginDrag(Component component, int x, int y) {
            throw new UnsupportedOperationException();
        }

        public void endDrag(DropAction dropAction) {
            throw new UnsupportedOperationException();
        }

        public boolean isNative() {
            throw new UnsupportedOperationException();
        }

        public Object getContent() {
            throw new UnsupportedOperationException();
        }

        public Visual getRepresentation() {
            return null;
        }

        public Point getOffset() {
            return null;
        }

        public int getSupportedDropActions() {
            return supportedDropActions;
        }
    }

    private DisplayHost displayHost;
    private Display display;

    protected static URL origin = null;

    private static ThreadLocal<ApplicationContext> applicationContext;

    private static HashMap<URL, Object> resourceCache = new HashMap<URL, Object>();
    private static ResourceCacheDictionary resourceCacheDictionary = new ResourceCacheDictionary();

    private static Timer timer = new Timer(true);
    private static HashMap<Integer, TimerTask> timerTaskMap = new HashMap<Integer, TimerTask>();
    private static int nextTimerTaskID = 0;

    private static Object textAntialiasingHint = null;

    private static final int DEFAULT_MULTI_CLICK_INTERVAL = 400;
    private static final int DEFAULT_CURSOR_BLINK_RATE = 600;
    private static final String DEFAULT_THEME_CLASS_NAME = "pivot.wtk.skin.terra.TerraTheme";

    protected ApplicationContext() {
        assert (applicationContext == null);

        applicationContext = new ThreadLocal<ApplicationContext>() {
            protected synchronized ApplicationContext initialValue() {
                return ApplicationContext.this;
            }
        };

        // Create the display host and display
        displayHost = new DisplayHost();
        display = new Display(this);

        try {
            // Load and instantiate the default theme, if possible
            Class<?> themeClass = Class.forName(DEFAULT_THEME_CLASS_NAME);
            Theme.setTheme((Theme)themeClass.newInstance());
        } catch (Exception exception) {
            // No-op; assume that a custom theme will be installed later
            // by the caller
            System.out.println("Warning: Unable to load default theme.");
        }
    }

    public DisplayHost getDisplayHost() {
        return displayHost;
    }

    public Display getDisplay() {
        return display;
    }

    protected void repaint(int x, int y, int width, int height) {
        if (displayHost != null) {
            displayHost.repaint(x, y, width, height);
        }
    }

    protected Graphics2D getGraphics() {
        Graphics2D graphics = null;

        if (displayHost != null) {
            graphics = (Graphics2D)displayHost.getGraphics();
        }

        return graphics;
    }

    protected abstract void contextOpen(URL location, String target);

    public static ApplicationContext getApplicationContext() {
        return applicationContext.get();
    }

    /**
     * Returns this application's origin (the URL of it's originating server).
     */
    public static URL getOrigin() {
        return origin;
    }

    /**
     * Resource properties accessor.
     */
    public static ResourceCacheDictionary getResourceCache() {
        return resourceCacheDictionary;
    }

    /**
     * Opens the resource at the given location.
     *
     * @param location
     */
    public static void open(URL location) {
        open(location, null);
    }

    /**
     * Opens the resource at the given location.
     *
     * @param location
     * @param target
     */
    public static void open(URL location, String target) {
        applicationContext.get().contextOpen(location, target);
    }

    /**
     * Issues a system alert sound.
     */
    public static void beep() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread.
     *
     * @param runnable
     * The task to execute.
     *
     * @param period
     * The interval at which the task should be executed.
     *
     * @return An integer ID that can be used to cancel execution of the task.
     */
    public static int setInterval(Runnable runnable, long period) {
        int intervalID = nextTimerTaskID++;

        IntervalTask intervalTask = new IntervalTask(runnable);
        timerTaskMap.put(intervalID, intervalTask);

        try {
            timer.schedule(intervalTask, 0, period);
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());

            // TODO This is a workaround for an apparent bug in the Mac OSX
            // Java Plugin, which appears to prematurely kill the timer thread.
            // Remove this when the issue is fixed.
            timer = new Timer(true);
            timerTaskMap.clear();
            timerTaskMap.put(intervalID, intervalTask);
            timer.schedule(intervalTask, 0, period);
        }

        return intervalID;
    }

    /**
     * Cancels execution of a scheduled task.
     *
     * @param intervalID
     * The ID of the task to cancel.
     */
    public static void clearInterval(int intervalID) {
        clearTimerTask(intervalID);
    }

    /**
     * Schedules a task for execution after an elapsed time.
     *
     * @param runnable
     * The task to execute.
     *
     * @param timeout
     * The time after which the task should begin executing.
     */
    public static int setTimeout(Runnable runnable, long timeout) {
        int timeoutID = nextTimerTaskID++;

        TimeoutTask timeoutTask = new TimeoutTask(runnable, timeoutID);
        timerTaskMap.put(timeoutID, timeoutTask);

        try {
            timer.schedule(timeoutTask, timeout);
        } catch (IllegalStateException exception) {
            System.out.println(exception.getMessage());

            // TODO This is a workaround for an apparent bug in the Mac OSX
            // Java Plugin, which appears to prematurely kill the timer thread.
            // Remove this when the issue is fixed.
            timer = new Timer(true);
            timerTaskMap.clear();
            timerTaskMap.put(timeoutID, timeoutTask);
            timer.schedule(timeoutTask, timeout);
        }

        return timeoutID;
    }

    /**
     * Cancels execution of a scheduled task.
     *
     * @param timeoutID
     * The ID of the task to cancel.
     */
    public static void clearTimeout(int timeoutID) {
        clearTimerTask(timeoutID);
    }

    /**
     * Cancels execution of a timer task.
     *
     * @param timerTaskID
     */
    private static void clearTimerTask(int timerTaskID) {
        TimerTask timerTask = timerTaskMap.remove(timerTaskID);
        if (timerTask != null) {
            timerTask.cancel();
        }
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * returns without waiting for the task to complete.
     *
     * @param runnable
     * The runnable to execute.
     */
    public static void queueCallback(Runnable runnable) {
        queueCallback(runnable, false);
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * optionally waits for the task to complete.
     *
     * @param runnable
     * The runnable to execute.
     *
     * @param wait
     * If <tt>true</tt>, does not return until the runnable has executed.
     * Otherwise, returns immediately.
     */
    public static void queueCallback(Runnable runnable, boolean wait) {
        if (wait) {
            try {
                java.awt.EventQueue.invokeAndWait(runnable);
            } catch (InvocationTargetException exception) {
            } catch (InterruptedException exception) {
            }
        } else {
            java.awt.EventQueue.invokeLater(runnable);
        }
    }

    /**
     * Returns the system text anti-aliasing hint.
     */
    public static Object getTextAntialiasingHint() {
        if (textAntialiasingHint == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            java.util.Map<?, ?> fontDesktopHints =
                (java.util.Map<?, ?>)toolkit.getDesktopProperty("awt.font.desktophints");

            if (fontDesktopHints == null) {
                textAntialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            } else {
                textAntialiasingHint = fontDesktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
                if (textAntialiasingHint.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)) {
                    textAntialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                }

                // Listen for changes to the property
                toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        ApplicationContext.textAntialiasingHint = null;
                    }
                });
            }
        }

        return textAntialiasingHint;
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
     * Returns the system cursor blink rate.
     */
    public static int getCursorBlinkRate() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer cursorBlinkRate = (Integer)toolkit.getDesktopProperty("awt.cursorBlinkRate");

        if (cursorBlinkRate == null) {
            cursorBlinkRate = DEFAULT_CURSOR_BLINK_RATE;
        }

        return cursorBlinkRate;
    }

    /**
     * Returns the system drag threshold.
     */
    public static int getDragThreshold() {
        return java.awt.dnd.DragSource.getDragThreshold();
    }

    /**
     * Selects the most appropriate content from a given transferable object.
     *
     * @param transferable
     *
     * @return
     * The preferred content value, or <tt>null</tt> if the transferable
     * object did not contain any usable data.
     */
    @SuppressWarnings("unchecked")
    protected static Object getPreferredContent(Transferable transferable) {
        Object content = null;

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();

        int i = 0;
        int n = transferDataFlavors.length;
        while (i < n
            && content == null) {
            DataFlavor dataFlavor = transferDataFlavors[i++];

            if (dataFlavor.isMimeTypeEqual(DataFlavor.stringFlavor)) {
                try {
                    content = (String)transferable.getTransferData(dataFlavor);
                } catch(Exception exception) {
                    // No-op
                }
            } else if (dataFlavor.isMimeTypeEqual(DataFlavor.imageFlavor)) {
                try {
                    content = new Picture((BufferedImage)transferable.getTransferData(dataFlavor));
                } catch(Exception exception) {
                    // No-op
                }
            } else {
                if (dataFlavor.isMimeTypeEqual(DataFlavor.javaFileListFlavor)) {
                    try {
                        content = new FileList((java.util.List<File>)transferable.getTransferData(dataFlavor));
                    } catch(Exception exception) {
                        // No-op
                    }
                }
            }
        }

        if (content == null
            && transferDataFlavors.length > 0) {
            try {
                content = transferable.getTransferData(transferDataFlavors[0]).toString();
            } catch(Exception exception) {
                // No-op
            }
        }

        return content;
    }

    /**
     * Selects the most appropriate content type from a given transferable
     * object.
     *
     * @param transferable
     *
     * @return
     * The preferred content type, or <tt>null</tt> if the transferable
     * object did not contain any usable data.
     */
    protected static Class<?> getPreferredContentType(Transferable transferable) {
        Class<?> contentType = null;

        DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();

        int i = 0;
        int n = transferDataFlavors.length;
        while (i < n
            && contentType == null) {
            DataFlavor dataFlavor = transferDataFlavors[i++];

            if (dataFlavor.isMimeTypeEqual(DataFlavor.stringFlavor)) {
                contentType = String.class;
            } else if (dataFlavor.isMimeTypeEqual(DataFlavor.imageFlavor)) {
                contentType = Picture.class;
            } else {
                if (dataFlavor.isMimeTypeEqual(DataFlavor.javaFileListFlavor)) {
                    contentType = FileList.class;
                }
            }
        }

        if (contentType == null) {
            contentType = String.class;
        }

        return contentType;
    }

    private static DropAction getUserDropAction(InputEvent event) {
        DropAction userDropAction;

        if ((event.isControlDown() && event.isShiftDown())
            || (event.isAltDown() && event.isMetaDown())) {
            userDropAction = DropAction.LINK;
        } else if (event.isControlDown()
            || (event.isAltDown())) {
            userDropAction = DropAction.COPY;
        } else if (event.isShiftDown()){
            userDropAction = DropAction.MOVE;
        } else {
            userDropAction = null;
        }

        return userDropAction;
    }

    private static DropAction getDropAction(int nativeDropAction) {
        DropAction dropAction = null;

        switch (nativeDropAction) {
            case DnDConstants.ACTION_COPY: {
                dropAction = DropAction.COPY;
                break;
            }

            case DnDConstants.ACTION_MOVE: {
                dropAction = DropAction.MOVE;
                break;
            }

            case DnDConstants.ACTION_LINK: {
                dropAction = DropAction.LINK;
                break;
            }
        }

        return dropAction;
    }

    private static int getNativeDropAction(DropAction dropAction) {
        int nativeDropAction = 0;

        if (dropAction != null) {
            switch(dropAction) {
                case COPY: {
                    nativeDropAction = DnDConstants.ACTION_COPY;
                    break;
                }

                case MOVE: {
                    nativeDropAction = DnDConstants.ACTION_MOVE;
                    break;
                }

                case LINK: {
                    nativeDropAction = DnDConstants.ACTION_LINK;
                    break;
                }
            }
        }

        return nativeDropAction;
    }

    private static java.awt.Cursor getDropCursor(DropAction dropAction) {
        // Update the drop cursor
        java.awt.Cursor cursor = java.awt.Cursor.getDefaultCursor();

        if (dropAction != null) {
            // Show the cursor for the drop action returned by the
            // drop target
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
        }

        return cursor;
    }
}
