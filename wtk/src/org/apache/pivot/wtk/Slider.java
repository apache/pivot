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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Allows a user to select one of a range of values.
 */
public class Slider extends Container {
    private int start = DEFAULT_START;
    private int end = DEFAULT_END;
    private int value = DEFAULT_VALUE;
    private Orientation orientation = null;

    private SliderListener.Listeners sliderListeners = new SliderListener.Listeners();
    private SliderValueListener.Listeners sliderValueListeners = new SliderValueListener.Listeners();

    public static final int DEFAULT_START = 0;
    public static final int DEFAULT_END = 100;
    public static final int DEFAULT_VALUE = 0;

    public Slider() {
        this(Orientation.HORIZONTAL);
    }

    public Slider(final Orientation orientation) {
        this.orientation = orientation;

        installSkin(Slider.class);
    }

    public final int getStart() {
        return start;
    }

    public final void setStart(final int start) {
        setRange(start, end);
    }

    public final int getEnd() {
        return end;
    }

    public final void setEnd(final int end) {
        setRange(start, end);
    }

    public final Span getRange() {
        return new Span(start, end);
    }

    public final void setRange(final int start, final int end) {
        if (start > end) {
            throw new IllegalArgumentException("start " + start + " is greater than maximum " + end + ".");
        }

        int previousStart = this.start;
        int previousEnd = this.end;
        int previousValue = this.value;

        if (start != previousStart || end != previousEnd) {
            this.start = start;
            if (value < start) {
                this.value = start;
            }

            this.end = end;
            if (value > end) {
                this.value = end;
            }

            sliderListeners.rangeChanged(this, previousStart, previousEnd);

            if (previousValue < start || previousValue > end) {
                sliderValueListeners.valueChanged(this, previousValue);
            }
        }
    }

    public final void setRange(final Span range) {
        Utils.checkNull(range, "range");

        setRange(range.start, range.end);
    }

    public final void setRange(final Dictionary<String, ?> range) {
        setRange(new Span(range));
    }

    public final void setRange(final Sequence<?> range) {
        setRange(new Span(range));
    }

    public final void setRange(final String range) {
        setRange(Span.decode(range));
    }

    public final int getValue() {
        return value;
    }

    public final void setValue(final int value) {
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

    public final Orientation getOrientation() {
        return orientation;
    }

    public final void setOrientation(final Orientation orientation) {
        Utils.checkNull(orientation, "orientation");

        if (this.orientation != orientation) {
            this.orientation = orientation;
            sliderListeners.orientationChanged(this);
        }
    }

    public final ListenerList<SliderListener> getSliderListeners() {
        return sliderListeners;
    }

    public final ListenerList<SliderValueListener> getSliderValueListeners() {
        return sliderValueListeners;
    }
}
