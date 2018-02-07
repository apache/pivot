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
package org.apache.pivot.tutorials;

import java.awt.Color;
import java.awt.Font;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Style;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;

public class HelloJava implements Application {
    private Window window = null;

    @Override
    public void startup(Display display, Map<String, String> properties) {
        this.window = new Window();

        Label label = new Label();
        label.setText("Hello World!");
        label.getStyles().put(Style.font, new Font("Arial", Font.BOLD, 24));
        label.getStyles().put(Style.color, Color.RED);
        label.getStyles().put(Style.horizontalAlignment, HorizontalAlignment.CENTER);
        label.getStyles().put(Style.verticalAlignment, VerticalAlignment.CENTER);

        this.window.setContent(label);
        this.window.setTitle("Hello World!");
        this.window.setMaximized(true);

        this.window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (this.window != null) {
            this.window.close();
        }

        return false;
    }

    // Useful to run this as a Java Application directly from the desktop
    public static void main(String[] args) {
        DesktopApplicationContext.main(HelloJava.class, args);
    }

}
