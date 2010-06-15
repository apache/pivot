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
package org.apache.pivot.tutorials.navigation;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.ExpanderListener;
import org.apache.pivot.wtk.Window;

public class Expanders implements Application {
    private Window window = null;
    private Expander stocksExpander = null;
    private Expander weatherExpander = null;
    private Expander calendarExpander = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        BeanSerializer wtkxSerializer = new BeanSerializer();
        window = (Window)wtkxSerializer.readObject(this, "expanders.wtkx");
        stocksExpander = (Expander)wtkxSerializer.get("stocksExpander");
        weatherExpander = (Expander)wtkxSerializer.get("weatherExpander");
        calendarExpander = (Expander)wtkxSerializer.get("calendarExpander");

        ExpanderListener expanderListener = new ExpanderListener.Adapter() {
            @Override
            public void expandedChanged(Expander expander) {
                if (expander.isExpanded()) {
                    expander.scrollAreaToVisible(0, 0, expander.getWidth(), expander.getHeight());
                }
            }
        };

        stocksExpander.getExpanderListeners().add(expanderListener);
        weatherExpander.getExpanderListeners().add(expanderListener);
        calendarExpander.getExpanderListeners().add(expanderListener);

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
        DesktopApplicationContext.main(Expanders.class, args);
    }
}
