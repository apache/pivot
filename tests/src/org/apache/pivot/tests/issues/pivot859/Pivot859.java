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
package org.apache.pivot.tests.issues.pivot859;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.Serializer;
import org.apache.pivot.serialization.StringSerializer;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Window;

/**
 * Test application , to be run in multiple instances in the same HTML page.
 */
public class Pivot859 implements Application {

    private Window window = null;
    private TextInput urlInput = null;
    private PushButton goButton = null;
    private TextArea contentArea = null;
    private PushButton clearButton = null;
    private Label statusLabel = null;

    private String appletName = null;
    private String defaultURL = null;

    @Override
    public void startup(final Display display, Map<String, String> properties) throws Exception {
        System.out.println("startup(...)");

        initializeProperties(properties);

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(Pivot859.class, "pivot_859.bxml");
        initializeFields(bxmlSerializer);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean b) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    /**
     * Set the Applet name. <p> Called by JavaScript from the Browser.
     *
     * @param name the name
     */
    public void setAppletName(String name) {
        appletName = name;
        System.out.println("set appletName to \"" + appletName + "\"");
    }

    /**
     * Get the Applet name. <p> Called by JavaScript from the Browser.
     *
     * @return the name
     */
    public String getAppletName() {
        return appletName;
    }

    private void initializeProperties(Map<String, String> properties) {
        defaultURL = properties.get("default_url");
        if (defaultURL == null) {
            defaultURL = "";
        }
        if (defaultURL.length() > 0) {
            System.out.println("got default URL from startup properties, to \"" + defaultURL + "\"");
        }
    }

    private void initializeFields(BXMLSerializer serializer) {
        System.out.println("initializeFields: start");

        urlInput = (TextInput) serializer.getNamespace().get("textInput");
        if (defaultURL.length() > 0) {
            urlInput.setText(defaultURL);
        }

        goButton = (PushButton) serializer.getNamespace().get("goButton");
        goButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                clearContent();
                retrieveURLContentSync();
            }
        });

        contentArea = (TextArea) serializer.getNamespace().get("textArea");
        clearButton = (PushButton) serializer.getNamespace().get("clearButton");
        clearButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                clearContent();
            }
        });

        statusLabel = (Label) serializer.getNamespace().get("textStatus");

        System.out.println("initializeFields: end");
    }

    private String getAppletNameForLog() {
        return ((getAppletName() != null) ? getAppletName() + ": " : "");
    }

    private void updateStatus(String status) {
        System.out.println(getAppletNameForLog() + status);
        statusLabel.setText(status);
    }

    private void clearContent() {
        // contentArea.clear();
        contentArea.setText("");
        updateStatus("Cleared text area content.");
    }

    private URL buildURL() {
        URL url = null;
        String urlTyped = urlInput.getText();

        try {
            url = new URL(urlTyped);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    /**
     * Retrieve content from the given URL (in applet GUI), but in a synchronous
     * way.
     */
    private void retrieveURLContentSync() {
        URL url = buildURL();
        if (url == null) {
            updateStatus("Unable to retrieve content from a bad URL");
            return;
        }

        try {
            updateStatus("Retrieving Content from URL \"" + url + "\" ...");

            Serializer<String> serializer = new StringSerializer();
            String result = null;

            long start = System.nanoTime();
            try (InputStream inputStream = url.openStream()) {
                result = serializer.readObject(inputStream);
            }
            long end = System.nanoTime();

            if (result == null) {
                result = "";
            }
            contentArea.setText(result);
            double elapsedSecs = ((double) (end - start)) / 1000000000.0d;
            updateStatus("retrieved " + result.length() + " chars in " + elapsedSecs + " sec.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TODO: retrieve content in the usual asynchronous way, but maybe in
    // another method ...

    public static void main(String[] args) {
        DesktopApplicationContext.main(Pivot859.class, args);
    }

}
