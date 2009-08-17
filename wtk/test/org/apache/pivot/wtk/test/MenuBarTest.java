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
package org.apache.pivot.wtk.test;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class MenuBarTest implements Application {
    private Frame frame1 = null;
    private Frame frame2 = null;

    @WTKX private TextInput textInput1 = null;
    @WTKX private TextInput textInput2 = null;
    @WTKX private TextInput textInput3 = null;

    public void startup(Display display, Map<String, String> properties) throws Exception {
        BoxPane boxPane = new BoxPane(Orientation.VERTICAL);
        boxPane.add(new TextInput());
        boxPane.add(new TextInput());
        boxPane.add(new TextInput());
        frame1 = new Frame(boxPane);
        frame1.setLocation(50, 50);
        frame1.setPreferredSize(320, 240);
        frame1.open(display);

        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame2 = (Frame)wtkxSerializer.readObject(this, "menu_bar_test.wtkx");
        wtkxSerializer.bind(this, MenuBarTest.class);

        MenuHandler menuHandler = new MenuHandler.Adapter() {
            @Override
            public void configureMenuBar(Component component, MenuBar menuBar) {
                System.out.println("Configure menu bar: " + component);
            }

            @Override
            public void cleanupMenuBar(Component component, MenuBar menuBar) {
                System.out.println("Clean up menu bar: " + component);
            }
        };

        textInput1.setMenuHandler(menuHandler);
        textInput2.setMenuHandler(menuHandler);
        textInput3.setMenuHandler(menuHandler);

        frame2.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (frame2 != null) {
            frame2.close();
        }

        return false;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(MenuBarTest.class, args);
    }
}
