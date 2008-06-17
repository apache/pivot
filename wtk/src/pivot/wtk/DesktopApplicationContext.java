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

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.imageio.ImageIO;

public final class DesktopApplicationContext extends ApplicationContext {
    private class WindowHandler implements WindowListener {
        public void windowOpened(WindowEvent event) {
            initialize(applicationClassName, propertiesResourceName);
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

    private java.awt.Frame hostFrame = new java.awt.Frame();

    private DesktopApplicationContext() {
        // NOTE These properties are supported on Windows only
        System.setProperty("sun.awt.noerasebackground", "true");
        System.setProperty("sun.awt.erasebackgroundonresize", "true");

        // Add the display host to the frame
        hostFrame.add(displayHost);

        // Add window listeners
        hostFrame.addWindowListener(new WindowHandler());

        // Disable focus traversal keys
        hostFrame.setFocusTraversalKeysEnabled(false);

        // Clear the back ground and initialize frame attributes
        hostFrame.setBackground(null);
        hostFrame.setTitle("WTK Application");

        try {
            java.io.InputStream iconInputStream = getClass().getResourceAsStream("vmware.png");
            hostFrame.setIconImage(ImageIO.read(iconInputStream));
        } catch(Exception exception) {
        }

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

    public void exit() {
        System.exit(0);
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            applicationClassName = args[0];

            if (args.length > 1) {
                propertiesResourceName = args[1];
            }
        }

        new DesktopApplicationContext();
    }

    private static String applicationClassName = null;
    private static String propertiesResourceName = null;
}
