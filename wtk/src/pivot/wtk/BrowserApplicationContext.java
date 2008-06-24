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

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import pivot.collections.HashMap;

public final class BrowserApplicationContext extends ApplicationContext {
    public static final class HostApplet extends java.applet.Applet {
        static {
            new BrowserApplicationContext();
        }

        private class InitCallback implements Runnable {
            public void run() {
                currentHostApplet = HostApplet.this;

                // Add the display host to the applet
                ApplicationContext applicationContext = ApplicationContext.getInstance();

                // NOTE We must create a new instance of DisplayHost every time
                // the applet is reloaded. There appears to be a bug in the Java
                // plugin that precludes re-using a single instance of DisplayHost
                // across page refreshes, even when the DisplayHost is added to
                // the applet in init() and removed in destroy().
                applicationContext.recreateDisplayHost();
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
                if (applicationClassName == null) {
                    Alert.alert(Alert.Type.ERROR, "Application class name is required.");
                } else {
                    applicationContext.initialize(applicationClassName);
                    applicationContext.startupApplication();
                }
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

                currentHostApplet = null;
            }
        }

        private HashMap<String, String> properties = null;

        public static final long serialVersionUID = 0;
        public static final String APPLICATION_CLASS_NAME_PARAMETER = "applicationClassName";

        @Override
        public void init() {
            // Load any properties specified on the query string
            properties = new HashMap<String, String>();

            URL documentBase = getDocumentBase();
            String queryString = documentBase.getQuery();
            if (queryString != null) {
                String[] arguments = queryString.split("&");

                for (int i = 0, n = arguments.length; i < n; i++) {
                    String argument = arguments[i];
                    String[] property = argument.split("=");

                    if (property.length == 2) {
                        String key, value;
                        try {
                            final String encoding = "UTF-8";
                            key = URLDecoder.decode(property[0], encoding);
                            value = URLDecoder.decode(property[1], encoding);
                            properties.put(key, value);
                        } catch(UnsupportedEncodingException exception) {
                        }
                    } else {
                        System.out.println(argument + " is not a valid startup property.");
                    }
                }
            }

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

    private static HostApplet currentHostApplet = null;

    public String getTitle() {
        return null;
    }

    public void setTitle(String title) {
        // No-op for applets; title is set in the host page
    }

    public String getProperty(String name) {
        return (currentHostApplet.properties.containsKey(name) ?
            currentHostApplet.properties.get(name) : currentHostApplet.getParameter(name));
    }

    public void open(URL location) {
        // TODO Use java.awt.Desktop class when Java 6 is available on OSX

        currentHostApplet.getAppletContext().showDocument(location, "_blank");
    }

    public void exit() {
        // No-op for applets
    }
}
