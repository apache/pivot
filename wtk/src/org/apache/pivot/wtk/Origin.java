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
 * An enumeration of the possible origin (that is, starting point) values
 * for a {@link Gauge} component, including the starting angle offset for each.
 */
public enum Origin {
    /** Origin is at the top. */
    NORTH(90.0f),
    /** Origin is on the right side of the gauge. */
    EAST(360.0f),
    /** Origin is at the bottom. */
    SOUTH(270.0f),
    /** Origin is to the left side of the gauge. */
    WEST(180.0f);

    /** The angle (degrees) represented by this origin value. */
    private float originAngle;

    /**
     * Construct an origin, specifying the angle.
     * @param angle The origin angle (in degrees).
     */
    Origin(final float angle) {
        this.originAngle = angle;
    }

    /**
     * @return The angle (in degrees) represented by this origin.
     */
    public float getOriginAngle() {
        return this.originAngle;
    }

}
