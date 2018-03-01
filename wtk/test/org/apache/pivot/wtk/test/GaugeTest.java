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

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import org.apache.pivot.wtk.Gauge;
import org.apache.pivot.wtk.GaugeListener;
import org.apache.pivot.wtk.Origin;


public final class GaugeTest implements GaugeListener<Integer> {
    private int originChangeCount = 0;
    private int valueChangeCount = 0;
    private int textChangeCount = 0;
    private int minMaxChangeCount = 0;
    private int warningCriticalChangeCount = 0;

    @Override
    public void originChanged(final Gauge<Integer> gauge, final Origin previousOrigin) {
        System.out.println("Origin changed to " + gauge.getOrigin());
        originChangeCount++;
    }

    @Override
    public void valueChanged(final Gauge<Integer> gauge, final Integer previousValue) {
        System.out.println("Value changed to " + gauge.getValue());
        valueChangeCount++;
    }

    @Override
    public void textChanged(final Gauge<Integer> gauge, final String previousText) {
        System.out.println("Text changed to " + gauge.getText());
        textChangeCount++;
    }

    @Override
    public void minValueChanged(final Gauge<Integer> gauge, final Integer previousMinValue) {
        System.out.println("Min changed: min=" + gauge.getMinValue());
        minMaxChangeCount++;
    }

    @Override
    public void maxValueChanged(final Gauge<Integer> gauge, final Integer previousMaxValue) {
        System.out.println("Max changed: max=" + gauge.getMaxValue());
        minMaxChangeCount++;
    }

    @Override
    public void warningLevelChanged(final Gauge<Integer> gauge, final Integer previousWarningLevel) {
        System.out.println("Warning level changed: warning=" + gauge.getWarningLevel());
        warningCriticalChangeCount++;
    }

    @Override
    public void criticalLevelChanged(final Gauge<Integer> gauge, final Integer previousCriticalLevel) {
        System.out.println("Critical level changed: critical=" + gauge.getCriticalLevel());
        warningCriticalChangeCount++;
    }

    @Test
    public void testListeners() {
        Gauge<Integer> gauge = new Gauge<>();
        gauge.getGaugeListeners().add(this);

        // Test all the listeners getting fired as they should
        gauge.setOrigin(Origin.NORTH);  // no change here
        gauge.setOrigin(Origin.SOUTH);
        gauge.setOrigin(Origin.SOUTH);  // again, no change
        gauge.setOrigin(Origin.EAST);
        gauge.setOrigin(Origin.WEST);
        gauge.setOrigin(Origin.NORTH);

        gauge.setMinValue(0);
        gauge.setMaxValue(100);
        gauge.setMinValue(0);
        gauge.setMaxValue(100);

        gauge.setWarningLevel(10);
        gauge.setWarningLevel(80);
        gauge.setWarningLevel(80);
        gauge.setCriticalLevel(10);
        gauge.setCriticalLevel(10);
        gauge.setCriticalLevel(90);

        gauge.setText(null);
        gauge.setText("");
        gauge.setText("");
        gauge.setText("20%");
        gauge.setText("20%");
        gauge.setText("100%");
        gauge.setText(null);
        gauge.setText("100%");

        gauge.setValue(0);
        gauge.setValue(2);
        gauge.setValue(10);
        gauge.setValue(0);

        // Now check for proper listener event counts
        assertEquals(originChangeCount, 4);
        assertEquals(minMaxChangeCount, 2);
        assertEquals(warningCriticalChangeCount, 4);
        assertEquals(textChangeCount, 5);
        assertEquals(valueChangeCount, 4);
    }
}
