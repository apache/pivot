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

import java.util.Map;

import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.io.SerializationException;
import org.apache.pivot.json.JSONSerializer;

/**
 * Class representing a stroke.
 */
public class Stroke {
    /**
     * Enumeration representing a line cap.
     */
    public enum LineCap {
        BUTT,
        ROUND,
        SQUARE
    }

    /**
     * Enumeration representing a line join.
     */
    public enum LineJoin {
        ROUND,
        BEVEL,
        MITER
    }

    /**
     * Enumeration representing a line style.
     */
    public enum LineStyle {
       SOLID,
       DASHED,
       DOTTED
    }

    public final int lineWidth;
    public final LineCap lineCap;
    public final LineJoin lineJoin;
    public final LineStyle lineStyle;
    public final int miterLimit;

    private Object nativeStroke = null;

    public static final int DEFAULT_LINE_WIDTH = 1;
    public static final LineCap DEFAULT_LINE_CAP = LineCap.BUTT;
    public static final LineJoin DEFAULT_LINE_JOIN = LineJoin.MITER;
    public static final LineStyle DEFAULT_LINE_STYLE = LineStyle.SOLID;
    public static final int DEFAULT_MITER_LIMIT = 10;

    public static final String LINE_WIDTH_KEY = "lineWidth";
    public static final String LINE_CAP_KEY = "lineCap";
    public static final String LINE_JOIN_KEY = "lineJoin";
    public static final String LINE_STYLE_KEY = "lineStyle";
    public static final String MITER_LIMIT_KEY = "miterLimit";

    public Stroke() {
        this(DEFAULT_LINE_WIDTH);
    }

    public Stroke(int lineWidth) {
        this(lineWidth, DEFAULT_LINE_CAP, DEFAULT_LINE_JOIN, DEFAULT_LINE_STYLE, DEFAULT_MITER_LIMIT);
    }

    public Stroke(int lineWidth, LineCap lineCap, LineJoin lineJoin, LineStyle lineStyle, int miterLimit) {
        this.lineWidth = lineWidth;
        this.lineCap = lineCap;
        this.lineJoin = lineJoin;
        this.lineStyle = lineStyle;
        this.miterLimit = miterLimit;
    }

    protected Object getNativeStroke() {
        if (nativeStroke == null) {
            nativeStroke = Platform.getPlatform().getNativeStroke(this);
        }

        return nativeStroke;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Stroke) {
            Stroke stroke = (Stroke)object;
            equals = (lineWidth == stroke.lineWidth
                && lineCap == stroke.lineCap
                && lineJoin == stroke.lineJoin
                && miterLimit == stroke.miterLimit);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + lineWidth;
        result = prime * result + lineCap.ordinal();
        result = prime * result + lineJoin.ordinal();
        result = prime * result + miterLimit;

        return result;
    }

    public static Stroke decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Stroke stroke;
        if (value.startsWith("{")) {
            Map<String, ?> map;
            try {
                map = JSONSerializer.parseMap(value);
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }

            int lineWidth = DEFAULT_LINE_WIDTH;
            if (map.containsKey(LINE_WIDTH_KEY)) {
                lineWidth = BeanAdapter.getInt(map, LINE_WIDTH_KEY);
            }

            LineCap lineCap = DEFAULT_LINE_CAP;
            if (map.containsKey(LINE_CAP_KEY)) {
                lineCap = BeanAdapter.get(map, LINE_CAP_KEY);
            }

            LineJoin lineJoin = DEFAULT_LINE_JOIN;
            if (map.containsKey(LINE_JOIN_KEY)) {
                lineJoin = BeanAdapter.get(map, LINE_JOIN_KEY);
            }

            LineStyle lineStyle = DEFAULT_LINE_STYLE;
            if (map.containsKey(LINE_STYLE_KEY)) {
                lineStyle = BeanAdapter.get(map, LINE_STYLE_KEY);
            }

            int miterLimit = DEFAULT_MITER_LIMIT;
            if (map.containsKey(MITER_LIMIT_KEY)) {
                miterLimit = BeanAdapter.getInt(map, MITER_LIMIT_KEY);
            }

            stroke = new Stroke(lineWidth, lineCap, lineJoin, lineStyle, miterLimit);
        } else {
            stroke = new Stroke(Integer.parseInt(value));
        }

        return stroke;
    }
}
