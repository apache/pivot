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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.pivot.wtk.ColorChooser;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ColorChooserSkin;

/**
 * Terra color chooser skin.
 */
public class TerraColorChooserSkin extends ColorChooserSkin {
    private Color borderColor;
    private int spacing;

    public TerraColorChooserSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        borderColor = theme.getColor(9);
        spacing = 4;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ColorChooser colorChooser = (ColorChooser)component;

        selectedColorChanged(colorChooser, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        // TODO
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        // TODO
        return 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO
        return new Dimensions(0, 0);
    }

    @Override
    public void layout() {
        // TODO
    }

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        // TODO
    }

    public Color getBorderColor() {
        return borderColor;
    }

    public void setBorderColor(Color borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        this.borderColor = borderColor;
        repaintComponent();
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("spacing is negative.");
        }

        this.spacing = spacing;
        invalidateComponent();
    }

    public final void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }
}
