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

import org.apache.pivot.collections.Dictionary;

import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.Label;
import pivot.wtk.Slider;
import pivot.wtk.SliderValueListener;
import pivot.wtk.Window;
import pivot.wtkx.WTKXSerializer;

public class SliderTest implements Application {
    private Window window = null;
    private Slider slider = null;
    private Label valueLabel = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("slider_test.wtkx")));
        slider = (Slider)wtkxSerializer.get("slider");
        slider.getSliderValueListeners().add(new SliderValueListener() {
            public void valueChanged(Slider slider, int previousValue) {
                valueLabel.setText(Integer.toString(slider.getValue()));
            }
        });

        valueLabel = (Label)wtkxSerializer.get("valueLabel");

        window.setTitle("Slider Test");
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void resume() {
    }


    public void suspend() {
    }
}
