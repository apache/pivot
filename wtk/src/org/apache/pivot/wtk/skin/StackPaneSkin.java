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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.StackPane;

/**
 * Stack pane skin.
 *
 * @author gbrown
 */
public class StackPaneSkin extends ContainerSkin {
    private Insets padding = new Insets(0);

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;
        StackPane stackPane = (StackPane)getComponent();

        for (Component component : stackPane) {
            preferredWidth = Math.max(preferredWidth,
                component.getPreferredWidth(height));
        }

        preferredWidth += (padding.left + padding.right);

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;
        StackPane stackPane = (StackPane)getComponent();

        for (Component component : stackPane) {
            preferredHeight = Math.max(preferredHeight,
                component.getPreferredHeight(width));
        }

        preferredHeight += (padding.top + padding.bottom);

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        StackPane stackPane = (StackPane)getComponent();

        for (Component component : stackPane) {
            Dimensions preferredCardSize = component.getPreferredSize();

            preferredWidth = Math.max(preferredWidth,
                preferredCardSize.width);

            preferredHeight = Math.max(preferredHeight,
                preferredCardSize.height);
        }

        preferredWidth += (padding.left + padding.right);
        preferredHeight += (padding.top + padding.bottom);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    public void layout() {
        // Set the size of all components to match the size of the stack pane,
        // minus padding
        StackPane stackPane = (StackPane)getComponent();

        int width = getWidth() - (padding.left + padding.right);
        int height = getHeight() - (padding.top + padding.bottom);

        for (Component component : stackPane) {
            component.setLocation(padding.left, padding.top);
            component.setSize(width, height);
        }
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
    }
}
