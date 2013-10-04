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
package org.apache.pivot.tutorials.progress;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class ActivityIndicators extends Window implements Bindable {
    private ActivityIndicator activityIndicator1 = null;
    private ActivityIndicator activityIndicator2 = null;
    private ActivityIndicator activityIndicator3 = null;
    private PushButton activityButton = null;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        activityIndicator1 = (ActivityIndicator) namespace.get("activityIndicator1");
        activityIndicator2 = (ActivityIndicator) namespace.get("activityIndicator2");
        activityIndicator3 = (ActivityIndicator) namespace.get("activityIndicator3");
        activityButton = (PushButton) namespace.get("activityButton");

        activityButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                activityIndicator1.setActive(!activityIndicator1.isActive());
                activityIndicator2.setActive(!activityIndicator2.isActive());
                activityIndicator3.setActive(!activityIndicator3.isActive());
                updateButtonData();
            }
        });

        updateButtonData();
    }

    private void updateButtonData() {
        activityButton.setButtonData(activityIndicator1.isActive() ? "Stop" : "Start");
    }
}
