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
import static java.awt.Frame.MAXIMIZED_BOTH;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Locale;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableMap;
import org.apache.pivot.util.StringUtils;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;

/**
 * Application context used to execute applications in a native frame window.
 */
public final class DesktopApplicationContext extends ApplicationContext {
    /**
     * Display listener interface.
     */
    public interface DisplayListener {
        /**
         * DisplayListener adapter.
         */
        public static class Adapter implements DisplayListener {
            @Override
            public void hostWindowOpened(Display display) {
                // empty block
            }

            @Override
            public void hostWindowClosed(Display display) {
                // empty block
            }
        }

        /**
         * Called when the host window for secondary display has been opened.
         *
         * @param display The new secondary display.
         */
        public void hostWindowOpened(Display display);

        /**
         * Called when the host window for secondary display has been closed.
         *
         * @param display The secondary display.
         */
        public void hostWindowClosed(Display display);
    }

    // Custom display host that sets the title of the host frame to match the
    // title of the root Pivot owner window
    private static class DesktopDisplayHost extends DisplayHost {
        private static final long serialVersionUID = 0;

        private transient Window rootOwner = null;
        private transient Runnable updateHostWindowTitleBarCallback = null;

        private transient WindowListener rootOwnerListener = new WindowListener() {
            @Override
            public void titleChanged(Window window, String previousTitle) {
                updateFrameTitleBar();
            }

            @Override
            public void iconAdded(Window window, Image addedIcon) {
                updateFrameTitleBar();
            }

            @Override
            public void iconsRemoved(Window window, int index, Sequence<Image> removed) {
                updateFrameTitleBar();
            }
        };

        public DesktopDisplayHost() {
            Display display = getDisplay();
            display.getContainerListeners().add(new ContainerListener() {
                @Override
                public void componentInserted(Container container, int index) {
                    if (index == container.getLength() - 1) {
                        updateFrameTitleBar();
                    }
                }

                @Override
                public void componentsRemoved(Container container, int index,
                    Sequence<Component> removed) {
                    if (index == container.getLength()) {
                        updateFrameTitleBar();
                    }
                }

                @Override
                public void componentMoved(Container container, int from, int to) {
                    int n = container.getLength();

                    if (from == n - 1 || to == n - 1) {
                        updateFrameTitleBar();
                    }
                }
            });
        }

        private void updateFrameTitleBar() {
            Display display = getDisplay();
            int n = display.getLength();

            Window rootOwnerLocal;
            if (n == 0) {
                rootOwnerLocal = null;
            } else {
                Window topWindow = (Window) display.get(display.getLength() - 1);
                rootOwnerLocal = topWindow.getRootOwner();
            }

            Window previousRootOwner = this.rootOwner;
            if (rootOwnerLocal != previousRootOwner) {
                if (previousRootOwner != null) {
                    previousRootOwner.getWindowListeners().remove(this.rootOwnerListener);
                }

                if (rootOwnerLocal != null) {
                    rootOwnerLocal.getWindowListeners().add(this.rootOwnerListener);
                }

                this.rootOwner = rootOwnerLocal;
            }

            if (this.updateHostWindowTitleBarCallback == null) {
                this.updateHostWindowTitleBarCallback = new Runnable() {
                    @Override
                    public void run() {
                        java.awt.Window hostWindow = getDisplay().getHostWindow();

                        Window ownerWindow = DesktopDisplayHost.this.rootOwner;
                        if (ownerWindow == null) {
                            ((TitledWindow) hostWindow).setTitle(DEFAULT_HOST_WINDOW_TITLE);
                            hostWindow.setIconImage(null);
                        } else {
                            ((TitledWindow) hostWindow).setTitle(ownerWindow.getTitle());

                            java.util.ArrayList<BufferedImage> iconImages = new java.util.ArrayList<>();
                            for (Image icon : ownerWindow.getIcons()) {
                                if (icon instanceof Picture) {
                                    iconImages.add(((Picture) icon).getBufferedImage());
                                }
                            }

                            if (iconImages.size() == 1) {
                                hostWindow.setIconImage(iconImages.get(0));
                            } else if (iconImages.size() > 1) {
                                hostWindow.setIconImages(iconImages);
                            }
                        }

                        DesktopDisplayHost.this.updateHostWindowTitleBarCallback = null;
                    }
                };

                queueCallback(this.updateHostWindowTitleBarCallback);
            }
        }
    }

    // The AWT Window class does not define a title property; this interface allows
    // the HostFrame and HostDialog titles to be handled polymorphically
    private interface TitledWindow {
        public String getTitle();

        public void setTitle(String title);
    }

    // Native host frame
    private static class HostFrame extends java.awt.Frame implements TitledWindow {
        private static final long serialVersionUID = 5340356674429280196L;

        public HostFrame() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK | AWTEvent.WINDOW_STATE_EVENT_MASK);

            // Disable focus traversal keys
            setFocusTraversalKeysEnabled(false);

            // Clear the background
            setBackground(null);
        }

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }

        @Override
        public String getTitle() {
            return super.getTitle();
        }

        @Override
        public void setTitle(String title) {
            super.setTitle(title);
        }

        @Override
        public void processWindowEvent(WindowEvent event) {
            super.processWindowEvent(event);

            switch (event.getID()) {
                case WindowEvent.WINDOW_CLOSING:
                    exit();
                    break;
                case WindowEvent.WINDOW_CLOSED:
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }

        @Override
        protected void processWindowStateEvent(WindowEvent event) {
            super.processWindowStateEvent(event);

            switch (event.getID()) {
                case WindowEvent.WINDOW_ICONIFIED:
                    try {
                        application.suspend();
                    } catch (Throwable exception) {
                        handleUncaughtException(exception);
                    }
                    break;
                case WindowEvent.WINDOW_DEICONIFIED:
                    try {
                        application.resume();
                    } catch (Throwable exception) {
                        handleUncaughtException(exception);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    // Native host dialog for secondary displays
    private static class HostDialog extends java.awt.Dialog implements TitledWindow {
        private static final long serialVersionUID = 5340356674429280196L;

        private DisplayHost displayHost = new DesktopDisplayHost();

        private DisplayListener displayCloseListener;

        public HostDialog(java.awt.Window owner, boolean modal, DisplayListener displayCloseListener) {
            super(owner, modal ? java.awt.Dialog.ModalityType.APPLICATION_MODAL
                : java.awt.Dialog.ModalityType.MODELESS);

            this.displayCloseListener = displayCloseListener;

            enableEvents(AWTEvent.WINDOW_EVENT_MASK);

            // Disable focus traversal keys
            setFocusTraversalKeysEnabled(false);

            // Clear the background
            setBackground(null);

            // Add the display host
            add(this.displayHost);
        }

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }

        public Display getDisplay() {
            return this.displayHost.getDisplay();
        }

        @Override
        public String getTitle() {
            return super.getTitle();
        }

        @Override
        public void setTitle(String title) {
            super.setTitle(title);
        }

        @Override
        public void processWindowEvent(WindowEvent event) {
            super.processWindowEvent(event);

            Display display;
            switch (event.getID()) {
                case WindowEvent.WINDOW_OPENED:
                    display = this.displayHost.getDisplay();
                    displays.add(display);

                    if (this.displayCloseListener != null) {
                        this.displayCloseListener.hostWindowOpened(display);
                    }

                    this.displayHost.requestFocus();
                    break;
                case WindowEvent.WINDOW_CLOSING:
                    dispose();
                    break;
                case WindowEvent.WINDOW_CLOSED:
                    display = this.displayHost.getDisplay();
                    displays.remove(display);

                    if (this.displayCloseListener != null) {
                        this.displayCloseListener.hostWindowClosed(display);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private static String applicationClassName = null;
    private static HashMap<String, String> properties = null;

    private static Application application = null;

    private static DisplayHost primaryDisplayHost = null;
    private static HostFrame windowedHostFrame = null;
    private static HostFrame fullScreenHostFrame = null;

    public static final String DEFAULT_HOST_WINDOW_TITLE = "Apache Pivot";

    public static final String X_ARGUMENT = "x";
    public static final String Y_ARGUMENT = "y";
    public static final String WIDTH_ARGUMENT = "width";
    public static final String HEIGHT_ARGUMENT = "height";
    public static final String CENTER_ARGUMENT = "center";
    public static final String RESIZABLE_ARGUMENT = "resizable";
    public static final String MAXIMIZED_ARGUMENT = "maximized";
    public static final String UNDECORATED_ARGUMENT = "undecorated";
    public static final String FULL_SCREEN_ARGUMENT = "fullScreen";
    public static final String PRESERVE_SPLASH_SCREEN_ARGUMENT = "preserveSplashScreen";
    public static final String ORIGIN_ARGUMENT = "origin";
    public static final String USE_APPLICATION_INSTANCE_ARGUMENT = "useApplicationInstance";

    private static final String INVALID_PROPERTY_FORMAT_MESSAGE = "\"%s\" is not a valid startup "
        + "property (expected format is \"--name[=value]\").";
    private static final String INVALID_PROPERTY_VALUE_MESSAGE = "\"%s\" is not a valid value for "
        + "startup property \"%s\".";

    public static boolean isActive() {
        return (application != null);
    }

    /**
     * Terminates the application context. this call is the same as exit(true)
     */
    public static void exit() {
        exit(true);
    }

    /**
     * Terminates the application context.
     *
     * @param optional If <tt>true</tt>, shutdown is optional and may be
     * cancelled. If <tt>false</tt>, shutdown cannot be cancelled.
     * @return Whether shutdown was canceled by the application.
     */
    public static boolean exit(boolean optional) {
        boolean cancelShutdown = false;

        if (application != null) {
            try {
                cancelShutdown = application.shutdown(optional);
            } catch (Throwable exception) {
                handleUncaughtException(exception);
            } finally {
                if (!cancelShutdown) {
                    // Remove the application from the application list
                    applications.remove(application);
                }
            }
        }

        if (!cancelShutdown) {
            try {
                Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
                preferences = preferences.node(applicationClassName);

                boolean maximized = (windowedHostFrame.getExtendedState() & MAXIMIZED_BOTH) == MAXIMIZED_BOTH;
                if (!maximized) {
                    preferences.putInt(X_ARGUMENT, windowedHostFrame.getX());
                    preferences.putInt(Y_ARGUMENT, windowedHostFrame.getY());
                    preferences.putInt(WIDTH_ARGUMENT, windowedHostFrame.getWidth());
                    preferences.putInt(HEIGHT_ARGUMENT, windowedHostFrame.getHeight());
                }

                preferences.putBoolean(MAXIMIZED_ARGUMENT, maximized);

                preferences.flush();
            } catch (SecurityException exception) {
                // No-op
            } catch (BackingStoreException exception) {
                // No-op
            }

            windowedHostFrame.dispose();
            fullScreenHostFrame.dispose();
        }

        return cancelShutdown;
    }

    /**
     * Calculate the entire virtual desktop bounding rectangle.
     * @return The entire bounding rectangle of all the screen devices.
     */
    private static Rectangle getVirtualDesktopBounds() {
        Rectangle virtualBounds = new Rectangle();
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (GraphicsDevice gd : ge.getScreenDevices()) {
            for (GraphicsConfiguration gc : gd.getConfigurations()) {
                virtualBounds = virtualBounds.union(gc.getBounds());
            }
        }
        return virtualBounds;
    }

    /**
     * Primary application entry point.
     *
     * @param args application arguments
     */
    public static void main(String[] args) {
        // Get the application class name
        if (args.length == 0) {
            System.err.println("Application class name is required.");
            return;
        }

        applicationClassName = args[0];

        // Get the startup properties
        properties = new HashMap<>();

        int x = 0;
        int y = 0;
        int width = 800;
        int height = 600;
        boolean center = false;
        boolean resizable = true;
        boolean maximized = false;
        boolean undecorated = false;
        boolean fullScreen = false;
        boolean preserveSplashScreen = false;
        boolean useApplicationInstance = false;

        try {
            Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
            preferences = preferences.node(applicationClassName);

            x = preferences.getInt(X_ARGUMENT, x);
            y = preferences.getInt(Y_ARGUMENT, y);
            width = preferences.getInt(WIDTH_ARGUMENT, width);
            height = preferences.getInt(HEIGHT_ARGUMENT, height);
            maximized = preferences.getBoolean(MAXIMIZED_ARGUMENT, maximized);
        } catch (SecurityException exception) {
            System.err.println("Unable to retrieve startup preferences: " + exception);
        }

        for (int i = 1; i < args.length; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                String[] property = arg.split("=", -1);

                String key = property[0];
                if (property.length == 2) {
                    String value = property[1];
                    if (value.isEmpty()) {
                        System.err.println(String.format(INVALID_PROPERTY_FORMAT_MESSAGE, arg));
                    } else {
                        try {
                            switch (key) {
                                case X_ARGUMENT:
                                    x = Integer.parseInt(value);
                                    break;
                                case Y_ARGUMENT:
                                    y = Integer.parseInt(value);
                                    break;
                                case WIDTH_ARGUMENT:
                                    width = Integer.parseInt(value);
                                    break;
                                case HEIGHT_ARGUMENT:
                                    height = Integer.parseInt(value);
                                    break;
                                case CENTER_ARGUMENT:
                                    center = StringUtils.toBoolean(value);
                                    break;
                                case RESIZABLE_ARGUMENT:
                                    resizable = StringUtils.toBoolean(value);
                                    break;
                                case MAXIMIZED_ARGUMENT:
                                    maximized = StringUtils.toBoolean(value);
                                    break;
                                case UNDECORATED_ARGUMENT:
                                    undecorated = StringUtils.toBoolean(value);
                                    break;
                                case FULL_SCREEN_ARGUMENT:
                                    fullScreen = StringUtils.toBoolean(value);
                                    break;
                                case PRESERVE_SPLASH_SCREEN_ARGUMENT:
                                    preserveSplashScreen = StringUtils.toBoolean(value);
                                    break;
                                case ORIGIN_ARGUMENT:
                                    origin = new URL(value);
                                    break;
                                case USE_APPLICATION_INSTANCE_ARGUMENT:
                                    useApplicationInstance = StringUtils.toBoolean(value);
                                    break;
                                default:
                                    properties.put(key, value);
                                    break;
                            }
                        } catch (Exception exception) {
                            System.err.println(String.format(INVALID_PROPERTY_VALUE_MESSAGE, value, key));
                        }
                    }
                } else if (property.length == 1) {
                    // Options of the form "--option" have a value of TRUE
                    properties.put(key, Boolean.TRUE.toString());
                } else {
                    System.err.println(String.format(INVALID_PROPERTY_FORMAT_MESSAGE, arg));
                }
            } else {
                System.err.println(String.format(INVALID_PROPERTY_FORMAT_MESSAGE, arg));
            }
        }

        // Start the timer
        createTimer();

        // Create the display host
        primaryDisplayHost = new DesktopDisplayHost();
        displays.add(primaryDisplayHost.getDisplay());

        // Create the windowed host frame
        windowedHostFrame = new HostFrame();
        windowedHostFrame.add(primaryDisplayHost);
        windowedHostFrame.setUndecorated(undecorated);

        windowedHostFrame.setTitle(DEFAULT_HOST_WINDOW_TITLE);
        windowedHostFrame.setSize(width, height);
        windowedHostFrame.setResizable(resizable);

        Rectangle screenSize = getVirtualDesktopBounds();
        if (center) {
            // Center on the virtual desktop (which could span multiple physical displays)
            windowedHostFrame.setLocation(
                ((screenSize.width - width) / 2) + screenSize.x,
                ((screenSize.height - height) / 2) + screenSize.y);
        } else {
            // Ensure that if the position was from a display configuration that is no longer
            // supported (like we last closed the app when there were two monitors and there
            // is now only one) that we don't end up completely offscreen.
            // This computation will always place the upper left corner of the app window somewhere
            // on the virtual screen (noting that the virtual x,y location could be negative when
            // secondary displays are left / above the primary one).
            if (x >= screenSize.width) {
                x = Math.max(screenSize.width - width, screenSize.x);
            }
            if (x < screenSize.x) {
                x = screenSize.x;
            }
            if (y >= screenSize.height) {
                y = Math.max(screenSize.height - height, screenSize.y);
            }
            if (y < screenSize.y) {
                y = screenSize.y;
            }
            windowedHostFrame.setLocation(x, y);
        }

        if (maximized) {
            windowedHostFrame.setExtendedState(MAXIMIZED_BOTH);
        }

        // Create the full-screen host frame
        fullScreenHostFrame = new HostFrame();
        fullScreenHostFrame.setUndecorated(true);

        // Add a key listener to the display host to toggle between full-screen
        // and windowed mode
        primaryDisplayHost.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_F
                    && (keyEvent.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) > 0
                    && (keyEvent.getModifiersEx() & InputEvent.SHIFT_DOWN_MASK) > 0) {
                    setFullScreen(!isFullScreen());
                }
            }
        });

        // Load the application
        try {
            Class<?> applicationClass = Class.forName(applicationClassName);
            // If the application has not already been set before calling this method...
            if (!useApplicationInstance) {
                application = (Application) applicationClass.getDeclaredConstructor().newInstance();
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
               | NoSuchMethodException | InvocationTargetException exception) {
            exception.printStackTrace();
        }

        if (application != null) {
            // Add the application to the application list
            applications.add(application);

            // Initialize OS-specific extensions
            initializeOSExtensions();

            // Don't make the window visible if there is a SplashScreen that the
            // application intends to use
            boolean visible = (!preserveSplashScreen || SplashScreen.getSplashScreen() == null);
            // Initial configuration of the windows
            setFullScreen(fullScreen, visible);

            // TODO This is a workaround for Java bug #6365898 on Linux (fixed only in Java 7),
            // revisit / remove later when we'll require Java 7
            if (maximized && visible) {
                windowedHostFrame.setExtendedState(MAXIMIZED_BOTH);
            }

            // Start the application in a callback to allow the host window to open first
            queueCallback(() -> {
                try {
                    application.startup(primaryDisplayHost.getDisplay(), new ImmutableMap<>(properties));
                } catch (Throwable exception) {
                    handleUncaughtException(exception);
                }
            });
        }
    }

    private static void initializeOSExtensions() {
        String osName = System.getProperty("os.name");

        if (osName.toLowerCase(Locale.ENGLISH).startsWith("mac os x")) {
            try {
                // Get the EAWT classes and methods
                Class<?> eawtApplicationClass = Class.forName("com.apple.eawt.Application");
                Class<?> eawtApplicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
                Class<?> eawtApplicationEventClass = Class.forName("com.apple.eawt.ApplicationEvent");

                Method setEnabledAboutMenuMethod = eawtApplicationClass.getMethod(
                    "setEnabledAboutMenu", new Class<?>[] {Boolean.TYPE});

                Method addApplicationListenerMethod = eawtApplicationClass.getMethod(
                    "addApplicationListener", new Class<?>[] {eawtApplicationListenerClass});

                final Method setHandledMethod = eawtApplicationEventClass.getMethod("setHandled",
                    new Class<?>[] {Boolean.TYPE});

                // Create the proxy handler
                InvocationHandler handler = new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                        boolean handled = true;

                        String methodName = method.getName();
                        if (methodName.equals("handleAbout")) {
                            Application.AboutHandler aboutHandler = (Application.AboutHandler) application;
                            aboutHandler.aboutRequested();
                        } else if (methodName.equals("handleQuit")) {
                            handled = !exit(true);
                        }

                        // Invoke setHandled()
                        setHandledMethod.invoke(args[0], new Object[] {Boolean.valueOf(handled)});

                        return null;
                    }
                };

                Object eawtApplication = eawtApplicationClass.getDeclaredConstructor().newInstance();

                setEnabledAboutMenuMethod.invoke(eawtApplication,
                    Boolean.valueOf(application instanceof Application.AboutHandler));

                Object eawtApplicationListener = Proxy.newProxyInstance(
                    DesktopApplicationContext.class.getClassLoader(),
                    new Class<?>[] {eawtApplicationListenerClass}, handler);

                // Invoke the addApplicationListener() method with the proxy listener
                addApplicationListenerMethod.invoke(eawtApplication,
                    new Object[] {eawtApplicationListener});
            } catch (Throwable throwable) {
                System.err.println("Unable to attach EAWT hooks: " + throwable);
            }
        }
    }

    /**
     * Returns the full-screen mode flag.
     * @return Whether or not the application is/should be displayed full screen.
     */
    public static boolean isFullScreen() {
        return fullScreenHostFrame.isVisible();
    }

    /**
     * Sets the full-screen mode flag.
     *
     * @param fullScreen Whether to display the application full screen.
     */
    public static void setFullScreen(boolean fullScreen) {
        setFullScreen(fullScreen, true);
    }

    private static void setFullScreen(boolean fullScreen, boolean visible) {
        GraphicsDevice graphicsDevice = windowedHostFrame.getGraphicsConfiguration().getDevice();

        if (fullScreen) {
            // Go to full screen mode
            if (windowedHostFrame.isVisible()) {
                windowedHostFrame.remove(primaryDisplayHost);
            }

            windowedHostFrame.setVisible(false);

            // Setting the full screen window now will cause a SplashScreen to
            // be dismissed, so don't do so if the --preserveSplashScreen
            // startup property was true, and a SplashScreen was supplied.
            // When the SplashScreen needs to be dismissed, users can call
            // replaceSplashScreen(Display) which will set the full screen
            // window, if required.
            if (visible) {
                graphicsDevice.setFullScreenWindow(fullScreenHostFrame);
            }

            fullScreenHostFrame.add(primaryDisplayHost);
            fullScreenHostFrame.setTitle(windowedHostFrame.getTitle());
            fullScreenHostFrame.setVisible(visible);
        } else {
            // Go to windowed mode
            if (fullScreenHostFrame.isVisible()) {
                fullScreenHostFrame.remove(primaryDisplayHost);
            }

            fullScreenHostFrame.setVisible(false);

            graphicsDevice.setFullScreenWindow(null);

            windowedHostFrame.add(primaryDisplayHost);
            windowedHostFrame.setTitle(fullScreenHostFrame.getTitle());
            windowedHostFrame.setVisible(visible);
        }

        primaryDisplayHost.requestFocusInWindow();
    }

    /**
     * Gets the window hosting the specified Display and makes it visible.<br>
     * This will cause a visible {@link SplashScreen} to be closed.<br> It is
     * intended to be called one time when the Pivot application has initialized
     * its UI and the SplashScreen is ready to be dismissed, but can be safely
     * called regardless of whether there is now, or used to be, a visible
     * SplashScreen.
     *
     * @param display Display to make visible
     * @see java.awt.SplashScreen
     */
    public static void replaceSplashScreen(Display display) {
        java.awt.Window hostWindow = display.getHostWindow();
        GraphicsDevice device = windowedHostFrame.getGraphicsConfiguration().getDevice();
        if ((hostWindow == fullScreenHostFrame) && (device.getFullScreenWindow() == null)) {
            device.setFullScreenWindow(fullScreenHostFrame);
        }
        hostWindow.setVisible(true);
    }

    /**
     * Sizes the window's native host frame to match its preferred size.
     *
     * @param window The window to size.
     * @throws IllegalArgumentException if the window parameter is {@code null}.
     * @throws IllegalStateException if the application is being displayed full screen.
     */
    public static void sizeHostToFit(Window window) {
        Utils.checkNull(window, "window");

        if (isFullScreen()) {
            throw new IllegalStateException("Full screen window cannot be sized to fit.");
        }

        Dimensions size = window.getPreferredSize();
        java.awt.Window hostWindow = window.getDisplay().getHostWindow();
        java.awt.Insets frameInsets = hostWindow.getInsets();
        hostWindow.setSize(size.width + (frameInsets.left + frameInsets.right), size.height
            + (frameInsets.top + frameInsets.bottom));
    }

    /**
     * Creates a new secondary display.
     *
     * @param width       The new width for the secondary display.
     * @param height      The height for the secondary display.
     * @param x           The new X-position for the display.
     * @param y           The new Y-position for the display.
     * @param modal       Whether or not the new display is to be modal.
     * @param resizable   Whether or not to make the new display resizable by the user.
     * @param undecorated Whether the new display should be undecorated (that is just
     *                    a bare window) or normal.
     * @param owner       The owner for the new display.
     * @param displayCloseListener The listener for the dialog being closed.
     * @return The newly created display.
     * @throws IllegalStateException if the full screen flag is set.
     */
    public static Display createDisplay(int width, int height, int x, int y, boolean modal,
        boolean resizable, boolean undecorated, java.awt.Window owner,
        DisplayListener displayCloseListener) {
        if (isFullScreen()) {
            throw new IllegalStateException(
                    "Secondary display cannot be created for full screen application.");
        }

        final HostDialog hostDialog = new HostDialog(owner, modal, displayCloseListener);
        hostDialog.setLocation(x, y);
        hostDialog.setSize(width, height);
        hostDialog.setResizable(resizable);
        hostDialog.setUndecorated(undecorated);

        // Open the window in a callback; otherwise, if it is modal, it will
        // block the calling thread
        ApplicationContext.queueCallback(() -> hostDialog.setVisible(true));

        return hostDialog.getDisplay();
    }

    /**
     * Utility method to make it easier to define <tt>main()</tt> entry-points
     * into applications. For example:
     * <pre> public class MyApp implements Application {
     *   public static void main(String[] args) throws Exception {
     *     DesktopApplicationContext.main(MyApp.class, args);
     *   }
     * } </pre>
     *
     * @param applicationClass the class of Application entry point
     * @param applicationArgs application arguments
     */
    public static final void main(Class<? extends Application> applicationClass,
        String[] applicationArgs) {
        String[] args = new String[applicationArgs.length + 1];
        System.arraycopy(applicationArgs, 0, args, 1, applicationArgs.length);
        args[0] = applicationClass.getName();
        main(args);
    }

    /**
     * Utility method to make it easier to define <tt>main()</tt> entry-points
     * into applications.<br> This is useful if application instance has
     * already been created, for example from a scripting environment and I set
     * some external properties in the application for later reuse, so I must
     * use that instance.<br> But it's important to NOT call usual methods of
     * application lifecycle before passing it here, to avoid side effects.
     *
     * @param applicationInstance an instance of Application entry point
     * @param applicationArgs application arguments
     */
    public static final void main(Application applicationInstance, String[] applicationArgs) {
        String[] args = new String[applicationArgs.length + 2];
        System.arraycopy(applicationArgs, 0, args, 1, applicationArgs.length);
        args[0] = applicationInstance.getClass().getName();
        args[applicationArgs.length + 1] = "--" + USE_APPLICATION_INSTANCE_ARGUMENT + "=" + "true";
        application = applicationInstance;
        main(args);
    }

}
