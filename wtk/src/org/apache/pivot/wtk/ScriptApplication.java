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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;

/**
 * Script application loader.
 * <p> This application will load a BXML file containing a Pivot component
 * and its content hierarchy and display it.  If the outermost component is
 * a {@link Window} (or subclass of it) then the window will be displayed
 * directly.  Otherwise a default {@code Window} will be created and the
 * component made its content.
 */
public class ScriptApplication implements Application {
    private Window window = null;

    public static final String SRC_KEY = "src";
    public static final String RESOURCES_KEY = "resources";
    public static final String STYLESHEET_KEY = "stylesheet";

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        // Get the location of the source file
        String src = properties.get(SRC_KEY);
        if (src == null) {
            throw new IllegalArgumentException(SRC_KEY + " argument is required.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL location = classLoader.getResource(src.substring(1));

        if (location == null) {
            // If the source file isn't in the resources, try finding it as a local file
            File localFile = new File(src);
            if (localFile.exists() && localFile.isFile() && localFile.canRead()) {
                try {
                    location = localFile.toURI().toURL();
                } catch (MalformedURLException mue) {
                }
            }
        }
        if (location == null) {
            throw new IllegalArgumentException("Cannot find source file \"" + src + "\".");
        }

        Resources resources;
        if (properties.containsKey(RESOURCES_KEY)) {
            resources = new Resources(properties.get(RESOURCES_KEY));
        } else {
            resources = null;
        }

        if (properties.containsKey(STYLESHEET_KEY)) {
            String stylesheet = properties.get(STYLESHEET_KEY);

            if (!stylesheet.startsWith("/")) {
                throw new IllegalArgumentException("Value for " + STYLESHEET_KEY
                    + " argument must start with a slash character.");
            }

            ApplicationContext.applyStylesheet(stylesheet);
        }

        // Load the file and open the window
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        Component component = (Component) bxmlSerializer.readObject(location, resources);

        if (component instanceof Sheet || component instanceof Palette) {
            window = new Window();
            window.setMaximized(true);
            window.open(display);
            Window auxWindow = (Window)component;
            auxWindow.open(window);
            auxWindow.requestFocus();
        } else {
            if (component instanceof Window) {
                window = (Window)component;
            } else {
                window = new Window();
                window.setContent(component);
            }
            window.open(display);

            Component content = window.getContent();
            if (content != null) {
                content.requestFocus();
            }
        }
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (this.window != null) {
            this.window.close();
        }

        return false;
    }

    @Override
    public void resume() {
        // empty block
    }

    @Override
    public void suspend() {
        // empty block
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ScriptApplication.class, args);
    }

}
