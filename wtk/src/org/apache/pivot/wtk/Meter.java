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
package org.apache.pivot.wtk;

import org.apache.pivot.util.ListenerList;

/**
 * Component that displays progress information.
 */
public class Meter extends Component {
    private static class MeterListenerList extends WTKListenerList<MeterListener>
    implements MeterListener {
        @Override
        public void orientationChanged(Meter meter) {
            for (MeterListener listener : this) {
                listener.orientationChanged(meter);
            }
        }

        @Override
        public void percentageChanged(Meter meter, double oldPercentage) {
            for (MeterListener listener : this) {
                listener.percentageChanged(meter, oldPercentage);
            }
        }

        @Override
        public void textChanged(Meter meter, String oldText) {
            for (MeterListener listener : this) {
                listener.textChanged(meter, oldText);
            }
        }
    }

    private double percentage = 0.0;
    private String text = null;
    private Orientation orientation = null;
    private MeterListenerList meterListeners = new MeterListenerList();

    public Meter() {
        this(Orientation.HORIZONTAL);
    }

    public Meter(Orientation orientation) {
        this.orientation = orientation;
        installSkin(Meter.class);
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        if (percentage < 0.0 || percentage > 1.0) {
            throw new IllegalArgumentException
                ("Percentage must be a number between 0 and 1");
        }

        double previousPercentage = this.percentage;

        if (previousPercentage != percentage) {
            this.percentage = percentage;
            meterListeners.percentageChanged(this, previousPercentage);
        }
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        String previousText = this.text;
        this.text = text;
        meterListeners.textChanged(this, previousText);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("orientation is null.");
        }

        if (this.orientation != orientation) {
            this.orientation = orientation;
            meterListeners.orientationChanged(this);
        }
    }

    public ListenerList<MeterListener> getMeterListeners() {
        return meterListeners;
    }
}
