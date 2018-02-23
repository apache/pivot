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

import java.awt.Color;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Gauge;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;

public class GaugeTest implements Application {
    private Window window;
    private PushButton gasPedal;
    private PushButton brakePedal;
    private Gauge<Integer> speedGauge;
    private int speed;
    private Color textColor;
    private Color warningColor;
    private Color criticalColor;

    private int randomInt(int bound) {
        double variant = Math.random();
        int value;
        if (variant >= 0.5) {
            value = (int)Math.floor((variant - 0.5) * (double)bound);
        } else {
            value = (int)Math.ceil((variant * -1.0) * (double)bound);
        }
        return value;
    }

    private void setSpeed(int value) {
        speed = Math.min(value, speedGauge.getMaxValue());
        speed = Math.max(speed, speedGauge.getMinValue());
        speedGauge.setValue(speed);
        Color color = textColor;
        if (speed >= speedGauge.getCriticalLevel()) {
            color = criticalColor;
        } else if (speed > speedGauge.getWarningLevel()) {
            color = warningColor;
        }
        speedGauge.getStyles().put("textColor", color);
        speedGauge.setText(Integer.toString(speed) + " mph");
    }

    private void hitTheGas() {
        setSpeed(speed + 5 + randomInt(2));
        System.out.println("Gas pedal -> " + speed);
    }

    private void hitTheBrakes() {
        setSpeed(speed - (10 + randomInt(3)));
        System.out.println("Brake pedal -> " + speed);
    }

    private void varyTheSpeed() {
        if (speed > 0) {
            setSpeed(speed + randomInt(5));
            System.out.println("Varying speed -> " + speed);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(getClass().getResource("gauge_test.bxml"));
        gasPedal = (PushButton)bxmlSerializer.getNamespace().get("gasPedal");
        brakePedal = (PushButton)bxmlSerializer.getNamespace().get("brakePedal");
        speedGauge = (Gauge<Integer>)bxmlSerializer.getNamespace().get("speedGauge");
        warningColor = speedGauge.getStyles().getColor("warningColor");
        criticalColor = speedGauge.getStyles().getColor("criticalColor");
        textColor = Theme.getTheme().getColor(6);
        setSpeed(speedGauge.getValue());
        gasPedal.getButtonPressListeners().add((button) -> hitTheGas());
        brakePedal.getButtonPressListeners().add((button) -> hitTheBrakes());
        ApplicationContext.scheduleRecurringCallback(() -> varyTheSpeed(), 500L);
        window.open(display);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(GaugeTest.class, args);
    }
}
