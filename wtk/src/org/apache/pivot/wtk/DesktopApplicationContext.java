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
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.immutable.ImmutableMap;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.Picture;

/**
 * Application context used to execute applications in a native frame
 * window.
 */
public final class DesktopApplicationContext extends ApplicationContext {
    private static class HostFrame extends java.awt.Frame {
        private static final long serialVersionUID = 5340356674429280196L;

        private HostFrame() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK
                | AWTEvent.WINDOW_STATE_EVENT_MASK);

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
        public void processWindowEvent(WindowEvent event) {
            super.processWindowEvent(event);

            if (this == windowedHostFrame) {
                switch(event.getID()) {
                    case WindowEvent.WINDOW_OPENED: {
                        addDisplay(applicationContext.getDisplay());
                        createTimer();

                        // Load the application
                        Application application = null;
                        try {
                            Class<?> applicationClass = Class.forName(applicationClassName);
                            application = (Application)applicationClass.newInstance();
                            applicationContext.setApplication(application);
                        } catch(Exception exception) {
                            Alert.alert(MessageType.ERROR, exception.getMessage(),
                                applicationContext.getDisplay());
                            exception.printStackTrace();
                        }

                        // Set focus to the display host
                        DisplayHost displayHost = applicationContext.getDisplayHost();
                        displayHost.requestFocus();

                        // Start the application
                        if (application != null) {
                            try {
                                application.startup(applicationContext.getDisplay(),
                                    new ImmutableMap<String, String>(properties));
                            } catch(Exception exception) {
                                displayException(exception);
                            }

                            // Add the application to the application list
                            addApplication(application);

                            // Hook into OS X application menu
                            String osName = System.getProperty("os.name");
                            if (osName.toLowerCase().startsWith("mac os x")) {
                                try {
                                    // Get the EAWT classes and methods
                                    Class<?> eawtApplicationClass = Class.forName("com.apple.eawt.Application");
                                    Class<?> eawtApplicationListenerClass = Class.forName("com.apple.eawt.ApplicationListener");
                                    Class<?> eawtApplicationEventClass = Class.forName("com.apple.eawt.ApplicationEvent");

                                    Method setEnabledAboutMenuMethod = eawtApplicationClass.getMethod("setEnabledAboutMenu",
                                        new Class<?>[] {Boolean.TYPE});

                                    Method addApplicationListenerMethod = eawtApplicationClass.getMethod("addApplicationListener",
                                        new Class<?>[] {eawtApplicationListenerClass});

                                    final Method setHandledMethod = eawtApplicationEventClass.getMethod("setHandled",
                                        new Class<?>[] {Boolean.TYPE});

                                    // Create the proxy handler
                                    InvocationHandler handler = new InvocationHandler() {
                                    @Override
                                        public Object invoke(Object proxy, Method method, Object[] args)
                                            throws Throwable {
                                            boolean handled = true;

                                            String methodName = method.getName();
                                            if (methodName.equals("handleAbout"))  {
                                                handleAbout();
                                            } else if (methodName.equals("handleQuit")) {
                                                handled = !exit();
                                            }

                                            // Invoke setHandled()
                                            setHandledMethod.invoke(args[0], new Object[] {handled});

                                            return null;
                                        }
                                    };

                                    Object eawtApplication = eawtApplicationClass.newInstance();

                                    setEnabledAboutMenuMethod.invoke(eawtApplication,
                                        application instanceof Application.AboutHandler);

                                    Object eawtApplicationListener =
                                        Proxy.newProxyInstance(DesktopApplicationContext.class.getClassLoader(),
                                            new Class<?>[]{eawtApplicationListenerClass}, handler);

                                    // Invoke the addApplicationListener() method with the proxy listener
                                    addApplicationListenerMethod.invoke(eawtApplication, new Object[] {eawtApplicationListener});
                                } catch(Throwable throwable) {
                                    throwable.printStackTrace(System.err);
                                }
                            }
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_CLOSING: {
                        exit();
                        break;
                    }

                    case WindowEvent.WINDOW_CLOSED: {
                        removeDisplay(applicationContext.getDisplay());
                        destroyTimer();
                        System.exit(0);
                        break;
                    }

                    case WindowEvent.WINDOW_DEACTIVATED: {
                        java.awt.Window oppositeWindow = event.getOppositeWindow();

                        if (oppositeWindow instanceof java.awt.Dialog) {
                            java.awt.Dialog dialog = (java.awt.Dialog)oppositeWindow;

                            switch (dialog.getModalityType()) {
                            case APPLICATION_MODAL:
                            case DOCUMENT_MODAL:
                            case TOOLKIT_MODAL:
                                applicationContext.getDisplay().setEnabled(false);

                                dialog.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowClosed(WindowEvent event) {
                                        applicationContext.getDisplay().setEnabled(true);
                                        event.getWindow().removeWindowListener(this);
                                    }
                                });

                                break;
                            }
                        }

                        break;
                    }
                }
            }
        }

        @Override
        protected void processWindowStateEvent(WindowEvent event) {
            super.processWindowStateEvent(event);

            if (this == windowedHostFrame) {
                Application application = applicationContext.getApplication();

                switch(event.getID()) {
                    case WindowEvent.WINDOW_ICONIFIED: {
                        try {
                            application.suspend();
                        } catch(Exception exception) {
                            displayException(exception);
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_DEICONIFIED: {
                        try {
                            application.resume();
                        } catch(Exception exception) {
                            displayException(exception);
                        }

                        break;
                    }
                }
            }
        }
    }

    private static DesktopApplicationContext applicationContext = null;
    private static String applicationClassName = null;
    private static HashMap<String, String> properties = null;

    private static HostFrame windowedHostFrame = null;
    private static HostFrame fullScreenHostFrame = null;

    private static Runnable updateFrameTitleBarCallback = null;

    private static final WindowListener TOP_WINDOW_LISTENER = new WindowListener.Adapter() {
        @Override
        public void titleChanged(Window window, String previousTitle) {
            updateFrameTitleBar(window.getRootOwner());
        }

        @Override
        public void iconChanged(Window window, Image previousIcon) {
            updateFrameTitleBar(window.getRootOwner());
        }
    };

    private static final String DEFAULT_HOST_FRAME_TITLE = "Apache Pivot";

    private static final String X_ARGUMENT = "x";
    private static final String Y_ARGUMENT = "y";
    private static final String WIDTH_ARGUMENT = "width";
    private static final String HEIGHT_ARGUMENT = "height";
    private static final String CENTER_ARGUMENT = "center";
    private static final String RESIZABLE_ARGUMENT = "resizable";

    private static final String FULL_SCREEN_ARGUMENT = "fullScreen";

    /**
     * Terminates the application context.
     */
    public static boolean exit() {
        boolean cancelShutdown = false;

        Application application = applicationContext.getApplication();
        if (application != null) {
            try {
                cancelShutdown = application.shutdown(true);
            } catch(Exception exception) {
                displayException(exception);
            }

            if (!cancelShutdown) {
                // Remove the application from the application list
                removeApplication(application);
            }
        }

        if (!cancelShutdown) {
            if (windowedHostFrame.getExtendedState() == 0) {
                try {
                    Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
                    preferences = preferences.node(applicationClassName);

                    preferences.putInt(X_ARGUMENT, windowedHostFrame.getX());
                    preferences.putInt(Y_ARGUMENT, windowedHostFrame.getY());
                    preferences.putInt(WIDTH_ARGUMENT, windowedHostFrame.getWidth());
                    preferences.putInt(HEIGHT_ARGUMENT, windowedHostFrame.getHeight());

                    preferences.flush();
                } catch (SecurityException exception) {
                    // No-op
                } catch (BackingStoreException exception) {
                    // No-op
                }
            }

            windowedHostFrame.dispose();
            fullScreenHostFrame.dispose();
        }

        return cancelShutdown;
    }

    /**
     * Primary aplication entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        if (applicationContext != null) {
            throw new IllegalStateException();
        }

        // Get the application class name
        if (args.length == 0) {
            System.err.println("Application class name is required.");
            return;
        }

        applicationClassName = args[0];

        // Get the startup properties
        properties = new HashMap<String, String>();

        int x = 0;
        int y = 0;
        int width = 800;
        int height = 600;
        boolean center = false;
        boolean resizable = true;
        boolean fullScreen = false;

        try {
            Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
            preferences = preferences.node(applicationClassName);

            x = preferences.getInt(X_ARGUMENT, x);
            y = preferences.getInt(Y_ARGUMENT, y);
            width = preferences.getInt(WIDTH_ARGUMENT, width);
            height = preferences.getInt(HEIGHT_ARGUMENT, height);
        } catch (SecurityException exception) {
            System.err.println("Unable to retrieve startup preferences: " + exception);
        }

        for (int i = 1, n = args.length; i < n; i++) {
            String arg = args[i];

            if (!arg.startsWith("--")) {
                throw new IllegalArgumentException("Startup property names must begin with \"--\".");
            }

            arg = arg.substring(2);
            String[] property = arg.split("=");

            if (property.length == 2) {
                String key = property[0];
                String value = property[1];

                try {
                    if (key.equals(X_ARGUMENT)) {
                        x = Integer.parseInt(value);
                    } else if (key.equals(Y_ARGUMENT)) {
                        y = Integer.parseInt(value);
                    } else if (key.equals(WIDTH_ARGUMENT)) {
                        width = Integer.parseInt(value);
                    } else if (key.equals(HEIGHT_ARGUMENT)) {
                        height = Integer.parseInt(value);
                    } else if (key.equals(CENTER_ARGUMENT)) {
                        center = Boolean.parseBoolean(value);
                    } else if (key.equals(RESIZABLE_ARGUMENT)) {
                        resizable = Boolean.parseBoolean(value);
                    } else if (key.equals(FULL_SCREEN_ARGUMENT)) {
                        fullScreen = Boolean.parseBoolean(value);
                    } else {
                        properties.put(key, value);
                    }
                } catch (NumberFormatException exception) {
                    System.err.println("Warning: " + exception.getClass().getSimpleName() + ": " +
                        exception.getLocalizedMessage());
                }
            } else {
                System.err.println(arg + " is not a valid startup property.");
            }
        }

        // Set the origin
        try {
            // Load the JNLP classes dynamically because they are only available
            // when run via javaws
            Class<?> serviceManagerClass = Class.forName("javax.jnlp.ServiceManager");
            Method lookupMethod = serviceManagerClass.getMethod("lookup", String.class);
            Object basicService = lookupMethod.invoke(null, "javax.jnlp.BasicService");

            Class<?> basicServiceClass = Class.forName("javax.jnlp.BasicService");
            Method getCodeBaseMethod = basicServiceClass.getMethod("getCodeBase");
            URL codeBase = (URL)getCodeBaseMethod.invoke(basicService);

            if (codeBase != null) {
                origin = new URL(codeBase.getProtocol(), codeBase.getHost(), codeBase.getPort(), "");
            }
        } catch (Exception exception) {
            // No-op
        }

        if (origin == null) {
            // Could not obtain origin from JNLP; use user's home directory
            File userHome = new File(System.getProperty("user.home"));

            try {
                origin = userHome.toURI().toURL();
            } catch(MalformedURLException exception) {
                // No-op
            }
        }

        // Create the application context
        applicationContext = new DesktopApplicationContext();
        DisplayHost displayHost = applicationContext.getDisplayHost();

        // Create the windowed host frame
        windowedHostFrame = new HostFrame();
        windowedHostFrame.add(displayHost);

        windowedHostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);
        windowedHostFrame.setSize(width, height);
        windowedHostFrame.setResizable(resizable);

        if (center) {
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            windowedHostFrame.setLocation((screenSize.width - width) / 2,
                (screenSize.height - height) / 2);
        } else {
            windowedHostFrame.setLocation(x, y);
        }

        applicationContext.getDisplay().getContainerListeners().add(new ContainerListener.Adapter() {
            @Override
            public void componentInserted(Container container, int index) {
                if (index == container.getLength() - 1) {
                    topWindowChanged((Display)container,
                        (index > 0) ? (Window)container.get(index - 1) : null);
                }
            }

            @Override
            public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
                int n = removed.getLength();

                if (index + n == container.getLength()) {
                    topWindowChanged((Display)container, (Window)removed.get(n - 1));
                }
            }

            @Override
            public void componentMoved(Container container, int from, int to) {
                int n = container.getLength();

                if (from == n - 1) {
                    // The top-most window was moved elsewhere
                    topWindowChanged((Display)container, (Window)container.get(from));
                } else if (to == n - 1) {
                    // A new window was moved to top
                    topWindowChanged((Display)container,
                        (to > 0) ? (Window)container.get(to - 1) : null);
                }
            }

            private void topWindowChanged(Display display, final Window previousTopWindow) {
                if (updateFrameTitleBarCallback == null) {
                    updateFrameTitleBarCallback = new Runnable() {
                        @Override
                        public void run() {
                            if (previousTopWindow != null) {
                                Window previousRootOwner = previousTopWindow.getRootOwner();
                                previousRootOwner.getWindowListeners().remove(TOP_WINDOW_LISTENER);
                            }

                            Display display = applicationContext.getDisplay();
                            int n = display.getLength();

                            if (n == 0) {
                                windowedHostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);
                                windowedHostFrame.setIconImage(null);
                            } else {
                                Window topWindow = (Window)display.get(n - 1);
                                Window rootOwner = topWindow.getRootOwner();
                                updateFrameTitleBar(rootOwner);

                                rootOwner.getWindowListeners().add(TOP_WINDOW_LISTENER);
                            }

                            updateFrameTitleBarCallback = null;
                        }
                    };

                    queueCallback(updateFrameTitleBarCallback);
                }
            }
        });

        // Add a key listener to the display host to toggle between full-screen
        // and windowed mode
        displayHost.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_F
                    && (keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0
                    && (keyEvent.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
                    setFullScreen(!isFullScreen());
                }
            }
        });

        // Create the full-screen host frame
        fullScreenHostFrame = new HostFrame();
        fullScreenHostFrame.setUndecorated(true);

        // Open the windowed host
        windowedHostFrame.setVisible(true);

        // Go to full-screen mode, if requested
        setFullScreen(fullScreen);
    }

    private static void updateFrameTitleBar(Window rootOwner) {
        windowedHostFrame.setTitle(rootOwner.getTitle());

        Image icon = rootOwner.getIcon();
        if (icon instanceof Picture) {
            Picture rootPicture = (Picture)icon;
            windowedHostFrame.setIconImage(rootPicture.getBufferedImage());
        }
    }

    private static void displayException(Exception exception) {
        exception.printStackTrace();

        String message = exception.getClass().getName();

        Label body = null;
        String bodyText = exception.getMessage();
        if (bodyText != null
            && bodyText.length() > 0) {
            body = new Label(bodyText);
            body.getStyles().put("wrapText", true);
        }

        Alert.alert(MessageType.ERROR, message, body, applicationContext.getDisplay());
    }

    /**
     * Invokes the application's about handler. The application must implement
     * the {@link Application.AboutHandler} interface.
     */
    public static void handleAbout() {
        Application application = applicationContext.getApplication();
        Application.AboutHandler aboutHandler = (Application.AboutHandler)application;
        aboutHandler.aboutRequested();
    }

    /**
     * Returns the full-screen mode flag.
     */
    public static boolean isFullScreen() {
        return (!windowedHostFrame.isVisible());
    }

    /**
     * Sets the full-screen mode flag.
     *
     * @param fullScreen
     */
    public static void setFullScreen(boolean fullScreen) {
        if (fullScreen != isFullScreen()) {
            DisplayHost displayHost = applicationContext.getDisplayHost();

            GraphicsDevice graphicsDevice =
                windowedHostFrame.getGraphicsConfiguration().getDevice();

            if (fullScreen) {
                // Go to full screen mode
                windowedHostFrame.remove(displayHost);
                windowedHostFrame.setVisible(false);

                fullScreenHostFrame.add(displayHost);
                fullScreenHostFrame.setVisible(true);

                graphicsDevice.setFullScreenWindow(fullScreenHostFrame);

            } else {
                // Go to windowed mode
                try {
                    graphicsDevice.setFullScreenWindow(null);
                } catch (Exception exception) {
                    // TODO remove this catch. On Win32 platforms, the
                    // preceding call can throw.
                }

                fullScreenHostFrame.remove(displayHost);
                fullScreenHostFrame.setVisible(false);

                windowedHostFrame.add(displayHost);
                windowedHostFrame.setVisible(true);
            }

            displayHost.requestFocus();
        }
    }

    public static void sizeToFit(Window window) {
        if (window == null) {
            throw new IllegalArgumentException();
        }

        if (applicationContext == null) {
            throw new IllegalStateException("Desktop application context is not active.");
        }

        if (isFullScreen()) {
            setFullScreen(false);
        }

        Dimensions size = window.getPreferredSize();
        java.awt.Insets frameInsets = windowedHostFrame.getInsets();
        windowedHostFrame.setSize(size.width + (frameInsets.left + frameInsets.right),
            size.height + (frameInsets.top + frameInsets.bottom));
    }

    /**
     * Utility method to make it easier to define <tt>main()</tt> entry-points
     * into applications. For example:
     *
     * <code>
     * public class MyApp implements Application {
     *   public static void main(String[] args) throws Exception {
     *     DesktopApplicationContext.main(MyApp.class, args);
     *   }
     * }
     * </code>
     *
     * @param applicationClass
     * @param applicationArgs
     */
    public static final void main(Class<? extends Application> applicationClass,
        String[] applicationArgs) {
        String[] args = new String[applicationArgs.length + 1];
        System.arraycopy(applicationArgs, 0, args, 1, applicationArgs.length);
        args[0] = applicationClass.getName();
        main(args);
    }
}
