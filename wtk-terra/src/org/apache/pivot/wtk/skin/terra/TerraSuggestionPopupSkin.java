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
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupListener;
import org.apache.pivot.wtk.SuggestionPopupStateListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Terra suggestion popup skin.
 */
public class TerraSuggestionPopupSkin extends WindowSkin
    implements SuggestionPopupListener, SuggestionPopupStateListener {
    private Border suggestionListViewBorder = new Border();
    private ListView suggestionListView = new ListView();

    private DropShadowDecorator dropShadowDecorator = null;
    private Transition closeTransition = null;

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

    private ComponentStateListener textInputStateListener = new ComponentStateListener.Adapter() {
        @Override
        public void focusedChanged(Component component, Component obverseComponent) {
            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();

            if (!component.isFocused()
                && !suggestionPopup.containsFocus()) {
                suggestionPopup.close();
            }
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

    private static final int CLOSE_TRANSITION_DURATION = 150;
    private static final int CLOSE_TRANSITION_RATE = 30;

    public TerraSuggestionPopupSkin () {
        suggestionListView.getStyles().put("variableItemHeight", true);
        suggestionListView.getListViewSelectionListeners().add(listViewSelectionListener);
        suggestionListViewBorder.setContent(suggestionListView);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        SuggestionPopup suggestionPopup = (SuggestionPopup)component;
        suggestionPopup.getSuggestionPopupListeners().add(this);
        suggestionPopup.getSuggestionPopupStateListeners().add(this);

        suggestionPopup.setContent(suggestionListViewBorder);

        suggestionListView.setListData(suggestionPopup.getSuggestions());
        suggestionListView.setItemRenderer(suggestionPopup.getSuggestionRenderer());

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        suggestionPopup.getDecorators().add(dropShadowDecorator);
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

                FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                    FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;
                textInput.transferFocus(direction);

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

        dropShadowDecorator.setShadowOpacity(DropShadowDecorator.DEFAULT_SHADOW_OPACITY);

        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
        TextInput textInput = suggestionPopup.getTextInput();
        textInput.getComponentStateListeners().add(textInputStateListener);
        textInput.getComponentKeyListeners().add(textInputKeyListener);

        // Reposition under text input
        Point location = textInput.mapPointToAncestor(textInput.getDisplay(), 0, 0);
        suggestionPopup.setLocation(location.x, location.y + textInput.getHeight() - 1);
        suggestionPopup.setMinimumPreferredWidth(textInput.getWidth());
    }

    @Override
    public void windowCloseVetoed(Window window, Vote reason) {
        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();

            suggestionListViewBorder.setEnabled(true);
            closeTransition = null;
        }
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        display.getContainerMouseListeners().remove(displayMouseListener);
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

    @Override
    public void selectedIndexChanged(SuggestionPopup suggestionPopup,
        int previousSelectedIndex) {
        TextInput textInput = suggestionPopup.getTextInput();

        Object suggestion = suggestionPopup.getSelectedSuggestion();
        if (suggestion != null) {
            ListView.ItemRenderer suggestionRenderer = suggestionPopup.getSuggestionRenderer();
            textInput.setText(suggestionRenderer.toString(suggestion));
        }
    }

    @Override
    public Vote previewSuggestionPopupClose(final SuggestionPopup suggestionPopup, final boolean result) {
        if (closeTransition == null) {
            suggestionListViewBorder.setEnabled(false);

            closeTransition = new FadeWindowTransition(suggestionPopup,
                CLOSE_TRANSITION_DURATION, CLOSE_TRANSITION_RATE,
                dropShadowDecorator);

            closeTransition.start(new TransitionListener() {
                @Override
                public void transitionCompleted(Transition transition) {
                    suggestionPopup.close(result);
                }
            });
        }

        return (closeTransition != null
            && closeTransition.isRunning()) ? Vote.DEFER : Vote.APPROVE;
    }

    @Override
    public void suggestionPopupCloseVetoed(SuggestionPopup suggestionPopup, Vote reason) {
        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();

            suggestionListViewBorder.setEnabled(true);
            closeTransition = null;
        }
    }

    @Override
    public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
        suggestionPopup.clearFocusDescendant();

        TextInput textInput = suggestionPopup.getTextInput();
        textInput.getComponentStateListeners().remove(textInputStateListener);
        textInput.getComponentKeyListeners().remove(textInputKeyListener);

        textInput.requestFocus();

        suggestionListViewBorder.setEnabled(true);
        closeTransition = null;
    }
}
