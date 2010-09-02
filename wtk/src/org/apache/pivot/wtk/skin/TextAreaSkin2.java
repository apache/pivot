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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Transparency;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.TextArea2;
import org.apache.pivot.wtk.TextAreaListener2;
import org.apache.pivot.wtk.TextAreaContentListener2;
import org.apache.pivot.wtk.TextAreaSelectionListener2;
import org.apache.pivot.wtk.Theme;

/**
 * Text area skin.
 */
public class TextAreaSkin2 extends ComponentSkin implements TextArea2.Skin,
    TextAreaListener2, TextAreaContentListener2, TextAreaSelectionListener2 {
    private Font font;
    private Color color;
    private Color backgroundColor;
    private Color inactiveColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;

    private Insets margin = new Insets(4);

    private boolean wrapText = true;

    public TextAreaSkin2() {
        Theme theme = Theme.getTheme();
        font = theme.getFont();
        color = Color.BLACK;
        backgroundColor = Color.WHITE;
        inactiveColor = Color.GRAY;
        selectionColor = Color.LIGHT_GRAY;
        selectionBackgroundColor = Color.BLACK;
        inactiveSelectionColor = Color.LIGHT_GRAY;
        inactiveSelectionBackgroundColor = Color.BLACK;
    }

    @Override
    public void install(Component component) {
        super.install(component);

        // TODO
    }

    @Override
    public int getPreferredWidth(int height) {
        // TODO
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        // TODO
        return 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO
        return new Dimensions(0, 0);
    }

    @Override
    public void layout() {
        // TODO
    }

    @Override
    public void paint(Graphics2D graphics) {
        // TODO
    }

    @Override
    public boolean isOpaque() {
        return (backgroundColor != null
            && backgroundColor.getTransparency() == Transparency.OPAQUE);
    }

    public int getInsertionPoint(int x, int y) {
        // TODO
        return -1;
    }

    public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
        // TODO
        return -1;
    }

    public int getRowIndex(int index) {
        // TODO
        return -1;
    }

    public int getRowCount() {
        // TODO
        return 0;
    }

    public Bounds getCharacterBounds(int index) {
        // TODO
        return null;
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public final void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(decodeFont(font));
    }

    public final void setFont(Dictionary<String, ?> font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Theme.deriveFont(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public final void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public Color getInactiveColor() {
        return inactiveColor;
    }

    public void setInactiveColor(Color inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        this.inactiveColor = inactiveColor;
        repaintComponent();
    }

    public final void setInactiveColor(String inactiveColor) {
        if (inactiveColor == null) {
            throw new IllegalArgumentException("inactiveColor is null.");
        }

        setColor(GraphicsUtilities.decodeColor(inactiveColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public final void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public final void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public final void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public final void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor));
    }

    public Insets getMargin() {
        return margin;
    }

    public void setMargin(Insets margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        this.margin = margin;
        invalidateComponent();
    }

    public final void setMargin(Dictionary<String, ?> margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(new Insets(margin));
    }

    public final void setMargin(int margin) {
        setMargin(new Insets(margin));
    }

    public final void setMargin(Number margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(margin.intValue());
    }

    public final void setMargin(String margin) {
        if (margin == null) {
            throw new IllegalArgumentException("margin is null.");
        }

        setMargin(Insets.decode(margin));
    }

    public boolean getWrapText() {
        return wrapText;
    }

    public void setWrapText(boolean wrapText) {
        this.wrapText = wrapText;
        invalidateComponent();
    }

    @Override
    public void maximumLengthChanged(TextArea2 textArea, int previousMaximumLength) {
        // TODO
    }

    @Override
    public void editableChanged(TextArea2 textArea) {
        // TODO
    }

    @Override
    public void textInserted(TextArea2 textArea, int index, int count) {
        // TODO
    }

    @Override
    public void textRemoved(TextArea2 textArea, int index, int count) {
        // TODO
    }

    @Override
    public void textChanged(TextArea2 textArea) {
        // TODO
    }

    @Override
    public void selectionChanged(TextArea2 textArea, int previousSelectionStart,
        int previousSelectionLength) {
        // TODO
    }
}
