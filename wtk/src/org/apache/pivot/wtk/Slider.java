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
 */
public class Slider extends Container {
    private static class SliderListenerList extends WTKListenerList<SliderListener>
        implements SliderListener {
        @Override
        public void orientationChanged(Slider slider) {
            for (SliderListener listener : this) {
                listener.orientationChanged(slider);
            }
        }

        @Override
        public void rangeChanged(Slider slider, int previousStart, int previousEnd) {
            for (SliderListener listener : this) {
                listener.rangeChanged(slider, previousStart, previousEnd);
            }
        }
    }

    private static class SliderValueListenerList extends WTKListenerList<SliderValueListener>
        implements SliderValueListener {
        @Override
        public void valueChanged(Slider slider, int previousValue) {
            for (SliderValueListener listener : this) {
                listener.valueChanged(slider, previousValue);
            }
        }
    }

    private int start = DEFAULT_START;
    private int end = DEFAULT_END;
    private int value = DEFAULT_VALUE;
    private Orientation orientation = null;

    private SliderListenerList sliderListeners = new SliderListenerList();
    private SliderValueListenerList sliderValueListeners = new SliderValueListenerList();

    public static final int DEFAULT_START = 0;
    public static final int DEFAULT_END = 100;
    public static final int DEFAULT_VALUE = 0;

    public Slider() {
        this(Orientation.HORIZONTAL);
    }

    public Slider(Orientation orientation) {
        this.orientation = orientation;

        installSkin(Slider.class);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        setRange(start, end);
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        setRange(start, end);
    }

    public void setRange(int start, int end) {
        if (start > end) {
            throw new IllegalArgumentException("start " + start + " is greater than maximum " + end + ".");
        }

        int previousStart = this.start;
        int previousEnd = this.end;
        int previousValue = this.value;

        if (start != previousStart
            || end != previousEnd) {
            this.start = start;
            if (value < start) {
                this.value = start;
            }

            this.end = end;
            if (value > end) {
                this.value = end;
            }

            sliderListeners.rangeChanged(this, previousStart, previousEnd);

            if (previousValue < start
                || previousValue > end) {
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
        if (value < start) {
            throw new IllegalArgumentException("value " + value + " is less than minimum " + start + ".");
        }

        if (value > end) {
            throw new IllegalArgumentException("value " + value + " is greater than maximum " + end + ".");
        }

        int previousValue = this.value;

        if (value != previousValue) {
            this.value = value;
            sliderValueListeners.valueChanged(this, previousValue);
        }
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
            sliderListeners.orientationChanged(this);
        }
    }
    public ListenerList<SliderListener> getSliderListeners() {
        return sliderListeners;
    }

    public ListenerList<SliderValueListener> getSliderValueListeners() {
        return sliderValueListeners;
    }
}
