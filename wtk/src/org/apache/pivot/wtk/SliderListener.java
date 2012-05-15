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

/**
 * Slider listener interface.
 */
public interface SliderListener {
    /**
     * Slider listener adapter.
     */
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
     * @param slider
     * @param previousStart
     * @param previousEnd
     */
    public void rangeChanged(Slider slider, int previousStart, int previousEnd);

    /**
     * Called when a sliders's orientation has changed.
     *
     * @param slider
     * The source of the event.
     */
    public void orientationChanged(Slider slider);
}
