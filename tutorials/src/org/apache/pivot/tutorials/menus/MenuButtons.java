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
package org.apache.pivot.tutorials.menus;

import java.awt.Color;
import java.awt.Paint;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ColorItem;
import org.apache.pivot.wtk.media.Drawing;
import org.apache.pivot.wtk.media.drawing.Canvas;
import org.apache.pivot.wtk.media.drawing.Ellipse;
import org.apache.pivot.wtk.media.drawing.Rectangle;
import org.apache.pivot.wtk.media.drawing.Shape;
import org.apache.pivot.wtk.media.drawing.Text;

public class MenuButtons implements Application {
    private Window window = null;
    private ListButton colorListButton = null;
    private ImageView imageView = null;

    private Drawing drawing = null;

    public static final int MAX_X = 480;
    public static final int MAX_Y = 360;

    public MenuButtons() {
        Action.getNamedActions().put("newCircle", new Action() {
            @Override
            public void perform() {
                Ellipse ellipse = new Ellipse();
                ellipse.setSize(50, 50);

                ellipse.setStroke((Paint)null);
                ellipse.setFill(getSelectedColor());
                ellipse.setOrigin(getRandomLocation(ellipse));

                drawing.getCanvas().add(ellipse);
            }
        });

        Action.getNamedActions().put("newSquare", new Action() {
            @Override
            public void perform() {
                Rectangle rectangle = new Rectangle();
                rectangle.setSize(50, 50);

                rectangle.setStroke((Paint)null);
                rectangle.setFill(getSelectedColor());
                rectangle.setOrigin(getRandomLocation(rectangle));

                drawing.getCanvas().add(rectangle);
            }
        });

        Action.getNamedActions().put("newText", new Action() {
            @Override
            public void perform() {
                Text text = new Text();
                text.setText("ABC");
                text.setFont("Arial BOLD 24");

                text.setFill(getSelectedColor());
                text.setOrigin(getRandomLocation(text));

                drawing.getCanvas().add(text);
            }
        });
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BeanSerializer beanSerializer = new BeanSerializer();
        window = (Window)beanSerializer.readObject(this, "menu_buttons.bxml");
        colorListButton = (ListButton)beanSerializer.get("colorListButton");
        imageView = (ImageView)beanSerializer.get("imageView");


        Rectangle borderRectangle = new Rectangle();
        borderRectangle.setSize(MAX_X, MAX_Y);
        borderRectangle.setStroke((Paint)null);
        borderRectangle.setFill("#eeeeee");

        Canvas canvas = new Canvas();
        canvas.add(borderRectangle);

        drawing = new Drawing(canvas);

        imageView.setImage(drawing);

        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public Color getSelectedColor() {
        ColorItem colorItem = (ColorItem)colorListButton.getSelectedItem();
        return colorItem.getColor();
    }

    public Point getRandomLocation(Shape shape) {
        Bounds bounds = shape.getBounds();

        int x = (int)(Math.random() * (MAX_X - bounds.width));
        int y = (int)(Math.random() * (MAX_Y - bounds.height));

        return new Point(x, y);
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(MenuButtons.class, args);
    }
}
