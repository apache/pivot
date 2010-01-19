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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Direction;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Terra suggestion popup skin.
 */
public class TerraSuggestionPopupSkin extends WindowSkin implements SuggestionPopupListener {
    private Border suggestionListViewBorder = new Border();
    private ListView suggestionListView = new ListView();

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
            TextInput textInput = suggestionPopup.getTextInput();

            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!suggestionPopup.isAncestor(descendant)
                && descendant != textInput) {
                suggestionPopup.close(false);
            }

            return false;
        }

        @Override
        public boolean mouseWheel(Container container, Mouse.ScrollType scrollType,
            int scrollAmount, int wheelRotation, int x, int y) {
            return true;
        }
    };

    private ComponentKeyListener textInputKeyListener = new ComponentKeyListener.Adapter() {
        @Override
        public boolean keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();

            if (keyCode == Keyboard.KeyCode.DOWN) {
                if (suggestionListView.getSelectedIndex() == -1
                    && suggestionListView.getListData().getLength() > 0) {
                    suggestionListView.setSelectedIndex(0);
                }

                suggestionPopup.requestFocus();
                consumed = true;
            } else if (keyCode == Keyboard.KeyCode.ESCAPE) {
                suggestionPopup.close(false);
                consumed = true;
            }

            return consumed;
        }
    };

    private ListViewSelectionListener listViewSelectionListener = new ListViewSelectionListener.Adapter() {
        @Override
        public void selectedRangesChanged(ListView listView, Sequence<Span> previousSelectedRanges) {
            int index = suggestionListView.getSelectedIndex();

            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
            suggestionPopup.setSelectedIndex(index);
        }
    };

    public TerraSuggestionPopupSkin () {
        suggestionListView.getListViewSelectionListeners().add(listViewSelectionListener);
        suggestionListViewBorder.setContent(suggestionListView);
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
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
        suggestionPopup.close(true);

        return true;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
        TextInput textInput = suggestionPopup.getTextInput();

        switch (keyCode) {
            case Keyboard.KeyCode.ENTER: {
                suggestionPopup.close(true);
                break;
            }

            case Keyboard.KeyCode.TAB: {
                suggestionPopup.close(true);

                if (suggestionPopup.isClosed()) {
                    Direction direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                        Direction.BACKWARD : Direction.FORWARD;
                    textInput.transferFocus(direction);
                }

                break;
            }

            case Keyboard.KeyCode.ESCAPE: {
                suggestionPopup.close(false);
                break;
            }
        }

        return false;
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);

        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
        TextInput textInput = suggestionPopup.getTextInput();
        textInput.getComponentKeyListeners().add(textInputKeyListener);

        // Reposition under text input
        int x = textInput.getX();
        int y = textInput.getY() + textInput.getHeight();
        suggestionPopup.setLocation(x, y - 1);
        suggestionPopup.setPreferredWidth(textInput.getWidth());
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        display.getContainerMouseListeners().remove(displayMouseListener);

        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
        suggestionPopup.clearFocusDescendant();

        TextInput textInput = suggestionPopup.getTextInput();
        textInput.getComponentKeyListeners().remove(textInputKeyListener);

        super.windowClosed(window, display, owner);

        textInput.requestFocus();
        textInput.setSelection(textInput.getTextLength(), 0);
    }

    @Override
    public void suggestionsChanged(SuggestionPopup suggestionPopup,
        List<?> previousSuggestions) {
        suggestionListView.setListData(suggestionPopup.getSuggestions());
    }

    @Override
    public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
        SuggestionPopup.SuggestionRenderer previousSuggestionRenderer) {
        suggestionListView.setItemRenderer(suggestionPopup.getSuggestionRenderer());
    }

    @Override
    public void selectedIndexChanged(SuggestionPopup suggestionPopup,
        int previousSelectedIndex) {
        TextInput textInput = suggestionPopup.getTextInput();

        Object suggestion = suggestionPopup.getSelectedSuggestion();
        if (suggestion != null) {
            SuggestionPopup.SuggestionRenderer suggestionRenderer =
                suggestionPopup.getSuggestionRenderer();
            textInput.setText(suggestionRenderer.toString(suggestion));
        }
    }
}
