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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

/**
 * Sample Test Application (not fully working) to show how to interact between
 * Java code and JavaScript code (interpreted by the JVM) inside bxml files.
 * Many things usually done in Java code are here shown but from the JS side.
 * <p> Some utility methods present here could be moved in a dedicated utility
 * class. <p> Note that there are many comments inside this and related sources,
 * and it's to show different ways to do the same things, even as iterative
 * development.
 */
public class JavascriptConsoleTest implements Application {
    public static final String LANGUAGE_KEY = "language";
    public static final String MAIN_CLASS_NAME = JavascriptConsoleTest.class.getName();

    private Display display = null;
    private Window window = null;

    private Locale locale = null;
    private Resources resources = null;

    @Override
    public void startup(Display displayArgument, Map<String, String> properties) throws Exception {
        logObject("startup: start");

        this.display = displayArgument;

        // get the locale from startup properties, or use the default
        String language = properties.get(LANGUAGE_KEY);
        locale = (language == null) ? Locale.getDefault() : new Locale(language);
        logObject("running with the locale " + locale);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        // add a reference to the application itself in bxml namespace, to be
        // used by JS inside bxml files
        bxmlSerializer.getNamespace().put("application", this);
        logObject("put a reference to application in serializer namespace");
        bxmlSerializer.getNamespace().put("mainClassName", MAIN_CLASS_NAME);
        logObject("put a reference to main class name in serializer namespace \"" + MAIN_CLASS_NAME
            + "\"");

        window = loadWindow("javascript_console_test.bxml", bxmlSerializer);
        initializeFields(bxmlSerializer);
        window.open(display);

        logObject("startup: end");
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * Utility method to initialize secondary fields/elements during application
     * startup.
     *
     * @param serializer the BXMLSerializer instance to use
     */
    private void initializeFields(BXMLSerializer serializer) {
        logObject("initializeFields: start");
        logObject("got BXMLSerializer instance = " + serializer);

        loadResources(MAIN_CLASS_NAME);

        logObject("initializeFields: end");
    }

    /**
     * Load resource files for the given classname, or if null a default will be used.
     *
     * @param className The full class name (to use as a base name), for loading resources.
     */
    private void loadResources(String className) {
        try {
            // load some resources here, just to show its usage from JS files,
            // but only if not already loaded ...
            if (resources == null) {
                resources = new Resources(MAIN_CLASS_NAME, locale);
                logObject("buildResources, load resources from " + "\""
                    // set a useful default
                    + ((className != null && className.length() > 0) ? className : MAIN_CLASS_NAME)
                    + "\", with locale " + locale);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Load (and returns) a Window, given its file name <p> Note that if public
     * this method could be called even from JS in a bxml file (but a reference
     * to the current application has to be put in serializer namespace).
     *
     * @param fileName the file name for the bxml file to load
     * @return the Window instance
     * @throws SerializationException in case of error
     * @throws IOException in case of error
     */
    public Window load(String fileName) throws SerializationException, IOException {
        logObject("load from \"" + fileName + "\"");
        return loadWindow(fileName, null);
    }

    /**
     * Load (and returns) a Window, given its file name and serializer to use.
     *
     * @param fileName the file name for the bxml file to load
     * @param bxmlSerializer the serializer to use, or if null a new one will be
     * created
     * @return the Window instance
     * @throws SerializationException in case of error
     * @throws IOException in case of error
     */
    private Window loadWindow(String fileName, final BXMLSerializer bxmlSerializer)
        throws SerializationException, IOException {
        logObject("loadWindow from \"" + fileName + "\", with the serializer " + bxmlSerializer);

        BXMLSerializer serializer = bxmlSerializer;
        if (serializer == null) {
            serializer = new BXMLSerializer();
        }

        // return (Window)bxmlSerializer.readObject(JavascriptConsoleTest.class,
        // fileName); // ok
        // better, to allow usage of resources (without having to call
        // setLocation or setResources in the serializer) ...
        return (Window) serializer.readObject(JavascriptConsoleTest.class, fileName, true);
    }

    /**
     * Load (and returns) a Window, given its URL and serializer to use <p> Note
     * that if public this method could be called even from JS in a bxml file
     * (but a reference to the current application has to be put in serializer
     * namespace). <p> Note that all Exceptions are catched inside this method,
     * to not expose them to JS code.
     *
     * @param urlString the URL of the bxml file to load, as a String
     * @param bxmlSerializer the serializer to use, or if null a new one will be created
     * @return the Window instance
     */
    public Window loadWindowFromURL(String urlString, final BXMLSerializer bxmlSerializer) {
        logObject("loadWindow from \"" + urlString + "\", with the serializer " + bxmlSerializer);

        BXMLSerializer serializer = bxmlSerializer;
        if (serializer == null) {
            serializer = new BXMLSerializer();
        }

        Window loadedWindow = null;
        try {
            URL url = new URL(urlString);

            // force the location, so it will be possible to decode resources
            // like labels ...
            serializer.setLocation(url);

            loadedWindow = (Window) serializer.readObject(url);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SerializationException e) {
            e.printStackTrace();
        }

        return loadedWindow;
    }

    /**
     * Return the value for the given label, from the resource file loaded at
     * application startup.
     *
     * @param name the label name
     * @return the value or the label, or empty string if not found
     */
    public String getLabel(String name) {
        String label = "";
        if (Utils.isNullOrEmpty(name)) {
            throw new IllegalArgumentException("name must be a valid string");
        }

        // note that if called from bxml files, resources could be not already
        // loaded, so try to force its load with a default value ...
        if (resources == null) {
            loadResources(null);
        }

        label = (String) resources.get(name);
        logObject("search label with name \"" + name + "\", find value \"" + label + "\"");

        return ((label == null) ? "" : label);
    }

    /**
     * Sample utility method to log a formatted dump of the given object to
     * System.out . <p> Note that it has been set public, static, and accepting
     * Object (and not String as usual), even to make some tests on it from JS code.
     *
     * @param msg the object (or message) to log
     */
    public static final void logObject(Object obj) {
        if (obj != null) {
            System.out.println(new java.util.Date() + ", log: { class: \""
                + obj.getClass().getName() + "\", msg:\"" + obj + "\" }");
        }
    }

    /**
     * Application entry point, when run as a Standard (Desktop) Java
     * Application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        DesktopApplicationContext.main(JavascriptConsoleTest.class, args);
    }

}
