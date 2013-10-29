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
     */
    public static Color brighten(final Color color, final float adjustment) {
        return adjustBrightness(color, adjustment);
    }

    /**
     * Returns a darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by the given
     * <tt>adjustment</tt> factor (usually in the range ]0 .. 1[).
     */
    public static Color darken(final Color color, final float adjustment) {
        return adjustBrightness(color, (adjustment * -1.0f));
    }

    public static Color adjustBrightness(final Color color, final float adjustment) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[2] = Math.min(Math.max(hsb[2] + adjustment, 0f), 1f);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color((color.getAlpha() << 24) | (rgb & 0xffffff), true);
    }

}
