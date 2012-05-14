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
package org.apache.pivot.wtk;

import java.applet.Applet;
import java.awt.Graphics;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Locale;

import netscape.javascript.JSObject;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.immutable.ImmutableMap;

/**
 * Application context used to execute applications in a web browser.
 */
public final class BrowserApplicationContext extends ApplicationContext {
    /**
     * Applet used to host applications in a web browser.
     * <p>
     * This applet supports the following parameters:
     * <ul>
     * <li><tt>application_class_name</tt> - the class name of the application to launch.</li>
     * <li><tt>startup_properties</tt> - startup properties to be passed to the application.
     * Properties use HTTP query string syntax; e.g. "a=1&b=2".</li>
     * <li><tt>system_properties</tt> - system properties to set at startup. Properties use HTTP
     * query string syntax; e.g. "a=1&b=2" (trusted applets only).</li>
     * </ul>
     */
    public static final class HostApplet extends Applet {
        private static final long serialVersionUID = -7710026348576806673L;

        private class InitCallback implements Runnable {
            public InitCallback() {
            }

            @Override
            public void run() {
                // Set the origin
                URL codeBase = getCodeBase();
                if (codeBase != null) {
                    if (codeBase.getProtocol().equals("file")) {
                        File userHome = null;
                        try {
                            userHome = new File(System.getProperty("user.home"));
                        } catch (SecurityException exception) {
                            // No-op
                        }

                        if (userHome != null) {
                            try {
                                origin = userHome.toURI().toURL();
                            } catch (MalformedURLException exception) {
                                // No-op
                            }
                        }
                    } else {
                        try {
                            origin = new URL(codeBase.getProtocol(), codeBase.getHost(),
                                codeBase.getPort(), "");
                        } catch (MalformedURLException exception) {
                            // No-op
                        }
                    }
                }

                // Load properties specified in the system properties parameter
                String systemPropertiesParameter = getParameter(SYSTEM_PROPERTIES_PARAMETER);
                if (systemPropertiesParameter != null) {
                    String[] arguments = systemPropertiesParameter.split("&");

                    String language = null;
                    String region = null;

                    for (int i = 0, n = arguments.length; i < n; i++) {
                        String argument = arguments[i];
                        String[] property = argument.split("=");

                        if (property.length == 2) {
                            String key = property[0].trim();
                            String value;
                            try {
                                value = URLDecoder.decode(property[1].trim(), "UTF-8");
                            } catch (UnsupportedEncodingException exception) {
                                throw new RuntimeException(exception);
                            }

                            if (key.equals("user.language")) {
                                language = value;
                            } else if (key.equals("user.region")) {
                                region = value;
                            } else {
                                System.setProperty(key, value);
                            }
                        } else {
                            System.err.println(argument + " is not a valid system property.");
                        }
                    }

                    if (language != null) {
                        Locale.setDefault((region == null) ? new Locale(language) : new Locale(language, region));
                    }
                }

                // Load properties specified in the startup properties parameter
                HostApplet.this.startupProperties = new HashMap<String, String>();

                String startupPropertiesParameter = getParameter(STARTUP_PROPERTIES_PARAMETER);
                if (startupPropertiesParameter != null) {
                    String[] arguments = startupPropertiesParameter.split("&");

                    for (int i = 0, n = arguments.length; i < n; i++) {
                        String argument = arguments[i];
                        String[] property = argument.split("=");

                        if (property.length == 2) {
                            String key = property[0].trim();
                            String value;
                            try {
                                value = URLDecoder.decode(property[1].trim(), "UTF-8");
                            } catch (UnsupportedEncodingException exception) {
                                throw new RuntimeException(exception);
                            }
                            HostApplet.this.startupProperties.put(key, value);
                        } else {
                            System.err.println(argument + " is not a valid startup property.");
                        }
                    }
                }

                // Create the display host
                HostApplet.this.displayHost = new DisplayHost();
                setLayout(new java.awt.BorderLayout());
                add(HostApplet.this.displayHost);

                // Add the display to the display list
                displays.add(HostApplet.this.displayHost.getDisplay());

                // Disable focus traversal keys
                setFocusTraversalKeysEnabled(false);

                // Clear the background
                setBackground(null);

                // Start the timer and add this applet to the host applet list
                if (hostApplets.getLength() == 0) {
                    createTimer();
                }

                hostApplets.add(HostApplet.this);

                // Load the application
                String applicationClassName = getParameter(APPLICATION_CLASS_NAME_PARAMETER);
                if (applicationClassName == null) {
                    System.err.println(APPLICATION_CLASS_NAME_PARAMETER + " paramter is required.");
                } else {
                    try {
                        Class<?> applicationClass = Class.forName(applicationClassName);
                        HostApplet.this.application = (Application)applicationClass.newInstance();
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                }
            }
        }

        private class StartCallback implements Runnable {
            public StartCallback() {
            }

            @Override
            public void run() {
                // Start the application
                if (HostApplet.this.application != null) {
                    try {
                        HostApplet.this.application.startup(HostApplet.this.displayHost.getDisplay(),
                            new ImmutableMap<String, String>(HostApplet.this.startupProperties));
                    } catch (Exception exception) {
                        displayException(exception);
                    }

                    // Add the application to the application list
                    applications.add(HostApplet.this.application);
                }
            }
        }

        private class StopCallback implements Runnable {
            public StopCallback() {
            }

            @Override
            public void run() {
                if (HostApplet.this.application != null) {
                    try {
                        HostApplet.this.application.shutdown(false);
                    } catch (Exception exception) {
                        displayException(exception);
                    }

                    // Remove the application from the application list
                    applications.remove(HostApplet.this.application);
                    HostApplet.this.application = null;
                }
            }
        }

        private class DestroyCallback implements Runnable {
            public DestroyCallback() {
            }

            @Override
            public void run() {
                // Remove the display from the display list
                displays.remove(HostApplet.this.displayHost.getDisplay());

                // Remove this applet from the host applets list and stop the timer
                hostApplets.remove(HostApplet.this);

                if (hostApplets.getLength() == 0) {
                    destroyTimer();
                }
            }
        }

        private DisplayHost displayHost = null;
        private Application application = null;
        private HashMap<String, String> startupProperties = null;

        public static final String APPLICATION_CLASS_NAME_PARAMETER = "application_class_name";
        public static final String STARTUP_PROPERTIES_PARAMETER = "startup_properties";
        public static final String SYSTEM_PROPERTIES_PARAMETER = "system_properties";

        public Application getApplication() {
            return this.application;
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

        private void displayException(Exception exception) {
            exception.printStackTrace();

            String message = exception.getClass().getName();

            TextArea body = null;
            String bodyText = exception.getMessage();
            if (bodyText != null
                && bodyText.length() > 0) {
                body = new TextArea();
                body.setText(bodyText);
                body.setEditable(false);
            }

            Alert alert = new Alert(MessageType.ERROR, message, null, body, false);
            alert.open(this.displayHost.getDisplay());
        }
    }

    private static ArrayList<HostApplet> hostApplets = new ArrayList<HostApplet>();

    public static boolean isActive() {
        return (hostApplets.getLength() > 0);
    }

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
