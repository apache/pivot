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
 * Meter listener interface.
 */
public interface MeterListener {
    /**
     * Meter listeners.
     */
    public static class Listeners extends ListenerList<MeterListener> implements
        MeterListener {
        @Override
        public void orientationChanged(Meter meter) {
            forEach(listener -> listener.orientationChanged(meter));
        }

        @Override
        public void percentageChanged(Meter meter, double oldPercentage) {
            forEach(listener -> listener.percentageChanged(meter, oldPercentage));
        }

        @Override
        public void textChanged(Meter meter, String oldText) {
            forEach(listener -> listener.textChanged(meter, oldText));
        }
    }

    /**
     * Meter listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements MeterListener {
        @Override
        public void percentageChanged(Meter meter, double previousPercentage) {
            // empty block
        }

        @Override
        public void textChanged(Meter meter, String previousText) {
            // empty block
        }

        @Override
        public void orientationChanged(Meter meter) {
            // empty block
        }
    }

    /**
     * Called when a meter's percentage value has changed.
     *
     * @param meter The meter that is changing.
     * @param previousPercentage What the meter's percentage value used to be.
     */
    default void percentageChanged(Meter meter, double previousPercentage) {
    }

    /**
     * Called when a meter's text has changed.
     *
     * @param meter The meter that has changed.
     * @param previousText The previous meter text.
     */
    default void textChanged(Meter meter, String previousText) {
    }

    /**
     * Called when a sliders's orientation has changed.
     *
     * @param meter The source of the event.
     */
    default void orientationChanged(Meter meter) {
    }
}
