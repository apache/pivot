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
 * Class representing a linear gradient paint.
 */
public final class LinearGradientPaint extends MultiStopGradientPaint {
    public final Point start;
    public final Point end;

    public LinearGradientPaint(int startX, int startY, int endX, int endY, List<Stop> stops) {
        this(new Point(startX, startY), new Point(endX, endY), stops);
    }

    public LinearGradientPaint(Point start, Point end, List<Stop> stops) {
        super(stops);

        this.start = start;
        this.end = end;
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

        if (object instanceof LinearGradientPaint) {
            LinearGradientPaint linearGradientPaint = (LinearGradientPaint)object;
            equals = (start.equals(linearGradientPaint.start)
                && end.equals(linearGradientPaint.end));
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return 31 * start.hashCode() + end.hashCode();
    }
}
