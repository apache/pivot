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

import java.util.List;

/**
 * Class representing a radial gradient fill.
 */
public final class RadialGradientPaint extends MultiStopGradientPaint {
    public final Point center;
    public final float radius;

    public RadialGradientPaint(int centerX, int centerY, float radius, List<Stop> stops) {
        this(new Point(centerX, centerY), radius, stops);
    }

    public RadialGradientPaint(Point center, float radius, List<Stop> stops) {
        super(stops);

        this.center = center;
        this.radius = radius;
    }

    @Override
    protected Object getNativePaint() {
        if (nativePaint == null) {
            nativePaint = Platform.getPlatform().getNativePaint(this);
        }

        return nativePaint;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof RadialGradientPaint) {
            RadialGradientPaint radialGradientPaint = (RadialGradientPaint)object;
            equals = (center.equals(radialGradientPaint.center)
                && radius == radialGradientPaint.radius);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * center.hashCode() + Float.floatToIntBits(radius);
    }
}
