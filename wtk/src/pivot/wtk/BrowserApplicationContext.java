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

import java.applet.Applet;
import java.awt.Graphics;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;

import netscape.javascript.JSObject;


/**
 * Application context used to execute applications in a web browser.
 *
 * @author gbrown
 */
public final class BrowserApplicationContext extends ApplicationContext {
    /**
     * Applet used to host applications in a web browser.
     *
     * @author gbrown
     */
    public static final class HostApplet extends Applet {
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
                return properties.containsKey(key)
                    || getParameter(key) != null;
            }

            public boolean isEmpty() {
                return properties.isEmpty();
            }
        }

        private class InitCallback implements Runnable {
            public void run() {
               // Set the origin
               URL codeBase = getCodeBase();
               if (codeBase != null) {
                   if (codeBase.getProtocol().equals("file")) {
                       File userHome = null;
                       try {
                           userHome = new File(System.getProperty("user.home"));
                       } catch(SecurityException exception) {
                           // No-op
                       }

                       if (userHome != null) {
                           try {
                               origin = userHome.toURI().toURL();
                           } catch(MalformedURLException exception) {
                               // No-op
                           }
                       }
                   } else {
                       try {
                           origin = new URL(codeBase.getProtocol(), codeBase.getHost(),
                               codeBase.getPort(), "");
                       } catch(MalformedURLException exception) {
                           // No-op
                       }
                   }
               }

               // Create the application context
               applicationContext = new BrowserApplicationContext();

                // Load properties specified on the query string
                properties = new HashMap<String, String>();

                URL documentBase = getDocumentBase();
                if (documentBase != null) {
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
                               System.err.println(argument + " is not a valid startup property.");
                           }
                       }
                   }
                }

                // Add the display host to the applet
                DisplayHost displayHost = applicationContext.getDisplayHost();
                setLayout(new java.awt.BorderLayout());
                add(displayHost);

                // Disable focus traversal keys
                setFocusTraversalKeysEnabled(false);

                // Clear the background
                setBackground(null);

                // Load the application
                String applicationClassName = getParameter(APPLICATION_CLASS_NAME_PARAMETER);
                if (applicationClassName == null) {
                    Alert.alert(MessageType.ERROR, "Application class name is required.",
                        applicationContext.getDisplay());
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

                if (hostApplets.getLength() == 0) {
                    createTimer();
                }

                hostApplets.add(HostApplet.this);
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
                        Alert.alert(MessageType.ERROR, exception.getMessage(),
                            applicationContext.getDisplay());
                        exception.printStackTrace();
                    }
                }
            }
        }

        private class StopCallback implements Runnable {
            public void run() {
                try {
                    application.shutdown(false);
                } catch(Exception exception) {
                    Alert.alert(MessageType.ERROR, exception.getMessage(),
                        applicationContext.getDisplay());
                    exception.printStackTrace();
                }
            }
        }

        private class DestroyCallback implements Runnable {
            public void run() {
                hostApplets.remove(HostApplet.this);

                if (hostApplets.getLength() == 0) {
                    destroyTimer();
                }
            }
        }

        private BrowserApplicationContext applicationContext = null;
        private HashMap<String, String> properties = null;
        private PropertyDictionary propertyDictionary = new PropertyDictionary();
        private Application application = null;

        public static final String APPLICATION_CLASS_NAME_PARAMETER = "applicationClassName";

        private static final long serialVersionUID = 0;

        public Application getApplication() {
            return application;
        }

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

    private static ArrayList<HostApplet> hostApplets = new ArrayList<HostApplet>();

    /**
     * Retrieves a named application.
     *
     * @param name
     * The name of the applet hosting the application.
     */
    public static Application getApplication(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Application application = null;
        for (HostApplet hostApplet : hostApplets) {
            if (hostApplet.getName().equals(name)) {
                application = hostApplet.getApplication();
                break;
            }
        }

        return application;
    }

    /**
     * Evaluates a script in the page context and returns the result.
     *
     * @param script
     * @param application
     */
    public static Object eval(String script, Application application) {
        if (application == null) {
            throw new IllegalArgumentException("application is null.");
        }

        HostApplet applicationHostApplet = null;
        for (HostApplet hostApplet : hostApplets) {
            if (hostApplet.getApplication() == application) {
                applicationHostApplet = hostApplet;
                break;
            }
        }

        if (applicationHostApplet == null) {
            throw new IllegalArgumentException("No applet is hosting the given application.");
        }

        try {
            JSObject window = JSObject.getWindow(applicationHostApplet);
            return window.eval(script);
        } catch (Throwable throwable) {
            throw new UnsupportedOperationException(throwable);
        }
    }
}
