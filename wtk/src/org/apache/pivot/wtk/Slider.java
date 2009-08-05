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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.ListenerList;

/**
 * Allows a user to select one of a range of values.
 *
 * @author gbrown
 */
public class Slider extends Container {
    private static class SliderListenerList extends ListenerList<SliderListener>
        implements SliderListener {
        public void rangeChanged(Slider slider, int previousRangeStart, int previousRangeEnd) {
            for (SliderListener listener : this) {
                listener.rangeChanged(slider, previousRangeStart, previousRangeEnd);
            }
        }
    }

    private static class SliderValueListenerList extends ListenerList<SliderValueListener>
        implements SliderValueListener {
        public void valueChanged(Slider slider, int previousValue) {
            for (SliderValueListener listener : this) {
                listener.valueChanged(slider, previousValue);
            }
        }
    }

    private int rangeStart = DEFAULT_RANGE_START;
    private int rangeEnd = DEFAULT_RANGE_END;
    private int value = DEFAULT_VALUE;

    private SliderListenerList sliderListeners = new SliderListenerList();
    private SliderValueListenerList sliderValueListeners = new SliderValueListenerList();

    public static final int DEFAULT_RANGE_START = 0;
    public static final int DEFAULT_RANGE_END = 100;
    public static final int DEFAULT_VALUE = 0;

    public Slider() {
        installSkin(Slider.class);
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public void setRangeStart(int rangeStart) {
        setRange(rangeStart, rangeEnd);
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public void setRangeEnd(int rangeEnd) {
        setRange(rangeStart, rangeEnd);
    }

    public void setRange(int rangeStart, int rangeEnd) {
        if (rangeStart > rangeEnd) {
            throw new IllegalArgumentException("rangeStart is greater than maximum.");
        }

        int previousRangeStart = this.rangeStart;
        int previousRangeEnd = this.rangeEnd;
        int previousValue = this.value;

        if (rangeStart != previousRangeStart
            || rangeEnd != previousRangeEnd) {
            this.rangeStart = rangeStart;
            if (value < rangeStart) {
                this.value = rangeStart;
            }

            this.rangeEnd = rangeEnd;
            if (value > rangeEnd) {
                this.value = rangeEnd;
            }

            sliderListeners.rangeChanged(this, previousRangeStart, previousRangeEnd);

            if (previousValue < rangeStart
                || previousValue > rangeEnd) {
                sliderValueListeners.valueChanged(this, previousValue);
            }
        }
    }

    public final void setRange(Span range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        setRange(range.start, range.end);
    }

    public final void setRange(Dictionary<String, ?> range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        setRange(new Span(range));
    }

    public final void setRange(String range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        setRange(Span.decode(range));
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if (value < rangeStart) {
            throw new IllegalArgumentException("value is less than minimum.");
        }

        if (value > rangeEnd) {
            throw new IllegalArgumentException("value is greater than maximum.");
        }

        int previousValue = this.value;

        if (value != previousValue) {
            this.value = value;
            sliderValueListeners.valueChanged(this, previousValue);
        }
    }

    public ListenerList<SliderListener> getSliderListeners() {
        return sliderListeners;
    }

    public ListenerList<SliderValueListener> getSliderValueListeners() {
        return sliderValueListeners;
    }
}
