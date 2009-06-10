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
package pivot.tutorials.buttons;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Checkbox;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.ImageView;
import pivot.wtk.Window;
import pivot.wtkx.WTKX;
import pivot.wtkx.WTKXSerializer;

public class Checkboxes implements Application {
    private Window window = null;

    @WTKX private Checkbox bellCheckbox;
    @WTKX private Checkbox clockCheckbox;
    @WTKX private Checkbox houseCheckbox;
    @WTKX private ImageView bellImageView;
    @WTKX private ImageView clockImageView;
    @WTKX private ImageView houseImageView;

    public void startup(Display display, Dictionary<String, String> properties) throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "checkboxes.wtkx");
        wtkxSerializer.bind(this);

        // Wire up event listeners
        bellCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                bellImageView.setDisplayable(!bellImageView.isDisplayable());
            }
        });

        clockCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                clockImageView.setDisplayable(!clockImageView.isDisplayable());
            }
        });

        houseCheckbox.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                houseImageView.setDisplayable(!houseImageView.isDisplayable());
            }
        });

        window.open(display);
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
        DesktopApplicationContext.main(Checkboxes.class, args);
    }
}
