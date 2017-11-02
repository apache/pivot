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
package org.apache.pivot.wtk;

import java.awt.Color;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The complete enumeration of the CSS3/X11 color names and values,
 * taken from here: <a href="http://www.w3.org/TR/css3-color/">http://www.w3.org/TR/css3-color/</a>,
 * and including the Java {@link Color} names (with all British/American spelling variants).
 * <p> Note: these are available through the {@link GraphicsUtilities#decodeColor GraphicsUtilities.decodeColor()}
 * and {@link org.apache.pivot.wtk.content.ColorItem#allCSSColors} methods.
 */
public enum CSSColor {
    AliceBlue           (new Color(240,248,255)),
    AntiqueWhite        (new Color(250,235,215)),
    Aqua                (new Color(  0,255,255)),
    Aquamarine          (new Color(127,255,212)),
    Azure               (new Color(240,255,255)),
    Beige               (new Color(245,245,220)),
    Bisque              (new Color(255,228,196)),
    Black               (Color.BLACK),
    BlanchedAlmond      (new Color(255,235,205)),
    Blue                (Color.BLUE),
    BlueViolet          (new Color(138, 43,226)),
    Brown               (new Color(165, 42, 42)),
    Burlywood           (new Color(222,184,135)),
    CadetBlue           (new Color( 95,158,160)),
    Chartreuse          (new Color(127,255,  0)),
    Chocolate           (new Color(210,105, 30)),
    Coral               (new Color(255,127, 80)),
    CornflowerBlue      (new Color(100,149,237)),
    Cornsilk            (new Color(255,248,220)),
    Crimson             (new Color(220, 20, 60)),
    Cyan                (Color.CYAN),
    DarkBlue            (new Color(  0,  0,139)),
    DarkCyan            (new Color(  0,139,139)),
    DarkGoldenrod       (new Color(184,134, 11)),
    DarkGray            (Color.DARK_GRAY),
    DarkGreen           (new Color(  0,100,  0)),
    DarkGrey            (Color.DARK_GRAY),
    DarkKhaki           (new Color(189,183,107)),
    DarkMagenta         (new Color(139,  0,139)),
    DarkOliveGreen      (new Color( 85,107, 47)),
    DarkOrange          (new Color(255,140,  0)),
    DarkOrchid          (new Color(153, 50,204)),
    DarkRed             (new Color(139,  0,  0)),
    DarkSalmon          (new Color(233,150,122)),
    DarkSeaGreen        (new Color(143,188,143)),
    DarkSlateBlue       (new Color( 72, 61,139)),
    DarkSlateGray       (new Color( 47, 79, 79)),
    DarkSlateGrey       (new Color( 47, 79, 79)),
    DarkTurquoise       (new Color(  0,206,209)),
    DarkViolet          (new Color(148,  0,211)),
    DeepPink            (new Color(255, 20,147)),
    DeepSkyBlue         (new Color(  0,191,255)),
    DimGray             (new Color(105,105,105)),
    DimGrey             (new Color(105,105,105)),
    DodgerBlue          (new Color( 30,144,255)),
    FireBrick           (new Color(178, 34, 34)),
    FloralWhite         (new Color(255,250,240)),
    ForestGreen         (new Color( 34,139, 34)),
    Fuchsia             (new Color(255,  0,255)),
    Gainsboro           (new Color(220,220,220)),
    GhostWhite          (new Color(248,248,255)),
    Gold                (new Color(255,215,  0)),
    Goldenrod           (new Color(218,165, 32)),
    Gray                (Color.GRAY),
    Green               (Color.GREEN),
    GreenYellow         (new Color(173,255, 47)),
    Grey                (Color.GRAY),
    Honeydew            (new Color(240,255,240)),
    HotPink             (new Color(255,105,180)),
    IndianRed           (new Color(205, 92, 92)),
    Indigo              (new Color( 75,  0,130)),
    Ivory               (new Color(255,255,240)),
    Khaki               (new Color(240,230,140)),
    Lavender            (new Color(230,230,250)),
    LavenderBlush       (new Color(255,240,245)),
    LawnGreen           (new Color(124,252,  0)),
    LemonChiffon        (new Color(255,250,205)),
    LightBlue           (new Color(173,216,230)),
    LightCoral          (new Color(240,128,128)),
    LightCyan           (new Color(224,255,255)),
    LightGoldenrodYellow(new Color(250,250,210)),
    LightGray           (Color.LIGHT_GRAY),
    LightGreen          (new Color(144,238,144)),
    LightGrey           (Color.LIGHT_GRAY),
    LightPink           (new Color(255,182,193)),
    LightSalmon         (new Color(255,160,122)),
    LightSeaGreen       (new Color( 32,178,170)),
    LightSkyBlue        (new Color(135,206,250)),
    LightSlateGray      (new Color(119,136,153)),
    LightSlateGrey      (new Color(119,136,153)),
    LightSteelBlue      (new Color(176,196,222)),
    LightYellow         (new Color(255,255,224)),
    Lime                (new Color(  0,255,  0)),
    LimeGreen           (new Color( 50,205, 50)),
    Linen               (new Color(250,240,230)),
    Magenta             (Color.MAGENTA),
    Maroon              (new Color(128,  0,  0)),
    MediumAquamarine    (new Color(102,205,170)),
    MediumBlue          (new Color(  0,  0,205)),
    MediumOrchid        (new Color(186, 85,211)),
    MediumPurple        (new Color(147,112,219)),
    MediumSeaGreen      (new Color( 60,179,113)),
    MediumSlateBlue     (new Color(123,104,238)),
    MediumSpringGreen   (new Color(  0,250,154)),
    MediumTurquoise     (new Color( 72,209,204)),
    MediumVioletRed     (new Color(199, 21,133)),
    MidnightBlue        (new Color( 25, 25,112)),
    MintCream           (new Color(245,255,250)),
    MistyRose           (new Color(255,228,225)),
    Moccasin            (new Color(255,228,181)),
    NavajoWhite         (new Color(255,222,173)),
    Navy                (new Color(  0,  0,128)),
    OldLace             (new Color(253,245,230)),
    Olive               (new Color(128,128,  0)),
    OliveDrab           (new Color(107,142, 35)),
    Orange              (Color.ORANGE),
    OrangeRed           (new Color(255, 69,  0)),
    Orchid              (new Color(218,112,214)),
    PaleGoldenrod       (new Color(238,232,170)),
    PaleGreen           (new Color(152,251,152)),
    PaleTurquoise       (new Color(175,238,238)),
    PaleVioletRed       (new Color(219,112,147)),
    PapayaWhip          (new Color(255,239,213)),
    PeachPuff           (new Color(255,218,185)),
    Peru                (new Color(205,133, 63)),
    Pink                (Color.PINK),
    Plum                (new Color(221,160,221)),
    PowderBlue          (new Color(176,224,230)),
    Purple              (new Color(128,  0,128)),
    Red                 (Color.RED),
    RosyBrown           (new Color(188,143,143)),
    RoyalBlue           (new Color( 65,105,225)),
    SaddleBrown         (new Color(139, 69, 19)),
    Salmon              (new Color(250,128,114)),
    SandyBrown          (new Color(244,164, 96)),
    SeaGreen            (new Color( 46,139, 87)),
    Seashell            (new Color(255,245,238)),
    Sienna              (new Color(160, 82, 45)),
    Silver              (new Color(192,192,192)),
    SkyBlue             (new Color(135,206,235)),
    SlateBlue           (new Color(106, 90,205)),
    SlateGray           (new Color(112,128,144)),
    SlateGrey           (new Color(112,128,144)),
    Snow                (new Color(255,250,250)),
    SpringGreen         (new Color(  0,255,127)),
    SteelBlue           (new Color( 70,130,180)),
    Tan                 (new Color(210,180,140)),
    Teal                (new Color(  0,128,128)),
    Thistle             (new Color(216,191,216)),
    Tomato              (new Color(255, 99, 71)),
    Turquoise           (new Color( 64,224,208)),
    Violet              (new Color(238,130,238)),
    Wheat               (new Color(245,222,179)),
    White               (Color.WHITE),
    WhiteSmoke          (new Color(245,245,245)),
    Yellow              (Color.YELLOW),
    YellowGreen         (new Color(154,205, 50));

    private Color color;
    private String colorName;

    private CSSColor(Color color) {
        this.color = color;
        this.colorName = super.toString().toLowerCase(Locale.ENGLISH);
    }

    /**
     * @return The standard color value (RGB) for this color according
     * to the W3C CSS color spec.
     */
    public Color getColor() {
        return this.color;
    }

    /**
     * @return The lowercase name of this color (as defined in the
     * W3C CSS color spec).
     */
    public String getColorName() {
        return this.colorName;
    }

    /**
     * @return The enum value of the given color name (compared in
     * lower case) if it can be found.
     * @param colorName The name of a color to match with one of our values.
     * @throws IllegalArgumentException if the color name cannot be found.
     */
    public static CSSColor fromString(String colorName) {
        String lowerName = colorName.toLowerCase(Locale.ENGLISH);
        CSSColor color = colorMap.get(lowerName);
        if (color == null) {
            throw new IllegalArgumentException("Color name \"" + colorName + "\" is not valid.");
        }
        return color;
    }

    private static Map<String, CSSColor> colorMap;

    static {
        colorMap = new HashMap<>();
        for (CSSColor color : values()) {
            colorMap.put(color.getColorName(), color);
        }
    }
}
