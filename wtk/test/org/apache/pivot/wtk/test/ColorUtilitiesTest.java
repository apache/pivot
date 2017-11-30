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
package org.apache.pivot.wtk.test;

import java.awt.Color;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.apache.pivot.wtk.CSSColor;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.util.ColorUtilities;


/**
 * Tests the {@link ColorUtilities} class methods.
 */
public class ColorUtilitiesTest {
    @Test
    public void test1() {
        Color black = CSSColor.Black.getColor();
        String sBlack = ColorUtilities.toStringValue(black);
        assertEquals(sBlack, "#000000");

        Color red = Color.RED;
        String sRed = ColorUtilities.toStringValue(red);
        assertEquals(sRed, "#FF0000");

        Color almostBlack = new Color(1, 1, 1);
        try {
            CSSColor almostBlackColor = CSSColor.fromColor(almostBlack);
            fail("almostBlack should not be a CSS color!");
        } catch (IllegalArgumentException iae) {
            System.out.println("Valid exception: " + iae.getMessage());
            assertEquals(iae.getMessage(), "Incorrect Color value.  " + almostBlack.toString() + " does not match any CSS color.");
        }

        Color translucentOrange = ColorUtilities.setTransparencyInColor(CSSColor.Orange, 128);
        assertEquals(ColorUtilities.toStringValue(translucentOrange), "0xFFC80080");
        try {
            CSSColor resultOrange = CSSColor.fromColor(translucentOrange);
            assertEquals(resultOrange.getColor(), Color.ORANGE);
        } catch (IllegalArgumentException iae) {
            fail("Didn't expect ORANGE not to match!");
        }

        Color lightGray = Color.LIGHT_GRAY;
        Color darkGray = Color.DARK_GRAY;
        assertEquals(lightGray, CSSColor.LightGray.getColor());
        assertEquals(lightGray, CSSColor.LightGrey.getColor());
        assertEquals(darkGray, CSSColor.DarkGray.getColor());
        assertEquals(darkGray, CSSColor.DarkGrey.getColor());
        CSSColor lightGrey = CSSColor.fromString("lightgrey");
        assertEquals(lightGrey.getColor(), lightGray);

        try {
            Color lg2 = GraphicsUtilities.decodeColor("lightGray");
            assertEquals(lg2, lightGray);
        } catch (IllegalArgumentException iae) {
            fail("Decode of \"lightGray\" should succeed!");
        }

        assertEquals((Color)null, GraphicsUtilities.decodeColor("null"));

        int transparency = -1;
        for (CSSColor color : CSSColor.values()) {
            transparency = (transparency + 1) % 256;
            Color originalColor = color.getColor();
            Color translucentColor = ColorUtilities.setTransparencyInColor(color, transparency);
            String value = transparency == 255 ? String.format("#%02X%02X%02X", originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue()) :
                                                 String.format("0x%02X%02X%02X%02X", originalColor.getRed(), originalColor.getGreen(), originalColor.getBlue(), transparency);
            String testValue = ColorUtilities.toStringValue(translucentColor);
            assertEquals(value, testValue);
        }

    }
}
