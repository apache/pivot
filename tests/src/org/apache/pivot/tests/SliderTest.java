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
package org.apache.pivot.tests;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;

public class SliderTest implements Application {
    private Window window = null;
    private Slider slider1 = null;
    private Slider slider2 = null;
    private Label valueLabel1 = null;
    private Label valueLabel2 = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = new Window((Component)wtkxSerializer.readObject(getClass().getResource("slider_test.wtkx")));
        slider1 = (Slider)wtkxSerializer.get("slider1");
        slider1.getSliderValueListeners().add(new SliderValueListener() {
            @Override
            public void valueChanged(Slider slider, int previousValue) {
                valueLabel1.setText(Integer.toString(slider.getValue()));
            }
        });
        slider2 = (Slider)wtkxSerializer.get("slider2");
        slider2.getSliderValueListeners().add(new SliderValueListener() {
            @Override
            public void valueChanged(Slider slider, int previousValue) {
                valueLabel2.setText(Integer.toString(slider.getValue()));
            }
        });

        valueLabel1 = (Label)wtkxSerializer.get("valueLabel1");
        valueLabel2 = (Label)wtkxSerializer.get("valueLabel2");

        window.setTitle("Slider Test");
        window.setMaximized(true);
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
    public void resume() {
    }


    @Override
    public void suspend() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(SliderTest.class, args);
    }
}
