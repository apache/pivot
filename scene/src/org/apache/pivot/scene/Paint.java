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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.io.SerializationException;
import org.apache.pivot.json.JSONSerializer;

/**
 * Abstract base class representing a paint.
 */
public abstract class Paint {
    /**
     * Enumeration representing a paint type.
     */
    public enum PaintType {
        SOLID_COLOR,
        GRADIENT,
        LINEAR_GRADIENT,
        RADIAL_GRADIENT
    }

    protected Object nativePaint = null;

    public static final String PAINT_TYPE_KEY = "paintType";

    public static final String COLOR_KEY = "color";

    public static final String START_X_KEY = "startX";
    public static final String START_Y_KEY = "startY";
    public static final String END_X_KEY = "endX";
    public static final String END_Y_KEY = "endY";

    public static final String START_COLOR_KEY = "startColor";
    public static final String END_COLOR_KEY = "endColor";

    public static final String CENTER_X_KEY = "centerX";
    public static final String CENTER_Y_KEY = "centerY";
    public static final String RADIUS_KEY = "radius";

    public static final String STOPS_KEY = "stops";
    public static final String OFFSET_KEY = "offset";

    /**
     * Returns the native object representing this paint.
     */
    protected abstract Object getNativePaint();

    @SuppressWarnings("unchecked")
    public static Paint decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Paint paint;
        if (value.startsWith("#")
            || value.startsWith("0x")
            || value.startsWith("0X")) {
            paint = new SolidColorPaint(Color.decode(value));
        } else {
            try {
                Map<String, ?> map = JSONSerializer.parseMap(value);

                String paintType = BeanAdapter.get(map, PAINT_TYPE_KEY);
                if (paintType == null) {
                    throw new IllegalArgumentException(PAINT_TYPE_KEY + " is required.");
                }

                switch(PaintType.valueOf(paintType.toUpperCase(Locale.ENGLISH))) {
                    case SOLID_COLOR: {
                        String color = BeanAdapter.get(map, COLOR_KEY);
                        paint = new SolidColorPaint(Color.decode(color));
                        break;
                    }

                    case LINEAR_GRADIENT: {
                        int startX = BeanAdapter.getInt(map, START_X_KEY);
                        int startY = BeanAdapter.getInt(map, START_Y_KEY);
                        int endX = BeanAdapter.getInt(map, END_X_KEY);
                        int endY = BeanAdapter.getInt(map, END_Y_KEY);

                        List<Map<String, ?>> stopValues =
                            (List<Map<String, ?>>)BeanAdapter.get(map, STOPS_KEY);

                        int n = stopValues.size();
                        ArrayList<MultiStopGradientPaint.Stop> stops =
                            new ArrayList<MultiStopGradientPaint.Stop>(n);

                        for (int i = 0; i < n; i++) {
                            Map<String, ?> stopValue = stopValues.get(i);

                            Color color = Color.decode((String)BeanAdapter.get(stopValue, COLOR_KEY));
                            float offset = BeanAdapter.getFloat(stopValue, OFFSET_KEY);

                            stops.add(new MultiStopGradientPaint.Stop(color, offset));
                        }

                        paint = new LinearGradientPaint(startX, startY, endX, endY, stops);
                        break;
                    }

                    case RADIAL_GRADIENT: {
                        int centerX = BeanAdapter.getInt(map, CENTER_X_KEY);
                        int centerY = BeanAdapter.getInt(map, CENTER_Y_KEY);
                        int radius = BeanAdapter.getInt(map, RADIUS_KEY);

                        List<Map<String, ?>> stopValues =
                            (List<Map<String, ?>>)BeanAdapter.get(map, STOPS_KEY);

                        int n = stopValues.size();
                        ArrayList<MultiStopGradientPaint.Stop> stops =
                            new ArrayList<MultiStopGradientPaint.Stop>(n);

                        for (int i = 0; i < n; i++) {
                            Map<String, ?> stopValue = stopValues.get(i);

                            Color color = Color.decode((String)BeanAdapter.get(stopValue, COLOR_KEY));
                            float offset = BeanAdapter.getFloat(stopValue, OFFSET_KEY);

                            stops.add(new MultiStopGradientPaint.Stop(color, offset));
                        }

                        paint = new RadialGradientPaint(centerX, centerY, radius, stops);
                        break;
                    }

                    default: {
                        throw new UnsupportedOperationException();
                    }
                }
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        }

        return paint;
    }
}
