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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.KeyLocation;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.ComponentSkin;
import org.apache.pivot.wtk.skin.SliderSkin;

/**
 * Terra slider skin.
 */
public class TerraSliderSkin extends SliderSkin {
    /**
     * Slider thumb component.
     */
    protected class Thumb extends Component {
        public Thumb() {
            setSkin(new ThumbSkin());
        }
    }

    /**
     * Slider thumb skin.
     */
    protected class ThumbSkin extends ComponentSkin {
        private boolean highlighted = false;

        @Override
        public boolean isFocusable() {
            return true;
        }

        @Override
        public int getPreferredWidth(int height) {
            return 0;
        }

        @Override
        public int getPreferredHeight(int width) {
            return 0;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            if (!themeIsFlat()) {
                graphics.setPaint(new GradientPaint(width / 2f, 0, buttonBevelColor, width / 2f,
                    height, buttonBackgroundColor));

            } else {
                graphics.setPaint(buttonBackgroundColor);
            }
            graphics.fillRect(0, 0, width, height);

            float alpha = (highlighted || dragOffset != null) ? 0.25f : 0.0f;
            graphics.setPaint(new Color(0, 0, 0, alpha));
            graphics.fillRect(0, 0, width, height);

            if (!themeIsFlat()) {
                graphics.setPaint(buttonBorderColor);
                GraphicsUtilities.drawRect(graphics, 0, 0, width, height);
            }
        }

        @Override
        public void enabledChanged(Component component) {
            super.enabledChanged(component);

            highlighted = false;
            repaintComponent();
        }

        @Override
        public void focusedChanged(Component component, Component obverseComponent) {
            super.focusedChanged(component, obverseComponent);

            TerraSliderSkin.this.repaintComponent();
        }

        @Override
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (Mouse.getCapturer() == component) {
                Slider slider = (Slider) TerraSliderSkin.this.getComponent();
                if (slider.getOrientation() == Orientation.HORIZONTAL) {
                    int sliderWidth = slider.getWidth();
                    int thumbWidthLocal = thumb.getWidth();

                    Point sliderLocation = thumb.mapPointToAncestor(slider, x, y);
                    int sliderX = sliderLocation.x;

                    int minX = dragOffset.x;
                    if (sliderX < minX) {
                        sliderX = minX;
                    }

                    int maxX = (sliderWidth - thumbWidthLocal) + dragOffset.x;
                    if (sliderX > maxX) {
                        sliderX = maxX;
                    }

                    float ratio = (float) (sliderX - dragOffset.x)
                        / (sliderWidth - thumbWidthLocal);

                    int start = slider.getStart();
                    int end = slider.getEnd();

                    int value = (int) (start + (end - start) * ratio);
                    slider.setValue(value);
                } else {
                    int sliderHeight = slider.getHeight();
                    int thumbHeightLocal = thumb.getHeight();

                    Point sliderLocation = thumb.mapPointToAncestor(slider, x, y);
                    int sliderY = sliderLocation.y;

                    int minY = dragOffset.y;
                    if (sliderY < minY) {
                        sliderY = minY;
                    }

                    int maxY = (sliderHeight - thumbHeightLocal) + dragOffset.y;
                    if (sliderY > maxY) {
                        sliderY = maxY;
                    }

                    float ratio = (float) (sliderY - dragOffset.y)
                        / (sliderHeight - thumbHeightLocal);

                    int start = slider.getStart();
                    int end = slider.getEnd();

                    int value = (int) (start + (end - start) * ratio);
                    slider.setValue(value);
                }
            }

            return consumed;
        }

        @Override
        public void mouseOver(Component component) {
            super.mouseOver(component);

            highlighted = true;
            repaintComponent();
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            highlighted = false;
            repaintComponent();
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            component.requestFocus();

            if (button == Mouse.Button.LEFT) {
                dragOffset = new Point(x, y);
                Mouse.capture(component);
                repaintComponent();

                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (Mouse.getCapturer() == component) {
                dragOffset = null;
                Mouse.release();
                repaintComponent();
            }

            return consumed;
        }

        /**
         * {@link KeyCode#LEFT LEFT} or {@link KeyCode#DOWN DOWN} Decrement the
         * slider's value.<br> {@link KeyCode#RIGHT RIGHT} or {@link KeyCode#UP
         * UP} Increment the slider's value.
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, KeyLocation keyLocation) {
            boolean consumed = super.keyPressed(component, keyCode, keyLocation);

            Slider slider = (Slider) TerraSliderSkin.this.getComponent();

            int start = slider.getStart();
            int end = slider.getEnd();
            int length = end - start;

            int value = slider.getValue();
            int increment = length / 10;

            if (keyCode == KeyCode.LEFT || keyCode == KeyCode.DOWN) {
                slider.setValue(Math.max(start, value - increment));
                consumed = true;
            } else if (keyCode == KeyCode.RIGHT || keyCode == KeyCode.UP) {
                slider.setValue(Math.min(end, value + increment));
                consumed = true;
            }

            return consumed;
        }
    }

    private Thumb thumb = new Thumb();
    Point dragOffset = null;

    private Color trackColor;
    private Color buttonBackgroundColor;
    private Color buttonBorderColor;
    private int trackWidth;
    private int thumbWidth;
    private int thumbHeight;
    private int tickSpacing;

    // Derived color
    private Color buttonBevelColor;

    public static final int DEFAULT_WIDTH = 120;
    public static final int MINIMUM_THUMB_WIDTH = 4;
    public static final int MINIMUM_THUMB_HEIGHT = 4;

    public TerraSliderSkin() {
        setTrackColor(6);
        setButtonBackgroundColor(10);
        setButtonBorderColor(7);

        trackWidth = 2;
        thumbWidth = 8;
        thumbHeight = 16;

        tickSpacing = -1;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        Slider slider = (Slider) component;
        slider.add(thumb);
    }

    @Override
    public int getPreferredWidth(int height) {
        Slider slider = (Slider) getComponent();

        int preferredWidth;
        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            preferredWidth = DEFAULT_WIDTH;
        } else {
            preferredWidth = thumbHeight;
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        Slider slider = (Slider) getComponent();

        int preferredHeight;
        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            preferredHeight = thumbHeight;
        } else {
            preferredHeight = DEFAULT_WIDTH;
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
    }

    @Override
    public void layout() {
        Slider slider = (Slider) getComponent();

        int width = getWidth();
        int height = getHeight();

        int start = slider.getStart();
        int end = slider.getEnd();
        int value = slider.getValue();

        float ratio = (float) (value - start) / (end - start);

        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            thumb.setSize(thumbWidth, thumbHeight);
            thumb.setLocation((int) ((width - thumbWidth) * ratio), (height - thumbHeight) / 2);
        } else {
            thumb.setSize(thumbHeight, thumbWidth);

            thumb.setLocation((width - thumbHeight) / 2, (int) ((height - thumbWidth) * ratio));
        }
    }

    private static final BasicStroke DASH_STROKE = new BasicStroke(1.0f, BasicStroke.CAP_ROUND,
        BasicStroke.JOIN_ROUND, 1.0f, new float[] {0.0f, 2.0f}, 0.0f);

    @Override
    public void paint(Graphics2D graphics) {
        super.paint(graphics);

        Slider slider = (Slider) getComponent();

        int width = getWidth();
        int height = getHeight();

        graphics.setColor(trackColor);
        GraphicsUtilities.setAntialiasingOn(graphics);

        if (slider.getOrientation() == Orientation.HORIZONTAL) {
            graphics.fillRect(0, (height - trackWidth) / 2, width, trackWidth);
            if (tickSpacing > 0) {
                int start = slider.getStart();
                int end = slider.getEnd();
                int value = start;
                while (value <= end) {
                    float ratio = (float) (value - start) / (end - start);
                    int x = (int) (width * ratio);
                    graphics.drawLine(x, height / 3, x, height * 2 / 3);
                    value += tickSpacing;
                }
            }
        } else {
            graphics.fillRect((width - trackWidth) / 2, 0, trackWidth, height);
            if (tickSpacing > 0) {
                int start = slider.getStart();
                int end = slider.getEnd();
                int value = start;
                while (value <= end) {
                    float ratio = (float) (value - start) / (end - start);
                    int y = (int) (height * ratio);
                    graphics.drawLine(width / 3, y, width * 2 / 3, y);
                    value += tickSpacing;
                }
            }
        }

        if (thumb.isFocused()) {
            graphics.setStroke(DASH_STROKE);
            graphics.setColor(buttonBorderColor);

            graphics.drawRect(0, 0, width - 1, height - 1);
        }
    }

    public Color getTrackColor() {
        return trackColor;
    }

    public void setTrackColor(Color trackColor) {
        Utils.checkNull(trackColor, "trackColor");

        this.trackColor = trackColor;
        repaintComponent();
    }

    public final void setTrackColor(String trackColor) {
        setTrackColor(GraphicsUtilities.decodeColor(trackColor, "trackColor"));
    }

    public final void setTrackColor(int trackColor) {
        Theme theme = currentTheme();
        setTrackColor(theme.getColor(trackColor));
    }

    public Color getButtonBackgroundColor() {
        return buttonBackgroundColor;
    }

    public void setButtonBackgroundColor(Color buttonBackgroundColor) {
        Utils.checkNull(buttonBackgroundColor, "buttonBackgroundColor");

        this.buttonBackgroundColor = buttonBackgroundColor;
        buttonBevelColor = TerraTheme.brighten(buttonBackgroundColor);
        repaintComponent();
    }

    public final void setButtonBackgroundColor(String buttonBackgroundColor) {
        setButtonBackgroundColor(GraphicsUtilities.decodeColor(buttonBackgroundColor, "buttonBackgroundColor"));
    }

    public final void setButtonBackgroundColor(int buttonBackgroundColor) {
        Theme theme = currentTheme();
        setButtonBackgroundColor(theme.getColor(buttonBackgroundColor));
    }

    public Color getButtonBorderColor() {
        return buttonBorderColor;
    }

    public void setButtonBorderColor(Color buttonBorderColor) {
        Utils.checkNull(buttonBorderColor, "buttonBorderColor");

        this.buttonBorderColor = buttonBorderColor;
        repaintComponent();
    }

    public final void setButtonBorderColor(String buttonBorderColor) {
        setButtonBorderColor(GraphicsUtilities.decodeColor(buttonBorderColor, "buttonBorderColor"));
    }

    public final void setButtonBorderColor(int buttonBorderColor) {
        Theme theme = currentTheme();
        setButtonBorderColor(theme.getColor(buttonBorderColor));
    }

    public int getTrackWidth() {
        return trackWidth;
    }

    public void setTrackWidth(int trackWidth) {
        Utils.checkNonNegative(trackWidth, "trackWidth");

        this.trackWidth = trackWidth;
        repaintComponent();
    }

    public void setTrackWidth(Number trackWidth) {
        Utils.checkNull(trackWidth, "trackWidth");

        setTrackWidth(trackWidth.intValue());
    }

    public int getThumbWidth() {
        return thumbWidth;
    }

    public void setThumbWidth(int thumbWidth) {
        if (thumbWidth < MINIMUM_THUMB_WIDTH) {
            throw new IllegalArgumentException("thumbWidth must be greater than or equal to "
                + MINIMUM_THUMB_WIDTH);
        }

        this.thumbWidth = thumbWidth;
        invalidateComponent();
    }

    public void setThumbWidth(Number thumbWidth) {
        Utils.checkNull(thumbWidth, "thumbWidth");

        setThumbWidth(thumbWidth.intValue());
    }

    public int getThumbHeight() {
        return thumbHeight;
    }

    public void setThumbHeight(int thumbHeight) {
        if (thumbHeight < MINIMUM_THUMB_HEIGHT) {
            throw new IllegalArgumentException("thumbHeight must be greater than or equal to "
                + MINIMUM_THUMB_HEIGHT);
        }

        this.thumbHeight = thumbHeight;
        invalidateComponent();
    }

    public void setThumbHeight(Number thumbHeight) {
        Utils.checkNull(thumbHeight, "thumbHeight");

        setThumbHeight(thumbHeight.intValue());
    }

    public int getTickSpacing() {
        return tickSpacing;
    }

    public void setTickSpacing(int tickSpacing) {
        this.tickSpacing = tickSpacing;
        repaintComponent();
    }

    public void setTickSpacing(Number tickSpacing) {
        Utils.checkNull(tickSpacing, "tickSpacing");

        setTickSpacing(tickSpacing.intValue());
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        thumb.requestFocus();
        return super.mouseClick(component, button, x, y, count);
    }

    @Override
    public void rangeChanged(Slider slider, int previousStart, int previousEnd) {
        invalidateComponent();
    }

    @Override
    public void orientationChanged(Slider slider) {
        invalidateComponent();
    }

    @Override
    public void valueChanged(Slider slider, int previousValue) {
        layout();
    }
}
