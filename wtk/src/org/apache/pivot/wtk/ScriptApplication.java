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

import java.net.URL;
import java.io.File;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;

/**
 * Script application loader.
 */
public class ScriptApplication implements Application {
    private Window window = null;

    public static final String SRC_KEY = "src";
    public static final String RESOURCES_KEY = "resources";

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        String src = properties.get(SRC_KEY);
        if (src == null) {
            throw new IllegalArgumentException(SRC_KEY + " argument is required.");
        }

        Resources resources = null;
        if (properties.containsKey(RESOURCES_KEY)) {
            resources = new Resources(properties.get(RESOURCES_KEY));
        }

        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL location = classLoader.getResource(src.substring(1));

        if (location == null) {
            File file = new File(src);

            if (file.exists()) {
               location = file.toURI().toURL();
            }
        }

        if (location == null) {
            throw new IllegalArgumentException("Cannot find source file \"" + src + "\".");
        }

        bxmlSerializer.getNamespace().put("location", location);
        window = (Window)bxmlSerializer.readObject(location, resources);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void resume() {
    }

    @Override
    public void suspend() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ScriptApplication.class, args);
    }
}
