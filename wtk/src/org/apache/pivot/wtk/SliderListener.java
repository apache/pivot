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
 * Slider listener interface.
 */
public interface SliderListener {
    /**
     * Slider listeners.
     */
    public static class Listeners extends ListenerList<SliderListener> implements SliderListener {
        @Override
        public void orientationChanged(Slider slider) {
            forEach(listener -> listener.orientationChanged(slider));
        }

        @Override
        public void rangeChanged(Slider slider, int previousStart, int previousEnd) {
            forEach(listener -> listener.rangeChanged(slider, previousStart, previousEnd));
        }
    }

    /**
     * Slider listener adapter.
     * @deprecated Since 2.1 and Java 8 the interface itself has default implementations.
     */
    @Deprecated
    public static class Adapter implements SliderListener {
        @Override
        public void rangeChanged(Slider slider, int previousStart, int previousEnd) {
            // empty block
        }

        @Override
        public void orientationChanged(Slider slider) {
            // empty block
        }
    }

    /**
     * Called when a slider's range has changed.
     *
     * @param slider The slider that has changed.
     * @param previousStart The previous start of the slider's range.
     * @param previousEnd The previous end value.
     */
    default void rangeChanged(Slider slider, int previousStart, int previousEnd) {
    }

    /**
     * Called when a sliders's orientation has changed.
     *
     * @param slider The source of the event.
     */
    default void orientationChanged(Slider slider) {
    }
}
