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
import java.awt.image.BufferedImage;

import org.apache.pivot.wtk.ColorChooser;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.skin.ColorChooserSkin;
import org.apache.pivot.wtk.skin.ComponentSkin;

/**
 * Terra color chooser skin.
 */
public class TerraColorChooserSkin extends ColorChooserSkin {
    private class HueChooser extends Component {
        private float hue = 0f;

        public HueChooser() {
            setSkin(new HueChooserSkin());
        }

        public float getHue() {
            return hue;
        }

        public void setHue(float hue) {
            this.hue = hue;
        }
    }

    private class HueChooserSkin extends ComponentSkin {
        private boolean capture = false;

        @Override
        public int getPreferredWidth(int height) {
            return 18;
        }

        @Override
        public int getPreferredHeight(int width) {
            return 185;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            if (hueSpectrumImage == null) {
                hueSpectrumImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D hueSpectrumImageGraphics = hueSpectrumImage.createGraphics();

                // Paint the hue spectrum
                for (int y = 0; y < height; y++) {
                    Color color = Color.getHSBColor(1f - (y / (float)height), 1f, 1f);
                    hueSpectrumImageGraphics.setColor(color);
                    hueSpectrumImageGraphics.fillRect(0, y, width, 1);
                }

                hueSpectrumImageGraphics.dispose();
            }

            graphics.drawImage(hueSpectrumImage, 0, 0, null);

            // Mark the selected hue
            float hue = hueChooser.getHue();
            graphics.setColor(Color.BLACK);
            graphics.fillRect(0, Math.min((int)(height * (1f - hue)), height - 1), width, 1);
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (capture
                && Mouse.getCapturer() != component) {
                Mouse.capture(component);
            }

            if (Mouse.getCapturer() == component) {
                setSelectedColor(y);
            }

            return consumed;
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            if (Mouse.getCapturer() != component) {
                capture = false;
            }
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                setSelectedColor(y);
                capture = true;
                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                capture = false;

                if (Mouse.getCapturer() == component) {
                    Mouse.release();
                }
            }

            return consumed;
        }

        private void setSelectedColor(int y) {
            ColorChooser colorChooser = (ColorChooser)TerraColorChooserSkin.this.getComponent();

            int height = getHeight();

            float hue = 1f - (Math.min(Math.max(y, 0), height - 1) / (float)height);
            float saturation = saturationValueChooser.getSaturation();
            float value = saturationValueChooser.getValue();

            hueChooser.setHue(hue);

            updating = true;
            try {
                colorChooser.setSelectedColor(Color.getHSBColor(hue, saturation, value));
            } finally {
                updating = false;
            }
        }
    }

    private class SaturationValueChooser extends Component {
        private float saturation = 0f;
        private float value = 0f;

        public SaturationValueChooser() {
            setSkin(new SaturationValueChooserSkin());
        }

        public float getSaturation() {
            return saturation;
        }

        public void setSaturation(float saturation) {
            this.saturation = saturation;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }
    }

    private class SaturationValueChooserSkin extends ComponentSkin {
        private boolean capture = false;

        @Override
        public int getPreferredWidth(int height) {
            return 140;
        }

        @Override
        public int getPreferredHeight(int width) {
            return 185;
        }

        @Override
        public void layout() {
            // No-op
        }

        @Override
        public void paint(Graphics2D graphics) {
            int width = getWidth();
            int height = getHeight();

            float hue = hueChooser.getHue();

            if (saturationValueImage == null) {
                saturationValueImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                Graphics2D saturationValueImageGraphics = saturationValueImage.createGraphics();

                // Paint the saturation/value spectrum
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        Color color = Color.getHSBColor(hue, 1f - (y / (float)height),
                            x / (float)width);
                        saturationValueImageGraphics.setColor(color);
                        saturationValueImageGraphics.fillRect(x, y, 1, 1);
                    }
                }

                saturationValueImageGraphics.dispose();
            }

            graphics.drawImage(saturationValueImage, 0, 0, null);

            // Mark the selected saturation/value
            if (!getComponent().getWindow().isClosing()) {
                float saturation = saturationValueChooser.getSaturation();
                float value = saturationValueChooser.getValue();
                graphics.setColor(Color.WHITE);
                graphics.setXORMode(Color.getHSBColor(hue, 0f, 0f));
                graphics.fillRect(0, Math.min((int)(height * (1f - saturation)), height - 1), width, 1);
                graphics.fillRect(Math.min((int)(width * value), width - 1), 0, 1, height);
            }
        }

        @Override
        public boolean isFocusable() {
            return false;
        }

        @Override
        public boolean mouseMove(Component component, int x, int y) {
            boolean consumed = super.mouseMove(component, x, y);

            if (capture
                && Mouse.getCapturer() != component) {
                Mouse.capture(component);
            }

            if (Mouse.getCapturer() == component) {
                setSelectedColor(x, y);
            }

            return consumed;
        }

        @Override
        public void mouseOut(Component component) {
            super.mouseOut(component);

            if (Mouse.getCapturer() != component) {
                capture = false;
            }
        }

        @Override
        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseDown(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                setSelectedColor(x, y);
                capture = true;
                consumed = true;
            }

            return consumed;
        }

        @Override
        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            boolean consumed = super.mouseUp(component, button, x, y);

            if (button == Mouse.Button.LEFT) {
                capture = false;

                if (Mouse.getCapturer() == component) {
                    Mouse.release();
                }
            }

            return consumed;
        }

        private void setSelectedColor(int x, int y) {
            ColorChooser colorChooser = (ColorChooser)TerraColorChooserSkin.this.getComponent();

            int width = getWidth();
            int height = getHeight();

            float hue = hueChooser.getHue();
            float saturation = 1f - (Math.min(Math.max(y, 0), height - 1) / (float)height);
            float value = Math.min(Math.max(x, 0), width - 1) / (float)width;

            saturationValueChooser.setSaturation(saturation);
            saturationValueChooser.setValue(value);

            updating = true;
            try {
                colorChooser.setSelectedColor(Color.getHSBColor(hue, saturation, value));
            } finally {
                updating = false;
            }
        }
    }

    private TablePane tablePane = new TablePane();
    private SaturationValueChooser saturationValueChooser = new SaturationValueChooser();
    private HueChooser hueChooser = new HueChooser();

    private BufferedImage saturationValueImage = null;
    private BufferedImage hueSpectrumImage = null;

    private boolean updating = false;

    public TerraColorChooserSkin() {
        tablePane.getStyles().put("horizontalSpacing", 6);
        tablePane.getColumns().add(new TablePane.Column(31, true));
        tablePane.getColumns().add(new TablePane.Column(4, true));

        TablePane.Row row = new TablePane.Row(1, true);
        tablePane.getRows().add(row);

        row.add(saturationValueChooser);
        row.add(hueChooser);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        ColorChooser colorChooser = (ColorChooser)component;
        colorChooser.add(tablePane);

        selectedColorChanged(colorChooser, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        return tablePane.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return tablePane.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return tablePane.getPreferredSize();
    }

    @Override
    public void layout() {
        tablePane.setSize(getWidth(), getHeight());
        tablePane.setLocation(0, 0);

        saturationValueImage = null;
        hueSpectrumImage = null;
    }

    public int getSpacing() {
        return (Integer)tablePane.getStyles().get("horizontalSpacing");
    }

    public void setSpacing(int spacing) {
        if (spacing < 0) {
            throw new IllegalArgumentException("spacing is negative.");
        }

        tablePane.getStyles().put("horizontalSpacing", spacing);
    }

    public final void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        tablePane.getStyles().put("horizontalSpacing", spacing);
    }

    @Override
    public void selectedColorChanged(ColorChooser colorChooser, Color previousSelectedColor) {
        saturationValueImage = null;

        if (!updating) {
            Color color = colorChooser.getSelectedColor();

            float hue = 0f;
            float saturation = 0f;
            float value = 0f;

            if (color != null) {
                float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(),
                    color.getBlue(), null);
                hue = hsb[0];
                saturation = hsb[1];
                value = hsb[2];
            }

            hueChooser.setHue(hue);
            saturationValueChooser.setSaturation(saturation);
            saturationValueChooser.setValue(value);
        }

        repaintComponent();
    }
}
