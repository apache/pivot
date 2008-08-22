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
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;

public final class BrowserApplicationContext extends ApplicationContext {
    public static final class HostApplet extends java.applet.Applet {
        private class PropertyDictionary implements Dictionary<String, String> {
            public String get(String key) {
                String value = properties.containsKey(key) ?
                    properties.get(key) : getParameter(key);
                return value;
            }

            public String put(String key, String value) {
                throw new UnsupportedOperationException();
            }

            public String remove(String key) {
                throw new UnsupportedOperationException();
            }

            public boolean containsKey(String key) {
                return properties.containsKey(key);
            }

            public boolean isEmpty() {
                return properties.isEmpty();
            }
        }

        private class InitCallback implements Runnable {
            public void run() {
                // Create the application context
                applicationContext = new BrowserApplicationContext();

                // Load properties specified on the query string
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

                // Create the display host and add it to the applet
                DisplayHost displayHost = applicationContext.getDisplayHost();
                setLayout(new java.awt.BorderLayout());
                add(displayHost);

                // Add a mouse motion listener to the display host
                displayHost.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent event) {
                        ApplicationContext.active = applicationContext;
                    }

                    public void mouseExited(MouseEvent event) {
                        ApplicationContext.active = null;
                    }
                });

                // Disable focus traversal keys
                setFocusTraversalKeysEnabled(false);

                // Clear the background
                setBackground(null);

                // Load the application
                String applicationClassName = getParameter(APPLICATION_CLASS_NAME_PARAMETER);
                if (applicationClassName == null) {
                    Alert.alert(Alert.Type.ERROR, "Application class name is required.",
                        applicationContext.getDisplay());
                } else {
                    try {
                        Class<?> applicationClass = Class.forName(applicationClassName);
                        application = (Application)applicationClass.newInstance();
                    } catch(Exception exception) {
                        Alert.alert(Alert.Type.ERROR, exception.getMessage(),
                            applicationContext.getDisplay());
                        exception.printStackTrace();
                    }
                }
            }
        }

        private class StartCallback implements Runnable {
            public void run() {
                // Set focus to the display host
                DisplayHost displayHost = applicationContext.getDisplayHost();
                displayHost.requestFocus();

                if (application != null) {
                    try {
                        application.startup(applicationContext.getDisplay(), propertyDictionary);
                    } catch(Exception exception) {
                        Alert.alert(Alert.Type.ERROR, exception.getMessage(),
                            applicationContext.getDisplay());
                        exception.printStackTrace();
                    }
                }
            }
        }

        private class StopCallback implements Runnable {
            public void run() {
                try {
                    application.shutdown(true);
                } catch(Exception exception) {
                    Alert.alert(Alert.Type.ERROR, exception.getMessage(),
                        applicationContext.getDisplay());
                    exception.printStackTrace();
                }
            }
        }

        private class DestroyCallback implements Runnable {
            public void run() {
                if (ApplicationContext.active == applicationContext) {
                    ApplicationContext.active = null;
                }
            }
        }

        private BrowserApplicationContext applicationContext = null;
        private HashMap<String, String> properties = null;
        private PropertyDictionary propertyDictionary = new PropertyDictionary();
        private Application application = null;

        public static final long serialVersionUID = 0;
        public static final String APPLICATION_CLASS_NAME_PARAMETER = "applicationClassName";

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

        @Override
        public void update(Graphics graphics) {
            paint(graphics);
        }
    }
}
