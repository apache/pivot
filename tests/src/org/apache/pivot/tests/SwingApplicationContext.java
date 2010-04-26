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
package org.apache.pivot.tests;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.immutable.ImmutableMap;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Window;

/**
 * Application context used to execute applications in a Swing frame
 * window.
 */
public final class SwingApplicationContext extends ApplicationContext {
    private static class HostFrame extends javax.swing.JFrame {
        private static final long serialVersionUID = 0;

        private HostFrame() {
            enableEvents(AWTEvent.WINDOW_EVENT_MASK);

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
            }
        }
    }

    private static SwingApplicationContext applicationContext = null;
    private static String applicationClassName = null;
    private static HashMap<String, String> properties = null;
    private static HostFrame hostFrame = null;

    private static final String DEFAULT_HOST_FRAME_TITLE = "Apache Pivot in JFrame";

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
        final String STARTUP_PROPERTY_WARNING = "\"%s\" is not a valid startup property (expected"
            + " format is \"--name=value\").";

        properties = new HashMap<String, String>();

        for (int i = 1, n = args.length; i < n; i++) {
            String arg = args[i];

            if (arg.startsWith("--")) {
                arg = arg.substring(2);
                String[] property = arg.split("=");

                if (property.length == 2) {
                    String key = property[0];
                    String value = property[1];
                    properties.put(key, value);
                } else {
                    System.err.println(String.format(STARTUP_PROPERTY_WARNING, arg));
                }
            } else {
                System.err.println(String.format(STARTUP_PROPERTY_WARNING, arg));
            }
        }

        // Create the application context
        applicationContext = new SwingApplicationContext();
        DisplayHost displayHost = applicationContext.getDisplayHost();

        // Create the host frame
        hostFrame = new HostFrame();
        createMenuBar();

        hostFrame.add(displayHost);
        hostFrame.setTitle(DEFAULT_HOST_FRAME_TITLE);
        hostFrame.setSize(800, 600);
        hostFrame.setResizable(true);
        hostFrame.setVisible(true);
    }

    private static void createMenuBar() {
        // NOTE Code taken from Swing menu tutorial:
        // http://java.sun.com/docs/books/tutorial/uiswing/components/menu.html

        // Where the GUI is created:
        JMenuBar menuBar;
        JMenu menu, submenu;
        JMenuItem menuItem;
        JRadioButtonMenuItem rbMenuItem;
        JCheckBoxMenuItem cbMenuItem;

        // Create the menu bar.
        menuBar = new JMenuBar();

        // Build the first menu.
        menu = new JMenu("A Menu");
        menu.setMnemonic(KeyEvent.VK_A);
        menu.getAccessibleContext().setAccessibleDescription(
                "The only menu in this program that has menu items");
        menuBar.add(menu);

        // a group of JMenuItems
        menuItem = new JMenuItem("A text-only menu item", KeyEvent.VK_T);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.ALT_MASK));
        menuItem.getAccessibleContext().setAccessibleDescription("This doesn't really do anything");
        menu.add(menuItem);

        menuItem = new JMenuItem("Both text and icon", new ImageIcon("images/middle.gif"));
        menuItem.setMnemonic(KeyEvent.VK_B);
        menu.add(menuItem);

        menuItem = new JMenuItem(new ImageIcon("images/middle.gif"));
        menuItem.setMnemonic(KeyEvent.VK_D);
        menu.add(menuItem);

        // a group of radio button menu items
        menu.addSeparator();
        ButtonGroup group = new ButtonGroup();
        rbMenuItem = new JRadioButtonMenuItem("A radio button menu item");
        rbMenuItem.setSelected(true);
        rbMenuItem.setMnemonic(KeyEvent.VK_R);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        rbMenuItem = new JRadioButtonMenuItem("Another one");
        rbMenuItem.setMnemonic(KeyEvent.VK_O);
        group.add(rbMenuItem);
        menu.add(rbMenuItem);

        // a group of check box menu items
        menu.addSeparator();
        cbMenuItem = new JCheckBoxMenuItem("A check box menu item");
        cbMenuItem.setMnemonic(KeyEvent.VK_C);
        menu.add(cbMenuItem);

        cbMenuItem = new JCheckBoxMenuItem("Another one");
        cbMenuItem.setMnemonic(KeyEvent.VK_H);
        menu.add(cbMenuItem);

        // a submenu
        menu.addSeparator();
        submenu = new JMenu("A submenu");
        submenu.setMnemonic(KeyEvent.VK_S);

        menuItem = new JMenuItem("An item in the submenu");
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, ActionEvent.ALT_MASK));
        submenu.add(menuItem);

        menuItem = new JMenuItem("Another item");
        submenu.add(menuItem);
        menu.add(submenu);

        // Build second menu in the menu bar.
        menu = new JMenu("Another Menu");
        menu.setMnemonic(KeyEvent.VK_N);
        menu.getAccessibleContext().setAccessibleDescription(
                "This menu does nothing");
        menuBar.add(menu);
        hostFrame.setJMenuBar(menuBar);
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

    public static void sizeToFit(Window window) {
        if (window == null) {
            throw new IllegalArgumentException();
        }

        if (applicationContext == null) {
            throw new IllegalStateException("Desktop application context is not active.");
        }

        Dimensions size = window.getPreferredSize();
        java.awt.Insets frameInsets = hostFrame.getInsets();
        hostFrame.setSize(size.width + (frameInsets.left + frameInsets.right),
            size.height + (frameInsets.top + frameInsets.bottom));
    }
}