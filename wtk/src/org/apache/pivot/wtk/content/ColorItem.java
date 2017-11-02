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
package org.apache.pivot.wtk.content;

import java.awt.Color;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.GraphicsUtilities;

/**
 * List item representing a color.
 */
public class ColorItem {
    private Color color;
    private String name;

    public ColorItem() {
        this(Color.BLACK, null);
    }

    public ColorItem(Color color) {
        this(color, null);
    }

    public ColorItem(Color color, String name) {
        setColor(color);
        setName(name);
    }

    public ColorItem(String color) {
        setColor(GraphicsUtilities.decodeColor(color));
        setName(color);
    }

    public ColorItem(CSSColor cssColor) {
        setColor(cssColor.getColor());
        setName(cssColor.toString());
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
    }

    public void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            this.name = String.format("#%02X%02X%02X",
                Integer.valueOf(color.getRed()),
                Integer.valueOf(color.getGreen()),
                Integer.valueOf(color.getBlue()));
        } else {
            this.name = name;
        }

    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ColorItem && ((ColorItem) o).color.equals(color));
    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }

    /** The cached list of all the color values (cached because it's a big list). */
    private static List<ColorItem> allColors = null;

    /**
     * @return A list of color items encompassing all the {@link CSSColor} values.
     * @see #allColors
     */
    public static List<ColorItem> allCSSColors() {
        if (allColors == null) {
            CSSColor[] colorValues = CSSColor.values();
            allColors = new ArrayList<>(colorValues.length);
            for (CSSColor color : colorValues) {
                allColors.add(new ColorItem(color));
            }
        }
        return allColors;
    }
}
