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
package org.apache.pivot.tutorials.navigation;

import java.awt.Color;
import java.awt.Graphics2D;

import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.skin.ComponentSkin;

class RulerSkin extends ComponentSkin implements RulerListener {
    @Override
    public void install(Component component) {
        super.install(component);

        Ruler ruler = (Ruler)component;
        ruler.getRulerListeners().add(this);
    }

    @Override
    public void uninstall() {
        Ruler ruler = (Ruler)getComponent();
        ruler.getRulerListeners().remove(this);

        super.uninstall();
    }

    @Override
    public void layout() {
        // No-op
    }

    @Override
    public int getPreferredHeight(int width) {
        Ruler ruler = (Ruler)getComponent();
        Orientation orientation = ruler.getOrientation();

        return (orientation == Orientation.HORIZONTAL) ? 20 : 0;
    }

    @Override
    public int getPreferredWidth(int height) {
        Ruler ruler = (Ruler)getComponent();
        Orientation orientation = ruler.getOrientation();

        return (orientation == Orientation.VERTICAL) ? 20 : 0;
    }

    @Override
    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        Ruler ruler = (Ruler)getComponent();

        graphics.setColor(new Color(0xFF, 0xFF, 0xE0));
        graphics.fillRect(0, 0, width, height);

        graphics.setColor(Color.BLACK);
        graphics.drawRect(0, 0, width - 1, height - 1);

        Orientation orientation = ruler.getOrientation();
        switch (orientation) {
            case HORIZONTAL: {
                for (int i = 0, n = width / 5 + 1; i < n; i++) {
                    int x = i * 5;

                    if (i % 4 == 0) {
                        graphics.drawLine(x, 0, x, height / 2);
                    } else {
                        graphics.drawLine(x, 0, x, height / 4);
                    }
                }

                break;
            }

            case VERTICAL: {
                for (int i = 0, n = height / 5 + 1; i < n; i++) {
                    int y = i * 5;

                    if (i % 4 == 0) {
                        graphics.drawLine(0, y, width / 2, y);
                    } else {
                        graphics.drawLine(0, y, width / 4, y);
                    }
                }

                break;
            }
        }
    }

    public void orientationChanged(Ruler ruler) {
        invalidateComponent();
    }
}
