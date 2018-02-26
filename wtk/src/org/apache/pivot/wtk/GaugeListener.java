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
 * Gauge listener interface.
 */
public interface GaugeListener<T extends Number> {
    /**
     * Gauge listeners.
     */
    public static class Listeners<T extends Number> extends ListenerList<GaugeListener<T>> implements GaugeListener<T> {
        @Override
        public void originChanged(Gauge<T> gauge, Origin previousOrigin) {
            forEach(listener -> listener.originChanged(gauge, previousOrigin));
        }

        @Override
        public void valueChanged(Gauge<T> gauge, T previousValue) {
            forEach(listener -> listener.valueChanged(gauge, previousValue));
        }

        @Override
        public void textChanged(Gauge<T> gauge, String previousText) {
            forEach(listener -> listener.textChanged(gauge, previousText));
        }

        @Override
        public void minValueChanged(Gauge<T> gauge, T previousMinValue) {
            forEach(listener -> listener.minValueChanged(gauge, previousMinValue));
        }

        @Override
        public void maxValueChanged(Gauge<T> gauge, T previousMaxValue) {
            forEach(listener -> listener.maxValueChanged(gauge, previousMaxValue));
        }

        @Override
        public void warningLevelChanged(Gauge<T> gauge, T previousWarningLevel) {
            forEach(listener -> listener.warningLevelChanged(gauge, previousWarningLevel));
        }

        @Override
        public void criticalLevelChanged(Gauge<T> gauge, T previousCriticalLevel) {
            forEach(listener -> listener.criticalLevelChanged(gauge, previousCriticalLevel));
        }
    }

    /**
     * Called when the origin (starting point of the gauge value) changes.
     *
     * @param gauge The gauge that has changed.
     * @param previousOrigin The previous origin value.
     */
    default public void originChanged(Gauge<T> gauge, Origin previousOrigin) {
    }

    /**
     * Called when the gauge value changes.
     *
     * @param gauge The gauge that is changing.
     * @param previousValue The old value.
     */
    default public void valueChanged(Gauge<T> gauge, T previousValue) {
    }

    /**
     * Called when the gauge's text changes.
     *
     * @param gauge The gauge whose text changed.
     * @param previousText The previous text.
     */
    default public void textChanged(Gauge<T> gauge, String previousText) {
    }

    /**
     * Called when min value changes.
     *
     * @param gauge The gauge that is changing.
     * @param previousMinValue The previous minimum.
     */
    default public void minValueChanged(Gauge<T> gauge, T previousMinValue) {
    }

    /**
     * Called when max value changes.
     *
     * @param gauge The gauge that is changing.
     * @param previousMaxValue The previous maximum.
     */
    default public void maxValueChanged(Gauge<T> gauge, T previousMaxValue) {
    }

    /**
     * Called when the warning level for the gauge has changed.
     *
     * @param gauge The gauge we're talking about.
     * @param previousWarningLevel The previous value for the warning level.
     */
    default public void warningLevelChanged(Gauge<T> gauge, T previousWarningLevel) {
    }

    /**
     * Called when the critical level for the gauge has changed.
     *
     * @param gauge The gauge we're talking about.
     * @param previousCriticalLevel The previous value for the critical level.
     */
    default public void criticalLevelChanged(Gauge<T> gauge, T previousCriticalLevel) {
    }
}

