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
package org.apache.pivot.tutorials.boundedrange;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.Window;

public class Sliders extends Window implements Bindable {
    private Slider slider = null;
    private Label label = null;

    @Override
    public void initialize(Dictionary<String, Object> context, Resources resources) {
        slider = (Slider)context.get("slider");
        label = (Label)context.get("label");

        slider.getSliderValueListeners().add(new SliderValueListener() {
            @Override
            public void valueChanged(Slider slider, int previousValue) {
                updateLabel();
            }
        });

        updateLabel();
    }

    private void updateLabel() {
        label.setText(Integer.toString(slider.getValue()));
    }
}
