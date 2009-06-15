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
package org.apache.pivot.demos.clock;

import org.apache.pivot.collections.Dictionary;

import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.MovieView;
import pivot.wtk.Window;

/**
 * Demonstrates how to write a movie "asset" class.
 *
 * @author tvolkert
 */
public class ClockDemo implements Application {
    private Window window;
    private Clock clock = new Clock();

    public void startup(Display display, Dictionary<String, String> properties) {
        window = new Window(new MovieView(clock));
        window.setMaximized(true);
        window.open(display);
        clock.play();
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ClockDemo.class, args);
    }
}
