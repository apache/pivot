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
package pivot.wtk;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

import pivot.collections.HashMap;
import pivot.collections.immutable.ImmutableMap;
import pivot.wtk.media.Image;
import pivot.wtk.media.Picture;

/**
 * Application context used to execute applications in a native frame
 * window.
 *
 * @author gbrown
 */
public final class DesktopApplicationContext extends ApplicationContext {
    private static class HostFrame extends java.awt.Frame {
        private static final long serialVersionUID = 0;

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
                        applicationContext.getDisplayHost().requestFocus();

                        createTimer();

                        try {
                            application.startup(applicationContext.getDisplay(),
                                new ImmutableMap<String, String>(properties));
                        } catch(Exception exception) {
                            exception.printStackTrace();
                            String message = exception.getMessage();
                            if (message == null) {
                                message = exception.getClass().getName();
                            }

                            Alert.alert(MessageType.ERROR, message, applicationContext.getDisplay());
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_CLOSING: {
                        boolean shutdown = true;

                        try {
                            shutdown = application.shutdown(true);
                        } catch(Exception exception) {
                            exception.printStackTrace();
                            Alert.alert(MessageType.ERROR, exception.getMessage(),
                                applicationContext.getDisplay());
                        }

                        if (shutdown) {
                            destroyTimer();

                            java.awt.Window window = event.getWindow();
                            window.setVisible(false);
                            window.dispose();
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_CLOSED: {
                        exit();
                        break;
                    }
                }
            }
        }

        @Override
        protected void processWindowStateEvent(WindowEvent event) {
            super.processWindowStateEvent(event);

            if (this == windowedHostFrame) {
                switch(event.getID()) {
                    case WindowEvent.WINDOW_ICONIFIED: {
                        try {
                            application.suspend();
                        } catch(Exception exception) {
                            exception.printStackTrace();
                            Alert.alert(MessageType.ERROR, exception.getMessage(),
                                applicationContext.getDisplay());
                        }

                        break;
                    }

                    case WindowEvent.WINDOW_DEICONIFIED: {
                        try {
                            application.resume();
                        } catch(Exception exception) {
                            exception.printStackTrace();
                            Alert.alert(MessageType.ERROR, exception.getMessage(),
                                applicationContext.getDisplay());
                        }

                        break;
                    }
                }
            }
        }
    }

    private static DesktopApplicationContext applicationContext = null;
    private static HashMap<String, String> properties = null;
    private static Application application = null;

    private static HostFrame windowedHostFrame = null;
    private static HostFrame fullScreenHostFrame = null;

    private static int x = 0;
    private static int y = 0;
    private static int width = 800;
    private static int height = 600;
    private static boolean center = false;
    private static boolean resizable = true;

    private static final String DEFAULT_HOST_FRAME_TITLE = "Pivot"; // TODO i18n

    private static final String X_ARGUMENT = "x";
    private static final String Y_ARGUMENT = "y";
    private static final String WIDTH_ARGUMENT = "width";
    private static final String HEIGHT_ARGUMENT = "height";
    private static final String CENTER_ARGUMENT = "center";
    private static final String RESIZABLE_ARGUMENT = "resizable";

    /**
     * Terminates the application context.
     */
    public static void exit() {
        System.exit(0);
    }

    public static void main(String[] args) {
        if (application != null) {
            throw new IllegalStateException();
        }

        // Get the application class name and startup properties
        String applicationClassName = null;
        properties = new HashMap<String, String>();

        for (int i = 0, n = args.length; i < n; i++) {
            String arg = args[i];

            if (i == 0) {
                applicationClassName = arg;
            } else {
                String[] property = arg.split(":");

                if (property.length == 2) {
                    String key = property[0];
                    String value = property[1];

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
                    } else {
                        properties.put(key, value);
                    }
                } else {
                    System.err.println(arg + " is not a valid startup property.");
                }
            }
        }

        // Set the origin
        try {
            // Load the JNLP classes dynamically because they are only available
            // when run via javaws
            Class<?> serviceManagerClass = Class.forName("javax.jnlp.ServiceManager");
            Method lookupMethod = serviceManagerClass.getMethod("lookup", new Class<?>[] {String.class});
            Object basicService = lookupMethod.invoke(null, "javax.jnlp.BasicService");

            Class<?> basicServiceClass = Class.forName("javax.jnlp.BasicService");
            Method getCodeBaseMethod = basicServiceClass.getMethod("getCodeBase", new Class<?>[] {});
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

        // Load the application
        if (applicationClassName == null) {
            System.err.println("Application class name is required.");
        } else {
            try {
                Class<?> applicationClass = Class.forName(applicationClassName);
                application = (Application)applicationClass.newInstance();
            } catch(Exception exception) {
                Alert.alert(MessageType.ERROR, exception.getMessage(),
                    applicationContext.getDisplay());
                exception.printStackTrace();
            }
        }

        final DisplayHost displayHost = applicationContext.getDisplayHost();

        // Create the windowed host frame
        windowedHostFrame = new HostFrame();
        windowedHostFrame.add(displayHost);

        windowedHostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);

        windowedHostFrame.setSize(width, height);
        windowedHostFrame.setResizable(resizable);

        if (center) {
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            windowedHostFrame.setLocation((screenSize.width - width) / 2, (screenSize.height - height) / 2);
        } else {
            windowedHostFrame.setLocation(x, y);
        }

        // Add a key listener to the display host to toggle between full-screen
        // and windowed mode
        displayHost.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent keyEvent) {
                if (keyEvent.getKeyCode() == KeyEvent.VK_F
                    && (keyEvent.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0
                    && (keyEvent.getModifiersEx() & KeyEvent.SHIFT_DOWN_MASK) > 0) {
                    GraphicsDevice graphicsDevice =
                        windowedHostFrame.getGraphicsConfiguration().getDevice();

                    if (windowedHostFrame.isVisible()) {
                        // Go to full screen mode
                        windowedHostFrame.remove(displayHost);
                        windowedHostFrame.setVisible(false);

                        fullScreenHostFrame.add(displayHost);
                        fullScreenHostFrame.setVisible(true);

                        graphicsDevice.setFullScreenWindow(fullScreenHostFrame);

                    } else {
                        // Go to windowed mode
                        graphicsDevice.setFullScreenWindow(null);

                        fullScreenHostFrame.remove(displayHost);
                        fullScreenHostFrame.setVisible(false);

                        windowedHostFrame.add(displayHost);
                        windowedHostFrame.setVisible(true);
                    }

                    displayHost.requestFocus();
                }
            }
        });

        // Create the full-screen host frame
        fullScreenHostFrame = new HostFrame();
        fullScreenHostFrame.setUndecorated(true);

        // Open the windowed host
        windowedHostFrame.setVisible(true);

        Window.getWindowClassListeners().add(new WindowClassListener() {
            public void activeWindowChanged(Window previousActiveWindow) {
                ApplicationContext.queueCallback(new Runnable() {
                    public void run() {
                        Window activeWindow = Window.getActiveWindow();

                        if (activeWindow == null) {
                            windowedHostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);
                        } else {
                            Window rootOwner = activeWindow.getRootOwner();
                            windowedHostFrame.setTitle(rootOwner.getTitle());

                            Image rootIcon = rootOwner.getIcon();
                            if (rootIcon instanceof Picture) {
                                Picture rootPicture = (Picture)rootIcon;
                                windowedHostFrame.setIconImage(rootPicture.getBufferedImage());
                            }
                        }
                    }
                });
            }
        });
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
    public static void main(Class<? extends Application> applicationClass,
        String[] applicationArgs) {
        String[] args = new String[applicationArgs.length + 1];
        System.arraycopy(applicationArgs, 0, args, 1, applicationArgs.length);
        args[0] = applicationClass.getName();
        main(args);
    }
}
