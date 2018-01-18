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
package org.apache.pivot.demos.roweditor;

import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

public class RowEditorDemo implements Application {
    private Window window = null;

    public static final String LANGUAGE_KEY = "language";
    private static final String RESOURCE_NAME = RowEditorDemo.class.getName();

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        String language = properties.get(LANGUAGE_KEY);
        Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
        Resources resources = new Resources(RESOURCE_NAME, locale);
        System.out.println("Loaded Resources from: " + resources.getBaseName() + ", for locale "
            + locale);

        // Search for a font that can support the sample string
        String title = (String) resources.get("title");
        System.out.println("Title from Resources file is: \"" + title + "\"");

        // print if the theme has transitions enabled
        System.out.println("Theme has transitions enabled: " + Theme.getTheme().isTransitionEnabled());

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(
            RowEditorDemo.class.getResource("row_editor_demo.bxml"), resources);

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(RowEditorDemo.class, args);
    }

}
