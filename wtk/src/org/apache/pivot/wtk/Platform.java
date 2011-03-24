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

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;

import org.apache.pivot.wtk.Keyboard.Modifier;

/**
 * Provides platform-specific information.
 */
public class Platform {
    private static FontRenderContext fontRenderContext;

    private static final int DEFAULT_MULTI_CLICK_INTERVAL = 400;
    private static final int DEFAULT_CURSOR_BLINK_RATE = 600;

    private static final Modifier COMMAND_MODIFIER;
    private static final Modifier WORD_NAVIGATION_MODIFIER;
    private static final String KEYSTROKE_MODIFIER_SEPARATOR;

    static {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);

        if (osName.startsWith("mac os x")) {
            COMMAND_MODIFIER = Modifier.META;
            WORD_NAVIGATION_MODIFIER = Modifier.ALT;
            KEYSTROKE_MODIFIER_SEPARATOR = "";
        } else if (osName.startsWith("windows")) {
            COMMAND_MODIFIER = Modifier.CTRL;
            WORD_NAVIGATION_MODIFIER = Modifier.CTRL;
            KEYSTROKE_MODIFIER_SEPARATOR = "+";
        } else {
            COMMAND_MODIFIER = Modifier.CTRL;
            WORD_NAVIGATION_MODIFIER = Modifier.CTRL;
            KEYSTROKE_MODIFIER_SEPARATOR = "-";
        }

        // Initialize the font render context
        initializeFontRenderContext();

        // Listen for changes to the font desktop hints property
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                initializeFontRenderContext();
                ApplicationContext.invalidateDisplays();
            }
        });
    }

    public static FontRenderContext getFontRenderContext() {
        return fontRenderContext;
    }

    private static void initializeFontRenderContext() {
        Object aaHint = null;
        Object fmHint = null;

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.util.Map<?, ?> fontDesktopHints =
            (java.util.Map<?, ?>)toolkit.getDesktopProperty("awt.font.desktophints");
        if (fontDesktopHints != null) {
            aaHint = fontDesktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            fmHint = fontDesktopHints.get(RenderingHints.KEY_FRACTIONALMETRICS);
        }

        if (aaHint == null) {
            aaHint = RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT;
        }

        if (fmHint == null) {
            fmHint = RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT;
        }

        fontRenderContext = new FontRenderContext(null, aaHint, fmHint);
    }

    /**
     * Returns the system multi-click interval.
     */
    public static int getMultiClickInterval() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer multiClickInterval = (Integer)toolkit.getDesktopProperty("awt.multiClickInterval");

        if (multiClickInterval == null) {
            multiClickInterval = DEFAULT_MULTI_CLICK_INTERVAL;
        }

        return multiClickInterval;
    }

    /**
     * Returns the system cursor blink rate.
     */
    public static int getCursorBlinkRate() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Integer cursorBlinkRate = (Integer)toolkit.getDesktopProperty("awt.cursorBlinkRate");

        if (cursorBlinkRate == null) {
            cursorBlinkRate = DEFAULT_CURSOR_BLINK_RATE;
        }

        return cursorBlinkRate;
    }

    /**
     * Returns the system drag threshold.
     */
    public static int getDragThreshold() {
        return java.awt.dnd.DragSource.getDragThreshold();
    }

    /**
     * Returns the system command modifier key.
     */
    public static Modifier getCommandModifier() {
        return COMMAND_MODIFIER;
    }

    /**
     * Returns the word navigation modifier key.
     */
    public static Modifier getWordNavigationModifier() {
        return WORD_NAVIGATION_MODIFIER;
    }

    /**
     * Returns the keystroke modifier separator text.
     */
    public static String getKeyStrokeModifierSeparator() {
        return KEYSTROKE_MODIFIER_SEPARATOR;
    }
}
