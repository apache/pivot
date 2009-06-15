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

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Expander;
import pivot.wtk.ExpanderListener;
import pivot.wtk.Window;
import pivot.wtkx.WTKX;
import pivot.wtkx.WTKXSerializer;

public class Expanders implements Application {
    private Window window = null;

    @WTKX private Expander stocksExpander;
    @WTKX private Expander weatherExpander;
    @WTKX private Expander calendarExpander;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "expanders.wtkx");
        wtkxSerializer.bind(this, Expanders.class);

        ExpanderListener expanderListener = new ExpanderListener.Adapter() {
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

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Expanders.class, args);
    }
}
