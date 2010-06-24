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

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.ColorItem;
import org.apache.pivot.wtk.media.Drawing;
import org.apache.pivot.wtk.media.drawing.Ellipse;
import org.apache.pivot.wtk.media.drawing.Rectangle;
import org.apache.pivot.wtk.media.drawing.Shape;
import org.apache.pivot.wtk.media.drawing.Text;

public class MenuButtons extends Window implements Bindable {
    private ListButton colorListButton = null;
    private Drawing drawing = null;
    private Rectangle border = null;

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
    public void initialize(Dictionary<String, Object> context, Resources resources) {
        colorListButton = (ListButton)context.get("colorListButton");
        drawing = (Drawing)context.get("drawing");
        border = (Rectangle)context.get("border");
    }

    public Color getSelectedColor() {
        ColorItem colorItem = (ColorItem)colorListButton.getSelectedItem();
        return colorItem.getColor();
    }

    public Point getRandomLocation(Shape shape) {
        Bounds bounds = shape.getBounds();
        int x = (int)(Math.random() * (border.getWidth() - bounds.width));
        int y = (int)(Math.random() * (border.getHeight() - bounds.height));

        return new Point(x, y);
    }
}
