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
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Provides platform-specific information.
 *
 */
public class Platform {
    private static Object textAntialiasingHint = null;

    private static final int DEFAULT_MULTI_CLICK_INTERVAL = 400;
    private static final int DEFAULT_CURSOR_BLINK_RATE = 600;

    /**
     * Returns the system text anti-aliasing hint.
     */
    public static Object getTextAntialiasingHint() {
        if (textAntialiasingHint == null) {
            Toolkit toolkit = Toolkit.getDefaultToolkit();
            java.util.Map<?, ?> fontDesktopHints =
                (java.util.Map<?, ?>)toolkit.getDesktopProperty("awt.font.desktophints");

            if (fontDesktopHints == null) {
                textAntialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            } else {
                textAntialiasingHint = fontDesktopHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
                if (textAntialiasingHint.equals(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)) {
                    textAntialiasingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
                }

                // Listen for changes to the property
                toolkit.addPropertyChangeListener("awt.font.desktophints", new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        Platform.textAntialiasingHint = null;
                    }
                });
            }
        }

        return textAntialiasingHint;
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

}
