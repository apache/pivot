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
package org.apache.pivot.ui;

import java.awt.Graphics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.apache.pivot.scene.AWTStageHost;
import org.apache.pivot.scene.media.Image;

/**
 * Application context used to execute applications in a native frame window.
 */
public class DesktopApplicationContext {
    private static Application application = null;

    private static java.awt.Frame hostFrame = new java.awt.Frame() {
        private static final long serialVersionUID = 0;

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }
    };

    public static final String X_ARGUMENT = "x";
    public static final String Y_ARGUMENT = "y";
    public static final String WIDTH_ARGUMENT = "width";
    public static final String HEIGHT_ARGUMENT = "height";
    public static final String CENTER_ARGUMENT = "center";
    public static final String MAXIMIZED_ARGUMENT = "maximized";
    public static final String RESIZABLE_ARGUMENT = "resizable";
    public static final String UNDECORATED_ARGUMENT = "undecorated";

    private static final String INVALID_PROPERTY_FORMAT_MESSAGE = "\"%s\" is not a valid startup "
        + "property (expected format is \"--name=value\").";
    private static final String INVALID_PROPERTY_VALUE_MESSAGE = "\"%s\" is not a valid value for "
        + "startup property \"%s\".";

    public static Application getApplication() {
        return application;
    }

    public static java.awt.Frame getHostFrame() {
        return hostFrame;
    }

    /**
     * Primary aplication entry point.
     *
     * @param args
     */
    public static void main(String[] args) {
        // Load the application
        if (args.length == 0) {
            System.err.println("Application class name is required.");
            return;
        }

        String applicationClassName = args[0];

        try {
            Class<?> applicationClass = Class.forName(applicationClassName);
            application = (Application)applicationClass.newInstance();
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }

        // Get the startup properties
        HashMap<String, String> properties = new HashMap<String, String>();

        int x = 0;
        int y = 0;
        int width = 800;
        int height = 600;
        boolean center = false;
        boolean maximized = false;
        boolean resizable = true;
        boolean undecorated = false;

        try {
            Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
            preferences = preferences.node(applicationClassName);

            x = preferences.getInt(X_ARGUMENT, x);
            y = preferences.getInt(Y_ARGUMENT, y);
            width = preferences.getInt(WIDTH_ARGUMENT, width);
            height = preferences.getInt(HEIGHT_ARGUMENT, height);
            maximized = preferences.getBoolean(MAXIMIZED_ARGUMENT, maximized);

            // Update positioning if window is off-screen
            GraphicsDevice[] screenDevices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
            if (screenDevices.length == 1) {
                if (x < 0) {
                    x = 0;
                }

                if (y < 0) {
                    y = 0;
                }
            }
        } catch (SecurityException exception) {
            System.err.println("Unable to retrieve startup preferences: " + exception);
        }

        for (int i = 1, n = args.length; i < n; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
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
                        } else if (key.equals(MAXIMIZED_ARGUMENT)) {
                            maximized = Boolean.parseBoolean(value);
                        } else if (key.equals(RESIZABLE_ARGUMENT)) {
                            resizable = Boolean.parseBoolean(value);
                        } else if (key.equals(UNDECORATED_ARGUMENT)) {
                            undecorated = Boolean.parseBoolean(value);
                        } else {
                            properties.put(key, value);
                        }
                    } catch (Exception exception) {
                        System.err.println(String.format(INVALID_PROPERTY_VALUE_MESSAGE, value, key));
                    }
                } else {
                    System.err.println(String.format(INVALID_PROPERTY_FORMAT_MESSAGE, arg));
                }
            } else {
                System.err.println(String.format(INVALID_PROPERTY_FORMAT_MESSAGE, arg));
            }
        }

        // Add stage host to host frame
        AWTStageHost awtStageHost = new AWTStageHost();
        awtStageHost.getStage().setLayout(new StageLayout());
        hostFrame.add(awtStageHost);

        // Add host frame listeners
        hostFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent event) {
                event.getWindow().requestFocus();
            }

            @Override
            public void windowClosing(WindowEvent arg0) {
                exit();
            }

            @Override
            public void windowClosed(WindowEvent arg0) {
                System.exit(0);
            }

            @Override
            public void windowIconified(WindowEvent arg0) {
                try {
                    application.suspend();
                } catch(Exception exception) {
                    exception.printStackTrace();
                }
            }

            @Override
            public void windowDeiconified(WindowEvent arg0) {
                try {
                    application.resume();
                } catch(Exception exception) {
                    exception.printStackTrace();
                }
            }
        });

        // Set the host frame properties
        hostFrame.setTitle(application.getTitle());

        List<Image> icons = application.getIcons();
        if (icons != null) {
            ArrayList<java.awt.Image> iconImages = new ArrayList<java.awt.Image>(icons.size());
            for (Image image : icons) {
                iconImages.add((BufferedImage)image.getRaster().getNativeRaster());
            }

            hostFrame.setIconImages(iconImages);
        } else {
            hostFrame.setIconImages(null);
        }

        hostFrame.setFocusTraversalKeysEnabled(false);
        hostFrame.setBackground(null);

        hostFrame.setSize(width, height);

        if (center) {
            java.awt.Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
            hostFrame.setLocation((screenSize.width - width) / 2,
                (screenSize.height - height) / 2);
        } else {
            hostFrame.setLocation(x, y);
        }

        if (maximized) {
            hostFrame.setExtendedState(java.awt.Frame.MAXIMIZED_BOTH);
        }

        hostFrame.setResizable(resizable);
        hostFrame.setUndecorated(undecorated);

        // Show the host frame
        hostFrame.setVisible(true);

        // Start up the application
        try {
            application.startup(awtStageHost.getStage(), properties);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    /**
     * Terminates the application context.
     */
    public static boolean exit() {
        boolean cancelShutdown = false;

        if (application != null) {
            // Shut down the application
            try {
                cancelShutdown = application.shutdown(true);
            } catch(Exception exception) {
                exception.printStackTrace();
            }

            if (!cancelShutdown) {
                try {
                    Preferences preferences = Preferences.userNodeForPackage(DesktopApplicationContext.class);
                    preferences = preferences.node(application.getClass().getName());

                    boolean maximized = (hostFrame.getExtendedState()
                        & java.awt.Frame.MAXIMIZED_BOTH) == java.awt.Frame.MAXIMIZED_BOTH;
                    if (!maximized) {
                        preferences.putInt(X_ARGUMENT, hostFrame.getX());
                        preferences.putInt(Y_ARGUMENT, hostFrame.getY());
                        preferences.putInt(WIDTH_ARGUMENT, hostFrame.getWidth());
                        preferences.putInt(HEIGHT_ARGUMENT, hostFrame.getHeight());
                    }

                    preferences.putBoolean(MAXIMIZED_ARGUMENT, maximized);

                    preferences.flush();
                } catch (SecurityException exception) {
                    // No-op
                } catch (BackingStoreException exception) {
                    // No-op
                }
            }
        }

        if (!cancelShutdown) {
            hostFrame.dispose();
        }

        return cancelShutdown;
    }
}
