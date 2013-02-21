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
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentStateListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.ContainerMouseListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.GraphicsUtilities;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.ListViewSelectionListener;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.SuggestionPopupListener;
import org.apache.pivot.wtk.SuggestionPopupSelectionListener;
import org.apache.pivot.wtk.SuggestionPopupStateListener;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.Keyboard.KeyCode;
import org.apache.pivot.wtk.Keyboard.Modifier;
import org.apache.pivot.wtk.effects.DropShadowDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.skin.WindowSkin;

/**
 * Terra suggestion popup skin.
 */
public class TerraSuggestionPopupSkin extends WindowSkin
    implements SuggestionPopupListener, SuggestionPopupSelectionListener, SuggestionPopupStateListener {
    private Panorama listViewPanorama;
    private Border listViewBorder;

    private ListView listView = new ListView();

    private DropShadowDecorator dropShadowDecorator = null;
    private Transition closeTransition = null;
    private boolean returnFocusToTextInput = true;

    private int closeTransitionDuration = DEFAULT_CLOSE_TRANSITION_DURATION;
    private int closeTransitionRate = DEFAULT_CLOSE_TRANSITION_RATE;

    private ContainerMouseListener displayMouseListener = new ContainerMouseListener.Adapter() {
        @Override
        public boolean mouseDown(Container container, Mouse.Button button, int x, int y) {
            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
            TextInput textInput = suggestionPopup.getTextInput();

            Display display = (Display)container;
            Component descendant = display.getDescendantAt(x, y);

            if (!suggestionPopup.isAncestor(descendant)
                && descendant != textInput) {
                returnFocusToTextInput = false;
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
                returnFocusToTextInput = false;
                suggestionPopup.close();
            }
        }
    };

    private ComponentKeyListener textInputKeyListener = new ComponentKeyListener.Adapter() {
        /**
         * {@link KeyCode#DOWN DOWN} Transfer focus to the suggestion list and
         * select the first suggestion if no others are selected.<br>
         * {@link KeyCode#ESCAPE ESCAPE} Close the popup with a 'result' of
         * false.
         */
        @Override
        public boolean keyPressed(Component component, int keyCode,
            Keyboard.KeyLocation keyLocation) {
            boolean consumed = false;

            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();

            if (keyCode == Keyboard.KeyCode.DOWN) {
                if (listView.getSelectedIndex() == -1
                    && listView.getListData().getLength() > 0) {
                    listView.setSelectedIndex(0);
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
        public void selectedItemChanged(ListView listViewArgument, Object previousSelectedItem) {
            int index = listViewArgument.getSelectedIndex();

            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
            suggestionPopup.setSelectedIndex(index);
        }
    };

    private ComponentKeyListener listViewKeyListener = new ComponentKeyListener.Adapter() {
        /**
         * {@link KeyCode#TAB TAB} Close the suggestion popup with a 'result' of
         * true, and transfer focus forwards from the TextInput.<br>
         * {@link KeyCode#TAB TAB} + {@link Modifier#SHIFT SHIFT} Close the
         * suggestion popup with a 'result' of true, and transfer focus backwards
         * from the TextInput.<br>
         */
        @Override
        public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
            TextInput textInput = suggestionPopup.getTextInput();

            switch (keyCode) {
                case Keyboard.KeyCode.TAB: {
                    returnFocusToTextInput = false;
                    suggestionPopup.close(true);

                    FocusTraversalDirection direction = (Keyboard.isPressed(Keyboard.Modifier.SHIFT)) ?
                        FocusTraversalDirection.BACKWARD : FocusTraversalDirection.FORWARD;
                    textInput.transferFocus(direction);

                    break;
                }

                default: {
                    break;
                }
            }

            return false;
        }
    };

    private static final int DEFAULT_CLOSE_TRANSITION_DURATION = 150;
    private static final int DEFAULT_CLOSE_TRANSITION_RATE = 30;

    public TerraSuggestionPopupSkin () {
        listView.getStyles().put("variableItemHeight", true);
        listView.getListViewSelectionListeners().add(listViewSelectionListener);
        listView.getComponentKeyListeners().add(listViewKeyListener);

        listViewPanorama = new Panorama(listView);
        listViewPanorama.getStyles().put("buttonBackgroundColor",
            listView.getStyles().get("backgroundColor"));
        listViewPanorama.getStyles().put("alwaysShowScrollButtons", true);

        listViewBorder = new Border(listViewPanorama);
    }

    @Override
    public void install(Component component) {
        super.install(component);

        SuggestionPopup suggestionPopup = (SuggestionPopup)component;
        suggestionPopup.getSuggestionPopupListeners().add(this);
        suggestionPopup.getSuggestionPopupSelectionListeners().add(this);
        suggestionPopup.getSuggestionPopupStateListeners().add(this);

        suggestionPopup.setContent(listViewBorder);

        listView.setListData(suggestionPopup.getSuggestionData());
        listView.setItemRenderer(suggestionPopup.getSuggestionRenderer());

        // Attach the drop-shadow decorator
        dropShadowDecorator = new DropShadowDecorator(3, 3, 3);
        suggestionPopup.getDecorators().add(dropShadowDecorator);
    }

    public Font getFont() {
        return (Font)listView.getStyles().get("font");
    }

    public void setFont(Font font) {
        listView.getStyles().put("font", font);
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
        return (Color)listView.getStyles().get("color");
    }

    public void setColor(Color color) {
        listView.getStyles().put("color", color);
    }

    public final void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public Color getBorderColor() {
        return (Color)listViewBorder.getStyles().get("color");
    }

    public void setBorderColor(Color borderColor) {
        listViewBorder.getStyles().put("color", borderColor);
    }

    public final void setBorderColor(String borderColor) {
        if (borderColor == null) {
            throw new IllegalArgumentException("borderColor is null.");
        }

        setBorderColor(GraphicsUtilities.decodeColor(borderColor));
    }

    public int getCloseTransitionDuration() {
        return closeTransitionDuration;
    }

    public void setCloseTransitionDuration(int closeTransitionDuration) {
        this.closeTransitionDuration = closeTransitionDuration;
    }

    public int getCloseTransitionRate() {
        return closeTransitionRate;
    }

    public void setCloseTransitionRate(int closeTransitionRate) {
        this.closeTransitionRate = closeTransitionRate;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();
        suggestionPopup.close(true);

        return true;
    }

    /**
     * {@link KeyCode#ENTER ENTER} Close the suggestion popup with a 'result' of
     * true.<br>
     * {@link KeyCode#ESCAPE ESCAPE} Close the suggestion popup with a 'result'
     * of false.
     */
    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        SuggestionPopup suggestionPopup = (SuggestionPopup)getComponent();

        switch (keyCode) {
            case Keyboard.KeyCode.ENTER: {
                suggestionPopup.close(true);
                break;
            }

            case Keyboard.KeyCode.ESCAPE: {
                suggestionPopup.close(false);
                break;
            }

            default: {
                break;
            }
        }

        return false;
    }

    @Override
    public void windowOpened(Window window) {
        super.windowOpened(window);

        // Adjust for list size
        SuggestionPopup suggestionPopup = (SuggestionPopup)window;

        int listSize = suggestionPopup.getListSize();
        if (listSize == -1) {
            listViewBorder.setPreferredHeight(-1);
        } else {
            if (!listViewBorder.isPreferredHeightSet()) {
                ListView.ItemRenderer itemRenderer = listView.getItemRenderer();
                int borderHeight = itemRenderer.getPreferredHeight(-1) * listSize + 2;

                if (listViewBorder.getPreferredHeight() > borderHeight) {
                    listViewBorder.setPreferredHeight(borderHeight);
                } else {
                    listViewBorder.setPreferredHeight(-1);
                }
            }
        }

        Display display = window.getDisplay();
        display.getContainerMouseListeners().add(displayMouseListener);

        dropShadowDecorator.setShadowOpacity(DropShadowDecorator.DEFAULT_SHADOW_OPACITY);

        returnFocusToTextInput = true;

        TextInput textInput = suggestionPopup.getTextInput();
        textInput.getComponentStateListeners().add(textInputStateListener);
        textInput.getComponentKeyListeners().add(textInputKeyListener);

        // Size and position the popup
        Point location = textInput.mapPointToAncestor(textInput.getDisplay(), 0, 0);
        window.setLocation(location.x, location.y + textInput.getHeight() - 1);
        window.setMinimumWidth(textInput.getWidth());
        window.setMaximumHeight(display.getHeight() - window.getY());
    }

    @Override
    public void windowCloseVetoed(Window window, Vote reason) {
        if (reason == Vote.DENY
            && closeTransition != null) {
            closeTransition.stop();

            listViewBorder.setEnabled(true);
            closeTransition = null;
        }
    }

    @Override
    public void windowClosed(Window window, Display display, Window owner) {
        display.getContainerMouseListeners().remove(displayMouseListener);
        super.windowClosed(window, display, owner);
    }

    @Override
    public void suggestionDataChanged(SuggestionPopup suggestionPopup,
        List<?> previousSuggestionData) {
        listView.setListData(suggestionPopup.getSuggestionData());
    }

    @Override
    public void suggestionRendererChanged(SuggestionPopup suggestionPopup,
        ListView.ItemRenderer previousSuggestionRenderer) {
        listView.setItemRenderer(suggestionPopup.getSuggestionRenderer());
    }

    @Override
    public void listSizeChanged(SuggestionPopup suggestionPopup, int previousListSize) {
        // No-op
    }

    @Override
    public void selectedIndexChanged(SuggestionPopup suggestionPopup, int previousSelectedIndex) {
        // No-op
    }

    @Override
    public void selectedSuggestionChanged(SuggestionPopup suggestionPopup, Object previousSelectedSuggestion) {
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
            listViewBorder.setEnabled(false);

            closeTransition = new FadeWindowTransition(suggestionPopup,
                closeTransitionDuration, closeTransitionRate,
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

            listViewBorder.setEnabled(true);
            closeTransition = null;
        }
    }

    @Override
    public void suggestionPopupClosed(SuggestionPopup suggestionPopup) {
        suggestionPopup.clearFocusDescendant();

        TextInput textInput = suggestionPopup.getTextInput();
        textInput.getComponentStateListeners().remove(textInputStateListener);
        textInput.getComponentKeyListeners().remove(textInputKeyListener);

        if (returnFocusToTextInput) {
            textInput.requestFocus();
        }

        listViewBorder.setEnabled(true);
        closeTransition = null;
    }
}
