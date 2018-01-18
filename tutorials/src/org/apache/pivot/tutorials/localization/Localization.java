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
package org.apache.pivot.tutorials.localization;

import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.util.Locale;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

public class Localization implements Application {
    private Window window = null;

    public static final String LANGUAGE_KEY = "language";

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        String language = properties.get(LANGUAGE_KEY);
        Locale locale = (language == null) ? Locale.getDefault() : new Locale(language);
        Resources resources = new Resources(getClass().getName(), locale);

        Theme theme = Theme.getTheme();
        Font font = theme.getFont();

        // Search for a font that can support the sample string
        String sampleResource = (String) resources.get("firstName");
        if (font.canDisplayUpTo(sampleResource) != -1) {
            Font[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();

            for (int i = 0; i < fonts.length; i++) {
                if (fonts[i].canDisplayUpTo(sampleResource) == -1) {
                    theme.setFont(fonts[i].deriveFont(Font.PLAIN, 12));
                    break;
                }
            }
        }

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(
            Localization.class.getResource("localization.bxml"), resources);
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
        DesktopApplicationContext.main(Localization.class, args);
    }

}
