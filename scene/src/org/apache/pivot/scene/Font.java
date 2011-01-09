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
 * Class representing a font.
 */
public class Font {
    /**
     * Class representing font metric information.
     */
    public static class Metrics {
        public final float ascent;
        public final float descent;
        public final float leading;

        public Metrics(float ascent, float descent, float leading) {
            this.ascent = ascent;
            this.descent = descent;
            this.leading = leading;
        }
    }

    public final String name;
    public final int size;
    public final boolean bold;
    public final boolean italic;

    private Object nativeFont = null;

    public static final String NAME_KEY = "name";
    public static final String SIZE_KEY = "size";
    public static final String BOLD_KEY = "bold";
    public static final String ITALIC_KEY = "italic";

    public Font(String name, int size) {
        this(name, size, false, false);
    }

    public Font(String name, int size, boolean bold, boolean italic) {
        this.name = name;
        this.size = size;
        this.bold = bold;
        this.italic = italic;
    }

    protected Object getNativeFont() {
        if (nativeFont == null) {
            nativeFont = Platform.getPlatform().getNativeFont(this);
        }

        return nativeFont;
    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Font) {
            Font font = (Font)object;
            equals = (name.equals(font.name)
                && size == font.size
                && bold == font.bold
                && italic == font.italic);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return name.hashCode() ^ size
            + (bold ? 0x10 : 0x00)
            + (italic ? 0x01 : 0x00);
    }

    @Override
    public String toString() {
        return getClass().getName() + " [" + name + " " + size
            + (bold ? " bold" : "")
            + (italic ? " italic" : "")
            + "]";
    }

    public static Font decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException();
        }

        Map<String, ?> map;
        try {
            map = JSONSerializer.parseMap(value);
        } catch (SerializationException exception) {
            throw new IllegalArgumentException(exception);
        }

        Font font = Platform.getPlatform().getDefaultFont();

        String name = font.name;
        if (map.containsKey(NAME_KEY)) {
            name = BeanAdapter.get(map, NAME_KEY);
        }

        int size = font.size;
        if (map.containsKey(SIZE_KEY)) {
            Object sizeValue = BeanAdapter.get(map, SIZE_KEY);

            if (sizeValue instanceof String) {
                size = decodeSize(font, (String)sizeValue);
            } else {
                size = ((Number)sizeValue).intValue();
            }
        }

        boolean bold = font.bold;
        if (map.containsKey(BOLD_KEY)) {
            bold = BeanAdapter.get(map, BOLD_KEY);
        }

        boolean italic = font.italic;
        if (map.containsKey(ITALIC_KEY)) {
            italic = BeanAdapter.get(map, ITALIC_KEY);
        }

        return new Font(name, size, bold, italic);
    }

    private static int decodeSize(Font font, String value) {
        int size;

        if (value.endsWith("%")) {
            value = value.substring(0, value.length() - 1);
            float percentage = Float.parseFloat(value) / 100f;
            size = Math.round(font.size * percentage);
        } else {
            throw new IllegalArgumentException(value + " is not a valid font size.");
        }

        return size;
    }
}
