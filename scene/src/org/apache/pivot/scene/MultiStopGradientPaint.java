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
package org.apache.pivot.scene;

import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for multi-stop gradient paints.
 */
public abstract class MultiStopGradientPaint extends Paint {
    /**
     * Class representing a stop point in a multi-stop gradient.
     */
    public static class Stop {
        public final Color color;
        public final float offset;

        public Stop(Color color, float offset) {
            this.color = color;
            this.offset = offset;
        }
    }

    /**
     * Unmodifiable list of gradient stops.
     */
    public final List<Stop> stops;

    public MultiStopGradientPaint(List<Stop> stops) {
        this.stops = Collections.unmodifiableList(stops);
    }
}
