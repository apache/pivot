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
 */package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ActivityIndicatorSkin;


/**
 * Activity indicator skin.
 */
public class TerraActivityIndicatorSkin extends ActivityIndicatorSkin {
    private Color[] colors;
    private Color backgroundColor;

    private int angle = 0;

    private ApplicationContext.ScheduledCallback updateCallback = null;

    public TerraActivityIndicatorSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(2));
        backgroundColor = null;
    }

    @Override
    public int getPreferredWidth(int height) {
        return 128;
    }

    @Override
    public int getPreferredHeight(int width) {
        return 128;
    }

    @Override
    public void paint(Graphics2D graphics) {
        ActivityIndicator activityIndicator = (ActivityIndicator)getComponent();

        int width = getWidth();
        int height = getHeight();

        if (backgroundColor != null) {
            graphics.setColor(backgroundColor);
            graphics.fillRect(0, 0, width, height);
        }

        if (activityIndicator.isActive()) {
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // Translate/scale to fit
            if (width > height) {
                graphics.translate((width - height) / 2, 0);

                float scale = height / 128f;
                graphics.scale(scale, scale);
            } else {
                graphics.translate(0, (height - width) / 2);

                float scale = width / 128f;
                graphics.scale(scale, scale);
            }

            graphics.translate(64, 64);
            graphics.rotate((2 * Math.PI) / 360 * angle);

            final double increment = (2 * Math.PI) / 360 * 30;

            for (int i = 0; i < 12; i++) {
                graphics.setColor(colors[i]);
                graphics.fillRoundRect(24, -4, 32, 8, 8, 8);

                graphics.rotate(increment);
            }
        }
    }

    public Color getColor() {
        return colors[0];
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        colors = new Color[12];
        for (int i = 0; i < 12; i++) {
            float alpha = 255f * i / 12;
            colors[i] = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)alpha);
        }
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    @Override
    public void activeChanged(ActivityIndicator activityIndicator) {
        if (activityIndicator.isActive()) {
            updateCallback = ApplicationContext.scheduleRecurringCallback(new Runnable() {
                @Override
                public void run() {
                    angle = (angle + 30) % 360;
                    repaintComponent();
                }
            }, 100);
        } else {
            updateCallback.cancel();
            updateCallback = null;
            repaintComponent();
        }
    }
}
