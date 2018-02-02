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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.StackPane;

/**
 * Stack pane skin.
 */
public class StackPaneSkin extends ContainerSkin {
    private Insets padding = Insets.NONE;

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;
        StackPane stackPane = (StackPane) getComponent();

        for (Component component : stackPane) {
            preferredWidth = Math.max(preferredWidth, component.getPreferredWidth(height));
        }

        preferredWidth += padding.getWidth();

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;
        StackPane stackPane = (StackPane) getComponent();

        for (Component component : stackPane) {
            preferredHeight = Math.max(preferredHeight, component.getPreferredHeight(width));
        }

        preferredHeight += padding.getHeight();

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        StackPane stackPane = (StackPane) getComponent();

        for (Component component : stackPane) {
            Dimensions preferredCardSize = component.getPreferredSize();

            preferredWidth = Math.max(preferredWidth, preferredCardSize.width);
            preferredHeight = Math.max(preferredHeight, preferredCardSize.height);
        }

        preferredWidth += padding.getWidth();
        preferredHeight += padding.getHeight();

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        return -1;
    }

    @Override
    public void layout() {
        // Set the size of all components to match the size of the stack pane,
        // minus padding
        StackPane stackPane = (StackPane) getComponent();

        int width = Math.max(getWidth() - padding.getWidth(), 0);
        int height = Math.max(getHeight() - padding.getHeight(), 0);

        for (Component component : stackPane) {
            component.setLocation(padding.left, padding.top);
            component.setSize(width, height);
        }
    }

    /**
     * @return The amount of space between the edge of the StackPane and its
     * components.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the StackPane and
     * its components.
     *
     * @param padding The individual padding amounts for each edge.
     */
    public void setPadding(Insets padding) {
        Utils.checkNull(padding, "padding");

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the StackPane and
     * its components.
     *
     * @param padding A dictionary with keys in the set {top, left, bottom, right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the StackPane and
     * its components.
     *
     * @param padding A sequence with values in the order [top, left, bottom, right].
     */
    public final void setPadding(Sequence<?> padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the StackPane and
     * its components, uniformly on all four edges.
     *
     * @param padding The single padding value for all four edges.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the StackPane and
     * its components, uniformly on all four edges.
     *
     * @param padding The single padding value for all four edges.
     */
    public final void setPadding(Number padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the StackPane and
     * its components.
     *
     * @param padding A string containing an integer or a JSON dictionary with
     * keys left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        setPadding(Insets.decode(padding));
    }
}
