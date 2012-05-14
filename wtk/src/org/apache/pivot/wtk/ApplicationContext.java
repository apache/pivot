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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.PrintGraphics;
import java.awt.RenderingHints;
import java.awt.Transparency;
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
import java.awt.image.VolatileImage;
import java.awt.print.PrinterGraphics;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Version;
import org.apache.pivot.wtk.Component.DecoratorSequence;
import org.apache.pivot.wtk.effects.Decorator;
import org.apache.pivot.wtk.effects.ShadeDecorator;

/**
 * Base class for application contexts.
 */
public abstract class ApplicationContext {
    /**
     * Native display host.
     */
    public static class DisplayHost extends java.awt.Component {
        private static final long serialVersionUID = -815713849595314026L;

        private transient Display display = new Display(this);
        private AWTEvent currentAWTEvent = null;

        private Component focusedComponent = null;

        private Point dragLocation = null;
        private Component dragDescendant = null;
        private transient Manifest dragManifest = null;
        private DropAction userDropAction = null;
        private Component dropDescendant = null;

        private MenuPopup menuPopup = null;

        private double scale = 1;

        private boolean debugPaint = false;

        private boolean bufferedImagePaintEnabled = true;
        private BufferedImage bufferedImage = null;
        private GraphicsConfiguration bufferedImageGC = null;

        private boolean volatileImagePaintEnabled = true;
        private VolatileImage volatileImage = null;
        private GraphicsConfiguration volatileImageGC = null;

        private Random random = null;

        private transient DropTargetListener dropTargetListener = new DropTargetListener() {
            @Override
            public void dragEnter(DropTargetDragEvent event) {
                if (dragDescendant != null) {
                    throw new IllegalStateException("Local drag already in progress.");
                }

                java.awt.Point location = event.getLocation();
                dragLocation = new Point(location.x, location.y);

                // Initialize drag state
                dragManifest = new RemoteManifest(event.getTransferable());

                // Initialize drop state
                userDropAction = getDropAction(event.getDropAction());

                // Notify drop target
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                        getSupportedDropActions(event.getSourceActions()), userDropAction);
                }

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }

                display.validate();
            }

            @Override
            public void dragExit(DropTargetEvent event) {
                // Clear drag location and state
                dragLocation = null;
                dragManifest = null;

                // Clear drop state
                userDropAction = null;

                if (dropDescendant != null) {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropTarget.dragExit(dropDescendant);
                }

                dropDescendant = null;

                display.validate();
            }

            @Override
            public void dragOver(DropTargetDragEvent event) {
                java.awt.Point location = event.getLocation();

                // Get the previous and current drop descendant and call
                // move or exit/enter as appropriate
                Component previousDropDescendant = dropDescendant;
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (previousDropDescendant == dropDescendant) {
                    if (dropDescendant != null) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();

                        Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                            location.x, location.y);
                        dropAction = dropTarget.dragMove(dropDescendant, dragManifest,
                            getSupportedDropActions(event.getSourceActions()),
                            dropLocation.x, dropLocation.y, userDropAction);
                    }
                } else {
                    if (previousDropDescendant != null) {
                        DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                        previousDropTarget.dragExit(previousDropDescendant);
                    }

                    if (dropDescendant != null) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();
                        dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                            getSupportedDropActions(event.getSourceActions()),
                            userDropAction);
                    }
                }

                // Update cursor
                setCursor(getDropCursor(dropAction));

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }

                display.validate();
            }

            @Override
            public void dropActionChanged(DropTargetDragEvent event) {
                userDropAction = getDropAction(event.getDropAction());

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    java.awt.Point location = event.getLocation();
                    Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                        location.x, location.y);

                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropAction = dropTarget.userDropActionChange(dropDescendant, dragManifest,
                        getSupportedDropActions(event.getSourceActions()), dropLocation.x, dropLocation.y,
                        userDropAction);
                }

                if (dropAction == null) {
                    event.rejectDrag();
                } else {
                    event.acceptDrag(getNativeDropAction(dropAction));
                }

                display.validate();
            }

            @Override
            public void drop(DropTargetDropEvent event) {
                java.awt.Point location = event.getLocation();
                dropDescendant = getDropDescendant(location.x, location.y);

                DropAction dropAction = null;

                if (dropDescendant != null) {
                    Point dropLocation = dropDescendant.mapPointFromAncestor(display,
                        location.x, location.y);
                    DropTarget dropTarget = dropDescendant.getDropTarget();

                    // Simulate a user drop action change to get the current drop action
                    int supportedDropActions = getSupportedDropActions(event.getSourceActions());

                    dropAction = dropTarget.userDropActionChange(dropDescendant, dragManifest,
                        supportedDropActions, dropLocation.x, dropLocation.y, userDropAction);

                    if (dropAction != null) {
                        // Perform the drop
                        event.acceptDrop(getNativeDropAction(dropAction));
                        dropTarget.drop(dropDescendant, dragManifest,
                            supportedDropActions, dropLocation.x, dropLocation.y, userDropAction);
                    }
                }

                if (dropAction == null) {
                    event.rejectDrop();
                }

                event.dropComplete(true);

                // Restore the cursor to the default
                setCursor(java.awt.Cursor.getDefaultCursor());

                // Clear drag state
                dragManifest = null;
                dragLocation = null;

                // Clear drop state
                dropDescendant = null;

                display.validate();
            }
        };

        public DisplayHost() {
            enableEvents(AWTEvent.COMPONENT_EVENT_MASK
                | AWTEvent.FOCUS_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);

            try {
                System.setProperty("sun.awt.noerasebackground", "true");
                System.setProperty("sun.awt.erasebackgroundonresize", "false");
            } catch (SecurityException exception) {
                // No-op
            }

            try {
                if (Boolean.getBoolean("org.apache.pivot.wtk.disablevolatilebuffer")) {
                    volatileImagePaintEnabled = false;
                }
            } catch (SecurityException ex) {
                // No-op
            }

            try {
                debugPaint = Boolean.getBoolean("org.apache.pivot.wtk.debugpaint");
                if (debugPaint == true) {
                    random = new Random();
                }
            } catch (SecurityException ex) {
                // No-op
            }

            try {
                boolean debugFocus = Boolean.getBoolean("org.apache.pivot.wtk.debugfocus");

                if (debugFocus) {
                    final Decorator focusDecorator = new ShadeDecorator(0.2f, Color.RED);

                    ComponentClassListener focusChangeListener = new ComponentClassListener() {
                        @Override
                        public void focusedComponentChanged(Component previousFocusedComponent) {
                            if (previousFocusedComponent != null
                                && previousFocusedComponent.getDecorators().indexOf(focusDecorator) > -1) {
                                previousFocusedComponent.getDecorators().remove(focusDecorator);
                            }

                            Component focusedComponentLocal = Component.getFocusedComponent();
                            if (focusedComponentLocal != null
                                && focusedComponentLocal.getDecorators().indexOf(focusDecorator) == -1) {
                                focusedComponentLocal.getDecorators().add(focusDecorator);
                            }

                            System.out.println("focusedComponentChanged():\n  from = " + previousFocusedComponent
                                + "\n  to = " + focusedComponentLocal);
                        }
                    };

                    Component.getComponentClassListeners().add(focusChangeListener);
                }
            } catch (SecurityException ex) {
                // No-op
            }

            // Add native drop support
            @SuppressWarnings("unused")
            java.awt.dnd.DropTarget dropTarget = new java.awt.dnd.DropTarget(this, dropTargetListener);

            setFocusTraversalKeysEnabled(false);
        }

        public Display getDisplay() {
            return display;
        }

        public AWTEvent getCurrentAWTEvent() {
            return currentAWTEvent;
        }

        public double getScale() {
            return scale;
        }

        public void setScale(double scale) {
            if (scale != this.scale) {
                this.scale = scale;
                display.setSize(Math.max((int)Math.ceil(getWidth() / scale), 0),
                    Math.max((int)Math.ceil(getHeight() / scale), 0));
                display.repaint();
            }
        }

        public void scaleUp() {
            double newScale;

            if (scale < 1) {
                newScale = 1;
            } else if (scale < 1.25) {
                newScale = 1.25;
            } else if (scale < 1.5) {
                newScale = 1.5;
            } else if (scale < 2) {
                newScale = 2;
            } else {
                newScale = Math.min(Math.floor(scale) + 1, 12);
            }

            setScale(newScale);
        }

        public void scaleDown() {
            double newScale;

            if (scale <= 1.25) {
                newScale = 1;
            } else if (scale <= 1.5) {
                newScale = 1.25;
            } else if (scale <= 2) {
                newScale = 1.5;
            } else {
                newScale = Math.ceil(scale) - 1;
            }

            setScale(newScale);
        }

        /**
         * Under some conditions, e.g. running under Linux in an applet, volatile buffering
         * can reduce performance.
         */
        public void setVolatileImagePaintEnabled(boolean enabled) {
            volatileImagePaintEnabled = enabled;
            if (enabled) {
                bufferedImage = null;
                bufferedImageGC = null;
            }
            else {
                volatileImage = null;
                volatileImageGC = null;
            }
        }

        public void setBufferedImagePaintEnabled(boolean enabled) {
            bufferedImagePaintEnabled = enabled;
            if (!enabled) {
                bufferedImage = null;
                bufferedImageGC = null;
            }
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
                if (scale == 1) {
                    super.repaint(x, y, width, height);
                } else {
                    super.repaint((int)Math.floor(x * scale), (int)Math.floor(y * scale),
                        (int)Math.ceil(width * scale) + 1, (int)Math.ceil(height * scale) + 1);
                }
            }
        }

        @Override
        public void paint(Graphics graphics) {
            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            graphics.clipRect(0, 0, getWidth(), getHeight());

            if (graphics instanceof PrintGraphics || graphics instanceof PrinterGraphics) {
                print(graphics);
                return;
            }

            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            if (clipBounds != null
                && !clipBounds.isEmpty()) {
                try {
                    boolean bPaintSuccess = false;
                    if (volatileImagePaintEnabled) {
                        bPaintSuccess = paintVolatileBuffered((Graphics2D)graphics);
                    }
                    if (!bPaintSuccess && bufferedImagePaintEnabled) {
                        bPaintSuccess = paintBuffered((Graphics2D)graphics);
                    }
                    if (!bPaintSuccess) {
                        paintDisplay((Graphics2D)graphics);
                    }

                    if (debugPaint) {
                        graphics.setColor(new java.awt.Color(random.nextInt(256),
                            random.nextInt(256), random.nextInt(256), 75));
                        graphics.fillRect(0, 0, getWidth(), getHeight());
                    }
                } catch (RuntimeException exception) {
                    System.err.println("Exception thrown during paint(): " + exception);
                    throw exception;
                }
            }
        }

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }

        @Override
        public void print(Graphics graphics) {
            // TODO: verify if/how we have to re-scale output in this case ...

            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            graphics.clipRect(0, 0, getWidth(), getHeight());

            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            if (clipBounds != null
                    && !clipBounds.isEmpty()) {
                try {
                    // When printing, there is no point in using offscreen buffers.
                    paintDisplay((Graphics2D)graphics);
                } catch (RuntimeException exception) {
                    System.err.println("Exception thrown during print(): " + exception);
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

            if (bufferedImage == null
                || bufferedImageGC != gc
                || bufferedImage.getWidth() < clipBounds.width
                || bufferedImage.getHeight() < clipBounds.height) {

                bufferedImage = gc.createCompatibleImage(clipBounds.width,
                    clipBounds.height, Transparency.OPAQUE);
                bufferedImageGC = gc;
            }

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
            java.awt.Rectangle gcBounds = gc.getBounds();
            if (volatileImage == null || volatileImageGC != gc) {
                if (volatileImage != null) {
                    volatileImage.flush();
                }
                volatileImage = gc.createCompatibleVolatileImage(gcBounds.width, gcBounds.height,
                    Transparency.OPAQUE);
                // we need to create a new volatile if the GC changes
                volatileImageGC = gc;
            }

            // If we have a valid volatile image, attempt to paint the
            // display to it
            int valid = volatileImage.validate(gc);

            if (valid == java.awt.image.VolatileImage.IMAGE_OK
                || valid == java.awt.image.VolatileImage.IMAGE_RESTORED) {
                java.awt.Rectangle clipBounds = graphics.getClipBounds();
                Graphics2D volatileImageGraphics = volatileImage.createGraphics();
                volatileImageGraphics.setClip(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);

                try {
                    paintDisplay(volatileImageGraphics);
                    // this drawImage method doesn't use width and height
                    int x2 = clipBounds.x + clipBounds.width;
                    int y2 = clipBounds.y + clipBounds.height;
                    graphics.drawImage(volatileImage,
                        clipBounds.x, clipBounds.y, x2, y2,
                        clipBounds.x, clipBounds.y, x2, y2,
                        this);
                } finally {
                    volatileImageGraphics.dispose();
                }

                painted = !volatileImage.contentsLost();
            } else {
                volatileImage.flush();
                volatileImage = null;
            }

            return painted;
        }

        /**
         * Paints the display including any decorators.
         *
         * @param graphics
         */
        private void paintDisplay(Graphics2D graphics) {
            if (scale != 1) {
                graphics.scale(scale, scale);
            }

            Graphics2D decoratedGraphics = graphics;

            DecoratorSequence decorators = display.getDecorators();
            int n = decorators.getLength();
            for (int i = n - 1; i >= 0; i--) {
                Decorator decorator = decorators.get(i);
                decoratedGraphics = decorator.prepare(display, decoratedGraphics);
            }

            graphics.setRenderingHint(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_SPEED);
            display.paint(graphics);

            for (int i = 0; i < n; i++) {
                Decorator decorator = decorators.get(i);
                decorator.update();
            }

            // Paint the drag visual
            if (dragDescendant != null) {
                DragSource dragSource = dragDescendant.getDragSource();
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
            DragSource dragSource = dragDescendant.getDragSource();
            Visual dragRepresentation = dragSource.getRepresentation();

            if (dragRepresentation != null) {
                Point dragOffset = dragSource.getOffset();

                repaint(dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                    dragRepresentation.getWidth(), dragRepresentation.getHeight());
            }
        }

        private Component getDropDescendant(int x, int y) {
            Component dropDescendantLocal = display.getDescendantAt(x, y);

            while (dropDescendantLocal != null
                && dropDescendantLocal.getDropTarget() == null) {
                dropDescendantLocal = dropDescendantLocal.getParent();
            }

            if (dropDescendantLocal != null
                && dropDescendantLocal.isBlocked()) {
                dropDescendantLocal = null;
            }

            return dropDescendantLocal;
        }

        private void startNativeDrag(final DragSource dragSource, final Component dragDescendantArgument,
            final MouseEvent mouseEvent) {
            java.awt.dnd.DragSource awtDragSource = java.awt.dnd.DragSource.getDefaultDragSource();

            final int supportedDropActions = dragSource.getSupportedDropActions();

            DragGestureRecognizer dragGestureRecognizer =
                new DragGestureRecognizer(java.awt.dnd.DragSource.getDefaultDragSource(), DisplayHost.this) {
                    private static final long serialVersionUID = -3204487375572082596L;

                {   appendEvent(mouseEvent);
                }

                @Override
                public synchronized int getSourceActions() {
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

                @Override
                protected void registerListeners() {
                    // No-op
                }

                @Override
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

            LocalManifest dragContent = dragSource.getContent();
            LocalManifestAdapter localManifestAdapter = new LocalManifestAdapter(dragContent);

            awtDragSource.startDrag(trigger, java.awt.Cursor.getDefaultCursor(),
                null, null, localManifestAdapter, new DragSourceListener() {
                @Override
                public void dragEnter(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                @Override
                public void dragExit(DragSourceEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(java.awt.Cursor.getDefaultCursor());
                }

                @Override
                public void dragOver(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                @Override
                public void dropActionChanged(DragSourceDragEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(getDropCursor(getDropAction(event.getDropAction())));
                }

                @Override
                public void dragDropEnd(DragSourceDropEvent event) {
                    DragSourceContext context = event.getDragSourceContext();
                    context.setCursor(java.awt.Cursor.getDefaultCursor());
                    dragSource.endDrag(dragDescendantArgument, getDropAction(event.getDropAction()));
                }
            });
        }

        @Override
        protected void processEvent(AWTEvent event) {
            currentAWTEvent = event;

            super.processEvent(event);

            currentAWTEvent = null;

            display.validate();
        }

        @Override
        protected void processComponentEvent(ComponentEvent event) {
            super.processComponentEvent(event);

            switch (event.getID()) {
                case ComponentEvent.COMPONENT_RESIZED: {
                    if (scale == 1) {
                        display.setSize(Math.max(getWidth(), 0), Math.max(getHeight(), 0));
                    } else {
                        display.setSize(Math.max((int)Math.ceil(getWidth() / scale), 0),
                            Math.max((int)Math.ceil(getHeight() / scale), 0));
                    }

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
                        focusedComponent.requestFocus();
                    }

                    break;
                }

                case FocusEvent.FOCUS_LOST: {
                    focusedComponent = Component.getFocusedComponent();
                    Component.clearFocus();

                    break;
                }
            }
        }

        @Override
        protected void processMouseEvent(MouseEvent event) {
            super.processMouseEvent(event);

            int x = (int)Math.round(event.getX() / scale);
            int y = (int)Math.round(event.getY() / scale);

            // Set the mouse button state
            int mouseButtons = 0x00;

            int modifiersEx = event.getModifiersEx();
            if ((modifiersEx & MouseEvent.BUTTON1_DOWN_MASK) > 0) {
                mouseButtons |= Mouse.Button.LEFT.getMask();
            }

            if ((modifiersEx & MouseEvent.BUTTON2_DOWN_MASK) > 0) {
                mouseButtons |= Mouse.Button.MIDDLE.getMask();
            }

            if ((modifiersEx & MouseEvent.BUTTON3_DOWN_MASK) > 0) {
                mouseButtons |= Mouse.Button.RIGHT.getMask();
            }

            Mouse.setButtons(mouseButtons);

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
            int eventID = event.getID();
            if (eventID == MouseEvent.MOUSE_ENTERED
                || eventID == MouseEvent.MOUSE_EXITED) {
                try {
                    switch(eventID) {
                        case MouseEvent.MOUSE_ENTERED: {
                            display.mouseOver();
                            break;
                        }

                        case MouseEvent.MOUSE_EXITED: {
                            display.mouseOut();
                            break;
                        }
                    }
                } catch (Exception exception) {
                    handleUncaughtException(exception);
                }
            } else {
                // Determine the mouse owner
                Component mouseOwner;
                Component mouseCapturer = Mouse.getCapturer();
                if (mouseCapturer == null) {
                    mouseOwner = display;
                } else {
                    mouseOwner = mouseCapturer;
                    Point location = mouseOwner.mapPointFromAncestor(display, x, y);
                    x = location.x;
                    y = location.y;
                }

                // Delegate the event to the owner
                try {
                    switch (eventID) {
                        case MouseEvent.MOUSE_PRESSED: {
                            requestFocus();
                            requestFocusInWindow();

                            boolean consumed = mouseOwner.mouseDown(button, x, y);

                            if (button == Mouse.Button.LEFT) {
                                dragLocation = new Point(x, y);
                            } else if (menuPopup == null
                                && button == Mouse.Button.RIGHT
                                && !consumed) {
                                // Instantiate a context menu
                                Menu menu = new Menu();

                                // Allow menu handlers to configure the menu
                                Component component = mouseOwner;
                                int componentX = x;
                                int componentY = y;

                                do {
                                    MenuHandler menuHandler = component.getMenuHandler();
                                    if (menuHandler != null) {
                                        if (menuHandler.configureContextMenu(component,
                                            menu, componentX, componentY)) {
                                            // Stop propagation
                                            break;
                                        }
                                    }

                                    if (component instanceof Container) {
                                        Container container = (Container)component;
                                        component = container.getComponentAt(componentX,
                                            componentY);

                                        if (component != null) {
                                            componentX -= component.getX();
                                            componentY -= component.getY();
                                        }
                                    } else {
                                        component = null;
                                    }
                                } while (component != null && component.isEnabled());

                                // Show the context menu if it contains any sections
                                if (menu.getSections().getLength() > 0) {
                                    menuPopup = new MenuPopup(menu);

                                    menuPopup.getWindowStateListeners().add(new WindowStateListener.Adapter() {
                                        @Override
                                        public void windowClosed(Window window, Display displayArgument, Window owner) {
                                            menuPopup.getMenu().getSections().clear();
                                            menuPopup = null;
                                            window.getWindowStateListeners().remove(this);
                                        }
                                    });

                                    Window window = null;
                                    if (mouseOwner == display) {
                                        window = (Window)display.getComponentAt(x, y);
                                    } else {
                                        window = mouseOwner.getWindow();
                                    }

                                    Display displayLocal = window.getDisplay();
                                    Point location = mouseOwner.mapPointToAncestor(displayLocal, x, y);
                                    menuPopup.open(window, location);
                                }
                            }

                            if (consumed) {
                                event.consume();
                            }

                            break;
                        }

                        case MouseEvent.MOUSE_RELEASED: {
                            if (dragDescendant == null) {
                                boolean consumed = mouseOwner.mouseUp(button, x, y);

                                if (consumed) {
                                    event.consume();
                                }
                            } else {
                                DragSource dragSource = dragDescendant.getDragSource();

                                repaintDragRepresentation();

                                if (dropDescendant == null) {
                                    dragSource.endDrag(dragDescendant, null);
                                } else {
                                    DropTarget dropTarget = dropDescendant.getDropTarget();
                                    DropAction dropAction = dropTarget.drop(dropDescendant,
                                        dragManifest, dragSource.getSupportedDropActions(), x, y,
                                        getUserDropAction(event));
                                    dragSource.endDrag(dragDescendant, dropAction);
                                }

                                setCursor(java.awt.Cursor.getDefaultCursor());

                                // Clear the drag state
                                dragDescendant = null;
                                dragManifest = null;

                                // Clear the drop state
                                userDropAction = null;
                                dropDescendant = null;
                            }

                            // Clear the drag location
                            dragLocation = null;

                            break;
                        }
                    }
                } catch (Exception exception) {
                    handleUncaughtException(exception);
                }
            }
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent event) {
            super.processMouseMotionEvent(event);

            int x = (int)Math.round(event.getX() / scale);
            int y = (int)Math.round(event.getY() / scale);

            // Process the event
            try {
                switch (event.getID()) {
                    case MouseEvent.MOUSE_MOVED:
                    case MouseEvent.MOUSE_DRAGGED: {
                        if (dragDescendant == null) {
                            // A drag is not active
                            Component mouseCapturer = Mouse.getCapturer();

                            if (mouseCapturer == null) {
                                // The mouse is not captured, so propagate the event to the display
                                if (!display.isMouseOver()) {
                                    display.mouseOver();
                                }

                                display.mouseMove(x, y);

                                int dragThreshold = Platform.getDragThreshold();

                                if (dragLocation != null
                                    && (Math.abs(x - dragLocation.x) > dragThreshold
                                        || Math.abs(y - dragLocation.y) > dragThreshold)) {
                                    // The user has dragged the mouse past the drag threshold; try
                                    // to find a drag source
                                    dragDescendant = display.getDescendantAt(dragLocation.x,
                                        dragLocation.y);

                                    while (dragDescendant != null
                                        && dragDescendant.getDragSource() == null) {
                                        dragDescendant = dragDescendant.getParent();
                                    }

                                    if (dragDescendant == null
                                        || dragDescendant.isBlocked()) {
                                        // There was nothing to drag, so clear the drag location
                                        dragDescendant = null;
                                        dragLocation = null;
                                    } else {
                                        DragSource dragSource = dragDescendant.getDragSource();
                                        dragLocation = dragDescendant.mapPointFromAncestor(display, x, y);

                                        if (dragSource.beginDrag(dragDescendant,
                                            dragLocation.x, dragLocation.y)) {
                                            // A drag has started
                                            if (dragSource.isNative()) {
                                                startNativeDrag(dragSource, dragDescendant,
                                                    event);

                                                // Clear the drag state since it is not used for
                                                // native drags
                                                dragDescendant = null;
                                                dragLocation = null;
                                            } else {
                                                if (dragSource.getRepresentation() != null
                                                    && dragSource.getOffset() == null) {
                                                    throw new IllegalStateException("Drag offset is required when a "
                                                        + " respresentation is specified.");
                                                }

                                                if (display.isMouseOver()) {
                                                    display.mouseOut();
                                                }

                                                // Get the drag content
                                                dragManifest = dragSource.getContent();

                                                // Get the initial user drop action
                                                userDropAction = getUserDropAction(event);

                                                // Repaint the drag visual
                                                dragLocation = new Point(x, y);
                                                repaintDragRepresentation();
                                            }
                                        } else {
                                            // Clear the drag state
                                            dragDescendant = null;
                                            dragLocation = null;
                                        }
                                    }
                                }
                            } else {
                                // Delegate the event to the capturer
                                Point location = mouseCapturer.mapPointFromAncestor(display, x, y);
                                boolean consumed = mouseCapturer.mouseMove(location.x, location.y);

                                if (consumed) {
                                    event.consume();
                                }
                            }
                        } else {
                            if (dragLocation != null) {
                                DragSource dragSource = dragDescendant.getDragSource();

                                // Get the previous and current drop descendant and call
                                // move or exit/enter as appropriate
                                Component previousDropDescendant = dropDescendant;
                                dropDescendant = getDropDescendant(x, y);

                                DropAction dropAction = null;

                                if (previousDropDescendant == dropDescendant) {
                                    if (dropDescendant != null) {
                                        DropTarget dropTarget = dropDescendant.getDropTarget();

                                        Point dropLocation = dropDescendant.mapPointFromAncestor(display, x, y);
                                        dropAction = dropTarget.dragMove(dropDescendant, dragManifest,
                                            dragSource.getSupportedDropActions(),
                                            dropLocation.x, dropLocation.y, userDropAction);
                                    }
                                } else {
                                    if (previousDropDescendant != null) {
                                        DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                                        previousDropTarget.dragExit(previousDropDescendant);
                                    }

                                    if (dropDescendant != null) {
                                        DropTarget dropTarget = dropDescendant.getDropTarget();
                                        dropAction = dropTarget.dragEnter(dropDescendant, dragManifest,
                                            dragSource.getSupportedDropActions(), userDropAction);
                                    }
                                }

                                // Update cursor
                                setCursor(getDropCursor(dropAction));

                                // Repaint the drag visual
                                repaintDragRepresentation();

                                dragLocation = new Point(x, y);
                                repaintDragRepresentation();
                            }
                        }

                        break;
                    }
                }
            } catch (Exception exception) {
                handleUncaughtException(exception);
            }
        }

        @Override
        protected void processMouseWheelEvent(MouseWheelEvent event) {
            super.processMouseWheelEvent(event);

            // Get the event coordinates
            int x = (int)Math.round(event.getX() / scale);
            int y = (int)Math.round(event.getY() / scale);

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
            try {
                switch (event.getID()) {
                    case MouseEvent.MOUSE_WHEEL: {
                        if (Keyboard.isPressed(Keyboard.Modifier.CTRL)
                            && Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            // Mouse wheel scaling
                            if (event.getWheelRotation() < 0) {
                                scaleUp();
                            } else {
                                scaleDown();
                            }
                        } else if (dragDescendant == null) {
                            // Determine the mouse owner
                            Component mouseOwner;
                            Component mouseCapturer = Mouse.getCapturer();
                            if (mouseCapturer == null) {
                                mouseOwner = display;
                            } else {
                                mouseOwner = mouseCapturer;
                                Point location = mouseOwner.mapPointFromAncestor(display, x, y);
                                x = location.x;
                                y = location.y;
                            }

                            // Delegate the event to the owner
                            boolean consumed = mouseOwner.mouseWheel(scrollType, event.getScrollAmount(),
                                event.getWheelRotation(), x, y);

                            if (consumed) {
                                event.consume();
                            }
                        }
                        break;
                    }
                }
            } catch (Exception exception) {
                handleUncaughtException(exception);
            }
        }

        @Override
        protected void processKeyEvent(KeyEvent event) {
            super.processKeyEvent(event);

            int modifiersEx = event.getModifiersEx();
            int awtKeyLocation = event.getKeyLocation();

            // Set the keyboard modifier state
            int keyboardModifiers = 0;
            if ((modifiersEx & KeyEvent.SHIFT_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.SHIFT.getMask();
            }

            // Ignore Control when Alt-Graphics is pressed
            if ((modifiersEx & KeyEvent.CTRL_DOWN_MASK) > 0
                && ((modifiersEx & KeyEvent.ALT_DOWN_MASK) == 0
                    || awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT)) {
                keyboardModifiers |= Keyboard.Modifier.CTRL.getMask();
            }

            if ((modifiersEx & KeyEvent.ALT_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.ALT.getMask();
            }

            if ((modifiersEx & KeyEvent.META_DOWN_MASK) > 0) {
                keyboardModifiers |= Keyboard.Modifier.META.getMask();
            }

            Keyboard.setModifiers(keyboardModifiers);

            // Get the key location
            Keyboard.KeyLocation keyLocation = null;
            switch (awtKeyLocation) {
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

            if (dragDescendant == null) {
                // Process the event
                Component focusedComponentLocal = Component.getFocusedComponent();

                switch (event.getID()) {
                    case KeyEvent.KEY_PRESSED: {
                        boolean consumed = false;

                        int keyCode = event.getKeyCode();

                        if (Keyboard.isPressed(Keyboard.Modifier.CTRL)
                            && Keyboard.isPressed(Keyboard.Modifier.SHIFT)) {
                            if (keyCode == Keyboard.KeyCode.PLUS
                                || keyCode == Keyboard.KeyCode.EQUALS
                                || keyCode == Keyboard.KeyCode.ADD) {
                                scaleUp();
                            } else if (keyCode == Keyboard.KeyCode.MINUS
                                || keyCode == Keyboard.KeyCode.SUBTRACT) {
                                scaleDown();
                            }
                        }

                        try {
                            if (focusedComponentLocal == null) {
                                for (Application application : applications) {
                                    if (application instanceof Application.UnprocessedKeyHandler) {
                                        Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                            (Application.UnprocessedKeyHandler)application;
                                        unprocessedKeyHandler.keyPressed(keyCode, keyLocation);
                                    }
                                }
                            } else {
                                if (!focusedComponentLocal.isBlocked()) {
                                    consumed = focusedComponentLocal.keyPressed(keyCode, keyLocation);
                                }
                            }
                        } catch (Exception exception) {
                            handleUncaughtException(exception);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }

                    case KeyEvent.KEY_RELEASED: {
                        boolean consumed = false;

                        int keyCode = event.getKeyCode();

                        try {
                            if (focusedComponentLocal == null) {
                                for (Application application : applications) {
                                    if (application instanceof Application.UnprocessedKeyHandler) {
                                        Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                            (Application.UnprocessedKeyHandler)application;
                                        unprocessedKeyHandler.keyReleased(keyCode, keyLocation);
                                    }
                                }
                            } else {
                                if (!focusedComponentLocal.isBlocked()) {
                                    consumed = focusedComponentLocal.keyReleased(keyCode, keyLocation);
                                }
                            }
                        } catch (Exception exception) {
                            handleUncaughtException(exception);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }

                    case KeyEvent.KEY_TYPED: {
                        boolean consumed = false;

                        char keyChar = event.getKeyChar();

                        try {
                            if (focusedComponentLocal == null) {
                                for (Application application : applications) {
                                    if (application instanceof Application.UnprocessedKeyHandler) {
                                        Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                            (Application.UnprocessedKeyHandler)application;
                                        unprocessedKeyHandler.keyTyped(keyChar);
                                    }
                                }
                            } else {
                                if (!focusedComponentLocal.isBlocked()) {
                                    consumed = focusedComponentLocal.keyTyped(keyChar);
                                }
                            }
                        } catch (Exception exception) {
                            handleUncaughtException(exception);
                        }

                        if (consumed) {
                            event.consume();
                        }

                        break;
                    }
                }
            } else {
                DragSource dragSource = dragDescendant.getDragSource();

                // If the user drop action changed, notify the drop descendant
                if (dropDescendant != null) {
                    DropAction previousUserDropAction = userDropAction;
                    userDropAction = getUserDropAction(event);

                    if (previousUserDropAction != userDropAction) {
                        DropTarget dropTarget = dropDescendant.getDropTarget();

                        Point dropLocation = dragLocation;
                        if (dropLocation == null) {
                            dropLocation = display.getMouseLocation();
                        }

                        dropLocation = dropDescendant.mapPointFromAncestor(display,
                            dropLocation.x, dropLocation.y);
                        dropTarget.userDropActionChange(dropDescendant, dragManifest,
                            dragSource.getSupportedDropActions(),
                            dropLocation.x, dropLocation.y, userDropAction);
                    }
                }
            }
        }
    }

    /**
     * Resource cache dictionary implementation.
     */
    public static final class ResourceCacheDictionary
        implements Dictionary<URL, Object>, Iterable<URL> {
        private ResourceCacheDictionary() {
        }

        @Override
        public synchronized Object get(URL key) {
            try {
                return resourceCache.get(key.toURI());
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public synchronized Object put(URL key, Object value) {
            try {
                return resourceCache.put(key.toURI(), value);
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public synchronized Object remove(URL key) {
            try {
                return resourceCache.remove(key.toURI());
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public synchronized boolean containsKey(URL key) {
            try {
                return resourceCache.containsKey(key.toURI());
            } catch (URISyntaxException exception) {
                throw new RuntimeException(exception);
            }
        }

        @Override
        public Iterator<URL> iterator() {
            return new Iterator<URL>() {
                private Iterator<URI> iterator = resourceCache.iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public URL next() {
                    try {
                        return iterator.next().toURL();
                    } catch (MalformedURLException exception) {
                        throw new RuntimeException(exception);
                    }
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Class representing a scheduled callback.
     */
    public static final class ScheduledCallback extends TimerTask {
        private Runnable callback;
        private QueuedCallback queuedCallback = null;

        private ScheduledCallback(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            if (queuedCallback != null) {
                queuedCallback.cancel();
            }

            queuedCallback = queueCallback(callback);
        }

        @Override
        public boolean cancel() {
            if (queuedCallback != null) {
                queuedCallback.cancel();
            }

            return super.cancel();
        }
    }

    /**
     * Class representing a queued callback.
     */
    public static class QueuedCallback implements Runnable {
        private Runnable callback;
        private volatile boolean executed = false;
        private volatile boolean cancelled = false;

        private QueuedCallback(Runnable callback) {
            this.callback = callback;
        }

        @Override
        public void run() {
            if (!cancelled) {
                try {
                    callback.run();
                } catch (Exception exception) {
                    exception.printStackTrace();

                    for (Application application : applications) {
                        if (application instanceof Application.UncaughtExceptionHandler) {
                            Application.UncaughtExceptionHandler uncaughtExceptionHandler =
                                (Application.UncaughtExceptionHandler)application;
                            uncaughtExceptionHandler.uncaughtExceptionThrown(exception);
                        }
                    }
                }

                for (Display display : displays) {
                    display.validate();
                }

                executed = true;
            }
        }

        public boolean cancel() {
            cancelled = true;
            return (!executed);
        }
    }

    protected static URL origin = null;
    protected static ArrayList<Display> displays = new ArrayList<Display>();
    protected static ArrayList<Application> applications = new ArrayList<Application>();

    private static Timer timer = null;

    private static HashMap<URI, Object> resourceCache = new HashMap<URI, Object>();
    private static ResourceCacheDictionary resourceCacheDictionary = new ResourceCacheDictionary();

    private static Version jvmVersion = null;
    private static Version pivotVersion = null;

    static {
        // Get the JVM version
        jvmVersion = Version.decode(System.getProperty("java.vm.version"));

        // Get the Pivot version
        String version = ApplicationContext.class.getPackage().getImplementationVersion();
        if (version == null) {
            pivotVersion = new Version(0, 0, 0, 0);
        } else {
            pivotVersion = Version.decode(version);
        }
    }

    /**
     * Returns this application's origin (the URL of it's originating server).
     *
     * @return
     * The application's origin, or <tt>null</tt> if the origin cannot be determined.
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
     * Adds the styles from a named stylesheet to the named or typed style collections.
     *
     * @param resourceName
     */
    @SuppressWarnings("unchecked")
    public static void applyStylesheet(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL stylesheetLocation = classLoader.getResource(resourceName.substring(1));
        if (stylesheetLocation == null) {
            throw new RuntimeException("Unable to locate style sheet resource \"" + resourceName + "\".");
        }

        try {
            InputStream inputStream = stylesheetLocation.openStream();

            try {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, ?> stylesheet = (Map<String, ?>)serializer.readObject(inputStream);

                for (String name : stylesheet) {
                    Map<String, ?> styles = (Map<String, ?>)stylesheet.get(name);

                    int i = name.lastIndexOf('.') + 1;
                    if (Character.isUpperCase(name.charAt(i))) {
                        // Assume the current package if none specified
                        if (!name.contains(".")) {
                            name = ApplicationContext.class.getPackage().getName() + "." + name;
                        }

                        Class<?> type = null;
                        try {
                            type = Class.forName(name);
                        } catch (ClassNotFoundException exception) {
                            // No-op
                        }

                        if (type != null
                            && Component.class.isAssignableFrom(type)) {
                            Component.getTypedStyles().put((Class<? extends Component>)type, styles);
                        }
                    } else {
                        Component.getNamedStyles().put(name, styles);
                    }
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Returns the current JVM version.
     *
     * @return
     * The current JVM version, or <tt>null</tt> if the version can't be
     * determined.
     */
    public static Version getJVMVersion() {
        return jvmVersion;
    }

    /**
     * Returns the current Pivot version.
     *
     * @return
     * The current Pivot version (determined at build time), or <tt>null</tt>
     * if the version can't be determined.
     */
    public static Version getPivotVersion() {
        return pivotVersion;
    }

    /**
     * Schedules a task for one-time execution. The task will be executed on
     * the UI thread.
     *
     * @param callback
     * The task to execute.
     *
     * @param delay
     * The length of time to wait before executing the task.
     */
    public static ScheduledCallback scheduleCallback(Runnable callback, long delay) {
        ScheduledCallback scheduledCallback = new ScheduledCallback(callback);

        // TODO This is a workaround for a potential OS X bug; revisit
        try {
            try {
                timer.schedule(scheduledCallback, delay);
            } catch (IllegalStateException exception) {
                createTimer();
                timer.schedule(scheduledCallback, delay);
            }
        } catch (Throwable throwable) {
            System.err.println("Unable to schedule callback: " + throwable);
        }

        return scheduledCallback;
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread and will begin executing immediately.
     *
     * @param callback
     * The task to execute.
     *
     * @param period
     * The interval at which the task will be repeated.
     */
    public static ScheduledCallback scheduleRecurringCallback(Runnable callback, long period) {
        return scheduleRecurringCallback(callback, 0, period);
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread.
     *
     * @param callback
     * The task to execute.
     *
     * @param delay
     * The length of time to wait before the first execution of the task
     *
     * @param period
     * The interval at which the task will be repeated.
     */
    public static ScheduledCallback scheduleRecurringCallback(Runnable callback, long delay, long period) {
        ScheduledCallback scheduledCallback = new ScheduledCallback(callback);

        // TODO This is a workaround for a potential OS X bug; revisit
        try {
            try {
                timer.schedule(scheduledCallback, delay, period);
            } catch (IllegalStateException exception) {
                createTimer();
                timer.schedule(scheduledCallback, delay, period);
            }
        } catch (Throwable throwable) {
            System.err.println("Unable to schedule callback: " + throwable);
        }

        return scheduledCallback;
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * returns without waiting for the task to complete.
     *
     * @param callback
     * The task to execute.
     */
    public static QueuedCallback queueCallback(Runnable callback) {
        return queueCallback(callback, false);
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * optionally waits for the task to complete.
     *
     * @param callback
     * The task to execute.
     *
     * @param wait
     * If <tt>true</tt>, does not return until the task has executed.
     * Otherwise, returns immediately.
     */
    public static QueuedCallback queueCallback(Runnable callback, boolean wait) {
        QueuedCallback queuedCallback = new QueuedCallback(callback);

        // TODO This is a workaround for a potential OS X bug; revisit
        try {
            if (wait) {
                try {
                    java.awt.EventQueue.invokeAndWait(queuedCallback);
                } catch (InvocationTargetException exception) {
                    throw new RuntimeException(exception.getCause());
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            } else {
                java.awt.EventQueue.invokeLater(queuedCallback);
            }
        } catch (Throwable throwable) {
            System.err.println("Unable to queue callback: " + throwable);
        }

        return queuedCallback;
    }

    protected static void createTimer() {
        timer = new Timer();
    }

    protected static void destroyTimer() {
        timer.cancel();
        timer = null;
    }

    protected static void invalidateDisplays() {
        for (Display display : displays) {
            display.invalidate();
        }
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

    private static int getSupportedDropActions(int sourceActions) {
        int dropActions = 0;

        if ((sourceActions & DnDConstants.ACTION_COPY) > 0) {
            dropActions |= DropAction.COPY.getMask();
        }

        if ((sourceActions & DnDConstants.ACTION_MOVE) > 0) {
            dropActions |= DropAction.MOVE.getMask();
        }

        if ((sourceActions & DnDConstants.ACTION_LINK) > 0) {
            dropActions |= DropAction.LINK.getMask();
        }

        return dropActions;
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

    private static void handleUncaughtException(Exception exception) {
        int n = 0;
        for (Application application : applications) {
            if (application instanceof Application.UncaughtExceptionHandler) {
                Application.UncaughtExceptionHandler uncaughtExceptionHandler =
                    (Application.UncaughtExceptionHandler)application;
                uncaughtExceptionHandler.uncaughtExceptionThrown(exception);
                n++;
            }
        }

        if (n == 0) {
            exception.printStackTrace();
        }
    }

}
