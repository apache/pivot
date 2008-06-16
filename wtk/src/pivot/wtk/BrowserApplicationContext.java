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

public final class BrowserApplicationContext extends ApplicationContext {
    public static final class HostApplet extends java.applet.Applet {
        public HostApplet() {
            // Create the application context; if the applet has been
            // previously loaded, the JVM may still be running, and the
            // instance may already exist
            ApplicationContext applicationContext = ApplicationContext.getInstance();

            if (applicationContext == null) {
                applicationContext = new BrowserApplicationContext();
            }

            // NOTE We must create a new instance of DisplayHost every time
            // the applet is reloaded. There appears to be a bug in the Java
            // plugin that precludes re-using a single instance of DisplayHost
            // across page refreshes, even when the DisplayHost is added to
            // the applet in init() and removed in destroy().
            applicationContext.displayHost = new DisplayHost();
        }

        private class InitCallback implements Runnable {
            public void run() {
                // Add the display host to the applet
                ApplicationContext applicationContext = ApplicationContext.getInstance();

                DisplayHost displayHost = applicationContext.getDisplayHost();
                setLayout(new java.awt.BorderLayout());
                add(displayHost);

                // Disable focus traversal keys
                setFocusTraversalKeysEnabled(false);

                // Clear the background
                setBackground(null);

                // Set focus to the display host
                displayHost.requestFocus();
            }
        }

        private class StartCallback implements Runnable {
            public void run() {
                // Initialize and start up the application
                ApplicationContext applicationContext = ApplicationContext.getInstance();

                String applicationClassName = getParameter(APPLICATION_CLASS_NAME_PARAMETER);
                String propertiesResourceName = getParameter(PROPERTIES_RESOURCE_NAME_PARAMETER);

                applicationContext.initialize(applicationClassName, propertiesResourceName);
                applicationContext.startupApplication();
            }
        }

        private class StopCallback implements Runnable {
            public void run() {
                // Shut down the application
                ApplicationContext applicationContext = ApplicationContext.getInstance();
                applicationContext.shutdownApplication();
                applicationContext.uninitialize();
            }
        }

        private class DestroyCallback implements Runnable {
            public void run() {
                // Remove the display host from the applet
                ApplicationContext applicationContext = ApplicationContext.getInstance();

                DisplayHost displayHost = applicationContext.getDisplayHost();
                remove(displayHost);
            }
        }

        public static final long serialVersionUID = 0;
        public static final String APPLICATION_CLASS_NAME_PARAMETER = "applicationClassName";
        public static final String PROPERTIES_RESOURCE_NAME_PARAMETER = "propertiesResourceName";

        @Override
        public void init() {
            InitCallback initCallback = new InitCallback();

            if (java.awt.EventQueue.isDispatchThread()) {
                initCallback.run();
            } else {
                queueCallback(initCallback, true);
            }
        }

        @Override
        public void start() {
            StartCallback startCallback = new StartCallback();

            if (java.awt.EventQueue.isDispatchThread()) {
                startCallback.run();
            } else {
                queueCallback(startCallback, true);
            }
        }

        @Override
        public void stop() {
            StopCallback stopCallback = new StopCallback();

            if (java.awt.EventQueue.isDispatchThread()) {
                stopCallback.run();
            } else {
                queueCallback(stopCallback, true);
            }
        }

        @Override
        public void destroy() {
            DestroyCallback destroyCallback = new DestroyCallback();

            if (java.awt.EventQueue.isDispatchThread()) {
                destroyCallback.run();
            } else {
                queueCallback(destroyCallback, true);
            }
        }
    }

    public String getTitle() {
        return null;
    }

    public void setTitle(String title) {
        // No-op for applets; title is set in the host page
    }

    public void exit() {
        // No-op for applets
    }
}
