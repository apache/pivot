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

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
    }

    public void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null) {
            name = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(),
                color.getBlue());
        }

        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof ColorItem
            && ((ColorItem)o).color.equals(color));
    }

    @Override
    public int hashCode() {
        return color.hashCode();
    }
}
