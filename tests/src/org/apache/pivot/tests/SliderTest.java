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
package org.apache.pivot.tests;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.Picture;

public final class SliderTest implements Application {
    private Window window = null;
    private Slider slider1 = null;
    private Slider slider2 = null;
    private Label valueLabel1 = null;
    private Label valueLabel2 = null;
    private ImageView image = null;
    private SliderPicture picture = null;

    private static class SliderPicture extends Picture {
        SliderPicture(final int width, final int height) {
            super(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB));
        }

        public void clear(final Color color) {
            BufferedImage image = getBufferedImage();
            Graphics2D graphics = image.createGraphics();
            graphics.setPaint(color);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.dispose();
        }

        public void drawPoint(final int x, final int y, final Color color) {
            BufferedImage image = getBufferedImage();
            Graphics2D graphics = image.createGraphics();
            graphics.setPaint(color);
            graphics.fillOval(x - 3, y - 3, 6, 6);
            graphics.dispose();
        }
    }

    private void drawPoint(final int x, final int y,
        final int xMin, final int xMax, final int yMin, final int yMax) {
        // Scale and translate the x, y values to the size of the picture
        int width = picture.getWidth();
        int height = picture.getHeight();
        int ourX = (x - xMin) * width / (xMax - xMin);
        int ourY = (y - yMin) * height / (yMax - yMin);
        picture.drawPoint(ourX, ourY, Color.RED);
        image.repaint();
    }

    @Override
    public void startup(final Display display, final Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = new Window((Component) bxmlSerializer.readObject(getClass().getResource(
            "slider_test.bxml")));
        slider1 = (Slider) bxmlSerializer.getNamespace().get("slider1");
        slider1.getSliderValueListeners().add((slider, previousValue) -> {
            valueLabel1.setText(Integer.toString(slider.getValue()));
            drawPoint(slider.getValue(), slider2.getValue(), slider.getStart(), slider.getEnd(),
                slider2.getStart(), slider2.getEnd());
        });
        slider2 = (Slider) bxmlSerializer.getNamespace().get("slider2");
        slider2.getSliderValueListeners().add((slider, previousValue) -> {
            valueLabel2.setText(Integer.toString(slider.getValue()));
            drawPoint(slider1.getValue(), slider.getValue(), slider1.getStart(), slider1.getEnd(),
                slider.getStart(), slider.getEnd());
        });

        valueLabel1 = (Label) bxmlSerializer.getNamespace().get("valueLabel1");
        valueLabel2 = (Label) bxmlSerializer.getNamespace().get("valueLabel2");

        image = (ImageView) bxmlSerializer.getNamespace().get("imageView");
        int width = slider1.getEnd() - slider1.getStart();
        int height = slider2.getEnd() - slider2.getStart();
        picture = new SliderPicture(width, height);
        picture.clear(Color.LIGHT_GRAY);
        drawPoint(slider1.getValue(), slider2.getValue(), slider1.getStart(), slider1.getEnd(),
            slider2.getStart(), slider2.getEnd());
        image.setImage(picture);

        window.setTitle("Slider Test");
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(final boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(SliderTest.class, args);
    }
}
