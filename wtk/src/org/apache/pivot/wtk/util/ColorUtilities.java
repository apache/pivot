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
package org.apache.pivot.wtk.util;

import java.awt.Color;

import org.apache.pivot.wtk.Theme;


/**
 * Utility methods for/on Colors.
 */
public final class ColorUtilities {

    /**
     * Private constructor
     */
    private ColorUtilities() {
    }

    /**
     * Returns a brighter version of the specified color. Specifically, it
     * increases the brightness (in the HSB color model) by the given
     * <tt>adjustment</tt> factor (usually in the range ]0 .. 1[).
     *
     * @param color the color
     * @param adjustment the adjustment factor
     * @return the color brightened
     */
    public static Color brighten(final Color color, final float adjustment) {
        return adjustBrightness(color, adjustment);
    }

    /**
     * Returns a darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by the given
     * <tt>adjustment</tt> factor (usually in the range ]0 .. 1[).
     *
     * @param color the color
     * @param adjustment the adjustment factor
     * @return the color darkened
     */
    public static Color darken(final Color color, final float adjustment) {
        return adjustBrightness(color, (adjustment * -1.0f));
    }

    /**
     * Change the brightness of the given color, and returns the changed color.
     *
     * @param color the color
     * @param adjustment the adjustment factor (usually in the range ]0 .. 1[)
     * @return the new color
     */
    public static Color adjustBrightness(final Color color, final float adjustment) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[2] = Math.min(Math.max(hsb[2] + adjustment, 0f), 1f);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color((color.getAlpha() << 24) | (rgb & 0xffffff), true);
    }

    /**
     * Returns a numeric version of the difference of all color RGB components.
     *
     * @param color1 the first color
     * @param color2 the second color
     * @return the value of the difference
     */
    public static int colorDifferenceRGBTotal(final Color color1, final Color color2) {
        return Math.abs(color1.getRed() - color2.getRed())
            + Math.abs(color1.getGreen() - color2.getGreen())
            + Math.abs(color1.getBlue() - color2.getBlue());
    }

    /**
     * Returns a numeric version of the difference of all color RGB components.
     *
     * @param color1 the first color
     * @param color2 the second color
     * @return an array of three elements, containing a value of the difference
     * for any channel (red, green, blue), with values in the usual RGB range
     */
    public static int[] colorDifferenceRGB(final Color color1, final Color color2) {
        int[] difference = new int[3];

        difference[0] = Math.abs(color1.getRed() - color2.getRed());
        difference[1] = Math.abs(color1.getGreen() - color2.getGreen());
        difference[2] = Math.abs(color1.getBlue() - color2.getBlue());

        return difference;
    }

    /**
     * Returns a Color by subtracting the given color from white.
     *
     * @param color the color to subtract
     * @return a Color
     */
    public static Color colorDifferenceFromWhite(final Color color) {
        int[] difference = new int[3];

        difference[0] = 255 - color.getRed();
        difference[1] = 255 - color.getGreen();
        difference[2] = 255 - color.getBlue();

        return new Color(difference[0], difference[1], difference[2]);
    }

    /**
     * Returns a numeric version of the difference of all color in HSV
     * components.
     *
     * @param color1 the first color
     * @param color2 the second color
     * @return an array of three elements, containing a value of the difference
     * for any channel (hue, saturation, brightness), with values in the usual
     * HSV range
     */
    public static float[] colorDifferenceHSV(final Color color1, final Color color2) {
        float[] difference = new float[3];

        float[] hsb1 = Color.RGBtoHSB(color1.getRed(), color1.getGreen(), color1.getBlue(), null);
        float[] hsb2 = Color.RGBtoHSB(color2.getRed(), color2.getGreen(), color2.getBlue(), null);

        difference[0] = Math.abs(hsb1[0] - hsb2[0]);
        difference[1] = Math.abs(hsb1[1] - hsb2[1]);
        difference[2] = Math.abs(hsb1[2] - hsb2[2]);

        return difference;
    }

    /**
     * Returns a modified version of the given Color.
     * @param original The original color
     * @param transparency The desired transparency (alpha) to set.
     * @return An updated version of the color, with the given transparency.
     */
    public static Color setTransparencyInColor(final Color original, final int transparency) {
        Color updated = new Color(original.getRed(), original.getGreen(), original.getBlue(),
            transparency);

        return updated;
    }

    /**
     * Returns a modified version of the given Theme color.
     * @param colorIndex Index into the Theme color palette of the color to modify.
     * @param transparency The transparency value to set in the color.
     * @return An updated version of the color, with the given transparency.
     */
    public static Color setTransparencyInColor(final int colorIndex, final int transparency) {
        Theme theme = Theme.getTheme();
        return setTransparencyInColor(theme.getColor(colorIndex), transparency);
    }
}
