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

import java.awt.Graphics;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import pivot.collections.HashMap;

public final class DesktopApplicationContext extends ApplicationContext {
    private static class HostFrame extends java.awt.Frame {
        public static final long serialVersionUID = 0;

        public void update(Graphics graphics) {
            paint(graphics);
        }
    }

    private static class WindowHandler implements WindowListener {
        public void windowOpened(WindowEvent event) {
            try {
                application.startup(applicationContext.getDisplay(), properties);
            } catch(Exception exception) {
                displaySystemError(exception);
            }
        }

        public void windowClosing(WindowEvent event) {
            boolean shutdown = true;

            try {
                shutdown = application.shutdown(false);
            } catch(Exception exception) {
                displaySystemError(exception);
            }

            if (shutdown) {
                java.awt.Window window = event.getWindow();
                window.setVisible(false);
                window.dispose();
            }
        }

        public void windowClosed(WindowEvent event) {
            exit();
        }

        public void windowActivated(WindowEvent event) {
        }

        public void windowDeactivated(WindowEvent event) {
        }

        public void windowIconified(WindowEvent event) {
            try {
                application.suspend();
            } catch(Exception exception) {
                displaySystemError(exception);
            }
        }

        public void windowDeiconified(WindowEvent event) {
            try {
                application.resume();
            } catch(Exception exception) {
                displaySystemError(exception);
            }
        }
    }

    private static DesktopApplicationContext applicationContext = null;
    private static HashMap<String, String> properties = null;
    private static Application application = null;

    public static void main(String[] args) throws Exception {
        // Get the application class name and startup properties
        String applicationClassName = null;
        HashMap<String, String> properties = new HashMap<String, String>();

        for (int i = 0, n = args.length; i < n; i++) {
            String arg = args[i];

            if (i == 0) {
                applicationClassName = arg;
            } else {
                String[] property = arg.split(":");

                if (property.length == 2) {
                    String key = property[0];
                    String value = property[1];
                    properties.put(key, value);
                } else {
                    System.out.println(arg + " is not a valid startup property.");
                }
            }
        }

        // Create the application context
        applicationContext = new DesktopApplicationContext();
        ApplicationContext.current = applicationContext;

        // Load the application
        if (applicationClassName == null) {
            System.out.println("Application class name is required.");
        } else {
            Class<?> applicationClass = Class.forName(applicationClassName);
            application = (Application)applicationClass.newInstance();
        }

        // Create the host frame
        HostFrame hostFrame = new HostFrame();

        // Add the display host to the frame
        DisplayHost displayHost = applicationContext.getDisplayHost();
        hostFrame.add(displayHost);

        // Add window listeners
        hostFrame.addWindowListener(new WindowHandler());

        // Disable focus traversal keys
        hostFrame.setFocusTraversalKeysEnabled(false);

        // Clear the back ground and initialize frame attributes
        hostFrame.setBackground(null);
        hostFrame.setTitle("WTK Application"); // TODO i18n

        // TODO Preserve most recent size
        hostFrame.setSize(800, 600);

        // Open the window and focus the display host
        hostFrame.setVisible(true);
        displayHost.requestFocus();
    }
}
