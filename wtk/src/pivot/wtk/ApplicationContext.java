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
import java.awt.Toolkit;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;

public abstract class ApplicationContext {
    public static class DisplayHost extends java.awt.Panel {
        public static final long serialVersionUID = 0;
        private Component focusedComponent = null;

        protected DisplayHost() {
            enableEvents(AWTEvent.COMPONENT_EVENT_MASK
                | AWTEvent.FOCUS_EVENT_MASK
                | AWTEvent.MOUSE_EVENT_MASK
                | AWTEvent.MOUSE_MOTION_EVENT_MASK
                | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                | AWTEvent.KEY_EVENT_MASK);

            setLayout(null);
            setFocusTraversalKeysEnabled(false);
        }

        @Override
        public void paint(Graphics graphics) {
            // Intersect the clip region with the bounds of this component
            // (for some reason, AWT does not do this automatically)
            ((Graphics2D)graphics).clip(new java.awt.Rectangle(0, 0, getWidth(), getHeight()));

            Shape clip = graphics.getClip();
            java.awt.Rectangle clipBounds = (clip == null) ? getBounds() : clip.getBounds();

            if (!clipBounds.isEmpty()) {
                try {
                    if (!paintVolatileBuffered(graphics)) {
                        System.out.println("Volatile buffer paint failed.");

                        if (!paintBuffered(graphics)) {
                            System.out.println("Standard buffer paint failed.");

                            Display.getInstance().paint((Graphics2D)graphics);
                            DragDropManager.getInstance().paint((Graphics2D)graphics);
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
        private boolean paintBuffered(Graphics graphics) {
            boolean painted = false;

            Shape clip = graphics.getClip();
            java.awt.Rectangle clipBounds = (clip == null) ? getBounds() : clip.getBounds();

            // Paint the display into an offscreen buffer
            java.awt.Image image = createImage(clipBounds.width, clipBounds.height);

            if (image != null) {
                Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
                imageGraphics.setClip(0, 0, clipBounds.width, clipBounds.height);
                imageGraphics.translate(-clipBounds.x, -clipBounds.y);

                try {
                    Display.getInstance().paint(imageGraphics);
                    DragDropManager.getInstance().paint((Graphics2D)imageGraphics);
                    graphics.drawImage(image, clipBounds.x, clipBounds.y, this);
                } finally {
                    imageGraphics.dispose();
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
        private boolean paintVolatileBuffered(Graphics graphics) {
            boolean painted = false;

            Shape clip = graphics.getClip();
            java.awt.Rectangle clipBounds = (clip == null) ? getBounds() : clip.getBounds();

            // Paint the display into a volatile offscreen buffer
            java.awt.image.VolatileImage volatileImage =
                createVolatileImage(clipBounds.width, clipBounds.height);

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
                        Display.getInstance().paint(volatileImageGraphics);
                        DragDropManager.getInstance().paint((Graphics2D)volatileImageGraphics);
                        graphics.drawImage(volatileImage, clipBounds.x, clipBounds.y, this);
                    } finally {
                        volatileImageGraphics.dispose();
                    }

                    painted = !volatileImage.contentsLost();
                }
            }

            return painted;
        }

        @Override
        public void update(Graphics graphics) {
            // NOTE We override this method to call paint() directly, since,
            // in Windows and Linux, the base method appears to clear the
            // background before calling paint().
            paint(graphics);
        }

        @Override
        protected void processComponentEvent(ComponentEvent event) {
            super.processComponentEvent(event);

            Display display = Display.getInstance();

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
                    if (focusedComponent != null) {
                        Component.setFocusedComponent(focusedComponent, true);
                    }

                    break;
                }

                case FocusEvent.FOCUS_LOST: {
                    focusedComponent = Component.getFocusedComponent();
                    Component.setFocusedComponent(null, true);

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

            // Set the Mouse button state
            int modifierMask = event.getModifiersEx();
            int buttonBitfield = 0x00;

            if ((modifierMask & MouseEvent.BUTTON1_DOWN_MASK) == MouseEvent.BUTTON1_DOWN_MASK) {
                buttonBitfield |= Mouse.Button.LEFT.getMask();
            }

            if ((modifierMask & MouseEvent.BUTTON2_DOWN_MASK) == MouseEvent.BUTTON2_DOWN_MASK) {
                buttonBitfield |= Mouse.Button.MIDDLE.getMask();
            }

            if ((modifierMask & MouseEvent.BUTTON3_DOWN_MASK) == MouseEvent.BUTTON3_DOWN_MASK) {
                buttonBitfield |= Mouse.Button.RIGHT.getMask();
            }

            Mouse.setButtons(buttonBitfield);

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
            Display display = Display.getInstance();
            DragDropManager dragDropManager = DragDropManager.getInstance();

            switch (event.getID()) {
                case MouseEvent.MOUSE_PRESSED: {
                    display.mouseDown(button, x, y);
                    dragDropManager.mouseDown(button, x, y);
                    break;
                }

                case MouseEvent.MOUSE_RELEASED: {
                    display.mouseUp(button, x, y);
                    dragDropManager.mouseUp(button, x, y);
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

            // Set the Mouse location state
            Mouse.setLocation(x, y);

            // Process the event
            Display display = Display.getInstance();
            DragDropManager dragDropManager = DragDropManager.getInstance();

            switch (event.getID()) {
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED: {
                    display.mouseMove(x, y);
                    dragDropManager.mouseMove(x, y);
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
            Display display = Display.getInstance();

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

            // Set Keyboard state
            int modifierMask = event.getModifiersEx();
            int modifierBitfield = 0x00;

            if ((modifierMask & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                modifierBitfield |= Keyboard.Modifier.SHIFT.getMask();
            }

            if ((modifierMask & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                modifierBitfield |= Keyboard.Modifier.CTRL.getMask();
            }

            if ((modifierMask & KeyEvent.ALT_DOWN_MASK) == KeyEvent.ALT_DOWN_MASK) {
                modifierBitfield |= Keyboard.Modifier.ALT.getMask();
            }

            if ((modifierMask & KeyEvent.META_DOWN_MASK) == KeyEvent.META_DOWN_MASK) {
                modifierBitfield |= Keyboard.Modifier.META.getMask();
            }

            Keyboard.setModifiers(modifierBitfield);

            // Process the event
            Component focusedComponent = Component.getFocusedComponent();
            DragDropManager dragDropManager = DragDropManager.getInstance();

            if (focusedComponent != null) {
                switch (event.getID()) {
                    case KeyEvent.KEY_TYPED: {
                        focusedComponent.keyTyped(event.getKeyChar());
                        break;
                    }

                    case KeyEvent.KEY_PRESSED: {
                        focusedComponent.keyPressed(event.getKeyCode(), keyLocation);
                        dragDropManager.keyPressed(event.getKeyCode(), keyLocation);
                        break;
                    }

                    case KeyEvent.KEY_RELEASED: {
                        focusedComponent.keyReleased(event.getKeyCode(), keyLocation);
                        dragDropManager.keyReleased(event.getKeyCode(), keyLocation);
                        break;
                    }
                }
            }

            // Consume the event
            event.consume();
        }
    }

    public final class PropertyDictionary implements Dictionary<String, Object> {
        public Object get(String key) {
            return properties.get(key);
        }

        public Object put(String key, Object value) {
            throw new UnsupportedOperationException();
        }

        public Object remove(String key) {
            throw new UnsupportedOperationException();
        }

        public boolean containsKey(String key) {
            return properties.containsKey(key);
        }

        public boolean isEmpty() {
            return properties.isEmpty();
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

    // NOTE This member is protected to work around an apparent bug in the
    // Java plugin. See comment in BrowserApplicationContext.HostApplet() for
    // details.
    protected DisplayHost displayHost = new DisplayHost();

    private Application application = null;
    private Map<String, Object> properties = new HashMap<String, Object>();
    private PropertyDictionary propertyDictionary = new PropertyDictionary();

    private HashMap<URL, Object> resources = new HashMap<URL, Object>();

    private boolean applicationStarted = false;
    private boolean applicationSuspended = false;

    private boolean busy = false;
    private Cursor cursor = Cursor.DEFAULT;

    private static ApplicationContext instance = null;

    private static Timer timer = null;
    private static HashMap<Integer, TimerTask> timerTaskMap = new HashMap<Integer, TimerTask>();
    private static int nextTimerTaskID = 0;

    private static int DEFAULT_MULTI_CLICK_INTERVAL = 400;
    private static int DEFAULT_CURSOR_BLINK_RATE = 600;

    /**
     * Creates the application context.
     */
    public ApplicationContext() {
        assert (ApplicationContext.instance == null) : "An application context already exists.";

        ApplicationContext.instance = this;
    }

    /**
     * Application accessor.
     */
    public Application getApplication() {
        return application;
    }

    public boolean isApplicationStarted() {
        return applicationStarted;
    }

    public boolean isApplicationSuspended() {
        return applicationSuspended;
    }

    /**
     * Startup properties accessor.
     */
    public PropertyDictionary getProperties() {
        return propertyDictionary;
    }

    /**
     * Resource properties accessor.
     */
    public Dictionary<URL, Object> getResources() {
        return resources;
    }

    /**
     * Returns the busy state of the application context.
     *
     * @return <tt>true</tt> if the context is busy; <tt>false</tt>,
     * otherwise.
     */
    public boolean isBusy() {
        return busy;
    }

    /**
     * Sets the busy state of the application context.
     *
     * @return <tt>true</tt> if the context is busy; <tt>false</tt>,
     * otherwise.
     */
    public void setBusy(boolean busy) {
        if (this.busy != busy) {
            if (displayHost == null) {
                throw new IllegalStateException("Display host not initialized.");
            }

            if (busy) {
                displayHost.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
            } else {
                setCursor(cursor);
            }

            this.busy = busy;
        }
    }

    public Cursor getCursor() {
        return cursor;
    }

    public void setCursor(Cursor cursor) {
        if (cursor == null) {
            throw new IllegalArgumentException("cursor is null.");
        }

        if (!busy
            && cursor != this.cursor) {
            this.cursor = cursor;

            if (displayHost == null) {
                throw new IllegalStateException("Display host not initialized.");
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
                    System.out.println(cursor + " cursor is not supported.");
                    cursorID = java.awt.Cursor.DEFAULT_CURSOR;
                    break;
                }
            }

            displayHost.setCursor(new java.awt.Cursor(cursorID));
        }
    }

    public void repaint(int x, int y, int width, int height) {
        if (displayHost != null) {
            displayHost.repaint(x, y, width, height);
        }
    }

    public Graphics2D getGraphics() {
        Graphics2D graphics = null;

        if (displayHost != null) {
            graphics = (Graphics2D) displayHost.getGraphics();
        }

        return graphics;
    }

    /**
     * Returns the display host component.
     */
    public DisplayHost getDisplayHost() {
        return displayHost;
    }

    /**
     * Recreates the display host. This is required to work around an apparent
     * bug in the Java plugin. See source code comments in
     * BrowserApplicationContext.java for more information.
     */
    protected void recreateDisplayHost() {
        displayHost = new DisplayHost();
    }

    /**
     * Returns title of the application context.
     *
     * @return
     * The application context title.
     */
    public abstract String getTitle();

    /**
     * Sets title of the application context.
     *
     * @param title
     * The application context title.
     */
    public abstract void setTitle(String title);

    /**
     * Terminates the application context.
     */
    public abstract void exit();

    /**
     * Initializes the application context. Loads the application class and
     * startup properties and starts the system timer.
     */
    protected void initialize(String applicationClassName, String propertiesResourceName) {
        assert (applicationClassName != null) : "applicationClassName is null.";

        try {
            loadApplication(applicationClassName);

            if (propertiesResourceName != null) {
                loadProperties(propertiesResourceName);
            }

            // Start the timer
            timer = new Timer();
        } catch (Exception exception) {
            displaySystemError(exception);
        }
    }

    /**
     * Un-initializes the application context. Stops the system timer,
     * cancelling all outstanding timer tasks.
     */
    protected void uninitialize() {
        // Stop the timer
        if (timer != null) {
            timer.cancel();
        }

        timer = null;
    }

    private void loadApplication(String applicationClassName)
        throws Exception {
        // Load the application's class
        Class<?> applicationClass = Class.forName(applicationClassName);

        // Instantiate the application
        application = (Application)applicationClass.newInstance();
    }

    @SuppressWarnings("unchecked")
    private void loadProperties(String propertiesResourceName)
        throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream propertiesStream = classLoader.getResourceAsStream(propertiesResourceName);
        JSONSerializer jsonSerializer = new JSONSerializer();
        properties = (Map<String, Object>)jsonSerializer.readObject(propertiesStream);
    }

    protected void startupApplication() {
        if (application != null && !applicationStarted) {
            try {
                applicationSuspended = false;

                application.startup();
                applicationStarted = true;
            } catch (Exception exception) {
                displaySystemError(exception);
            }
        }
    }

    protected void shutdownApplication() {
        if (application != null && applicationStarted) {
            try {
                applicationSuspended = false;

                application.shutdown();
                applicationStarted = false;
            } catch (Exception exception) {
                displaySystemError(exception);
            }
        }
    }

    protected void suspendApplication() {
        if (application != null && !applicationSuspended) {
            try {
                application.suspend();
                applicationSuspended = true;
            } catch (Exception exception) {
                displaySystemError(exception);
            }
        }
    }

    protected void resumeApplication() {
        if (application != null && applicationSuspended) {
            try {
                application.resume();
                applicationSuspended = false;
            } catch (Exception exception) {
                displaySystemError(exception);
            }
        }
    }

    private void displaySystemError(Exception exception) {
        String message = exception.getMessage();
        if (message == null) {
            message = exception.toString();
        }

        Alert alert = new Alert(Alert.Type.ERROR, message);
        alert.setTitle(exception.getClass().getName());

        // TODO i18n
        alert.getOptionData().add("OK");
        alert.open();

        exception.printStackTrace();
    }

    /**
     * Returns the application context instance.
     */
    public static ApplicationContext getInstance() {
        return instance;
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
    public static int setInterval(Runnable runnable, int period) {
        int intervalID = nextTimerTaskID++;

        IntervalTask intervalTask = new IntervalTask(runnable);
        timerTaskMap.put(intervalID, intervalTask);
        timer.schedule(intervalTask, 0, period);

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
     *
     * @return
     */
    public static int setTimeout(Runnable runnable, int timeout) {
        int timeoutID = nextTimerTaskID++;

        TimeoutTask timeoutTask = new TimeoutTask(runnable, timeoutID);
        timerTaskMap.put(timeoutID, timeoutTask);
        timer.schedule(timeoutTask, timeout);

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

    private static void clearTimerTask(int timerTaskID) {
        TimerTask timerTask = (TimerTask)timerTaskMap.remove(timerTaskID);
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
     * Issues a system alert sound.
     */
    public static void beep() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }

    public static int getMultiClickInterval() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer multiClickInterval = (Integer)toolkit.getDesktopProperty("awt.multiClickInterval");

        if (multiClickInterval == null) {
            multiClickInterval = DEFAULT_MULTI_CLICK_INTERVAL;
        }

        return multiClickInterval;
    }

    public static int getCursorBlinkRate() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer cursorBlinkRate = (Integer)toolkit.getDesktopProperty("awt.cursorBlinkRate");

        if (cursorBlinkRate == null) {
            cursorBlinkRate = DEFAULT_CURSOR_BLINK_RATE;
        }

        return cursorBlinkRate;
    }
}
