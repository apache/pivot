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

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

public class JavascriptConsoleTest extends Application.Adapter {
    private Display display = null;
    private Window window = null;

    @Override
    public void startup(Display displayArgument, Map<String, String> properties) throws Exception {
        logObject("startup: start");

        this.display = displayArgument;

        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        // add a reference to the application itself in bxml namespace, to be used by JS inside bxml files
        bxmlSerializer.getNamespace().put("application", this);
        logObject("put a reference to application in serializer namespace");

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

    private void initializeFields(BXMLSerializer serializer) {
        logObject("initializeFields: start");

        logObject("got BXMLSerializer instance = " + serializer);

        logObject("initializeFields: end");
    }

    /**
     * Load (and returns) a Window, given its file name and serializer to use
     *
     * @param fileName the file name for the bxml file to load
     * @param bxmlSerializer the serializer to use, or if null a new one will be created
     * @return the Window instance
     * @throws SerializationException in case of error
     * @throws IOException in case of error
     */
    private Window loadWindow(String fileName, BXMLSerializer bxmlSerializer)
        throws SerializationException, IOException {
        logObject("loadWindow from \"" + fileName + "\", with the serializer " + bxmlSerializer);

        if (bxmlSerializer == null) {
            bxmlSerializer = new BXMLSerializer();
        }

        return (Window)bxmlSerializer.readObject(JavascriptConsoleTest.class, fileName);
    }

    /**
     * Load (and returns) a Window, given its URL and serializer to use
     * <p>
     * Note that if public this method could be called even from JS in a bxml file
     * (but a reference to the current application has to be put in serializer namespace).
     * <p>
     * Note that all Exceptions are catched inside this method, to not expose them to JS code.
     *
     * @param url the URL of the bxml file to load
     * @param bxmlSerializer the serializer to use, or if null a new one will be created
     * @return the Window instance
     */
    public Window loadWindowFromURL(String url, BXMLSerializer bxmlSerializer) {
        logObject("loadWindow from \"" + url + "\", with the serializer " + bxmlSerializer);

        if (bxmlSerializer == null) {
            bxmlSerializer = new BXMLSerializer();
        }

        Window loadedWindow = null;
        try {
            loadedWindow = (Window)bxmlSerializer.readObject(new URL(url));
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
     * Sample utility method to log a formatted dump of the given object to System.out .
     * <p>
     * Note that it has been set public, static, and accepting Object (and not String as usual),
     * even to make some tests on it from JS code.
     *
     * @param msg the object (or message) to log
     */
    public static final void logObject(Object obj) {
        if (obj != null) {
            System.out.println(new java.util.Date() + ", log: { class: \"" + obj.getClass().getName() + "\", msg:\"" + obj + "\" }");
        }
    }


    /**
     * Application entry point, when run as a Standard (Desktop) Java Application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        DesktopApplicationContext.main(JavascriptConsoleTest.class, args);
    }

}
