/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Font;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.Decorator;
import pivot.wtk.Label;
import pivot.wtk.Rectangle;

/**
 * Decorator that overlays a watermark over a component.
 *
 * @author tvolkert
 * @author gbrown
 */
public class WatermarkDecorator implements Decorator {
    private double theta = Math.PI / 4d;
    private float opacity = 0.25f;
    private Label label = new Label();

    private Component component = null;
    private Graphics2D graphics = null;

    public WatermarkDecorator() {
        this(null);
    }

    public WatermarkDecorator(String text) {
        Font font = (Font)label.getStyles().get("font");
        label.getStyles().put("font", font.deriveFont(Font.BOLD, 36));

        setText(text);
    }

    public String getText() {
        return label.getText();
    }

    public void setText(String text) {
        label.setText(text);
        label.setSize(label.getPreferredSize());
    }

    public Font getFont() {
        return (Font)label.getStyles().get("font");
    }

    public void setFont(Font font) {
        label.getStyles().put("font", font);
    }

    public final void setFont(String font) {
        label.getStyles().put("font", font);
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        this.opacity = opacity;
    }

    public Graphics2D prepare(Component component, Graphics2D graphics) {
        this.component = component;
        this.graphics = graphics;

        return graphics;
    }

    public void update() {
        final double cosTheta = Math.cos(theta);
        final double sinTheta = Math.sin(theta);

        double labelWidth = label.getWidth();
        double labelHeight = label.getHeight();

        double rotatedWidth = labelWidth * cosTheta + labelHeight * sinTheta;
        double rotatedHeight = labelWidth * sinTheta + labelHeight * cosTheta;

        Graphics2D watermarkGraphics = (Graphics2D)graphics.create();

        watermarkGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        watermarkGraphics.translate(((double)component.getWidth() - rotatedWidth) / 2d,
            ((double)component.getHeight() - rotatedHeight) / 2d + labelWidth * sinTheta);
        watermarkGraphics.rotate(-theta);

        graphics.setClip(null);

        label.paint(watermarkGraphics);

        watermarkGraphics.dispose();
    }

    public Rectangle getBounds(Component component) {
        return component.getBounds();
    }

    public void repaint(Component component, int x, int y, int width, int height) {
        // No-op
    }
}
