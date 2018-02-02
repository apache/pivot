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
import java.awt.Font;
import java.awt.Graphics2D;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Utils;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.skin.LinkButtonSkin;

/**
 * Terra link button skin.
 */
public class TerraLinkButtonSkin extends LinkButtonSkin {
    private Font font;
    private Color color;
    private Color disabledColor;

    public TerraLinkButtonSkin() {
        Theme theme = currentTheme();
        font = theme.getFont();
        color = theme.getColor(12);
        disabledColor = theme.getColor(7);
    }

    @Override
    public int getPreferredWidth(int height) {
        LinkButton linkButton = (LinkButton) getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        LinkButton linkButton = (LinkButton) getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        LinkButton linkButton = (LinkButton) getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getPreferredSize();
    }

    @Override
    public int getBaseline(int width, int height) {
        LinkButton linkButton = (LinkButton) getComponent();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, false);

        return dataRenderer.getBaseline(width, height);
    }

    @Override
    public void paint(Graphics2D graphics) {
        LinkButton linkButton = (LinkButton) getComponent();
        Dimensions size = getSize();

        Button.DataRenderer dataRenderer = linkButton.getDataRenderer();
        dataRenderer.render(linkButton.getButtonData(), linkButton, highlighted);
        dataRenderer.setSize(size);

        dataRenderer.paint(graphics);
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        Utils.checkNull(font, "font");

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        Utils.checkNull(color, "color");

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        setColor(GraphicsUtilities.decodeColor(color, "color"));
    }

    public final void setColor(int color) {
        Theme theme = currentTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        Utils.checkNull(disabledColor, "disabledColor");

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public final void setDisabledColor(String disabledColor) {
        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor, "disabledColor"));
    }

    public final void setDisabledColor(int disabledColor) {
        Theme theme = currentTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }
}
