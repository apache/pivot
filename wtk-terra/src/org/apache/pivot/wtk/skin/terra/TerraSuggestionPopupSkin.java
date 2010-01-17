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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupListener;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Terra suggestion popup skin.
 */
public class TerraSuggestionPopupSkin extends WindowSkin implements SuggestionPopupListener {
    private Border suggestionListViewBorder = new Border();
    private ListView suggestionListView = new ListView();

    public TerraSuggestionPopupSkin () {
        suggestionListViewBorder.setContent(suggestionListView);

        // TODO Attach listeners to suggestion list view
    }

    @Override
    public void install(Component component) {
        super.install(component);

        SuggestionPopup suggestionPopup = (SuggestionPopup)component;
        suggestionPopup.getSuggestionPopupListeners().add(this);

        suggestionPopup.setContent(suggestionListViewBorder);

        suggestionListView.setListData(suggestionPopup.getSuggestions());
        suggestionListView.setItemRenderer(suggestionPopup.getSuggestionRenderer());
    }

    public Font getFont() {
        return (Font)suggestionListView.getStyles().get("font");
    }

    public void setFont(Font font) {
        suggestionListView.getStyles().put("font", font);
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
        return (Color)suggestionListView.getStyles().get("color");
    }

    public void setColor(Color color) {
        suggestionListView.getStyles().put("color", color);
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getBorderColor() {
        return (Color)suggestionListViewBorder.getStyles().get("color");
    }

    public void setBorderColor(Color borderColor) {
        suggestionListViewBorder.getStyles().put("color", borderColor);
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        // TODO Add listeners to text input
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        // TODO Remove listeners from text input

        super.windowClosed(window, display, owner);
    }

    @Override
    public void suggestionsChanged(SuggestionPopup suggestionPopup,
        List<?> previousSuggestions) {
        suggestionListView.setListData(suggestionPopup.getSuggestions());
    }

    @Override
    public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
        ListView.ItemRenderer previousSuggestionRenderer) {
        suggestionListView.setItemRenderer(suggestionPopup.getSuggestionRenderer());
    }
}
