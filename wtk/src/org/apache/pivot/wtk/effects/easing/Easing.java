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
package org.apache.pivot.wtk.effects.easing;

/**
 * Base interface for easing operations.
 */
public interface Easing {
    /**
     * Easing in - accelerating from zero velocity.
     *
     * @param time     The current time since the beginning, or how long into the
     *                 easing we are.
     * @param begin    The beginning position.
     * @param change   The total change in position.
     * @param duration The total duration of the easing.
     * @return The updated position at the current point in time, according to the
     *         easing equation.
     */
    public float easeIn(float time, float begin, float change, float duration);

    /**
     * Easing out - decelerating to zero velocity.
     *
     * @param time     The current time since the beginning, or how long into the
     *                 easing we are.
     * @param begin    The beginning position.
     * @param change   The total change in position.
     * @param duration The total duration of the easing.
     * @return The updated position at the current point in time, according to the
     *         easing equation.
     */
    public float easeOut(float time, float begin, float change, float duration);

    /**
     * Easing in and out - acceleration until halfway, then deceleration.
     *
     * @param time     The current time since the beginning, or how long into the
     *                 easing we are.
     * @param begin    The beginning position.
     * @param change   The total change in position.
     * @param duration The total duration of the easing.
     * @return The updated position at the current point in time, according to the
     *         easing equation.
     */
    public float easeInOut(float time, float begin, float change, float duration);
}
