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

import java.applet.Applet;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;

import org.apache.pivot.scene.AWTStageHost;

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
public class BrowserApplicationContext extends Applet {
    private static final long serialVersionUID = 0;

    private Application application = null;

    public static final String APPLICATION_CLASS_NAME_PARAMETER = "application_class_name";
    public static final String STARTUP_PROPERTIES_PARAMETER = "startup_properties";
    public static final String SYSTEM_PROPERTIES_PARAMETER = "system_properties";

    @Override
    public void init() {
        // Load the application
        String applicationClassName = getParameter(APPLICATION_CLASS_NAME_PARAMETER);

        if (applicationClassName == null) {
            System.err.println(APPLICATION_CLASS_NAME_PARAMETER + " parameter is required.");
        } else {
            try {
                Class<?> applicationClass = Class.forName(applicationClassName);
                application = (Application)applicationClass.newInstance();
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        // Load properties specified in the startup properties parameter
        final HashMap<String, String> startupProperties = new HashMap<String, String>();

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
                    startupProperties.put(key, value);
                } else {
                    System.err.println(argument + " is not a valid startup property.");
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

        // Add stage host to applet
        final AWTStageHost awtStageHost = new AWTStageHost();
        awtStageHost.getStage().setLayout(new StageLayout());
        setLayout(new java.awt.BorderLayout());
        add(awtStageHost);

        // Set applet properties
        setFocusTraversalKeysEnabled(false);
        setBackground(null);

        // Start up the application
        Runnable startupCallback = new Runnable() {
            @Override
            public void run() {
                if (application != null) {
                    try {
                        application.startup(awtStageHost.getStage(), startupProperties);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }
        };

        if (java.awt.EventQueue.isDispatchThread()) {
            startupCallback.run();
        } else {
            try {
                EventQueue.invokeAndWait(startupCallback);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public void destroy() {
        // Shut down the application
        Runnable shutdownCallback = new Runnable() {
            @Override
            public void run() {
                if (application != null) {
                    try {
                        application.shutdown(false);
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }

                    application = null;
                }
            }
        };

        if (java.awt.EventQueue.isDispatchThread()) {
            shutdownCallback.run();
        } else {
            try {
                EventQueue.invokeAndWait(shutdownCallback);
            } catch (InterruptedException exception) {
                throw new RuntimeException(exception);
            } catch (InvocationTargetException exception) {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public void update(Graphics graphics) {
        paint(graphics);
    }
}
