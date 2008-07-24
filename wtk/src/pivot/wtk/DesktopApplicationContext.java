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
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;

import pivot.collections.HashMap;

public final class DesktopApplicationContext extends ApplicationContext {
    private static class HostFrame extends java.awt.Frame {
        public static final long serialVersionUID = 0;

        public void update(Graphics graphics) {
            paint(graphics);
        }
    }

    private class WindowHandler implements WindowListener {
        public void windowOpened(WindowEvent event) {
            initialize(applicationClassName);
            startupApplication();
        }

        public void windowClosing(WindowEvent event) {
            java.awt.Window window = event.getWindow();
            window.setVisible(false);
            window.dispose();
        }

        public void windowClosed(WindowEvent event) {
            shutdownApplication();
            uninitialize();
            System.exit(0);
        }

        public void windowActivated(WindowEvent event) {
        }

        public void windowDeactivated(WindowEvent event) {
        }

        public void windowIconified(WindowEvent event) {
            suspendApplication();
        }

        public void windowDeiconified(WindowEvent event) {
            resumeApplication();
        }

    }

    private String applicationClassName = null;
    private HashMap<String, String> properties = null;
    private HostFrame hostFrame = new HostFrame();

    private DesktopApplicationContext(String applicationClassName,
        HashMap<String, String> properties) {
        this.applicationClassName = applicationClassName;
        this.properties = properties;

        // Add the display host to the frame
        hostFrame.add(displayHost);

        // Add window listeners
        hostFrame.addWindowListener(new WindowHandler());

        // Disable focus traversal keys
        hostFrame.setFocusTraversalKeysEnabled(false);

        // Clear the back ground and initialize frame attributes
        hostFrame.setBackground(null);
        hostFrame.setTitle("WTK Application");

        // TODO Create a Pivot icon to use here
        /*
        try {
            java.io.InputStream iconInputStream = getClass().getResourceAsStream("pivot.png");
            hostFrame.setIconImage(ImageIO.read(iconInputStream));
        } catch(Exception exception) {
        }
        */

        // TODO Preserve most recent size
        hostFrame.setSize(800, 600);

        // Open the window and focus the display host
        hostFrame.setVisible(true);
        displayHost.requestFocus();
    }

    public String getTitle() {
        return hostFrame.getTitle();
    }

    public void setTitle(String title) {
        hostFrame.setTitle(title);
    }

    public String getProperty(String name) {
        return properties.get(name);
    }

    public void open(URL location) {
    	try {
    	    Class<?> desktopClass = Class.forName("java.awt.Desktop");
    	    Method getDesktopMethod = desktopClass.getMethod("getDesktop",
    		    new Class<?>[] {});
    	    Method browseMethod = desktopClass.getMethod("browse",
    		    new Class[] { URI.class });
    	    Object desktop = getDesktopMethod.invoke(null, (Object[]) null);
    	    browseMethod.invoke(desktop, location.toURI());
    	} catch (Exception exception) {
    	    System.out.println("Unable to open URL in default browser.");
    	}
    }

    public void exit() {
        System.exit(0);
    }

    public static void main(String[] args) {
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

        if (applicationClassName == null) {
            System.out.println("Application class name is required.");
        } else {
            new DesktopApplicationContext(applicationClassName, properties);
        }
    }
}
