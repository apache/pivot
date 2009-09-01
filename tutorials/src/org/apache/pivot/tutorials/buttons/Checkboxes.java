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
package org.apache.pivot.tutorials.buttons;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class Checkboxes implements Application {
    private Window window = null;
    private Checkbox bellCheckbox = null;
    private Checkbox clockCheckbox = null;
    private Checkbox houseCheckbox = null;
    private ImageView bellImageView = null;
    private ImageView clockImageView = null;
    private ImageView houseImageView = null;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "checkboxes.wtkx");
        bellCheckbox = (Checkbox)wtkxSerializer.get("bellCheckbox");
        clockCheckbox = (Checkbox)wtkxSerializer.get("clockCheckbox");
        houseCheckbox = (Checkbox)wtkxSerializer.get("houseCheckbox");
        bellImageView = (ImageView)wtkxSerializer.get("bellImageView");
        clockImageView = (ImageView)wtkxSerializer.get("clockImageView");
        houseImageView = (ImageView)wtkxSerializer.get("houseImageView");

        // Wire up event listeners
        bellCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                bellImageView.setVisible(!bellImageView.isVisible());
            }
        });

        clockCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                clockImageView.setVisible(!clockImageView.isVisible());
            }
        });

        houseCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                houseImageView.setVisible(!houseImageView.isVisible());
            }
        });

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
        DesktopApplicationContext.main(Checkboxes.class, args);
    }
}
