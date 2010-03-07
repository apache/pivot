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
package org.apache.pivot.tutorials.stocktracker;

import java.util.Locale;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtkx.WTKXSerializer;

/**
 * Stock Tracker application.
 */
public class StockTracker implements Application {
    private StockTrackerWindow window = null;

    public static final String LANGUAGE_PROPERTY_NAME = "language";

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        String language = properties.get(LANGUAGE_PROPERTY_NAME);
        if (language != null) {
            Locale.setDefault(new Locale(language));
        }

        Resources resources = new Resources(StockTrackerWindow.class.getName());
        WTKXSerializer wtkxSerializer = new WTKXSerializer(resources);
        window = (StockTrackerWindow)wtkxSerializer.readObject(this, "stock_tracker_window.wtkx");
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
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(StockTracker.class, args);
    }
}
