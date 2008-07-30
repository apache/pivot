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
package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.ScrollPane;
import pivot.wtk.skin.ComponentSkin;

public class ScrollPaneCornerSkin extends ComponentSkin {
    private static final Color backgroundColor = new Color(0xF0, 0xEC, 0xE7);
    private static final Color color = new Color(0x81, 0x76, 0x67);

    @Override
    public void install(Component component) {
        validateComponentType(component, ScrollPane.Corner.class);

        super.install(component);
    }

    @Override
    public boolean isFocusable() {
        return false;
    }

    public int getPreferredWidth(int height) {
        // ScrollPane corners have no implicit preferred size.
        return 0;
    }

    public int getPreferredHeight(int width) {
        // ScrollPane corners have no implicit preferred size.
        return 0;
    }

    public Dimensions getPreferredSize() {
        // ScrollPane corners have no implicit preferred size.
        return new Dimensions(0, 0);
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        int width = getWidth();
        int height = getHeight();

        graphics.setPaint(backgroundColor);
        graphics.fill(new Rectangle2D.Double(0, 0, width, height));
    }
}
