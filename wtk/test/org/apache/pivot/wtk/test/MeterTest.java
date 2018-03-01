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

import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.MeterListener;
import org.apache.pivot.wtk.Orientation;


public final class MeterTest implements MeterListener {
    private int percentChangeCount = 0;
    private int textChangeCount = 0;
    private int orientationChangeCount = 0;

    @Override
    public void percentageChanged(final Meter meter, final double previousPercentage) {
        percentChangeCount++;
    }

    @Override
    public void textChanged(final Meter meter, final String previousText) {
        textChangeCount++;
    }

    @Override
    public void orientationChanged(final Meter meter) {
        orientationChangeCount++;
    }

    @Test
    public void testListeners() {
        Meter meter = new Meter();
        meter.getMeterListeners().add(this);

        // Test all the listeners getting fired as they should
        meter.setText("abc");
        meter.setText(null);
        meter.setText(null);
        meter.setText("123");
        meter.setText("123");
        meter.setOrientation(Orientation.HORIZONTAL);
        meter.setOrientation(Orientation.VERTICAL);
        meter.setOrientation(Orientation.HORIZONTAL);
        meter.setPercentage(0.25);
        meter.setPercentage(0.25);
        meter.setPercentage(0.5);
        meter.setPercentage(0.75);
        meter.setPercentage(1.0);

        // Now check for proper listener event counts
        assertEquals(percentChangeCount, 4);
        assertEquals(textChangeCount, 3);
        assertEquals(orientationChangeCount, 2);
    }
}
