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
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.util.ImmutableIterator;
import pivot.wtk.Component.DecoratorSequence;
import pivot.wtk.media.Picture;

/**
 * Base class for application contexts.
 * <p>
 * TODO Fire events when entries are added to/removed from the cache?
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
    public class DisplayHost extends java.awt.Container {
        public static final long serialVersionUID = 0;

        private int mouseX = 0;
        private int mouseY = 0;
        private int mouseButtonModifiersEx = 0;
        private int keyboardModifiersEx = 0;

        private Component focusedComponent = null;

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

            setFocusTraversalKeysEnabled(false);
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
        }

        public int getMouseX() {
            return mouseX;
        }

        public int getMouseY() {
            return mouseY;
        }

        public int getMouseButtonModifiersEx() {
            return mouseButtonModifiersEx;
        }

        public int getKeyboardModifiersEx() {
            return keyboardModifiersEx;
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

            mouseEvent = event;

            // Get the event coordinates
            int x = event.getX();
            int y = event.getY();

            // Get the mouse button modifiers
            mouseButtonModifiersEx = event.getModifiersEx();

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
                    display.mouseDown(button, x, y);
                    break;
                }

                case MouseEvent.MOUSE_RELEASED: {
                    display.mouseUp(button, x, y);
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

            mouseEvent = null;
        }

        @Override
        protected void processMouseMotionEvent(MouseEvent event) {
            super.processMouseMotionEvent(event);

            mouseEvent = event;

            // Get the event coordinates
            mouseX = event.getX();
            mouseY = event.getY();

            // Process the event
            switch (event.getID()) {
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED: {
                    display.mouseMove(mouseX, mouseY);
                    break;
                }
            }

            mouseEvent = null;
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
                    display.mouseWheel(scrollType, event.getScrollAmount(),
                        event.getWheelRotation(), x, y);
                    break;
                }
            }
        }

        @Override
        protected void processKeyEvent(KeyEvent event) {
            super.processKeyEvent(event);

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

            // Get the keyboard modifiers
            keyboardModifiersEx = event.getModifiersEx();

            // Process the event
            Component focusedComponent = Component.getFocusedComponent();

            switch (event.getID()) {
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

                case KeyEvent.KEY_PRESSED: {
                    boolean consumed = false;
                    int keyCode = event.getKeyCode();

                    if (focusedComponent != null) {
                        consumed = focusedComponent.keyPressed(keyCode, keyLocation);
                    }

                    if (consumed) {
                        event.consume();
                    }

                    break;
                }

                case KeyEvent.KEY_RELEASED: {
                    boolean consumed = false;
                    int keyCode = event.getKeyCode();

                    if (focusedComponent != null) {
                        consumed = focusedComponent.keyReleased(keyCode, keyLocation);
                    }

                    if (consumed) {
                        event.consume();
                    }

                    break;
                }
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

    private DisplayHost displayHost;
    private Display display;

    private MouseEvent mouseEvent = null;

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

        // Create the display host
        displayHost = new DisplayHost();

        // Add native drop support
        new java.awt.dnd.DropTarget(displayHost, new DropTargetAdapter() {
            @Override
            public void dragEnter(DropTargetDragEvent event) {
                Mouse.setDragContentType(Clipboard.getContentType(event.getTransferable()));

                int supportedDropActions = 0;
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

                Mouse.setSupportedDropActions(supportedDropActions);
            }

            @Override
            public void dragExit(DropTargetEvent event) {
                Mouse.setDragContentType(null);
                Mouse.setSupportedDropActions(0);
            }

            public void drop(DropTargetDropEvent event) {
                Transferable transferable = event.getTransferable();
                java.awt.Point location = event.getLocation();

                // Look for a drop handler
                Component dropTarget = display.getDescendantAt(location.x, location.y);

                DropTarget dropHandler = null;
                while (dropTarget != null) {
                    dropHandler = dropTarget.getDropTarget();

                    if (dropHandler == null) {
                        dropTarget = dropTarget.getParent();
                    } else {
                        break;
                    }
                }

                if (dropHandler != null) {
                    // A drop handler was found
                    DropAction dropAction = null;
                    Point dropLocation = dropTarget.mapPointFromAncestor(display,
                        location.x, location.y);

                    Class<?> contentType = Clipboard.getContentType(transferable);
                    dropAction = dropHandler.getDropAction(dropTarget, contentType,
                        Mouse.getSupportedDropActions(), dropLocation.x, dropLocation.y);

                    if (dropAction != null) {
                        int awtDropAction = 0;

                        switch(dropAction) {
                            case COPY: {
                                awtDropAction = DnDConstants.ACTION_COPY;
                                break;
                            }

                            case MOVE: {
                                awtDropAction = DnDConstants.ACTION_MOVE;
                                break;
                            }

                            case LINK: {
                                awtDropAction = DnDConstants.ACTION_LINK;
                                break;
                            }
                        }

                        // Drop the content
                        event.acceptDrop(awtDropAction);

                        Object content = Clipboard.getContent(transferable);
                        if (content != null) {
                            dropHandler.drop(dropTarget, content, dropLocation.x, dropLocation.y);
                        }

                        event.dropComplete(true);
                    }
                } else {
                    event.rejectDrop();
                }
            }
        });

        // Create the display
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
                displayHost.repaint(x, y, width, height);
            }
        }
    }

    protected Graphics2D getGraphics() {
        Graphics2D graphics = null;

        if (displayHost != null) {
            graphics = (Graphics2D) displayHost.getGraphics();
        }

        return graphics;
    }

    protected void startDrag(DragSource dragSource) {
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

        DragGestureEvent trigger = new DragGestureEvent(dragGestureRecognizer,
            DnDConstants.ACTION_COPY, new java.awt.Point(Mouse.getX(), Mouse.getY()),
            inputEvents);

        java.awt.Image image = null;
        java.awt.Point awtOffset = new java.awt.Point();

        Visual representation = dragSource.getRepresentation();
        if (representation instanceof Picture) {
            Picture picture = (Picture)representation;
            image = picture.getBufferedImage();

            Point offset = dragSource.getOffset();
            awtOffset.x = -offset.x;
            awtOffset.y = -offset.y;
        }

        Transferable transferable = new Clipboard.TransferableContent(dragSource.getContent());
        awtDragSource.startDrag(trigger, java.awt.Cursor.getDefaultCursor(),
            image, awtOffset, transferable, new DragSourceAdapter() {});
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

            System.out.println("Set text anti-aliasing hint to \"" + textAntialiasingHint + "\".");
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
}
