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
package org.apache.pivot.wtk.skin;

import java.awt.Color;

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;

/**
 * Display skin.
 */
public class DisplaySkin extends ContainerSkin {
    public DisplaySkin() {
        super();
        setBackgroundColor(Color.LIGHT_GRAY);
    }

    @Override
    public void install(Component component) {
        if (!(component instanceof Display)) {
            throw new IllegalArgumentException("DisplaySkin can only be installed on instances of Display.");
        }

        super.install(component);
    }

    @Override
    public void layout() {
        Display display = (Display)getComponent();

        // Set all components to their preferred sizes
        for (Component component : display) {
            Window window = (Window)component;

            if (window.isVisible()) {
                if (window.isMaximized()) {
                    window.setSize(display.getSize());
                } else {
                    Dimensions preferredSize = window.getPreferredSize();

                    if (window.getWidth() != preferredSize.width
                        || window.getHeight() != preferredSize.height) {
                        window.setSize(preferredSize.width, preferredSize.height);
                    }
                }
            }
        }
    }
}

